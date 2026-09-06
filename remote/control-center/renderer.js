const $ = (selector) => document.querySelector(selector);
const connectPanel = $("#connectPanel");
const sessionPanel = $("#sessionPanel");
const badge = $("#connectionBadge");
const errorBox = $("#error");
const preview = $("#preview");
const frameMeta = $("#frameMeta");
let currentStatus = "DISCONNECTED";

function setStatus(status) {
  currentStatus = status;
  badge.textContent = status.replaceAll("_", " ");
  badge.className = `badge ${status === "CONNECTED" ? "online" : status === "DISCONNECTED" ? "muted" : "pending"}`;
  const connected = status !== "DISCONNECTED" && status !== "CONNECTION_ERROR";
  connectPanel.classList.toggle("hidden", connected);
  sessionPanel.classList.toggle("hidden", !connected);
  $("#liveText").textContent = status === "CONNECTED" ? "LIVE" : status.replaceAll("_", " ");
  $("#sessionSub").textContent = status === "CONNECTED" ? "Connected with explicit user consent" : "Menunggu persetujuan User";
  if (status === "CONNECTED") {
    $("#previewTitle").textContent = "Screen share aktif";
    $("#previewSub").textContent = "Klik preview untuk mengirim kontrol sentuh.";
  }
}

$("#connectButton").addEventListener("click", async () => {
  errorBox.textContent = "";
  try {
    await window.wawRemote.connect({ relayUrl: $("#relayUrl").value.trim(), otp: $("#otp").value.trim() });
  } catch (error) { errorBox.textContent = error.message; }
});

function disconnect() { window.wawRemote.disconnect(); setStatus("DISCONNECTED"); }
$("#disconnectButton").addEventListener("click", disconnect);
$("#stopSession").addEventListener("click", disconnect);

document.querySelectorAll("[data-key]").forEach((button) => {
  button.addEventListener("click", () => window.wawRemote.send({ type: "input-command", inputType: "KEY_DOWN", keyCode: Number(button.dataset.key) }));
});

preview.addEventListener("click", (event) => {
  if (currentStatus !== "CONNECTED") return;
  const rect = preview.getBoundingClientRect();
  window.wawRemote.send({ type: "input-command", inputType: "TOUCH_DOWN", x: event.clientX - rect.left, y: event.clientY - rect.top });
});

window.wawRemote.onEvent(({ type, status, message }) => {
  if (type === "status") setStatus(status);
  if (type === "error") { errorBox.textContent = message; setStatus("CONNECTION_ERROR"); }
  if (type !== "message" || !message) return;
  if (message.type === "viewer-ready") setStatus("WAITING_FOR_USER_APPROVAL");
  if (message.type === "approved") setStatus("CONNECTED");
  if (message.type === "screen-frame" && message.payloadBase64) {
    const image = new Image();
    image.onload = () => { preview.replaceChildren(image); image.className = "remote-image"; frameMeta.textContent = `${message.width || "?"} × ${message.height || "?"} · frame ${message.sequence || 0}`; };
    image.src = `data:image/jpeg;base64,${message.payloadBase64}`;
  }
});

setStatus("DISCONNECTED");
