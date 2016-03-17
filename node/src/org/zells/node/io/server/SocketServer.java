package org.zells.node.io.server;

import org.zells.node.model.connect.*;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketServer implements Server {

    private final ServerSocket serverSocket;
    private final Protocol protocol;

    public SocketServer(Protocol protocol, int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.protocol = protocol;
    }

    @Override
    public void listen(SignalListener listener) {
        while (true) {
            try {
                new Thread(new SignalWorker(protocol, listener, serverSocket.accept())).run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Peer makePeer(String host, Integer port) {
        return new SocketPeer(protocol, host, port);
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }

}
