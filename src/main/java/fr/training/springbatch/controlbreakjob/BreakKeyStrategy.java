package fr.training.springbatch.controlbreakjob;

/**
 * Strategy pattern interface to define control break strategy
 *
 * @param <T> the record type
 */
public interface BreakKeyStrategy<T> {

	/**
	 * Comparing method to determine if 2 items are in the same group.
	 *
	 * @param item1 first item to compare
	 * @param item2 second item to compare
	 *
	 * @return true if items are in the same group.
	 */
	boolean isSameGroup(T item1, T item2);

}
