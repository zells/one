package org.zells.node;

import org.zells.node.io.SocketServer;
import org.zells.node.model.Cell;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.Root;
import org.zells.node.model.react.Reaction;

import java.io.IOException;

public class Launcher {

    public static void main(String[] args) throws IOException {
        Cell root = new Cell();
        root.createChild("echo").setReaction(new EchoMessage());

        String host = "localhost";
        int port = 12345;

        System.out.println("Listening on " + port);
        new Node(root, new SocketServer(host, port)).run();
    }

    private static class EchoMessage implements Reaction {
        @Override
        public void execute(Cell cell, Path context, Path message) {
            cell.deliver(context, message, new Path(Root.name()));
        }
    }
}
