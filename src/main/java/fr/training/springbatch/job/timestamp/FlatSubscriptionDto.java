package fr.training.springbatch.job.timestamp;

import java.util.Date;

public record FlatSubscriptionDto(String number, String distributor, Date createDate, String title, String lastName, String firstName, String email,
        String phoneNumber, String overdraft, String tnc, String teg, String taeg, Integer term, String monthlyPayment) {

}
