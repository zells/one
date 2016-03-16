package org.zells.node;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.connect.Peer;
import org.zells.node.model.connect.Protocol;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.react.Reaction;
import org.zells.node.model.refer.Path;

import static org.junit.Assert.*;

public class InheritFromStemSpec {

    private Cell executedBy;
    private Path executedAs;
    private Path executedWith;
    private Reaction reaction;
    private Peer peer;
    private String sent;

    @Before
    public void setUp() throws Exception {
        reaction = new Reaction() {
            @Override
            public void execute(Cell cell, Delivery delivery) {
                executedBy = cell;
                executedAs = delivery.getRole();
                executedWith = delivery.getMessage();
            }
        };

        peer = new Peer() {
            @Override
            public String send(String signal) {
                sent = signal;
                return Protocol.ok();
            }
        };
    }

    @Test
    public void noStem() {
        assertFalse(deliver(new Cell(), ""));
    }

    @Test
    public void executeStem() {
        Cell root = new Cell();
        root.createChild("sub").setStem(Path.parse("*.stem"));
        Cell stem = root.createChild("stem").setReaction(reaction);

        assertTrue(deliver(root, "sub"));
        assertEquals(stem, executedBy);
        assertEquals(Path.parse("*.sub"), executedAs);
        assertEquals(Path.parse("^.message"), executedWith);
    }

    @Test
    public void executeStemOfStem() {
        Cell root = new Cell();
        Cell stem = root.createChild("foo").setReaction(reaction);
        root.createChild("bar").setStem(Path.parse("*.foo"));
        root.createChild("baz").setStem(Path.parse("*.bar"));

        assertTrue(deliver(root, "baz"));
        assertEquals(stem, executedBy);
        assertEquals(Path.parse("*.baz"), executedAs);
        assertEquals(Path.parse("^.message"), executedWith);
    }

    @Test
    public void nonExistingStem() {
        Cell root = new Cell();
        root.createChild("foo").setStem(Path.parse("*.not"));

        assertFalse(deliver(root, "foo"));
    }

    @Test
    public void remoteStem() {
        Cell root = new Cell();
        root.createChild("stem").joinedBy(peer);
        root.createChild("sub").setStem(Path.parse("*.stem"));

        assertTrue(deliver(root, "sub"));
        assertEquals(Protocol.deliver(new Delivery(Path.parse("*"), Path.parse("stem"), Path.parse("message"), Path.parse("*.sub"))), sent);
    }

    @Test
    public void childCannotBeStem() {
        Cell root = new Cell();
        root.createChild("foo").setStem(Path.parse("child"));
        assertFalse(deliver(root, "foo"));
    }

    @Test
    public void childCannotBeSelf() {
        Cell root = new Cell();
        root.createChild("foo").setStem(Path.parse("*.foo.bar"));
        assertFalse(deliver(root, "foo"));
    }

    private boolean deliver(Cell root, String target) {
        return root.deliver(new Delivery(Path.parse("*"), Path.parse(target), Path.parse("message")));
    }
}
