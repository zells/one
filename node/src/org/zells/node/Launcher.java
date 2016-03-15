package org.zells.node;

import org.zells.node.io.SocketServer;
import org.zells.node.model.Cell;

import java.io.IOException;

public class Launcher {

    public static void main(String[] args) throws IOException {
        Cell root = new Cell();
        root.createChild("foo");

        new Node(root, new SocketServer("localhost", 12345)).run();
    }
}
