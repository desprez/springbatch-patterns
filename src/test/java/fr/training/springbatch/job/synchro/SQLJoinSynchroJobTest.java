package fr.training.springbatch.job.synchro;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

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
@SpringBootTest(classes = { BatchTestConfiguration.class, SQLJoinSynchroJobConfig.class }, properties = "spring.batch.job.enabled=false")
class SQLJoinSynchroJobTest {

    private static final String OUTPUT_FILE = "target/output/outputfile.csv";

    private static final String EXPECTED_FILE = "src/test/resources/datas/csv/customer-expected.csv";

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Test
    void sqlJoinSynchroStep_should_produce_expected_file() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()) //
                .addString("output-file", OUTPUT_FILE) //
                .toJobParameters();
        // When
        final JobExecution jobExecution = testUtils.launchStep("sqljoinsynchro-step", jobParameters);

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(new File(OUTPUT_FILE)).hasSameTextualContentAs(new File(EXPECTED_FILE));
    }
}
