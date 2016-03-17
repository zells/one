package org.zells.node.model.connect.signals;

import org.zells.node.model.connect.Signal;

public abstract class FailSignal implements Signal {

    public abstract String getMessage();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FailSignal
                && getMessage().equals(((FailSignal) obj).getMessage());
    }

    @Override
    public int hashCode() {
        return getMessage().hashCode();
    }
}
