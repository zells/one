package org.zells.node.io.protocol;

import org.zells.node.model.connect.Signal;
import org.zells.node.model.connect.signals.JoinSignal;
import org.zells.node.model.refer.Path;

import java.io.IOException;

class StandardJoinSignal extends JoinSignal {
    private static final String MARKER = "JOIN";
    private final Path path;
    private final String host;
    private final int port;

    public StandardJoinSignal(Path path, String host, int port) {
        this.path = path;
        this.host = host;
        this.port = port;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String serialize() throws IOException {
        return MARKER + " " +
                StandardParser.serializePath(path) + " " +
                host + " " +
                port;
    }

    public static boolean recognizes(String signal) {
        return signal.startsWith(MARKER);
    }

    public static Signal parse(String signal) throws IOException {
        String[] parts = signal.split(" ");
        if (parts.length != 4) {
            throw new IOException("Malformed signal");
        }
        return new StandardJoinSignal(
                StandardParser.parsePath(parts[1]),
                parts[2],
                Integer.parseInt(parts[3])
        );
    }
}
