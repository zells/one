package org.zells.node.model;

import org.junit.Test;
import org.zells.node.Specification;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DeliverLocallySpec extends Specification {

    @Test
    public void noResponse() {
        deliver(new Cell(), "", "", "");
    }

    @Test
    public void executeResponse() {
        Cell root = new Cell().setReaction(reaction);

        assertEquals(path("foo"), deliver(root, "foo", "", "bar"));

        assertEquals(root, reaction.executedBy);
        assertEquals(path("foo"), reaction.executedWith.getContext());
        assertEquals(path("bar"), reaction.executedWith.getMessage());
    }

    @Test
    public void noChildren() {
        assertNull(deliver(new Cell(), "", "foo", ""));
    }

    @Test
    public void wrongChild() {
        Cell root = new Cell();
        root.createChild("foo");

        assertNull(deliver(root, "", "bar", ""));
    }

    @Test
    public void deliverToChild() {
        Cell root = new Cell();
        Cell foo = root.createChild("foo").setReaction(reaction);

        assertEquals(path("me.foo"), deliver(root, "me", "foo", "message"));

        assertEquals(foo, reaction.executedBy);
        assertEquals(path("me.foo"), reaction.executedWith.getContext());
        assertEquals(path("^.message"), reaction.executedWith.getMessage());
    }

    @Test
    public void replaceChild() {
        Cell root = new Cell();

        root.createChild("foo").setReaction(reaction);
        Cell replaced = root.createChild("foo").setReaction(reaction);

        deliver(root, "me", "foo", "message");

        assertEquals(replaced, reaction.executedBy);
        assertEquals(path("me.foo"), reaction.executedWith.getContext());
        assertEquals(path("^.message"), reaction.executedWith.getMessage());
    }

    @Test
    public void deliverToParent() {
        Cell root = new Cell().setReaction(reaction);
        Cell foo = root.createChild("foo");

        assertEquals(path("parent"), deliver(foo, "parent.child", "^", "message"));

        assertEquals(root, reaction.executedBy);
        assertEquals(path("parent"), reaction.executedWith.getContext());
        assertEquals(path("child.message"), reaction.executedWith.getMessage());
    }

    @Test
    public void deliverToRoot() {
        Cell root = new Cell().setReaction(reaction);
        Cell foo = root.createChild("foo");
        Cell bar = foo.createChild("bar");

        assertEquals(path("one"), deliver(bar, "one.two.three", "*", "message"));

        assertEquals(root, reaction.executedBy);
        assertEquals(path("one"), reaction.executedWith.getContext());
        assertEquals(path("two.three.message"), reaction.executedWith.getMessage());
    }

    private Path deliver(Cell root, String context, String target, String message) {
        return root.deliver(new Delivery(
                path(context),
                path(target),
                path(message)));
    }
}