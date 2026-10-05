CREATE TABLE applications (
                              id BIGSERIAL PRIMARY KEY,

                              candidate_id BIGINT NOT NULL,

                              job_id BIGINT NOT NULL,

                              status VARCHAR(50) NOT NULL,

                              applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_applications_candidate
                                  FOREIGN KEY (candidate_id)
                                      REFERENCES candidates(id),

                              CONSTRAINT fk_applications_job
                                  FOREIGN KEY (job_id)
                                      REFERENCES jobs(id),

                              CONSTRAINT uq_application_candidate_job
                                  UNIQUE (candidate_id, job_id)
);