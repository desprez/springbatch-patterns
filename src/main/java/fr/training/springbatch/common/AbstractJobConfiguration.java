package fr.training.springbatch.common;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import fr.training.springbatch.tools.listener.ItemCountListener;
import fr.training.springbatch.tools.listener.JobReportListener;

/**
 * Abstract JobConfiguration class to factorize factories declarations and
 * others beans used in all jobs.
 */
public abstract class AbstractJobConfiguration {

	@Autowired
	protected JobBuilderFactory jobBuilderFactory;

	@Autowired
	protected StepBuilderFactory stepBuilderFactory;

	public AbstractJobConfiguration() {
		super();
	}

	/**
	 * Display report at the end of the job
	 */
	@Bean
	public JobReportListener reportListener() {
		return new JobReportListener();
	}

	/**
	 * Used for logging step progression
	 */
	@Bean
	public ItemCountListener progressListener() {
		final ItemCountListener listener = new ItemCountListener();
		listener.setItemName("Transaction(s)");
		listener.setLoggingInterval(50); // Log process item count every 50
		return listener;
	}
}