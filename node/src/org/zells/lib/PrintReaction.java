package org.zells.lib;

import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.react.Reaction;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Child;
import org.zells.node.model.refer.names.Parent;

class PrintReaction implements Reaction {

    private String string;

    public PrintReaction(String string) {
        this.string = string;
    }

    @Override
    public Path execute(Cell cell, Delivery delivery) {
        Path me = delivery.getContext().with(new Path(
                Parent.name(),
                Parent.name(),
                Parent.name(),
                Child.name("strings"),
                Child.name(string)));

        cell.deliver(new Delivery(delivery.getContext(), delivery.getMessage(), me));
        return delivery.getContext();
    }
}
