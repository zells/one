package org.zells.node.model;

import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Protocol;
import org.zells.node.model.refer.Name;
import org.zells.node.model.refer.Path;

import java.util.ArrayList;
import java.util.List;

public class RemoteCell extends Cell {

    private final String host;
    private final int port;
    private final List<Peer> peers = new ArrayList<Peer>();

    public RemoteCell(Cell parent, String host, int port) {
        super(parent);
        this.host = host;
        this.port = port;
    }

    @Override
    protected void execute(Path context, Path message) {
        deliverOn(context, new Path(), message);
    }

    @Override
    public boolean deliverOn(Path context, Path target, Path message) {
        for (Peer peer : peers) {
            if (peer.send(Protocol.deliver(context, target, message)).equals(Protocol.ok())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Name nameOf(Cell child) {
        throw new RuntimeException("Remote cells cannot have children.");
    }

    @Override
    public LocalCell resolve(Path path) {
        throw new RuntimeException("Remote cells cannot resolve paths");
    }

    public RemoteCell join(Peer peer) {
        peer.send(Protocol.join(getPath(), host, port));
        return joinedBy(peer);
    }

    public RemoteCell joinedBy(Peer peer) {
        peers.add(peer);
        return this;
    }
}