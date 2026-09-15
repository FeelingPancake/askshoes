CREATE TABLE IF NOT EXISTS krn_ref_type (
    id uuid primary key default gen_random_uuid(),
    code varchar(255) not null unique
);

CREATE TABLE IF NOT EXISTS krn_ref_item (
    id uuid primary key default gen_random_uuid(),
    ref_type_id uuid NOT NULL,
    code varchar(255) NOT NULL,
    name varchar(255) NOT NULL,
    active boolean NOT NULl DEFAULT true,
    sort_order integer NOT NULL DEFAULT 0,
    attributes jsonb,

    FOREIGN KEY (ref_type_id) REFERENCES krn_ref_type(id) ON DELETE CASCADE,
    UNIQUE(ref_type_id, code)
);