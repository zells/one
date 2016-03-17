package org.zells.node.io.protocol;

import org.zells.node.model.connect.Signal;
import org.zells.node.model.connect.signals.FailSignal;

import java.io.IOException;

class StandardFailSignal extends FailSignal {
    private static final String MARKER = "FAIL";
    private String message;

    public StandardFailSignal(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String serialize() {
        return MARKER + " " + message;
    }

    public static boolean recognizes(String signal) {
        return signal.startsWith(MARKER);
    }

    public static Signal parse(String signal) throws IOException {
        if (signal.length() < MARKER.length() + 1) {
            throw new IOException("Malformed signal");
        }
        return new StandardFailSignal(signal.substring(MARKER.length() + 1));
    }

    @Override
    public String toString() {
        return "Fail: " + message;
    }
}
