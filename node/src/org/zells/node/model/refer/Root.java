package org.zells.node.model.refer;

public class Root implements Name {

    private static Root singleton = new Root();

    private Root() {
    }

    public static Root name() {
        return singleton;
    }

    @Override
    public String toString() {
        return "root";
    }
}
