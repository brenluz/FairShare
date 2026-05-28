CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(255) not null unique,
    email VARCHAR(255) not null unique,
    password VARCHAR(255) not null,
    created_at TIMESTAMP not null default now(),
    updated_at TIMESTAMP not null default now());