import test from "node:test";
import assert from "node:assert/strict";
import { MetaCloudClient } from "./meta-client.js";

test("Meta client retries transient failure and returns normalized id", async () => {
  let calls = 0;
  const client = new MetaCloudClient({ accessToken: "secret-not-logged", phoneNumberId: "phone", fetchImpl: async (_url, options) => { calls += 1; assert.equal(options.headers.authorization, "Bearer secret-not-logged"); if (calls < 2) return new Response(JSON.stringify({ error: { code: "TEMP" } }), { status: 503 }); return new Response(JSON.stringify({ messages: [{ id: "wamid.1" }] }), { status: 200 }); } });
  const result = await client.send({ messaging_product: "whatsapp", to: "15551234567", type: "text", text: { body: "hello" } });
  assert.equal(result.externalId, "wamid.1"); assert.equal(calls, 2);
});

test("Meta client does not retry permanent failure", async () => {
  let calls = 0;
  const client = new MetaCloudClient({ accessToken: "secret", phoneNumberId: "phone", fetchImpl: async () => { calls += 1; return new Response(JSON.stringify({ error: { code: "BAD_REQUEST" } }), { status: 400 }); } });
  await assert.rejects(() => client.send({}), /META_GRAPH_REQUEST_FAILED/);
  assert.equal(calls, 1);
});
