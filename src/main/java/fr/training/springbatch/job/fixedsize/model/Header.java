package fr.training.springbatch.job.fixedsize.model;

import java.time.LocalDate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Header extends AbstractLine {

	private LocalDate createdDate;
	private String transmitterCode;
	private String receiverCode;
	private String sequenceNumber;

	public Header() {
		super("00");
	}

	public Header(final LocalDate createdDate, final String transmitterCode, final String receiverCode, final String sequenceNumber) {
		super("00");
		this.createdDate = createdDate;
		this.transmitterCode = transmitterCode;
		this.receiverCode = receiverCode;
		this.sequenceNumber = sequenceNumber;
	}

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(final LocalDate createdDate) {
		this.createdDate = createdDate;
	}

	public String getTransmitterCode() {
		return transmitterCode;
	}

	public void setTransmitterCode(final String transmitterCode) {
		this.transmitterCode = transmitterCode;
	}

	public String getReceiverCode() {
		return receiverCode;
	}

	public void setReceiverCode(final String receiverCode) {
		this.receiverCode = receiverCode;
	}

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(final String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE) //
				.append("createdDate", getCreatedDate()) //
				.append("transmitterCode", getTransmitterCode()) //
				.append("receiverCode", getReceiverCode()) //
				.append("sequenceNumber", getSequenceNumber()) //
				.toString();
	}

}
