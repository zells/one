package org.zells.node;

import org.zells.node.io.server.SocketServer;
import org.zells.node.io.protocol.StandardProtocol;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Root;
import org.zells.node.model.react.Reaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Launcher {

    public static void main(String[] args) throws IOException {
        Cell root = new Cell();
        root.createChild("echo").setReaction(new EchoMessage());

        int port = 12345;

        final SocketServer server = new SocketServer(new StandardProtocol(), port);

        System.out.println("Listening on " + port);
        new Node(root, server).run();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Server stopped");
                server.stopListening();
            }
        });

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Enter 'stop' to quit");
        System.out.print("> ");

        String input;
        while ((input = reader.readLine()) != null) {
            if (input.equals("stop")) {
                System.exit(0);
            }
            System.out.print("> ");
        }
    }

    private static class EchoMessage implements Reaction {
        @Override
        public void execute(Cell cell, Delivery delivery) {
            cell.deliver(new Delivery(delivery.getContext(), delivery.getMessage(), new Path(Root.name())));
        }
    }
}
