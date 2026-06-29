CREATE TABLE token_cache (
    user_id      VARCHAR(255) NOT NULL,
    cache_data   LONGBLOB     NOT NULL,
    last_updated TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
