# SpringBatch patterns

This repository is for trainning purpose. It show a full Springboot SpringBatch integration using modular configuration to avoid bean name/type conflicts.

Each job can be launch independently using **-Dspring.batch.job.names={jobname}** parameter. (see Eclipse launch configurations for other parameters)

It use **postgreSQL** database and **H2** for tests.

## Introduction

## Pattern 1 : Export Job

![alt text](./images/simpleExportJob.svg "Export Job")

[SimpleExportJobConfig.java](https://github.com/desprez/springbatch-patterns/blob/master/src/main/java/fr/training/springbatch/exportjob/SimpleExportJobConfig.java)

This is the simplest job configuration (no really inovation here).
One step use the reader / processor / writer pattern to read a database table and write the content "as is" to a comma separated flat file.

**Specificity :** the **incrementalFilename** method get an unique filename resource according to a file name and a job unique run identifier (Must be used in conjunction with RunIdIncrementer).

## Pattern 2 : Import Job

![alt text](./images/simpleImportJob.svg "Import Job")

## Pattern 3 : Synchronize 2 files (master/detail)

![alt text](./images/file2FileSynchroJob.svg "file2FileSynchroJob")

## Pattern 4 : Synchronize a file with a table

![alt text](./images/file2TableSynchroJob.svg "file2TableSynchroJob")

## Pattern 5 : Synchronize a table with a with a file

![alt text](./images/table2FileSynchroJob.svg "table2FileSynchroJob")

## Pattern 6 : Grouping file records

![alt text](./images/groupingRecordJob.svg "groupingRecordJob")

## Pattern 7 : Grouping tables records (with SQL)

![alt text](./images/sqlJoinSynchroJob.svg "sqlJoinSynchroJob")

## Pattern 8 : Staging Job
