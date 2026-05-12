package fr.training.springbatch.job.controlbreak;

import fr.training.springbatch.job.BatchTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, ControlBreakChunkJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=controlbreak-chunk-job" })
class ControlBreakChunkJobTest {

    private static final String OUTPUT_FILE = "target/output/outputfile.csv";

    private static final String TRANSACTION_FILE = "src/main/resources/csv/transaction.csv";

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Test
    void controlChunkJob_should_produce_expected_file() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()) //
                .addString("transaction-file", TRANSACTION_FILE) //
                .addString("output-file", OUTPUT_FILE) //
                .toJobParameters();
        // When
        final JobExecution jobExecution = testUtils.launchJob(jobParameters);

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

}
