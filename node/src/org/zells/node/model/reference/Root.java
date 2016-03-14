package org.zells.node.model.reference;

public class Root implements Name {

    private static Root singleton = new Root();

    private Root() {
    }

    public static Root name() {
        return singleton;
    }

    @Override
    public String toString() {
        return "°";
    }
}
