package fr.training.springbatch.job.synchro;

import fr.training.springbatch.job.BatchTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.JobOperatorTestUtils;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, File2FileSynchroJobConfig.class }, properties = { "spring.batch.job.enabled=false",
        "spring.batch.job.names=file2filesynchro-job" })
class File2FileSynchroJobTest {

    private static final String OUTPUT_FILE = "target/output/outputfile.csv";

    private static final String CUSTOMER_FILE = "src/main/resources/csv/customer.csv";

    private static final String TRANSACTION_FILE = "src/main/resources/csv/transaction.csv";

    private static final String EXPECTED_FILE = "src/test/resources/datas/csv/customer-expected.csv";

    @Autowired
    private JobOperatorTestUtils testUtils;

    @Autowired
    private Job job;

    @Test
    void file2FileSynchroStep_should_produce_expected_file() {
        // Given
        final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()) //
                .addString("customer-file", CUSTOMER_FILE) //
                .addString("transaction-file", TRANSACTION_FILE) //
                .addString("output-file", OUTPUT_FILE) //
                .toJobParameters();
        testUtils.setJob(job);
        StepExecution fixtureExecution = MetaDataInstanceFactory.createStepExecution();
        // When
        final JobExecution execution = testUtils.startStep("file2filesynchro-step", jobParameters, fixtureExecution.getExecutionContext());

        // Then
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

        assertThat(new File(OUTPUT_FILE)).hasSameTextualContentAs(new File(EXPECTED_FILE));
    }

}
