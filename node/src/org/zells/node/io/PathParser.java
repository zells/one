package org.zells.node.io;

import org.zells.node.model.refer.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathParser {

    public static Path parseOne(String string) {
        List<Path> paths = parse(string);
        if (paths.isEmpty()) {
            return new Path();
        }
        return paths.get(0);
    }

    public static List<Path> parse(String string) {
        List<Path> list = new ArrayList<Path>();
        if (string.isEmpty()) {
            return list;
        }

        Path path = new Path();
        StringBuilder name = new StringBuilder();

        String quoted = null;
        boolean escaped = false;

        string = string.trim() + " ";
        for (String c : Arrays.asList(string.split("(?!^)"))) {
            if (quoted == null && !escaped && (c.equals(".") || c.equals(" "))) {
                if (name.length() != 0) {
                    if (name.toString().equals(Parent.name().toString())) {
                        path = path.with(Parent.name());
                    } else if (name.toString().equals(Root.name().toString())) {
                        path = path.with(Root.name());
                    } else {
                        path = path.with(Child.name(name.toString()));
                    }
                }
                name = new StringBuilder();

                if (c.equals(" ") && !path.isEmpty()) {
                    list.add(path);
                    path = new Path();
                }
            } else if (!escaped && c.equals("\\")) {
                escaped = true;
            } else if (!escaped && (c.equals("'") || c.equals("\""))) {
                if (c.equals(quoted)) {
                    quoted = null;
                } else if (quoted == null) {
                    quoted = c;
                } else {
                    name.append(c);
                }
            } else {
                name.append(c);
                escaped = false;
            }
        }

        return list;
    }

    public static String serialize(Path... paths) {
        if (paths.length == 0) {
            return "";
        }

        StringBuilder out = new StringBuilder();
        for (Path path: paths) {
            if (path.isEmpty()) {
                continue;
            }

            out.append(" ");
            out.append(path.toString());
        }

        return out.substring(1);
    }

    public static String serialize(List<Name> names) {
        if (names.isEmpty()) {
            return "";
        }

        StringBuilder path = new StringBuilder();
        for (Name name : names) {
            path.append(".");
            String string = name.toString();

            if (string.contains(".") || string.contains(" ") || string.contains("'") || string.contains("\"")) {
                path.append("'");
                path.append(string
                        .replace("\\", "\\\\")
                        .replace("'", "\\'"));
                path.append("'");
            } else {
                path.append(string);
            }
        }
        return path.substring(1);
    }
}
