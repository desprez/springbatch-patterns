package fr.training.springbatch.job.extract;

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
@SpringBootTest(classes = { BatchTestConfiguration.class, SimpleExtractJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=simple-extract-job" })
class SimpleExtractStepTest {

    private static final String OUTPUT_DIR = "target/output";
    private static final String OUTPUT_FILE = OUTPUT_DIR + "/simple-extract-null.csv";
    private static final String EXPECTED_FILE = "src/test/resources/datas/csv/extract-expected.csv";

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Test
    void simpleExtractStep_should_produce_expected_csv_file() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()) //
                .addString("output-dir", OUTPUT_DIR) //
                .toJobParameters();
        // When
        final JobExecution jobExec = testUtils.launchStep("simple-extract-step", jobParameters);

        // Then
        assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(new File(OUTPUT_FILE)).hasSameTextualContentAs(new File(EXPECTED_FILE));
    }

}
