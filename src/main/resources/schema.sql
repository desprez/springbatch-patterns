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
     amount           numeric,
     transaction_date DATE,
     processed        VARCHAR(1) DEFAULT 'N'
  );

DROP SEQUENCE IF EXISTS batch_staging_seq ;

CREATE SEQUENCE batch_staging_seq;

DROP TABLE  IF EXISTS batch_staging ;

CREATE TABLE batch_staging
  (
	id BIGINT  NOT NULL PRIMARY KEY ,
	job_id BIGINT NOT NULL,
	value BYTEA NOT NULL,
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


