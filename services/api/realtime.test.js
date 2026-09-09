import test from "node:test";
import assert from "node:assert/strict";
import http from "node:http";
import WebSocket from "ws";
import { createRealtimeGateway } from "./realtime.js";

function connect(url, hello) {
  return new Promise((resolve, reject) => {
    const socket = new WebSocket(url);
    socket.once("open", () => socket.send(JSON.stringify(hello)));
    socket.once("message", (raw) => resolve({ socket, first: JSON.parse(raw.toString()) }));
    socket.once("error", reject);
  });
}

test("realtime scopes events to tenant/workspace", async () => {
  const server = http.createServer();
  const gateway = createRealtimeGateway({ server, authorize: async (hello) => hello.userId !== "denied", now: () => 1000 });
  await new Promise((resolve) => server.listen(0, resolve));
  const url = `ws://127.0.0.1:${server.address().port}/realtime`;
  const a = await connect(url, { type: "subscribe", tenantId: "tenant-a", workspaceId: "workspace-a", userId: "user-a" });
  const b = await connect(url, { type: "subscribe", tenantId: "tenant-b", workspaceId: "workspace-b", userId: "user-b" });
  const received = [];
  b.socket.on("message", (raw) => received.push(JSON.parse(raw.toString())));
  a.socket.send(JSON.stringify({ type: "typing", tenantId: "tenant-a", workspaceId: "workspace-a", conversationId: "c1", state: "started" }));
  await new Promise((resolve) => setTimeout(resolve, 50));
  assert.equal(received.some((event) => event.type === "typing.changed"), false);
  a.socket.close(); b.socket.close(); await gateway.close(); await new Promise((resolve) => server.close(resolve));
});

test("realtime accepts receipt only in subscribed scope", async () => {
  const server = http.createServer();
  const gateway = createRealtimeGateway({ server, authorize: async () => true });
  await new Promise((resolve) => server.listen(0, resolve));
  const url = `ws://127.0.0.1:${server.address().port}/realtime`;
  const a = await connect(url, { type: "subscribe", tenantId: "tenant-a", workspaceId: "workspace-a", userId: "user-a" });
  const b = await connect(url, { type: "subscribe", tenantId: "tenant-a", workspaceId: "workspace-a", userId: "user-b" });
  const events = []; b.socket.on("message", (raw) => events.push(JSON.parse(raw.toString())));
  a.socket.send(JSON.stringify({ type: "receipt", tenantId: "tenant-a", workspaceId: "workspace-a", conversationId: "c1", messageId: "m1", state: "read" }));
  await new Promise((resolve) => setTimeout(resolve, 50));
  assert.equal(events.some((event) => event.type === "message.receipt" && event.messageId === "m1"), true);
  a.socket.close(); b.socket.close(); await gateway.close(); await new Promise((resolve) => server.close(resolve));
});
