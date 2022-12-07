package fr.training.springbatch.tools.chaos.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;

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
 * Test upon RandomFailTasket class
 */
@ExtendWith(MockitoExtension.class)
class SlowPGQueryTaskletTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Captor
    private ArgumentCaptor<String> captor;

    @Test
    void execute_with_fixed_mode_should_success() throws Exception {
        // Given
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
        final StepContribution contrib = new StepContribution(stepExecution);
        final ChunkContext context = new ChunkContext(new StepContext(stepExecution));
        final SlowPGQueryTasklet slowPGQueryTasklet = new SlowPGQueryTasklet();
        slowPGQueryTasklet.setDuration(Duration.ofSeconds(30));
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

        verify(jdbcTemplate, times(2)).execute(captor.capture());
        assertThat(captor.getAllValues().get(0)).isEqualTo("select pg_sleep(30);");
        assertThat(captor.getAllValues().get(1)).isEqualTo("select pg_sleep(30);");
    }

    @Test
    void execute_with_random_mode_should_success() throws Exception {
        // Given
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
        final StepContribution contrib = new StepContribution(stepExecution);
        final ChunkContext context = new ChunkContext(new StepContext(stepExecution));
        final SlowPGQueryTasklet slowPGQueryTasklet = new SlowPGQueryTasklet();
        slowPGQueryTasklet.setDuration(Duration.ofSeconds(30));
        slowPGQueryTasklet.setMaxQueries(2);
        slowPGQueryTasklet.setJdbcTemplate(jdbcTemplate);
        slowPGQueryTasklet.setMode(SlowPGQueryTasklet.Mode.RANDOM);

        // When
        RepeatStatus status = slowPGQueryTasklet.execute(contrib, context);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.CONTINUABLE);

        // And when
        status = slowPGQueryTasklet.execute(contrib, context);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        verify(jdbcTemplate, times(2)).execute(captor.capture());
        assertThat(captor.getAllValues().get(0)).contains("select pg_sleep");
        assertThat(captor.getAllValues().get(1)).contains("select pg_sleep");
    }

    @Test
    void execute_with_ramptup_mode_should_success() throws Exception {
        // Given
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
        final StepContribution contrib = new StepContribution(stepExecution);
        final ChunkContext context = new ChunkContext(new StepContext(stepExecution));
        final SlowPGQueryTasklet slowPGQueryTasklet = new SlowPGQueryTasklet();
        slowPGQueryTasklet.setDuration(Duration.ofSeconds(30));
        slowPGQueryTasklet.setMaxQueries(2);
        slowPGQueryTasklet.setJdbcTemplate(jdbcTemplate);
        slowPGQueryTasklet.setMode(SlowPGQueryTasklet.Mode.RAMPUP);

        // When
        RepeatStatus status = slowPGQueryTasklet.execute(contrib, context);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.CONTINUABLE);

        // And when
        status = slowPGQueryTasklet.execute(contrib, context);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        verify(jdbcTemplate, times(2)).execute(captor.capture());
        assertThat(captor.getAllValues().get(0)).isEqualTo("select pg_sleep(15);");
        assertThat(captor.getAllValues().get(1)).isEqualTo("select pg_sleep(30);");
    }
}
