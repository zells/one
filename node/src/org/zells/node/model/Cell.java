package org.zells.node.model;

import org.zells.node.model.reference.Path;

public interface Cell {

    void deliver(Path context, Path target, Path message) throws DeliveryFailed;
}
