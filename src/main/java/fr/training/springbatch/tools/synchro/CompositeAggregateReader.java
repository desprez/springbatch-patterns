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
 * <p>
 * An {@link ItemReader} be able to use 2 StreamReaders (Files or whatever class
 * implements {@link AbstractItemStreamItemReader}) simultaneously and to
 * synchronize them in order to return an aggregate.
 * </p>
 * <b>The 2 Streams must share the same key and must be ordered on this key.</b>
 *
 * <p>
 * <b>not</b> thread-safe because the undernline used
 * {@link SingleItemPeekableItemReader } is not.
 * </p>
 *
 * @param <M> The master Item Type (the aggregate root)
 * @param <S> The slave Item Type
 * @param <K> the type of the master item key
 *
 * @author Desprez
 */
public class CompositeAggregateReader<M, S, K extends Comparable<K>> extends AbstractItemStreamItemReader<M>
implements InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(CompositeAggregateReader.class);

	private AbstractItemStreamItemReader<M> masterItemReader;

	private AbstractItemStreamItemReader<S> slaveItemReader;

	private SingleItemPeekableItemReader<S> peekableItemReader;

	private Function<M, K> masterKeyExtractor;

	private Function<S, K> slaveKeyExtractor;

	private BiConsumer<M, S> masterAggregator;

	@Override
	public M read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		final M item = masterItemReader.read();

		if (item == null) {
			return null;
		}
		final K masterKey = masterKeyExtractor.apply(item);

		while (true) {
			final S possibleRelatedItem = peekableItemReader.peek();
			if (possibleRelatedItem == null) {
				return item;
			}

			final K slaveKey = slaveKeyExtractor.apply(possibleRelatedItem);

			// logic to determine if next line in slave file relates to same Master object
			final int keyComparison = slaveKey.compareTo(masterKey);

			log.trace("MasterKey {}, slaveKey {} match {}", masterKey, slaveKey, keyComparison);

			if (keyComparison == 0) {
				// keys are equals : accumulate the slave item
				masterAggregator.accept(item, peekableItemReader.read());
			} else if (keyComparison > 0) {
				// Slave key is greater than master key : aggregate is complete, return it
				return item;
			} else {
				// Slave key is lower than master key : find Slave item forward
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
		Assert.notNull(masterAggregator, "The 'masterAggregator' may not be null");
		Assert.notNull(slaveItemReader, "The 'slaveItemReader' may not be null");
		Assert.notNull(slaveKeyExtractor, "The 'slaveKeyExtractor' may not be null");
	}

	/**
	 * Establishes the {@link AbstractItemStreamItemReader<M>} reader that will read
	 * master items.
	 *
	 * @param reader {@link AbstractItemStreamItemReader<M>} reader that will read
	 *               master items.
	 */
	public void setMasterItemReader(final AbstractItemStreamItemReader<M> masterItemReader) {
		this.masterItemReader = masterItemReader;
	}

	/**
	 * Establishes the {@link AbstractItemStreamItemReader<S>} reader that will read
	 * slave items.
	 *
	 * @param reader {@link AbstractItemStreamItemReader<S>} reader that will read
	 *               slave items.
	 */
	public void setSlaveItemReader(final AbstractItemStreamItemReader<S> slaveItemReader) {
		this.slaveItemReader = slaveItemReader;
		peekableItemReader = new SingleItemPeekableItemReader<>();
		peekableItemReader.setDelegate(slaveItemReader);
	}

	/**
	 * The {@link Function<M, K>} used to extract the key of the master item.
	 *
	 * @param masterKeyExtractor {@link Function<M, K>} used to extract the key of
	 *                           the master item.
	 */
	public void setMasterKeyExtractor(final Function<M, K> masterKeyExtractor) {
		this.masterKeyExtractor = masterKeyExtractor;
	}

	/**
	 * The {@link Function<S, K>} used to extract the key of the slave item.
	 *
	 * @param slaveKeyExtractor {@link Function<S, K>} used to extract the key of
	 *                          the slave item.
	 */
	public void setSlaveKeyExtractor(final Function<S, K> slaveKeyExtractor) {
		this.slaveKeyExtractor = slaveKeyExtractor;
	}

	/**
	 * The {@link BiConsumer<M, S>} function to add/set the slave Item to the master
	 * Item.
	 * <p>
	 * Typicaly a collection <b>add method</b> if the relation between master &
	 * slave items is one-to-many and a <b>set method</b> if the relation between
	 * master & slave items is one-to-one.
	 * </p>
	 *
	 * @param masterAggregator {@link BiConsumer<M, S>} function to add the slave
	 *                         Item to the master Item.
	 */
	public void setMasterAggregator(final BiConsumer<M, S> masterAggregator) {
		this.masterAggregator = masterAggregator;
	}

}
