package fr.training.springbatch.tools.incrementer;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * {@link JobParametersIncrementer} implementation incrementing a long parameter. Default incremented parameter is "run.id" ; starting with 1. Quite similar to
 * {@link RunIdIncrementer} but does't copy all other parameters to not break optional parameter concept ({@link RunIdIncrementer} copy all parameter of the
 * previous execution so a parameter given once, will be present on all the next execution of this job).
 */
public class ExtendedRunIdIncrementer implements JobParametersIncrementer {

    public static final String RUN_ID_KEY = "run.id";

    protected static final long DEFAULT_STARTING_VALUE = 1;

    private final String incrementedParameterName;

    private final long firstExecutionValue;

    /**
     * Default constructor incrementing "run.id" parameter starting with 1.
     */
    public ExtendedRunIdIncrementer() {
        this(RUN_ID_KEY);
    }

    /**
     * Constructor for incrementing a custom parameter starting with 1.
     *
     * @param incrementedParameterName
     *            the name of the parameter to increment. Must be not null.
     */
    public ExtendedRunIdIncrementer(final String incrementedParameterName) {
        this(incrementedParameterName, DEFAULT_STARTING_VALUE);
    }

    /**
     * Constructor for incrementing a custom parameter.
     *
     * @param incrementedParameterName
     *            the name of the parameter to increment. Must be not null.
     * @param firstExecutionValue
     *            the value of the parameter for the first execution of the job.
     */
    public ExtendedRunIdIncrementer(final String incrementedParameterName, final long firstExecutionValue) {
        Assert.notNull(incrementedParameterName, "incrementedParameterName must be not null");
        this.incrementedParameterName = incrementedParameterName;
        this.firstExecutionValue = firstExecutionValue;
    }

    @Override
    public JobParameters getNext(@Nullable final JobParameters lastExecParameters) {
        final long nextValue;
        if (lastExecParameters == null || lastExecParameters.getLong(this.incrementedParameterName) == null) {
            nextValue = this.firstExecutionValue;
        } else {
            nextValue = lastExecParameters.getLong(this.incrementedParameterName) + 1;
        }
        return new JobParametersBuilder().addLong(this.incrementedParameterName, nextValue).toJobParameters();
    }

}
