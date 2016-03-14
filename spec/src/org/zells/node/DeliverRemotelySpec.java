package org.zells.node;

import org.junit.Test;
import org.zells.node.model.local.LocalCell;
import org.zells.node.model.local.Peer;
import org.zells.node.model.reference.Child;
import org.zells.node.model.reference.Path;
import org.zells.node.model.remote.Protocol;
import org.zells.node.model.remote.RemoteCell;

import static org.junit.Assert.*;

public class DeliverRemotelySpec {

    @Test
    public void joinPeers() {
        LocalCell root = new LocalCell();
        LocalCell cell = new LocalCell(root);
        root.setChild(Child.name("foo"), cell);

        FakePeer peer = new FakePeer();
        RemoteCell remoteCell = new RemoteCell(cell, "example.com", 1234);
        cell.setChild(Child.name("bar"), remoteCell);
        remoteCell.join(peer);

        assertEquals(Protocol.join(Path.parse("°.foo.bar"), "example.com", 1234), peer.sent);
    }

    @Test
    public void doNotJoinBack() {
        LocalCell cell = new LocalCell();

        FakePeer peer = new FakePeer();
        RemoteCell remoteCell = new RemoteCell(cell, "example.com", 1234);
        cell.setChild(Child.name("foo"), remoteCell);
        remoteCell.joinedBy(peer);

        assertNull(peer.sent);
        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), peer.sent);
    }

    @Test
    public void executeWithPeer() {
        LocalCell cell = new LocalCell();
        FakePeer peer = new FakePeer();
        RemoteCell remote = new RemoteCell(cell, "example.com", 12345);
        cell.setChild(Child.name("foo"), remote);
        remote.join(peer);

        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), peer.sent);
    }

    @Test
    public void deliverToPeer() {
        LocalCell cell = new LocalCell();
        FakePeer peer = new FakePeer();
        RemoteCell remote = new RemoteCell(cell, "example.com", 12345);
        cell.setChild(Child.name("foo"), remote);
        remote.join(peer);

        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo.bar"), Path.parse("message")));
        assertEquals(Protocol.deliver(Path.parse("context.foo"), Path.parse("bar"), Path.parse("^.message")), peer.sent);
    }

    @Test
    public void executeWithNextPeer() {
        FakePeer one = new FakePeer(Protocol.fail("foo"));
        FakePeer two = new FakePeer();

        LocalCell cell = new LocalCell();
        RemoteCell remote = new RemoteCell(cell, "example.com", 12345);
        cell.setChild(Child.name("foo"), remote);
        remote.join(one).join(two);

        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));

        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), one.sent);
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), two.sent);
    }

    @Test
    public void deliverToNextPeer() {
        FakePeer one = new FakePeer(Protocol.fail("foo"));
        FakePeer two = new FakePeer();

        LocalCell cell = new LocalCell();
        RemoteCell remote = new RemoteCell(cell, "example.com", 12345);
        cell.setChild(Child.name("foo"), remote);
        remote.join(one).join(two);

        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo.bar"), Path.parse("message")));

        assertEquals(Protocol.deliver(Path.parse("context.foo"), Path.parse("bar"), Path.parse("^.message")), one.sent);
        assertEquals(Protocol.deliver(Path.parse("context.foo"), Path.parse("bar"), Path.parse("^.message")), two.sent);
    }

    @Test
    public void cannotDeliver() {
        FakePeer peer = new FakePeer(Protocol.fail("foo"));

        LocalCell cell = new LocalCell();
        RemoteCell remote = new RemoteCell(cell, "example.com", 12345);
        cell.setChild(Child.name("foo"), remote);
        remote.join(peer);

        assertFalse(cell.deliver(Path.parse("context"), Path.parse("foo.bar"), Path.parse("message")));
    }

    private class FakePeer implements Peer {
        public String sent;
        private String response = Protocol.ack();

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