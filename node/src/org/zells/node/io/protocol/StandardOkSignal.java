package org.zells.node.io.protocol;

import org.zells.node.model.connect.Signal;
import org.zells.node.model.connect.signals.OkSignal;

class StandardOkSignal extends OkSignal {
    private static final String MARKER = "OK";

    @Override
    public String serialize() {
        return MARKER;
    }

    public static boolean recognizes(String signal) {
        return signal.startsWith(MARKER);
    }

    public static Signal parse() {
        return new StandardOkSignal();
    }
}
