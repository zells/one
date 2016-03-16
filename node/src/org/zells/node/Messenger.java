package org.zells.node;

import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;

public class Messenger extends Thread {

    private long timeOutMs = 1000;
    private long waitMs;
    private double waitFactorBase = 1;

    private Cell cell;
    private Delivery delivery;

    private boolean delivered = false;
    private boolean isDelivering = false;
    private Runnable whenFailed;

    public Messenger setTimeOutMs(long timeOutMs) {
        this.timeOutMs = timeOutMs;
        return this;
    }

    public Messenger setWaitMs(long waitMs) {
        this.waitMs = waitMs;
        return this;
    }

    public Messenger setWaitFactorBase(double exponent) {
        this.waitFactorBase = exponent;
        return this;
    }

    public Messenger deliver(Cell cell, Delivery delivery) {
        this.isDelivering = true;
        this.cell = cell;
        this.delivery = delivery;

        start();
        return this;
    }

    @Override
    public void run() {
        int tryCount = 0;
        long start = System.currentTimeMillis();
        while (!delivered && System.currentTimeMillis() - start < timeOutMs) {
            tryCount++;
            delivered = cell.deliver(delivery);

            try {
                Thread.sleep(Math.round(waitMs * Math.pow(waitFactorBase, tryCount)));
            } catch (InterruptedException ignored) {
            }
        }

        isDelivering = false;
        if (!delivered && whenFailed != null) {
            whenFailed.run();
        }
    }

    public boolean hasDelivered() {
        return delivered;
    }

    public boolean isDelivering() {
        return isDelivering;
    }

    public Messenger waitForIt() {
        while (isDelivering) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {
            }
        }
        return this;
    }

    public Messenger whenFailed(Runnable runnable) {
        whenFailed = runnable;
        return this;
    }
}
