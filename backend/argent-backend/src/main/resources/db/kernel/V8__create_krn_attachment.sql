CREATE TABLE IF NOT EXISTS krn_attachment (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type text NOT NULL,
    entity_id text NOT NULL,
    filename varchar(255) NOT NULL,
    content_type varchar(255) NOT NULL,
    size_bytes bigint NOT NULL,
    storage_key text NOT NULL,
    uploaded_by uuid NULL,
    created_at timestamptz NOT NULL
);