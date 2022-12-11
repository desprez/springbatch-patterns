package fr.training.springbatch.tools.tasklet;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ContextConfiguration;

import fr.training.springbatch.job.BatchTestConfiguration;

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
        tasklet.setSql("SELECT * FROM TRANSACTION");

        // When
        final RepeatStatus status = tasklet.execute(contribution, context);

        // Then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

}
