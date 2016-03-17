package org.zells.node.io.protocol;

import org.zells.node.model.refer.*;
import org.zells.node.model.refer.names.Child;
import org.zells.node.model.refer.names.Parent;
import org.zells.node.model.refer.names.Root;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

class StandardParser {
    public static final char ESCAPE = '+';

    public static String serializePath(Path path) throws IOException {
        StringBuilder serialized = new StringBuilder();
        for (Name n : path.getNames()) {
            serialized.append(".");
            if (n instanceof Root) {
                serialized.append("*");
            } else if (n instanceof Parent) {
                serialized.append("^");
            } else if (n instanceof Child) {
                serialized.append(escape(((Child) n).getName()));
            } else {
                throw new IOException("Could not serialize path");
            }
        }

        return serialized.toString().substring(1);
    }

    public static Path parsePath(String string) {
        Path path = new Path();
        for (String s : string.split("\\.")) {
            if (s.equals("*")) {
                path = path.with(Root.name());
            } else if (s.equals("^")) {
                path = path.with(Parent.name());
            } else {
                path = path.with(Child.name(unEscape(s)));
            }
        }

        return path;
    }

    private static String escape(String string) {
        List<Character> specialCharacters = Arrays.asList('^', '*', ':', '_', ESCAPE);
        StringBuilder escaped = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (specialCharacters.contains(c)) {
                escaped.append(ESCAPE);
            } else if (c == ' ') {
                c = '_';
            }  else if (c == '.') {
                c = ':';
            }
            escaped.append(c);
        }
        return escaped.toString();
    }

    private static String unEscape(String string) {
        boolean escaped = false;
        StringBuilder unescaped = new StringBuilder();
        for (char c : string.toCharArray()) {
            if (!escaped && c == ESCAPE) {
                escaped = true;
            } else {
                if (!escaped && c == ':') {
                    c = '.';
                } else if (!escaped && c == '_') {
                    c = ' ';
                }

                unescaped.append(c);
                escaped = false;
            }
        }
        return unescaped.toString();
    }
}
