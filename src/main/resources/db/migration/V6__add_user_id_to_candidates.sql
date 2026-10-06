ALTER TABLE candidates
    ADD COLUMN user_id BIGINT;

ALTER TABLE candidates
    ADD CONSTRAINT fk_candidates_user
        FOREIGN KEY (user_id)
            REFERENCES users(id);