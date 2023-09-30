package fr.training.springbatch.app.dto;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long customerNumber;
    private String number;
    private LocalDate transactionDate;
    private Double amount;

    private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    public Transaction() {

    }

    public Transaction(final Long customerNumber, final String number, final LocalDate transactionDate, final Double amount) {
        this.customerNumber = customerNumber;
        this.number = number;
        this.transactionDate = transactionDate;
        this.amount = amount;
    }

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

    public String getDateString() {
        return formatter.format(transactionDate);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Transaction [customerNumber=").append(customerNumber) //
                .append(", number=").append(number).append(", transactionDate=").append(transactionDate) //
                .append(", amount=").append(amount).append("]");
        return builder.toString();
    }

}