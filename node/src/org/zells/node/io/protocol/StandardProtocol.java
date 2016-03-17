package org.zells.node.io.protocol;

import org.zells.node.model.connect.Protocol;
import org.zells.node.model.connect.Signal;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.*;

import java.io.IOException;

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

}
