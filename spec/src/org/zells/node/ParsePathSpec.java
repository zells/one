package org.zells.node;

import org.junit.Test;
import org.zells.node.model.refer.Child;
import org.zells.node.model.refer.Parent;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.Root;

import static org.junit.Assert.assertEquals;

public class ParsePathSpec {

    @Test
    public void emptyPath() {
        assertPath(new Path(), "");
    }

    @Test
    public void singleName() {
        assertPath(new Path(Child.name("foo")), "foo");
    }

    @Test
    public void multipleNames() {
        assertPath(new Path(Child.name("foo"), Child.name("bar"), Child.name("baz")), "foo.bar.baz");
    }

    @Test
    public void parent() {
        assertPath(new Path(Parent.name()), "^");
    }

    @Test
    public void root() {
        assertPath(new Path(Root.name()), "*");
    }

    @Test
    public void escapedName() {
        assertPath(new Path(Child.name("fo\\o.bar's"), Child.name("baz")), "'fo\\\\o.bar\\'s'.baz");
    }

    @Test
    public void multipleDots() {
        assertEquals(new Path(Child.name("foo"), Child.name("bar")), Path.parse("..foo..bar.."));
    }

    @Test
    public void withParent() {
        assertEquals(Path.parse("foo"), Path.parse("foo.bar").with(Parent.name()));
        assertEquals(Path.parse(""), Path.parse("foo").with(Parent.name()));
        assertEquals(Path.parse("^.^"), Path.parse("^").with(Parent.name()));
        assertEquals(Path.parse("^"), Path.parse("").with(Parent.name()));
    }

    @Test
    public void withRoot() {
        assertEquals(Path.parse("*"), Path.parse("foo.bar").with(Root.name()));
        assertEquals(Path.parse("*"), Path.parse("foo").with(Root.name()));
        assertEquals(Path.parse("*"), Path.parse("").with(Root.name()));
    }

    @Test
    public void upFromRoot() {
        assertEquals(Path.parse("*"), Path.parse("*").with(Parent.name()));
        assertEquals(Path.parse("*"), Path.parse("*").up());
    }

    private void assertPath(Path path, String string) {
        assertEquals(path, Path.parse(string));
        assertEquals(string, path.toString());
    }
}
