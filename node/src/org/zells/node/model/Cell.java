package org.zells.node.model;

import org.zells.node.model.reference.Path;

public interface Cell {

    boolean deliver(Path context, Path target, Path message);
}
