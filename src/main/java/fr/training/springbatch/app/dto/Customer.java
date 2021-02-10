package fr.training.springbatch.app.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer data and wrapped transactions
 */
public class Customer implements Serializable {

	private static final long serialVersionUID = 1L;

	private String number;
	private String firstName;
	private String lastName;
	private String address;
	private String city;
	private String state;
	private String postCode;

	// computed
	private List<Transaction> transactions = new ArrayList<Transaction>();

	// computed
	private double balance;

	public Customer() {
		// java bean tools expected constructor
	}

	public Customer(final String number, final String firstName, final String lastName, final String address,
			final String city, final String state, final String postCode) {
		super();
		this.number = number;
		this.firstName = firstName;
		this.lastName = lastName;
		this.address = address;
		this.city = city;
		this.state = state;
		this.postCode = postCode;
	}

	/**
	 * Copy constructor
	 */
	public Customer(final Customer other) {
		number = other.number;
		firstName = other.firstName;
		lastName = other.lastName;
		address = other.address;
		city = other.city;
		state = other.state;
		postCode = other.postCode;
	}

	public Customer(final Customer customer, final List<Transaction> transactions) {
		this(customer);
		this.transactions = transactions;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(final String number) {
		this.number = number;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(final String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(final String lastName) {
		this.lastName = lastName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(final String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(final String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(final String state) {
		this.state = state;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(final String postCode) {
		this.postCode = postCode;
	}

	public void setTransactions(final List<Transaction> transactions) {
		this.transactions = transactions;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void addTransaction(final Transaction transaction) {
		transactions.add(transaction);
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
		builder.append("Customer [number=").append(number) //
		.append(", firstName=").append(firstName) //
		.append(", lastName=").append(lastName) //
		.append(", address=").append(address) //
		.append(", city=").append(city) //
		.append(", state=").append(state) //
		.append(", postCode=").append(postCode) //
		.append(", transactions=").append(transactions) //
		.append(", balance=").append(balance) //
		.append("]");
		return builder.toString();
	}

}