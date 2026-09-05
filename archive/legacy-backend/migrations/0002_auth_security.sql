-- WAW authentication security migration.
-- Extends the 0001 schema without duplicating tables.

ALTER TABLE users ADD COLUMN password_salt TEXT;
ALTER TABLE users ADD COLUMN status TEXT NOT NULL DEFAULT 'offline';
ALTER TABLE sessions ADD COLUMN revoked_at INTEGER;

CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_sessions_active ON sessions(token_hash, expires_at, revoked_at);
