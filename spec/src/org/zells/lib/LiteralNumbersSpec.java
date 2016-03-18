package org.zells.lib;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.Specification;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;

import static org.junit.Assert.assertEquals;

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
    public void print() {
        assertEquals(path("*.lib.literals.numbers.42.print"), deliver("lib.literals.numbers.42.print"));
        assertEquals(path("*.lib.literals.strings.42"), reaction.executedWith.getMessage());
    }

    private Path deliver(String target) {
        return root.deliver(new Delivery(
                path("*"),
                path(target),
                path("message")
        ));
    }
}
