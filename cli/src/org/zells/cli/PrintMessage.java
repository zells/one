package org.zells.cli;

import org.zells.node.model.Cell;
import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;

import java.io.PrintStream;

public class PrintMessage extends Cell {

    private final PrintStream out;

    public PrintMessage(Cell parent, PrintStream out) {
        super(parent);
        this.out = out;
    }

    @Override
    public Path deliver(Delivery delivery) {
        out.println(">>> " + delivery.getTarget() + " " + delivery.getMessage());
        out.print(Shell.PROMPT);
        return delivery.getContext();
    }
}
