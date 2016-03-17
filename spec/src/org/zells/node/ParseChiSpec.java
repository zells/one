package org.zells.node;

import org.junit.Test;
import org.zells.node.io.ChiParser;
import org.zells.node.model.react.Mailing;
import org.zells.node.model.refer.Name;
import org.zells.node.model.refer.Path;
import org.zells.node.model.refer.names.*;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ParseChiSpec extends Specification {

    private List<Mailing> mailings;

    @Test
    public void onlyTarget() {
        parse("  foo  ");

        assertEquals(1, mailings.size());
        assertEquals(new Path(Child.name("foo")), mailings.get(0).getTarget());
        assertEquals(new Path(), mailings.get(0).getMessage());
    }

    @Test
    public void targetAndMessage() {
        parse("  foo   bar  ");

        assertEquals(1, mailings.size());
        assertEquals(new Path(Child.name("foo")), mailings.get(0).getTarget());
        assertEquals(new Path(Child.name("bar")), mailings.get(0).getMessage());
    }

    @Test
    public void tabs() {
        parse(" \t foo \t bar \t ");

        assertEquals(1, mailings.size());
        assertEquals(new Path(Child.name("foo")), mailings.get(0).getTarget());
        assertEquals(new Path(Child.name("bar")), mailings.get(0).getMessage());
    }

    @Test
    public void comment() {
        parse("foo bar all the rest is ignored");

        assertEquals(1, mailings.size());
        assertEquals(new Path(Child.name("foo")), mailings.get(0).getTarget());
        assertEquals(new Path(Child.name("bar")), mailings.get(0).getMessage());
    }

    @Test
    public void multipleMailings() {
        parse("foo bar\nbaz bam");

        assertEquals(2, mailings.size());
        assertEquals(new Path(Child.name("foo")), mailings.get(0).getTarget());
        assertEquals(new Path(Child.name("bar")), mailings.get(0).getMessage());
        assertEquals(new Path(Child.name("baz")), mailings.get(1).getTarget());
        assertEquals(new Path(Child.name("bam")), mailings.get(1).getMessage());
    }

    @Test
    public void emptyLines() {
        parse("foo bar and stuff\r\n\t\n   \r\n   \t \r\nbaz bam");
        assertEquals(2, mailings.size());
    }

    @Test
    public void composedPath() {
        assertParsedName("foo.bar", Child.name("foo"), Child.name("bar"));
    }

    @Test
    public void specialNames() {
        assertParsedName("*", Root.name());
        assertParsedName("^", Parent.name());
        assertParsedName("@", Message.name());
        assertParsedName("~", Execution.name());
    }

    @Test
    public void specialNamesInside() {
        assertParsedName("foo*~^@", Child.name("foo*~^@"));
    }

    @Test
    public void quotes() {
        assertParsedName("\"foo\"", Child.name("foo"));
        assertParsedName("\"foo.bar\"", Child.name("foo.bar"));
        assertParsedName("\"foo bar\"", Child.name("foo bar"));
        assertParsedName("\"*\"", Child.name("*"));
        assertParsedName("\"^\"", Child.name("^"));
        assertParsedName("\"@\"", Child.name("@"));
        assertParsedName("\"~\"", Child.name("~"));
    }

    @Test
    public void skipDot() {
        assertParsedName("*foo", Root.name(), Child.name("foo"));
        assertParsedName("^foo", Parent.name(), Child.name("foo"));
        assertParsedName("@foo", Message.name(), Child.name("foo"));
        assertParsedName("~foo", Execution.name(), Child.name("foo"));
        assertParsedName("\"*foo\"", Child.name("*foo"));
        assertParsedName("\"^foo\"", Child.name("^foo"));
        assertParsedName("\"@foo\"", Child.name("@foo"));
        assertParsedName("\"~foo\"", Child.name("~foo"));
    }

    @Test
    public void shortLiterals() {
        assertParsedName("\"$foo\"", Child.name("$foo"));
        assertParsedName("\"#foo\"", Child.name("#foo"));

        assertParsedName("bar.$foo", Child.name("bar"), Child.name("$foo"));
        assertParsedName("bar.#foo", Child.name("bar"), Child.name("#foo"));

        assertParsedName("$foo.bar", Root.name(), Child.name("zells"), Child.name("literals"), Child.name("strings"), Child.name("foo"), Child.name("bar"));
        assertParsedName("#foo.bar", Root.name(), Child.name("zells"), Child.name("literals"), Child.name("numbers"), Child.name("foo"), Child.name("bar"));
    }

    private void parse(String foo) {
        mailings = new ChiParser().parse(foo);
    }

    private void assertName(Name... name) {
        assertEquals(new Path(name), mailings.get(0).getTarget());
    }

    private void assertParsedName(String string, Name... name) {
        parse(string);
        assertName(name);
    }
}
