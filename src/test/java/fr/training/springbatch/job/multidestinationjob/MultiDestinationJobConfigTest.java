package fr.training.springbatch.job.multidestinationjob;

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
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, MultiDestinationJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=multi-destination-job" })
class MultiDestinationJobConfigTest {

    private static final String OUTPUT_FILE1 = "target/output/outputfile1.csv";
    private static final String OUTPUT_FILE2 = "target/output/outputfile2.csv";

    @Autowired
    private JobOperatorTestUtils testUtils;

    @Autowired
    private Job job;

    @Test
    void launch_MultiDestinationJob_nominal_should_success() throws Exception {

        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
                .addString("outputFile1", OUTPUT_FILE1) //
                .addString("outputFile2", OUTPUT_FILE2) //
                .toJobParameters();
        testUtils.setJob(job);

        // When
        final JobExecution execution = testUtils.startJob(jobParameters);

        // Then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

    }

}
