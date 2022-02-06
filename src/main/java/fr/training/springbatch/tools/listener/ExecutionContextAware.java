package fr.training.springbatch.tools.listener;

import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;

/**
 * Interface with default <b>getExecutionContext()</b> method used to share
 * datas between step of a same job.
 * <p>
 * ex :
 * <ul>
 * <li>store "foo" data from step1 with <code>
 * getExecutionContext(chunkContext).put("foo", "bar");
 * </code>
 *
 * <li>retrieve "foo" data in the step2 with
 * <code>getExecutionContext(chunkContext).get("foo");</code>
 * </ul>
 *
 * <p>
 * Could be used directly in {@link tasklet} execute() method or with
 * {@link StepExecutionListener} in chunk oriented Step or any @Jobscope
 * reader/rpocessor writer with @Value("#{jobExecutionContext['foo']}")
 * parameter.
 */
public interface ExecutionContextAware {

	default ExecutionContext getExecutionContext(final ChunkContext chunkContext) {
		return chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
	}

}
