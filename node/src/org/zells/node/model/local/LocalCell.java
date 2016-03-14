package org.zells.node.model.local;

import org.zells.node.model.Cell;
import org.zells.node.model.reference.*;

import java.util.HashMap;
import java.util.Map;

public class LocalCell implements Cell {

    private LocalCell parent;
    private Response response;
    private Map<Name, Cell> children = new HashMap<Name, Cell>();

    public LocalCell() {
    }

    public LocalCell(LocalCell parent) {
        this.parent = parent;
    }

    public LocalCell setResponse(Response response) {
        this.response = response;
        return this;
    }

    @Override
    public boolean deliver(Path context, Path target, Path message) {
        if (target.isEmpty()) {
            if (response != null) {
                response.execute(this, context, message);
            }
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

        return children.containsKey(name)
                && children.get(name).deliver(context.with(name), target.rest(), message.in(Parent.name()));

    }

    public void setChild(Child name, Cell child) {
        children.put(name, child);
    }
}
