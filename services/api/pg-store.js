import pg from "pg";

const { Pool } = pg;

export class PgStore {
  constructor({ connectionString = process.env.DATABASE_URL } = {}) {
    if (!connectionString) throw new Error("DATABASE_URL is required for production API");
    this.pool = new Pool({ connectionString, max: 10, connectionTimeoutMillis: 5000 });
  }

  async close() { await this.pool.end(); }

  async addAudit(record) {
    await this.pool.query(
      `INSERT INTO audit_records (tenant_id, workspace_id, actor_id, action, result, reason_code, trace_id, metadata, occurred_at)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8::jsonb, $9)`,
      [record.tenantId, record.workspaceId || null, record.actorId, record.action, record.result, record.reasonCode, record.traceId, JSON.stringify({}), record.occurredAt]
    );
  }

  async addMessage(message) {
    const result = await this.pool.query(
      `INSERT INTO messages (id, tenant_id, workspace_id, conversation_id, channel, sender_user_id, message_kind, body_json, created_at)
       VALUES ($1, $2, $3, $4, 'waw_internal', $5, $6, $7::jsonb, $8)
       RETURNING id, created_at`,
      [message.messageId, message.tenantId, message.workspaceId, message.conversationId, message.senderId, message.kind, JSON.stringify({ text: message.text }), message.createdAt]
    );
    return { ...message, messageId: result.rows[0].id, createdAt: result.rows[0].created_at.toISOString() };
  }

  async getWebhook(key) {
    const [channel, externalId, eventType] = key.split(":");
    const result = await this.pool.query(
      `SELECT id AS "eventId" FROM webhook_events WHERE channel = $1 AND external_id = $2 AND event_type = $3 LIMIT 1`,
      [channel, externalId, eventType]
    );
    return result.rows[0] || null;
  }

  async saveWebhook(key, event) {
    const [channel, externalId, eventType] = key.split(":");
    const result = await this.pool.query(
      `INSERT INTO webhook_events (id, tenant_id, channel, external_id, event_type, payload_redacted, status, attempts, received_at)
       VALUES ($1, $2, $3, $4, $5, '{}'::jsonb, 'received', $6, $7)
       ON CONFLICT (channel, external_id, event_type) DO NOTHING
       RETURNING id AS "eventId"`,
      [event.eventId, event.tenantId, channel, externalId, eventType, event.attempt, event.receivedAt]
    );
    return result.rows[0] || this.getWebhook(key);
  }
}
