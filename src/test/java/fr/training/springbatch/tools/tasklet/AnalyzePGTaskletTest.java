package fr.training.springbatch.tools.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Test upon AnalyzePGTasklet class
 */
@ExtendWith(MockitoExtension.class)
class AnalyzePGTaskletTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	@Captor
	private ArgumentCaptor<String> captor;

	@Test
	void execute_with_all_expected_fields_should_success() throws Exception {
		// Given
		final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		final StepContribution contribution = new StepContribution(stepExecution);
		final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

		final AnalyzePGTasklet tasklet = new AnalyzePGTasklet();
		tasklet.setJdbcTemplate(jdbcTemplate);
		tasklet.setTableNames("my1stTable", "my2ndTable");
		tasklet.setSchema("schema");

		// When
		final RepeatStatus status = tasklet.execute(contribution, context);

		// Then
		assertThat(status).isEqualTo(RepeatStatus.FINISHED);
		verify(jdbcTemplate, times(2)).execute(captor.capture());
		assertThat(captor.getAllValues().get(0)).isEqualTo("ANALYSE schema.my1stTable;");
		assertThat(captor.getAllValues().get(1)).isEqualTo("ANALYSE schema.my2ndTable;");
	}

}
