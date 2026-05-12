package fr.training.springbatch.job.computedelta;

import fr.training.springbatch.job.BatchTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
    private JobOperatorTestUtils testUtils;

    @Autowired
    private Job job;

    @Test
    void launch_CompareJob_nominal_should_success() throws Exception {
        // Given
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "yesterday_stock")).isZero();

        JobParameters jobParameters = new JobParametersBuilder() //
                .addString("today-stock-file", YESTERDAY_FILE) //
                .toJobParameters();
        testUtils.setJob(job);

        // When
        JobExecution jobExec = testUtils.startJob(jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "yesterday_stock")).isEqualTo(7);
        assertWriteCount(jobExec, "process-added-step", 7);

        jobParameters = new JobParametersBuilder() //
                .addString("today-stock-file", TODAY_FILE) //
                .toJobParameters();

        // When
        jobExec = testUtils.startJob(jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "yesterday_stock")).isEqualTo(8);
        assertWriteCount(jobExec, "process-added-step", 4);
        assertWriteCount(jobExec, "process-removed-step", 3);
    }

    private void assertWriteCount(final JobExecution jobExec, final String stepName, final int expectedWriteCounts) {
        // And expected read / write counts
        final Optional<StepExecution> executionOpt = jobExec.getStepExecutions().stream().filter(e -> stepName.equals(e.getStepName())).findFirst();
        assertThat(executionOpt).isPresent();
        final StepExecution stepExec = executionOpt.get();

        assertThat((int) stepExec.getWriteCount()).isEqualTo(expectedWriteCounts);
    }

}
