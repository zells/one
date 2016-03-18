package org.zells.node;

import org.junit.Before;
import org.zells.node.io.protocol.StandardProtocol;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Protocol;
import org.zells.node.model.connect.Signal;
import org.zells.node.model.connect.signals.DeliverSignal;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.react.Reaction;
import org.zells.node.model.refer.Name;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Child;
import org.zells.node.model.refer.names.Parent;
import org.zells.node.model.refer.names.Root;

import java.util.ArrayList;
import java.util.List;

public abstract class Specification {

    protected SpecReaction reaction;
    protected Protocol protocol = new StandardProtocol();

    @Before
    public void setUp() throws Exception {
        reaction = new SpecReaction();
    }

    protected Path path(String s) {
        List<Name> names = new ArrayList<Name>();
        for (String part : s.split("\\.")) {
            if (part.equals("*")) {
                names.add(Root.name());
            } else if (part.equals("^")) {
                names.add(Parent.name());
            } else if (!part.isEmpty()) {
                names.add(Child.name(part));
            }
        }
        return new Path(names);
    }

    protected class SpecReaction implements Reaction {
        public Delivery executedWith;
        public Cell executedBy;
        @Override
        public Path execute(Cell cell, Delivery delivery) {
            executedBy = cell;
            executedWith = delivery;
            return delivery.getContext();
        }
    }

    protected class SpecPeer implements Peer {
        public String sentTo;
        private Signal response;
        public Signal sent;

        public SpecPeer() {
        }

        public SpecPeer(Signal response) {
            this.response = response;
        }

        public SpecPeer(String host, Integer port) {
            this.sentTo = host + ":" + port;
        }

        @Override
        public Signal send(Signal signal) {
            sent = signal;
            if (response != null) {
                return response;
            } else if (signal instanceof DeliverSignal) {
                Delivery delivery = ((DeliverSignal) signal).getDelivery();
                return protocol.received(delivery.getTarget().in(delivery.getContext()));
            } else {
                return protocol.ok();
            }
        }

        @Override
        public Protocol getProtocol() {
            return protocol;
        }

    }
}
