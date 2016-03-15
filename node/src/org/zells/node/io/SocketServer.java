package org.zells.node.io;

import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer implements Server {

    private final ServerSocket serverSocket;
    private final String host;
    private final int port;

    public SocketServer(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void listen(SignalListener listener) {
        while (true) {
            try {
                new Thread(new SignalWorker(listener, serverSocket.accept())).run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Peer makePeer(String host, Integer port) {
        return new SocketPeer(host, port);
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public int getPort() {
        return port;
    }

    private class SignalWorker implements Runnable {
        private final PrintWriter out;
        private final BufferedReader in;
        private SignalListener listener;
        private final Socket client;

        public SignalWorker(SignalListener listener, Socket client) throws IOException {
            this.listener = listener;
            this.client = client;

            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String signal = in.readLine();
                String response = listener.respondTo(signal);
                out.println(response);
            } catch (IOException e) {
                out.println(Protocol.fail(e.getMessage()));
                e.printStackTrace();
            }

            try {
                out.close();
                in.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
