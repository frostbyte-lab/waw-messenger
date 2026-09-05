const enc = new TextEncoder();

function hex(buffer) {
  return [...new Uint8Array(buffer)].map((b) => b.toString(16).padStart(2, "0")).join("");
}

async function hmacSha256(secret, value) {
  const key = await crypto.subtle.importKey(
    "raw",
    enc.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );
  return key;
}

export async function verifyMetaSignature(request, env, rawBody) {
  const appSecret = String(env.META_APP_SECRET || "");
  const signature = request.headers.get("X-Hub-Signature-256") || "";
  if (!appSecret || !signature.startsWith("sha256=")) return false;
  const expected = signature.slice("sha256=".length);
  const key = await hmacSha256(appSecret, rawBody);
  const data = await crypto.subtle.sign("HMAC", key, enc.encode(rawBody));
  return expected === hex(data);
}

export async function verifyMetaWebhook(request, env) {
  const url = new URL(request.url);
  const mode = url.searchParams.get("hub.mode");
  const token = url.searchParams.get("hub.verify_token");
  const challenge = url.searchParams.get("hub.challenge");
  if (mode !== "subscribe" || !challenge || token !== String(env.META_VERIFY_TOKEN || "")) {
    return new Response("Forbidden", { status: 403 });
  }
  return new Response(challenge, { status: 200 });
}

export async function handleMetaWebhook(request, env) {
  const rawBody = await request.text();
  if (!(await verifyMetaSignature(request, env, rawBody))) {
    return new Response("Invalid webhook signature", { status: 401 });
  }

  let payload;
  try {
    payload = JSON.parse(rawBody);
  } catch {
    return new Response("Invalid JSON", { status: 400 });
  }

  const events = [];
  for (const entry of payload.entry || []) {
    for (const change of entry.changes || []) {
      const value = change.value || {};
      for (const message of value.messages || []) {
        events.push({
          id: message.id || crypto.randomUUID(),
          kind: "message",
          waId: message.from || null,
          messageType: message.type || "unknown",
          payload: message,
          receivedAt: Date.now()
        });
      }
      for (const status of value.statuses || []) {
        events.push({
          id: status.id || crypto.randomUUID(),
          kind: "status",
          waId: status.recipient_id || null,
          messageType: status.status || "unknown",
          payload: status,
          receivedAt: Date.now()
        });
      }
      for (const call of value.calls || []) {
        events.push({
          id: call.id || crypto.randomUUID(),
          kind: "call",
          waId: call.from || call.to || null,
          messageType: call.event || "unknown",
          payload: call,
          receivedAt: Date.now()
        });
      }
    }
  }

  for (const event of events) {
    await env.DB.prepare(
      `INSERT OR IGNORE INTO meta_events
       (id,kind,wa_id,event_type,payload_json,received_at)
       VALUES (?,?,?,?,?,?)`
    ).bind(
      event.id,
      event.kind,
      event.waId,
      event.messageType,
      JSON.stringify(event.payload),
      event.receivedAt
    ).run();
  }

  return new Response("EVENT_RECEIVED", { status: 200 });
}

export async function sendMetaText(env, to, body) {
  const version = String(env.META_GRAPH_VERSION || "v23.0");
  const phoneNumberId = String(env.META_PHONE_NUMBER_ID || "");
  const accessToken = String(env.META_ACCESS_TOKEN || "");
  if (!phoneNumberId || !accessToken) throw new Error("META_CLOUD_API_NOT_CONFIGURED");
  if (!/^\+?[1-9]\d{7,14}$/.test(String(to))) throw new Error("INVALID_DESTINATION");
  if (!String(body || "").trim() || String(body).length > 4096) throw new Error("INVALID_MESSAGE");

  const response = await fetch(`https://graph.facebook.com/${version}/${phoneNumberId}/messages`, {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${accessToken}`,
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      messaging_product: "whatsapp",
      recipient_type: "individual",
      to: String(to).replace(/^\+/, ""),
      type: "text",
      text: { preview_url: false, body: String(body).trim() }
    })
  });

  const result = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(`META_GRAPH_${response.status}`);
  }
  return result;
}
