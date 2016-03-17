package org.zells.node.model.connect;

public interface Server {

    void listen(SignalListener listener);

    Peer makePeer(String host, Integer port);

    Protocol getProtocol();
}
