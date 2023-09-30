package fr.training.springbatch.tools.incrementer;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.jdbc.support.incrementer.DataFieldMaxValueIncrementer;

/**
 * In case of new {@code JobInstance}, this implementation provides an extra job parameter by using a database sequence. The main advantage of this
 * implementation over the {@code RunIdIncrementer} is that in concurrent situation a database sequence call is <b>thread-safe</b> (ensure uniqueness of the
 * returned ids).
 *
 * @see org.springframework.batch.core.launch.support.RunIdIncrementer
 * @author Almex
 */
public class JobParametersSequenceIncrementer implements JobParametersIncrementer {

    /**
     * For legacy support we use the same name as the default one provided by {@link org.springframework.batch.core.launch.support.RunIdIncrementer}
     */
    public static final String INCREMENTER_PARAMETER_NAME = "run.id";

    private DataFieldMaxValueIncrementer sequence;

    @Override
    public JobParameters getNext(final JobParameters jobParameters) {
        final JobParametersBuilder builder = new JobParametersBuilder(jobParameters);

        builder.addLong(INCREMENTER_PARAMETER_NAME, sequence.nextLongValue());

        return builder.toJobParameters();
    }

    public void setSequence(final DataFieldMaxValueIncrementer sequence) {
        this.sequence = sequence;
    }

}