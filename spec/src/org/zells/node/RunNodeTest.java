package org.zells.node;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.io.Server;
import org.zells.node.io.SignalListener;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Protocol;
import org.zells.node.model.refer.Path;
import org.zells.node.model.react.Reaction;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class RunNodeTest {

    private String sent;
    private final PrintStream error = new PrintStream(new ByteArrayOutputStream());
    private FakeServer server;
    private Cell root;

    @Before
    public void setUp() {
        root = new Cell();
        server = new FakeServer();
        new Node(root, server).setErrorStream(error).run();
    }

    @Test
    public void unknownSignal() {
        server.receive("foo");
        assertEquals(Protocol.fail("Unknown signal"), server.responded);
    }

    @Test
    public void malformedSignal() {
        server.receive("DELIVER foo");
        assertEquals(Protocol.fail("Malformed signal"), server.responded);
    }

    @Test
    public void cannotDeliver() {
        server.receive(Protocol.deliver(Path.parse("*.foo"), Path.parse("*.message")));
        assertEquals(Protocol.fail("Delivery failed"), server.responded);
    }

    @Test
    public void deliverMessage() {
        FakeReaction response = new FakeReaction();

        Cell cell = root.createChild("foo");
        Cell child = cell.createChild("bar").setReaction(response);

        server.receive(Protocol.deliver(Path.parse("root.foo.bar"), Path.parse("root.foo.message")));

        assertEquals(Protocol.ok(), server.responded);
        assertEquals(child, response.executed);
        assertEquals(Path.parse("root.foo.bar"), response.in);
    }

    @Test
    public void deliverToNonCanonicalPath() {
        FakeReaction response = new FakeReaction();

        Cell foo = root.createChild("foo");
        Cell bar = foo.createChild("bar").setReaction(response);

        server.receive(Protocol.deliver(Path.parse("root.*.foo.bar.^.^.foo.^.foo.*.foo.bar"), Path.parse("root.*.foo.^.foo.message")));

        assertEquals(Protocol.ok(), server.responded);
        assertEquals(bar, response.executed);
    }

    @Test
    public void letJoinNewCell() {
        server.receive(Protocol.join(Path.parse("root.foo.bar"), "other.host", 1234));
        assertEquals(Protocol.ok(), server.responded);

        root.deliver(Path.parse("root"), Path.parse("foo.bar.baz"), Path.parse("message"));
        assertEquals(Protocol.deliver(Path.parse("root.foo.bar.baz"), Path.parse("root.message")) +
                " -> other.host:1234", sent);
    }

    @Test
    public void nonCanonicalPath() {
        server.receive(Protocol.join(Path.parse("root.foo.^.bar"), "other.host", 1234));
        assertEquals(Protocol.fail("Malformed signal: path not canonical."), server.responded);
    }

    @Test
    public void letJoinExistingCell() {
        root.createChild("foo");

        server.receive(Protocol.join(Path.parse("root.foo"), "other.host", 1234));
        assertEquals(Protocol.ok(), server.responded);

        root.deliver(Path.parse("root"), Path.parse("foo.bar"), Path.parse("message"));
        assertEquals(Protocol.deliver(Path.parse("root.foo.bar"), Path.parse("root.message")) +
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

    private class FakeReaction implements Reaction {
        public Cell executed;
        public Path in;

        @Override
        public void execute(Cell cell, Path context, Path message) {
            executed = cell;
            in = context;
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
