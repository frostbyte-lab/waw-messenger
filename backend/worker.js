import { handleMetaWebhook, sendMetaText, verifyMetaWebhook } from "./meta.js";

const json = (data, status = 200) => Response.json(data, {
  status,
  headers: {
    "Cache-Control": "no-store",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Hub-Signature-256",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS"
  }
});

const enc = new TextEncoder();

function id(prefix) { return `${prefix}_${crypto.randomUUID()}`; }
function hex(buffer) { return [...new Uint8Array(buffer)].map(b => b.toString(16).padStart(2, "0")).join(""); }
async function sha256(value) { return hex(await crypto.subtle.digest("SHA-256", enc.encode(value))); }
function token(bytes) {
  let binary = "";
  for (const b of bytes) binary += String.fromCharCode(b);
  return btoa(binary).replaceAll("+", "-").replaceAll("/", "_").replaceAll("=", "");
}

async function passwordHash(password, salt) {
  const key = await crypto.subtle.importKey("raw", enc.encode(password), "PBKDF2", false, ["deriveBits"]);
  const bits = await crypto.subtle.deriveBits(
    { name: "PBKDF2", salt: enc.encode(salt), iterations: 210000, hash: "SHA-256" },
    key,
    256
  );
  return hex(bits);
}

async function newPassword(password) {
  const salt = token(crypto.getRandomValues(new Uint8Array(16)));
  return { salt, hash: await passwordHash(password, salt) };
}

async function body(request) {
  try { return await request.json(); } catch { return null; }
}

async function createSession(env, userId) {
  const raw = token(crypto.getRandomValues(new Uint8Array(32)));
  const now = Date.now();
  const expiresAt = now + 30 * 24 * 60 * 60 * 1000;
  await env.DB.prepare(
    "INSERT INTO sessions (id,user_id,token_hash,expires_at,created_at) VALUES (?,?,?,?,?)"
  ).bind(id("ses"), userId, await sha256(raw), expiresAt, now).run();
  return { token: raw, expiresAt };
}

async function currentUser(request, env) {
  const auth = request.headers.get("Authorization") || "";
  if (!auth.startsWith("Bearer ")) return null;
  const raw = auth.slice(7).trim();
  if (!raw) return null;
  return await env.DB.prepare(
    `SELECT u.id,u.username,u.email,u.display_name,u.avatar_url,s.id AS session_id
     FROM sessions s JOIN users u ON u.id=s.user_id
     WHERE s.token_hash=? AND s.expires_at>? AND s.revoked_at IS NULL LIMIT 1`
  ).bind(await sha256(raw), Date.now()).first();
}

async function register(request, env) {
  const data = await body(request);
  if (!data) return json({ error: "INVALID_JSON" }, 400);

  const username = String(data.username || "").trim().toLowerCase();
  const email = String(data.email || "").trim().toLowerCase();
  const password = String(data.password || "");
  const displayName = String(data.displayName || username).trim();

  if (!/^[a-z0-9_]{3,32}$/.test(username)) return json({ error: "INVALID_USERNAME" }, 400);
  if (!/^\S+@\S+\.\S+$/.test(email)) return json({ error: "INVALID_EMAIL" }, 400);
  if (password.length < 8 || password.length > 128) return json({ error: "INVALID_PASSWORD" }, 400);
  if (!displayName || displayName.length > 80) return json({ error: "INVALID_DISPLAY_NAME" }, 400);

  const exists = await env.DB.prepare("SELECT id FROM users WHERE username=? OR email=? LIMIT 1").bind(username, email).first();
  if (exists) return json({ error: "USER_ALREADY_EXISTS" }, 409);

  const { hash, salt } = await newPassword(password);
  const userId = id("usr");
  const now = Date.now();

  try {
    await env.DB.prepare(
      `INSERT INTO users (id,username,email,password_hash,password_salt,display_name,created_at,updated_at)
       VALUES (?,?,?,?,?,?,?,?)`
    ).bind(userId, username, email, hash, salt, displayName, now, now).run();
  } catch (error) {
    if (String(error).includes("UNIQUE")) return json({ error: "USER_ALREADY_EXISTS" }, 409);
    throw error;
  }

  return json({
    user: { id: userId, username, email, displayName },
    session: await createSession(env, userId)
  }, 201);
}

async function login(request, env) {
  const data = await body(request);
  if (!data) return json({ error: "INVALID_JSON" }, 400);

  const identifier = String(data.identifier || data.username || data.email || "").trim().toLowerCase();
  const password = String(data.password || "");
  if (!identifier || !password) return json({ error: "INVALID_CREDENTIALS" }, 400);

  const user = await env.DB.prepare(
    `SELECT id,username,email,password_hash,password_salt,display_name,avatar_url
     FROM users WHERE username=? OR email=? LIMIT 1`
  ).bind(identifier, identifier).first();

  if (!user || !user.password_salt) return json({ error: "INVALID_CREDENTIALS" }, 401);
  if (await passwordHash(password, user.password_salt) !== user.password_hash) {
    return json({ error: "INVALID_CREDENTIALS" }, 401);
  }

  await env.DB.prepare("UPDATE users SET status='online',updated_at=? WHERE id=?").bind(Date.now(), user.id).run();
  return json({
    user: { id: user.id, username: user.username, email: user.email, displayName: user.display_name, avatarUrl: user.avatar_url || null },
    session: await createSession(env, user.id)
  });
}

async function logout(request, env) {
  const auth = request.headers.get("Authorization") || "";
  if (auth.startsWith("Bearer ")) {
    await env.DB.prepare("UPDATE sessions SET revoked_at=? WHERE token_hash=?")
      .bind(Date.now(), await sha256(auth.slice(7).trim())).run();
  }
  return json({ ok: true });
}

async function me(request, env) {
  const user = await currentUser(request, env);
  if (!user) return json({ error: "UNAUTHORIZED" }, 401);
  return json({ user: {
    id: user.id,
    username: user.username,
    email: user.email,
    displayName: user.display_name,
    avatarUrl: user.avatar_url || null
  }});
}

async function listConversations(request, env) {
  const user = await currentUser(request, env);
  if (!user) return json({ error: "UNAUTHORIZED" }, 401);
  const rows = await env.DB.prepare(
    `SELECT c.id,c.type,c.created_at,c.updated_at,
            u.id AS participant_id,u.username,u.display_name,u.avatar_url,
            (SELECT text FROM messages m WHERE m.conversation_id=c.id ORDER BY m.created_at DESC LIMIT 1) AS last_message,
            (SELECT COUNT(*) FROM messages m WHERE m.conversation_id=c.id AND m.sender_id<>? AND m.status<>'READ') AS unread_count
     FROM conversations c
     JOIN conversation_members cm ON cm.conversation_id=c.id AND cm.user_id=?
     JOIN conversation_members other_cm ON other_cm.conversation_id=c.id AND other_cm.user_id<>cm.user_id
     JOIN users u ON u.id=other_cm.user_id
     ORDER BY c.updated_at DESC`
  ).bind(user.id, user.id).all();
  return json({ conversations: rows.results || [] });
}

async function createDirectConversation(request, env) {
  const user = await currentUser(request, env);
  if (!user) return json({ error: "UNAUTHORIZED" }, 401);
  const data = await body(request);
  const participantId = String(data?.participantId || "").trim();
  if (!participantId || participantId === user.id) return json({ error: "INVALID_PARTICIPANT" }, 400);
  const participant = await env.DB.prepare("SELECT id FROM users WHERE id=? LIMIT 1").bind(participantId).first();
  if (!participant) return json({ error: "USER_NOT_FOUND" }, 404);
  const existing = await env.DB.prepare(
    `SELECT c.id FROM conversations c
     JOIN conversation_members a ON a.conversation_id=c.id AND a.user_id=?
     JOIN conversation_members b ON b.conversation_id=c.id AND b.user_id=?
     WHERE c.type='direct' LIMIT 1`
  ).bind(user.id, participantId).first();
  if (existing) return json({ conversationId: existing.id });

  const conversationId = id("conv");
  const now = Date.now();
  await env.DB.batch([
    env.DB.prepare("INSERT INTO conversations (id,type,created_at,updated_at) VALUES (?,'direct',?,?)").bind(conversationId, now, now),
    env.DB.prepare("INSERT INTO conversation_members (conversation_id,user_id,joined_at) VALUES (?,?,?)").bind(conversationId, user.id, now),
    env.DB.prepare("INSERT INTO conversation_members (conversation_id,user_id,joined_at) VALUES (?,?,?)").bind(conversationId, participantId, now)
  ]);
  return json({ conversationId }, 201);
}

async function conversationMessages(request, env, conversationId) {
  const user = await currentUser(request, env);
  if (!user) return json({ error: "UNAUTHORIZED" }, 401);
  const member = await env.DB.prepare("SELECT 1 FROM conversation_members WHERE conversation_id=? AND user_id=? LIMIT 1")
    .bind(conversationId, user.id).first();
  if (!member) return json({ error: "FORBIDDEN_CONVERSATION" }, 403);
  const url = new URL(request.url);
  const limit = Math.min(100, Math.max(1, Number(url.searchParams.get("limit") || 50)));
  const rows = await env.DB.prepare(
    `SELECT id,conversation_id,sender_id,client_id,text,status,created_at,updated_at,deleted_at
     FROM messages WHERE conversation_id=? ORDER BY created_at DESC LIMIT ?`
  ).bind(conversationId, limit).all();
  return json({ messages: (rows.results || []).reverse() });
}

export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === "OPTIONS") return new Response(null, {
      status: 204,
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Hub-Signature-256",
        "Access-Control-Allow-Methods": "GET, POST, OPTIONS"
      }
    });

    if (url.pathname === "/meta/webhook" && request.method === "GET") return verifyMetaWebhook(request, env);
    if (url.pathname === "/meta/webhook" && request.method === "POST") return handleMetaWebhook(request, env);

    if (url.pathname === "/health") return json({ ok: true, service: "waw-chat", metaCloudApi: Boolean(env.META_PHONE_NUMBER_ID && env.META_ACCESS_TOKEN) });
    if (url.pathname === "/auth/register" && request.method === "POST") return register(request, env);
    if (url.pathname === "/auth/login" && request.method === "POST") return login(request, env);
    if (url.pathname === "/auth/logout" && request.method === "POST") return logout(request, env);
    if (url.pathname === "/auth/me" && request.method === "GET") return me(request, env);
    if (url.pathname === "/conversations" && request.method === "GET") return listConversations(request, env);
    if (url.pathname === "/conversations/direct" && request.method === "POST") return createDirectConversation(request, env);
    if (url.pathname.startsWith("/conversations/") && url.pathname.endsWith("/messages") && request.method === "GET") {
      const conversationId = url.pathname.slice("/conversations/".length, -"/messages".length).replace(/\/$/, "");
      return conversationMessages(request, env, conversationId);
    }

    if (url.pathname === "/meta/send-text" && request.method === "POST") {
      const user = await currentUser(request, env);
      if (!user) return json({ error: "UNAUTHORIZED" }, 401);
      const data = await body(request);
      if (!data) return json({ error: "INVALID_JSON" }, 400);
      try {
        const result = await sendMetaText(env, data.to, data.body);
        return json({ ok: true, result });
      } catch (error) {
        return json({ error: String(error?.message || "META_SEND_FAILED") }, 502);
      }
    }

    if (url.pathname === "/ws") {
      if (request.headers.get("Upgrade") !== "websocket") return new Response("WebSocket required", { status: 426 });
      const user = await currentUser(request, env);
      if (!user) return json({ error: "UNAUTHORIZED" }, 401);

      const pair = new WebSocketPair();
      const [client, server] = Object.values(pair);
      server.accept();
      server.send(JSON.stringify({ type: "connected", userId: user.id }));
      server.addEventListener("message", async event => {
        try {
          const message = JSON.parse(event.data);
          if (message.type !== "message" || !message.conversationId || !String(message.text || "").trim()) return;
          const member = await env.DB.prepare(
            "SELECT 1 FROM conversation_members WHERE conversation_id=? AND user_id=? LIMIT 1"
          ).bind(message.conversationId, user.id).first();
          if (!member) return server.send(JSON.stringify({ type: "error", error: "FORBIDDEN_CONVERSATION" }));

          const now = Date.now();
          const messageId = id("msg");
          const text = String(message.text).trim();
          if (text.length > 4096) return server.send(JSON.stringify({ type: "error", error: "MESSAGE_TOO_LONG" }));
          await env.DB.prepare(
            `INSERT INTO messages (id,conversation_id,sender_id,client_id,text,status,created_at,updated_at)
             VALUES (?,?,?,?,?,'SENT',?,?)`
          ).bind(messageId, message.conversationId, user.id, message.clientId || null, text, now, now).run();

          server.send(JSON.stringify({ type: "message", id: messageId, conversationId: message.conversationId,
            senderId: user.id, clientId: message.clientId || null, text, status: "SENT", createdAt: now }));
        } catch { server.send(JSON.stringify({ type: "error", error: "INVALID_MESSAGE" })); }
      });
      return new Response(null, { status: 101, webSocket: client });
    }

    return new Response("WAW Chat Server");
  }
};
