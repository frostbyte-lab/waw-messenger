import { WebSocketServer } from "ws";
import crypto from "node:crypto";

const HEARTBEAT_MS = Number(process.env.REALTIME_HEARTBEAT_MS || 30000);
const PRESENCE_TTL_MS = Number(process.env.REALTIME_PRESENCE_TTL_MS || HEARTBEAT_MS * 2);

function send(socket, event) {
  if (socket.readyState === socket.OPEN) socket.send(JSON.stringify(event));
}

function scoped(socket, message) {
  return message.tenantId === socket.tenantId && message.workspaceId === socket.workspaceId;
}

export function createRealtimeGateway({ server, authorize = async () => true, now = () => Date.now() }) {
  const wss = new WebSocketServer({ server, path: "/realtime" });
  const clients = new Set();
  const presence = new Map();

  function broadcast(event, predicate = () => true) {
    for (const client of clients) if (predicate(client)) send(client, event);
  }

  function removeClient(socket) {
    clients.delete(socket);
    if (socket.userId && presence.delete(`${socket.tenantId}:${socket.workspaceId}:${socket.userId}`)) {
      broadcast({ eventId: crypto.randomUUID(), type: "presence.changed", tenantId: socket.tenantId, workspaceId: socket.workspaceId, userId: socket.userId, state: "offline", occurredAt: new Date().toISOString() }, (peer) => scoped(peer, socket));
    }
  }

  wss.on("connection", (socket) => {
    socket.isAlive = true;
    socket.once("message", async (raw) => {
      let hello;
      try { hello = JSON.parse(raw.toString()); } catch { socket.close(1003, "invalid json"); return; }
      if (hello.type !== "subscribe" || !hello.tenantId || !hello.workspaceId || !hello.userId || !(await authorize(hello))) { socket.close(1008, "authorization failed"); return; }
      socket.tenantId = hello.tenantId; socket.workspaceId = hello.workspaceId; socket.userId = hello.userId; socket.subscribed = true; clients.add(socket);
      send(socket, { eventId: crypto.randomUUID(), type: "realtime.ready", tenantId: socket.tenantId, workspaceId: socket.workspaceId, occurredAt: new Date().toISOString() });
      const key = `${socket.tenantId}:${socket.workspaceId}:${socket.userId}`;
      presence.set(key, now() + PRESENCE_TTL_MS);
      broadcast({ eventId: crypto.randomUUID(), type: "presence.changed", tenantId: socket.tenantId, workspaceId: socket.workspaceId, userId: socket.userId, state: "online", occurredAt: new Date().toISOString() }, (peer) => scoped(peer, socket));
    });

    socket.on("pong", () => { socket.isAlive = true; if (socket.userId) presence.set(`${socket.tenantId}:${socket.workspaceId}:${socket.userId}`, now() + PRESENCE_TTL_MS); });
    socket.on("message", (raw) => {
      if (!socket.subscribed) return;
      let message; try { message = JSON.parse(raw.toString()); } catch { return; }
      if (message.type === "heartbeat") { socket.isAlive = true; presence.set(`${socket.tenantId}:${socket.workspaceId}:${socket.userId}`, now() + PRESENCE_TTL_MS); send(socket, { eventId: crypto.randomUUID(), type: "heartbeat.ack", occurredAt: new Date().toISOString() }); return; }
      if (!["typing", "receipt"].includes(message.type) || message.tenantId !== socket.tenantId || message.workspaceId !== socket.workspaceId) return;
      if (message.type === "typing") {
        broadcast({ eventId: crypto.randomUUID(), type: "typing.changed", tenantId: socket.tenantId, workspaceId: socket.workspaceId, conversationId: message.conversationId, userId: socket.userId, state: message.state === "started" ? "started" : "stopped", occurredAt: new Date().toISOString() }, (peer) => scoped(peer, socket) && peer !== socket);
      } else if (["delivered", "read"].includes(message.state) && message.messageId && message.conversationId) {
        broadcast({ eventId: crypto.randomUUID(), type: "message.receipt", tenantId: socket.tenantId, workspaceId: socket.workspaceId, conversationId: message.conversationId, messageId: message.messageId, userId: socket.userId, state: message.state, occurredAt: new Date().toISOString() }, (peer) => scoped(peer, socket) && peer !== socket);
      }
    });
    socket.on("close", () => removeClient(socket));
    socket.on("error", () => removeClient(socket));
  });

  const timer = setInterval(() => {
    for (const client of clients) {
      if (!client.isAlive) { client.terminate(); continue; }
      client.isAlive = false; client.ping();
      if (client.userId && (presence.get(`${client.tenantId}:${client.workspaceId}:${client.userId}`) || 0) < now()) {
        presence.delete(`${client.tenantId}:${client.workspaceId}:${client.userId}`);
        send(client, { eventId: crypto.randomUUID(), type: "presence.expired", tenantId: client.tenantId, workspaceId: client.workspaceId, userId: client.userId, occurredAt: new Date().toISOString() });
      }
    }
  }, HEARTBEAT_MS);
  timer.unref?.();

  return { wss, clients, presence, close: async () => { clearInterval(timer); for (const client of clients) client.close(); await new Promise((resolve) => wss.close(resolve)); } };
}
