package org.zells.lib;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.Specification;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Child;

import static org.junit.Assert.*;

public class LiteralNumbersSpec extends Specification {

    private Cell root;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        root = new Cell();

        root.createChild("message").setReaction(reaction);
        root.putChild("lib", new StandardLibraryCell(root));
    }

    @Test
    public void value() {
        assertEquals(path("*.lib.literals.numbers.42"), deliver("lib.literals.numbers.42.z.value"));
    }

    @Test
    public void notANumber() {
        assertNull(deliver("lib.literals.numbers.foo.z.value"));
    }

    @Test
    public void print() {
        assertEquals(path("*.lib.literals.numbers.42.print"), deliver("lib.literals.numbers.42.print"));
        assertEquals(path("*.lib.literals.strings.42"), reaction.executedWith.getMessage());
    }

    @Test
    public void addNonExistingCell() {
        assertNull(deliver("lib.literals.numbers.21.plus", "*.foo"));
    }

    @Test
    public void add() {
        Cell foo = root.createChild("foo").setStem(path("*.lib.literals.numbers.21"));
        assertNotNull(deliver("lib.literals.numbers.21.plus", "*.foo"));

        assertTrue(foo.hasChild(Child.name("result")));
        assertEquals(path("*.lib.literals.numbers.42"), foo.getChild(Child.name("result")).getStem());
    }

    private Path deliver(String target) {
        return deliver(target, "message");
    }

    private Path deliver(String target, String message) {
        return root.deliver(new Delivery(
                path("*"),
                path(target),
                path(message)
        ));
    }
}
