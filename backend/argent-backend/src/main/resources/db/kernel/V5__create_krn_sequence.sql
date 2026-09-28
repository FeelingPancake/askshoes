CREATE TABLE IF NOT EXISTS krn_sequence (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code varchar(255) NOT NULL UNIQUE,
    pattern TEXT NOT NULL,
    reset_period TEXT NOT NULL,
    period_key TEXT,
    counter bigint NOT NULL DEFAULT 0
);