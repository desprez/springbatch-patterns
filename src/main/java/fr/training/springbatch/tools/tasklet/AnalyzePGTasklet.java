package fr.training.springbatch.tools.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

public class AnalyzePGTasklet implements Tasklet {

	private String[] tableNames;

	private String schema;

	private JdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {

		final StringBuilder sql = new StringBuilder();
		sql.append(" BEGIN   ");

		for (int i = 0; i < tableNames.length; i++) {
			sql.append("DBMS_STATS.GATHER_TABLE_STATS('").append(schema).append("','").append(tableNames[i])
			.append("') ; ");
		}
		sql.append(" END;    ");

		jdbcTemplate.execute(sql.toString());
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
