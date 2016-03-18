package org.zells.lib;

import org.zells.node.model.Cell;

public class StringCell extends Cell {
    public StringCell(StringsCell parent, String string) {
        super(parent);

        createChild("print").setReaction(new PrintReaction(string));
    }
}
