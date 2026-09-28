CREATE TABLE IF NOT EXISTS krn_status_type (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    code text NOT NULL
);

CREATE TABLE IF NOT EXISTS krn_status (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    status_type_id uuid NOT NULL,
    code text NOT NULL,
    name text NOT NULL,
    sort_order integer NOT NULL DEFAULT 0,
    UNIQUE(status_type_id, code),
    FOREIGN KEY (status_type_id) REFERENCES krn_status_type(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS krn_status_transition (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    status_type_id uuid NOT NULL,
    from_status_id uuid NULL,
    to_status_id uuid NOT NULL,
    required_role uuid NULL,
    FOREIGN KEY (required_role) REFERENCES krn_role(id),
    FOREIGN KEY (status_type_id) REFERENCES krn_status_type(id),
    FOREIGN KEY (from_status_id) REFERENCES krn_status(id),
    FOREIGN KEY (to_status_id) REFERENCES krn_status(id)
);