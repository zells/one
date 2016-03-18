package org.zells.lib;

import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.react.Reaction;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Child;
import org.zells.node.model.refer.names.Parent;

public class NumberCell extends Cell {

    private final int integer;

    public NumberCell(Cell parent, final int integer) {
        super(parent);
        this.integer = integer;

        createChild("print").setReaction(new PrintReaction(String.valueOf(integer)));
        createChild("plus").setReaction(new AddNumber());
    }

    private class AddNumber implements Reaction {
        @Override
        public Path execute(Cell cell, Delivery delivery) {
            Path value = cell.deliver(new Delivery(
                    delivery.getContext(),
                    delivery.getMessage().with(new Path(Child.name("z"), Child.name("value"))),
                    new Path()
            ));

            if (value == null) {
                return null;
            }

            String result = String.valueOf(integer + Integer.parseInt(value.last().toString()));

            cell.deliver(new Delivery(
                    delivery.getContext(),
                    delivery.getMessage().with(new Path(Child.name("z"), Child.name("create"))),
                    new Path(Parent.name(), Parent.name(), Parent.name(), Child.name("strings"), Child.name("result"))));

            cell.deliver(new Delivery(
                    delivery.getContext(),
                    delivery.getMessage().with(new Path(Child.name("result"), Child.name("z"), Child.name("stemFrom"))),
                    new Path(Parent.name(), Parent.name(), Parent.name(), Child.name("numbers"), Child.name(result))));

            return delivery.getContext();
        }
    }
}
