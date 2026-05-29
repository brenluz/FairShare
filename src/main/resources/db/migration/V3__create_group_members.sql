CREATE TABLE group_members (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                               user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               joined_at TIMESTAMP NOT NULL DEFAULT NOW(),
                               UNIQUE (group_id, user_id)
);