package fr.training.springbatch.tools.tasklet;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class RestServiceTasklet implements Tasklet, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RestServiceTasklet.class);

    private URI uri;
    private String requestBody;
    private String contentType;
    private HttpMethod httpMethod;
    private RestTemplate restTemplate;

    public RestServiceTasklet() {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (restTemplate == null) {
            restTemplate = new RestTemplateBuilder()
                    // .errorHandler(new RestErrorHandler())
                    .build();
        }
    }

    @Override
    public RepeatStatus execute(final StepContribution stepcontribution, final ChunkContext chunkContext) throws Exception {

        logger.info("uri {}", uri);

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", contentType);
        headers.add("Accept", "application/json");

        final HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        logger.debug(requestEntity.toString());

        try {
            final ResponseEntity<String> responseEntity = restTemplate.exchange(uri, httpMethod, requestEntity, String.class);

            logger.debug(responseEntity.getBody());

            // set step status based on HTTP status
            final HttpStatus code = responseEntity.getStatusCode();
            logger.debug("HttpStatus {} ", code);

            // if ok, return HttpStatus
            if (code.is2xxSuccessful()) {
                // Put response body to job execution context to share it with others steps
                chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("responseBody", responseEntity.getBody());

                return RepeatStatus.FINISHED;
            }
            // else set error code and exit with failure
            stepcontribution.setExitStatus(ExitStatus.FAILED);
            throw new UnexpectedJobExecutionException("Service responded with an error code: " + code);
        } catch (final HttpClientErrorException e) {
            stepcontribution.setExitStatus(ExitStatus.FAILED);
            throw new UnexpectedJobExecutionException(e.getMessage());
        }
    }

    public void setUri(final URI uri) {
        this.uri = uri;
    }

    public void setRestTemplate(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setRequestBody(final String requestBody) {
        this.requestBody = requestBody;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public void setHttpMethod(final String httpMethod) {
        this.httpMethod = HttpMethod.valueOf(httpMethod);
    }

}