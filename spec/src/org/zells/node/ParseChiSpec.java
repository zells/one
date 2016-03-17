package org.zells.node;

import org.junit.Ignore;
import org.junit.Test;
import org.zells.node.io.ChiParser;
import org.zells.node.model.react.Mailing;
import org.zells.node.model.refer.*;
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
        parse("foo.bar");
        assertEquals(new Path(Child.name("foo"), Child.name("bar")), mailings.get(0).getTarget());
    }

    @Test
    public void rootName() {
        assertParsedName("*", Root.name());
    }

    @Test
    public void parentName() {
        assertParsedName("^", Parent.name());
    }

    @Test
    public void messageAlias() {
        assertParsedName("@", Message.name());
    }

    @Test
    public void executionAlias() {
        assertParsedName("~", Execution.name());
    }

    @Test
    public void quotedName() {
        assertParsedName("\"foo\"", Child.name("foo"));
    }

    @Test
    public void escapeDots() {
        assertParsedName("\"foo.bar\"", Child.name("foo.bar"));
    }

    @Test
    public void escapeSpaces() {
        assertParsedName("\"foo bar\"", Child.name("foo bar"));
    }

    @Test
    public void escapeRoot() {
        assertParsedName("\"*\"", Child.name("*"));
    }

    @Test
    public void escapeParent() {
        assertParsedName("\"^\"", Child.name("^"));
    }

    @Test
    public void escapeMessage() {
        assertParsedName("\"@\"", Child.name("@"));
    }

    @Test
    public void escapeExecution() {
        assertParsedName("\"~\"", Child.name("~"));
    }

    private void parse(String foo) {
        mailings = new ChiParser().parse(foo);
    }

    private void assertName(Name name) {
        assertEquals(new Path(name), mailings.get(0).getTarget());
    }

    private void assertParsedName(String string, Name name) {
        parse(string);
        assertName(name);
    }
}
