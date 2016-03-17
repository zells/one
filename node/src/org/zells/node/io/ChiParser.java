package org.zells.node.io;

import org.zells.node.model.react.Mailing;
import org.zells.node.model.refer.names.*;
import org.zells.node.model.refer.Path;

import java.util.ArrayList;
import java.util.List;

public class ChiParser {

    public List<Mailing> parse(String input) {
        List<Mailing> mailings = new ArrayList<Mailing>();
        List<Path> paths = new ArrayList<Path>();
        Path path = new Path();
        StringBuilder name = new StringBuilder();

        input = input + "\n";
        String[] lines = input.split("\n");

        boolean quoted = false;
        boolean wasQuoted = false;
        for (String line : lines) {
            line = line.trim() + " ";

            for (char c : line.toCharArray()) {
                if (!quoted && c == '"') {
                    quoted = true;
                    wasQuoted = true;

                } else if (quoted && c == '"') {
                    quoted = false;

                } else if (!quoted && (c == ' ' || c == '.' || c == '\t')) {
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

                    if (c == ' ' || c == '\t') {
                        if (!path.isEmpty()) {
                            paths.add(path);
                        }
                        path = new Path();

                        if (paths.size() == 2) {
                            break;
                        }
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
}
