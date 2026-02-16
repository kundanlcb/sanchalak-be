CREATE TABLE school_users (
    id UUID PRIMARY KEY,
    school_id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_school_users_school_id FOREIGN KEY (school_id) REFERENCES schools (id),
    CONSTRAINT fk_school_users_user_id FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_school_users_school_user UNIQUE (school_id, user_id)
);
