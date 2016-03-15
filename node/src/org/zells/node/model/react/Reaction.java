package org.zells.node.model.react;

import org.zells.node.model.Cell;
import org.zells.node.model.refer.Path;

public interface Reaction {

    void execute(Cell cell, Path context, Path message);
}
