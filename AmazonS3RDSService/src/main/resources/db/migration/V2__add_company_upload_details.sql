ALTER TABLE file_processing
    MODIFY COLUMN idempotency_key VARCHAR(128) NULL,
    MODIFY COLUMN content_hash VARCHAR(64) NULL,
    ADD COLUMN company_id VARCHAR(100),
    ADD COLUMN company_name VARCHAR(200),
    ADD COLUMN submitted_date DATE;

UPDATE file_processing
SET company_id = 'legacy',
    company_name = 'Legacy upload',
    submitted_date = DATE(created_at)
WHERE company_id IS NULL;

ALTER TABLE file_processing
    MODIFY COLUMN company_id VARCHAR(100) NOT NULL,
    MODIFY COLUMN company_name VARCHAR(200) NOT NULL,
    MODIFY COLUMN submitted_date DATE NOT NULL;
