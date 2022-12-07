package fr.training.springbatch.job.businessdays;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import fr.training.springbatch.job.BatchTestConfiguration;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, BusinessDaysJobConfig.class }, properties = "spring.batch.job.enabled=false")
class BusinessDaysJobConfigTest {

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Test
    void launch_DailyJob_nominal_should_success() throws Exception {
        // Given

        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()).addDate("processDate", new Date()).toJobParameters();
        // When
        final JobExecution jobExec = testUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

}
