package org.zells.node.model.reference;

public class Parent implements Name {

    private static Parent singleton = new Parent();

    private Parent() {
    }

    public static Parent name() {
        return singleton;
    }

    @Override
    public String toString() {
        return "^";
    }
}
