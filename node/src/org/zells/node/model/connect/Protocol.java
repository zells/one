package org.zells.node.model.connect;

import org.zells.node.model.react.Delivery;
import org.zells.node.model.refer.Path;

import java.io.IOException;

public interface Protocol {

    public Signal ok();

    public Signal fail(String message);

    public Signal deliver(Delivery delivery);

    public Signal join(Path path, String host, int port);

    public Signal parse(String signal) throws IOException;
}
