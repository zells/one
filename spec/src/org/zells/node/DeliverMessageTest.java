package org.zells.node;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.DeliveryFailed;
import org.zells.node.model.Response;
import org.zells.node.model.local.LocalCell;
import org.zells.node.model.reference.*;

import static org.junit.Assert.*;

public class DeliverMessageTest {

    private TestResponse response;

    @Before
    public void setUp() {
        response = new TestResponse();
    }

    @Test
    public void noResponse() throws DeliveryFailed {
        new LocalCell().deliver(new Path(), new Path(), new Path());
    }

    @Test
    public void executeResponse() throws DeliveryFailed {
        LocalCell cell = new LocalCell().setResponse(response);

        cell.deliver(
                new Path(new Name("foo")),
                new Path(),
                new Path(new Name("bar")));

        assertEquals(cell, response.cell);
        assertEquals(new Path(new Name("foo")), response.context);
        assertEquals(new Path(new Name("bar")), response.message);
    }

    @Test
    public void noChildren() {
        try {
            new LocalCell().deliver(
                    new Path(),
                    new Path(new Name("foo")),
                    new Path());
            fail("No exception thrown");
        } catch (DeliveryFailed ignored) {
        }
    }

    @Test
    public void wrongChild() {
        LocalCell cell = new LocalCell();
        cell.setChild(new Name("foo"), new LocalCell(cell));

        try {
            cell.deliver(
                    new Path(),
                    new Path(new Name("bar")),
                    new Path());
            fail("No exception thrown");
        } catch (DeliveryFailed ignored) {
        }
    }

    @Test
    public void deliverToChild() throws DeliveryFailed {
        LocalCell cell = new LocalCell();
        LocalCell child = new LocalCell(cell).setResponse(response);

        cell.setChild(new Name("foo"), child);
        cell.deliver(
                new Path(new Name("me")),
                new Path(new Name("foo")),
                new Path(new Name("message")));

        assertEquals(child, response.cell);
        assertEquals(new Path(new Name("me"), new Name("foo")), response.context);
        assertEquals(new Path(Parent.name(), new Name("message")), response.message);
    }

    @Test
    public void replaceChild() throws DeliveryFailed {
        LocalCell cell = new LocalCell();
        LocalCell child = new LocalCell(cell).setResponse(response);
        LocalCell replaced = new LocalCell(cell).setResponse(response);

        cell.setChild(new Name("foo"), child);
        cell.setChild(new Name("foo"), replaced);

        cell.deliver(
                new Path(new Name("me")),
                new Path(new Name("foo")),
                new Path(new Name("message")));

        assertEquals(replaced, response.cell);
        assertEquals(new Path(new Name("me"), new Name("foo")), response.context);
        assertEquals(new Path(Parent.name(), new Name("message")), response.message);
    }

    @Test
    public void deliverToParent() throws DeliveryFailed {
        LocalCell cell = new LocalCell().setResponse(response);
        LocalCell child = new LocalCell(cell);

        cell.setChild(new Name("foo"), child);
        child.deliver(
                new Path(new Name("parent"), new Name("child")),
                new Path(Parent.name()),
                new Path(new Name("message")));

        assertEquals(cell, response.cell);
        assertEquals(new Path(new Name("parent")), response.context);
        assertEquals(new Path(new Name("child"), new Name("message")), response.message);
    }

    @Test
    public void deliverToRoot() throws DeliveryFailed {
        LocalCell root = new LocalCell().setResponse(response);
        LocalCell cell = new LocalCell(root);
        LocalCell child = new LocalCell(cell);

        root.setChild(new Name("foo"), cell);
        cell.setChild(new Name("bar"), child);

        child.deliver(
                new Path(new Name("one"), new Name("two"), new Name("three")),
                new Path(Root.name()),
                new Path(new Name("message")));

        assertEquals(root, response.cell);
        assertEquals(new Path(new Name("one")), response.context);
        assertEquals(new Path(new Name("two"), new Name("three"), new Name("message")), response.message);
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