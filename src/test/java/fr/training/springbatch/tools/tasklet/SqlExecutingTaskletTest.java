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
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class SqlExecutingTaskletTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	@Captor
	private ArgumentCaptor<String> captor;

	@Test
	void execute_should_success() throws Exception {
		// Given
		final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		final ExecutionContext executionContext = new ExecutionContext();
		stepExecution.setExecutionContext(executionContext);
		final StepContribution contribution = new StepContribution(stepExecution);
		final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

		final SqlExecutingTasklet tasklet = new SqlExecutingTasklet();
		tasklet.setJdbcTemplate(jdbcTemplate);
		tasklet.setSqlCommands("CREATE TABLE Foo;", "DROP TABLE Foo;");
		tasklet.setExecutionContext(executionContext);

		// When
		RepeatStatus status = tasklet.execute(contribution, context);

		// Then
		assertThat(status).isEqualTo(RepeatStatus.CONTINUABLE);

		// When
		status = tasklet.execute(contribution, context);

		// Then
		assertThat(status).isEqualTo(RepeatStatus.FINISHED);

		verify(jdbcTemplate, times(2)).execute(captor.capture());
		assertThat(captor.getAllValues().get(0)).isEqualTo("CREATE TABLE Foo;");
		assertThat(captor.getAllValues().get(1)).isEqualTo("DROP TABLE Foo;");
	}

}
