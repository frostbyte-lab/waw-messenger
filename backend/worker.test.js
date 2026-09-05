import test from "node:test";
import assert from "node:assert/strict";
import worker from "./worker.js";

const env = {};

async function responseJson(response) {
  return await response.json();
}

test("GET /health returns a healthy service response", async () => {
  const response = await worker.fetch(new Request("https://example.test/health"), env);
  assert.equal(response.status, 200);
  assert.deepEqual(await responseJson(response), { ok: true, service: "waw-chat" });
});

test("OPTIONS returns the expected CORS headers", async () => {
  const response = await worker.fetch(new Request("https://example.test/auth/login", { method: "OPTIONS" }), env);
  assert.equal(response.status, 204);
  assert.equal(response.headers.get("Access-Control-Allow-Origin"), "*");
  assert.match(response.headers.get("Access-Control-Allow-Methods") ?? "", /POST/);
});

test("protected routes reject missing authentication", async () => {
  for (const path of ["/auth/me", "/users", "/conversations"]) {
    const response = await worker.fetch(new Request(`https://example.test${path}`), env);
    assert.equal(response.status, 401, path);
    assert.equal((await responseJson(response)).error, "UNAUTHORIZED", path);
  }
});

test("websocket route requires an upgrade request", async () => {
  const response = await worker.fetch(new Request("https://example.test/ws"), env);
  assert.equal(response.status, 426);
});
