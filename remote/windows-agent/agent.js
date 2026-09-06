import WebSocket from "ws";
import fs from "node:fs";
import readline from "node:readline";

const relay = process.env.WAW_RELAY_URL || "ws://127.0.0.1:8787";
const code = process.env.WAW_PAIRING_CODE;
if (!code || !/^\d{6}$/.test(code)) throw new Error("Set WAW_PAIRING_CODE to the six-digit code shown on Android");
const ws = new WebSocket(relay);
let approved = false;
let sequence = 0;
fs.mkdirSync("captures", { recursive: true });

ws.on("open", () => ws.send(JSON.stringify({ type: "viewer", code })));
ws.on("message", (raw) => {
  const message = JSON.parse(raw.toString());
  if (message.type === "pair-request") return console.log("Android is requesting/awaiting approval");
  if (message.type === "viewer-ready") return console.log(`Paired session ${message.sessionId}; waiting for Android approval`);
  if (message.type === "approved") { approved = true; return console.log("Approved; screen frames will be saved in captures/"); }
  if (message.type === "screen-frame" && approved && message.payloadBase64) {
    const name = `captures/frame-${String(sequence++).padStart(6, "0")}.bin`;
    fs.writeFileSync(name, Buffer.from(message.payloadBase64, "base64"));
    console.log(name);
  }
});
ws.on("close", () => process.exit(0));
ws.on("error", (error) => console.error(error.message));

const input = readline.createInterface({ input: process.stdin, output: process.stdout });
console.log("Commands: approve | touch x y | key keyCode | disconnect");
input.on("line", (line) => {
  if (line === "approve") return ws.send(JSON.stringify({ type: "approve" }));
  if (line === "disconnect") { ws.send(JSON.stringify({ type: "disconnect" })); return ws.close(); }
  const touch = line.match(/^touch\s+([\d.]+)\s+([\d.]+)$/);
  if (touch) return ws.send(JSON.stringify({ type: "input-command", inputType: "TOUCH_DOWN", x: Number(touch[1]), y: Number(touch[2]) }));
  const key = line.match(/^key\s+(\d+)$/);
  if (key) return ws.send(JSON.stringify({ type: "input-command", inputType: "KEY_DOWN", keyCode: Number(key[1]) }));
});
