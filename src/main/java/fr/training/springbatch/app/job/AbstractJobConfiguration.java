package fr.training.springbatch.app.job;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;

import fr.training.springbatch.tools.listener.ItemCountListener;
import fr.training.springbatch.tools.listener.JobReportListener;

/**
 * Abstract JobConfiguration class to factorize factories declarations and others beans used in all jobs.
 */
public abstract class AbstractJobConfiguration {

    @Autowired
    protected JobBuilderFactory jobBuilderFactory;

    @Autowired
    protected StepBuilderFactory stepBuilderFactory;

    public AbstractJobConfiguration() {
    }

    /**
     * Display report at the end of the job
     */
    @Bean
    protected JobReportListener reportListener() {
        return new JobReportListener();
    }

    /**
     * Used for logging step progression
     */
    @Bean
    protected ItemCountListener progressListener() {
        final ItemCountListener listener = new ItemCountListener();
        listener.setItemName("Transaction(s)");
        listener.setLoggingInterval(50); // Log process item count every 50
        return listener;
    }

    /**
     * Converter to parse local date
     */
    @Bean
    public ConversionService localDateConverter() {
        final DefaultConversionService dcs = new DefaultConversionService();
        DefaultConversionService.addDefaultConverters(dcs);
        dcs.addConverter(new Converter<String, LocalDate>() { 
            @Override
            public LocalDate convert(final String text) {
                return LocalDate.parse(text, DateTimeFormatter.ISO_DATE);
            }
        });
        return dcs;
    }
}