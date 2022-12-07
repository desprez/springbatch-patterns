package fr.training.springbatch.job.multilinesrecord.dto;

import java.io.Serializable;

public abstract class Record implements Serializable {

    private static final long serialVersionUID = 4914387950852223658L;

    private String recordType;

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(final String recordType) {
        this.recordType = recordType;
    }

}
