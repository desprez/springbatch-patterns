package fr.training.springbatch.tools.staging;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ReaderNotOpenException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.SerializationUtils;

/**
 * Thread-safe database {@link ItemReader} implementing the process indicator
 * pattern.
 *
 * To achieve restartability use together with {@link StagingItemProcessor}.
 */
public class StagingItemReader<T>
implements ItemReader<ProcessIndicatorItemWrapper<T>>, StepExecutionListener, InitializingBean, DisposableBean {

	private static Log logger = LogFactory.getLog(StagingItemReader.class);

	private StepExecution stepExecution;

	private final Object lock = new Object();

	private volatile boolean initialized = false;

	private volatile Iterator<Long> keys;

	private JdbcOperations jdbcTemplate;

	public void setDataSource(final DataSource dataSource) {
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Override
	public void destroy() throws Exception {
		initialized = false;
		keys = null;
	}

	@Override
	public final void afterPropertiesSet() throws Exception {
		Assert.notNull(jdbcTemplate, "You must provide a DataSource.");
	}

	private List<Long> retrieveKeys() {

		synchronized (lock) {

			return jdbcTemplate.query(

					"SELECT ID FROM BATCH_STAGING WHERE JOB_ID=? AND PROCESSED=? ORDER BY ID",

					new RowMapper<Long>() {
						@Override
						public Long mapRow(final ResultSet rs, final int rowNum) throws SQLException {
							return rs.getLong(1);
						}
					},

					stepExecution.getJobExecution().getJobId(), StagingItemWriter.NEW);
		}

	}

	@Nullable
	@Override
	public ProcessIndicatorItemWrapper<T> read() {
		if (!initialized) {
			throw new ReaderNotOpenException("Reader must be open before it can be used.");
		}

		Long id = null;
		synchronized (lock) {
			if (keys.hasNext()) {
				id = keys.next();
			}
		}
		logger.debug("Retrieved key from list: " + id);

		if (id == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		final T result = (T) jdbcTemplate.queryForObject("SELECT VALUE FROM BATCH_STAGING WHERE ID=?",
				new RowMapper<Object>() {
			@Override
			public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
				final byte[] blob = rs.getBytes(1);
				return SerializationUtils.deserialize(blob);
			}
		}, id);

		return new ProcessIndicatorItemWrapper<>(id, result);
	}

	@Nullable
	@Override
	public ExitStatus afterStep(final StepExecution stepExecution) {
		return null;
	}

	@Override
	public void beforeStep(final StepExecution stepExecution) {
		this.stepExecution = stepExecution;
		synchronized (lock) {
			if (keys == null) {
				keys = retrieveKeys().iterator();
				logger.info("Keys obtained for staging.");
				initialized = true;
			}
		}
	}

}