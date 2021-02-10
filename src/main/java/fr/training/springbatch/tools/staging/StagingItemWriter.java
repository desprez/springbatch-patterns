package fr.training.springbatch.tools.staging;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.SerializationUtils;

/**
 * Database {@link ItemWriter} implementing the process indicator pattern.
 */
public class StagingItemWriter<T> extends JdbcDaoSupport implements StepExecutionListener, ItemWriter<T> {

	protected static final String NEW = "N";

	protected static final String DONE = "Y";

	private DataFieldMaxValueIncrementer incrementer;

	private StepExecution stepExecution;

	/**
	 * Check mandatory properties.
	 *
	 * @see org.springframework.dao.support.DaoSupport#initDao()
	 */
	@Override
	protected void initDao() throws Exception {
		super.initDao();
		Assert.notNull(incrementer, "DataFieldMaxValueIncrementer is required - set the incrementer property in the "
				+ ClassUtils.getShortName(StagingItemWriter.class));
	}

	/**
	 * Setter for the key generator for the staging table.
	 *
	 * @param incrementer the {@link DataFieldMaxValueIncrementer} to set
	 */
	public void setIncrementer(final DataFieldMaxValueIncrementer incrementer) {
		this.incrementer = incrementer;
	}

	/**
	 * Serialize the item to the staging table, and add a NEW processed flag.
	 *
	 * @see ItemWriter#write(java.util.List)
	 */
	@Override
	public void write(final List<? extends T> items) {
		final ListIterator<? extends T> itemIterator = items.listIterator();

		getJdbcTemplate().batchUpdate("INSERT into BATCH_STAGING (ID, JOB_ID, VALUE, PROCESSED) values (?,?,?,?)",
				new BatchPreparedStatementSetter() {
			@Override
			public int getBatchSize() {
				return items.size();
			}

			@Override
			public void setValues(final PreparedStatement ps, final int i) throws SQLException {
				Assert.state(itemIterator.nextIndex() == i, "Item ordering must be preserved in batch sql update");

				ps.setLong(1, incrementer.nextLongValue());
				ps.setLong(2, stepExecution.getJobExecution().getJobId());
				ps.setBytes(3, SerializationUtils.serialize(itemIterator.next()));
				ps.setString(4, NEW);
			}
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.springframework.batch.core.domain.StepListener#afterStep(StepExecution
	 * )
	 */
	@Nullable
	@Override
	public ExitStatus afterStep(final StepExecution stepExecution) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.batch.core.domain.StepListener#beforeStep(org.
	 * springframework.batch.core.domain.StepExecution)
	 */
	@Override
	public void beforeStep(final StepExecution stepExecution) {
		this.stepExecution = stepExecution;
	}
}