package org.zells.node.model.connect;

import org.zells.node.model.refer.Path;

public class Protocol {

    private static final String OK = "OK";
    private static final String FAIL = "FAIL";
    private static final String DELIVER = "DELIVER";
    private static final String JOIN = "JOIN";

    public static String ok() {
        return OK;
    }

    public static String fail(String message) {
        return FAIL + " " + message;
    }

    public static String deliver(Path target, Path message) {
        return DELIVER + " " + target + " " + message;
    }

    public static boolean isDeliver(String signal) {
        return signal.startsWith(DELIVER);
    }

    public static Object[] parseDeliver(String signal) throws Exception {
        String[] parts = split(signal, 2);
        return new Object[]{
                Path.parse(parts[1]),
                Path.parse(parts[2])
        };
    }

    public static String join(Path path, String host, int port) {
        return JOIN + " " + path + " " + host + " " + port;
    }

    public static boolean isJoin(String signal) {
        return signal.startsWith(JOIN);
    }

    public static Object[] parseJoin(String signal) throws Exception {
        String[] parts = split(signal, 3);
        return new Object[]{
                Path.parse(parts[1]),
                parts[2],
                Integer.parseInt(parts[3])
        };
    }

    private static String[] split(String signal, int argumentCount) throws Exception {
        String[] parts = signal.split(" ");
        if (parts.length != argumentCount + 1) {
            throw new Exception("Malformed signal");
        }
        return parts;
    }
}
