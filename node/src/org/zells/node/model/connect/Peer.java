package org.zells.node.model.connect;

public interface Peer {

    Signal send(Signal signal);

    Protocol getProtocol();
}
