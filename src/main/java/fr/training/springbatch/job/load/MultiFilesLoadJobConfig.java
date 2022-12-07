package fr.training.springbatch.job.load;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.DefaultJobParametersValidator;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import fr.training.springbatch.app.dto.Customer;
import fr.training.springbatch.app.job.AbstractJobConfiguration;

/**
 * This Job load Transaction csv files present in a directory sequentialy insert each read line in a Transaction Table.
 *
 * @author desprez
 */
public class MultiFilesLoadJobConfig extends AbstractJobConfiguration {

    @Value("${application.multi-load-step.chunksize:10}")
    private int chunkSize;

    @Autowired
    private DataSource dataSource;

    @Bean
    public Job multiLoadJob(final Step multiLoadStep) {
        return jobBuilderFactory.get("multi-load-job") //
                .validator(new DefaultJobParametersValidator(new String[] { "input-path" }, new String[] {})) //
                .start(multiLoadStep) //
                .build();
    }

    @Bean
    public Step multiLoadStep(final MultiResourceItemReader<Customer> multiResourceItemReader, final JdbcBatchItemWriter<Customer> writer) {

        return stepBuilderFactory.get("multi-load-step") //
                .<Customer, Customer> chunk(chunkSize) //
                .reader(multiResourceItemReader) //
                .writer(writer) //
                .build();
    }

    @StepScope // Mandatory for using jobParameters
    @Bean
    public MultiResourceItemReader<Customer> multiResourceItemReader(@Value("#{jobParameters['input-path']}") final Resource[] inputResources) {

        final MultiResourceItemReader<Customer> resourceItemReader = new MultiResourceItemReader<Customer>();
        resourceItemReader.setResources(inputResources);
        resourceItemReader.setDelegate(reader());
        return resourceItemReader;
    }

    @Bean
    public FlatFileItemReader<Customer> reader() {
        return new FlatFileItemReaderBuilder<Customer>() //
                .name("itemReader") //
                .delimited() //
                .delimiter(";") //
                .names("number", "firstName", "lastName", "address", "city", "state", "postCode", "birtDate") //
                .fieldSetMapper(new BeanWrapperFieldSetMapper<Customer>() {
                    {
                        setTargetType(Customer.class);
                        setConversionService(localDateConverter());
                    }
                }).build();
    }

    @Bean
    public JdbcBatchItemWriter<Customer> writer() {
        return new JdbcBatchItemWriterBuilder<Customer>() //
                .dataSource(dataSource)
                .sql("INSERT INTO Customer(number, first_name, last_name, address, city, state, post_code, birth_date) "
                        + "VALUES (:number, :firstName, :lastName, :address, :city, :state, :postCode, :birthDate)")
                .beanMapped() //
                .build();
    }

}
