package org.zells.node;

import org.zells.lib.StandardLibraryCell;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.*;
import org.zells.node.model.connect.signals.DeliverSignal;
import org.zells.node.model.connect.signals.JoinSignal;
import org.zells.node.model.refer.names.Child;
import org.zells.node.model.refer.Name;
import org.zells.node.model.refer.Path;

import java.io.PrintStream;

public class Node implements SignalListener {

    private final Cell root;
    private final Server server;

    private PrintStream error = System.err;

    public Node(Cell root, Server server) {
        this.root = root;
        root.putChild("zells", new StandardLibraryCell(root));
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
    public Signal respondTo(Signal signal) {
        try {
            return handleSignal(signal);
        } catch (Exception e) {
            e.printStackTrace(error);
            return server.getProtocol().fail(e.getMessage());
        }
    }

    private Signal handleSignal(Signal signal) throws Exception {
        if (signal instanceof DeliverSignal) {
            return handleDeliver((DeliverSignal) signal);
        } else if (signal instanceof JoinSignal) {
            return handleJoin((JoinSignal) signal);
        }

        throw new Exception("Unknown signal");
    }

    private Signal handleDeliver(DeliverSignal signal) throws Exception {
        Messenger messenger = new Messenger()
                .deliver(root, signal.getDelivery())
                .waitForIt();

        if (messenger.hasDelivered()) {
            return server.getProtocol().received(messenger.getReceiver());
        }

        return server.getProtocol().fail("Delivery failed");
    }

    private Signal handleJoin(JoinSignal signal) throws Exception {
        Peer peer = server.makePeer(signal.getHost(), signal.getPort());
        resolve(signal.getPath(), root).joinedBy(peer);
        return server.getProtocol().ok();
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