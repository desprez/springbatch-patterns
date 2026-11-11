package fr.training.springbatch.job.classifier.processor;

import static org.assertj.core.api.Assertions.assertThat;

import fr.training.springbatch.job.classifier.model.AdoptionStatus;
import fr.training.springbatch.job.classifier.model.PetCsvLine;
import fr.training.springbatch.job.classifier.model.PetToDelete;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdoptedPetProcessorTest {

    @InjectMocks
    private AdoptedPetProcessor adoptedPetProcessor;

    @Test
    void should_process_adopted_pet() {
        //given
        var csvLine = new PetCsvLine("toto", AdoptionStatus.ADOPTED, "cat");

        //when
        var petToUpdate = adoptedPetProcessor.process(csvLine);

        //then
        assertThat(petToUpdate).isNotNull().isInstanceOf(PetToDelete.class);
        assertThat(petToUpdate.getName()).isEqualTo("toto");
    }

}