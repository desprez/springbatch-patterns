package fr.training.springbatch.tools.synchro;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.ByteArrayResource;

/**
 * Tests for {@link ItemAccumulator}.
 *
 * @author Desprez
 */
class ItemAccumulatorTest {

	private static final String FOOS = "1;foo1\n" + "2;foo2\n" + "3;foo3\n" + "4;foo4\n";

	final Bar bar1 = new Bar("bar1", 1);
	final Bar bar2 = new Bar("bar2", 2);
	final Bar bar3 = new Bar("bar3", 2);
	final Bar bar4 = new Bar("bar4", 4);
	final Bar bar5 = new Bar("bar5", 4);

	private static final String BARS = "bar1;1\n" + "bar2;2\n" + "bar3;2\n" + "bar4;4\n" + "bar5;4\n";

	private FooBarReader fooBarReader;

	protected FlatFileItemReader<Foo> getFooItemReader() throws Exception {
		return new FlatFileItemReaderBuilder<Foo>() //
				.name("FooItemReader").resource(new ByteArrayResource(FOOS.getBytes())) //
				.delimited() //
				.delimiter(";") //
				.names("id", "name") //
				.targetType(Foo.class) //
				.saveState(true).build();
	}

	protected FlatFileItemReader<Bar> getBarItemReader() throws Exception {
		return new FlatFileItemReaderBuilder<Bar>() //
				.name("BarItemReader").resource(new ByteArrayResource(BARS.getBytes())) //
				.delimited() //
				.delimiter(";") //
				.names("name", "fooId") //
				.targetType(Bar.class) //
				.saveState(true).build();
	}

	@BeforeEach
	void setUp() throws Exception {
		fooBarReader = new FooBarReader();
		fooBarReader.setMasterAccumulator(new FooAccumulator(getFooItemReader()));
		fooBarReader.setDetailAccumulator(new BarAccumulator(getBarItemReader()));

		fooBarReader.open(new ExecutionContext());
	}

	@Test
	void shouldFetchItems() throws Exception {
		Foo item = fooBarReader.read();
		assertThat(item).isNotNull();
		assertReadedItem(item, "foo1", bar1);

		item = fooBarReader.read();
		assertThat(item).isNotNull();
		assertReadedItem(item, "foo2", bar2, bar3);

		item = fooBarReader.read();
		assertThat(item).isNotNull();
		assertReadedItem(item, "foo3");

		item = fooBarReader.read();
		assertThat(item).isNotNull();
		assertReadedItem(item, "foo4", bar4, bar5);

		item = fooBarReader.read();
		assertThat(item).isNull();
	}

	private void assertReadedItem(final Foo item, final String expectedName, final Bar... bars) {
		assertThat(item.getName()).isEqualTo(expectedName);
		assertThat(item.getBars()).containsExactly(bars);
	}

}
