package fr.training.springbatch.job.multilinesrecord.dto;

import java.time.LocalDate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CustomerRecord extends Record {

    private static final long serialVersionUID = 1;

    private Long number;
    private String firstName;
    private String lastName;
    private String address;
    private String city;
    private String state;
    private String postCode;
    private LocalDate birthDate;

    public Long getNumber() {
        return number;
    }

    public void setNumber(final Long number) {
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(final LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
                .append("number", getNumber()) //
                .append("firstName", getFirstName()) //
                .append("lastName", getLastName()) //
                .append("address", getAddress()) //
                .append("city", getCity()) //
                .append("state", getState()) //
                .append("postCode", getPostCode()) //
                .append("birthDate", getBirthDate()) //
                .toString();
    }

}
