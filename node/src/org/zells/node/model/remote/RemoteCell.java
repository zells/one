package org.zells.node.model.remote;

import org.zells.node.model.Cell;
import org.zells.node.model.DeliveryFailed;
import org.zells.node.model.reference.Name;
import org.zells.node.model.reference.Path;

public class RemoteCell implements Cell {

    @Override
    public void deliver(Path context, Path target, Path message) throws DeliveryFailed {
        // Try to deliver to peer
    }
}
