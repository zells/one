package org.zells.node;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.*;
import org.zells.node.model.react.Delivery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RunNodeSpec extends Specification {

    private final PrintStream error = new PrintStream(new ByteArrayOutputStream());
    private SpecServer server;
    private Cell root;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        root = new Cell();
        server = new SpecServer();
        new Node(root, server)
                .setErrorStream(error)
                .setMessenger(new Messenger()
                        .setTimeOutMs(1))
                .run();
    }

    @Test
    public void unknownSignal() {
        try {
            protocol.parse("foo");
        } catch (IOException e) {
            assertEquals("Signal not recognized", e.getMessage());
            return;
        }
        fail("No exception thrown");
    }

    @Test
    public void malformedSignal() {
        try {
            protocol.parse("DELIVER foo");
        } catch (IOException e) {
            assertEquals("Malformed signal", e.getMessage());
            return;
        }
        fail("No exception thrown");
    }

    @Test
    public void cannotDeliver() {
        server.receive(protocolDeliver("*", "foo", "message"));
        assertEquals(protocol.fail("DELIVER *.foo *.message *.foo"), server.responded);
    }

    @Test
    public void deliverMessage() {
        Cell cell = root.createChild("foo");
        Cell child = cell.createChild("bar").setReaction(reaction);

        server.receive(protocolDeliver("root", "foo.bar", "foo.message"));

        assertEquals(protocol.ok(), server.responded);
        assertEquals(child, reaction.executedBy);
    }

    @Test
    public void executeInRole() {
        Cell cell = root.createChild("foo");
        cell.createChild("bar").setReaction(reaction);

        server.receive(protocolDeliver("root", "foo.bar", "foo.message", "some.role"));

        assertEquals(protocol.ok(), server.responded);
        assertEquals(path("some.role"), reaction.executedWith.getRole());
    }

    @Test
    public void deliverToNonCanonicalPath() {
        Cell foo = root.createChild("foo");
        Cell bar = foo.createChild("bar").setReaction(reaction);

        server.receive(protocolDeliver("root", "*.foo.bar.^.^.foo.^.foo.*.foo.bar", "message"));

        assertEquals(protocol.ok(), server.responded);
        assertEquals(bar, reaction.executedBy);
    }

    @Test
    public void letJoinNewCell() {
        server.receive(protocol.join(path("root.foo.bar"), "other.host", 1234));
        assertEquals(protocol.ok(), server.responded);

        deliver("root", "foo.bar.baz", "message");
        assertEquals(protocolDeliver("root.foo.bar", "baz", "^.^.message"), server.peer.sent);
        assertEquals("other.host:1234", server.peer.sentTo);
    }

    @Test
    public void letJoinExistingCell() {
        root.createChild("foo");

        server.receive(protocol.join(path("root.foo"), "other.host", 1234));
        assertEquals(protocol.ok(), server.responded);

        deliver("root", "foo.bar", "message");
        assertEquals(protocolDeliver("root.foo", "bar", "^.message"), server.peer.sent);
        assertEquals("other.host:1234", server.peer.sentTo);
    }

    private boolean deliver(String context, String target, String message) {
        return root.deliver(new Delivery(path(context), path(target), path(message)));
    }

    private Signal protocolDeliver(String context, String target, String message) {
        return protocol.deliver(new Delivery(path(context), path(target), path(message)));
    }

    private Signal protocolDeliver(String context, String target, String message, String role) {
        return protocol.deliver(new Delivery(path(context), path(target), path(message), path(role)));
    }

    private class SpecServer implements Server {

        public SpecPeer peer;
        public Signal responded;
        private SignalListener listener;

        public void receive(Signal signal) {
            responded = listener.respondTo(signal);
        }

        @Override
        public void listen(SignalListener listener) {
            this.listener = listener;
        }

        @Override
        public void stopListening() {
        }

        @Override
        public Peer makePeer(String host, Integer port) {
            peer = new SpecPeer(host, port);
            return peer;
        }

        @Override
        public Protocol getProtocol() {
            return protocol;
        }
    }
}
