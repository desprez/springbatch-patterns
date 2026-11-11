package fr.training.springbatch.job.classifier.processor;

import static org.junit.jupiter.api.Assertions.*;

import fr.training.springbatch.job.classifier.model.AdoptionStatus;
import fr.training.springbatch.job.classifier.model.PetCsvLine;
import fr.training.springbatch.job.classifier.model.PetToCreate;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class NewPetProcessorTest {

    private static final NewPetProcessor NEW_PET_PROCESSOR = new NewPetProcessor();

    @Test
    void should_process_new_pet() {
        //given
        var csvLine = new PetCsvLine("dummy", AdoptionStatus.NEW, "cat");

        //when
        var petToCreate = NEW_PET_PROCESSOR.process(csvLine);

        //then
        assertThat(petToCreate).isNotNull().isInstanceOf(PetToCreate.class);

        assertThat(petToCreate.getName()).isEqualTo("dummy");
        assertThat(petToCreate.getStatus()).isEqualTo(AdoptionStatus.NEW);
        assertThat(petToCreate.getSpecies()).isEqualTo("cat");
    }
}