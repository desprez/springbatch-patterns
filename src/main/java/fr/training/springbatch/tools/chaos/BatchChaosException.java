package fr.training.springbatch.tools.chaos;

/**
 *  Exceptions throwed by Chaos batch tools
 */
public class BatchChaosException extends RuntimeException {

	private static final long serialVersionUID = -6353927845872026332L;

	public BatchChaosException(final String message) {
		super(message);
	}

}
