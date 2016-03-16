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
        Cell cell = new Cell();
        assertFalse(cell.deliver(new Delivery(new Path(), new Path(), new Path())));
    }

    @Test
    public void executeStem() {
        Cell root = new Cell();
        root.createChild("sub").setStem(Path.parse("*.stem"));
        Cell stem = root.createChild("stem").setReaction(reaction);

        assertTrue(root.deliver(new Delivery(Path.parse("*"), Path.parse("sub"), Path.parse("message"))));
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

        assertTrue(root.deliver(new Delivery(Path.parse("*"), Path.parse("baz"), Path.parse("message"))));
        assertEquals(stem, executedBy);
        assertEquals(Path.parse("*.baz"), executedAs);
        assertEquals(Path.parse("^.message"), executedWith);
    }

    @Test
    public void nonExistingStem() {
        Cell root = new Cell();
        root.createChild("foo").setStem(Path.parse("*.not"));

        assertFalse(root.deliver(new Delivery(Path.parse("*"), Path.parse("foo"), Path.parse("message"))));
    }

    @Test
    public void remoteStem() {
        Cell root = new Cell();
        root.createChild("stem").joinedBy(peer);
        root.createChild("sub").setStem(Path.parse("*.stem"));

        assertTrue(root.deliver(new Delivery(Path.parse("*"), Path.parse("sub"), Path.parse("message"))));
        assertEquals(Protocol.deliver(new Delivery(Path.parse("*"), Path.parse("stem"), Path.parse("message"), Path.parse("*.sub"))), sent);
    }
}
