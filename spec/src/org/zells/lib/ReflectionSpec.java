package org.zells.lib;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.Specification;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Child;

import static org.junit.Assert.*;

public class ReflectionSpec extends Specification {

    private Cell root;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        root = new Cell();
    }

    @Test
    public void createCell() {
        Cell foo = root.createChild("foo");
        root.createChild("bar");

        assertNotNull(deliver("foo.z.create", "*.bar"));
        assertTrue(foo.hasChild(Child.name("bar")));
    }

    @Test
    public void createWithNonExistingCell() {
        root.createChild("foo");
        assertNull(deliver("foo.z.create", "*.name"));
    }

    @Test
    public void createWithSubString() {
        Cell foo = root.createChild("foo");
        root.createChild("stem").setStem(path("*.bar"));
        root.createChild("name").setStem(path("*.stem"));
        root.createChild("bar");

        assertNotNull(deliver("foo.z.create", "name"));
        assertTrue(foo.hasChild(Child.name("bar")));
    }

    @Test
    public void changeStem() {
        Cell foo = root.createChild("foo");
        root.createChild("bar");

        assertNotNull(deliver("foo.z.stemFrom", "bar"));
        assertEquals(path("*.bar"), foo.getStem());
    }

    private Path deliver(String target, String message) {
        return root.deliver(new Delivery(path("*"), path(target), path(message)));
    }
}
