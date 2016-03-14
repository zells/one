package org.zells.node.model.remote;

import org.zells.node.model.reference.Path;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Peer {
    private final String host;
    private final int port;

    public static final String SIGNAL_DELIVER = "DELIVER";
    public static final String SIGNAL_JOIN = "JOIN";
    public static final String SIGNAL_LEAVE = "LEAVE";
    public static final String SIGNAL_ACK = "ACK";
    public static final String SIGNAL_FAIL = "FAIL";

    public Peer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public boolean deliver(Path context, Path target, Path message) throws Exception {
        Socket socket = new Socket(host, port);

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        System.out.println("< " + SIGNAL_DELIVER + " " + context + " " + target + " " + message);
        out.println(SIGNAL_DELIVER + " " + context + " " + target + " " + message);
        String response = in.readLine();

        out.close();
        in.close();
        socket.close();

        String[] signalContent = response.split(" ", 2);

        if (signalContent[0].equals(SIGNAL_ACK)) {
            return true;
        }

        if (signalContent.length > 1) {
            throw new Exception(signalContent[1]);
        }

        throw new Exception("Unknown error");
    }

    public void join(String name, Peer self) throws Exception {
        Socket socket = new Socket(host, port);

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.println(SIGNAL_JOIN + " " + name + " " + self.host + " " + self.port);
        String response = in.readLine();

        out.close();
        in.close();
        socket.close();

        String[] signalContent = response.split(" ", 2);
        if (signalContent[0].equals(SIGNAL_ACK)) {
            return;
        }

        if (signalContent.length > 1) {
            throw new Exception(signalContent[1]);
        }

        throw new Exception("Unknown error");
    }
}
