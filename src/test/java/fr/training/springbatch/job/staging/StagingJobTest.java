package fr.training.springbatch.job.staging;

import fr.training.springbatch.job.BatchTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, StagingJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=staging-job" })
class StagingJobTest {

    @Autowired
    private JobOperatorTestUtils testUtils;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Job job;

    @Autowired
    public void setDataSource(final DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void stagingjob_should_proccess_all_batch_staging_table_records() throws Exception {
        // Given
        final int before = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STAGING");

        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
                .addString("input-file", "src/main/resources/csv/transaction.csv")
                .toJobParameters();
        testUtils.setJob(job);

        // When
        final JobExecution execution = testUtils.startJob(jobParameters);

        // Then
        final int after = JdbcTestUtils.countRowsInTable(jdbcTemplate, "BATCH_STAGING");
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat((int) execution.getStepExecutions().iterator().next().getReadCount()).isEqualTo(after - before);
    }

}