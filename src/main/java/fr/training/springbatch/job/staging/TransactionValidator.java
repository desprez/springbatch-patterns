package fr.training.springbatch.job.staging;

import java.time.LocalDate;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import fr.training.springbatch.app.dto.Transaction;

public class TransactionValidator implements Validator {

    @Override
    public boolean supports(final Class<?> clazz) {
        return clazz.equals(Transaction.class);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
        final Transaction transaction = (Transaction) target;
        if (transaction.number().length() != 8) {
            errors.rejectValue("number", "number_length");
        }
        if (transaction.transactionDate().isAfter(LocalDate.now())) {
            errors.rejectValue("transactionDate", "transactionDate_future");
        }
    }
}