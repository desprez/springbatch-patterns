package fr.training.springbatch.job.classifier.processor;

import fr.training.springbatch.job.classifier.model.PetCsvLine;
import fr.training.springbatch.job.classifier.model.PetToDelete;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.ItemProcessor;

public class AdoptedPetProcessor implements ItemProcessor<PetCsvLine, PetToDelete> {

    @Override
    public PetToDelete process(@NonNull PetCsvLine item) {
        return new PetToDelete(item.name());
    }
}