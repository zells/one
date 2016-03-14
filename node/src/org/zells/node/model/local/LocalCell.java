package org.zells.node.model.local;

import org.zells.node.model.Cell;
import org.zells.node.model.DeliveryFailed;
import org.zells.node.model.Response;
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
    public void deliver(Path context, Path target, Path message) throws DeliveryFailed {
        if (target.isEmpty()) {
            if (response != null) {
                response.execute(this, context, message);
            }
            return;
        }

        Name name = target.first();

        if (name instanceof Parent && parent != null) {
            parent.deliver(context.up(), target.rest(), message.in(context.last()));
            return;
        }

        if (name instanceof Root) {
            if (parent == null) {
                if (response != null) {
                    response.execute(this, context, message);
                }
                return;
            }

            parent.deliver(context.up(), target, message.in(context.last()));
            return;
        }

        if (children.containsKey(name)) {
            children.get(name).deliver(context.with(name), target.rest(), message.in(Parent.name()));
            return;
        }

        throw new DeliveryFailed();
    }

    public void setChild(Child name, Cell child) {
        children.put(name, child);
    }
}
