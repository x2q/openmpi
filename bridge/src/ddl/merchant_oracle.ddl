CREATE OR REPLACE TABLE merchant (
merchant_id           VARCHAR2(24) NOT NULL,
name                  VARCHAR2(25) NOT NULL,
datasource_jndi       VARCHAR2(50),
jdbc_driver_name      VARCHAR2(50),
database_url          VARCHAR2(50),
database_user_name    VARCHAR2(25),
database_password     VARCHAR2(25),
merchant_password     VARCHAR2(30),
schema_name           VARCHAR2(20),
merchant_url          VARCHAR2(255) NOT NULL,
countrycode           VARCHAR2(3) NOT NULL,
purchasecurrency      VARCHAR2(3) NOT NULL,
acquirerbin           VARCHAR2(11) NOT NULL,
protocol_support      NUMBER(1) NOT NULL,
licensing_key 	    VARCHAR2(30),
key_expiry_date       NUMBER(16),
PRIMARY KEY           (merchant_id)
);
