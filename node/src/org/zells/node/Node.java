package org.zells.node;

import org.zells.node.model.remote.Peer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Node {

    private final ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        int portNumber = 9999;
        if (args.length == 1) {
            portNumber = Integer.parseInt(args[0]);
        }

        new Node(portNumber).run();
    }

    public Node(int portNumber) throws IOException {
        serverSocket = new ServerSocket(portNumber);
    }

    private void run() throws IOException {
        while (true) {
            new Thread(new SignalWorker(serverSocket.accept()))
                    .start();
        }
    }

    private void handleSignal(String signal) throws Exception {
        String[] parameters = signal.split(" ");

//        switch (parameters[0]) {
//            case Peer.SIGNAL_DELIVER:
//                if (parameters.length != 4) {
//                    throw new Exception("Malformed signal");
//                }
//                root.deliver(Path.parse(parameters[1]), Path.parse(parameters[2]), Path.parse(parameters[3]));
//                return;
//            case Peer.SIGNAL_JOIN:
//                if (parameters.length != 4) {
//                    throw new Exception("Malformed signal");
//                }
//                root.join(Path.parse(parameters[1]), parameters[2], Integer.parseInt(parameters[3]));
//                return;
//            default:
//                throw new Exception("Invalid signal");
//        }
    }

    private class SignalWorker implements Runnable {
        private final PrintWriter out;
        private final BufferedReader in;
        private final Socket client;

        public SignalWorker(Socket client) throws IOException {
            this.client = client;
            out = new PrintWriter(client.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        }

        @Override
        public void run() {
            try {
                String signal = in.readLine();

                System.out.println("> " + signal);
                handleSignal(signal);

                out.println(Peer.SIGNAL_ACK);
                System.out.println("< " + Peer.SIGNAL_ACK);

            } catch (Exception e) {
                out.println(Peer.SIGNAL_FAIL + " " + e.getMessage());
                System.out.println("< " + Peer.SIGNAL_FAIL + " " + e.getMessage());
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