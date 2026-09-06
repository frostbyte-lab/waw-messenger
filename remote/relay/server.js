import { WebSocketServer } from "ws";
import crypto from "node:crypto";

const port = Number(process.env.PORT || 8787);
const ttlMs = Number(process.env.PAIRING_TTL_MS || 120000);
const maxPayload = 8 * 1024 * 1024;
const sessions = new Map();

const wss = new WebSocketServer({ port, maxPayload });
const json = (value) => JSON.stringify(value);
const send = (socket, value) => { if (socket.readyState === socket.OPEN) socket.send(json(value)); };
const close = (socket, code, reason) => { try { socket.close(code, reason); } catch {} };

function remove(session) {
  clearTimeout(session.timer);
  sessions.delete(session.code);
  for (const peer of [session.host, session.viewer]) if (peer) close(peer, 1000, "session closed");
}

wss.on("connection", (socket) => {
  socket.once("message", (raw) => {
    let hello;
    try { hello = JSON.parse(raw.toString()); } catch { return close(socket, 1003, "invalid json"); }
    if (hello.type === "host" && /^[0-9]{6}$/.test(hello.code)) {
      if (sessions.has(hello.code)) return close(socket, 1008, "code in use");
      const session = { code: hello.code, host: socket, viewer: null, id: crypto.randomUUID(), timer: null };
      session.timer = setTimeout(() => remove(session), ttlMs);
      sessions.set(hello.code, session);
      socket.session = session;
      socket.role = "host";
      return send(socket, { type: "host-ready", sessionId: session.id, expiresAt: Date.now() + ttlMs });
    }
    if (hello.type === "viewer" && /^[0-9]{6}$/.test(hello.code)) {
      const session = sessions.get(hello.code);
      if (!session || session.viewer) return close(socket, 1008, "pairing unavailable");
      session.viewer = socket;
      socket.session = session;
      socket.role = "viewer";
      send(session.host, { type: "pair-request", sessionId: session.id });
      return send(socket, { type: "viewer-ready", sessionId: session.id });
    }
    close(socket, 1008, "pairing required");
  });

  socket.on("message", (raw) => {
    if (!socket.session) return;
    let message;
    try { message = JSON.parse(raw.toString()); } catch { return close(socket, 1003, "invalid json"); }
    if (message.type === "approve" && socket.role === "host") {
      send(socket.session.viewer, { type: "approved", sessionId: socket.session.id });
      return;
    }
    if (message.type === "disconnect") return remove(socket.session);
    const target = socket.role === "host" ? socket.session.viewer : socket.session.host;
    if (target) send(target, message);
  });
  socket.on("close", () => { if (socket.session) remove(socket.session); });
});

console.log(`WAW relay listening on :${port}; terminate TLS at a trusted reverse proxy and expose only wss.`);
