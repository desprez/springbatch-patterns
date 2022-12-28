package fr.training.springbatch.job.update;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
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
import org.springframework.test.context.ActiveProfiles;

import fr.training.springbatch.job.BatchTestConfiguration;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, SimpleUpdateJobConfig.class }, properties = "spring.batch.job.enabled=false")
class SimpleUpdateJobTest {

    private static final String REJECT_FILE_PATH = "target/output/reject-updates.csv";

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Test
    void launch_SimpleUpdateJob_nominal_should_success() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
                .addString("input-file", "src/main/resources/csv/customer-update.csv") //
                .addString("rejectfile", REJECT_FILE_PATH) //
                .toJobParameters();
        // When
        final JobExecution jobExec = testUtils.launchJob(jobParameters);
        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(new File(REJECT_FILE_PATH)).doesNotExist();

        // And expected read / write counts
        final Optional<StepExecution> executionOpt = jobExec.getStepExecutions().stream().filter(e -> "simple-update-step".equals(e.getStepName())).findFirst();
        assertThat(executionOpt.isPresent()).isTrue();
        final StepExecution stepExec = executionOpt.get();

        assertThat(stepExec.getReadCount()).isEqualTo(200);
        assertThat(stepExec.getWriteCount()).isEqualTo(200);
    }

}
