package org.zells.node.model.connect.signals;

import org.zells.node.model.connect.Signal;
import org.zells.node.model.refer.Path;

public abstract class JoinSignal implements Signal {

    public abstract Path getPath();

    public abstract String getHost();

    public abstract int getPort();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof JoinSignal
                && getPath().equals(((JoinSignal) obj).getPath())
                && getHost().equals(((JoinSignal) obj).getHost())
                && getPort() == ((JoinSignal) obj).getPort();
    }

    @Override
    public int hashCode() {
        return getPath().hashCode() + getHost().hashCode() + getPort();
    }
}
