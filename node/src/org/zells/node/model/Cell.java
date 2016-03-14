package org.zells.node.model;

import org.zells.node.model.local.LocalCell;
import org.zells.node.model.reference.*;

public abstract class Cell {

    private Cell parent;

    public Cell() {
    }

    public Cell(Cell parent) {
        this.parent = parent;
    }

    protected abstract void execute(Path context, Path message);

    protected abstract boolean deliverOn(Path context, Path target, Path message);

    protected abstract Name nameOf(Cell child);

    protected Path getPath() {
        if (parent == null) {
            return new Path(Root.name());
        }
        return parent.getPath().with(parent.nameOf(this));
    }

    public boolean deliver(Path context, Path target, Path message) {
        if (target.isEmpty()) {
            execute(context, message);
            return true;
        }

        Name name = target.first();

        if (name instanceof Parent && parent != null) {
            return parent.deliver(context.up(), target.rest(), message.in(context.last()));
        }

        if (name instanceof Root) {
            if (parent == null) {
                return deliver(context, new Path(), message);
            }

            return parent.deliver(context.up(), target, message.in(context.last()));
        }

        return deliverOn(context, target, message);
    }

    public abstract LocalCell resolve(Path path);
}
