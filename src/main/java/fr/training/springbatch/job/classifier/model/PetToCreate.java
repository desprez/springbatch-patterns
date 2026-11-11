package fr.training.springbatch.job.classifier.model;

public class PetToCreate extends PetAction {
    private AdoptionStatus status;
    private String species;

    public PetToCreate(String name, AdoptionStatus status, String species) {
        super(name);
        this.status = status;
        this.species = species;
    }

    public AdoptionStatus getStatus() {
        return status;
    }

    public void setStatus(AdoptionStatus status) {
        this.status = status;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }
}