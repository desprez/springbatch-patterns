package fr.training.springbatch.tools.tasklet;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * A {link Tasklet} that execute Oracle's GATHER_TABLE_STATS command upon given Tables of the given schema.
 */
public class GatherStatisticTasklet implements Tasklet {

    private String[] tableNames;

    private String schema;

    private JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {

        final StringBuilder sql = new StringBuilder();
        sql.append(" BEGIN   ");

        for (int i = 0; i < tableNames.length; i++) {
            sql.append("DBMS_STATS.GATHER_TABLE_STATS('").append(schema).append("','").append(tableNames[i]).append("') ; ");
            contribution.incrementWriteCount(1);
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
