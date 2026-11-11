package fr.training.springbatch.job.classifier.writer;

import fr.training.springbatch.job.classifier.PetService;
import fr.training.springbatch.job.classifier.model.PetToCreate;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

public class CreatePetWriter implements ItemWriter<PetToCreate> {

    private final PetService petService;


    public CreatePetWriter(PetService petService) {
        this.petService = petService;
    }

    @Override
    public void write(Chunk<? extends PetToCreate> pets) {
        pets.forEach(petService::createPet);
    }
}