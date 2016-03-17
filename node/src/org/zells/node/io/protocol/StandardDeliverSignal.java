package org.zells.node.io.protocol;

import org.zells.node.model.connect.Signal;
import org.zells.node.model.connect.signals.DeliverSignal;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Root;

import java.io.IOException;
import java.util.Arrays;

class StandardDeliverSignal extends DeliverSignal {
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
                StandardParser.serializePath(delivery.getTarget().in(delivery.getContext())) + " " +
                StandardParser.serializePath(delivery.getMessage().in(delivery.getContext())) + " " +
                StandardParser.serializePath(delivery.getRole());
    }

    @Override
    public String toString() {
        try {
            return Arrays.asList(new String[]{
                    StandardParser.serializePath(delivery.getContext()),
                    StandardParser.serializePath(delivery.getTarget()),
                    StandardParser.serializePath(delivery.getMessage()),
                    StandardParser.serializePath(delivery.getRole())
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
                StandardParser.parsePath(parts[1]).rest(),
                StandardParser.parsePath(parts[2]).rest(),
                StandardParser.parsePath(parts[3])
        ));
    }
}
