package fr.training.springbatch.job.controlbreak;

import org.springframework.batch.core.annotation.AfterRead;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.PeekableItemReader;
import org.springframework.batch.repeat.RepeatContext;
import org.springframework.batch.repeat.context.RepeatContextSupport;
import org.springframework.batch.repeat.policy.CompletionPolicySupport;

/**
 * using a custom CompletionPolicy and a PeekableItemReader. The idea behind the code is to peek next item, perform next element read and check from value
 * change. When a value change happens return true from CompletionPolicy.isComplete().
 *
 *
 * Important: this policy must be registered as step listener!
 */
public class BreakKeyCompletionPolicy<T> extends CompletionPolicySupport {

    private BreakKeyCompletionContext cc;

    private PeekableItemReader<T> reader;

    // Strategy used to check for value break
    private BreakKeyStrategy<T> breakKeyStrategy;

    @Override
    public boolean isComplete(final RepeatContext context) {
        return cc.isComplete();
    }

    @Override
    public RepeatContext start(final RepeatContext context) {
        context.setAttribute("current", null);
        cc = new BreakKeyCompletionContext(context);
        return cc;
    }

    /**
     * Context contains current element ("current" property" and manage next element. Null next element is treated as a key break
     */
    protected class BreakKeyCompletionContext extends RepeatContextSupport {
        public BreakKeyCompletionContext(final RepeatContext context) {
            super(context);
        }

        @SuppressWarnings("unchecked")
        public boolean isComplete() {
            final Object next;
            try {
                next = reader.peek();
            } catch (final Exception e) {
                throw new NonTransientResourceException("Unable to peek", e);
            }
            if (null == next) {
                return true;
            }
            return breakKeyStrategy.isKeyBreak((T) getAttribute("current"), (T) next);
        }
    }

    @AfterRead
    public void afterRead(final Object item) {
        cc.setAttribute("current", item);
    }

    public void setReader(final PeekableItemReader<T> forseeingReader) {
        reader = forseeingReader;
    }

    public void setBreakKeyStrategy(final BreakKeyStrategy<T> breakKeyStrategy) {
        this.breakKeyStrategy = breakKeyStrategy;
    }

}