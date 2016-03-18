package org.zells.lib;

import org.zells.node.model.Cell;

public class StandardLibraryCell extends Cell {

    public StandardLibraryCell(Cell root) {
        super(root);

        Cell literals = createChild("literals");
        literals.putChild("numbers", new NumbersCell(literals));
    }
}
