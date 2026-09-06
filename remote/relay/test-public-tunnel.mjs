import WebSocket from "ws";
const url = process.env.WAW_PUBLIC_RELAY;
if (!url?.startsWith("wss://")) throw new Error("WAW_PUBLIC_RELAY must be wss://");
const ws = new WebSocket(url);
const timer = setTimeout(() => { console.error("TIMEOUT"); process.exit(1); }, 10000);
ws.on("open", () => { clearTimeout(timer); console.log("PASS: public WSS handshake established"); ws.close(); });
ws.on("error", (error) => { clearTimeout(timer); console.error(error.message); process.exit(1); });
