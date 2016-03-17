package org.zells.node.model.refer.names;

import org.zells.node.model.refer.Name;

public class Execution implements Name {

    private static Execution singleton = new Execution();

    private Execution() {
    }

    public static Execution name() {
        return singleton;
    }

    @Override
    public String toString() {
        return "<execution>";
    }
}
