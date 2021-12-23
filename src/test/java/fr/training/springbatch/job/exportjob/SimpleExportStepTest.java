package fr.training.springbatch.job.exportjob;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
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

import fr.training.springbatch.job.BatchTestConfiguration;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class,
		SimpleExportJobConfig.class }, properties = "spring.batch.job.enabled=false")
class SimpleExportStepTest {

	private static final String OUTPUT_DIR = "target/output";
	private static final String OUTPUT_FILE = OUTPUT_DIR + "/simple-export-null.csv";
	private static final String EXPECTED_FILE = "src/test/resources/datas/csv/export-expected.csv";

	@Autowired
	private JobLauncherTestUtils testUtils;

	@Test
	void simpleExportStep_should_produce_expected_csv_file() throws Exception {
		// Given
		final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters()) //
				.addString("output-dir", OUTPUT_DIR) //
				.toJobParameters();
		// When
		final JobExecution jobExec = testUtils.launchStep("simple-export-step", jobParameters);

		// Then
		assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);

		AssertFile.assertFileEquals(new FileSystemResource(EXPECTED_FILE), //
				new FileSystemResource(OUTPUT_FILE));
	}

}
