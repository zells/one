package org.zells.node.model.remote;

import org.zells.node.model.reference.Path;

public class Protocol {

    private static final String DELIVER = "DELIVER";
    private static final String JOIN = "JOIN";

    public static String ack() {
        return "ACK";
    }

    public static String deliver(Path context, Path target, Path message) {
        return DELIVER + " " + context + " " + target + " " + message;
    }

    public static String fail(String message) {
        return "FAIL " + message;
    }

    public static String join(Path path, String host, int port) {
        return JOIN + " " + path + " " + host + " " + port;
    }

    public static boolean isDeliver(String signal) {
        return signal.startsWith(DELIVER);
    }

    public static Object[] parseDeliver(String signal) throws Exception {
        String[] parts = split(signal, 4);
        return new Object[]{
                Path.parse(parts[1]),
                Path.parse(parts[2]),
                Path.parse(parts[3])
        };
    }

    public static boolean isJoin(String signal) {
        return signal.startsWith(JOIN);
    }

    public static Object[] parseJoin(String signal) throws Exception {
        String[] parts = split(signal, 4);
        return new Object[]{
                Path.parse(parts[1]),
                parts[2],
                Integer.parseInt(parts[3])
        };
    }

    private static String[] split(String signal, int count) throws Exception {
        String[] parts = signal.split(" ");
        if (parts.length != count) {
            throw new Exception("Malformed signal");
        }
        return parts;
    }
}
