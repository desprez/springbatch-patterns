-- SQL script for create tables, sequences and index

DROP TABLE IF EXISTS customer;

CREATE TABLE customer
  (
     number     VARCHAR(3) NOT NULL,
     address    VARCHAR(50),
     city       VARCHAR(30),
     first_name VARCHAR(30),
     last_name  VARCHAR(30),
     post_code  VARCHAR(5),
     state      VARCHAR(2),
     PRIMARY KEY (number)
  );

DROP TABLE IF EXISTS transaction;

CREATE TABLE transaction
  (
     customer_number  VARCHAR(3) NOT NULL,
     number           VARCHAR(8) NOT NULL,
     amount           DOUBLE,
     transaction_date DATE
  );

DROP SEQUENCE batch_staging_seq IF EXISTS;

CREATE SEQUENCE batch_staging_seq START WITH 0;

DROP TABLE batch_staging IF EXISTS;

CREATE TABLE batch_staging  (
	id BIGINT IDENTITY NOT NULL PRIMARY KEY ,
	job_id BIGINT NOT NULL,
	value LONGVARBINARY NOT NULL,
	processed CHAR(1) NOT NULL
) ;