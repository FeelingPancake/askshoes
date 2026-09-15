CREATE TABLE IF NOT EXISTS krn_user_auth (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,

    FOREIGN KEY (user_id) REFERENCES krn_user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS krn_user_session (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT current_timestamp,
    expires_at TIMESTAMPTZ NOT NULL,
    FOREIGN KEY (user_id) REFERENCES krn_user(id) ON DELETE CASCADE
);