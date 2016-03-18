package org.zells.node.model.reflect;

import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.react.Reaction;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Child;

public class ReflectionCell extends Cell {

    private final Cell reflected;

    public ReflectionCell(Cell cell) {
        super(cell);
        this.reflected = cell;

        createChild("create").setReaction(new CreateChild());
        createChild("value").setReaction(new GetValue());
        createChild("stemFrom").setReaction(new ChangeStem());
    }

    private Path getValue(Cell cell, Delivery delivery, Path of) {
        return cell.deliver(new Delivery(
                delivery.getContext(),
                of.with(new Path(Child.name("z"), Child.name("value"))),
                new Path()));
    }

    private class CreateChild implements Reaction {
        @Override
        public Path execute(Cell cell, Delivery delivery) {
            Path value = getValue(cell, delivery, delivery.getMessage());

            if (value == null) {
                return null;
            }

            reflected.createChild(value.last().toString());
            return delivery.getContext();
        }
    }

    private class GetValue implements Reaction {
        @Override
        public Path execute(Cell cell, Delivery delivery) {
            if (reflected.getStem() == null) {
                return delivery.getContext().up().up();
            }
            return getValue(cell, delivery, reflected.getStem());
        }
    }

    private class ChangeStem implements Reaction {
        @Override
        public Path execute(Cell cell, Delivery delivery) {
            Path value = getValue(cell, delivery, delivery.getMessage());

            if (value == null) {
                return null;
            }

            reflected.setStem(value);
            return delivery.getContext();
        }
    }
}
