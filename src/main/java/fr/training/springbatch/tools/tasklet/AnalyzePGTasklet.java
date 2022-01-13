package fr.training.springbatch.tools.tasklet;

import static org.springframework.util.Assert.notNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * This {@link Tasklet} execute PostgreSQL's ANALYSE command upon a given Tables
 * names and a given schema.
 *
 * This use a JDBCTemplate that ca be injected in the bean that declare this
 * Tasklet.
 */
public class AnalyzePGTasklet implements Tasklet {

	private static final Logger log = LoggerFactory.getLogger(AnalyzePGTasklet.class);

	private String[] tableNames;

	private String schema;

	private JdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
		notNull(tableNames, "A list names is required");

		for (final String tableName : tableNames) {
			final String sql = String.format("ANALYSE %s.%s;", schema, tableName);
			log.debug("Executing {}", sql);

			jdbcTemplate.execute(sql);

			contribution.incrementWriteCount(1);
		}
		return RepeatStatus.FINISHED;
	}

	public void setSchema(final String schema) {
		this.schema = schema;
	}

	public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void setTableNames(final String... tableNames) {
		this.tableNames = tableNames;
	}

}
