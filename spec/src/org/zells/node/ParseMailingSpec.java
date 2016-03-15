package org.zells.node;

import org.junit.Test;
import org.zells.node.model.react.Mailing;
import org.zells.node.model.refer.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParseMailingSpec {

    @Test
    public void emptyMailing() {
        try {
            Mailing.parse("  \t ");
        } catch (Exception ignored) {
            return;
        }

        fail("No exception thrown");
    }

    @Test
    public void noMessage() {
        assertMailing("foo", "foo", "");
    }

    @Test
    public void regular() {
        assertMailing("foo bar", "foo", "bar");
    }

    @Test
    public void escaped() {
        assertMailing("'foo bar'.me \"baz bam\"", "'foo bar'.me", "'baz bam'", "'foo bar'.me 'baz bam'");
    }

    private void assertMailing(String string, String target, String message, String back) {
        try {
            Mailing expected = new Mailing(Path.parse(target), Path.parse(message));
            Mailing actual = Mailing.parse(string);
            assertEquals(expected, actual);
            assertEquals(back, actual.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void assertMailing(String string, String target, String message) {
        assertMailing(string, target, message, string);
    }
}
