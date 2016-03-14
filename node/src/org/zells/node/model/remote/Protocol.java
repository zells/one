package org.zells.node.model.remote;

import org.zells.node.model.reference.Path;

public class Protocol {


    public static String ack() {
        return "ACK";
    }

    public static String deliver(Path context, Path target, Path message) {
        return "DELIVER " + context + " " + target + " " + message;
    }

    public static String fail(String message) {
        return "FAIL " + message;
    }
}
