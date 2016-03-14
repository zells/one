package org.zells.node;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.local.LocalCell;
import org.zells.node.model.local.Response;
import org.zells.node.model.reference.Child;
import org.zells.node.model.reference.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DeliverLocallyTest {

    private TestResponse response;

    @Before
    public void setUp() {
        response = new TestResponse();
    }

    @Test
    public void noResponse() {
        new LocalCell().deliver(new Path(), new Path(), new Path());
    }

    @Test
    public void executeResponse() {
        LocalCell cell = new LocalCell().setResponse(response);

        cell.deliver(
                new Path(Child.name("foo")),
                new Path(),
                new Path(Child.name("bar")));

        assertEquals(cell, response.cell);
        assertEquals(Path.parse("foo"), response.context);
        assertEquals(Path.parse("bar"), response.message);
    }

    @Test
    public void noChildren() {
        assertFalse(new LocalCell().deliver(
                new Path(),
                Path.parse("foo"),
                new Path()));
    }

    @Test
    public void wrongChild() {
        LocalCell cell = new LocalCell();
        cell.setChild(Child.name("foo"), new LocalCell(cell));

        assertFalse(cell.deliver(
                new Path(),
                Path.parse("bar"),
                new Path()));
    }

    @Test
    public void deliverToChild() {
        LocalCell cell = new LocalCell();
        LocalCell child = new LocalCell(cell).setResponse(response);

        cell.setChild(Child.name("foo"), child);
        cell.deliver(
                Path.parse("me"),
                Path.parse("foo"),
                Path.parse("message"));

        assertEquals(child, response.cell);
        assertEquals(Path.parse("me.foo"), response.context);
        assertEquals(Path.parse("^.message"), response.message);
    }

    @Test
    public void replaceChild() {
        LocalCell cell = new LocalCell();
        LocalCell child = new LocalCell(cell).setResponse(response);
        LocalCell replaced = new LocalCell(cell).setResponse(response);

        cell.setChild(Child.name("foo"), child);
        cell.setChild(Child.name("foo"), replaced);

        cell.deliver(
                Path.parse("me"),
                Path.parse("foo"),
                Path.parse("message"));

        assertEquals(replaced, response.cell);
        assertEquals(Path.parse("me.foo"), response.context);
        assertEquals(Path.parse("^.message"), response.message);
    }

    @Test
    public void deliverToParent() {
        LocalCell cell = new LocalCell().setResponse(response);
        LocalCell child = new LocalCell(cell);

        cell.setChild(Child.name("foo"), child);
        child.deliver(
                Path.parse("parent.child"),
                Path.parse("^"),
                Path.parse("message"));

        assertEquals(cell, response.cell);
        assertEquals(Path.parse("parent"), response.context);
        assertEquals(Path.parse("child.message"), response.message);
    }

    @Test
    public void deliverToRoot() {
        LocalCell root = new LocalCell().setResponse(response);
        LocalCell cell = new LocalCell(root);
        LocalCell child = new LocalCell(cell);

        root.setChild(Child.name("foo"), cell);
        cell.setChild(Child.name("bar"), child);

        child.deliver(
                Path.parse("one.two.three"),
                Path.parse("°"),
                Path.parse("message"));

        assertEquals(root, response.cell);
        assertEquals(Path.parse("one"), response.context);
        assertEquals(Path.parse("two.three.message"), response.message);
    }

    private class TestResponse implements Response {
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