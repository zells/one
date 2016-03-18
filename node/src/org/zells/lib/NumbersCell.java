package org.zells.lib;

import org.zells.node.model.Cell;
import org.zells.node.model.refer.Name;

public class NumbersCell extends Cell {

    public NumbersCell(Cell parent) {
        super(parent);
    }

    @Override
    public boolean hasChild(Name name) {
        return true;
    }

    @Override
    public Cell getChild(Name name) {
        return new LiteralNumberCell(this, Integer.parseInt(name.toString()));
    }
}
