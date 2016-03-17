package org.zells.node;

import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Root;

import static org.junit.Assert.*;

public class InheritFromStemSpec extends Specification {

    @Test
    public void noStem() {
        assertFalse(deliver(new Cell(), ""));
    }

    @Test
    public void executeStem() {
        Cell root = new Cell();
        root.createChild("sub").setStem(path("*.stem"));
        Cell stem = root.createChild("stem").setReaction(reaction);

        assertTrue(deliver(root, "sub"));
        assertEquals(stem, reaction.executedBy);
        assertEquals(path("*.sub"), reaction.executedWith.getRole());
        assertEquals(path("^.message"), reaction.executedWith.getMessage());
    }

    @Test
    public void executeStemOfStem() {
        Cell root = new Cell();
        Cell stem = root.createChild("foo").setReaction(reaction);
        root.createChild("bar").setStem(path("*.foo"));
        root.createChild("baz").setStem(path("*.bar"));

        assertTrue(deliver(root, "baz"));
        assertEquals(stem, reaction.executedBy);
        assertEquals(path("*.baz"), reaction.executedWith.getRole());
        assertEquals(path("^.message"), reaction.executedWith.getMessage());
    }

    @Test
    public void nonExistingStem() {
        Cell root = new Cell();
        root.createChild("foo").setStem(path("*.not"));

        assertFalse(deliver(root, "foo"));
    }

    @Test
    public void remoteStem() {
        SpecPeer peer = new SpecPeer();

        Cell root = new Cell();
        root.createChild("stem").joinedBy(peer);
        root.createChild("sub").setStem(path("*.stem"));

        assertTrue(deliver(root, "sub"));
        assertEquals(protocol.deliver(new Delivery(path("*.stem"), path(""), path("^.message"), path("*.sub"))), peer.sent);
    }

    @Test
    public void stemCannotBeChild() {
        Cell root = new Cell();
        root.createChild("foo").setStem(path("child"));
        assertFalse(deliver(root, "foo"));
    }

    @Test
    public void stemCannotBeSelf() {
        Cell root = new Cell();
        root.createChild("foo").setStem(path("*.foo.bar"));
        assertFalse(deliver(root, "foo"));
    }

    private boolean deliver(Cell root, String target) {
        return root.deliver(new Delivery(new Path(Root.name()), path(target), path("message")));
    }
}
