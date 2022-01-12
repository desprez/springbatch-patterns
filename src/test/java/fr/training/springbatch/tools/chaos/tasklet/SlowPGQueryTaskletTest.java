package fr.training.springbatch.tools.chaos.tasklet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Test upon RandomFailTasket class
 */
@ExtendWith(MockitoExtension.class)
class SlowPGQueryTaskletTest {

	@Mock
	private JdbcTemplate jdbcTemplate;

	@Test
	void execute_should_success() throws Exception {
		// Given
		final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
		final StepContribution contrib = new StepContribution(stepExecution);
		final ChunkContext context = new ChunkContext(new StepContext(stepExecution));
		final SlowPGQueryTasklet slowPGQueryTasklet = new SlowPGQueryTasklet();
		slowPGQueryTasklet.setDuration(Duration.ofSeconds(60));
		slowPGQueryTasklet.setMaxQueries(2);
		slowPGQueryTasklet.setJdbcTemplate(jdbcTemplate);

		// When
		RepeatStatus status = slowPGQueryTasklet.execute(contrib, context);

		// Then
		assertThat(status).isEqualTo(RepeatStatus.CONTINUABLE);

		// And when
		status = slowPGQueryTasklet.execute(contrib, context);

		// Then
		assertThat(status).isEqualTo(RepeatStatus.FINISHED);
	}

}
