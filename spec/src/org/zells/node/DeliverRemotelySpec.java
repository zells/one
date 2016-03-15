package org.zells.node;

import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Protocol;
import org.zells.node.model.refer.Path;

import static org.junit.Assert.*;

public class DeliverRemotelySpec {

    @Test
    public void joinPeers() {Cell root = new Cell();
        Cell foo = root.createChild("foo");

        FakePeer peer = new FakePeer();
        Cell bar = foo.createChild("bar");
        bar.join(peer, Path.parse("some.path"), "example.com", 1234);

        assertEquals(Protocol.join(Path.parse("some.path"), "example.com", 1234), peer.sent);
    }

    @Test
    public void doNotJoinBack() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        FakePeer peer = new FakePeer();
        foo.joinedBy(peer);

        assertNull(peer.sent);
        assertTrue(root.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), peer.sent);
    }

    @Test
    public void executeWithPeer() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        FakePeer peer = new FakePeer();
        foo.joinedBy(peer);

        assertTrue(root.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), peer.sent);
    }

    @Test
    public void deliverToPeer() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        FakePeer peer = new FakePeer();
        foo.joinedBy(peer);

        assertTrue(root.deliver(Path.parse("context"), Path.parse("foo.bar"), Path.parse("message")));
        assertEquals(Protocol.deliver(Path.parse("context.foo"), Path.parse("bar"), Path.parse("^.message")), peer.sent);
    }

    @Test
    public void executeWithNextPeer() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        FakePeer one = new FakePeer(Protocol.fail("foo"));
        FakePeer two = new FakePeer();

        foo.joinedBy(one);
        foo.joinedBy(two);

        assertTrue(root.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));

        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), one.sent);
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), two.sent);
    }

    @Test
    public void deliverToNextPeer() {
        Cell root = new Cell();
        Cell remote = root.createChild("foo");

        FakePeer one = new FakePeer(Protocol.fail("foo"));
        FakePeer two = new FakePeer();

        remote.joinedBy(one);
        remote.joinedBy(two);

        assertTrue(root.deliver(Path.parse("context"), Path.parse("foo.bar"), Path.parse("message")));

        assertEquals(Protocol.deliver(Path.parse("context.foo"), Path.parse("bar"), Path.parse("^.message")), one.sent);
        assertEquals(Protocol.deliver(Path.parse("context.foo"), Path.parse("bar"), Path.parse("^.message")), two.sent);
    }

    @Test
    public void cannotDeliver() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        FakePeer peer = new FakePeer(Protocol.fail("foo"));
        foo.joinedBy(peer);

        assertFalse(root.deliver(Path.parse("context"), Path.parse("foo.bar"), Path.parse("message")));
    }

    private class FakePeer implements Peer {
        public String sent;
        private String response = Protocol.ok();

        public FakePeer() {
        }

        public FakePeer(String response) {
            this.response = response;
        }

        @Override
        public String send(String signal) {
            sent = signal;
            return response;
        }
    }
}