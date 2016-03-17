package org.zells.node.model.refer.names;

import org.zells.node.model.refer.Name;

public class Child implements Name {

    private String name;

    private Child(String name) {
        this.name = name;
    }

    public static Child name(String name) {
        return new Child(name);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Child && ((Child) obj).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
