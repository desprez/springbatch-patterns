package fr.training.springbatch.job.flows.decider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

/**
 * This SpringBatch job configuration ilustrate the JobDecider usage :
 *
 * Try with emptyItemReader the file will not produced and the job ends.
 *
 * Try with filledItemReader the file will produced and the sendAndArchiveFlow wil be executed.
 *
 */
@Configuration
@EnableBatchProcessing
public class FileExistDeciderJobConfig {

    private static final Logger logger = LoggerFactory.getLogger(FileExistDeciderJobConfig.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    Step producerStep(final ItemWriter<String> itemWriter) {
        return stepBuilderFactory.get("producer-Step").<String, String> chunk(3) //
                .reader(emptyItemReader()) //
                .writer(itemWriter) //
                .build();
    }

    @Bean
    ListItemReader<String> emptyItemReader() {
        return new ListItemReader<>(new ArrayList<String>());
    }

    @Bean
    ListItemReader<String> filledItemReader() {
        return new ListItemReader<>(Arrays.asList("Line 1", "Line 2", "Line 3", "Line 4", "Line 5", "Line 6"));
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemWriter<String> itemWriter(@Value("#{jobParameters['output-file']}") final String fileName) {
        return new FlatFileItemWriterBuilder<String>() //
                .name("lineWriter") //
                .resource(new FileSystemResource(fileName)) //
                .lineAggregator(new PassThroughLineAggregator<>()) //
                .shouldDeleteIfEmpty(true) //
                .build();
    }

    @Bean
    JobExecutionDecider fileExistDecider() {
        return (jobExecution, stepExecution) -> {
            final String filename = jobExecution.getJobParameters().getString("output-file");
            if (new File(filename).exists()) {
                return new FlowExecutionStatus("CONTINUE");
            }
            return FlowExecutionStatus.COMPLETED;
        };
    }

    @Bean
    Flow sendAndArchiveFlow() {
        // @formatter:off
        return new FlowBuilder<Flow>("send-archive-flow").start(stepBuilderFactory.get("send-step").tasklet(sendTasklet()).build())
                .next(stepBuilderFactory.get("archive-step").tasklet(archiveTasklet()).build()).build();
        // @formatter:on
    }

    @Bean
    Tasklet sendTasklet() {
        return (contribution, chunkContext) -> {
            logger.debug("launch sendTasklet");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    Tasklet archiveTasklet() {
        return (contribution, chunkContext) -> {
            logger.debug("launch archiveTasklet");
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    Flow mainFlow(final Step producerStep) {
        // @formatter:off
        return new FlowBuilder<Flow>("main-flow").start(producerStep).on("*").to(fileExistDecider()).from(fileExistDecider()).on("CONTINUE")
                .to(sendAndArchiveFlow()).from(fileExistDecider()).on("COMPLETED").end().build();
        // @formatter:on
    }

    @Bean
    Job job(final Flow mainFlow) {
        // @formatter:off
        return jobBuilderFactory.get("exampleJob").start(mainFlow).end().build();
        // @formatter:on
    }

    public static void main(final String[] args) throws Exception {
        final GenericApplicationContext context = new AnnotationConfigApplicationContext(FileExistDeciderJobConfig.class);
        final JobLauncher jobLauncher = context.getBean(JobLauncher.class);
        final Job job = context.getBean(Job.class);

        final JobParameters jobParameters = new JobParametersBuilder().addString("output-file", "target/output/test.txt").toJobParameters();

        jobLauncher.run(job, jobParameters);

        context.close();
    }

}
