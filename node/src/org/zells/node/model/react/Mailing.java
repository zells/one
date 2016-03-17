package org.zells.node.model.react;

import org.zells.node.model.refer.Path;

public class Mailing {

    private final Path target;
    private final Path message;

    public Mailing(Path target, Path message) {
        this.target = target;
        this.message = message;
    }

    public Path getTarget() {
        return target;
    }

    public Path getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Mailing
                && target.equals(((Mailing) obj).target)
                && message.equals(((Mailing) obj).message);
    }

    @Override
    public int hashCode() {
        return target.hashCode() + message.hashCode();
    }
}
