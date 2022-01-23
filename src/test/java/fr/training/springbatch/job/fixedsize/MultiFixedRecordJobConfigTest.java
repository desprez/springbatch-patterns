package fr.training.springbatch.job.fixedsize;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.SimpleDateFormat;

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
import fr.training.springbatch.job.fixedsize.MultiFixedRecordJobConfig;

@ActiveProfiles("test")
@SpringBatchTest
@SpringBootTest(classes = { BatchTestConfiguration.class, MultiFixedRecordJobConfig.class }, properties = {
		"spring.batch.job.enabled=false", "application.batch.transmitterCode=AP99325" })
class MultiFixedRecordJobConfigTest {

	private static final String OUTPUT_FILE_PATH = "target/fixedresult.txt";
	private static final String INPUT_FILE_PATH = "src/test/resources/datas/fixed/multirecordfile.txt";

	@Autowired
	private JobLauncherTestUtils testUtils;

	@Test
	void fixedJob_should_success() throws Exception {
		// Given
		final JobParameters jobParameters = new JobParametersBuilder(testUtils.getUniqueJobParameters())
				.addString("inputfile", INPUT_FILE_PATH) //
				.addString("outputfile", OUTPUT_FILE_PATH) //
				.addString("receivercode", "AP99530") //
				.addDate("created-date", new SimpleDateFormat("yyyy-MM-dd").parse("2021-05-31")) //
				.toJobParameters();
		// When
		final JobExecution jobExec = testUtils.launchJob(jobParameters);
		// Then
		assertThat(jobExec.getStatus()).isEqualTo(BatchStatus.COMPLETED);

		AssertFile.assertFileEquals(new FileSystemResource(INPUT_FILE_PATH), //
				new FileSystemResource(OUTPUT_FILE_PATH));
	}

}
