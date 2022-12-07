package fr.training.springbatch.tools.chaos.tasklet;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;

/**
 * Test upon WaitingTasklet class
 */
class WaitingTaskletTest {

    private static final int ONE_SECOND_DURATION = 1;

    @Test
    void execute_should_success() throws Exception {
        // Given
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();
        final StepContribution contrib = new StepContribution(stepExecution);
        final ChunkContext context = new ChunkContext(new StepContext(stepExecution));
        final WaitingTasklet waitingTasklet = new WaitingTasklet(Duration.ofSeconds(ONE_SECOND_DURATION));

        // When
        final RepeatStatus status = waitingTasklet.execute(contrib, context);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

}
