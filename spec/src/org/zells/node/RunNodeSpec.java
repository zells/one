package org.zells.node;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.io.Server;
import org.zells.node.io.SignalListener;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Protocol;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.react.Reaction;
import org.zells.node.model.refer.Path;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class RunNodeSpec {

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
        server.receive(protocolDeliver("*", "foo", "message"));
        assertEquals(Protocol.fail("DELIVER *.foo *.message *.foo"), server.responded);
    }

    @Test
    public void deliverMessage() {
        FakeReaction response = new FakeReaction();

        Cell cell = root.createChild("foo");
        Cell child = cell.createChild("bar").setReaction(response);

        server.receive(protocolDeliver("root", "foo.bar", "foo.message"));

        assertEquals(Protocol.ok(), server.responded);
        assertEquals(child, response.executed);
    }

    @Test
    public void executeInRole() {
        FakeReaction response = new FakeReaction();

        Cell cell = root.createChild("foo");
        cell.createChild("bar").setReaction(response);

        server.receive(protocolDeliver("root", "foo.bar", "foo.message", "some.role"));

        assertEquals(Protocol.ok(), server.responded);
        assertEquals(Path.parse("some.role"), response.in);
    }

    @Test
    public void deliverToNonCanonicalPath() {
        FakeReaction response = new FakeReaction();

        Cell foo = root.createChild("foo");
        Cell bar = foo.createChild("bar").setReaction(response);

        server.receive(protocolDeliver("root.*.foo.bar.^.^.foo.^.foo.*.foo", "bar", "message"));

        assertEquals(Protocol.ok(), server.responded);
        assertEquals(bar, response.executed);
    }

    @Test
    public void letJoinNewCell() {
        server.receive(Protocol.join(Path.parse("root.foo.bar"), "other.host", 1234));
        assertEquals(Protocol.ok(), server.responded);

        deliver("root", "foo.bar.baz", "message");
        assertEquals(protocolDeliver("root", "foo.bar.baz", "message") +
                " -> other.host:1234", sent);
    }

    @Test
    public void letJoinExistingCell() {
        root.createChild("foo");

        server.receive(Protocol.join(Path.parse("root.foo"), "other.host", 1234));
        assertEquals(Protocol.ok(), server.responded);

        deliver("root", "foo.bar", "message");
        assertEquals(protocolDeliver("root", "foo.bar", "message") +
                " -> other.host:1234", sent);
    }

    private boolean deliver(String context, String target, String message) {
        return root.deliver(new Delivery(Path.parse(context), Path.parse(target), Path.parse(message)));
    }

    private String protocolDeliver(String context, String target, String message) {
        return Protocol.deliver(new Delivery(Path.parse(context), Path.parse(target), Path.parse(message)));
    }

    private String protocolDeliver(String context, String target, String message, String role) {
        return Protocol.deliver(new Delivery(Path.parse(context), Path.parse(target), Path.parse(message), Path.parse(role)));
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
        public void execute(Cell cell, Delivery delivery) {
            executed = cell;
            in = delivery.getRole();
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
