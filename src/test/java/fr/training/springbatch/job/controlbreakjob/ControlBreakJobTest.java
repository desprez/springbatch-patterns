package fr.training.springbatch.job.controlbreakjob;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import fr.training.springbatch.job.BatchTestConfiguration;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class,
		ControlBreakJobConfig.class }, properties = "spring.batch.job.enabled=false")
public class ControlBreakJobTest {

	private static final String OUTPUT_FILE = "target/output/outputfile.csv";

	private static final String TRANSACTION_FILE = "src/main/resources/csv/transaction.csv";

	private static final String EXPECTED_FILE = "src/test/resources/datas/csv/transaction-sum-expected.csv";

	@Autowired
	private JobLauncherTestUtils testUtils;

	@Test
	public void controlBreakStep_should_produce_expected_file() throws Exception {
		// Given
		final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()) //
				.addString("transaction-file", TRANSACTION_FILE) //
				.addString("output-file", OUTPUT_FILE) //
				.toJobParameters();
		// When
		final JobExecution jobExecution = testUtils.launchStep("controlbreak-step", jobParameters);

		// Then
		assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);

		AssertFile.assertFileEquals(new FileSystemResource(EXPECTED_FILE), //
				new FileSystemResource(OUTPUT_FILE));
	}

}
