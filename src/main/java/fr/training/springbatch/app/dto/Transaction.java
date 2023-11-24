package fr.training.springbatch.app.dto;

import java.io.Serializable;
import java.time.LocalDate;

public record Transaction(Long customerNumber, String number, LocalDate transactionDate, Double amount) implements Serializable {

}