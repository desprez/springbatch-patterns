-- SQL script for create tables, sequences and index

DROP TABLE IF EXISTS customer;

CREATE TABLE customer
  (
     number     BIGINT NOT NULL,
     address    VARCHAR(50),
     city       VARCHAR(30),
     first_name VARCHAR(30),
     last_name  VARCHAR(30),
     post_code  VARCHAR(5),
     state      VARCHAR(2),
     birth_date DATE,
     PRIMARY KEY (number)
  );

DROP TABLE IF EXISTS transaction;

CREATE TABLE transaction
  (
     customer_number  VARCHAR(3) NOT NULL,
     number           BIGINT NOT NULL,
     amount           DECIMAL(8,2),
     transaction_date DATE,
     processed        VARCHAR(1) DEFAULT 'N'
     -- ,
     -- PRIMARY KEY (customer_number, number)
  );

DROP SEQUENCE IF EXISTS batch_staging_seq ;

CREATE SEQUENCE batch_staging_seq;

DROP TABLE IF EXISTS batch_staging ;

CREATE TABLE batch_staging
  (
	id BIGINT  NOT NULL PRIMARY KEY ,
	job_id BIGINT NOT NULL,
	value_ BYTEA NOT NULL,
	processed CHAR(1) NOT NULL
  );


DROP TABLE IF EXISTS new_customer;

CREATE TABLE new_customer
  (
     number     VARCHAR(3) NOT NULL,
     address    VARCHAR(50),
     city       VARCHAR(30),
     first_name VARCHAR(30),
     last_name  VARCHAR(30),
     post_code  VARCHAR(5),
     state      VARCHAR(2),
     birth_date DATE,
     PRIMARY KEY (number)
  );

DROP TABLE IF EXISTS yesterday_stock;

CREATE TABLE yesterday_stock
  (
     number     BIGINT NOT NULL,
     label      VARCHAR(50),
     PRIMARY KEY (number)
  );

  
DROP TABLE IF EXISTS subscription;

CREATE TABLE subscription
  (
     number VARCHAR(15) NOT NULL PRIMARY KEY ,
     status VARCHAR(08), 
     distributor_number VARCHAR(3) NOT NULL,
     creation_date TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP, 
     update_date TIMESTAMP (6), 
     title VARCHAR(03), 
     last_name VARCHAR(30) NOT NULL,
     first_name VARCHAR(30) NOT NULL,
     phone_number VARCHAR(10) NOT NULL,
     email_address VARCHAR(50) NOT NULL,
     loan_overdraft NUMERIC(13,2), 
     loan_term NUMERIC(3,0), 
     loan_tnc NUMERIC(4,2), 
     loan_teg NUMERIC(4,2), 
     loan_taeg NUMERIC(4,2), 
     loan_month_pay NUMERIC(13,2)
  );

