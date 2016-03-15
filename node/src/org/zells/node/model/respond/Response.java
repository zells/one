package org.zells.node.model.respond;

import org.zells.node.model.Cell;
import org.zells.node.model.refer.Path;

public interface Response {

    void execute(Cell cell, Path context, Path message);
}
