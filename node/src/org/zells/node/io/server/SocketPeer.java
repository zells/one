package org.zells.node.io.server;

import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Protocol;
import org.zells.node.model.connect.Signal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class SocketPeer implements Peer {

    private final Protocol protocol;
    private final String host;
    private final int port;

    public SocketPeer(Protocol protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    @Override
    public Signal send(Signal signal) {
        try {
            Socket socket = new Socket(host, port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(signal.serialize());
            String response = in.readLine();

            out.close();
            in.close();
            socket.close();

            return protocol.parse(response);
        } catch (IOException e) {
            return protocol.fail(e.getMessage());
        }
    }

    @Override
    public Protocol getProtocol() {
        return protocol;
    }
}
