CREATE TABLE companies (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(300) NOT NULL UNIQUE,
                           cin CHAR(21) NOT NULL UNIQUE,
                           website VARCHAR(500),
                           description TEXT,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);