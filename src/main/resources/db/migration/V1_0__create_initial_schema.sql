CREATE TABLE IF NOT EXISTS token_cache (
             user_id VARCHAR(255) NOT NULL PRIMARY KEY,
             cache_data MEDIUMBLOB NOT NULL,
             last_updated Timestamp NOT NULL
) engine=InnoDB;