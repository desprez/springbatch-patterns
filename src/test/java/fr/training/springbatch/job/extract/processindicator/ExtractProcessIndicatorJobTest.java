package fr.training.springbatch.job.extract.processindicator;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;

import fr.training.springbatch.job.BatchTestConfiguration;
import fr.training.springbatch.job.extract.processindicatorjob.ExtractProcessIndicatorJobConfig;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class,
		ExtractProcessIndicatorJobConfig.class }, properties = "spring.batch.job.enabled=false")
class ExtractProcessIndicatorJobTest {

	private static final String OUTPUT_FILE = "target/output/extract_process_indicator.csv";
	private static final String EXPECTED_FILE = "src/test/resources/datas/csv/extract-expected.csv";

	@Autowired
	private JobLauncherTestUtils testUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void launch_ExtractProcessIndicatorJob_nominal_should_success() throws Exception {
		// Given
		final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
				.addString("output-file", OUTPUT_FILE) //
				.toJobParameters();
		// When
		final JobExecution jobExec = testUtils.launchJob(jobParameters);
		// Then
		assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);

		AssertFile.assertFileEquals(new FileSystemResource(EXPECTED_FILE), //
				new FileSystemResource(OUTPUT_FILE));

		assertThat(JdbcTestUtils.countRowsInTable(jdbcTemplate, "TRANSACTION")).isZero();
	}

}
