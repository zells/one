package org.zells.lib;

import org.zells.node.model.Cell;
import org.zells.node.model.refer.Name;
import org.zells.node.model.refer.names.Child;

import java.text.NumberFormat;
import java.text.ParsePosition;

public class NumbersCell extends Cell {

    public NumbersCell(Cell parent) {
        super(parent);
    }

    @Override
    public boolean hasChild(Name name) {
        return name instanceof Child && isNumeric(name.toString());
    }

    @Override
    public Cell getChild(Name name) {
        return new NumberCell(this, Integer.parseInt(name.toString()));
    }

    public boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }
}
