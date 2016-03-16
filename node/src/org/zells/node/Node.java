package org.zells.node;

import org.zells.node.io.Server;
import org.zells.node.io.SignalListener;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.Protocol;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.*;

import java.io.PrintStream;

public class Node implements Runnable, SignalListener {

    private final Cell root;
    private Server server;
    private PrintStream error = System.err;

    public Node(Cell root, Server server) {
        this.root = root;
        this.server = server;
    }

    public Node setErrorStream(PrintStream stream) {
        error = stream;
        return this;
    }

    public void run() {
        server.listen(this);
    }

    @Override
    public String respondTo(String signal) {
        try {
            return handleSignal(signal);
        } catch (Exception e) {
            e.printStackTrace(error);
            return Protocol.fail(e.getMessage());
        }
    }

    private String handleSignal(String signal) throws Exception {
        if (Protocol.isDeliver(signal)) {
            return handleDeliver(Protocol.parseDeliver(signal));
        } else if (Protocol.isJoin(signal)) {
            return handleJoin(Protocol.parseJoin(signal));
        }

        throw new Exception("Unknown signal");
    }

    private String handleJoin(Object[] parameters) throws Exception {
        Path path = (Path) parameters[0];

        Cell cell = root;
        Path current = new Path(path.first());
        path = path.rest();
        while (!path.isEmpty()) {
            Name name = path.first();

            if (cell.hasChild(name)) {
                cell = cell.getChild(name);
            } else if (name instanceof Child) {
                cell = cell.createChild(name.toString());
            } else {
                throw new Exception("Malformed signal: path not canonical.");
            }

            current = current.with(name);
            path = path.rest();
        }

        cell.joinedBy(server.makePeer((String) parameters[1], (Integer) parameters[2]));
        return Protocol.ok();
    }

    private String handleDeliver(Object[] parameters) throws Exception {
        Path target = (Path) parameters[0];
        Path message = (Path) parameters[1];
        Path role = (Path) parameters[2];

        if (root.deliver(new Delivery(new Path(target.first()), target.rest(), message.rest(), role))) {
            return Protocol.ok();
        }

        throw new Exception("Delivery failed");
    }
}