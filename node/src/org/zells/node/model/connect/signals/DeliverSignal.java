package org.zells.node.model.connect.signals;

import org.zells.node.model.connect.Signal;
import org.zells.node.model.react.Delivery;

public abstract class DeliverSignal implements Signal {

    public abstract Delivery getDelivery();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DeliverSignal
                && getDelivery().equals(((DeliverSignal) obj).getDelivery());
    }

    @Override
    public int hashCode() {
        return getDelivery().hashCode();
    }
}
