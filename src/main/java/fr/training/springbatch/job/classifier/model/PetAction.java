package fr.training.springbatch.job.classifier.model;

public abstract class PetAction {
    protected PetAction(String name) {
        this.name = name;
    }

    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}