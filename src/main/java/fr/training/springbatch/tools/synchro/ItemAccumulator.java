package fr.training.springbatch.tools.synchro;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Methods for finding and reading items from an ItemReader and accumulating
 * them according to a key value.
 *
 * @param <T> The class of the items to be processed
 * @param <K> The class of the key value of the items being processed. Used for
 *        positioning the reader to a particular place in the input
 *
 * @author Jeremy Yearron
 * @author Desprez (fix)
 */
public abstract class ItemAccumulator<T, K> implements ItemStream, InitializingBean {

	private ItemReader<T> reader;

	private T lastItem;
	private List<T> lastItemList;

	public ItemAccumulator(final ItemReader<T> reader) {
		super();
		this.reader = reader;
	}

	/**
	 * Extract the key of an item.
	 *
	 * @param item
	 * @return the key value for the item
	 */
	public abstract K getKey(T item);

	/**
	 * Determine whether an item matches the key or is for a key greater than the
	 * specified key.
	 * <p/>
	 * This is used when reading forward to find records for a particular key, if
	 * any exist.
	 *
	 * @param item item to be checked
	 * @param key  key value
	 * @return true if the item matches or is greater than the key, otherwise false.
	 */
	@SuppressWarnings("unchecked")
	public boolean checkPositionKey(final T item, final K key) {
		return getKey(item) == null || ((Comparable<K>) getKey(item)).compareTo(key) < 0;
	}

	/**
	 * Determine whether an item matches the key.
	 *
	 * @param item item to be checked
	 * @param key  key value
	 * @return true if the item matches the key, otherwise false.
	 */
	public boolean checkEqualKey(final T item, final K key) {
		return getKey(item).equals(key);
	}

	/**
	 * Read forward through the Reader looking for the first record with a key that
	 * is equal to or greater than the key passed in.
	 *
	 * @param key Key to search for.
	 * @return First item matching search criteria, or null if no suitable item
	 *         found.
	 * @throws Exception
	 */
	public T findEventRecord(final K key) throws Exception {

		T secondRecord = lastItem;

		while (secondRecord != null && checkPositionKey(secondRecord, key)) {
			secondRecord = reader.read();
		}

		return secondRecord;
	}

	/**
	 * Read the items for a particular key, if any exist.
	 * <p/>
	 * The reader will be positioned on the first record for the next key.
	 *
	 * @param key
	 * @return List of items
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<T> readNextItems(final K key) throws Exception {

		// If haven't read first record yet, then do that now.
		if (lastItem == null) {
			lastItem = reader.read();
		} else if (((Comparable<K>) getKey(lastItem)).compareTo(key) > 0) {
			return new ArrayList<T>(0);
		}

		// does last read list match requested key ?
		if (lastItem != null && lastItemList != null && !lastItemList.isEmpty()
				&& checkEqualKey(lastItemList.get(0), key)) {
			return lastItemList;
		}

		// Look for first item for key
		lastItem = readNextItem(key);

		if (lastItem != null) {

			lastItemList = new ArrayList<T>();

			while (lastItem != null && checkEqualKey(lastItem, key)) {
				lastItemList.add(lastItem);
				// Read next item. On EOF, data will be null.
				lastItem = reader.read();
			}
		} else {
			lastItemList = new ArrayList<T>(0);
		}

		return lastItemList;
	}

	/**
	 * Read the items for a particular key, if any exist.
	 * <p/>
	 * The reader will be positioned on the first record for the next key.
	 *
	 * @param key
	 * @return next item for the key, or null if no such record
	 * @throws Exception
	 */
	public T readNextItem(final K key) throws Exception {

		// If haven't read first record yet, then do that now.
		if (lastItem == null) {
			lastItem = reader.read();
		}

		if (lastItem != null) {
			// Haven't reached EOF for the data,
			// so look for more data for the current key
			if (!checkEqualKey(lastItem, key)) {
				lastItem = findEventRecord(key);
			}

			if (lastItem != null && !checkEqualKey(lastItem, key)) {
				return null;
			}
		}

		return lastItem;
	}

	/**
	 * Read the items for the next key.
	 * <p/>
	 * The reader will be positioned on the first record for the next key.
	 *
	 * @param key
	 * @return List of items
	 * @throws Exception
	 */
	public List<T> readNextItems() throws Exception {

		// If haven't read first record yet, then do that now.
		if (lastItem == null) {
			lastItem = reader.read();
		}

		final K lastKey = lastItem == null ? null : getKey(lastItem);

		lastItemList = new ArrayList<T>();

		// Haven't reached EOF for the data,
		// so look for more data for the current key
		while (lastItem != null && checkEqualKey(lastItem, lastKey)) {
			lastItemList.add(lastItem);
			// Read next record. On EOF, data will be null.
			lastItem = reader.read();
		}

		return lastItemList;
	}

	@Override
	public void open(final ExecutionContext executionContext) throws ItemStreamException {
		if (reader instanceof ItemStream) {
			((ItemStream) reader).open(executionContext);
		}
	}

	@Override
	public void update(final ExecutionContext executionContext) throws ItemStreamException {
		if (reader instanceof ItemStream) {
			((ItemStream) reader).update(executionContext);
		}
	}

	@Override
	public void close() throws ItemStreamException {
		if (reader instanceof ItemStream) {
			((ItemStream) reader).close();
		}
	}

	public void setReader(final ItemReader<T> reader) {
		this.reader = reader;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(reader, "reader must be set");
	}
}