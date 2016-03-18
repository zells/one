package org.zells.node.model.connect.signals;

import org.zells.node.model.connect.Signal;
import org.zells.node.model.refer.Path;

public abstract class ReceivedSignal implements Signal {

    protected Path path;

    public ReceivedSignal(Path path) {
        this.path = path;
    }

    public Path getPath() {
        return path;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ReceivedSignal
                && path.equals(((ReceivedSignal) obj).path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
