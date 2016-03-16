package org.zells.node.model.react;

import org.zells.node.model.refer.Name;
import org.zells.node.model.refer.Parent;
import org.zells.node.model.refer.Path;

public class Delivery {

    private Path context;
    private Path target;
    private Path message;
    private Path role;

    public Delivery(Path context, Path target, Path message) {
        this(context, target, message, target.in(context));
    }

    public Delivery(Path context, Path target, Path message, Path role) {
        this.context = context;
        this.target = target;
        this.message = message;
        this.role = role;
    }

    public Path getContext() {
        return context;
    }

    public Path getTarget() {
        return target;
    }

    public Path getMessage() {
        return message;
    }

    public Path getRole() {
        return role;
    }

    public boolean hasArrived() {
        return target.isEmpty();
    }

    public Name nextTarget() {
        return target.first();
    }

    public Delivery toStem(Path stem) {
        return new Delivery(context.up(), stem.in(context.last()), message.in(context.last()), role);
    }

    public Delivery toChild() {
        return new Delivery(context.with(nextTarget()), target.rest(), message.in(Parent.name()), role);
    }

    public Delivery toParent() {
        return new Delivery(context.up(), target.in(context.last()), message.in(context.last()), role);
    }

    public Delivery toSelf() {
        return new Delivery(context, target.rest(), message, role);
    }
}
