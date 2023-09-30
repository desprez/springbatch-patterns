package fr.training.springbatch.tools.synchro;

public class Bar {

    private String name;
    private int fooId;

    public Bar() {

    }

    public Bar(final String name, final int fooId) {
        this.name = name;
        this.fooId = fooId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getFooId() {
        return fooId;
    }

    public void setFooId(final int fooId) {
        this.fooId = fooId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fooId;
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Bar other = (Bar) obj;
        if (fooId != other.fooId) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("Bar [name=").append(name).append(", fooId=").append(fooId).append("]");
        return builder.toString();
    }

}