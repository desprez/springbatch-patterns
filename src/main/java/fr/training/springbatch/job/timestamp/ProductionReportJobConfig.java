package fr.training.springbatch.job.timestamp;

import java.sql.Types;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.ArgumentTypePreparedStatementSetter;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterUtils;
import org.springframework.transaction.PlatformTransactionManager;

import fr.training.springbatch.app.job.AbstractJobConfiguration;
import fr.training.springbatch.tools.writer.ReportConsoleItemWriter;

@Configuration
@ConditionalOnProperty(name = "spring.batch.job.names", havingValue = ProductionReportJobConfig.MONITORING_JOB)
public class ProductionReportJobConfig extends AbstractJobConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ProductionReportJobConfig.class);

    protected static final String MONITORING_JOB = "monitoringJob";

    private static final String LAST_EXEC_DATE_PARAM_NAME = "lastdatetime";

    private static final LocalDateTime APPLICATION_FIRST_LAUNCH_DATE = LocalDateTime.of(2023, 06, 01, 0, 0);

    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Value("${application.monitoringStep.chunksize:500}")
    private int chunkSize;

    @Autowired
    public JobExplorer jobExplorer;

    // @Bean
    // public JobParametersValidator jobParametersValidator(
    // final JobParametersValidatorBuilder jobParametersValidatorBuilder) {
    //
    // return jobParametersValidatorBuilder //
    // .parameter(RUN_ID_PARAM).required().identifying()
    // .build();
    // }

    @Bean
    public Job monitoringJob(final JobRepository jobRepository,
            /* final JobParametersValidator jobParametersValidator, */ final Step monitoringStep) {

        return new JobBuilder(MONITORING_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                // .validator(jobParametersValidator)
                .flow(monitoringStep)
                .end()
                .listener(lastCompletedJobExecutionDateProvider())
                .build();
    }

    /**
     * This {@link JobExecutionListener} find last Succeeded JobExecution if exist (else a fixed sooner Date) and put Date in the {@link ExecutionContext} to be
     * retrieved later in the reader.
     */
    private JobExecutionListener lastCompletedJobExecutionDateProvider() {

        return new JobExecutionListener() {
            @Override
            public void beforeJob(final JobExecution jobExecution) {

                final LocalDateTime lastDateToUse = getLastCompletedJobExecutionDate(
                        jobExecution.getJobInstance().getJobName(), jobExplorer)
                                .orElse(APPLICATION_FIRST_LAUNCH_DATE);

                log.info("Using {} to retrieve Canceled Opportunities", lastDateToUse);

                jobExecution.getExecutionContext().put(LAST_EXEC_DATE_PARAM_NAME, lastDateToUse);
            }
        };
    }

    private Optional<LocalDateTime> getLastCompletedJobExecutionDate(final String jobName,
            final JobExplorer jobExplorer) {

        final List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, Integer.MAX_VALUE);

        return jobInstances.stream()
                .map(jobExplorer::getJobExecutions)
                .flatMap(List<JobExecution>::stream)
                .filter(param -> BatchStatus.COMPLETED.equals(param.getStatus()))
                .sorted((final JobExecution execution1, final JobExecution execution2) -> execution2.getStartTime()
                        .compareTo(execution1.getStartTime()))
                .map(JobExecution::getCreateTime)
                .findFirst();
    }

    @Bean
    public Step monitoringStep(final JobRepository jobRepository, final PlatformTransactionManager transactionManager,
            final ItemReader<FlatSubscriptionDto> monitorReader,
            final ItemWriter<FlatSubscriptionDto> monitorWriter) {

        return new StepBuilder("monitoringStep", jobRepository)
                .<FlatSubscriptionDto, FlatSubscriptionDto> chunk(chunkSize, transactionManager)
                .reader(monitorReader)
                .writer(monitorWriter())
                .build();
    }

    /**
     * A {@link ItemReader} that use JDBC to retrieve new abandoned Opportunities.
     *
     * @param lastDateTime
     *            Last Completed JobExecution date used as criteria to retrieve new abandoned Opportunities created after the last JobExecution until now.
     * @return
     */
    @StepScope // Mandatory for using jobExecutionContext
    @Bean
    JdbcCursorItemReader<FlatSubscriptionDto> monitorReader(final DataSource dataSource,
            @Value("#{jobExecutionContext['" + LAST_EXEC_DATE_PARAM_NAME + "']}") final LocalDateTime lastDateTime) {

        final String sql = """
                SELECT *
                FROM subscription
                WHERE ( status = 'STARTED' )
                AND (creation_date BETWEEN :lastRunTimestamp AND current_timestamp)
                ORDER BY distributor_number ASC, number DESC;
                """;

        log.info("Using parameters {}", lastDateTime);
        final Map<String, Object> namedParameters = Map.of("lastRunTimestamp", lastDateTime);
        // "expected format : 2023-10-23 22:03:45.265"

        final String preparedSql = NamedParameterUtils.substituteNamedParameters(sql,
                new MapSqlParameterSource(namedParameters));
        final PreparedStatementSetter preparedStatementSetter = new ArgumentTypePreparedStatementSetter(
                NamedParameterUtils.buildValueArray(sql, namedParameters), new int[] { Types.TIMESTAMP });

        return new JdbcCursorItemReaderBuilder<FlatSubscriptionDto>() //
                .name("monitorReader") //
                .dataSource(dataSource) //
                .rowMapper(abandonedOpportunityRowMapper()) //
                .sql(preparedSql)
                .preparedStatementSetter(preparedStatementSetter)
                .build();
    }

    private RowMapper<FlatSubscriptionDto> abandonedOpportunityRowMapper() {
        return (rs, rowNum) -> {
            final var report = new FlatSubscriptionDto();
            report.setOpportunityId(rs.getString("number"));
            report.setDistributorNumber(rs.getString("distributor_number"));
            report.setCreationDate(rs.getDate("creation_date"));
            report.setTitle(rs.getString("title"));
            report.setName(rs.getString("last_name"));
            report.setFirstName(rs.getString("first_name"));
            report.setEmail(rs.getString("email_address"));
            report.setPhoneNumber(rs.getString("phone_number"));
            report.setOverdraft(df.format(rs.getDouble("loan_overdraft")));
            report.setTaeg(df.format(rs.getDouble("loan_taeg")));
            report.setTeg(df.format(rs.getDouble("loan_teg")));
            report.setTnc(df.format(rs.getDouble("loan_tnc")));
            report.setTerm(rs.getInt("loan_term"));
            report.setMonthlyPaymentWithoutInsurance(df.format(rs.getDouble("loan_month_pay")));
            return report;
        };
    }

    @Bean
    ItemWriter<FlatSubscriptionDto> monitorWriter() {
        final ReportConsoleItemWriter<FlatSubscriptionDto> lineWriter = new ReportConsoleItemWriter<>();
        lineWriter.setHeader(
                "Reference,Civilite;Nom;Prenom;Email;Telephone;Vendeur;Montant Achat;Duree;TEG;TAEG;TNC;Mensualite;creationDate");
        final LineAggregator<FlatSubscriptionDto> lineAggregator = csvLineAggregator();
        lineWriter.setLineAggregator(lineAggregator);
        return lineWriter;
    }

    private LineAggregator<FlatSubscriptionDto> csvLineAggregator() {
        final DelimitedLineAggregator<FlatSubscriptionDto> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(";");
        final BeanWrapperFieldExtractor<FlatSubscriptionDto> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[] { "opportunityId", "title", "Name", "firstName", "email", "phoneNumber",
                "distributorNumber", "overdraft", "taeg", "teg", "tnc", "term", "monthlyPaymentWithoutInsurance", "creationDate" });
        lineAggregator.setFieldExtractor(fieldExtractor);
        return lineAggregator;
    }

}
