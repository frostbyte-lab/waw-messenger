import test from "node:test";
import assert from "node:assert/strict";
import crypto from "node:crypto";
import { createServer, MemoryStore } from "./server.js";

async function runningServer(options = {}) {
  const server = createServer(options);
  await new Promise((resolve) => server.listen(0, resolve));
  const port = server.address().port;
  return { server, url: `http://127.0.0.1:${port}` };
}

async function close(server) { await new Promise((resolve) => server.close(resolve)); }

test("internal message requires tenant and workspace context", async () => {
  const { server, url } = await runningServer({ store: new MemoryStore() });
  try {
    const response = await fetch(`${url}/v1/internal/messages`, { method: "POST", headers: { "content-type": "application/json" }, body: JSON.stringify({ channel: "waw_internal", text: "hello" }) });
    assert.equal(response.status, 400);
    assert.equal((await response.json()).error.code, "TENANT_CONTEXT_REQUIRED");
  } finally { await close(server); }
});

test("Meta webhook rejects invalid signature and de-duplicates valid event", async () => {
  const secret = "test-meta-secret";
  const store = new MemoryStore();
  const { server, url } = await runningServer({ store, metaAppSecret: secret, metaVerifyToken: "verify-me" });
  const payload = JSON.stringify({ entry: [{ changes: [{ value: { messages: [{ id: "wamid.TEST-1" }] } }] }] });
  try {
    let response = await fetch(`${url}/v1/webhooks/whatsapp`, { method: "POST", headers: { "content-type": "application/json", "x-hub-signature-256": "sha256=invalid" }, body: payload });
    assert.equal(response.status, 401);
    const signature = crypto.createHmac("sha256", secret).update(payload).digest("hex");
    const headers = { "content-type": "application/json", "x-tenant-id": crypto.randomUUID(), "x-hub-signature-256": `sha256=${signature}` };
    response = await fetch(`${url}/v1/webhooks/whatsapp`, { method: "POST", headers, body: payload });
    assert.equal(response.status, 202);
    response = await fetch(`${url}/v1/webhooks/whatsapp`, { method: "POST", headers, body: payload });
    assert.equal(response.status, 200);
    assert.equal((await response.json()).duplicate, true);
    assert.equal(store.webhooks.size, 1);
  } finally { await close(server); }
});

test("internal channel cannot be impersonated as WhatsApp Business", async () => {
  const store = new MemoryStore();
  const { server, url } = await runningServer({ store });
  try {
    const headers = { "content-type": "application/json", "x-tenant-id": crypto.randomUUID(), "x-workspace-id": crypto.randomUUID() };
    const response = await fetch(`${url}/v1/internal/messages`, { method: "POST", headers, body: JSON.stringify({ channel: "whatsapp_business", text: "wrong boundary" }) });
    assert.equal(response.status, 422);
    assert.equal(store.messages.length, 0);
  } finally { await close(server); }
});
