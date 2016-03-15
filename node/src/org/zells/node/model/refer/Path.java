package org.zells.node.model.refer;

import org.zells.node.io.PathParser;

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
        if (name.equals(Parent.name()) && !isEmpty()) {
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

    public Path rest() {
        if (names.size() == 1) {
            return new Path();
        }
        return new Path(names.subList(1, names.size()));
    }

    public Path up() {
        if (names.size() == 1 && names.get(0) == Root.name()) {
            return this;
        }
        return new Path(names.subList(0, names.size() - 1));
    }

    public Path in(Name parent) {
        return in(new Path(parent));
    }

    public Path in(Path context) {
        List<Name> newNames = new ArrayList<Name>(names.size() + 1);
        for (Name name : context.names) newNames.add(name);
        for (Name name : names) newNames.add(name);

        return new Path(newNames);
    }

    public boolean isEmpty() {
        return names.isEmpty();
    }

    public static Path parse(String string) {
        return PathParser.parse(string);
    }

    @Override
    public String toString() {
        return PathParser.serialize(names);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Path && ((Path) obj).names.equals(names);
    }

    @Override
    public int hashCode() {
        return names.hashCode();
    }
}
