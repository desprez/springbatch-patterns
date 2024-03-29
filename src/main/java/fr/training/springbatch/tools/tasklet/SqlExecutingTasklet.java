package fr.training.springbatch.tools.tasklet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.util.ExecutionContextUserSupport;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A {@link Tasklet} that run a given SQL queries/commands list. The executionContext is use to store the execution status (postion in the SQL list) that allow
 * to be restartable.
 *
 *
 * Example :
 *
 * <pre>
 * &#64;Bean
 * public SqlExecutingTasklet sqlExecutingTasklet() {
 *
 *     final SqlExecutingTasklet tasklet = new SqlExecutingTasklet();
 *     tasklet.tasklet.setJdbcTemplate(jdbcTemplate);
 *     tasklet.setSqlCommands("CREATE TABLE Foo;", "DROP TABLE bar;");
 *
 *     return tasklet;
 * }
 * </pre>
 *
 * @author Morten Andersen-Gott
 * @author Desprez (simplify a lot)
 */
public class SqlExecutingTasklet implements Tasklet {

    private static final Logger logger = LoggerFactory.getLogger(SqlExecutingTasklet.class);

    private static final String EXECUTION_COUNT = "sql.execution.count";

    private JdbcTemplate jdbcTemplate;
    private final ExecutionContextUserSupport ecSupport;
    private String[] sqlCommands;
    private int count;

    public SqlExecutingTasklet() {
        ecSupport = new ExecutionContextUserSupport(SqlExecutingTasklet.class.getSimpleName());
    }

    public SqlExecutingTasklet(final JdbcTemplate jdbcTemplate, final String... sqlCommands) {
        this();
        this.jdbcTemplate = jdbcTemplate;
        this.sqlCommands = sqlCommands;
    }

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        count = getCount(getExecutionContext(chunkContext));
        final String sqlCommand = sqlCommands[count];

        logger.info("executing : {}", sqlCommand);
        jdbcTemplate.execute(sqlCommand);

        incrementCount(getExecutionContext(chunkContext));

        return RepeatStatus.continueIf(count < sqlCommands.length);
    }

    private ExecutionContext getExecutionContext(final ChunkContext chunkContext) {
        return chunkContext.getStepContext().getStepExecution().getExecutionContext();
    }

    public int getCount(final ExecutionContext executionContext) {
        return executionContext.containsKey(ecSupport.getKey(EXECUTION_COUNT)) ? executionContext.getInt(ecSupport.getKey(EXECUTION_COUNT)) : 0;
    }

    public void incrementCount(final ExecutionContext executionContext) {
        executionContext.putInt(ecSupport.getKey(EXECUTION_COUNT), ++count);
    }

    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setSqlCommands(final String... sqlCommands) {
        this.sqlCommands = sqlCommands;
    }

}