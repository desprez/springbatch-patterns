package fr.training.springbatch.tools.chaos.tasklet;

import static org.springframework.util.Assert.notNull;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Tasklet used to mimimic slow PosgreSQL queries.
 *
 * Useful for chaos testing on cloud.
 *
 * @author Desprez
 */
public class SlowPGQueryTasklet implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(SlowPGQueryTasklet.class);

	private static final String SLOW_PG_QUERY_COMMAND = "select pg_sleep(%d);";

	private Duration duration = Duration.ofSeconds(60); // Default 1 minute

	private String waitingMessage = "slowQueryTasklet is waiting for {} second # {}";

	private long maxQueries = 1; // Default 1 query

	private long queryCount = 0;

	private JdbcTemplate jdbcTemplate;

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
		notNull(jdbcTemplate, "jdbcTemplate is required");
		queryCount++;
		logger.info(waitingMessage, duration.getSeconds(), queryCount);
		jdbcTemplate.execute(String.format(SLOW_PG_QUERY_COMMAND, duration.getSeconds()));
		if (queryCount < maxQueries) {
			return RepeatStatus.CONTINUABLE;
		}
		return RepeatStatus.FINISHED;
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

	public void setJdbcTemplate(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
