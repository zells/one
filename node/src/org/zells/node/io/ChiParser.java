package org.zells.node.io;

import org.zells.node.model.react.Mailing;
import org.zells.node.model.refer.Name;
import org.zells.node.model.refer.names.*;
import org.zells.node.model.refer.Path;

import java.util.*;

public class ChiParser {

    public static final char QUOTE = '"';
    public static final char SEPARATOR = '.';

    private static Map<Character, Name> names;
    static {
        names = new HashMap<Character, Name>();
        names.put('*', Root.name());
        names.put('^', Parent.name());
        names.put('@', Message.name());
        names.put('~', Execution.name());
    }

    private static Map<Character, Path> shortcuts;
    static {
        shortcuts = new HashMap<Character, Path>();
        shortcuts.put('$', new Path(Root.name(), Child.name("zells"), Child.name("literals"), Child.name("strings")));
        shortcuts.put('#', new Path(Root.name(), Child.name("zells"), Child.name("literals"), Child.name("numbers")));
    }

    private List<Character> whitespaces = Arrays.asList(' ', '\t');

    private final List<Mailing> mailings = new ArrayList<Mailing>();
    private List<Path> paths = new ArrayList<Path>();
    private Path path = new Path();
    private StringBuilder name = new StringBuilder();

    private boolean quoted = false;
    private boolean wasQuoted = false;

    public List<Mailing> parse(String input) {
        input = input + "\n";
        String[] lines = input.split("\n");

        for (String line : lines) {
            line = line.trim() + " ";

            for (char c : line.toCharArray()) {
                if (!quoted && c == QUOTE) {
                    quoted = true;
                    wasQuoted = true;

                } else if (quoted && c == QUOTE) {
                    quoted = false;

                } else if (!quoted && name.toString().isEmpty() && names.containsKey(c)) {
                    name.append(c);
                    endName();

                } else if (!quoted && name.toString().isEmpty() && path.isEmpty() && shortcuts.containsKey(c)) {
                    path = shortcuts.get(c);

                } else if (!quoted && (whitespaces.contains(c) || c == SEPARATOR)) {
                    endName();

                    if (whitespaces.contains(c)) {
                        endPath();
                    }

                    if (paths.size() == 2) {
                        break;
                    }
                } else {
                    name.append(c);
                }
            }

            if (paths.size() == 1) {
                mailings.add(new Mailing(paths.get(0), new Path()));
            } else if (paths.size() == 2) {
                mailings.add(new Mailing(paths.get(0), paths.get(1)));
            }
            paths = new ArrayList<Path>();
        }

        return mailings;
    }

    private void endPath() {
        if (!path.isEmpty()) {
            paths.add(path);
        }
        path = new Path();
    }

    private void endName() {
        String nameString = name.toString();
        if (!nameString.isEmpty()) {
            if (!wasQuoted && nameString.length() == 1 && names.containsKey(nameString.charAt(0))) {
                path = path.with(names.get(nameString.charAt(0)));
            } else {
                path = path.with(Child.name(nameString));
            }

            name = new StringBuilder();
            wasQuoted = false;
        }
    }
}
