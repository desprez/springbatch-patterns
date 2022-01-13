package fr.training.springbatch.tools.tasklet;

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ActiveProfiles;

import fr.training.springbatch.job.BatchTestConfiguration;

/**
 * Unit test of the {@link RemoveSpringBatchHistoryTasklet}
 *
 */
@ActiveProfiles("test")
@SpringBootTest(classes = { BatchTestConfiguration.class }, properties = "spring.batch.job.enabled=false")
class RemoveSpringBatchHistoryTaskletTest {

	@Autowired
	protected JobExplorer jobExplorer;

	@Autowired
	protected DataSource dataSource;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void execute() throws Exception {
		// 1. Prepare test dataset
		final Resource sqlScript = new ClassPathResource("datas/tasklet/TestRemoveSpringBatchHistoryTasklet.sql");
		// The JdbcTestUtils is using the deprecated SimpleJdbcTemplate, so we don't
		// have the choice
		ScriptUtils.executeSqlScript(dataSource.getConnection(), sqlScript);

		// 2. Check the dataset before removing history
		List<JobInstance> jobInstances = jobExplorer.getJobInstances("jobTest", 0, 5);
		assertEquals("2 job instances before the purge", 2, jobInstances.size());

		final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		final StepContribution stepContribution = new StepContribution(stepExecution);
		final ChunkContext chunkContext = new ChunkContext(new StepContext(stepExecution));

		// 3. Execute the tested method
		final RemoveSpringBatchHistoryTasklet tasklet = new RemoveSpringBatchHistoryTasklet();
		tasklet.setJdbcTemplate(jdbcTemplate);
		tasklet.execute(stepContribution, chunkContext);

		// 4. Assertions
		assertEquals("6 lines should be deleted from the history", 6, stepContribution.getWriteCount());
		jobInstances = jobExplorer.getJobInstances("jobTest", 0, 5);
		assertEquals("Just a single job instance after the delete", 1, jobInstances.size());
		final JobInstance jobInstance = jobInstances.get(0);
		assertEquals("Only the job instance number 2 should remain into the history", Long.valueOf(-102),
				jobInstance.getId());
	}
}