@echo lancenement du batch daily-job

for /f %%i in ('powershell get-date -format "{yyyy/MM/dd}"') do set processdate=%%i

java -Dspring.profiles.active=local -Dspring.batch.job.names=daily-job -jar target\springbatch-patterns.jar processDate(date)=%processdate%

echo exitcode is %errorlevel%