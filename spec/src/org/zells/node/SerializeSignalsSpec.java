package org.zells.node;

import org.junit.Test;
import org.zells.node.model.connect.Signal;
import org.zells.node.model.refer.names.Child;
import org.zells.node.model.refer.names.Parent;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.Root;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class SerializeSignalsSpec extends Specification {

    @Test
    public void singleName() {
        assertSignal(new Path(Child.name("foo")), "foo");
    }

    @Test
    public void composedPath() {
        assertSignal(new Path(Child.name("foo"), Child.name("bar")), "foo.bar");
    }

    @Test
    public void rootName() {
        assertSignal(new Path(Root.name(), Child.name("foo")), "*.foo");
    }

    @Test
    public void shortenRoot() {
        assertSignal(new Path(Root.name(), Child.name("foo"), Root.name(), Child.name("bar")), "*.foo.*.bar",
                new Path(Root.name(), Child.name("bar")));
    }

    @Test
    public void parentName() {
        assertSignal(new Path(Parent.name(), Child.name("foo")), "^.foo");
    }

    @Test
    public void resolveParent() {
        assertSignal(new Path(Child.name("bar"), Parent.name(), Child.name("foo")), "bar.^.foo",
                new Path(Child.name("foo")));
    }

    @Test
    public void escapeDots() {
        assertSignal(new Path(Child.name("foo.bar")), "foo:bar");
    }

    @Test
    public void escapeSemicolons() {
        assertSignal(new Path(Child.name("foo:bar")), "foo+:bar");
        assertSignal(new Path(Child.name("foo:.:bar")), "foo+::+:bar");
        assertSignal(new Path(Child.name("foo+:+.+:+bar")), "foo+++:++:+++:++bar");
    }

    @Test
    public void replaceSpaces() {
        assertSignal(new Path(Child.name("foo bar")), "foo_bar");
    }

    @Test
    public void escapeUnderscores() {
        assertSignal(new Path(Child.name("foo_bar")), "foo+_bar");
        assertSignal(new Path(Child.name("foo_ _bar")), "foo+__+_bar");
        assertSignal(new Path(Child.name("foo+_+ +_+bar")), "foo+++_++_+++_++bar");
    }

    @Test
    public void escapeRootName() {
        assertSignal(new Path(Child.name("*")), "+*");
        assertSignal(new Path(Child.name("+*")), "+++*");
    }

    @Test
    public void escapeParentName() {
        assertSignal(new Path(Child.name("^")), "+^");
        assertSignal(new Path(Child.name("+^")), "+++^");
    }

    @Test
    public void escapePlus() {
        assertSignal(new Path(Child.name("+")), "++");
    }

    private void assertSignal(Path path, String string) {
        assertSignal(path, string, path);
    }

    private void assertSignal(Path path, String string, Path parsed) {
        try {
            Signal signal = protocol.join(path, "me", 0);
            assertEquals("Serializing failed", "JOIN " + string + " me 0", signal.serialize());
            assertEquals("Parsing failed", protocol.join(parsed, "me", 0), protocol.parse(signal.serialize()));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
