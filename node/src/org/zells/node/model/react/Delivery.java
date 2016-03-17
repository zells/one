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
        return new Delivery(context.up(), stem.in(context.last()).with(target), message.in(context.last()), role);
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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Delivery
                && context.equals(((Delivery) obj).context)
                && target.equals(((Delivery)obj).target)
                && message.equals(((Delivery) obj).message)
                && role.equals(((Delivery) obj).role);
    }

    @Override
    public int hashCode() {
        return context.hashCode() + target.hashCode() + message.hashCode() + role.hashCode();
    }
}
