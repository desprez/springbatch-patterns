package fr.training.springbatch.tools.writer;

import static org.springframework.util.Assert.notNull;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

/**
 * Generic {@link ItemWriter} that write conditionaly by delegating to a real ItemWriter if a dryrun flag is set.
 *
 * <pre class="code">
 * &#064;Bean
 * public DryRunItemWriter<Person> writer(final JdbcBatchItemWriter<Person> delegate, &#064;Value("#{jobParameters['dryrun']?:true}") final boolean dryrun) {
 *
 *     final DryRunItemWriter<Person> dryRunItemWriter = new DryRunItemWriter<Person>();
 *     dryRunItemWriter.setDelegate(delegate);
 *     dryRunItemWriter.setDryrun(dryrun);
 *
 *     return dryRunItemWriter;
 * }
 * </pre>
 */
public class DryRunItemWriter<T> implements ItemWriter<T> {

    private boolean dryrun;

    private ItemWriter<T> delegate;

    @Override
    public void write(final List<? extends T> items) throws Exception {
        notNull(delegate, "delegate is required");
        if (!dryrun) {
            delegate.write(items);
        }
    }

    public void setDryrun(final boolean dryrun) {
        this.dryrun = dryrun;
    }

    public void setDelegate(final ItemWriter<T> delegate) {
        this.delegate = delegate;
    }
}
