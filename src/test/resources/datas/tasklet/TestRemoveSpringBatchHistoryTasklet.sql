 /* History of the "jobTest" execution:
  *  - A first execution the 1st mai 2012 (JOB_INSTANCE_ID = -101)
  *  - A second execution at the current date (JOB_INSTANCE_ID = -102)
  */

Insert into BATCH_JOB_INSTANCE (JOB_INSTANCE_ID,VERSION,JOB_NAME,JOB_KEY)
values (-101,0,'jobTest','be310a80201b313039ac64ba1dd90d9f');
Insert into BATCH_JOB_INSTANCE (JOB_INSTANCE_ID,VERSION,JOB_NAME,JOB_KEY)
values (-102,0,'jobTest','ae310a90201b313039ac64ba1dd90d9f');

Insert into BATCH_JOB_EXECUTION (JOB_EXECUTION_ID,VERSION,JOB_INSTANCE_ID,CREATE_TIME,START_TIME,END_TIME,STATUS,EXIT_CODE,EXIT_MESSAGE,LAST_UPDATED)
values (-1,1,-101,TIMESTAMP '2012-05-01', TIMESTAMP '2012-05-01',TIMESTAMP '2012-05-01','COMPLETED','COMPLETED',null,CURRENT_TIMESTAMP);
Insert into BATCH_JOB_EXECUTION (JOB_EXECUTION_ID,VERSION,JOB_INSTANCE_ID,CREATE_TIME,START_TIME,END_TIME,STATUS,EXIT_CODE,EXIT_MESSAGE,LAST_UPDATED)
values (-2,1,-102,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),'COMPLETED','COMPLETED',null,CURRENT_TIMESTAMP());

Insert into BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID,SHORT_CONTEXT)
values (-1,'{"map":""}');
Insert into BATCH_JOB_EXECUTION_CONTEXT (JOB_EXECUTION_ID,SHORT_CONTEXT)
values (-2,'{"map":""}');

Insert into BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID,TYPE_CD,KEY_NAME,STRING_VAL,DATE_VAL,LONG_VAL,DOUBLE_VAL,IDENTIFYING)
values (-1,'STRING','param','value',CURRENT_TIMESTAMP(),0,0,'1');
Insert into BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID,TYPE_CD,KEY_NAME,STRING_VAL,DATE_VAL,LONG_VAL,DOUBLE_VAL,IDENTIFYING)
values (-2,'STRING','param','value',TIMESTAMP '2012-05-01',0,0,'1');

Insert into BATCH_STEP_EXECUTION (STEP_EXECUTION_ID,VERSION,STEP_NAME,JOB_EXECUTION_ID,START_TIME,END_TIME,STATUS,COMMIT_COUNT,READ_COUNT,FILTER_COUNT,WRITE_COUNT,READ_SKIP_COUNT,WRITE_SKIP_COUNT,PROCESS_SKIP_COUNT,ROLLBACK_COUNT,EXIT_CODE,EXIT_MESSAGE,LAST_UPDATED)
values (-11,1,'step1',-1,TIMESTAMP '2012-05-01',TIMESTAMP '2012-05-01','COMPLETED',1,6,0,6,0,0,0,0,'COMPLETED',null,TIMESTAMP '2012-05-01');
Insert into BATCH_STEP_EXECUTION (STEP_EXECUTION_ID,VERSION,STEP_NAME,JOB_EXECUTION_ID,START_TIME,END_TIME,STATUS,COMMIT_COUNT,READ_COUNT,FILTER_COUNT,WRITE_COUNT,READ_SKIP_COUNT,WRITE_SKIP_COUNT,PROCESS_SKIP_COUNT,ROLLBACK_COUNT,EXIT_CODE,EXIT_MESSAGE,LAST_UPDATED)
values (-12,1,'step1',-2,CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP(),'COMPLETED',1,6,0,6,0,0,0,0,'COMPLETED',null,CURRENT_TIMESTAMP());

Insert into BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID,SHORT_CONTEXT)
values (-11,'{"map":{"entry":{"string":"JdbcCursorItemReader.read.count","int":9}}}');
Insert into BATCH_STEP_EXECUTION_CONTEXT (STEP_EXECUTION_ID,SHORT_CONTEXT)
values (-12,'{"map":{"entry":{"string":"JdbcCursorItemReader.read.count","int":9}}}');