package org.zells.lib;

import org.zells.node.model.Cell;
import org.zells.node.model.refer.Name;
import org.zells.node.model.refer.names.Child;

public class StringsCell extends Cell {

    public StringsCell(Cell parent) {
        super(parent);
    }

    @Override
    public boolean hasChild(Name name) {
        return name instanceof Child;

    }

    @Override
    public Cell getChild(Name name) {
        return new StringCell(this, name.toString());
    }
}
