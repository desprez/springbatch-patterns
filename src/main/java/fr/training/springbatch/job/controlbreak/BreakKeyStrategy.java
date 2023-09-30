package fr.training.springbatch.job.controlbreak;

/**
 * Strategy pattern interface to define control break strategy
 *
 * @param <T>
 *            the record type
 */
public interface BreakKeyStrategy<T> {

    /**
     * Comparing method to determine if 2 items aren't in the same group.
     *
     * @param item1
     *            first item to compare
     * @param item2
     *            second item to compare
     *
     * @return true if items aren't in the same group.
     */
    boolean isKeyBreak(T item1, T item2);

}
