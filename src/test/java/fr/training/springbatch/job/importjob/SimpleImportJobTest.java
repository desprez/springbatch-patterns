package fr.training.springbatch.job.importjob;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
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

	@Autowired
	private JobLauncherTestUtils testUtils;

	@Test
	public void launch_SimpleImportJob_nominal_should_success() throws Exception {
		// Given
		final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
				.addString("input-file", "src/main/resources/csv/transaction.csv").toJobParameters();
		// When
		final JobExecution jobExec = testUtils.launchJob(jobParameters);
		// Then
		assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);
	}

}
