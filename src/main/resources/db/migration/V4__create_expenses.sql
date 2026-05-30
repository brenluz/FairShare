CREATE TABLE expenses (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               paid_by UUID NOT NULL REFERENCES users(id),
                               group_id UUID NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
                               amount DECIMAL(10,2) NOT NULL,
                               split_type VARCHAR(255) NOT NULL,
                               description VARCHAR(255),
                               created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE expense_splits (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          expense_id UUID NOT NULL REFERENCES expenses(id) ON DELETE CASCADE,
                          user_id UUID NOT NULL REFERENCES users(id),
                          owed_amount DECIMAL(10,2) NOT NULL
);