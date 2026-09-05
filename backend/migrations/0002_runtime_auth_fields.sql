-- WAW database migration 0002
-- Runtime fields required by the deployed auth/chat worker.

PRAGMA foreign_keys = ON;

ALTER TABLE users ADD COLUMN password_salt TEXT;
ALTER TABLE users ADD COLUMN status TEXT NOT NULL DEFAULT 'offline';
ALTER TABLE sessions ADD COLUMN revoked_at INTEGER;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id TEXT PRIMARY KEY,
  user_id TEXT NOT NULL,
  token_hash TEXT NOT NULL UNIQUE,
  expires_at INTEGER NOT NULL,
  created_at INTEGER NOT NULL,
  used_at INTEGER,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_password_reset_token_hash ON password_reset_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_password_reset_user ON password_reset_tokens(user_id);
