package org.zells.node.model.remote;

import org.zells.node.model.Cell;
import org.zells.node.model.local.Peer;
import org.zells.node.model.reference.Path;

import java.util.ArrayList;
import java.util.List;

public class RemoteCell implements Cell {

    private final List<Peer> peers = new ArrayList<Peer>();

    public RemoteCell(Cell parent, Peer peer) {
        peers.add(peer);
    }

    @Override
    public boolean deliver(Path context, Path target, Path message) {
        for (Peer peer : peers) {
            if (peer.send(Protocol.deliver(context, target, message)).equals(Protocol.ack())) {
                return true;
            }
        }
        return false;
    }

    public RemoteCell addPeer(Peer peer) {
        peers.add(peer);
        return this;
    }
}