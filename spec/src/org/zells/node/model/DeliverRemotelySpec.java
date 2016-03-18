package org.zells.node.model;

import org.junit.Test;
import org.zells.node.Specification;
import org.zells.node.model.connect.Signal;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DeliverRemotelySpec extends Specification {

    @Test
    public void joinPeers() {Cell root = new Cell();
        Cell foo = root.createChild("foo");

        SpecPeer peer = new SpecPeer();
        Cell bar = foo.createChild("bar");
        bar.join(peer, path("some.path"), "example.com", 1234);

        assertEquals(protocol.join(path("some.path"), "example.com", 1234), peer.sent);
    }

    @Test
    public void doNotJoinBack() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        SpecPeer peer = new SpecPeer();
        foo.joinedBy(peer);

        assertNull(peer.sent);
        assertEquals(path("context.foo"), deliver(root, "context", "foo", "message"));
        assertEquals(protocolDeliver("context.foo", "", "^.message"), peer.sent);
    }

    @Test
    public void executeWithPeer() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        SpecPeer peer = new SpecPeer();
        foo.joinedBy(peer);

        assertEquals(path("context.foo"), deliver(root, "context", "foo", "message"));
        assertEquals(protocolDeliver("context.foo", "", "^.message"), peer.sent);
    }

    @Test
    public void deliverToPeer() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        SpecPeer peer = new SpecPeer();
        foo.joinedBy(peer);

        assertEquals(path("context.foo.bar"), deliver(root, "context", "foo.bar", "message"));
        assertEquals(protocolDeliver("context.foo", "bar", "^.message"), peer.sent);
    }

    @Test
    public void executeWithNextPeer() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        SpecPeer one = new SpecPeer(protocol.fail("foo"));
        SpecPeer two = new SpecPeer();

        foo.joinedBy(one);
        foo.joinedBy(two);

        assertEquals(path("context.foo"), deliver(root, "context", "foo", "message"));

        assertEquals(protocolDeliver("context.foo", "", "^.message"), one.sent);
        assertEquals(protocolDeliver("context.foo", "", "^.message"), two.sent);
    }

    @Test
    public void deliverToNextPeer() {
        Cell root = new Cell();
        Cell remote = root.createChild("foo");

        SpecPeer one = new SpecPeer(protocol.fail("foo"));
        SpecPeer two = new SpecPeer();

        remote.joinedBy(one);
        remote.joinedBy(two);

        assertEquals(path("context.foo.bar"), deliver(root, "context", "foo.bar", "message"));

        assertEquals(protocolDeliver("context.foo", "bar", "^.message"), one.sent);
        assertEquals(protocolDeliver("context.foo", "bar", "^.message"), two.sent);
    }

    @Test
    public void cannotDeliver() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo");

        SpecPeer peer = new SpecPeer(protocol.fail("foo"));
        foo.joinedBy(peer);

        assertNull(deliver(root, "context", "foo.bar", "message"));
    }

    @Test
    public void searchInPeers() {
        SpecPeer peer = new SpecPeer();

        Cell root = new Cell();
        Cell foo = root.createChild("foo").joinedBy(peer);
        foo.createChild("bar");

        assertEquals(path("*.foo.baz"), deliver(root, "*", "foo.baz", "message"));
        assertEquals(protocolDeliver("*.foo", "baz", "^.message"), peer.sent);
    }

    @Test
    public void searchChildInParent() {
        SpecPeer peer = new SpecPeer();

        Cell root = new Cell();
        Cell foo = root.createChild("foo").joinedBy(peer);
        Cell bar = foo.createChild("bar");
        Cell baz = bar.createChild("baz");

        assertEquals(path("*.foo.bar.baz.bam"), deliver(baz, "*.foo.bar.baz", "bam", "message"));
        assertEquals(protocolDeliver("*.foo", "bar.baz.bam", "bar.baz.message"), peer.sent);
    }

    @Test
    public void searchExecutionInParent() {
        SpecPeer peer = new SpecPeer();

        Cell root = new Cell();
        Cell foo = root.createChild("foo").joinedBy(peer);
        Cell bar = foo.createChild("bar");
        Cell baz = bar.createChild("baz");

        assertEquals(path("*.foo.bar.baz"), deliver(baz, "*.foo.bar.baz", "", "message"));
        assertEquals(protocolDeliver("*.foo", "bar.baz", "bar.baz.message"), peer.sent);
    }

    private Signal protocolDeliver(String context, String target, String message) {
        return protocol.deliver(new Delivery(path(context), path(target), path(message)));
    }

    private Path deliver(Cell cell, String context, String target, String message) {
        return cell.deliver(new Delivery(path(context), path(target), path(message)));
    }
}