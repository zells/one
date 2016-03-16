package org.zells.node;

import org.zells.node.io.Server;
import org.zells.node.io.SignalListener;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.Peer;
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
            return handleSignal(signal)
                    ? Protocol.ok()
                    : Protocol.fail(signal);
        } catch (Exception e) {
            e.printStackTrace(error);
            return Protocol.fail(e.getMessage());
        }
    }

    private boolean handleSignal(String signal) throws Exception {
        if (Protocol.isDeliver(signal)) {
            return handleDeliver(Protocol.parseDeliver(signal));
        } else if (Protocol.isJoin(signal)) {
            return handleJoin(Protocol.parseJoin(signal));
        }

        throw new Exception("Unknown signal");
    }

    private boolean handleDeliver(Object[] parameters) throws Exception {
        Path target = (Path) parameters[0];
        Path message = (Path) parameters[1];
        Path role = (Path) parameters[2];

        return new Messenger()
                .deliver(root, new Delivery(new Path(target.first()), target.rest(), message.rest(), role))
                .waitForIt()
                .hasDelivered();
    }

    private boolean handleJoin(Object[] parameters) throws Exception {
        Peer peer = server.makePeer((String) parameters[1], (Integer) parameters[2]);
        resolve((Path) parameters[0], root).joinedBy(peer);
        return true;
    }

    private Cell resolve(Path path, Cell cell) throws Exception {
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
        return cell;
    }
}