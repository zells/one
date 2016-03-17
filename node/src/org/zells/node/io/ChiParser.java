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
    private static Map<Character, Path> shortcuts;

    static {
        names = new HashMap<Character, Name>();
        names.put('*', Root.name());
        names.put('^', Parent.name());
        names.put('@', Message.name());
        names.put('~', Execution.name());

        shortcuts = new HashMap<Character, Path>();
        shortcuts.put('$', new Path(Root.name(), Child.name("zells"), Child.name("literals"), Child.name("strings")));
        shortcuts.put('#', new Path(Root.name(), Child.name("zells"), Child.name("literals"), Child.name("numbers")));
    }

    private List<Character> whitespaces = Arrays.asList(' ', '\t', '\r', '\n');

    private List<Mailing> mailings = new ArrayList<Mailing>();
    private List<Path> paths = new ArrayList<Path>();
    private Path path = new Path();
    private StringBuilder name = new StringBuilder();

    private boolean quoted = false;
    private boolean wasQuoted = false;
    private boolean skipRestOfLine = false;

    public List<Mailing> parse(String input) {
        initialize();
        input = input + "\n";

        for (char c : input.toCharArray()) {
            parseLine(c);
        }

        return mailings;
    }

    private void initialize() {
        mailings = new ArrayList<Mailing>();
        paths = new ArrayList<Path>();
        path = new Path();
        name = new StringBuilder();

        quoted = false;
        wasQuoted = false;
        skipRestOfLine = false;
    }

    private void parseLine(char c) {
        if (c == '\n') {
            skipRestOfLine = false;
        }

        if (!skipRestOfLine) {
            parseCharacter(c);
        }
    }

    private void parseCharacter(char c) {
        if (quoted && c == QUOTE) {
            endQuotes();
        } else if (!quoted && c == QUOTE) {
            startQuotes();
        } else if (!quoted && name.toString().isEmpty() && names.containsKey(c)) {
            skipDot(c);
        } else if (!quoted && name.toString().isEmpty() && path.isEmpty() && shortcuts.containsKey(c)) {
            insertShortcut(c);
        } else if (!quoted && (whitespaces.contains(c) || c == SEPARATOR)) {
            parseLastCharacter(c);
        } else {
            name.append(c);
        }
    }

    private void parseLastCharacter(char c) {
        endName();

        if (whitespaces.contains(c)) {
            endPath();
        }

        if (paths.size() == 2) {
            endPaths(new Mailing(paths.get(0), paths.get(1)));
            skipRestOfLine = (c != '\n');
        } else if (c == '\n' && !paths.isEmpty()) {
            endPaths(new Mailing(paths.get(0), new Path()));
        }
    }

    private void endQuotes() {
        quoted = false;
    }

    private void startQuotes() {
        quoted = true;
        wasQuoted = true;
    }

    private void skipDot(char c) {
        name.append(c);
        endName();
    }

    private void insertShortcut(char c) {
        path = shortcuts.get(c);
    }

    private void endName() {
        String nameString = name.toString();
        if (!nameString.isEmpty()) {
            addName(nameString);

            name = new StringBuilder();
            wasQuoted = false;
        }
    }

    private void addName(String nameString) {
        if (!wasQuoted && nameString.length() == 1 && names.containsKey(nameString.charAt(0))) {
            path = path.with(names.get(nameString.charAt(0)));
        } else {
            path = path.with(Child.name(nameString));
        }

    }

    private void endPath() {
        if (!path.isEmpty()) {
            paths.add(path);
        }
        path = new Path();
    }

    private void endPaths(Mailing e) {
        mailings.add(e);
        paths = new ArrayList<Path>();
    }
}
