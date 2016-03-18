package org.zells.node.model;

import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Signal;
import org.zells.node.model.connect.signals.ReceivedSignal;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.react.Reaction;
import org.zells.node.model.refer.Name;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Child;
import org.zells.node.model.refer.names.Parent;
import org.zells.node.model.refer.names.Root;
import org.zells.node.model.reflect.ReflectionCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cell {

    private Cell parent;
    private Reaction reaction;
    private Map<Name, Cell> children = new HashMap<Name, Cell>();
    private final List<Peer> peers = new ArrayList<Peer>();
    private Path stem;
    private Cell reflection;

    public Cell() {
    }

    public Cell(Cell parent) {
        this.parent = parent;
    }

    public Cell setStem(Path stem) {
        this.stem = stem;
        return this;
    }

    public Path getStem() {
        return stem;
    }

    public Cell setReaction(Reaction reaction) {
        this.reaction = reaction;
        return this;
    }

    public Cell createChild(String name) {
        return putChild(name, new Cell(this));
    }

    public Cell putChild(String name, Cell child) {
        children.put(Child.name(name), child);
        return child;
    }

    public boolean hasChild(Name name) {
        return name.toString().equals("z") || children.containsKey(name);
    }

    public Cell getChild(Name name) {
        if (name.toString().equals("z")) {
            if (reflection == null) {
                reflection = new ReflectionCell(this);
            }
            return reflection;
        }
        return children.get(name);
    }

    public Cell join(Peer peer, Path myPath, String myHost, int myPort) {
        peer.send(peer.getProtocol().join(myPath, myHost, myPort));
        return joinedBy(peer);
    }

    public Cell joinedBy(Peer peer) {
        peers.add(peer);
        return this;
    }

    public Path deliver(Delivery delivery) {
        Path reached;
        if ((reached = deliverToSelf(delivery)) != null
                || (reached = deliverToParent(delivery)) != null
                || (reached = deliverToSelfRoot(delivery)) != null
                || (reached = deliverToRoot(delivery)) != null
                || (reached = deliverToChild(delivery)) != null
                || (reached = deliverToPeers(delivery)) != null
                || (reached = deliverToStem(delivery)) != null) {
            return reached;
        }

        return null;
    }

    private Path deliverToSelf(Delivery delivery) {
        if (!delivery.hasArrived()
                || reaction == null) {
            return null;
        }

        return reaction.execute(this, delivery);
    }

    private Path deliverToParent(Delivery delivery) {
        if (parent == null
                || delivery.hasArrived()
                || !delivery.nextTarget().equals(Parent.name())) {
            return null;
        }
        return parent.deliver(delivery.toParent());
    }

    private Path deliverToSelfRoot(Delivery delivery) {
        if (parent != null
                || delivery.hasArrived()
                || !delivery.nextTarget().equals(Root.name())) {
            return null;
        }
        return deliver(delivery.toSelf());
    }

    private Path deliverToRoot(Delivery delivery) {
        if (parent == null
                || delivery.hasArrived()
                || !delivery.nextTarget().equals(Root.name())) {
            return null;
        }
        return parent.deliver(delivery.toParent());
    }

    private Path deliverToChild(Delivery delivery) {
        if (delivery.hasArrived() || !hasChild(delivery.nextTarget())) {
            return null;
        }

        return getChild(delivery.nextTarget()).deliver(delivery.toChild());
    }

    private Path deliverToPeers(Delivery delivery) {
        for (Peer peer : peers) {
            Signal response = peer.send(peer.getProtocol().deliver(delivery));
            if (response instanceof ReceivedSignal) {
                return ((ReceivedSignal) response).getPath();
            }
        }

        if (parent == null) {
            return null;
        }

        return parent.deliverToPeers(delivery.toParent());
    }

    private Path deliverToStem(Delivery delivery) {
        Path reached;

        if (stem == null
                || parent == null
                || (stem.first() instanceof Child)
                || stem.isIn(delivery.getContext())) {
            return null;
        }

        if ((reached = parent.deliver(delivery.toStem(stem))) == null) {
            return null;
        }

        adopt(delivery);
        return reached;
    }

    private boolean adopt(Delivery delivery) {
        Cell cell = this;
        Path current = stem;
        while (!delivery.hasArrived()) {
            current = current.with(delivery.nextTarget());
            cell.createChild(delivery.nextTarget().toString());

            cell = cell.getChild(delivery.nextTarget());
            cell.setStem(current);

            delivery = delivery.toChild();
        }
        return true;
    }
}
