create table if not exists krn_user (
    id uuid primary key default gen_random_uuid(),
    username varchar(255) not null unique,
    display_name varchar(255) not null,
    created_at timestamptz default current_timestamp
);