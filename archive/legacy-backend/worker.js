const json = (data, status = 200) => Response.json(data, {
  status,
  headers: { "Cache-Control": "no-store", "Access-Control-Allow-Origin": "*" }
});
const enc = new TextEncoder();
function id(prefix) { return `${prefix}_${crypto.randomUUID()}`; }
function hex(buffer) { return [...new Uint8Array(buffer)].map(b => b.toString(16).padStart(2, "0")).join(""); }
async function sha256(value) { return hex(await crypto.subtle.digest("SHA-256", enc.encode(value))); }
function token(bytes) { let binary = ""; for (const b of bytes) binary += String.fromCharCode(b); return btoa(binary).replaceAll("+", "-").replaceAll("/", "_").replaceAll("=", ""); }
async function passwordHash(password, salt) {
  const key = await crypto.subtle.importKey("raw", enc.encode(password), "PBKDF2", false, ["deriveBits"]);
  return hex(await crypto.subtle.deriveBits({ name: "PBKDF2", salt: enc.encode(salt), iterations: 100000, hash: "SHA-256" }, key, 256));
}
async function newPassword(password) { const salt = token(crypto.getRandomValues(new Uint8Array(16))); return { salt, hash: await passwordHash(password, salt) }; }
async function body(request) { try { return await request.json(); } catch { return null; } }
const liveSockets = new Map();
function addLiveSocket(userId, socket) {
  const sockets = liveSockets.get(userId) || new Set();
  sockets.add(socket);
  liveSockets.set(userId, sockets);
}
function removeLiveSocket(userId, socket) {
  const sockets = liveSockets.get(userId);
  if (!sockets) return;
  sockets.delete(socket);
  if (!sockets.size) liveSockets.delete(userId);
}
async function broadcastMessage(env, message) {
  const members = await env.DB.prepare("SELECT user_id FROM conversation_members WHERE conversation_id=?").bind(message.conversation_id).all();
  const payload = JSON.stringify({
    type: "message",
    id: message.id,
    conversationId: message.conversation_id,
    senderId: message.sender_id,
    clientId: message.client_id || null,
    text: message.text,
    status: message.status,
    createdAt: message.created_at,
    updatedAt: message.updated_at
  });
  for (const member of members.results || []) {
    for (const socket of liveSockets.get(member.user_id) || []) {
      try { socket.send(payload); } catch { removeLiveSocket(member.user_id, socket); }
    }
  }
}

function authToken(request) { const auth = request.headers.get("Authorization") || ""; return auth.startsWith("Bearer ") ? auth.slice(7).trim() : ""; }
async function createSession(env, userId) {
  const raw = token(crypto.getRandomValues(new Uint8Array(32))); const now = Date.now(); const expiresAt = now + 30 * 24 * 60 * 60 * 1000;
  await env.DB.prepare("INSERT INTO sessions (id,user_id,token_hash,expires_at,created_at) VALUES (?,?,?,?,?)").bind(id("ses"), userId, await sha256(raw), expiresAt, now).run();
  return { token: raw, expiresAt };
}
async function currentUser(request, env) {
  const raw = authToken(request); if (!raw) return null;
  return env.DB.prepare(`SELECT u.id,u.username,u.email,u.display_name,u.avatar_url,s.id AS session_id FROM sessions s JOIN users u ON u.id=s.user_id WHERE s.token_hash=? AND s.expires_at>? AND s.revoked_at IS NULL LIMIT 1`).bind(await sha256(raw), Date.now()).first();
}
async function register(request, env) {
  const data = await body(request); if (!data) return json({ error: "INVALID_JSON" }, 400);
  const username = String(data.username || "").trim().toLowerCase(); const email = String(data.email || "").trim().toLowerCase(); const password = String(data.password || ""); const displayName = String(data.displayName || username).trim();
  if (!/^[a-z0-9_]{3,32}$/.test(username)) return json({ error: "INVALID_USERNAME" }, 400);
  if (!/^\S+@\S+\.\S+$/.test(email)) return json({ error: "INVALID_EMAIL" }, 400);
  if (password.length < 8 || password.length > 128) return json({ error: "INVALID_PASSWORD" }, 400);
  if (!displayName || displayName.length > 80) return json({ error: "INVALID_DISPLAY_NAME" }, 400);
  if (await env.DB.prepare("SELECT id FROM users WHERE username=? OR email=? LIMIT 1").bind(username, email).first()) return json({ error: "USER_ALREADY_EXISTS" }, 409);
  const { hash, salt } = await newPassword(password); const userId = id("usr"); const now = Date.now();
  try {
    await env.DB.prepare(`INSERT INTO users (id,username,email,password_hash,password_salt,display_name,created_at,updated_at) VALUES (?,?,?,?,?,?,?,?)`).bind(userId, username, email, hash, salt, displayName, now, now).run();
  } catch (error) { if (String(error).includes("UNIQUE")) return json({ error: "USER_ALREADY_EXISTS" }, 409); throw error; }
  await env.DB.prepare("UPDATE users SET status='online' WHERE id=?").bind(userId).run();
  return json({ user: { id: userId, username, email, displayName }, session: await createSession(env, userId) }, 201);
}
async function forgotPassword(request, env) {
  const data = await body(request); const generic = { ok: true, message: "Jika akun ditemukan, instruksi pemulihan akan dikirim." }; if (!data) return json(generic);
  const identifier = String(data.identifier || data.email || data.username || "").trim().toLowerCase(); if (!identifier || identifier.length > 128) return json(generic);
  const user = await env.DB.prepare("SELECT id,email,display_name FROM users WHERE username=? OR email=? LIMIT 1").bind(identifier, identifier).first(); if (!user) return json(generic);
  const rawToken = token(crypto.getRandomValues(new Uint8Array(32))); const now = Date.now(); const expiresAt = now + 15 * 60 * 1000;
  await env.DB.prepare("INSERT INTO password_reset_tokens (id,user_id,token_hash,expires_at,created_at) VALUES (?,?,?,?,?)").bind(id("rst"), user.id, await sha256(rawToken), expiresAt, now).run();
  if (env.RESET_EMAIL_WEBHOOK) await fetch(env.RESET_EMAIL_WEBHOOK, { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ to: user.email, displayName: user.display_name, token: rawToken, expiresAt }) }).catch(() => undefined);
  return json(generic);
}
async function resetPassword(request, env) {
  const data = await body(request); if (!data) return json({ error: "INVALID_JSON" }, 400); const rawToken = String(data.token || "").trim(); const password = String(data.password || "");
  if (!rawToken || password.length < 8 || password.length > 128) return json({ error: "INVALID_RESET_REQUEST" }, 400);
  const reset = await env.DB.prepare("SELECT id,user_id FROM password_reset_tokens WHERE token_hash=? AND expires_at>? AND used_at IS NULL LIMIT 1").bind(await sha256(rawToken), Date.now()).first(); if (!reset) return json({ error: "INVALID_OR_EXPIRED_RESET_TOKEN" }, 400);
  const { hash, salt } = await newPassword(password); const now = Date.now();
  await env.DB.batch([
    env.DB.prepare("UPDATE users SET password_hash=?,password_salt=?,updated_at=?,status='offline' WHERE id=?").bind(hash, salt, now, reset.user_id),
    env.DB.prepare("UPDATE password_reset_tokens SET used_at=? WHERE id=? AND used_at IS NULL").bind(now, reset.id),
    env.DB.prepare("UPDATE sessions SET revoked_at=? WHERE user_id=? AND revoked_at IS NULL").bind(now, reset.user_id)
  ]); return json({ ok: true });
}
async function login(request, env) {
  const data = await body(request); if (!data) return json({ error: "INVALID_JSON" }, 400); const identifier = String(data.identifier || data.username || data.email || "").trim().toLowerCase(); const password = String(data.password || "");
  if (!identifier || !password) return json({ error: "INVALID_CREDENTIALS" }, 400);
  const user = await env.DB.prepare(`SELECT id,username,email,password_hash,password_salt,display_name,avatar_url FROM users WHERE username=? OR email=? LIMIT 1`).bind(identifier, identifier).first();
  if (!user || !user.password_salt || await passwordHash(password, user.password_salt) !== user.password_hash) return json({ error: "INVALID_CREDENTIALS" }, 401);
  await env.DB.prepare("UPDATE users SET status='online',updated_at=? WHERE id=?").bind(Date.now(), user.id).run();
  return json({ user: { id: user.id, username: user.username, email: user.email, displayName: user.display_name, avatarUrl: user.avatar_url || null }, session: await createSession(env, user.id) });
}
async function logout(request, env) {
  const raw = authToken(request); if (raw) {
    const hash = await sha256(raw); const session = await env.DB.prepare("SELECT user_id FROM sessions WHERE token_hash=? LIMIT 1").bind(hash).first(); const now = Date.now();
    await env.DB.prepare("UPDATE sessions SET revoked_at=? WHERE token_hash=? AND revoked_at IS NULL").bind(now, hash).run();
    if (session && !await env.DB.prepare("SELECT 1 FROM sessions WHERE user_id=? AND expires_at>? AND revoked_at IS NULL LIMIT 1").bind(session.user_id, now).first()) await env.DB.prepare("UPDATE users SET status='offline',updated_at=? WHERE id=?").bind(now, session.user_id).run();
  } return json({ ok: true });
}
async function me(request, env) { const user = await currentUser(request, env); if (!user) return json({ error: "UNAUTHORIZED" }, 401); return json({ user: { id: user.id, username: user.username, email: user.email, displayName: user.display_name, avatarUrl: user.avatar_url || null } }); }
async function users(request, env) {
  const user = await currentUser(request, env); if (!user) return json({ error: "UNAUTHORIZED" }, 401);
  const result = await env.DB.prepare("SELECT id,username,email,display_name,avatar_url,status FROM users WHERE id<>? ORDER BY username LIMIT 100").bind(user.id).all();
  return json({ users: (result.results || []).map(item => ({ id: item.id, username: item.username, email: item.email, displayName: item.display_name, avatarUrl: item.avatar_url || null, status: item.status })) });
}
async function conversations(request, env) {
  const user = await currentUser(request, env); if (!user) return json({ error: "UNAUTHORIZED" }, 401);
  if (request.method === "GET") {
    const result = await env.DB.prepare(`SELECT c.id,c.type,c.created_at,c.updated_at,u.id AS participant_id,u.username AS participant_username,u.email AS participant_email,u.display_name AS participant_display_name,u.avatar_url AS participant_avatar,u.status AS participant_status,(SELECT m.text FROM messages m WHERE m.conversation_id=c.id ORDER BY m.created_at DESC LIMIT 1) AS last_message,(SELECT COUNT(*) FROM messages m WHERE m.conversation_id=c.id AND m.sender_id<>? AND NOT EXISTS (SELECT 1 FROM message_receipts r WHERE r.message_id=m.id AND r.user_id=? AND r.read_at IS NOT NULL)) AS unread_count FROM conversations c JOIN conversation_members mine ON mine.conversation_id=c.id AND mine.user_id=? JOIN conversation_members other ON other.conversation_id=c.id AND other.user_id<>mine.user_id JOIN users u ON u.id=other.user_id ORDER BY c.updated_at DESC LIMIT 100`).bind(user.id, user.id, user.id).all();
    return json({ conversations: (result.results || []).map(item => ({ id: item.id, type: item.type, createdAt: item.created_at, updatedAt: item.updated_at, lastMessage: item.last_message || "", unreadCount: Number(item.unread_count || 0), participant: { id: item.participant_id, username: item.participant_username, email: item.participant_email, displayName: item.participant_display_name, avatarUrl: item.participant_avatar || null, status: item.participant_status } })) });
  }
  const data = await body(request); const participantId = String(data?.participantId || "").trim(); if (!participantId || participantId === user.id) return json({ error: "INVALID_PARTICIPANT" }, 400);
  if (!await env.DB.prepare("SELECT id FROM users WHERE id=? LIMIT 1").bind(participantId).first()) return json({ error: "USER_NOT_FOUND" }, 404);
  const existing = await env.DB.prepare(`SELECT c.id FROM conversations c JOIN conversation_members a ON a.conversation_id=c.id AND a.user_id=? JOIN conversation_members b ON b.conversation_id=c.id AND b.user_id=? WHERE c.type='direct' LIMIT 1`).bind(user.id, participantId).first();
  if (existing) return json({ conversation: { id: existing.id, existing: true } });
  const conversationId = id("conv"); const now = Date.now();
  await env.DB.batch([
    env.DB.prepare("INSERT INTO conversations (id,type,created_at,updated_at) VALUES (?,?,?,?)").bind(conversationId, "direct", now, now),
    env.DB.prepare("INSERT INTO conversation_members (conversation_id,user_id,joined_at) VALUES (?,?,?)").bind(conversationId, user.id, now),
    env.DB.prepare("INSERT INTO conversation_members (conversation_id,user_id,joined_at) VALUES (?,?,?)").bind(conversationId, participantId, now)
  ]); return json({ conversation: { id: conversationId, type: "direct", createdAt: now, updatedAt: now }, existing: false }, 201);
}
async function conversationMessages(request, env, conversationId) {
  const user = await currentUser(request, env); if (!user) return json({ error: "UNAUTHORIZED" }, 401);
  if (!await env.DB.prepare("SELECT 1 FROM conversation_members WHERE conversation_id=? AND user_id=? LIMIT 1").bind(conversationId, user.id).first()) return json({ error: "FORBIDDEN_CONVERSATION" }, 403);
  const result = await env.DB.prepare("SELECT id,conversation_id,sender_id,client_id,text,status,created_at,updated_at,deleted_at FROM messages WHERE conversation_id=? ORDER BY created_at ASC LIMIT 500").bind(conversationId).all();
  return json({ messages: (result.results || []).map(item => ({ id: item.id, conversationId: item.conversation_id, senderId: item.sender_id, clientId: item.client_id || null, text: item.text, status: item.status, createdAt: item.created_at, updatedAt: item.updated_at, deletedAt: item.deleted_at || null })) });
}
async function markRead(request, env, conversationId) {
  const user = await currentUser(request, env); if (!user) return json({ error: "UNAUTHORIZED" }, 401);
  if (!await env.DB.prepare("SELECT 1 FROM conversation_members WHERE conversation_id=? AND user_id=? LIMIT 1").bind(conversationId, user.id).first()) return json({ error: "FORBIDDEN_CONVERSATION" }, 403);
  const now = Date.now(); const result = await env.DB.prepare(`SELECT m.id FROM messages m WHERE m.conversation_id=? AND m.sender_id<>? AND NOT EXISTS (SELECT 1 FROM message_receipts r WHERE r.message_id=m.id AND r.user_id=? AND r.read_at IS NOT NULL) LIMIT 500`).bind(conversationId, user.id, user.id).all();
  const statements = (result.results || []).map(item => env.DB.prepare(`INSERT INTO message_receipts (message_id,user_id,delivered_at,read_at) VALUES (?,?,?,?) ON CONFLICT(message_id,user_id) DO UPDATE SET delivered_at=COALESCE(message_receipts.delivered_at,excluded.delivered_at),read_at=excluded.read_at`).bind(item.id, user.id, now, now));
  if (statements.length) await env.DB.batch(statements);
  if (result.results?.length) await env.DB.prepare("UPDATE messages SET status='READ',updated_at=? WHERE conversation_id=? AND sender_id<>?").bind(now, conversationId, user.id).run();
  return json({ ok: true, marked: result.results?.length || 0 });
}
async function persistMessage(env, userId, conversationId, clientId, text) {
  if (!conversationId) return { error: "INVALID_CONVERSATION" };
  if (!await env.DB.prepare("SELECT 1 FROM conversation_members WHERE conversation_id=? AND user_id=? LIMIT 1").bind(conversationId, userId).first()) return { error: "FORBIDDEN_CONVERSATION" };
  const clean = String(text || "").trim(); if (!clean || clean.length > 4000) return { error: "INVALID_MESSAGE" };
  const normalizedClientId = String(clientId || "").trim() || null;
  if (normalizedClientId) {
    const existing = await env.DB.prepare("SELECT id,conversation_id,sender_id,client_id,text,status,created_at,updated_at,deleted_at FROM messages WHERE sender_id=? AND client_id=? LIMIT 1").bind(userId, normalizedClientId).first();
    if (existing) return { message: existing };
  }
  const now = Date.now(); const messageId = id("msg");
  try {
    await env.DB.batch([
      env.DB.prepare("INSERT INTO messages (id,conversation_id,sender_id,client_id,text,status,created_at,updated_at) VALUES (?,?,?,?,?,'SENT',?,?)").bind(messageId, conversationId, userId, normalizedClientId, clean, now, now),
      env.DB.prepare("UPDATE conversations SET updated_at=? WHERE id=?").bind(now, conversationId)
    ]);
  } catch (error) {
    if (normalizedClientId && String(error).includes("UNIQUE")) {
      const existing = await env.DB.prepare("SELECT id,conversation_id,sender_id,client_id,text,status,created_at,updated_at,deleted_at FROM messages WHERE sender_id=? AND client_id=? LIMIT 1").bind(userId, normalizedClientId).first();
      if (existing) return { message: existing };
    } throw error;
  }
  return { message: await env.DB.prepare("SELECT id,conversation_id,sender_id,client_id,text,status,created_at,updated_at,deleted_at FROM messages WHERE id=? LIMIT 1").bind(messageId).first() };
}
async function postMessage(request, env, conversationId) {
  const user = await currentUser(request, env); if (!user) return json({ error: "UNAUTHORIZED" }, 401); const data = await body(request);
  const result = await persistMessage(env, user.id, conversationId, data?.clientId, data?.text); if (result.error) return json({ error: result.error }, result.error === "FORBIDDEN_CONVERSATION" ? 403 : 400); await broadcastMessage(env, result.message); return json({ message: result.message }, 201);
}
export default {
  async fetch(request, env) {
    const url = new URL(request.url);
    if (request.method === "OPTIONS") return new Response(null, { status: 204, headers: { "Access-Control-Allow-Origin": "*", "Access-Control-Allow-Headers": "Content-Type, Authorization", "Access-Control-Allow-Methods": "GET, POST, OPTIONS" } });
    if (url.pathname === "/health") return json({ ok: true, service: "waw-chat" });
    if (url.pathname === "/auth/register" && request.method === "POST") return register(request, env);
    if (url.pathname === "/auth/login" && request.method === "POST") return login(request, env);
    if (url.pathname === "/auth/forgot-password" && request.method === "POST") return forgotPassword(request, env);
    if (url.pathname === "/auth/reset-password" && request.method === "POST") return resetPassword(request, env);
    if (url.pathname === "/auth/logout" && request.method === "POST") return logout(request, env);
    if (url.pathname === "/auth/me" && request.method === "GET") return me(request, env);
    if (url.pathname === "/users" && request.method === "GET") return users(request, env);
    if (url.pathname === "/conversations" && (request.method === "GET" || request.method === "POST")) return conversations(request, env);
    const readMatch = url.pathname.match(/^\/conversations\/([^/]+)\/read$/); if (readMatch && request.method === "POST") return markRead(request, env, readMatch[1]);
    const messagesMatch = url.pathname.match(/^\/conversations\/([^/]+)\/messages$/); if (messagesMatch && request.method === "GET") return conversationMessages(request, env, messagesMatch[1]); if (messagesMatch && request.method === "POST") return postMessage(request, env, messagesMatch[1]);
    if (url.pathname === "/ws") {
      if (request.headers.get("Upgrade") !== "websocket") return new Response("WebSocket required", { status: 426 });
      const user = await currentUser(request, env); if (!user) return json({ error: "UNAUTHORIZED" }, 401);
      const pair = new WebSocketPair(); const [client, server] = Object.values(pair); server.accept(); addLiveSocket(user.id, server); server.addEventListener("close", () => removeLiveSocket(user.id, server)); server.addEventListener("error", () => removeLiveSocket(user.id, server)); server.send(JSON.stringify({ type: "connected", userId: user.id }));
      server.addEventListener("message", async event => {
        try {
          const message = JSON.parse(event.data); if (message.type !== "message") return;
          const result = await persistMessage(env, user.id, String(message.conversationId || ""), message.clientId || message.id, message.text);
          if (result.error) { server.send(JSON.stringify({ type: "error", error: result.error, clientId: message.clientId || message.id || null })); return; }
          await broadcastMessage(env, result.message);
        } catch { server.send(JSON.stringify({ type: "error", error: "INVALID_MESSAGE" })); }
      });
      return new Response(null, { status: 101, webSocket: client });
    }
    return new Response("WAW Chat Server");
  }
};
