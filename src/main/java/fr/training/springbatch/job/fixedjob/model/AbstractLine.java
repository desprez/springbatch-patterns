package fr.training.springbatch.job.fixedjob.model;

public abstract class AbstractLine {

	private String recordType;

	public AbstractLine() {

	}

	public AbstractLine(final String recordType) {
		this.recordType = recordType;
	}

	public String getRecordType() {
		return recordType;
	}

	public void setRecordType(final String recordType) {
		this.recordType = recordType;
	}

}
