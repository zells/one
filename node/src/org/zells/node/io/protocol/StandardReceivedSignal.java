package org.zells.node.io.protocol;

import org.zells.node.model.connect.Signal;
import org.zells.node.model.connect.signals.ReceivedSignal;
import org.zells.node.model.refer.Path;

import java.io.IOException;

public class StandardReceivedSignal extends ReceivedSignal {

    private static final String MARKER = "RECEIVED";

    public StandardReceivedSignal(Path path) {
        super(path);
    }

    @Override
    public String serialize() throws IOException {
        return MARKER + " " + StandardParser.serializePath(path);
    }

    public static boolean recognizes(String signal) {
        return signal.startsWith(MARKER);
    }

    public static Signal parse(String signal) throws IOException {
        if (signal.length() < MARKER.length() + 1) {
            throw new IOException("Malformed signal");
        }
        return new StandardReceivedSignal(
                StandardParser.parsePath(signal.substring(MARKER.length() + 1))
        );
    }
}
