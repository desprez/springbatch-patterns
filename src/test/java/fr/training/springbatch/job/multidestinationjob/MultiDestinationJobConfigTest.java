package fr.training.springbatch.job.multidestinationjob;

import static org.assertj.core.api.Assertions.assertThat;

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
@SpringBootTest(classes = { BatchTestConfiguration.class, MultiDestinationJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=multi-destination-job" })
class MultiDestinationJobConfigTest {

    private static final String OUTPUT_FILE1 = "target/output/outputfile1.csv";
    private static final String OUTPUT_FILE2 = "target/output/outputfile2.csv";

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Test
    void launch_MultiDestinationJob_nominal_should_success() throws Exception {

        // Given
        final JobParameters jobParameters = new JobParametersBuilder(jobLauncherTestUtils.getUniqueJobParameters())
                .addString("outputFile1", OUTPUT_FILE1) //
                .addString("outputFile2", OUTPUT_FILE2) //
                .toJobParameters();
        // When
        final JobExecution execution = jobLauncherTestUtils.launchJob(jobParameters);
        // Then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    }

}
