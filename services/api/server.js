import http from "node:http";
import crypto from "node:crypto";

const PORT = Number(process.env.PORT || 8788);
const MAX_WEBHOOK_BYTES = Number(process.env.MAX_WEBHOOK_BYTES || 1024 * 1024);

export class MemoryStore {
  constructor() {
    this.messages = [];
    this.audit = [];
    this.webhooks = new Map();
  }
  addAudit(record) { this.audit.push(record); }
  addMessage(message) { this.messages.push(message); return message; }
  getWebhook(key) { return this.webhooks.get(key); }
  saveWebhook(key, event) { this.webhooks.set(key, event); return event; }
}

function json(res, status, body) {
  res.writeHead(status, { "content-type": "application/json; charset=utf-8", "cache-control": "no-store" });
  res.end(JSON.stringify(body));
}

function requestContext(req) {
  return {
    tenantId: req.headers["x-tenant-id"],
    workspaceId: req.headers["x-workspace-id"],
    actorId: req.headers["x-actor-id"] || "service",
    traceId: req.headers["x-trace-id"] || crypto.randomUUID()
  };
}

function validUuid(value) {
  return typeof value === "string" && /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(value);
}

function readBody(req, limit) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    req.on("data", (chunk) => {
      size += chunk.length;
      if (size > limit) { reject(Object.assign(new Error("body_too_large"), { status: 413 })); req.destroy(); return; }
      chunks.push(chunk);
    });
    req.on("end", () => resolve(Buffer.concat(chunks)));
    req.on("error", reject);
  });
}

function verifyMetaSignature(raw, signature, secret) {
  if (!secret || typeof signature !== "string" || !signature.startsWith("sha256=")) return false;
  const expected = crypto.createHmac("sha256", secret).update(raw).digest("hex");
  const supplied = signature.slice("sha256=".length);
  return supplied.length === expected.length && crypto.timingSafeEqual(Buffer.from(supplied), Buffer.from(expected));
}

function audit(store, context, action, result, reasonCode = null) {
  store.addAudit({ tenantId: context.tenantId || null, workspaceId: context.workspaceId || null, actorId: context.actorId, action, result, reasonCode, traceId: context.traceId, occurredAt: new Date().toISOString() });
}

export function createServer({ store = new MemoryStore(), metaAppSecret = process.env.META_APP_SECRET, metaVerifyToken = process.env.META_VERIFY_TOKEN } = {}) {
  return http.createServer(async (req, res) => {
    const url = new URL(req.url, "http://localhost");
    if (req.method === "GET" && url.pathname === "/healthz") return json(res, 200, { ok: true, service: "waw-hybrid-api" });
    if (req.method === "GET" && url.pathname === "/readyz") return json(res, process.env.DATABASE_URL ? 200 : 503, { ok: Boolean(process.env.DATABASE_URL), database: Boolean(process.env.DATABASE_URL) });

    if (url.pathname === "/v1/webhooks/whatsapp" && req.method === "GET") {
      const mode = url.searchParams.get("hub.mode");
      const token = url.searchParams.get("hub.verify_token");
      const challenge = url.searchParams.get("hub.challenge");
      if (mode === "subscribe" && token && token === metaVerifyToken && challenge) return json(res, 200, { challenge });
      return json(res, 403, { error: { code: "WEBHOOK_VERIFY_FAILED" } });
    }

    if (url.pathname === "/v1/webhooks/whatsapp" && req.method === "POST") {
      const context = requestContext(req);
      let raw;
      try { raw = await readBody(req, MAX_WEBHOOK_BYTES); } catch (error) { audit(store, context, "webhook.receive", "denied", error.message); return json(res, error.status || 400, { error: { code: error.message } }); }
      if (!verifyMetaSignature(raw, req.headers["x-hub-signature-256"], metaAppSecret)) { audit(store, context, "webhook.receive", "denied", "invalid_signature"); return json(res, 401, { error: { code: "INVALID_SIGNATURE" } }); }
      let payload;
      try { payload = JSON.parse(raw); } catch { audit(store, context, "webhook.receive", "denied", "invalid_json"); return json(res, 400, { error: { code: "INVALID_JSON" } }); }
      const entry = payload?.entry?.[0];
      const change = entry?.changes?.[0];
      const value = change?.value || {};
      const event = value.messages?.[0] || value.statuses?.[0];
      const externalId = event?.id;
      if (!externalId) { audit(store, context, "webhook.receive", "failed", "missing_external_id"); return json(res, 422, { error: { code: "MISSING_EXTERNAL_ID" } }); }
      const eventType = value.messages?.length ? "message.received" : "message.status";
      const key = `whatsapp_business:${externalId}:${eventType}`;
      const existing = store.getWebhook(key);
      if (existing) return json(res, 200, { accepted: true, duplicate: true, eventId: existing.eventId });
      const record = { eventId: crypto.randomUUID(), tenantId: context.tenantId || null, channel: "whatsapp_business", externalId, eventType, signatureVerified: true, receivedAt: new Date().toISOString(), attempt: 1, status: "received" };
      store.saveWebhook(key, record);
      audit(store, context, "webhook.receive", "allowed");
      return json(res, 202, { accepted: true, duplicate: false, eventId: record.eventId });
    }

    if (url.pathname === "/v1/internal/messages" && req.method === "POST") {
      const context = requestContext(req);
      if (!validUuid(context.tenantId) || !validUuid(context.workspaceId)) { audit(store, context, "message.create", "denied", "missing_tenant_context"); return json(res, 400, { error: { code: "TENANT_CONTEXT_REQUIRED", traceId: context.traceId } }); }
      let payload;
      try { payload = JSON.parse(await readBody(req, 1024 * 1024)); } catch { audit(store, context, "message.create", "denied", "invalid_json"); return json(res, 400, { error: { code: "INVALID_JSON", traceId: context.traceId } }); }
      if (payload.channel !== "waw_internal" || typeof payload.text !== "string" || !payload.text.trim()) { audit(store, context, "message.create", "denied", "invalid_internal_message"); return json(res, 422, { error: { code: "INVALID_INTERNAL_MESSAGE", traceId: context.traceId } }); }
      const message = { messageId: crypto.randomUUID(), conversationId: payload.conversationId || null, tenantId: context.tenantId, workspaceId: context.workspaceId, senderId: context.actorId, channel: "waw_internal", kind: "text", text: payload.text.trim(), createdAt: new Date().toISOString() };
      store.addMessage(message); audit(store, context, "message.create", "allowed"); return json(res, 201, message);
    }
    return json(res, 404, { error: { code: "NOT_FOUND" } });
  });
}

if (import.meta.url === `file://${process.argv[1]}`) createServer().listen(PORT, () => console.log(`waw-hybrid-api listening on ${PORT}`));
