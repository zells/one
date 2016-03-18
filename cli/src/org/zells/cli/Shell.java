package org.zells.cli;

import org.zells.node.Messenger;
import org.zells.node.Node;
import org.zells.node.io.ChiParser;
import org.zells.node.io.protocol.StandardProtocol;
import org.zells.node.io.server.SocketServer;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.react.Mailing;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Child;
import org.zells.node.model.refer.names.Root;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

public class Shell {

    public static final String PROMPT = "z$ ";
    private final Path self;
    private final Cell root;
    private final SocketServer server;

    public Shell(String name, String myHost, int myPort, String remoteHost, int remotePort) throws IOException {
        server = new SocketServer(new StandardProtocol(), myPort);

        Path myPath = new Path(Root.name(), Child.name(name));

        root = new Cell();
        root.join(server.makePeer(remoteHost, remotePort), myPath, myHost, myPort);
        root.createChild("out").setReaction(new PrintMessage(System.out));

        self = new Path(Root.name(), Child.name(name));
    }

    public static void main(String[] args) throws Exception {
        String name = new BigInteger(64, new SecureRandom()).toString(32);
        new Shell(name, "localhost", 4242, "localhost", 12345).run();
    }

    private void run() throws Exception {
        new Node(root, server).run();
        deliverInput();
    }

    private void deliverInput() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintStream out = System.out;

        out.println("Welcome to the Zells shell.");
        out.println();
        out.println("Enter '<targetPath>[ <messagePath>]' so send messages.");
        out.println("Enter '!!' to quit.");
        out.println();

        out.print(PROMPT);

        String input;
        while ((input = in.readLine()) != null) {

            if (input.equals("!!")) {
                out.println();
                out.println("Good bye =)");
                server.stopListening();
                System.exit(0);
            }

            final List<Mailing> mailings = new ChiParser().parse(input);

            if (mailings.size() != 1) {
                out.print(PROMPT);
                continue;
            }

            new Messenger()
                    .deliver(root, new Delivery(
                            self,
                            mailings.get(0).getTarget(),
                            mailings.get(0).getMessage()))
                    .whenFailed(new Runnable() {
                        @Override
                        public void run() {
                            System.err.println();
                            System.err.println("Delivery failed: " + mailings.get(0));
                            System.err.print(PROMPT);
                        }
                    });

            out.print(PROMPT);
        }
    }
}
