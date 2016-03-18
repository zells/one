package org.zells.node;

import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;

public class Messenger extends Thread {

    private long retries = 100;
    private long waitMs;
    private double waitFactorBase = 1;

    private Cell cell;
    private Delivery delivery;

    private Path receivedBy;
    private boolean isDelivering = false;
    private Runnable whenFailed;

    public Messenger setMaxRetries(long retries) {
        this.retries = retries;
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
        while (receivedBy == null) {
            receivedBy = cell.deliver(delivery);

            if (tryCount == retries) {
                break;
            }
            tryCount++;

            try {
                Thread.sleep(Math.round(waitMs * Math.pow(waitFactorBase, tryCount)));
            } catch (InterruptedException ignored) {
            }
        }

        if (receivedBy == null && whenFailed != null) {
            whenFailed.run();
        }
        isDelivering = false;
    }

    public boolean hasDelivered() {
        return receivedBy != null;
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

    public Path getReceiver() {
        return receivedBy;
    }
}
