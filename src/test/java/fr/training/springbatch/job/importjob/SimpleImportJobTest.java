package fr.training.springbatch.job.importjob;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import fr.training.springbatch.job.BatchTestConfiguration;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class,
		SimpleImportJobConfig.class }, properties = "spring.batch.job.enabled=false")
public class SimpleImportJobTest {

	private static final String REJECT_FILE_PATH = "target/output/reject.csv";
	@Autowired
	private JobLauncherTestUtils testUtils;

	@Before
	public void cleanUp() throws IOException {
		final File rejectFile = new File(REJECT_FILE_PATH);
		if (rejectFile.exists()) {
			FileUtils.deleteQuietly(rejectFile);
		}
	}

	@Test
	public void launch_SimpleImportJob_nominal_should_success() throws Exception {
		// Given
		final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
				.addString("input-file", "src/main/resources/csv/transaction.csv") //
				.addString("rejectfile", REJECT_FILE_PATH) //
				.toJobParameters();
		// When
		final JobExecution jobExec = testUtils.launchJob(jobParameters);
		// Then
		assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(new File(REJECT_FILE_PATH)).doesNotExist();

		// And expected read / write counts
		final Optional<StepExecution> executionOpt = jobExec.getStepExecutions().stream().filter(e -> {
			return "simple-import-step".equals(e.getStepName());
		}).findFirst();
		assertThat(executionOpt.isPresent()).isTrue();
		final StepExecution stepExec = executionOpt.get();

		assertThat(stepExec.getReadCount()).isEqualTo(310);
		assertThat(stepExec.getWriteCount()).isEqualTo(310);
	}

	@Test
	public void launch_SimpleImportJob_with_bad_records_should_success_with_reject_file() throws Exception {
		// Given
		final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
				.addString("input-file", "src/main/resources/csv/transaction_bad.csv") //
				.addString("rejectfile", REJECT_FILE_PATH) //
				.toJobParameters();
		// When
		final JobExecution jobExec = testUtils.launchJob(jobParameters);
		// Then
		assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
		assertThat(new File(REJECT_FILE_PATH)).exists();
		assertThat(new File(REJECT_FILE_PATH))
		.hasContent("019;17878417;19-08-31;3.96\r\n" + "025;17878461;2019-08-12;X76.23\r\n");

		// And expected read / write / skip counts
		final Optional<StepExecution> executionOpt = jobExec.getStepExecutions().stream().filter(e -> {
			return "simple-import-step".equals(e.getStepName());
		}).findFirst();
		assertThat(executionOpt.isPresent()).isTrue();
		final StepExecution stepExec = executionOpt.get();

		assertThat(stepExec.getReadCount()).isEqualTo(308);
		assertThat(stepExec.getWriteCount()).isEqualTo(308);
		assertThat(stepExec.getSkipCount()).isEqualTo(2);
	}

}
