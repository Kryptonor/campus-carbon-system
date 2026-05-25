-- Campus Carbon Footprint System database schema
-- Engine: InnoDB, Charset: utf8mb4

CREATE DATABASE IF NOT EXISTS campus_carbon DEFAULT CHARACTER SET utf8mb4;
USE campus_carbon;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(64) NOT NULL,
    phone VARCHAR(32),
    wallet_address VARCHAR(128),
    avatar_url VARCHAR(255),
    points_balance BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(128) NOT NULL,
    description VARCHAR(512),
    price_points BIGINT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    image_url VARCHAR(255),
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exchange_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    amount INT NOT NULL DEFAULT 1,
    total_points BIGINT NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    redeem_code VARCHAR(64),
    redeem_status TINYINT NOT NULL DEFAULT 0,
    redeemed_at DATETIME,
    tx_hash VARCHAR(128),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_exchange_user (user_id),
    INDEX idx_exchange_product (product_id),
    UNIQUE INDEX uk_exchange_redeem_code (redeem_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS system_config (
    config_key VARCHAR(64) PRIMARY KEY,
    config_value VARCHAR(256) NOT NULL,
    config_desc VARCHAR(256),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS behavior_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    behavior_type VARCHAR(64) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    image_hash VARCHAR(64) NOT NULL,
    points BIGINT NOT NULL DEFAULT 0,
    ai_label VARCHAR(128),
    ai_score DECIMAL(5,4) NOT NULL DEFAULT 0.0000,
    decision VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    tx_hash VARCHAR(128),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_behavior_user (user_id),
    INDEX idx_behavior_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO system_config (config_key, config_value, config_desc)
VALUES
    ('ai_threshold', '0.80', 'AI confidence threshold for green action verification'),
    ('daily_limit', '3', 'Daily check-in limit per user'),
    ('points.clean_plate', '10', 'Points for clean plate action'),
    ('points.recycle', '5', 'Points for recycle action'),
    ('carbon.exchange.rate', '0.10', 'Carbon point to carbon reduction rate')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), config_desc = VALUES(config_desc);
