package fr.training.springbatch.tools.tasklet;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.launch.NoSuchJobInstanceException;
import org.springframework.batch.core.repository.dao.AbstractJdbcBatchMetadataDao;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class RemoveOneJobHistoryTasklet implements Tasklet, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(RemoveSpringBatchHistoryTasklet.class);

    /**
     * SQL statements removing step and job executions for a given job name.
     */

    private static final String SQL_FIND_JOB_INSTANCE_ID = """
            SELECT JOB_INSTANCE_ID
            FROM %PREFIX%JOB_INSTANCE
            WHERE JOB_NAME = ?
            ORDER BY JOB_INSTANCE_ID DESC
            """;
    private static final String SQL_DELETE_BATCH_STEP_EXECUTION_CONTEXT = """
            DELETE FROM %PREFIX%STEP_EXECUTION_CONTEXT
            WHERE STEP_EXECUTION_ID IN (SELECT STEP_EXECUTION_ID
                                        FROM %PREFIX%STEP_EXECUTION
                                        WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID
                                                                   FROM  %PREFIX%JOB_EXECUTION
                                                                   WHERE JOB_INSTANCE_ID = ?))
            """;
    private static final String SQL_DELETE_BATCH_STEP_EXECUTION = """
            DELETE FROM %PREFIX%STEP_EXECUTION
            WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID
                                       FROM %PREFIX%JOB_EXECUTION
                                       WHERE JOB_INSTANCE_ID = ?)
            """;
    private static final String SQL_DELETE_BATCH_JOB_EXECUTION_CONTEXT = """
            DELETE FROM %PREFIX%JOB_EXECUTION_CONTEXT
            WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID
                                       FROM  %PREFIX%JOB_EXECUTION
                                       WHERE JOB_INSTANCE_ID = ?)
            """;
    private static final String SQL_DELETE_BATCH_JOB_EXECUTION_PARAMS = """
            DELETE FROM %PREFIX%JOB_EXECUTION_PARAMS
            WHERE JOB_EXECUTION_ID IN (SELECT JOB_EXECUTION_ID
                                       FROM %PREFIX%JOB_EXECUTION
                                       WHERE JOB_INSTANCE_ID = ?)
            """;
    private static final String SQL_DELETE_BATCH_JOB_EXECUTION = """
            DELETE FROM %PREFIX%JOB_EXECUTION
            WHERE JOB_INSTANCE_ID = ?
            """;
    private static final String SQL_DELETE_BATCH_JOB_INSTANCE = """
            DELETE FROM %PREFIX%JOB_INSTANCE
            WHERE JOB_INSTANCE_ID = ?
            """;

    /**
     * Default value for the table prefix property.
     */
    private static final String DEFAULT_TABLE_PREFIX = AbstractJdbcBatchMetadataDao.DEFAULT_TABLE_PREFIX;

    private String tablePrefix = DEFAULT_TABLE_PREFIX;

    private String jobName;

    private JdbcTemplate jdbcTemplate;

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws NoSuchJobInstanceException {
        int totalCount = 0;

        LOG.info("Remove the Spring Batch history for job {}", jobName);

        final List<Long> instanceIdList = jdbcTemplate.queryForList(getQuery(SQL_FIND_JOB_INSTANCE_ID), Long.class, jobName);
        if (instanceIdList.isEmpty()) {
            throw new NoSuchJobInstanceException(String.format("No job instance with jobName=%s", jobName));
        }

        final Long instanceId = instanceIdList.get(0);

        int rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_STEP_EXECUTION_CONTEXT), instanceId);
        LOG.info("Deleted rows number from the BATCH_STEP_EXECUTION_CONTEXT table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_STEP_EXECUTION), instanceId);
        LOG.info("Deleted rows number from the BATCH_STEP_EXECUTION table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_EXECUTION_CONTEXT), instanceId);
        LOG.info("Deleted rows number from the BATCH_JOB_EXECUTION_CONTEXT table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_EXECUTION_PARAMS), instanceId);
        LOG.info("Deleted rows number from the BATCH_JOB_EXECUTION_PARAMS table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_EXECUTION), instanceId);
        LOG.info("Deleted rows number from the BATCH_JOB_EXECUTION table: {}", rowCount);
        totalCount += rowCount;

        rowCount = jdbcTemplate.update(getQuery(SQL_DELETE_BATCH_JOB_INSTANCE), instanceId);
        LOG.info("Deleted rows number from the BATCH_JOB_INSTANCE table: {}", rowCount);
        totalCount += rowCount;

        contribution.incrementWriteCount(totalCount);

        if (instanceIdList.size() > 1) {
            return RepeatStatus.CONTINUABLE;
        }
        return RepeatStatus.FINISHED;
    }

    protected String getQuery(final String base) {
        return StringUtils.replace(base, "%PREFIX%", tablePrefix);
    }

    public void setTablePrefix(final String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public void setJobName(final String jobName) {
        this.jobName = jobName;
    }

    public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(jdbcTemplate, "The jdbcTemplate must not be null");
        Assert.notNull(jobName, "The jobName must not be null");
    }

}
