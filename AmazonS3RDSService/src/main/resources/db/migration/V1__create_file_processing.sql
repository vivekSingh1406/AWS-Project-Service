CREATE TABLE file_processing (
    id BIGINT NOT NULL AUTO_INCREMENT,
    request_id BINARY(16) NOT NULL,
    idempotency_key VARCHAR(128) NOT NULL,
    content_hash VARCHAR(64) NOT NULL,
    xml_file_name VARCHAR(255) NOT NULL,
    xml_s3_key VARCHAR(512),
    json_file_name VARCHAR(255),
    json_s3_key VARCHAR(512),
    status VARCHAR(32) NOT NULL,
    error_code VARCHAR(64),
    error_message VARCHAR(500),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_file_processing_request_id UNIQUE (request_id),
    CONSTRAINT uk_file_processing_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT ck_file_processing_status CHECK (status IN ('RECEIVED','XML_STORED','JSON_STORED','COMPLETED','FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_file_processing_status ON file_processing(status);
CREATE INDEX idx_file_processing_created_at ON file_processing(created_at);
