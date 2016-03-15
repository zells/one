package org.zells.node;

import org.zells.node.io.SocketServer;
import org.zells.node.model.Cell;
import org.zells.node.model.refer.Child;
import org.zells.node.model.refer.Path;
import org.zells.node.model.respond.Response;

import java.io.IOException;

public class Launcher {

    public static void main(String[] args) throws IOException {
        Cell root = new Cell();
        root.putChild(Child.name("foo"), new Cell().setResponse(new Response() {
            @Override
            public void execute(Cell cell, Path context, Path message) {
                System.out.println(context);
                System.out.println(message);
            }
        }));

        new Node(root, new SocketServer("localhost", 12345)).run();
    }
}
