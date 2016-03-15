package org.zells.node;

import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.Peer;
import org.zells.node.model.refer.Child;
import org.zells.node.model.refer.Path;
import org.zells.node.model.connect.Protocol;

import static org.junit.Assert.*;

public class DeliverRemotelySpec {

    @Test
    public void joinPeers() {Cell root = new Cell();
        Cell cell = new Cell(root);
        root.putChild(Child.name("foo"), cell);

        FakePeer peer = new FakePeer();
        Cell Cell = new Cell(cell);
        cell.putChild(Child.name("bar"), Cell);
        Cell.join(peer, Path.parse("some.path"), "example.com", 1234);

        assertEquals(Protocol.join(Path.parse("some.path"), "example.com", 1234), peer.sent);
    }

    @Test
    public void doNotJoinBack() {
        Cell cell = new Cell();

        FakePeer peer = new FakePeer();
        Cell remoteCell = new Cell(cell);
        cell.putChild(Child.name("foo"), remoteCell);
        remoteCell.joinedBy(peer);

        assertNull(peer.sent);
        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), peer.sent);
    }

    @Test
    public void executeWithPeer() {
        Cell cell = new Cell();
        FakePeer peer = new FakePeer();
        Cell remote = new Cell(cell);
        cell.putChild(Child.name("foo"), remote);
        remote.joinedBy(peer);

        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), peer.sent);
    }

    @Test
    public void deliverToPeer() {
        Cell cell = new Cell();
        FakePeer peer = new FakePeer();
        Cell remote = new Cell(cell);
        cell.putChild(Child.name("foo"), remote);
        remote.joinedBy(peer);

        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo.bar"), Path.parse("message")));
        assertEquals(Protocol.deliver(Path.parse("context.foo"), Path.parse("bar"), Path.parse("^.message")), peer.sent);
    }

    @Test
    public void executeWithNextPeer() {
        FakePeer one = new FakePeer(Protocol.fail("foo"));
        FakePeer two = new FakePeer();

        Cell cell = new Cell();
        Cell remote = new Cell(cell);
        cell.putChild(Child.name("foo"), remote);
        remote.joinedBy(one);
        remote.joinedBy(two);

        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));

        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), one.sent);
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), two.sent);
    }

    @Test
    public void deliverToNextPeer() {
        FakePeer one = new FakePeer(Protocol.fail("foo"));
        FakePeer two = new FakePeer();

        Cell cell = new Cell();
        Cell remote = new Cell(cell);
        cell.putChild(Child.name("foo"), remote);
        remote.joinedBy(one);
        remote.joinedBy(two);

        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo.bar"), Path.parse("message")));

        assertEquals(Protocol.deliver(Path.parse("context.foo"), Path.parse("bar"), Path.parse("^.message")), one.sent);
        assertEquals(Protocol.deliver(Path.parse("context.foo"), Path.parse("bar"), Path.parse("^.message")), two.sent);
    }

    @Test
    public void cannotDeliver() {
        FakePeer peer = new FakePeer(Protocol.fail("foo"));

        Cell cell = new Cell();
        Cell remote = new Cell(cell);
        cell.putChild(Child.name("foo"), remote);
        remote.joinedBy(peer);

        assertFalse(cell.deliver(Path.parse("context"), Path.parse("foo.bar"), Path.parse("message")));
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