package org.zells.node.model.connect;

import org.zells.node.io.PathParser;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;

import java.util.List;

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

    public static String deliver(Delivery delivery) {
        return DELIVER + " " +
                delivery.getTarget().in(delivery.getContext()) + " " +
                delivery.getMessage().in(delivery.getContext()) + " " +
                delivery.getRole();
    }

    public static boolean isDeliver(String signal) {
        return signal.startsWith(DELIVER);
    }

    public static Object[] parseDeliver(String signal) throws Exception {
        List<Path> parts = parse(signal, 3);
        return new Object[]{
                parts.get(0),
                parts.get(1),
                parts.get(2)
        };
    }

    public static String join(Path path, String host, int port) {
        return JOIN + " " + path + " " + host + " " + port;
    }

    public static boolean isJoin(String signal) {
        return signal.startsWith(JOIN);
    }

    public static Object[] parseJoin(String signal) throws Exception {
        List<Path> parts = parse(signal, 3);
        return new Object[]{
                parts.get(0),
                parts.get(1).toString(),
                Integer.parseInt(parts.get(2).toString())
        };
    }

    private static List<Path> parse(String signal, int argumentCount) throws Exception {
        List<Path> paths = PathParser.parse(signal);
        if (paths.size() != argumentCount + 1) {
            throw new Exception("Malformed signal");
        }
        return paths.subList(1, paths.size());
    }
}
