package org.zells.node;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
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
        deliver(new Cell(), "", "", "");
    }

    @Test
    public void executeResponse() {
        Cell root = new Cell().setReaction(response);

        deliver(root, "foo", "", "bar");

        assertEquals(root, response.cell);
        assertEquals(Path.parse("foo"), response.context);
        assertEquals(Path.parse("bar"), response.message);
    }

    @Test
    public void noChildren() {
        assertFalse(deliver(new Cell(), "", "foo", ""));
    }

    @Test
    public void wrongChild() {
        Cell root = new Cell();
        root.createChild("foo");

        assertFalse(deliver(root, "", "bar", ""));
    }

    @Test
    public void deliverToChild() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo").setReaction(response);

        deliver(root, "me", "foo", "message");

        assertEquals(foo, response.cell);
        assertEquals(Path.parse("me.foo"), response.context);
        assertEquals(Path.parse("^.message"), response.message);
    }

    @Test
    public void replaceChild() {
        Cell root = new Cell();

        root.createChild("foo").setReaction(response);
        Cell replaced = root.createChild("foo").setReaction(response);

        deliver(root, "me", "foo", "message");

        assertEquals(replaced, response.cell);
        assertEquals(Path.parse("me.foo"), response.context);
        assertEquals(Path.parse("^.message"), response.message);
    }

    @Test
    public void deliverToParent() {
        Cell root = new Cell().setReaction(response);
        Cell foo = root.createChild("foo");

        deliver(foo, "parent.child", "^", "message");

        assertEquals(root, response.cell);
        assertEquals(Path.parse("parent"), response.context);
        assertEquals(Path.parse("child.message"), response.message);
    }

    @Test
    public void deliverToRoot() {
        Cell root = new Cell().setReaction(response);
        Cell foo = root.createChild("foo");
        Cell bar = foo.createChild("bar");

        deliver(bar, "one.two.three", "*", "message");

        assertEquals(root, response.cell);
        assertEquals(Path.parse("one"), response.context);
        assertEquals(Path.parse("two.three.message"), response.message);
    }

    private boolean deliver(Cell root, String context, String target, String message) {
        return root.deliver(new Delivery(
                Path.parse(context),
                Path.parse(target),
                Path.parse(message)));
    }

    private class TestReaction implements Reaction {
        public Path message;
        public Cell cell;
        public Path context;

        @Override
        public void execute(Cell cell, Delivery delivery) {
            this.cell = cell;
            this.context = delivery.getContext();
            this.message = delivery.getMessage();
        }
    }
}