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
    public void deliverToPeer() {
        LocalCell cell = new LocalCell();
        FakePeer peer = new FakePeer();
        cell.setChild(Child.name("foo"), new RemoteCell(cell, peer));

        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), peer.sent);
    }

    @Test
    public void nextPeer() {
        FakePeer one = new FakePeer(Protocol.fail("foo"));
        FakePeer two = new FakePeer();

        LocalCell cell = new LocalCell();
        cell.setChild(Child.name("foo"), new RemoteCell(cell, one).addPeer(two));

        assertTrue(cell.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));

        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), one.sent);
        assertEquals(Protocol.deliver(Path.parse("context.foo"), new Path(), Path.parse("^.message")), two.sent);
    }

    @Test
    public void cannotDeliver() {
        FakePeer peer = new FakePeer(Protocol.fail("foo"));

        LocalCell cell = new LocalCell();
        cell.setChild(Child.name("foo"), new RemoteCell(cell, peer));

        assertFalse(cell.deliver(Path.parse("context"), Path.parse("foo"), Path.parse("message")));
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