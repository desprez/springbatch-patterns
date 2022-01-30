package fr.training.springbatch.tools.synchro;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;
import org.springframework.batch.item.support.SingleItemPeekableItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 *
 *
 *
 * @param <M> The master Item Type
 * @param <S> The slave Item Type
 * @param <T> the type of the master item key
 *
 * @author Desprez
 */
public class CompositeAggregateReader<M, S, T> extends AbstractItemStreamItemReader<M>
implements InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(CompositeAggregateReader.class);

	private AbstractItemStreamItemReader<M> masterItemReader;

	private AbstractItemStreamItemReader<S> slaveItemReader;

	private SingleItemPeekableItemReader<S> peekableItemReader;

	private Function<M, T> masterKeyExtractor;

	private Function<S, T> slaveKeyExtractor;

	private BiConsumer<M, S> masterAccumulator;

	@Override
	public M read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		final M item = masterItemReader.read();

		if (item == null) {
			return null;
		}
		final T masterKey = masterKeyExtractor.apply(item);

		while (true) {
			final S possibleRelatedObject = peekableItemReader.peek();
			if (possibleRelatedObject == null) {
				return item;
			}

			final T slaveKey = slaveKeyExtractor.apply(possibleRelatedObject);

			// logic to determine if next line in slave file relates to same Master object
			@SuppressWarnings("unchecked")
			final int match = ((Comparable<T>) slaveKey).compareTo(masterKey);

			log.info("MasterKey {}, slaveKey {} match {}", masterKey, slaveKey, match);

			if (match == 0) {
				// keys are equals : accumulate slave item
				masterAccumulator.accept(item, peekableItemReader.read());
			} else if (match > 0) {
				// Slave key is greater than master key : go back to the master reader
				return item;
			} else {
				// Slave key is lower than master key : find Slave forward
				peekableItemReader.read();
			}
		}
	}

	@Override
	public void close() {
		masterItemReader.close();
		peekableItemReader.close();
	}

	@Override
	public void open(final ExecutionContext executionContext) {
		masterItemReader.open(executionContext);
		peekableItemReader.open(executionContext);
	}

	@Override
	public void update(final ExecutionContext executionContext) {
		masterItemReader.update(executionContext);
		peekableItemReader.update(executionContext);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(masterItemReader, "The 'masterItemReader' may not be null");
		Assert.notNull(masterKeyExtractor, "The 'masterKeyExtractor' may not be null");
		Assert.notNull(masterAccumulator, "The 'masterAccumulator' may not be null");
		Assert.notNull(slaveItemReader, "The 'slaveItemReader' may not be null");
		Assert.notNull(slaveKeyExtractor, "The 'slaveKeyExtractor' may not be null");
	}

	/**
	 * Establishes the {@link AbstractItemStreamItemReader<I>} reader that will read
	 * base items.
	 *
	 * @param reader {@link AbstractItemStreamItemReader<I>} reader that will read
	 *               base items.
	 */
	public void setMasterItemReader(final AbstractItemStreamItemReader<M> masterItemReader) {
		this.masterItemReader = masterItemReader;
	}

	/**
	 *
	 * @param slaveItemReader
	 */
	public void setSlaveItemReader(final AbstractItemStreamItemReader<S> slaveItemReader) {
		this.slaveItemReader = slaveItemReader;
		peekableItemReader = new SingleItemPeekableItemReader<>();
		peekableItemReader.setDelegate(slaveItemReader);
	}

	/**
	 *
	 * @param masterKeyExtractor
	 */
	public void setMasterKeyExtractor(final Function<M, T> masterKeyExtractor) {
		this.masterKeyExtractor = masterKeyExtractor;
	}

	/**
	 *
	 * @param slaveKeyExtractor
	 */
	public void setSlaveKeyExtractor(final Function<S, T> slaveKeyExtractor) {
		this.slaveKeyExtractor = slaveKeyExtractor;
	}

	/**
	 * BiConsumer function to add the slave Item to the master Item.
	 *
	 * @param masterAccumulator
	 */
	public void setMasterAccumulator(final BiConsumer<M, S> masterAccumulator) {
		this.masterAccumulator = masterAccumulator;
	}

}
