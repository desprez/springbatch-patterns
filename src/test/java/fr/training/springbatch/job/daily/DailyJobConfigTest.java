package fr.training.springbatch.job.daily;

import fr.training.springbatch.job.BatchTestConfiguration;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, DailyJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=daily-job" })
class DailyJobConfigTest {

    @Autowired
    private JobOperatorTestUtils testUtils;

    @Autowired
    private Job job;

    @Test
    void launch_DailyJob_nominal_should_success() throws Exception {
        Assertions.setMaxStackTraceElementsDisplayed(800);
        // Given
        final JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDate("processDate", LocalDate.now())
                .toJobParameters();
        testUtils.setJob(job);

        // When
        final JobExecution jobExec = testUtils.startJob(jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        // When
        final Throwable thrown = catchThrowable(() -> {
            // And When
            final JobExecution newJobExec = testUtils.launchJob(jobParameters);
            assertThat(newJobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        });
        // Then
        assertThat(thrown).isExactlyInstanceOf(JobInstanceAlreadyCompleteException.class);
        assertThat(thrown.getMessage()).startsWith("A job instance already exists and is complete for identifying parameters={JobParameter{name='processDate',")
                .endsWith("If you want to run this job again, change the parameters.");
    }

}
