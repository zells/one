package org.zells.lib;

import org.zells.node.model.Cell;

public class LiteralNumberCell extends Cell {

    public LiteralNumberCell(Cell parent, final int integer) {
        super(parent);

        createChild("print").setReaction(new PrintReaction(String.valueOf(integer)));
    }
}
