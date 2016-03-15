package org.zells.node.model;

import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Protocol;
import org.zells.node.model.refer.*;
import org.zells.node.model.respond.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cell {

    private Cell parent;
    private Response response;
    private Map<Name, Cell> children = new HashMap<Name, Cell>();
    private final List<Peer> peers = new ArrayList<Peer>();

    public Cell() {
    }

    public Cell(Cell parent) {
        this.parent = parent;
    }

    public Cell setResponse(Response response) {
        this.response = response;
        return this;
    }

    public Cell createChild(String name) {
        Cell child = new Cell(this);
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
        peer.send(Protocol.join(myPath, myHost, myPort));
        return joinedBy(peer);
    }

    public Cell joinedBy(Peer peer) {
        peers.add(peer);
        return this;
    }

    public boolean deliver(Path context, Path target, Path message) {
        if (target.isEmpty()) {
            execute(context, message);
            return true;
        }

        Name name = target.first();

        if (name instanceof Parent && parent != null) {
            return parent.deliver(context.up(), target.rest(), message.in(context.last()));
        }

        if (name instanceof Root) {
            if (parent == null) {
                return deliver(context, new Path(), message);
            }

            return parent.deliver(context.up(), target, message.in(context.last()));
        }


        if (children.containsKey(name)) {
            return children.get(name).deliver(context.with(name), target.rest(), message.in(Parent.name()));
        }

        return deliverToPeers(context, target, message);
    }

    private boolean deliverToPeers(Path context, Path target, Path message) {
        for (Peer peer : peers) {
            if (peer.send(Protocol.deliver(context, target, message)).equals(Protocol.ok())) {
                return true;
            }
        }

        return parent != null
                && parent.deliverToPeers(context.up(), target.in(context.last()), message.in(context.last()));
    }

    protected void execute(Path context, Path message) {
        if (response != null) {
            response.execute(this, context, message);
            return;
        }

        deliverToPeers(context, new Path(), message);
    }
}
