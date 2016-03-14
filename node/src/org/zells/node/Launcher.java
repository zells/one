package org.zells.node;

import org.zells.node.io.SocketServer;
import org.zells.node.model.Cell;
import org.zells.node.model.local.LocalCell;
import org.zells.node.model.local.Response;
import org.zells.node.model.reference.Child;
import org.zells.node.model.reference.Path;

import java.io.IOException;

public class Launcher {

    public static void main(String[] args) throws IOException {
        LocalCell root = new LocalCell();
        root.setChild(Child.name("foo"), new LocalCell().setResponse(new Response() {
            @Override
            public void execute(Cell cell, Path context, Path message) {
                System.out.println(context);
                System.out.println(message);
            }
        }));

        new Node(root, new SocketServer("localhost", 12345)).run();
    }
}
