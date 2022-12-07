package fr.training.springbatch.tools.staging;

import javax.sql.DataSource;

import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

/**
 * Thread-safe database {@link ItemReader} implementing the process indicator pattern.
 */
public class StagingItemListener extends StepListenerSupport<Long, Long> implements InitializingBean {

    private JdbcOperations jdbcTemplate;

    public void setDataSource(final DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        Assert.notNull(jdbcTemplate, "You must provide a DataSource.");
    }

    @Override
    public void afterRead(final Long id) {
        final int count = jdbcTemplate.update("UPDATE BATCH_STAGING SET PROCESSED=? WHERE ID=? AND PROCESSED=?", StagingItemWriter.DONE, id,
                StagingItemWriter.NEW);
        if (count != 1) {
            throw new OptimisticLockingFailureException(
                    "The staging record with ID=" + id + " was updated concurrently when trying to mark as complete (updated " + count + " records.");
        }
    }

}