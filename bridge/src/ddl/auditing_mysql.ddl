DROP TABLE MESSAGES;

CREATE TABLE MESSAGES (
    message_id VARCHAR(50) NOT NULL,
    merchant_id VARCHAR(24) NOT NULL,
    message_type VARCHAR(18) NOT NULL,
    message_version VARCHAR(12),
    auth_protocol TINYINT,
    message_status CHAR(1),
    card_number VARCHAR(50),
    card_number_flag TINYINT,
    transaction_id VARCHAR(28),
    time_of_publishing BIGINT NOT NULL,
    message TEXT,
    PRIMARY KEY (merchant_id, message_id, message_type)
);

