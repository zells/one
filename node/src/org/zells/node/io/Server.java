package org.zells.node.io;

import org.zells.node.model.local.Peer;

public interface Server {

    void listen(SignalListener listener);

    Peer makePeer(String host, Integer port);

    String getHost();

    int getPort();
}
