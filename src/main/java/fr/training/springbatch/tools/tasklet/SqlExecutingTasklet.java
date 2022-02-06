package fr.training.springbatch.tools.tasklet;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

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
 * A {@link Tasklet} that run a given SQL queries list. The executionContext is
 * use to store the execution status (postion in the SQL list) that allow to be
 * restartable.
 *
 *
 * Example :
 *
 * <pre>
 * &#64;StepScope // Mandatory for using stepExecution
 * &#64;Bean
 * public SqlExecutingTasklet sqlExecutingTasklet(final DataSource dataSource,
 * 		&#64;Value("#{stepExecution['executionContext']}") final ExecutionContext executionContext) {
 *
 * 	final SqlExecutingTasklet tasklet = new SqlExecutingTasklet();
 * 	tasklet.setDataSource(dataSource);
 * 	tasklet.setExecutionContext(executionContext);
 * 	final List<String> sqls = new ArrayList<>();
 * 	sqls.add(
 * 			"INSERT INTO Transaction(customer_number, number, transaction_date, amount) VALUES (1, '17878496', '2022-09-12', 99.55)");
 * 	sqls.add(
 * 			"INSERT INTO Transaction(customer_number, number, transaction_date, amount) VALUES (2, '17888399', '2022-05-30', 11.75)");
 * 	tasklet.setSqls(sqls);
 *
 * 	return tasklet;
 * }
 * </pre>
 *
 * @author Morten Andersen-Gott
 * @author Desprez
 */
public class SqlExecutingTasklet implements Tasklet {

	private static final Logger log = LoggerFactory.getLogger(SqlExecutingTasklet.class);

	private final static String EXECUTION_COUNT = "sql.execution.count";

	private JdbcTemplate jdbcTemplate;
	private final ExecutionContextUserSupport ecSupport;
	private String[] sqls;
	private int count = 0;
	private ExecutionContext executionContext;

	public SqlExecutingTasklet() {
		ecSupport = new ExecutionContextUserSupport(SqlExecutingTasklet.class.getSimpleName());
	}

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
		count = getCount();
		final String sql = sqls[count];

		if (sql.trim().toUpperCase().startsWith("SELECT")) {
			final int updateCount = jdbcTemplate.update(sql);
			contribution.incrementWriteCount(updateCount);
		} else {
			// jdbcTemplate.execute(sql);

			final List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
			final String msg = "Result: " + result;
			log.info(msg);
		}

		incrementCount();

		return RepeatStatus.continueIf(count < sqls.length);
	}

	public void setDataSource(final DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public int getCount() {
		return executionContext.containsKey(ecSupport.getKey(EXECUTION_COUNT))
				? executionContext.getInt(ecSupport.getKey(EXECUTION_COUNT))
						: 0;
	}

	public void incrementCount() {
		executionContext.putInt(ecSupport.getKey(EXECUTION_COUNT), ++count);
	}

	public void setSqls(final String... sqls) {
		this.sqls = sqls;
	}

	public void setExecutionContext(final ExecutionContext executionContext) {
		this.executionContext = executionContext;
	}

}