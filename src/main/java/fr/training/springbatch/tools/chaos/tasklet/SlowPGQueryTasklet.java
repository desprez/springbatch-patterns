package fr.training.springbatch.tools.chaos.tasklet;

import static org.springframework.util.Assert.notNull;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Tasklet used to mimimic slow PosgreSQL queries.
 *
 * Useful for chaos testing on cloud.
 *
 * @author Desprez
 */
public class SlowPGQueryTasklet implements StoppableTasklet {

	private static final Logger logger = LoggerFactory.getLogger(SlowPGQueryTasklet.class);

	private static final String SLOW_PG_QUERY_COMMAND = "select pg_sleep(%d);";

	private Duration duration = Duration.ofSeconds(60); // Default 1 minute

	private String waitingMessage = "Sending slow query # {} for {} second ";

	private long maxQueries = 1; // Default 1 query

	private long queryCount = 0;

	private JdbcTemplate jdbcTemplate;

	private Mode mode = Mode.FIXED;

	private boolean stopped = false;

	public enum Mode {
		FIXED, RANDOM, RAMPUP
	}

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
		notNull(jdbcTemplate, "jdbcTemplate is required");
		queryCount++;

		final long seconds = computeQueryDuration();

		logger.info(waitingMessage + mode, queryCount, seconds);

		jdbcTemplate.execute(String.format(SLOW_PG_QUERY_COMMAND, seconds));

		if (queryCount < maxQueries) {
			return RepeatStatus.continueIf(!stopped);
		}
		return RepeatStatus.FINISHED;
	}

	@Override
	public void stop() {
		logger.info("Stop requested");
		stopped = true;
	}

	private long computeQueryDuration() {
		if (Mode.RANDOM.equals(mode)) {
			return randomizeSeconds(duration.getSeconds());

		} else if (Mode.RAMPUP.equals(mode)) {
			return duration.getSeconds() / maxQueries * queryCount;

		} else {
			return duration.getSeconds();
		}
	}

	private long randomizeSeconds(final long max) {
		return 1 + (long) (Math.random() * (max - 1));
	}

	public void setDuration(final Duration seconds) {
		duration = seconds;
	}

	public void setWaitingMessage(final String waitingMessage) {
		this.waitingMessage = waitingMessage;
	}

	public void setMaxQueries(final long maxQueries) {
		this.maxQueries = maxQueries;
	}

	public void setMode(final Mode mode) {
		this.mode = mode;
	}

	public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
