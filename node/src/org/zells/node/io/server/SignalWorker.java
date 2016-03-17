package org.zells.node.io.server;

import org.zells.node.model.connect.Protocol;
import org.zells.node.model.connect.Signal;
import org.zells.node.model.connect.SignalListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class SignalWorker implements Runnable {

    private final Protocol protocol;
    private final SignalListener listener;
    private final Socket client;

    private final PrintWriter out;
    private final BufferedReader in;

    public SignalWorker(Protocol protocol, SignalListener listener, Socket client) throws IOException {
        this.protocol = protocol;
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
