CREATE TABLE IF NOT EXISTS krn_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS krn_user_role (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    UNIQUE (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES krn_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES krn_role(id) ON DELETE CASCADE
);