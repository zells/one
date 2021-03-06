package org.zells.node.model.refer;

import org.zells.node.model.refer.names.Parent;
import org.zells.node.model.refer.names.Root;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Path {

    private final List<Name> names;

    public Path(List<Name> names) {
        this.names = names;
    }

    public Path() {
        names = new ArrayList<Name>();
    }

    public Path(Name... names) {
        this.names = Arrays.asList(names);
    }

    public Name first() {
        return names.get(0);
    }

    public Name last() {
        return names.get(names.size() - 1);
    }

    public Path with(Name name) {
        if (name.equals(Parent.name()) && !isEmpty() && last() != Parent.name()) {
            return up();
        }
        if (name.equals(Root.name())) {
            return new Path(name);
        }

        List<Name> newNames = new ArrayList<Name>(names.size() + 1);
        for (Name n : names) newNames.add(n);
        newNames.add(name);

        return new Path(newNames);
    }

    public Path with(Path path) {
        Path newPath = this;
        for (Name n : path.names) newPath = newPath.with(n);
        return newPath;
    }

    public Path rest() {
        if (names.size() == 1) {
            return new Path();
        }
        return new Path(names.subList(1, names.size()));
    }

    public Path up() {
        if (last() == Root.name()) {
            return this;
        }
        return new Path(names.subList(0, names.size() - 1));
    }

    public Path in(Name parent) {
        return in(new Path(parent));
    }

    public Path in(Path context) {
        return context.with(this);
    }

    public boolean isEmpty() {
        return names.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Path && ((Path) obj).names.equals(names);
    }

    @Override
    public int hashCode() {
        return names.hashCode();
    }

    public List<Name> getNames() {
        return names;
    }

    @Override
    public String toString() {
        return names.toString();
    }

    public boolean isIn(Path path) {
        for (int i = 0; i < path.names.size(); i++) {
            if (i == names.size() || !names.get(i).equals(path.names.get(i))) {
                return false;
            }
        }
        return true;
    }
}
