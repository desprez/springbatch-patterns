# SpringBatch patterns

This repository is for trainnig purpose. It show a full springboot SpringBatch integration using modular configuration to avoid bean name/type conflicts.

Each job can be lauch independently using **-Dspring.batch.job.names={jobname}** parameters. (see Eclipse launch configuation for other parameters) 

## Introduction

## Pattern 1 : Export Job

![alt text](./images/exportjob.svg "Export Job")

This is the simplest job configuration (no inovation here). 
One step use the reader / processor / writer pattern to read a database table and write the content "as is" to a comma separated flat file. 

## Pattern 2 : Import Job

![alt text](./images/importjob.svg "Import Job")

## Pattern 3 : Staging Job

## Pattern 4 : Synchronize a file with a table

## Pattern 5 : Synchronize a table with a with a file

## Pattern 6 : Synchronize 2 tables

## Pattern 7 : Grouping file records

## Pattern 8 : Grouping tables records (with SQL)
