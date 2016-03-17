package org.zells.node.model.refer.names;

import org.zells.node.model.refer.Name;

public class Message implements Name {

    private static Message singleton = new Message();

    private Message() {
    }

    public static Message name() {
        return singleton;
    }

    @Override
    public String toString() {
        return "<message>";
    }
}
