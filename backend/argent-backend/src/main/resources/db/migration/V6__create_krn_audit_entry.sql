CREATE TABLE IF NOT EXISTS krn_audit_entry (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type text NOT NULL,
    entity_id text NOT NULL,
    action text NOT NULL,
    field text NULL,
    old_value text,
    new_value text,
    actor_id uuid NULL,
    actor_name text,
    created_at timestamptz NOT NULL
);