const { app, BrowserWindow, ipcMain } = require("electron");
const path = require("node:path");
const WebSocket = require("ws");

let windowRef;
let socket;

function createWindow() {
  windowRef = new BrowserWindow({
    width: 1240,
    height: 820,
    minWidth: 980,
    minHeight: 680,
    backgroundColor: "#07110f",
    title: "WAW Control Center",
    webPreferences: {
      preload: path.join(__dirname, "preload.cjs"),
      contextIsolation: true,
      nodeIntegration: false,
    },
  });
  windowRef.loadFile(path.join(__dirname, "index.html"));
}

function notify(type, payload = {}) {
  if (windowRef && !windowRef.isDestroyed()) windowRef.webContents.send("remote:event", { type, ...payload });
}

ipcMain.handle("remote:connect", (_event, { relayUrl, otp }) => {
  if (!relayUrl?.startsWith("wss://")) throw new Error("Relay harus memakai wss://");
  if (!/^\d{6}$/.test(otp || "")) throw new Error("OTP harus terdiri dari 6 digit");
  socket?.close();
  socket = new WebSocket(relayUrl);
  socket.on("open", () => {
    socket.send(JSON.stringify({ type: "viewer", code: otp }));
    notify("status", { status: "PAIRING" });
  });
  socket.on("message", (raw) => {
    try { notify("message", { message: JSON.parse(raw.toString()) }); }
    catch { notify("status", { status: "INVALID_MESSAGE" }); }
  });
  socket.on("close", () => notify("status", { status: "DISCONNECTED" }));
  socket.on("error", (error) => notify("error", { message: error.message }));
  return true;
});

ipcMain.handle("remote:send", (_event, message) => {
  if (socket?.readyState === WebSocket.OPEN) socket.send(JSON.stringify(message));
  return true;
});

ipcMain.handle("remote:disconnect", () => {
  if (socket) {
    try { socket.send(JSON.stringify({ type: "disconnect" })); } catch {}
    socket.close();
    socket = undefined;
  }
  notify("status", { status: "DISCONNECTED" });
  return true;
});

app.whenReady().then(() => {
  createWindow();
  app.on("activate", () => { if (BrowserWindow.getAllWindows().length === 0) createWindow(); });
});
app.on("window-all-closed", () => { if (process.platform !== "darwin") app.quit(); });
app.on("before-quit", () => { try { socket?.close(); } catch {} });
