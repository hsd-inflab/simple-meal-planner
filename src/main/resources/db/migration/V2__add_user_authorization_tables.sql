CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE app_user_roles (
    user_id UUID NOT NULL,
    role VARCHAR(255) NOT NULL,

    CONSTRAINT fk_app_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES app_user(id)
            ON DELETE CASCADE
);
