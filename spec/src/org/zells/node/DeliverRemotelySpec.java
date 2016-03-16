package org.zells.node;

import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Protocol;
import org.zells.node.model.react.Delivery;
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
        assertTrue(deliver(root, "context", "foo", "message"));
        assertEquals(protocolDeliver("context", "foo", "message"), peer.sent);
    }

    @Test
    public void executeWithPeer() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        FakePeer peer = new FakePeer();
        foo.joinedBy(peer);

        assertTrue(deliver(root, "context", "foo", "message"));
        assertEquals(protocolDeliver("context", "foo", "message"), peer.sent);
    }

    @Test
    public void deliverToPeer() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        FakePeer peer = new FakePeer();
        foo.joinedBy(peer);

        assertTrue(deliver(root, "context", "foo.bar", "message"));
        assertEquals(protocolDeliver("context", "foo.bar", "message"), peer.sent);
    }

    @Test
    public void executeWithNextPeer() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        FakePeer one = new FakePeer(Protocol.fail("foo"));
        FakePeer two = new FakePeer();

        foo.joinedBy(one);
        foo.joinedBy(two);

        assertTrue(deliver(root, "context", "foo", "message"));

        assertEquals(protocolDeliver("context", "foo", "message"), one.sent);
        assertEquals(protocolDeliver("context", "foo", "message"), two.sent);
    }

    @Test
    public void deliverToNextPeer() {
        Cell root = new Cell();
        Cell remote = root.createChild("foo");

        FakePeer one = new FakePeer(Protocol.fail("foo"));
        FakePeer two = new FakePeer();

        remote.joinedBy(one);
        remote.joinedBy(two);

        assertTrue(deliver(root, "context", "foo.bar", "message"));

        assertEquals(protocolDeliver("context", "foo.bar", "message"), one.sent);
        assertEquals(protocolDeliver("context", "foo.bar", "message"), two.sent);
    }

    @Test
    public void cannotDeliver() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        FakePeer peer = new FakePeer(Protocol.fail("foo"));
        foo.joinedBy(peer);

        assertFalse(deliver(root, "context", "foo.bar", "message"));
    }

    @Test
    public void searchInPeers() {
        FakePeer peer = new FakePeer();

        Cell root = new Cell();
        Cell foo = root.createChild("foo").joinedBy(peer);
        foo.createChild("bar");

        assertTrue(deliver(root, "*", "foo.baz", "message"));
        assertEquals(protocolDeliver("*", "foo.baz", "message"), peer.sent);
    }

    @Test
    public void searchChildInParent() {
        FakePeer peer = new FakePeer();

        Cell root = new Cell();
        Cell foo = root.createChild("foo").joinedBy(peer);
        Cell bar = foo.createChild("bar");
        Cell baz = bar.createChild("baz");

        assertTrue(deliver(baz, "*.foo.bar.baz", "bam", "message"));
        assertEquals(protocolDeliver("*", "foo.bar.baz.bam", "foo.bar.baz.message"), peer.sent);
    }

    @Test
    public void searchExecutionInParent() {
        FakePeer peer = new FakePeer();

        Cell root = new Cell();
        Cell foo = root.createChild("foo").joinedBy(peer);
        Cell bar = foo.createChild("bar");
        Cell baz = bar.createChild("baz");

        assertTrue(deliver(baz, "*.foo.bar.baz", "", "message"));
        assertEquals(protocolDeliver("*", "foo.bar.baz", "foo.bar.baz.message"), peer.sent);
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

    private String protocolDeliver(String context, String target, String message) {
        return Protocol.deliver(new Delivery(Path.parse(context), Path.parse(target), Path.parse(message)));
    }

    private boolean deliver(Cell cell, String context, String target, String message) {
        return cell.deliver(new Delivery(Path.parse(context), Path.parse(target), Path.parse(message)));
    }
}