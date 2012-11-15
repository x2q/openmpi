DROP TABLE merchant;


CREATE TABLE merchant (
merchant_id           VARCHAR(24) NOT NULL,
name                  VARCHAR(25) NOT NULL,
datasource_jndi       VARCHAR(50),
jdbc_driver_name      VARCHAR(50),
database_url          VARCHAR(50),
database_user_name    VARCHAR(25),
database_password     VARCHAR(25),
merchant_password     VARCHAR(30),
schema_name           VARCHAR(20),
merchant_url          VARCHAR(255) NOT NULL,
countrycode           VARCHAR(3) NOT NULL,
purchasecurrency      VARCHAR(3) NOT NULL,
acquirerbin           VARCHAR(11) NOT NULL,
protocol_support      TINYINT  NOT NULL,
licensing_key	    VARCHAR(30),
key_expiry_date 	    BIGINT,
PRIMARY KEY           (merchant_id)
);
