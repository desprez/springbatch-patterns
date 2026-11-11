package fr.training.springbatch.job.classifier;

import fr.training.springbatch.job.classifier.model.AdoptionStatus;
import fr.training.springbatch.job.classifier.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

class PetProcessorClassifierTest {

    private static final PetProcessorClassifier CLASSIFIER = new PetProcessorClassifier(
            it -> new PetToUpdate(it.name(), it.status()),
            it -> new PetToDelete(it.name()),
            it -> new PetToCreate(it.name(), it.status(), it.species())
    );

    @Test
    void should_classify_as_new() throws Exception {
        //given
        var csvLine = new PetCsvLine("toto", AdoptionStatus.NEW, "cat");

        //when
        var processor = CLASSIFIER.classify(csvLine);

        //then
        assertThat(processor.process(csvLine)).isInstanceOf(PetToCreate.class);
    }

    @Test
    void should_classify_as_adopted() throws Exception {
        //given
        var csvLine = new PetCsvLine("toto", AdoptionStatus.ADOPTED, "cat");

        //when
        var processor = CLASSIFIER.classify(csvLine);

        //then
        assertThat(processor.process(csvLine)).isInstanceOf(PetToDelete.class);
    }

    @ParameterizedTest
    @EnumSource(value = AdoptionStatus.class, names = { "WAITING", "AVAILABLE" })
    void should_classify_as_updated(AdoptionStatus status) throws Exception {
        //given
        var csvLine = new PetCsvLine("toto", status, "cat");

        //when
        var processor = CLASSIFIER.classify(csvLine);

        //then
        assertThat(processor.process(csvLine)).isInstanceOf(PetToUpdate.class);
    }
}