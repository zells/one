package org.zells.node;

import org.junit.Test;
import org.zells.node.io.Server;
import org.zells.node.io.SignalListener;
import org.zells.node.model.Cell;
import org.zells.node.model.local.LocalCell;
import org.zells.node.model.local.Peer;
import org.zells.node.model.local.Response;
import org.zells.node.model.reference.Child;
import org.zells.node.model.reference.Path;
import org.zells.node.model.remote.Protocol;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class RunNodeTest {

    private String sent;

    @Test
    public void unknownSignal() {
        FakeServer server = new FakeServer();

        new Node(new LocalCell(), server)
                .setErrorStream(new PrintStream(new ByteArrayOutputStream()))
                .run();
        server.receive("foo");

        assertEquals(Protocol.fail("Unknown signal"), server.responded);
    }

    @Test
    public void malformedSignal() {
        FakeServer server = new FakeServer();

        new Node(new LocalCell(), server)
                .setErrorStream(new PrintStream(new ByteArrayOutputStream()))
                .run();
        server.receive("DELIVER foo");

        assertEquals(Protocol.fail("Malformed signal"), server.responded);
    }

    @Test
    public void cannotDeliver() {
        FakeServer server = new FakeServer();
        LocalCell root = new LocalCell();

        new Node(root, server)
                .setErrorStream(new PrintStream(new ByteArrayOutputStream()))
                .run();
        server.receive(Protocol.deliver(new Path(), Path.parse("foo"), Path.parse("message")));

        assertEquals(Protocol.fail("Delivery failed"), server.responded);
    }

    @Test
    public void deliverMessageWithoutContext() {
        FakeServer server = new FakeServer();
        FakeResponse response = new FakeResponse();
        LocalCell root = new LocalCell();
        Cell cell = new LocalCell(root).setResponse(response);
        root.setChild(Child.name("foo"), cell);

        new Node(root, server).run();
        server.receive(Protocol.deliver(new Path(), Path.parse("foo"), Path.parse("message")));

        assertEquals(Protocol.ok(), server.responded);
        assertEquals(cell, response.executed);
    }

    @Test
    public void deliverMessageWithContext() {
        FakeServer server = new FakeServer();
        FakeResponse response = new FakeResponse();

        LocalCell root = new LocalCell();
        LocalCell cell = new LocalCell(root);
        root.setChild(Child.name("foo"), cell);
        LocalCell child = new LocalCell(cell).setResponse(response);
        cell.setChild(Child.name("bar"), child);

        new Node(root, server).run();
        server.receive(Protocol.deliver(Path.parse("root.foo"), Path.parse("bar"), Path.parse("message")));

        assertEquals(Protocol.ok(), server.responded);
        assertEquals(child, response.executed);
    }

    @Test
    public void letPeersJoin() {
        FakeServer server = new FakeServer();

        LocalCell root = new LocalCell();
        LocalCell cell = new LocalCell(root);
        root.setChild(Child.name("foo"), cell);

        new Node(root, server).run();
        server.receive(Protocol.join(Path.parse("root.foo.bar"), "other.host", 1234));

        assertEquals(Protocol.ok(), server.responded);

        cell.deliver(Path.parse("root.foo"), Path.parse("bar.baz"), Path.parse("message"));
        assertEquals(Protocol.deliver(Path.parse("root.foo.bar"), Path.parse("baz"), Path.parse("^.message")) +
                " -> other.host:1234", sent);
    }

    private class FakeServer implements Server {
        public String responded;
        private SignalListener listener;

        public void receive(String signal) {
            responded = listener.respondTo(signal);
        }

        @Override
        public void listen(SignalListener listener) {
            this.listener = listener;
        }

        @Override
        public Peer makePeer(String host, Integer port) {
            return new FakePeer(host, port);
        }

        @Override
        public String getHost() {
            return "other.host";
        }

        @Override
        public int getPort() {
            return 42;
        }
    }

    private class FakeResponse implements Response {
        public Cell executed;

        @Override
        public void execute(Cell cell, Path context, Path message) {
            executed = cell;
        }
    }

    private class FakePeer implements Peer {
        private final String host;
        private final int port;

        public FakePeer(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public String send(String signal) {
            sent = signal + " -> " + host + ":" + port;
            return Protocol.ok();
        }
    }
}
