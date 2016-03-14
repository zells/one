package org.zells.node.model.reference;

public class Root extends Name {

    private static Root singleton = new Root();

    private Root() {
        super("*");
    }

    public static Name name() {
        return singleton;
    }
}
