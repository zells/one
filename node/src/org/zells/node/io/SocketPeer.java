package org.zells.node.io;

import org.zells.node.model.local.Peer;
import org.zells.node.model.remote.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketPeer implements Peer {

    private final String host;
    private final int port;

    public SocketPeer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String send(String signal) {
        try {
            Socket socket = new Socket(host, port);

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(signal);
            String response = in.readLine();

            out.close();
            in.close();
            socket.close();

            return response;
        } catch (IOException e) {
            return Protocol.fail(e.getMessage());
        }
    }
}
