CREATE TABLE "groups" (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        name VARCHAR(255) NOT NULL,
                        description VARCHAR(255),
                        created_by UUID NOT NULL REFERENCES users(id),
                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);