CREATE TABLE settlements (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          payer_id UUID NOT NULL REFERENCES users(id),
                          payee_id UUID NOT NULL REFERENCES users(id),
                          group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                          amount DECIMAL(10,2) NOT NULL,
                          settled_at TIMESTAMP NOT NULL DEFAULT NOW()
);