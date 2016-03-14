package org.zells.node.model;

import org.zells.node.model.reference.Path;

public interface Response {

    void execute(Cell cell, Path context, Path message);
}
