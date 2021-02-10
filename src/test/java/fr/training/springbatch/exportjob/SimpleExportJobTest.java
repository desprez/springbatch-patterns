package fr.training.springbatch.exportjob;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.training.springbatch.BatchTestConfiguration;
import fr.training.springbatch.tools.listener.JobReportListener;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { BatchTestConfiguration.class, SimpleExportJobConfig.class, JobReportListener.class })
public class SimpleExportJobTest {

	@Autowired
	private JobLauncherTestUtils testUtils;

	@Autowired
	@Qualifier("simpleExportJob")
	private Job simpleExportJob;

	@Test
	public void launch_SimpleExportJob_nominal_should_success() throws Exception {
		// Given
		testUtils.setJob(simpleExportJob);
		final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
				.addString("output-dir", "target/output").toJobParameters();
		// When
		final JobExecution jobExec = testUtils.launchJob(jobParameters);
		// Then
		assertThat(jobExec.getStatus(), equalTo(BatchStatus.COMPLETED));
	}

}