package fr.training.springbatch.job.classifier.model;

public record PetCsvLine(
        String name,
        AdoptionStatus status,
        String species) {
}