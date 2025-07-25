CREATE EXTENSION IF NOT EXISTS citext;

CREATE TABLE users
(
    id                 UUID PRIMARY KEY,
    username           CITEXT NOT NULL UNIQUE,
    email              VARCHAR(255) NOT NULL UNIQUE,
    first_name         VARCHAR(255),
    last_name          VARCHAR(255),
    city               VARCHAR(255),
    created_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE preferences
(
    id                 UUID PRIMARY KEY,
    activity_type      VARCHAR(255) NOT NULL,
    user_id            UUID NOT NULL,
    created_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_preferences_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_preferences_user_id ON preferences (user_id);
