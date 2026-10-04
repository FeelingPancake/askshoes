CREATE TABLE IF NOT EXISTS app_client (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name text NOT NULL,
    phone text NOT NULL UNIQUE,
    address text,
    login text,
    source_id uuid REFERENCES krn_ref_item(id),
    status_id uuid REFERENCES krn_ref_item(id),
    created_at timestamptz NOT NULL
);

CREATE TABLE IF NOT EXISTS app_client_contact_method (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id uuid NOT NULL REFERENCES app_client(id),
    contact_method_id uuid NOT NULL REFERENCES krn_ref_item(id),
    UNIQUE (client_id, contact_method_id)
);

CREATE TABLE IF NOT EXISTS app_order (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    number text NOT NULL UNIQUE,
    accepted_at timestamptz NOT NULL,
    client_id uuid NOT NULL REFERENCES app_client(id),
    manager_id uuid REFERENCES krn_user(id),
    due_date date,
    note text,
    status_id uuid NOT NULL REFERENCES krn_status(id),
    works_total numeric(12, 2) NOT NULL DEFAULT 0,
    discount_percent numeric(12, 2) NOT NULL DEFAULT 0,
    discount_amount numeric(12, 2) NOT NULL DEFAULT 0,
    total numeric(12, 2) NOT NULL DEFAULT 0,
    paid_amount numeric(12, 2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS app_order_item (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id uuid NOT NULL REFERENCES app_order(id),
    seq_no integer NOT NULL,
    category_id uuid REFERENCES krn_ref_item(id),
    item_type_id uuid NOT NULL REFERENCES krn_ref_item(id),
    brand_id uuid REFERENCES krn_ref_item(id),
    warranty boolean NOT NULL DEFAULT false,
    color_id uuid REFERENCES krn_ref_item(id),
    material_id uuid REFERENCES krn_ref_item(id),
    size_id uuid REFERENCES krn_ref_item(id),
    wear_percent integer CHECK (wear_percent BETWEEN 0 AND 100),
    comment text,
    works_total numeric(12, 2) NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS app_order_item_defect (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id uuid NOT NULL REFERENCES app_order_item(id),
    defect_type_id uuid NOT NULL REFERENCES krn_ref_item(id),
    UNIQUE (item_id, defect_type_id)
);

CREATE TABLE IF NOT EXISTS app_order_item_work (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    item_id uuid NOT NULL REFERENCES app_order_item(id),
    service_type_id uuid NOT NULL REFERENCES krn_ref_item(id),
    price numeric(12, 2) NOT NULL,
    total numeric(12, 2) NOT NULL
);
