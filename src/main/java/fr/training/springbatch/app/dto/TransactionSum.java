package fr.training.springbatch.app.dto;

public class TransactionSum {

    private Long customerNumber;

    private double balance;

    public Long getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(final Long customerNumber) {
        this.customerNumber = customerNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(final double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("TransactionSum [customerNumber=").append(customerNumber) //
                .append(", balance=").append(balance).append("]");
        return builder.toString();
    }

}