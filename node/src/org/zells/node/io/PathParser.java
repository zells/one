package org.zells.node.io;

import org.zells.node.model.reference.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PathParser {

    public static Path parse(String string) {
        if (string.isEmpty()) {
            return new Path();
        }

        List<Name> names = new ArrayList<Name>();
        StringBuilder currentName = new StringBuilder();
        String quoted = null;
        boolean escaped = false;

        string += ".";
        for (String c : Arrays.asList(string.split("(?!^)"))) {
            if (quoted == null && c.equals(".")) {
                if (currentName.length() != 0) {
                    if (currentName.toString().equals(Parent.name().toString())) {
                        names.add(Parent.name());
                    } else if (currentName.toString().equals(Root.name().toString())) {
                        names.add(Root.name());
                    } else {
                        names.add(Child.name(currentName.toString()));
                    }
                }
                currentName = new StringBuilder();
            } else if (!escaped && c.equals("\\")) {
                escaped = true;
            } else if (!escaped && (c.equals("'") || c.equals("\""))) {
                if (c.equals(quoted)) {
                    quoted = null;
                } else {
                    quoted = c;
                }
            } else {
                currentName.append(c);
                escaped = false;
            }
        }

        return new Path(names);
    }

    public static String serialize(List<Name> names) {
        if (names.isEmpty()) {
            return "";
        }

        StringBuilder path = new StringBuilder();
        for (Name name : names) {
            path.append(".");

            String string = name.toString();
            if (string.contains(".")) {
                string = string.replace("\\", "\\\\");
                string = string.replace("'", "\\'");
                path.append("'").append(string).append("'");
            } else {
                path.append(string);
            }
        }
        return path.substring(1);
    }
}
