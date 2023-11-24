package fr.training.springbatch.tools.incrementer;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

class TodayJobParameterProviderTest {

    @Test
    void should_return_valued_jobParameter_if_absent() {
        // Given
        final TodayJobParameterProvider jobParameterProvider = new TodayJobParameterProvider("today");

        // When
        final JobParameters result = jobParameterProvider.getNext(null);

        // Then
        assertThat(result.getLocalDate("today")).isEqualTo(LocalDate.now());
    }

    @Test
    void should_return_new_valued_jobParameter_if_present() {
        // Given
        final TodayJobParameterProvider jobParameterProvider = new TodayJobParameterProvider("today");

        final JobParameters lastJobParameters = new JobParametersBuilder()
                .addLong(ExtendedRunIdIncrementer.RUN_ID_KEY, 2L) //
                .addString("otherParameter", "avalue") //
                .toJobParameters();
        // When
        final JobParameters result = jobParameterProvider.getNext(lastJobParameters);

        // Then
        assertThat(result.getLocalDate("today")).isEqualTo(LocalDate.now());
        assertThat(result.getString("otherParameter")).isEqualTo("avalue");
    }

    @Test
    void should_return_old_valued_default_jobParameter_if_present() {
        // Given
        final TodayJobParameterProvider jobParameterProvider = new TodayJobParameterProvider();

        final JobParameters lastJobParameters = new JobParametersBuilder()
                .addLocalDate("currentDate", LocalDate.now().minusDays(1)) //
                .addString("otherParameter", "avalue") //
                .toJobParameters();
        // When
        final JobParameters result = jobParameterProvider.getNext(lastJobParameters);

        // Then
        assertThat(result.getLocalDate("currentDate")).isEqualTo(LocalDate.now().minusDays(1));
        assertThat(result.getString("otherParameter")).isEqualTo("avalue");
    }
}
