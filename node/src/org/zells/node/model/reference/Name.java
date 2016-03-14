package org.zells.node.model.reference;

public class Name {

    private String name;

    public Name(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Name && ((Name) obj).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
