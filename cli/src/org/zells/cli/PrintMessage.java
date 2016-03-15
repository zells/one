package org.zells.cli;

import org.zells.node.model.Cell;
import org.zells.node.model.refer.Path;

import java.io.PrintStream;

public class PrintMessage extends Cell {

    private final PrintStream out;

    public PrintMessage(Cell parent, PrintStream out) {
        super(parent);
        this.out = out;
    }

    @Override
    public boolean deliver(Path context, Path target, Path message) {
        out.println(">>> " + target + " " + message);
        out.print(Shell.PROMPT);
        return true;
    }
}
