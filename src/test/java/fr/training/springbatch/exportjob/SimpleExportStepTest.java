package fr.training.springbatch.exportjob;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.AssertFile;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.training.springbatch.BatchTestConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { BatchTestConfiguration.class, SimpleExportJobConfig.class})
public class SimpleExportStepTest {

	private static final String EXPECTED_FILE = "src/test/resources/datas/export-expected.csv";
	private static final String OUTPUT_DIR = "target/output";
	private static final String OUTPUT_FILE = OUTPUT_DIR + "/export-null.csv";

	@Autowired
	private JobLauncherTestUtils testUtils;

	@Test
	public void simpleExportStep_should_produce_expected_csv_file() throws Exception {
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
