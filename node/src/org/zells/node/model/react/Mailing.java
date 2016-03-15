package org.zells.node.model.react;

import org.zells.node.io.PathParser;
import org.zells.node.model.refer.Path;

import java.util.List;

public class Mailing {

    private final Path target;
    private final Path message;

    public Mailing(Path target, Path message) {
        this.target = target;
        this.message = message;
    }

    public static Mailing parse(String string) throws Exception {
        List<Path> paths = PathParser.parse(string);

        if (paths.isEmpty()) {
            throw new Exception("Empty input");
        }

        Path target = paths.get(0);
        Path message = new Path();
        if (paths.size() == 2) {
            message = paths.get(1);
        }

        return new Mailing(target, message);
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

    @Override
    public String toString() {
        return PathParser.serialize(target, message);
    }
}
