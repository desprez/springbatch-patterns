package fr.training.springbatch.job.staging;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import fr.training.springbatch.job.BatchTestConfiguration;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, StagingJobConfig.class }, properties = "spring.batch.job.enabled=false")
class StagingJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void stagingjob_should_proccess_all_batch_staging_table_records() throws Exception {
        final int before = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STAGING");

        final JobParameters jobParameters = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
                .addString("input-file", "src/main/resources/csv/transaction.csv").toJobParameters();

        final JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);

        final int after = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STAGING");
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(execution.getStepExecutions().iterator().next().getReadCount()).isEqualTo(after - before);
    }

}