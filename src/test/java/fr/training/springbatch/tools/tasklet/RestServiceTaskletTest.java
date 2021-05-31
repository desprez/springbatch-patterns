package fr.training.springbatch.tools.tasklet;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
public class RestServiceTaskletTest {

	private static final Logger logger = LoggerFactory.getLogger(RestServiceTaskletTest.class);

	@Test
	public void execute_GET_method_with_existing_resource_should_success() throws Exception {
		// Given
		final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();

		final StepContribution contribution = new StepContribution(stepExecution);
		final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

		final RestServiceTasklet tasklet = new RestServiceTasklet();
		tasklet.setHttpMethod("GET");
		tasklet.setUri(new URI("https://jsonplaceholder.typicode.com/posts/1"));
		tasklet.setContentType("application/json");
		tasklet.afterPropertiesSet();

		// When
		final RepeatStatus status = tasklet.execute(contribution, context);

		// Then
		assertThat(status).isEqualTo(RepeatStatus.FINISHED);
	}

	@Test(expected = UnexpectedJobExecutionException.class)
	public void execute_GET_method_with_unknown_resource_should_fails() throws Exception {
		// Given
		final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();

		final StepContribution contribution = new StepContribution(stepExecution);
		final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

		final RestServiceTasklet tasklet = new RestServiceTasklet();
		tasklet.setHttpMethod("GET");
		tasklet.setUri(new URI("https://jsonplaceholder.typicode.com/posts/9999"));
		tasklet.setContentType("application/json");
		tasklet.afterPropertiesSet();

		// When
		final RepeatStatus status = tasklet.execute(contribution, context);

		// Then
		assertThat(status).isEqualTo(RepeatStatus.FINISHED);
	}

	@Test
	public void execute_POST_method_should_success() throws Exception {
		// Given
		final StepExecution stepExecution = MetaDataInstanceFactory.createStepExecution();

		final StepContribution contribution = new StepContribution(stepExecution);
		final ChunkContext context = new ChunkContext(new StepContext(stepExecution));

		final RestServiceTasklet tasklet = new RestServiceTasklet();
		tasklet.setHttpMethod("POST");
		tasklet.setUri(new URI("https://jsonplaceholder.typicode.com/posts"));
		tasklet.setContentType("application/json");

		final Map<String, Object> params = new HashMap<>();
		params.put("title", "foo");
		params.put("body", "bar");
		params.put("userId", 1);
		final String payload = new ObjectMapper().writeValueAsString(params);

		tasklet.setRequestBody(payload);
		tasklet.afterPropertiesSet();

		// When
		final RepeatStatus status = tasklet.execute(contribution, context);

		// Then
		assertThat(status).isEqualTo(RepeatStatus.FINISHED);

		logger.info("responseBody {}", stepExecution.getJobExecution().getExecutionContext().get("responseBody"));
	}

}
