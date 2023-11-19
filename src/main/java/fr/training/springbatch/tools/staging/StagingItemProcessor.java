package fr.training.springbatch.tools.staging;

import javax.sql.DataSource;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Marks the input row as 'processed'. (This change will rollback if there is problem later)
 *
 * @param <T>
 *            item type
 *
 * @see StagingItemReader
 * @see StagingItemWriter
 * @see ProcessIndicatorItemWrapper
 *
 * @author Robert Kasanicky
 */
public class StagingItemProcessor<T> implements ItemProcessor<ProcessIndicatorItemWrapper<T>, T>, InitializingBean {

    private JdbcOperations jdbcTemplate;

    public void setJdbcTemplate(final JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setDataSource(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(jdbcTemplate, "Either jdbcTemplate or dataSource must be set");
    }

    /**
     * Use the technical identifier to mark the input row as processed and return unwrapped item.
     */
    @Nullable
    @Override
    public T process(final @NonNull ProcessIndicatorItemWrapper<T> wrapper) throws Exception {

        final int count = jdbcTemplate.update("UPDATE BATCH_STAGING SET PROCESSED=? WHERE ID=? AND PROCESSED=?", StagingItemWriter.DONE, wrapper.getId(),
                StagingItemWriter.NEW);
        if (count != 1) {
            throw new OptimisticLockingFailureException("The staging record with ID=" + wrapper.getId()
                    + " was updated concurrently when trying to mark as complete (updated " + count + " records.");
        }
        return wrapper.getItem();
    }

}