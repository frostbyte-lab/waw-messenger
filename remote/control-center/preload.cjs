const { contextBridge, ipcRenderer } = require("electron");

contextBridge.exposeInMainWorld("wawRemote", {
  connect: (payload) => ipcRenderer.invoke("remote:connect", payload),
  approve: () => ipcRenderer.invoke("remote:send", { type: "approve" }),
  send: (message) => ipcRenderer.invoke("remote:send", message),
  disconnect: () => ipcRenderer.invoke("remote:disconnect"),
  onEvent: (callback) => ipcRenderer.on("remote:event", (_event, payload) => callback(payload)),
});
