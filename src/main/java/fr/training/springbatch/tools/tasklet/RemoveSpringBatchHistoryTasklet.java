package fr.training.springbatch.tools.tasklet;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Tasklet used to delete data from Spring Batch Metadata tables that are N months old.
 *
 * <p>
 * The row numbers in the 6 tables of Spring Batch may increase a lot. This tasklet cleans the Spring Batch database by removing old job instances executions
 * and keep the history of recent job executions (last 6 months by default).<br>
 * Spring Batch tables prefix could be customized by the {@link #setTablePrefix(String)}<br>
 * Thanks to Giovanni Dalloglio for his initial SQL statements.
 * </p>
 *
 * @see https://jira.springsource.org/browse/BATCH-1747
 * @author arey, desprez (using LocalDateTime instead java.util.date).
 *
 */
public class RemoveSpringBatchHistoryTasklet implements Tasklet, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveSpringBatchHistoryTasklet.class);

    /**
     * SQL statements removing step and job executions compared to a given date.
     */
    private static final String SQL_DELETE_BATCH_STEP_EXECUTION_CONTEXT = "DELETE FROM %PREFIX%STEP_EXECUTION_CONTEXT WHERE STEP_EXECUTION_ID IN (SELECT STEP_EXECUTION_ID FROM %PREFIX%STEP_EXECUTION WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM  %PREFIX%JOB_EXECUTION where CREATE_TIME < ?))";
    private static final String SQL_DELETE_BATCH_STEP_EXECUTION = "DELETE FROM %PREFIX%STEP_EXECUTION WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM %PREFIX%JOB_EXECUTION where CREATE_TIME < ?)";
    private static final String SQL_DELETE_BATCH_JOB_EXECUTION_CONTEXT = "DELETE FROM %PREFIX%JOB_EXECUTION_CONTEXT WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM  %PREFIX%JOB_EXECUTION where CREATE_TIME < ?)";
    private static final String SQL_DELETE_BATCH_JOB_EXECUTION_PARAMS = "DELETE FROM %PREFIX%JOB_EXECUTION_PARAMS WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID FROM %PREFIX%JOB_EXECUTION where CREATE_TIME < ?)";
    private static final String SQL_DELETE_BATCH_JOB_EXECUTION = "DELETE FROM %PREFIX%JOB_EXECUTION where CREATE_TIME < ?";
    private static final String SQL_DELETE_BATCH_JOB_INSTANCE = "DELETE FROM %PREFIX%JOB_INSTANCE WHERE JOB_INSTANCE_ID NOT IN (SELECT JOB_INSTANCE_ID FROM %PREFIX%JOB_EXECUTION)";

    /**
     * Default value for the table prefix property.
     */
    private static final String DEFAULT_TABLE_PREFIX = AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

    /**
     * Default value for the data retention (in month)
     */
    private static final Integer DEFAULT_RETENTION_MONTH = 6;

    private String tablePrefix = DEFAULT_TABLE_PREFIX;

    private Integer historyRetentionMonth = DEFAULT_RETENTION_MONTH;

    private JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) {
        int totalCount = 0;
        final LocalDateTime dateTime = LocalDateTime.now().minusMonths(historyRetentionMonth);
        LOG.info("Remove the Spring Batch history before the {}", dateTime);

        int rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_STEP_EXECUTION_CONTEXT), dateTime);
        LOG.info("Deleted rows number from the BATCH_STEP_EXECUTION_CONTEXT table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_STEP_EXECUTION), dateTime);
        LOG.info("Deleted rows number from the BATCH_STEP_EXECUTION table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_EXECUTION_CONTEXT), dateTime);
        LOG.info("Deleted rows number from the BATCH_JOB_EXECUTION_CONTEXT table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_EXECUTION_PARAMS), dateTime);
        LOG.info("Deleted rows number from the BATCH_JOB_EXECUTION_PARAMS table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_EXECUTION), dateTime);
        LOG.info("Deleted rows number from the BATCH_JOB_EXECUTION table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_INSTANCE));
        LOG.info("Deleted rows number from the BATCH_JOB_INSTANCE table: {}", rowCount);
        totalCount += rowCount;

        contribution.incrementWriteCount(totalCount);

        return RepeatStatus.FINISHED;
    }

    protected String getQuery(final String base) {
        return StringUtils.replace(base, "%PREFIX%", tablePrefix);
    }

    public void setTablePrefix(final String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public void setHistoryRetentionMonth(final Integer historyRetentionMonth) {
        this.historyRetentionMonth = historyRetentionMonth;
    }

    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(jdbcTemplate, "The jdbcTemplate must not be null");
    }

}