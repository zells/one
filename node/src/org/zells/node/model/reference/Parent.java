package org.zells.node.model.reference;

public class Parent extends Name {

    private static Parent singleton = new Parent();

    private Parent() {
        super("^");
    }

    public static Name name() {
        return singleton;
    }
}
