package fr.training.springbatch.job.controlbreak;

import org.springframework.batch.infrastructure.item.*;
import org.springframework.batch.infrastructure.repeat.RepeatContext;
import org.springframework.batch.infrastructure.repeat.policy.SimpleCompletionPolicy;

public class ItemPeekingCompletionPolicyReader<T> extends SimpleCompletionPolicy implements ItemStreamReader<T> {

    private PeekableItemReader<T> delegate;

    private T currentReadItem = null;

    private BreakKeyStrategy<T> breakKeyStrategy;

    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        currentReadItem = delegate.read();
        return currentReadItem;
    }

    @Override
    public void open(final ExecutionContext executionContext) throws ItemStreamException {
        if (delegate instanceof ItemStream stream) {
            stream.open(executionContext);
        }
    }

    @Override
    public void update(final ExecutionContext executionContext) throws ItemStreamException {
        if (delegate instanceof ItemStream stream) {
            stream.update(executionContext);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (delegate instanceof ItemStream stream) {
            stream.close();
        }

    }

    @Override
    public RepeatContext start(final RepeatContext context) {
        return new ComparisonPolicyTerminationContext(context);
    }

    protected class ComparisonPolicyTerminationContext extends SimpleTerminationContext {

        public ComparisonPolicyTerminationContext(final RepeatContext context) {
            super(context);
        }

        @Override
        public boolean isComplete() {
            T nextReadItem;
            try {
                nextReadItem = delegate.peek();
            } catch (final Exception e) {
                throw new NonTransientResourceException("Unable to peek", e);
            }
            return breakKeyStrategy.isKeyBreak(currentReadItem, nextReadItem);
        }
    }

    public void setDelegate(final PeekableItemReader<T> delegate) {
        this.delegate = delegate;
    }

    public void setBreakKeyStrategy(final BreakKeyStrategy<T> breakKeyStrategy) {
        this.breakKeyStrategy = breakKeyStrategy;
    }

}