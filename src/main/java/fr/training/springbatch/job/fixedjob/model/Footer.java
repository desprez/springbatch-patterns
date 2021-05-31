package fr.training.springbatch.job.fixedjob.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Footer extends AbstractLine {

	private Integer detailRecordCount;

	public Footer() {

	}

	public Footer(final String recordType) {
		super("99");
	}

	public Footer(final Integer detailRecordCount) {
		super("99");
		this.detailRecordCount = detailRecordCount;
	}

	public Integer getDetailRecordCount() {
		return detailRecordCount;
	}

	public void setDetailRecordCount(final Integer detailRecordCount) {
		this.detailRecordCount = detailRecordCount;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE) //
				.append("detailRecordCount", getDetailRecordCount()) //
				.toString();
	}

}
