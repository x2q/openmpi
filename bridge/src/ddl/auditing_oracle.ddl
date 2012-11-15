
CREATE OR REPLACE TABLE MESSAGES (
    message_id VARCHAR2(50) NOT NULL,
    merchant_id VARCHAR2(24) NOT NULL,
    message_type VARCHAR2(18) NOT NULL,
    message_version VARCHAR2(12),
    auth_protocol NUMBER(1),
    message_status CHAR(1),
    card_number VARCHAR2(50),
    card_number_flag NUMBER(1),
    transaction_id VARCHAR2(28),
    time_of_publishing NUMBER(16) NOT NULL,
    message VARCHAR2(4000),
    PRIMARY KEY (merchant_id, message_id, message_type)
);

