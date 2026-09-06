import { WebSocketServer } from "ws";
import crypto from "node:crypto";

const port = Number(process.env.PORT || 8787);
const pairingTtlMs = Number(process.env.PAIRING_TTL_MS || 120000);
const sessionTtlMs = Number(process.env.SESSION_TTL_MS || 8 * 60 * 60 * 1000);
const maxPayload = 8 * 1024 * 1024;
const sessions = new Map();
const wss = new WebSocketServer({ port, maxPayload });

const json = (value) => JSON.stringify(value);
const send = (socket, value) => {
  if (socket && socket.readyState === socket.OPEN) socket.send(json(value));
};
const close = (socket, code, reason) => { try { socket?.close(code, reason); } catch {} };

function remove(session, reason = "session closed") {
  clearTimeout(session.timer);
  sessions.delete(session.code);
  for (const peer of [session.host, session.viewer]) close(peer, 1000, reason);
}

function audit(session, event, extra = {}) {
  console.log(JSON.stringify({ at: new Date().toISOString(), event, sessionId: session.id, ...extra }));
}

function armExpiry(session, ttlMs) {
  clearTimeout(session.timer);
  session.expiresAt = Date.now() + ttlMs;
  session.timer = setTimeout(() => remove(session), ttlMs);
}

function forward(session, role, message) {
  if (!session.userApproved || !session.operatorApproved) return;
  const target = role === "host" ? session.viewer : session.host;
  if (target) send(target, message);
}

wss.on("connection", (socket) => {
  socket.once("message", (raw) => {
    let hello;
    try { hello = JSON.parse(raw.toString()); } catch { return close(socket, 1003, "invalid json"); }

    if (hello.type === "host" && /^[0-9]{6}$/.test(hello.code)) {
      if (sessions.has(hello.code)) return close(socket, 1008, "code in use");
      const session = {
        code: hello.code,
        host: socket,
        viewer: null,
        userApproved: false,
        operatorApproved: false,
        capabilities: [],
        id: crypto.randomUUID(),
        timer: null,
        expiresAt: 0,
      };
      sessions.set(hello.code, session);
      socket.session = session;
      socket.role = "host";
      armExpiry(session, pairingTtlMs);
      audit(session, "pairing-created");
      return send(socket, { type: "host-ready", sessionId: session.id, expiresAt: session.expiresAt });
    }

    if (hello.type === "viewer" && /^[0-9]{6}$/.test(hello.code)) {
      const session = sessions.get(hello.code);
      if (!session || session.expiresAt < Date.now() || session.viewer) return close(socket, 1008, "pairing unavailable");
      session.viewer = socket;
      socket.session = session;
      socket.role = "viewer";
      send(session.host, { type: "pair-request", sessionId: session.id });
      send(socket, { type: "viewer-ready", sessionId: session.id, expiresAt: session.expiresAt });
      if (session.userApproved) send(socket, { type: "user-consent", sessionId: session.id, capabilities: session.capabilities });
      audit(session, "operator-paired");
      return;
    }

    close(socket, 1008, "pairing required");
  });

  socket.on("message", (raw) => {
    const session = socket.session;
    if (!session) return;
    let message;
    try { message = JSON.parse(raw.toString()); } catch { return close(socket, 1003, "invalid json"); }

    if (message.type === "user-consent" && socket.role === "host") {
      session.userApproved = true;
      session.capabilities = Array.isArray(message.capabilities) ? message.capabilities.slice(0, 8) : [];
      audit(session, "user-consent", { capabilities: session.capabilities });
      send(session.viewer, { type: "user-consent", sessionId: session.id, capabilities: session.capabilities });
      return;
    }
    if (message.type === "approve" && socket.role === "viewer") {
      if (!session.userApproved) return send(socket, { type: "approval-rejected", reason: "user consent required" });
      session.operatorApproved = true;
      armExpiry(session, sessionTtlMs);
      send(session.viewer, { type: "approved", sessionId: session.id, expiresAt: session.expiresAt });
      send(session.host, { type: "approved", sessionId: session.id, expiresAt: session.expiresAt });
      audit(session, "operator-approved");
      return;
    }
    if (message.type === "disconnect" || message.type === "revoke") {
      audit(session, "session-revoked", { by: socket.role });
      send(session.host, { type: "revoked", sessionId: session.id });
      send(session.viewer, { type: "revoked", sessionId: session.id });
      return remove(session, "revoked");
    }
    if (message.sessionId && message.sessionId !== session.id) return;
    if (message.type === "input-command" && socket.role === "viewer" && !session.capabilities.includes(message.capability)) return;
    forward(session, socket.role, message);
  });

  socket.on("close", () => { if (socket.session) remove(socket.session); });
  socket.on("error", () => { if (socket.session) remove(socket.session); });
});

console.log(`WAW relay listening on :${port}; terminate TLS at a trusted reverse proxy and expose only wss.`);
