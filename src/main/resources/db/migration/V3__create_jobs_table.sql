CREATE TABLE jobs (
                      id BIGSERIAL PRIMARY KEY,

                      company_id BIGINT NOT NULL,

                      title VARCHAR(300) NOT NULL,

                      description TEXT,

                      location VARCHAR(200),

                      employment_type VARCHAR(50) NOT NULL,

                      salary_min NUMERIC(12, 2),

                      salary_max NUMERIC(12, 2),

                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                      CONSTRAINT fk_jobs_company
                          FOREIGN KEY (company_id)
                              REFERENCES companies(id)
);