#!/bin/sh
d=$(date +%Y/%m/%d)
echo "$d"
java -Dspring.batch.job.names=daily-job -jar target/springbatch-patterns.jar processDate\(date\)=$d