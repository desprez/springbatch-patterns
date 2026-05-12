package fr.training.springbatch.job.multidestinationjob;

import fr.training.springbatch.app.dto.Customer;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.classify.Classifier;

import java.io.Serial;

public class CustomerClassifier implements Classifier<Customer, ItemWriter<? super Customer>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private final ItemWriter<Customer> after50Writer;
    private final ItemWriter<Customer> before50Writer;

    public CustomerClassifier(final ItemWriter<Customer> after50Writer, final ItemWriter<Customer> before50Writer) {
        this.after50Writer = after50Writer;
        this.before50Writer = before50Writer;
    }

    @Override
    public ItemWriter<? super Customer> classify(final Customer customer) {
        return customer.getAge() > 50 ? after50Writer : before50Writer;
    }
}