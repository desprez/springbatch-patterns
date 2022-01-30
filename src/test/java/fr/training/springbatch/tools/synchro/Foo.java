package fr.training.springbatch.tools.synchro;

import java.util.ArrayList;
import java.util.List;

public class Foo {

	private int id;
	private String name;
	private List<Bar> bars = new ArrayList<>();

	public Foo() {

	}

	public Foo(final Foo other) {
		id = other.id;
		name = other.name;
	}

	public Foo(final Foo foo, final List<Bar> bars) {
		this(foo);
		this.bars = bars;
	}

	public int getId() {
		return id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public List<Bar> getBars() {
		return bars;
	}

	public void addBar(final Bar bar) {
		bars.add(bar);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + (name == null ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Foo other = (Foo) obj;
		if (id != other.id) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Foo [id=").append(id).append(", name=").append(name).append("]");
		return builder.toString();
	}

}