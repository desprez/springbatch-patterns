package fr.training.springbatch.job.load;

import fr.training.springbatch.job.BatchTestConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, SimpleLoadJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=simple-load-job" })
class SimpleLoadJobTest {

    private static final String REJECT_FILE_PATH = "target/output/reject.csv";

    @Autowired
    private JobOperatorTestUtils testUtils;

    @Autowired
    private Job job;

    @BeforeEach
    void cleanUp() {
        final File rejectFile = new File(REJECT_FILE_PATH);
        if (rejectFile.exists()) {
            FileUtils.deleteQuietly(rejectFile);
        }
    }

    @Test
    void launch_SimpleLoadJob_nominal_should_success() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
                .addString("input-file", "src/main/resources/csv/transaction.csv")
                .addString("rejectfile", REJECT_FILE_PATH)
                .toJobParameters();
        testUtils.setJob(job);

        // When
        final JobExecution jobExec = testUtils.startJob(jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(new File(REJECT_FILE_PATH)).doesNotExist();

        // And expected read / write counts
        final Optional<StepExecution> executionOpt = jobExec.getStepExecutions().stream().filter(e -> "simple-load-step".equals(e.getStepName())).findFirst();
        assertThat(executionOpt).isPresent();
        final StepExecution stepExec = executionOpt.get();

        assertThat((int) stepExec.getReadCount()).isEqualTo(310);
        assertThat((int) stepExec.getWriteCount()).isEqualTo(310);
    }

    @Test
    void launch_SimpleLoadJob_with_bad_records_should_success_with_reject_file() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
                .addString("input-file", "src/main/resources/csv/transaction_bad.csv") //
                .addString("rejectfile", REJECT_FILE_PATH) //
                .toJobParameters();
        // When
        final JobExecution jobExec = testUtils.launchJob(jobParameters);
        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(new File(REJECT_FILE_PATH)).exists();
        assertThat(new File(REJECT_FILE_PATH)).hasContent("019;17878417;19-08-31;3.96\r\n" + "025;17878461;2019-08-12;X76.23\r\n");

        // And expected read / write / skip counts
        final Optional<StepExecution> executionOpt = jobExec.getStepExecutions().stream().filter(e -> "simple-load-step".equals(e.getStepName())).findFirst();
        assertThat(executionOpt).isPresent();
        final StepExecution stepExec = executionOpt.get();

        assertThat((int) stepExec.getReadCount()).isEqualTo(308);
        assertThat((int) stepExec.getWriteCount()).isEqualTo(308);
        assertThat((int) stepExec.getSkipCount()).isEqualTo(2);
    }

}
