package dfh.grammar;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Based on {@link SimpleMatchTest}. Tests comments and blank lines.
 * <p>
 * <b>Creation date:</b> Mar 22, 2011
 * 
 * @author David Houghton
 * 
 */
public class CommentTest {

	private static Grammar g;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>  # first comment", //
				"",// blank line
				"<a> = <foo> <s> <bar>",//
				"  # comment line",//
				"<b> = <quux> <s> <baz> # another comment",//
				"<s> = /\\s++/",//
				"<foo> = /foo/",//
				"<bar> = /bar/",//
				"<quux> = /quux/",//
				"<baz> = /baz/",//
		};
		g = new Grammar(rules);
	}

	@Test
	public void aTest() {
		String s = "foo bar";
		Matcher m = g.matches(s);
		Match n = m.match();
		assertNotNull("found 'foo bar'", n);
	}

	@Test
	public void bTest() {
		String s = "quux baz";
		Matcher m = g.matches(s);
		Match n = m.match();
		assertNotNull("found 'quux baz'", n);
	}

	@Test
	public void abTest() {
		String s = "quux baz  foo bar";
		int count = 0;
		Matcher m = g.find(s);
		while (count < 3 && m.match() != null) {
			count++;
		}
		assertTrue("found both matches in " + s, count == 2);
	}

	@Test
	public void failureTest() {
		String s = "quwerewr";
		Matcher m = g.matches(s);
		Match n = m.match();
		assertNull("recognized non-match for " + s, n);
	}
}
