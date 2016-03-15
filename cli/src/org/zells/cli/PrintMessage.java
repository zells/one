package org.zells.cli;

import org.zells.node.model.Cell;
import org.zells.node.model.refer.Path;
import org.zells.node.model.respond.Response;

import java.io.PrintStream;

public class PrintMessage implements Response {

    private final PrintStream out;

    public PrintMessage(PrintStream out) {
        this.out = out;
    }

    @Override
    public void execute(Cell cell, Path context, Path message) {
        out.println(">>> " + message);
        out.print(Shell.PROMPT);
    }
}
