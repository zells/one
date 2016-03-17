package org.zells.node.model.connect.signals;

import org.zells.node.model.connect.Signal;

public abstract class OkSignal implements Signal {

    @Override
    public boolean equals(Object obj) {
        return obj instanceof OkSignal;
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
