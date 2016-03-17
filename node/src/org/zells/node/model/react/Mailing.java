package org.zells.node.model.react;

import org.zells.node.model.refer.Path;

public class Mailing {

    private Path target;
    private Path message;

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
}
