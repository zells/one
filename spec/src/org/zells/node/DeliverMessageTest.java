package org.zells.node;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.DeliveryFailed;
import org.zells.node.model.Response;
import org.zells.node.model.local.LocalCell;
import org.zells.node.model.reference.Child;
import org.zells.node.model.reference.Parent;
import org.zells.node.model.reference.Path;
import org.zells.node.model.reference.Root;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
                new Path(Child.name("foo")),
                new Path(),
                new Path(Child.name("bar")));

        assertEquals(cell, response.cell);
        assertEquals(new Path(Child.name("foo")), response.context);
        assertEquals(new Path(Child.name("bar")), response.message);
    }

    @Test
    public void noChildren() {
        try {
            new LocalCell().deliver(
                    new Path(),
                    new Path(Child.name("foo")),
                    new Path());
            fail("No exception thrown");
        } catch (DeliveryFailed ignored) {
        }
    }

    @Test
    public void wrongChild() {
        LocalCell cell = new LocalCell();
        cell.setChild(Child.name("foo"), new LocalCell(cell));

        try {
            cell.deliver(
                    new Path(),
                    new Path(Child.name("bar")),
                    new Path());
            fail("No exception thrown");
        } catch (DeliveryFailed ignored) {
        }
    }

    @Test
    public void deliverToChild() throws DeliveryFailed {
        LocalCell cell = new LocalCell();
        LocalCell child = new LocalCell(cell).setResponse(response);

        cell.setChild(Child.name("foo"), child);
        cell.deliver(
                new Path(Child.name("me")),
                new Path(Child.name("foo")),
                new Path(Child.name("message")));

        assertEquals(child, response.cell);
        assertEquals(new Path(Child.name("me"), Child.name("foo")), response.context);
        assertEquals(new Path(Parent.name(), Child.name("message")), response.message);
    }

    @Test
    public void replaceChild() throws DeliveryFailed {
        LocalCell cell = new LocalCell();
        LocalCell child = new LocalCell(cell).setResponse(response);
        LocalCell replaced = new LocalCell(cell).setResponse(response);

        cell.setChild(Child.name("foo"), child);
        cell.setChild(Child.name("foo"), replaced);

        cell.deliver(
                new Path(Child.name("me")),
                new Path(Child.name("foo")),
                new Path(Child.name("message")));

        assertEquals(replaced, response.cell);
        assertEquals(new Path(Child.name("me"), Child.name("foo")), response.context);
        assertEquals(new Path(Parent.name(), Child.name("message")), response.message);
    }

    @Test
    public void deliverToParent() throws DeliveryFailed {
        LocalCell cell = new LocalCell().setResponse(response);
        LocalCell child = new LocalCell(cell);

        cell.setChild(Child.name("foo"), child);
        child.deliver(
                new Path(Child.name("parent"), Child.name("child")),
                new Path(Parent.name()),
                new Path(Child.name("message")));

        assertEquals(cell, response.cell);
        assertEquals(new Path(Child.name("parent")), response.context);
        assertEquals(new Path(Child.name("child"), Child.name("message")), response.message);
    }

    @Test
    public void deliverToRoot() throws DeliveryFailed {
        LocalCell root = new LocalCell().setResponse(response);
        LocalCell cell = new LocalCell(root);
        LocalCell child = new LocalCell(cell);

        root.setChild(Child.name("foo"), cell);
        cell.setChild(Child.name("bar"), child);

        child.deliver(
                new Path(Child.name("one"), Child.name("two"), Child.name("three")),
                new Path(Root.name()),
                new Path(Child.name("message")));

        assertEquals(root, response.cell);
        assertEquals(new Path(Child.name("one")), response.context);
        assertEquals(new Path(Child.name("two"), Child.name("three"), Child.name("message")), response.message);
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