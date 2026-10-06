ALTER TABLE companies
    ADD COLUMN recruiter_id BIGINT;

ALTER TABLE companies
    ADD CONSTRAINT fk_companies_recruiter
        FOREIGN KEY (recruiter_id)
            REFERENCES users(id);