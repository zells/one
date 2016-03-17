package org.zells.node.io;

import org.zells.node.model.connect.Protocol;
import org.zells.node.model.connect.Signal;
import org.zells.node.model.connect.signals.DeliverSignal;
import org.zells.node.model.connect.signals.FailSignal;
import org.zells.node.model.connect.signals.JoinSignal;
import org.zells.node.model.connect.signals.OkSignal;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.*;

import java.io.IOException;
import java.util.Arrays;

public class StandardProtocol implements Protocol {

    public Signal ok() {
        return new StandardOkSignal();
    }

    public Signal fail(String message) {
        return new StandardFailSignal(message);
    }

    public Signal deliver(Delivery delivery) {
        return new StandardDeliverSignal(delivery);
    }

    public Signal join(Path path, String host, int port) {
        return new StandardJoinSignal(path, host, port);
    }

    @Override
    public Signal parse(String signal) throws IOException {
        if (StandardOkSignal.recognizes(signal)) {
            return StandardOkSignal.parse();
        } else if (StandardFailSignal.recognizes(signal)) {
            return StandardFailSignal.parse(signal);
        } else if (StandardDeliverSignal.recognizes(signal)) {
            return StandardDeliverSignal.parse(signal);
        } else if (StandardJoinSignal.recognizes(signal)) {
            return StandardJoinSignal.parse(signal);
        }

        throw new IOException("Signal not recognized");
    }

    private static String serializePath(Path path) throws IOException {
        StringBuilder serialized = new StringBuilder();
        for (Name n : path.getNames()) {
            serialized.append(".");
            if (n instanceof Root) {
                serialized.append("*");
            } else if (n instanceof Parent) {
                serialized.append("^");
            } else if (n instanceof Child) {
                serialized.append(((Child) n).getName());
            } else {
                throw new IOException("Could not serialize path");
            }
        }

        return serialized.toString().substring(1);
    }

    private static Path inflatePath(String string) {
        Path path = new Path();
        for (String s : string.split("\\.")) {
            if (s.equals("*")) {
                path = path.with(Root.name());
            } else if (s.equals("^")) {
                path = path.with(Parent.name());
            } else {
                path = path.with(Child.name(s));
            }
        }

        return path;
    }

    private static class StandardOkSignal extends OkSignal {
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

    private static class StandardFailSignal extends FailSignal {
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

    private static class StandardDeliverSignal extends DeliverSignal {
        private static final String MARKER = "DELIVER";
        private Delivery delivery;

        public StandardDeliverSignal(Delivery delivery) {
            this.delivery = delivery;
        }

        @Override
        public Delivery getDelivery() {
            return delivery;
        }

        @Override
        public String serialize() throws IOException {
            return MARKER + " " +
                    serializePath(delivery.getTarget().in(delivery.getContext())) + " " +
                    serializePath(delivery.getMessage().in(delivery.getContext())) + " " +
                    serializePath(delivery.getRole());
        }

        @Override
        public String toString() {
            try {
                return Arrays.asList(new String[]{
                    serializePath(delivery.getContext()),
                    serializePath(delivery.getTarget()),
                    serializePath(delivery.getMessage()),
                    serializePath(delivery.getRole())
                }).toString();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        public static boolean recognizes(String signal) {
            return signal.startsWith(MARKER);
        }

        public static Signal parse(String signal) throws IOException {
            String[] parts = signal.split(" ");
            if (parts.length != 4) {
                throw new IOException("Malformed signal");
            }
            return new StandardDeliverSignal(new Delivery(
                    new Path(Root.name()),
                    inflatePath(parts[1]).rest(),
                    inflatePath(parts[2]).rest(),
                    inflatePath(parts[3])
            ));
        }
    }

    private static class StandardJoinSignal extends JoinSignal {
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
                    serializePath(path) + " " +
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
                    inflatePath(parts[1]),
                    parts[2],
                    Integer.parseInt(parts[3])
            );
        }
    }
}
