package fr.training.springbatch.tools.chaos.tasklet;

import fr.training.springbatch.tools.chaos.BatchChaosException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test upon BipolarTasklet class
 */
@ExtendWith(SpringExtension.class)
class BipolarTaskletTest {

    private static final long FIRST_LAUNCH = 1L;
    private static final long SECOND_LAUNCH = 2L;

    @Test
    void execute_with_odd_job_instanceId_should_success() {
        // Given
        final JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution("myJob", FIRST_LAUNCH, 1L);
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobExecution, "MyStep", 1L);
        final StepContribution contrib = new StepContribution(stepExecution);
        final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

        final BipolarTasklet bipolarTasklet = new BipolarTasklet();

        // Then
        assertDoesNotThrow(() -> {
            // When
            bipolarTasklet.execute(contrib, context);
        });
    }

    @Test
    void execute_with_even_job_instanceId_should_fails()  {
        // Given
        final JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution("myJob", SECOND_LAUNCH, 1L);
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobExecution, "MyStep", 1L);
        final StepContribution contrib = new StepContribution(stepExecution);
        final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

        final BipolarTasklet bipolarTasklet = new BipolarTasklet();

        // Then
        final Throwable exceptionThatWasThrown = assertThrows(BatchChaosException.class, () -> {
            // When
            bipolarTasklet.execute(contrib, context);
        });
        assertThat(exceptionThatWasThrown.getMessage()).isEqualTo("BipolarTasklet expected fail");
    }
}