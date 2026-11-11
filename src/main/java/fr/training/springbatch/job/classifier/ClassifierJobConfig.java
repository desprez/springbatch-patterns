package fr.training.springbatch.job.classifier;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.job.classifier.model.*;
import fr.training.springbatch.job.classifier.processor.AdoptedPetProcessor;
import fr.training.springbatch.job.classifier.processor.NewPetProcessor;
import fr.training.springbatch.job.classifier.processor.UpdatedPetProcessor;
import fr.training.springbatch.job.classifier.writer.CreatePetWriter;
import fr.training.springbatch.job.classifier.writer.DeletePetWriter;
import fr.training.springbatch.job.classifier.writer.UpdatePetWriter;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.RecordFieldSetMapper;
import org.springframework.batch.infrastructure.item.support.ClassifierCompositeItemProcessor;
import org.springframework.batch.infrastructure.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.infrastructure.item.support.builder.ClassifierCompositeItemProcessorBuilder;
import org.springframework.batch.infrastructure.item.support.builder.ClassifierCompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.classify.Classifier;
import org.springframework.classify.SubclassClassifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Map;

/**
 * Batch configuration for the {@code import-pets-job}, which imports a CSV file of pets and
 * applies the resulting create, update or delete actions to the database.
 * <p>
 * The job is composed of a single step ({@code import-pets-step}) that uses the
 * <em>classifier</em> pattern in both directions:
 * <ul>
 *     <li>the processor side routes each {@link PetCsvLine} to a specific {@link ItemProcessor}
 *     depending on its adoption status, producing a {@link PetAction}</li>
 *     <li>the writer side routes each {@link PetAction} to a specific {@link org.springframework.batch.infrastructure.item.ItemWriter}
 *     depending on its concrete type</li>
 * </ul>
 * The job is only activated when {@code spring.batch.job.names} is set to {@code classifier-job}.
 *
 * @author Bonnissent, Desprez
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = ClassifierJobConfig.CLASSIFIER_JOB)
public class ClassifierJobConfig extends AbstractJobConfiguration {

    /**
     * Name of the job, used as the value of {@code spring.batch.job.names} to enable this configuration.
     */
    protected static final String CLASSIFIER_JOB = "classifier-job";

    /**
     * Chunk size read from {@code application.classifier-step.chunksize} (default 10). Currently not used: the step applies a fixed chunk size of 100.
     */
    @Value("${application.classifier-step.chunksize:10}")
    private int chunkSize;

    /**
     * Path of the input CSV file, configured by {@code application.classifier-job.filepath}.
     */
    @Value("${application.classifier-job.filepath}")
    private String filePath;

    /**
     * Reads the pets CSV file, skipping the header line. Each line is mapped to a {@link PetCsvLine}
     * using the columns {@code name}, {@code status} and {@code species}.
     *
     * @return the flat file reader for the pets file
     */
    @Bean
    public FlatFileItemReader<PetCsvLine> csvReader() {
        return new FlatFileItemReaderBuilder<PetCsvLine>()
                .name("pet-item-reader")
                .resource(new FileSystemResource(filePath))
                .linesToSkip(1)
                .delimited()
                .names("name", "status", "species")
                .fieldSetMapper(new RecordFieldSetMapper<>(PetCsvLine.class))
                .build();
    }

    /**
     * Composite processor that delegates each {@link PetCsvLine} to the processor selected by
     * {@link #processorClassifier()}.
     *
     * @param processorClassifier the classifier choosing the processor based on the pet status
     * @return the composite processor producing a {@link PetAction}
     */
    @Bean
    public ClassifierCompositeItemProcessor<PetCsvLine, PetAction> petProcessor(
            Classifier<PetCsvLine, ItemProcessor<?, ? extends PetAction>> processorClassifier
    ) {
        return new ClassifierCompositeItemProcessorBuilder<PetCsvLine, PetAction>()
                .classifier(processorClassifier)
                .build();
    }

    /**
     * Routes a {@link PetCsvLine} to a processor according to its adoption status:
     * <ul>
     *     <li>{@code NEW} – {@link fr.training.springbatch.job.classifier.processor.NewPetProcessor}
     *     (creates a {@link PetToCreate})</li>
     *     <li>{@code ADOPTED} – {@link fr.training.springbatch.job.classifier.processor.AdoptedPetProcessor}
     *     (deletes a {@link PetToDelete})</li>
     *     <li>{@code WAITING}, {@code AVAILABLE} – {@link fr.training.springbatch.job.classifier.processor.UpdatedPetProcessor}
     *     (updates a {@link PetToUpdate})</li>
     * </ul>
     *
     * @return the processor classifier
     */
    @Bean
    public Classifier<PetCsvLine, ItemProcessor<?, ? extends PetAction>> processorClassifier() {
        return new PetProcessorClassifier(new UpdatedPetProcessor(), new AdoptedPetProcessor(), new NewPetProcessor());
    }

    /**
     * Routes each {@link PetAction} to the writer matching its concrete type:
     * {@link PetToDelete} to {@link DeletePetWriter}, {@link PetToCreate} to {@link CreatePetWriter}
     * and {@link PetToUpdate} to {@link UpdatePetWriter}. No default writer is configured.
     *
     * @param petService the service used by the writers to persist changes
     * @return the subclass classifier used by the composite writer
     */
    @Bean
    public SubclassClassifier<PetAction, ItemWriter<? extends PetAction>> writerClassifier(PetService petService) {
        var typeMap = Map.of(
                PetToDelete.class, new DeletePetWriter(petService),
                PetToCreate.class, new CreatePetWriter(petService),
                PetToUpdate.class, new UpdatePetWriter(petService)
        );

        return new SubclassClassifier<>(typeMap, null);
    }

    /**
     * Service encapsulating the database operations (create, update, delete) on pets.
     *
     * @param namedParameterJdbcTemplate the JDBC template used to execute the SQL statements
     * @return the pet service
     */
    @Bean
    public PetService petService(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new PetService(namedParameterJdbcTemplate);
    }

    /**
     * Composite writer that delegates each {@link PetAction} to the writer selected by
     * {@link #writerClassifier(PetService)}.
     *
     * @param writerClassifier the classifier choosing the writer based on the action type
     * @return the composite writer
     */
    @Bean
    //@SuppressWarnings("unchecked")
    public ClassifierCompositeItemWriter<PetAction> petWriter(Classifier<PetAction, ?> writerClassifier) {
        // Each delegate writer handles a PetAction subtype, hence the unchecked cast to the builder's expected type
        return new ClassifierCompositeItemWriterBuilder<PetAction>()
                .classifier((Classifier<PetAction, ItemWriter<? super PetAction>>) writerClassifier)
                .build();
    }

    /**
     * Step reading the CSV file, processing each line through the classifier-based processor and
     * writing the resulting actions through the classifier-based writer, by chunks of 100 items.
     *
     * @param jobRepository      the job repository
     * @param transactionManager the transaction manager used for each chunk
     * @param csvReader          the pets CSV reader
     * @param petProcessor       the composite processor
     * @param petWriter          the composite writer
     * @return the import step
     */
    @Bean
    public Step importPetsStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               FlatFileItemReader<PetCsvLine> csvReader,
                               ClassifierCompositeItemProcessor<PetCsvLine, PetAction> petProcessor,
                               ClassifierCompositeItemWriter<PetAction> petWriter) {

        return new StepBuilder("import-pets-step", jobRepository)
                .<PetCsvLine, PetAction>chunk(10)
                .transactionManager(transactionManager)
                .reader(csvReader)
                .processor(petProcessor)
                .writer(petWriter)
                .build();
    }

    /**
     * Job {@code import-pets-job} made of the {@code import-pets-step}. A {@link RunIdIncrementer}
     * is used so that the job can be launched several times with the same parameters.
     *
     * @param jobRepository  the job repository
     * @param importPetsStep the import step
     * @return the import pets job
     */
    @Bean
    public Job importPetsJob(JobRepository jobRepository,
                             Step importPetsStep) {
        return new JobBuilder("import-pets-job", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(importPetsStep)
                .build();
    }
}
