package fr.training.springbatch.app.dto;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Stock {

	private Long number;

	private String label;

	public Stock() {

	}

	public Stock(final Long number, final String label) {
		super();
		this.number = number;
		this.label = label;
	}

	public Long getNumber() {
		return number;
	}

	public void setNumber(final Long number) {
		this.number = number;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Stock)) {
			return false;
		}
		final Stock other = (Stock) obj;
		return new EqualsBuilder().append(getNumber(), other.getNumber()).append(getLabel(), other.getLabel())
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(getNumber()).append(getLabel()).toHashCode();
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("Stock [number=").append(number).append(", label=").append(label).append("]");
		return builder.toString();
	}

}
