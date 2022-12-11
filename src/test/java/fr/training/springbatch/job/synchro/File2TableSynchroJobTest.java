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
@SpringBootTest(classes = { BatchTestConfiguration.class, File2TableSynchroJobConfig.class }, properties = "spring.batch.job.enabled=false")
class File2TableSynchroJobTest {

    private static final String OUTPUT_FILE = "target/output/outputfile.csv";

    private static final String CUSTOMER_FILE = "src/main/resources/csv/customer.csv";

    private static final String EXPECTED_FILE = "src/test/resources/datas/csv/customer-expected.csv";

    @Autowired
    private JobLauncherTestUtils testUtils;

    @Test
    void file2DBSynchroStep_should_produce_expected_file() throws Exception {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()) //
                .addString("customer-file", CUSTOMER_FILE) //
                .addString("output-file", OUTPUT_FILE) //
                .toJobParameters();
        // When
        final JobExecution jobExecution = testUtils.launchStep("file2tablesynchro-step", jobParameters);

        // Then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(new File(OUTPUT_FILE)).hasSameTextualContentAs(new File(EXPECTED_FILE));
    }
}
