package org.zells.node;

import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Child;

import static org.junit.Assert.*;

public class AdoptOnDeliverySpec extends Specification {

    @Test
    public void noStem() {
        Cell cell = new Cell();
        assertFalse(deliver(cell, "root", "foo", "m"));
    }

    @Test
    public void noReaction() {
        Cell root = new Cell();
        Cell stem = root.createChild("stem");
        stem.createChild("nope");
        Cell foo = root.createChild("foo").setStem(path("*.stem"));

        assertFalse(deliver(root, "root", "foo.nope", "m"));
        assertFalse(foo.hasChild(Child.name("nope")));
    }

    @Test
    public void noChildOfStem() {
        Cell root = new Cell();
        root.createChild("stem");
        Cell foo = root.createChild("foo").setStem(path("*.stem"));

        assertFalse(deliver(root, "foo.not"));
        assertFalse(foo.hasChild(Child.name("not")));
    }

    @Test
    public void adoptChildOfStem() {
        Cell root = new Cell();
        Cell stem = root.createChild("stem");
        stem.createChild("adopted").setReaction(reaction);
        Cell foo = root.createChild("foo").setStem(path("*.stem"));

        assertTrue(deliver(root, "foo.adopted"));
        assertTrue(foo.hasChild(Child.name("adopted")));
        assertEquals(path("*.stem.adopted"), foo.getChild(Child.name("adopted")).getStem());
    }

    @Test
    public void adoptGrandChildOfStem() {
        Cell root = new Cell();
        Cell stem = root.createChild("stem");
        Cell stemChild = stem.createChild("child");
        stemChild.createChild("adopted").setReaction(reaction);

        Cell foo = root.createChild("foo").setStem(path("*.stem"));

        assertTrue(deliver(root, "foo.child.adopted"));
        assertTrue(foo.hasChild(Child.name("child")));
        assertTrue(foo.getChild(Child.name("child")).hasChild(Child.name("adopted")));
        assertEquals(path("*.stem.child.adopted"), foo.getChild(Child.name("child")).getChild(Child.name("adopted")).getStem());
    }

    @Test
    public void adoptChildOfStemOfStem() {
        Cell root = new Cell();

        Cell two = root.createChild("two");
        two.createChild("adopted").setReaction(reaction);

        root.createChild("one").setStem(path("*.two"));

        Cell foo = root.createChild("foo").setStem(path("*.one"));

        assertTrue(deliver(root, "foo.adopted"));
        assertTrue(foo.hasChild(Child.name("adopted")));
    }

    private boolean deliver(Cell cell, String context, String target, String message) {
        return cell.deliver(new Delivery(path(context), path(target), path(message)));
    }

    private boolean deliver(Cell cell, String target) {
        return deliver(cell, "r", target, "m");
    }
}
