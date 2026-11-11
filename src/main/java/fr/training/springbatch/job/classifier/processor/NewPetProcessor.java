package fr.training.springbatch.job.classifier.processor;

import fr.training.springbatch.job.classifier.model.PetCsvLine;
import fr.training.springbatch.job.classifier.model.PetToCreate;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class NewPetProcessor implements ItemProcessor<PetCsvLine, PetToCreate> {

    @Override
    public PetToCreate process(@NonNull PetCsvLine item) {
        return new PetToCreate(item.name(), item.status(), item.species());
    }
}