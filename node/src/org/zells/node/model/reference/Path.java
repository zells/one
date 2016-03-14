package org.zells.node.model.reference;

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

    public Path with(Name child) {
        List<Name> newNames = new ArrayList<Name>(names.size() + 1);
        for (Name name : names) newNames.add(name);
        newNames.add(child);

        return new Path(newNames);
    }

    public Path rest() {
        if (names.size() == 1) {
            return new Path();
        }
        return new Path(names.subList(1, names.size()));
    }

    public Path up() {
        return new Path(names.subList(0, names.size() - 1));
    }

    public Path in(Name parent) {
        List<Name> newNames = new ArrayList<Name>(names.size() + 1);
        newNames.add(parent);
        for (Name name : names) newNames.add(name);

        return new Path(newNames);
    }

    public boolean isEmpty() {
        return names.isEmpty();
    }

    public static Path parse(String string) {
        List<Name> names = new ArrayList<Name>();
        for (String s : Arrays.asList(string.split("/\\./"))) names.add(new Name(s));

        return new Path(names);
    }

    @Override
    public String toString() {
        if (names.isEmpty()) {
            return "";
        }

        StringBuilder path = new StringBuilder();
        for (Name name : names) {
            path.append(".").append(name.toString());
        }
        return path.substring(1);
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
