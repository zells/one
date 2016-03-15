package org.zells.cli;

import org.zells.node.Node;
import org.zells.node.io.SocketPeer;
import org.zells.node.io.SocketServer;
import org.zells.node.model.Cell;
import org.zells.node.model.refer.Child;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.Root;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.SecureRandom;

public class Shell {

    public static final String PROMPT = "z$ ";
    private final String self;
    private final Cell root;
    private final String host;
    private final int port;

    public Shell(String name, String myHost, int myPort, String remoteHost, int remotePort) {
        host = myHost;
        port = myPort;
        Path myPath = new Path(Root.name(), Child.name(name));

        root = new Cell();
        root.join(new SocketPeer(remoteHost, remotePort), myPath, host, port);
        root.putChild(name, new PrintMessage(root, System.out));

        self = name;
    }

    public static void main(String[] args) throws Exception {
        String name = new BigInteger(64, new SecureRandom()).toString(32);
        new Shell(name, "localhost", 4242, "localhost", 12345).run();
    }

    private void run() throws Exception {
        new Thread(new Node(root, new SocketServer(host, port))).start();
        deliverInput();
    }

    private void deliverInput() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintStream out = System.out;

        out.print(PROMPT);

        String input;
        while ((input = in.readLine()) != null) {
            final Path[] targetMessage = parseInput(input);

            if (targetMessage.length == 2) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!root.deliver(new Path(Root.name()), targetMessage[0], targetMessage[1])) {
                            System.err.println();
                            System.err.println("Deliver failed: " + targetMessage[0] + " " + targetMessage[1]);
                            System.err.print(PROMPT);
                        }
                    }
                }).start();
            }
            out.print(PROMPT);
        }
    }

    private Path[] parseInput(String input) {
        String[] targetMessageStrings = input.split(" ");

        String target = targetMessageStrings[0];
        if (target.isEmpty()) {
            return new Path[0];
        }

        Path message = Path.parse(self);
        if (targetMessageStrings.length > 1) {
            message = message.with(Path.parse(targetMessageStrings[1]));
        }

        return new Path[]{
                Path.parse(target),
                message
        };
    }
}
