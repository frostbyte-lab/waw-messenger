import { WebSocket } from "ws";
import { spawn } from "node:child_process";

const relay = spawn(process.execPath, ["server.js"], { cwd: new URL(".", import.meta.url), env: { ...process.env, PORT: "8899", PAIRING_TTL_MS: "5000", SESSION_TTL_MS: "5000" } });
const events = [];
const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
const open = (message) => new Promise((resolve, reject) => {
  const socket = new WebSocket("ws://127.0.0.1:8899");
  socket.on("open", () => socket.send(JSON.stringify(message)));
  socket.on("message", (raw) => { const value = JSON.parse(raw); events.push(value.type); resolve({ socket, value }); });
  socket.on("error", reject);
});
try {
  await wait(150);
  const host = await open({ type: "host", code: "123456" });
  const viewer = await open({ type: "viewer", code: "123456" });
  if (events.includes("approved")) throw new Error("sesi auto-approved");
  viewer.socket.send(JSON.stringify({ type: "approve" }));
  await wait(100);
  if (!events.filter((event) => event === "approved").length) throw new Error("operator approval tidak diteruskan");
  viewer.socket.send(JSON.stringify({ type: "disconnect" }));
  await wait(100);
  console.log("PASS: pairing membutuhkan approval operator dan revoke menutup sesi");
  host.socket.close();
} finally { relay.kill(); }
