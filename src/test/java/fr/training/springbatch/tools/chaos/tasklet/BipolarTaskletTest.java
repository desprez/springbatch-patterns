package fr.training.springbatch.tools.chaos.tasklet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import fr.training.springbatch.tools.chaos.BatchChaosException;

/**
 * Test upon BipolarTasklet class
 */
@ExtendWith(SpringExtension.class)
class BipolarTaskletTest {

    private static final long FIRST_LAUNCH = 1L;
    private static final long SECOND_LAUNCH = 2L;

    @Test
    void execute_with_odd_job_instanceId_should_success() throws Exception {
        // Given
        final JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution("myJob", FIRST_LAUNCH, 1L);
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobExecution, "MyStep", null);
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
    void execute_with_even_job_instanceId_should_fails() throws Exception {
        // Given
        final JobExecution jobExecution = MetaDataInstanceFactory.createJobExecution("myJob", SECOND_LAUNCH, 1L);
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution(jobExecution, "MyStep", null);
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