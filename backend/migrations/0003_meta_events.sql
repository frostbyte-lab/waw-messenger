-- Official WhatsApp Business Platform event inbox.
-- Stores verified webhook events idempotently before application processing.

CREATE TABLE IF NOT EXISTS meta_events (
  id TEXT PRIMARY KEY,
  kind TEXT NOT NULL,
  wa_id TEXT,
  event_type TEXT NOT NULL,
  payload_json TEXT NOT NULL,
  received_at INTEGER NOT NULL,
  processed_at INTEGER
);

CREATE INDEX IF NOT EXISTS idx_meta_events_unprocessed
  ON meta_events(processed_at, received_at);

CREATE INDEX IF NOT EXISTS idx_meta_events_wa_id
  ON meta_events(wa_id, received_at);
