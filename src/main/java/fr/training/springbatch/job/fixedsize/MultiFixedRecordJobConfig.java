package fr.training.springbatch.job.fixedsize;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileFooterCallback;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.mapping.PatternMatchingCompositeLineMapper;
import org.springframework.batch.item.file.transform.FixedLengthTokenizer;
import org.springframework.batch.item.file.transform.LineTokenizer;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.job.fixedsize.model.AbstractLine;
import fr.training.springbatch.job.fixedsize.model.Detail;
import fr.training.springbatch.job.fixedsize.model.Footer;
import fr.training.springbatch.job.fixedsize.model.Header;

/**
 * This job use a {@link PatternMatchingCompositeLineMapper} to map line with a record Type (ie: 00 for header, 01 for details and 99 for footer).
 *
 * It read a fixed lenght file and produce the same file for demonstration purpose.
 */
@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = MultiFixedRecordJobConfig.FIXED_JOB)
public class MultiFixedRecordJobConfig extends AbstractJobConfiguration {

    protected static final String FIXED_JOB = "fixed-job";

    private static final Logger logger = LoggerFactory.getLogger(MultiFixedRecordJobConfig.class);

    private static final String FOOTER_RECORD_TYPE = "99*";
    private static final String DETAIL_RECORD_TYPE = "01*";
    private static final String HEADER_RECORD_TYPE = "00*";

    private static final String INPUTFILE_PARAMETER_NAME = "inputfile";
    private static final String OUTPUTFILE_PARAMETER_NAME = "outputfile";
    private static final String RECEIVER_CODE_PARAMETER = "receivercode";
    private static final String CREATED_DATE_PARAMETER = "created-date";

    @Value("${application.fixedjob.chunksize:10}")
    private int chunkSize;

    @Bean
    Job fixedJob(final Step validationStep, final Step processStep, final JobRepository jobRepository) {

        return new JobBuilder(FIXED_JOB, jobRepository) //
                .validator(new DefaultJobParametersValidator(
                        new String[] { INPUTFILE_PARAMETER_NAME, OUTPUTFILE_PARAMETER_NAME, RECEIVER_CODE_PARAMETER, CREATED_DATE_PARAMETER }, new String[] {})) //
                .incrementer(new RunIdIncrementer()) //
                .start(validationStep) //
                .next(processStep) //
                .listener(reportListener()) //
                .build();
    }

    @Bean
    Step validationStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final FlatFileItemReader<AbstractLine> itemReader) throws Exception {
        return new StepBuilder("validation-step", jobRepository) //
                .<AbstractLine, AbstractLine> chunk(chunkSize, transactionManager) //
                .reader(itemReader) //
                .processor(validationProcessor()) //
                .writer(items -> {
                    // do nothing
                }) //
                .listener(validationProcessor()) //
                .listener(reportListener()) //
                .build();
    }

    @Bean
    Step processStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager, final FlatFileItemReader<AbstractLine> itemReader,
            final ItemWriter<Detail> fixedItemWriter) throws Exception {

        return new StepBuilder("process-step", jobRepository) //
                .<AbstractLine, Detail> chunk(chunkSize, transactionManager) //
                .reader(itemReader) //
                // return only detail items
                .processor(item -> item instanceof Detail ? (Detail) item : null) //
                .writer(fixedItemWriter) //
                .listener(reportListener()) //
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemReader<AbstractLine> itemReader(@Value("#{jobParameters['inputfile']}") final String inputFile) throws Exception {

        final FlatFileItemReader<AbstractLine> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputFile));
        reader.setLineMapper(abstractLineMapper());
        return reader;
    }

    /**
     * Define a {@link LineMapper} that route to the tokenizers and mappers according to a string at represent a record type.
     *
     * @return a PatternMatchingCompositeLineMapper instance
     */
    @Bean
    PatternMatchingCompositeLineMapper<AbstractLine> abstractLineMapper() {
        final PatternMatchingCompositeLineMapper<AbstractLine> lineMapper = new PatternMatchingCompositeLineMapper<>();

        final Map<String, LineTokenizer> tokenizers = new HashMap<>(3);
        tokenizers.put(HEADER_RECORD_TYPE, headerTokenizer());
        tokenizers.put(DETAIL_RECORD_TYPE, detailTokenizer());
        tokenizers.put(FOOTER_RECORD_TYPE, footerTokenizer());
        lineMapper.setTokenizers(tokenizers);

        final Map<String, FieldSetMapper<AbstractLine>> mappers = new HashMap<>(3);
        mappers.put(HEADER_RECORD_TYPE, simpleFieldSetMapper(Header.class));
        mappers.put(DETAIL_RECORD_TYPE, simpleFieldSetMapper(Detail.class));
        mappers.put(FOOTER_RECORD_TYPE, simpleFieldSetMapper(Footer.class));
        lineMapper.setFieldSetMappers(mappers);

        return lineMapper;
    }

    /**
     * Tokenizer for the <b>header</b> record type
     *
     * @return a LineTokenizer instance
     */
    private LineTokenizer headerTokenizer() {
        final FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setNames("recordType", "createdDate", "transmitterCode", "receiverCode", "sequenceNumber");
        tokenizer.setColumns(new Range(1, 2), // recordType
                new Range(3, 12), // createdDate
                new Range(13, 22), // transmitterCode
                new Range(23, 32), // receiverCode
                new Range(33, 37) // sequenceNumber
        );
        return tokenizer;
    }

    /**
     * Tokenizer for the <b>detail</b> record type
     *
     * @return a LineTokenizer instance
     */
    private LineTokenizer detailTokenizer() {
        final FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();

        tokenizer.setNames("recordType", "boaId", "contractType", "headquarter", "account", "key", "iban", "currency", "fluxId", "docId", "productionDate",
                "docType", "sendState", "sendDate", "groupDate", "groupCode", "anomalyCode", "anomalyLabel", "anomalyDate", "anomalyCount", "recyclingDate1",
                "recyclingDate2", "recyclingDate3", "recyclingDate4", "recyclingDate5", "bicCode");
        tokenizer.setColumns(new Range(1, 2), // recordType
                new Range(3, 19), // boaId
                new Range(20, 22), // contractType
                new Range(23, 27), // headquarter
                new Range(28, 33), // account
                new Range(34, 35), // key
                new Range(36, 62), // iban
                new Range(63, 65), // currency
                new Range(66, 95), // fluxId
                new Range(96, 112), // docId
                new Range(113, 122), // productionDate
                new Range(123, 127), // docType
                new Range(128, 130), // sendState
                new Range(131, 140), // sendDate
                new Range(141, 150), // groupDate
                new Range(151, 153), // groupCode
                new Range(154, 159), // anomalyCode
                new Range(160, 239), // anomalyLabel
                new Range(240, 248), // anomalyDate
                new Range(249, 251), // anomalyCount
                new Range(252, 261), // recyclingDate1
                new Range(262, 271), // recyclingDate2
                new Range(272, 281), // recyclingDate3
                new Range(282, 291), // recyclingDate4
                new Range(292, 301), // recyclingDate5
                new Range(302, 312) // bicCode
        );
        return tokenizer;
    }

    /**
     * Tokenizer for the <b>footer</b> record type
     *
     * @return a LineTokenizer instance
     */
    private LineTokenizer footerTokenizer() {
        final FixedLengthTokenizer tokenizer = new FixedLengthTokenizer();
        tokenizer.setNames("recordType", "detailRecordCount");
        tokenizer.setColumns(new Range(1, 2), // recordType
                new Range(3, 12) // detailRecordCount
        );
        return tokenizer;
    }

    /**
     * Return a {@link BeanWrapperFieldSetMapper} instance according to the given type.
     *
     * @param type
     *            a AbstractLine inherited class
     *
     * @return a {@link BeanWrapperFieldSetMapper} instance
     */
    private FieldSetMapper<AbstractLine> simpleFieldSetMapper(final Class<? extends AbstractLine> type) {
        final BeanWrapperFieldSetMapper<AbstractLine> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(type);
        mapper.setConversionService(localDateConverter());
        return mapper;
    }

    /**
     * Both processor and listener bean that count records and check if the file structure is well formed.
     *
     * @return an IntegrityFileValidator instance.
     */
    @Bean
    ItemProcessor<AbstractLine, AbstractLine> validationProcessor() {
        return new IntegrityFileValidator();
    }

    @Bean
    ItemProcessor<AbstractLine, Detail> detailProcessor() {
        return item -> {
            if (item instanceof Header) {
                // final Header header = (Header) item;
            }
            if (item instanceof Detail) {
                return (Detail) item;
            }
            return null;
        };
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileItemWriter<Detail> fixedItemWriter(@Value("#{jobParameters['outputfile']}") final String fileName, final FlatFileFooterCallback footerProvider,
            final FlatFileHeaderCallback headerProvider) throws Exception {

        return new FlatFileItemWriterBuilder<Detail>() //
                .name("fixedItemWriter") //
                .resource(new FileSystemResource(fileName)) //
                .headerCallback(headerProvider) //
                .footerCallback(footerProvider) //
                .formatted() //
                // –> "-" is a flag indicating the output of each field should be left justified
                // –> 2, 17, 3 ... is width of the field
                // –> s means the result of arg.toString() method
                .format("%-2s%-17s%3s%5s%6s%2s%27s%3s%-30s%17s%10s%-5s%3s%10s%10s%3s%6s%80s%9s%3s%10s%10s%10s%10s%10s%11s") //
                .names("recordType", "boaId", "contractType", "headquarter", "account", "key", "iban", "currency", "fluxId", "docId", "productionDate",
                        "docType", "sendState", "sendDate", "groupDate", "groupCode", "anomalyCode", "anomalyLabel", "anomalyDate", "anomalyCount",
                        "recyclingDate1", "recyclingDate2", "recyclingDate3", "recyclingDate4", "recyclingDate5", "bicCode") //
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileHeaderCallback headerProvider(@Value("#{jobParameters['created-date']}") final Date createdDate,
            @Value("${application.batch.transmitterCode}") final String transmitterCode, @Value("#{jobParameters['receivercode']}") final String receiverCode) {
        return writer -> {
            final Header header = new Header(createdDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(), transmitterCode, receiverCode, "00001");

            writer.write(String.format("%2s%10s%-10s%-10s%5s", //
                    header.getRecordType(), header.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), header.getTransmitterCode(), //
                    header.getReceiverCode(), //
                    header.getSequenceNumber()));
        };
    }

    /**
     * @param writeCount
     *            is injected by spring-batch. It allow to fill the write count in the footer.
     */
    @StepScope // Mandatory for using jobParameters
    @Bean
    FlatFileFooterCallback footerProvider(@Value("#{stepExecutionContext['fixedItemWriter.written']}") final Integer writeCount) {

        return writer -> {
            final Footer footer = new Footer(writeCount);
            writer.write(String.format("%2s%010d", footer.getRecordType(), footer.getDetailRecordCount()));
        };
    }

    /**
     *
     */
    static class IntegrityFileValidator extends StepExecutionListenerSupport implements ItemProcessor<AbstractLine, AbstractLine> {

        private boolean headerFound;
        private boolean footerFound;
        private Integer expectedCount;
        private Integer detailCount = 0;

        @Override
        public AbstractLine process(final AbstractLine item) throws Exception {
            if (item instanceof Header) {
                final Header header = (Header) item;
                logger.debug("header:{}", header);
                headerFound = true;
            }
            if (item instanceof Detail) {
                final Detail detail = (Detail) item;
                logger.debug("detail:{}", detail);
                detailCount++;
            }
            if (item instanceof Footer) {
                final Footer footer = (Footer) item;
                footerFound = true;
                expectedCount = footer.getDetailRecordCount();
            }
            return item;
        }

        @Override
        public ExitStatus afterStep(final StepExecution stepExecution) {
            logger.debug("headerFound:{}, footerFound:{}, expectedCount:{} detailCount:{}", headerFound, footerFound, expectedCount, detailCount);

            notNull(headerFound, "A header is required");

            notNull(footerFound, "A footer is required");

            isTrue(detailCount.equals(expectedCount), String.format("%d record count expected but was %d", expectedCount.intValue(), detailCount.intValue()));
            return stepExecution.getExitStatus();
        }
    }

}
