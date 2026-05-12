package fr.training.springbatch.job.multilinesrecord.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.time.LocalDate;

public class TransactionRecord extends Record {

    @Serial
    private static final long serialVersionUID = 1;

    private Long customerNumber;
    private String number;
    private LocalDate transactionDate;
    private Double amount;

    public Long getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(final Long customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(final LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(final Double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
                .append("customerNumber", getCustomerNumber()) //
                .append("number", getNumber()) //
                .append("transactionDate", getTransactionDate()) //
                .append("amount", getAmount()) //
                .toString();
    }

}
