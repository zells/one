package org.zells.node.model;

import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.signals.OkSignal;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.react.Reaction;
import org.zells.node.model.refer.*;

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
        return children.containsKey(name);
    }

    public Cell getChild(Name name) {
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

    public boolean deliver(Delivery delivery) {
        return deliverToSelf(delivery)
                || deliverToParent(delivery)
                || deliverToSelfRoot(delivery)
                || deliverToRoot(delivery)
                || deliverToChild(delivery)
                || deliverToPeers(delivery)
                || deliverToStem(delivery);
    }

    protected boolean execute(Delivery delivery) {
        if (reaction != null) {
            reaction.execute(this, delivery);
            return true;
        }

        return false;
    }

    private boolean deliverToSelf(Delivery delivery) {
        return delivery.hasArrived()
                && execute(delivery);
    }

    private boolean deliverToParent(Delivery delivery) {
        return !delivery.hasArrived()
                && delivery.nextTarget() instanceof Parent
                && parent != null
                && parent.deliver(delivery.toParent());

    }

    private boolean deliverToSelfRoot(Delivery delivery) {
        return !delivery.hasArrived()
                && delivery.nextTarget() instanceof Root
                && parent == null
                && deliver(delivery.toSelf());

    }

    private boolean deliverToRoot(Delivery delivery) {
        return !delivery.hasArrived()
                && delivery.nextTarget() instanceof Root
                && parent != null
                && parent.deliver(delivery.toParent());

    }

    private boolean deliverToChild(Delivery delivery) {
        return !delivery.hasArrived()
                && children.containsKey(delivery.nextTarget())
                && children.get(delivery.nextTarget()).deliver(delivery.toChild());
    }

    private boolean deliverToPeers(Delivery delivery) {
        for (Peer peer : peers) {
            if (peer.send(peer.getProtocol().deliver(delivery)) instanceof OkSignal) {
                return true;
            }
        }

        return parent != null
                && parent.deliverToPeers(delivery.toParent());
    }

    private boolean deliverToStem(Delivery delivery) {
        return stem != null
                && !(stem.first() instanceof Child)
                && !stem.isIn(delivery.getContext())
                && parent != null
                && parent.deliver(delivery.toStem(stem))
                && adopt(delivery);
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
