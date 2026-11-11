package fr.training.springbatch.job.classifier.writer;

import fr.training.springbatch.job.classifier.PetService;
import fr.training.springbatch.job.classifier.model.PetToDelete;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

public class DeletePetWriter implements ItemWriter<PetToDelete> {

    private final PetService petService;

    public DeletePetWriter(PetService petService) {
        this.petService = petService;
    }

    @Override
    public void write(Chunk<? extends PetToDelete> pets) {
        pets.forEach(petService::deletePet);
    }
}