package org.zells.cli;

import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.react.Reaction;
import org.zells.node.model.refer.Path;

import java.io.PrintStream;

public class PrintMessage implements Reaction {

    private final PrintStream out;

    public PrintMessage(PrintStream out) {
        this.out = out;
    }

    @Override
    public Path execute(Cell cell, Delivery delivery) {
        out.println(">>> " + delivery.getMessage());
        out.print(Shell.PROMPT);
        return delivery.getContext();
    }
}
