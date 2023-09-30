package fr.training.springbatch.job.computedelta;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
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
@SpringBootTest(classes = { BatchTestConfiguration.class, ComputeDeltaJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=compute-delta-job" })
class ComputeDeltaJobTest {

    private static final String YESTERDAY_FILE = "src/test/resources/datas/stock/yesterday-stock.csv";

    private static final String TODAY_FILE = "src/test/resources/datas/stock/today-stock.csv";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Test
    void launch_CompareJob_nominal_should_success() throws Exception {
        // Given
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "yesterday_stock")).isZero();

        JobParameters jobParameters = new JobParametersBuilder() //
                .addString("today-stock-file", YESTERDAY_FILE) //
                .toJobParameters();
        // When
        JobExecution jobExec = testUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "yesterday_stock")).isEqualTo(7);
        assertWriteCount(jobExec, "process-added-step", 7);

        jobParameters = new JobParametersBuilder() //
                .addString("today-stock-file", TODAY_FILE) //
                .toJobParameters();

        // When
        jobExec = testUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "yesterday_stock")).isEqualTo(8);
        assertWriteCount(jobExec, "process-added-step", 4);
        assertWriteCount(jobExec, "process-removed-step", 3);
    }

    private void assertWriteCount(final JobExecution jobExec, final String stepName, final int expectedWriteCounts) {
        // And expected read / write counts
        final Optional<StepExecution> executionOpt = jobExec.getStepExecutions().stream().filter(e -> stepName.equals(e.getStepName())).findFirst();
        assertThat(executionOpt.isPresent()).isTrue();
        final StepExecution stepExec = executionOpt.get();

        assertThat(stepExec.getWriteCount()).isEqualTo(expectedWriteCounts);
    }

}
