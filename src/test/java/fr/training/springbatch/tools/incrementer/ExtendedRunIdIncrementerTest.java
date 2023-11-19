package fr.training.springbatch.tools.incrementer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

class ExtendedRunIdIncrementerTest {

    @Test
    void extendedRunIdIncrementer_should_add_new_runid_parameter_from_scratch() {
        // Given
        final ExtendedRunIdIncrementer incrementer = new ExtendedRunIdIncrementer();

        // When
        final JobParameters parameters = incrementer.getNext(null);

        // Then
        assertThat(parameters.getLong(ExtendedRunIdIncrementer.RUN_ID_KEY)).isEqualTo(1);
    }

    @Test
    void extendedRunIdIncrementer_should_add_the_given_key_parameter() {
        // Given
        final ExtendedRunIdIncrementer incrementer = new ExtendedRunIdIncrementer("NEWKEY");

        // When
        final JobParameters parameters = incrementer.getNext(null);

        // Then
        assertThat(parameters.getLong("NEWKEY")).isEqualTo(1);
    }

    @Test
    void extendedRunIdIncrementer_should_increment_exiting_jobparameter() {
        // Given
        final JobParameters lastJobParameters = new JobParametersBuilder()
                .addLong(ExtendedRunIdIncrementer.RUN_ID_KEY, 2L) //
                .addString("otherParameter", "avalue") //
                .toJobParameters();
        final ExtendedRunIdIncrementer incrementer = new ExtendedRunIdIncrementer();

        // When
        final JobParameters parameters = incrementer.getNext(lastJobParameters);

        // Then
        assertThat(parameters.getParameters()).hasSize(1);
        assertThat(parameters.getLong(ExtendedRunIdIncrementer.RUN_ID_KEY)).isEqualTo(3);
    }

}
