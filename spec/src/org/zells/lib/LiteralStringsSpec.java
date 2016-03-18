package org.zells.lib;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.Specification;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LiteralStringsSpec extends Specification {

    private Cell root;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        root = new Cell();

        root.createChild("message").setReaction(reaction);
        root.putChild("lib", new StandardLibraryCell(root));
    }

    @Test
    public void returnValue() {
        assertEquals(path("*.lib.literals.strings.foo"), deliver("lib.literals.strings.foo.z.value", "m"));
    }

    @Test
    public void print() {
        root.createChild("bar").setReaction(reaction);

        assertNotNull(deliver("lib.literals.strings.foo.print", "bar"));
        assertEquals(path("*.lib.literals.strings.foo"), reaction.executedWith.getMessage());
    }

    private Path deliver(String target, String message) {
        return root.deliver(new Delivery(
                path("*"),
                path(target),
                path(message)
        ));
    }
}
