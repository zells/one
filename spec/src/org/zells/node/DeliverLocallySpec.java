package org.zells.node;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Reaction;
import org.zells.node.model.refer.Child;
import org.zells.node.model.refer.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DeliverLocallySpec {

    private TestReaction response;

    @Before
    public void setUp() {
        response = new TestReaction();
    }

    @Test
    public void noResponse() {
        new Cell().deliver(new Path(), new Path(), new Path());
    }

    @Test
    public void executeResponse() {
        Cell root = new Cell().setReaction(response);

        root.deliver(
                new Path(Child.name("foo")),
                new Path(),
                new Path(Child.name("bar")));

        assertEquals(root, response.cell);
        assertEquals(Path.parse("foo"), response.context);
        assertEquals(Path.parse("bar"), response.message);
    }

    @Test
    public void noChildren() {
        assertFalse(new Cell().deliver(
                new Path(),
                Path.parse("foo"),
                new Path()));
    }

    @Test
    public void wrongChild() {
        Cell root = new Cell();
        root.createChild("foo");

        assertFalse(root.deliver(
                new Path(),
                Path.parse("bar"),
                new Path()));
    }

    @Test
    public void deliverToChild() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo").setReaction(response);

        root.deliver(
                Path.parse("me"),
                Path.parse("foo"),
                Path.parse("message"));

        assertEquals(foo, response.cell);
        assertEquals(Path.parse("me.foo"), response.context);
        assertEquals(Path.parse("^.message"), response.message);
    }

    @Test
    public void replaceChild() {
        Cell root = new Cell();

        root.createChild("foo").setReaction(response);
        Cell replaced = root.createChild("foo").setReaction(response);

        root.deliver(
                Path.parse("me"),
                Path.parse("foo"),
                Path.parse("message"));

        assertEquals(replaced, response.cell);
        assertEquals(Path.parse("me.foo"), response.context);
        assertEquals(Path.parse("^.message"), response.message);
    }

    @Test
    public void deliverToParent() {
        Cell root = new Cell().setReaction(response);
        Cell foo = root.createChild("foo");

        foo.deliver(
                Path.parse("parent.child"),
                Path.parse("^"),
                Path.parse("message"));

        assertEquals(root, response.cell);
        assertEquals(Path.parse("parent"), response.context);
        assertEquals(Path.parse("child.message"), response.message);
    }

    @Test
    public void deliverToRoot() {
        Cell root = new Cell().setReaction(response);
        Cell foo = root.createChild("foo");
        Cell bar = foo.createChild("bar");

        bar.deliver(
                Path.parse("one.two.three"),
                Path.parse("*"),
                Path.parse("message"));

        assertEquals(root, response.cell);
        assertEquals(Path.parse("one"), response.context);
        assertEquals(Path.parse("two.three.message"), response.message);
    }

    private class TestReaction implements Reaction {
        public Path message;
        public Cell cell;
        public Path context;

        @Override
        public void execute(Cell cell, Path context, Path message) {
            this.cell = cell;
            this.context = context;
            this.message = message;
        }
    }
}