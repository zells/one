package org.zells.node;

import org.zells.node.io.Server;
import org.zells.node.io.SignalListener;
import org.zells.node.model.Cell;
import org.zells.node.model.local.LocalCell;
import org.zells.node.model.reference.Child;
import org.zells.node.model.reference.Path;
import org.zells.node.model.remote.Protocol;
import org.zells.node.model.remote.RemoteCell;

import java.io.PrintStream;

public class Node implements SignalListener {

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

    private String handleJoin(Object[] parameters) {
        Path path = (Path) parameters[0];

        LocalCell parent = root.resolve(path.rest().up());
        RemoteCell remote = new RemoteCell(parent, server.getHost(), server.getPort());

        parent.setChild((Child)path.last(), remote);
        remote.joinedBy(server.makePeer((String)parameters[1], (Integer)parameters[2]));

        return Protocol.ack();
    }

    private String handleDeliver(Object[] parameters) throws Exception {
        Path context = (Path) parameters[0];
        Path target = (Path) parameters[1];
        Path message = (Path) parameters[2];

        Cell cell = root;
        if (!context.isEmpty()) {
            cell = root.resolve(context.rest());
        }

        if (cell.deliver(context, target, message)) {
            return Protocol.ack();
        }

        throw new Exception("Delivery failed");
    }
}