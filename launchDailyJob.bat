@echo lancenement du batch daily-job

for /f %%i in ('powershell get-date -format "{yyyy-MM-dd}"') do set processdate=%%i

java -Dspring.batch.job.names=daily-job -jar target\springbatch-patterns.jar processDate=%processdate%,java.time.LocalDate,true

echo exitcode is %errorlevel%