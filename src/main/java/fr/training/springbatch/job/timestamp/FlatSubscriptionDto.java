package fr.training.springbatch.job.timestamp;

import java.util.Date;

public class FlatSubscriptionDto {

    private String opportunityId;

    private String distributorNumber;

    private Date creationDate;

    private String title;

    private String name;

    private String firstName;

    private String email;

    private String phoneNumber;

    private String overdraft;

    private String tnc;

    private String teg;

    private String taeg;

    private Integer term;

    private String monthlyPaymentWithoutInsurance;

    public String getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(final String opportunityId) {
        this.opportunityId = opportunityId;
    }

    public String getDistributorNumber() {
        return distributorNumber;
    }

    public void setDistributorNumber(final String distributorNumber) {
        this.distributorNumber = distributorNumber;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getOverdraft() {
        return overdraft;
    }

    public void setOverdraft(final String overdraft) {
        this.overdraft = overdraft;
    }

    public String getTnc() {
        return tnc;
    }

    public void setTnc(final String tnc) {
        this.tnc = tnc;
    }

    public String getTeg() {
        return teg;
    }

    public void setTeg(final String teg) {
        this.teg = teg;
    }

    public String getTaeg() {
        return taeg;
    }

    public void setTaeg(final String taeg) {
        this.taeg = taeg;
    }

    public Integer getTerm() {
        return term;
    }

    public void setTerm(final Integer term) {
        this.term = term;
    }

    public String getMonthlyPaymentWithoutInsurance() {
        return monthlyPaymentWithoutInsurance;
    }

    public void setMonthlyPaymentWithoutInsurance(final String monthlyPaymentWithoutInsurance) {
        this.monthlyPaymentWithoutInsurance = monthlyPaymentWithoutInsurance;
    }

}
