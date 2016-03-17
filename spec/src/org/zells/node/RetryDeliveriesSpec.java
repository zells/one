package org.zells.node;

import org.junit.Before;
import org.junit.Test;
import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RetryDeliveriesSpec extends Specification {

    private Cell root;
    private List<Long> tries;
    private long last = 0;
    private boolean failed = false;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        tries = new ArrayList<Long>();
        root = new Cell();
    }

    @Test
    public void fail() throws InterruptedException {
        Messenger m = deliver(new Messenger()
                .setWaitFactorBase(0)
                .setWaitMs(10)
                .setTimeOutMs(50)
                .whenFailed(new Runnable() {
                    @Override
                    public void run() {
                        failed = true;
                    }
                }));

        assertTrue(m.isDelivering());
        m.waitForIt();

        assertFalse(m.hasDelivered());
        assertTrue(failed);
    }

    @Test
    public void succeed() throws InterruptedException {
        Messenger m = deliver(new Messenger()
                .setTimeOutMs(100)
                .setWaitMs(0));

        Thread.sleep(30);
        assertTrue(m.isDelivering());
        root.createChild("foo").setReaction(reaction);

        m.waitForIt();

        assertTrue(m.hasDelivered());
        assertFalse(m.isDelivering());
        assertEquals(path("c.foo"), reaction.executedWith.getRole());
    }

    @Test
    public void increaseWaitTime() {
        root = new CountingCell();
        deliver(new Messenger()
                .setTimeOutMs(100)
                .setWaitMs(2)
                .setWaitFactorBase(2))
                .waitForIt();

        assertTrue(tries.size() >= 4);
        assertTrue(tries.get(0) >= 4);
        assertTrue(tries.get(1) >= 8);
        assertTrue(tries.get(2) >= 16);
        assertTrue(tries.get(3) >= 32);
    }

    private Messenger deliver(Messenger m) {
        return m.deliver(root, new Delivery(path("c"), path("foo"), path("m")));
    }

    private class CountingCell extends Cell {
        @Override
        public boolean deliver(Delivery delivery) {
            if (last != 0) tries.add(System.currentTimeMillis() - last);
            last = System.currentTimeMillis();
            return false;
        }
    }
}
