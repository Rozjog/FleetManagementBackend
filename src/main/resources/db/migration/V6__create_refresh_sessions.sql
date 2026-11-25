CREATE TABLE refresh_sessions (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL
    REFERENCES user_accounts(id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_refresh_sessions_account_id
    ON refresh_sessions(account_id);

CREATE INDEX idx_refresh_sessions_expires_at
    ON refresh_sessions(expires_at);