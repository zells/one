package org.zells.node.io;

import org.zells.node.model.connect.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
                new Thread(new SignalWorker(listener, serverSocket.accept())).run();
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
                Signal response = listener.respondTo(protocol.parse(signal));
                out.println(response.serialize());
            } catch (IOException e) {
                out.println(protocol.fail(e.getMessage()));
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

    private class SocketPeer implements Peer {

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
}
