package fr.training.springbatch.job.classifier.model;

public class PetToUpdate extends PetAction {
    private AdoptionStatus status;

    public PetToUpdate(String name, AdoptionStatus status) {
        super(name);
        this.status = status;
    }

    public AdoptionStatus getStatus() {
        return status;
    }

    public void setStatus(AdoptionStatus status) {
        this.status = status;
    }
}