package org.zells.nodeAlpha;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.security.SecureRandom;

public class Shell {

    private final String self;

    public Shell(String name, String host, int port, String myHost, int myPort) {
//        root = new LocalCell(null, new Peer(myHost, myPort));
//        root.addPeer(new Peer(host, port));
//        root.addChild(name, new ConsoleCell(root));
        self = name;
    }

    public static void main(String[] args) throws Exception {
        String name = new BigInteger(64, new SecureRandom()).toString(32);
        String host = "localhost";
        int port = 9999;

        if (args.length > 0) {
            name = args[0];
        }
        if (args.length > 1) {
            host = args[1];
        }
        if (args.length > 2) {
            port = Integer.parseInt(args[2]);
        }

        new Shell(name, host, port, "localhost", 9998).run();
    }

    private void run() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        PrintStream out = System.out;

        out.print("z$ ");

        String input;
        while ((input = in.readLine()) != null) {
            String[] targetMessage = input.split(" ");

            String target = targetMessage[0];
            if (!target.isEmpty()) {
                String message = self;
                if (targetMessage.length > 1) {
                    message = targetMessage[1];
                }

                try {
//                    root.deliver(new Path(new Root()), Path.parse(target), Path.parse(message));
                } catch (Exception e) {
                    out.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            out.print("z$ ");
        }
    }
}
