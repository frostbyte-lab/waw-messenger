-- HYB-030 core schema for PostgreSQL.
-- Production deployment must run this through a migration runner with backup/rollback controls.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS tenants (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name text NOT NULL CHECK (length(trim(name)) > 0),
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz
);

CREATE TABLE IF NOT EXISTS users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    email text NOT NULL UNIQUE,
    display_name text NOT NULL,
    auth_provider text NOT NULL CHECK (auth_provider IN ('password', 'oauth', 'service')),
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz
);

CREATE TABLE IF NOT EXISTS workspaces (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id uuid NOT NULL REFERENCES tenants(id),
    name text NOT NULL CHECK (length(trim(name)) > 0),
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,
    UNIQUE (tenant_id, name),
    UNIQUE (id, tenant_id)
);

CREATE TABLE IF NOT EXISTS memberships (
    tenant_id uuid NOT NULL REFERENCES tenants(id),
    user_id uuid NOT NULL REFERENCES users(id),
    workspace_id uuid REFERENCES workspaces(id),
    role text NOT NULL CHECK (role IN ('owner', 'admin', 'operator', 'member', 'viewer')),
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (tenant_id, user_id, workspace_id),
    CONSTRAINT membership_workspace_tenant_fk FOREIGN KEY (workspace_id, tenant_id)
        REFERENCES workspaces(id, tenant_id)
        DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE IF NOT EXISTS conversations (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id uuid NOT NULL REFERENCES tenants(id),
    workspace_id uuid NOT NULL,
    channel text NOT NULL CHECK (channel IN ('waw_internal', 'whatsapp_business')),
    external_id text,
    title text,
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,
    UNIQUE (tenant_id, workspace_id, channel, external_id),
    CONSTRAINT conversation_workspace_tenant_fk FOREIGN KEY (workspace_id, tenant_id)
        REFERENCES workspaces(id, tenant_id)
);

CREATE TABLE IF NOT EXISTS messages (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id uuid NOT NULL REFERENCES tenants(id),
    workspace_id uuid NOT NULL,
    conversation_id uuid NOT NULL REFERENCES conversations(id),
    channel text NOT NULL CHECK (channel IN ('waw_internal', 'whatsapp_business')),
    external_id text,
    sender_user_id uuid REFERENCES users(id),
    message_kind text NOT NULL CHECK (message_kind IN ('text', 'media', 'template', 'system')),
    body_json jsonb NOT NULL DEFAULT '{}'::jsonb,
    created_at timestamptz NOT NULL DEFAULT now(),
    deleted_at timestamptz,
    UNIQUE (channel, external_id),
    CONSTRAINT message_workspace_tenant_fk FOREIGN KEY (workspace_id, tenant_id)
        REFERENCES workspaces(id, tenant_id)
);

CREATE TABLE IF NOT EXISTS media_objects (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id uuid NOT NULL REFERENCES tenants(id),
    workspace_id uuid NOT NULL,
    storage_key text NOT NULL UNIQUE,
    content_type text NOT NULL,
    byte_size bigint NOT NULL CHECK (byte_size >= 0),
    scan_status text NOT NULL CHECK (scan_status IN ('pending', 'clean', 'quarantined', 'deleted')),
    retention_until timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT media_workspace_tenant_fk FOREIGN KEY (workspace_id, tenant_id)
        REFERENCES workspaces(id, tenant_id)
);

CREATE TABLE IF NOT EXISTS audit_records (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id uuid NOT NULL REFERENCES tenants(id),
    workspace_id uuid,
    actor_id text NOT NULL,
    action text NOT NULL,
    resource_type text,
    resource_id text,
    result text NOT NULL CHECK (result IN ('allowed', 'denied', 'failed')),
    reason_code text,
    trace_id text NOT NULL,
    metadata jsonb NOT NULL DEFAULT '{}'::jsonb,
    occurred_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT audit_workspace_tenant_fk FOREIGN KEY (workspace_id, tenant_id)
        REFERENCES workspaces(id, tenant_id)
);

CREATE TABLE IF NOT EXISTS webhook_events (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id uuid REFERENCES tenants(id),
    channel text NOT NULL CHECK (channel = 'whatsapp_business'),
    external_id text NOT NULL,
    event_type text NOT NULL,
    payload_redacted jsonb NOT NULL,
    status text NOT NULL CHECK (status IN ('received', 'processed', 'failed', 'dead_letter')),
    attempts integer NOT NULL DEFAULT 0 CHECK (attempts >= 0),
    received_at timestamptz NOT NULL DEFAULT now(),
    processed_at timestamptz,
    UNIQUE (channel, external_id, event_type)
);

CREATE INDEX IF NOT EXISTS idx_workspaces_tenant ON workspaces(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_messages_conversation_created ON messages(tenant_id, conversation_id, created_at);
CREATE INDEX IF NOT EXISTS idx_audit_tenant_occurred ON audit_records(tenant_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_webhook_status ON webhook_events(status, received_at);
