package org.zells.node.model.local;

import org.zells.node.model.Cell;
import org.zells.node.model.reference.*;

import java.util.HashMap;
import java.util.Map;

public class LocalCell extends Cell {

    private Response response;
    private Map<Name, Cell> children = new HashMap<Name, Cell>();

    public LocalCell() {
        super();
    }

    public LocalCell(LocalCell parent) {
        super(parent);
    }

    public LocalCell setResponse(Response response) {
        this.response = response;
        return this;
    }

    public void setChild(Child name, Cell child) {
        children.put(name, child);
    }

    @Override
    protected boolean deliverOn(Path context, Path target, Path message) {
        Name name = target.first();

        return children.containsKey(name)
                && children.get(name).deliver(context.with(name), target.rest(), message.in(Parent.name()));
    }

    @Override
    protected Name nameOf(Cell child) {
        for (Name name : children.keySet()) {
            if (children.get(name) == child) {
                return name;
            }
        }

        throw new RuntimeException("Not my child");
    }

    @Override
    public LocalCell resolve(Path path) {
        if (path.isEmpty()) {
            return this;
        }

        if (children.containsKey(path.first())) {
            return children.get(path.first()).resolve(path.rest());
        }

        throw new RuntimeException("Child not found");
    }

    @Override
    protected void execute(Path context, Path message) {
        if (response != null) {
            response.execute(this, context, message);
        }
    }
}
