package org.zells.node.io;

import org.zells.node.model.react.Mailing;
import org.zells.node.model.refer.names.*;
import org.zells.node.model.refer.Path;

import java.util.ArrayList;
import java.util.List;

public class ChiParser {

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
                if (!quoted && c == '"') {
                    quoted = true;
                    wasQuoted = true;

                } else if (quoted && c == '"') {
                    quoted = false;

                } else if (!quoted && name.toString().isEmpty() && c == '*') {
                    name.append(c);
                    endName();

                } else if (!quoted && name.toString().isEmpty() && c == '~') {
                    name.append(c);
                    endName();

                } else if (!quoted && name.toString().isEmpty() && c == '^') {
                    name.append(c);
                    endName();

                } else if (!quoted && name.toString().isEmpty() && c == '@') {
                    name.append(c);
                    endName();

                } else if (!quoted && name.toString().isEmpty() && path.isEmpty() && c == '$') {
                    path = new Path(Root.name(), Child.name("zells"), Child.name("literals"), Child.name("strings"));

                } else if (!quoted && name.toString().isEmpty() && path.isEmpty() && c == '#') {
                    path = new Path(Root.name(), Child.name("zells"), Child.name("literals"), Child.name("numbers"));

                } else if (!quoted && (c == ' ' || c == '.' || c == '\t')) {
                    endName();

                    if (c == ' ' || c == '\t') {
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
            if (!wasQuoted && nameString.equals("*")) {
                path = path.with(Root.name());
            } else if (!wasQuoted && nameString.equals("^")) {
                path = path.with(Parent.name());
            } else if (!wasQuoted && nameString.equals("~")) {
                path = path.with(Execution.name());
            } else if (!wasQuoted && nameString.equals("@")) {
                path = path.with(Message.name());
            } else {
                path = path.with(Child.name(nameString));
            }

            name = new StringBuilder();
            wasQuoted = false;
        }
    }
}
