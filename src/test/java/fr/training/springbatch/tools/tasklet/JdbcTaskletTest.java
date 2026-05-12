package fr.training.springbatch.tools.tasklet;

import fr.training.springbatch.job.BatchTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.test.context.ContextConfiguration;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = { BatchTestConfiguration.class })
@JdbcTest
class JdbcTaskletTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void testExecute() throws Exception {

        // Given
        final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();

        final StepContribution contribution = new StepContribution(stepExecution);
        final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

        final JdbcTasklet tasklet = new JdbcTasklet();
        tasklet.setDataSource(dataSource);
        tasklet.setSql("select 1 from dual");

        // When
        final RepeatStatus status = tasklet.execute(contribution, context);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

}
