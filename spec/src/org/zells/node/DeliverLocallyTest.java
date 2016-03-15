package org.zells.node;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.respond.Response;
import org.zells.node.model.refer.Child;
import org.zells.node.model.refer.Path;

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
        new Cell().deliver(new Path(), new Path(), new Path());
    }

    @Test
    public void executeResponse() {
        Cell cell = new Cell().setResponse(response);

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
        assertFalse(new Cell().deliver(
                new Path(),
                Path.parse("foo"),
                new Path()));
    }

    @Test
    public void wrongChild() {
        Cell cell = new Cell();
        cell.putChild(Child.name("foo"), new Cell(cell));

        assertFalse(cell.deliver(
                new Path(),
                Path.parse("bar"),
                new Path()));
    }

    @Test
    public void deliverToChild() {
        Cell cell = new Cell();
        Cell child = new Cell(cell).setResponse(response);

        cell.putChild(Child.name("foo"), child);
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
        Cell cell = new Cell();
        Cell child = new Cell(cell).setResponse(response);
        Cell replaced = new Cell(cell).setResponse(response);

        cell.putChild(Child.name("foo"), child);
        cell.putChild(Child.name("foo"), replaced);

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
        Cell cell = new Cell().setResponse(response);
        Cell child = new Cell(cell);

        cell.putChild(Child.name("foo"), child);
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
        Cell root = new Cell().setResponse(response);
        Cell cell = new Cell(root);
        Cell child = new Cell(cell);

        root.putChild(Child.name("foo"), cell);
        cell.putChild(Child.name("bar"), child);

        child.deliver(
                Path.parse("one.two.three"),
                Path.parse("*"),
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