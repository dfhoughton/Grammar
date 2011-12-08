package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

/**
 * Checks whether escaped characters are being interpreted properly in literals.
 * 
 * <b>Creation date:</b> Oct 6, 2011
 * 
 * @author David Houghton
 * 
 */
public class EscapesInLiterals {

	@Test
	public void tab() {
		String[] rules = { "ROOT = 'a\\tb'",//
		};
		try {
			Grammar g = new Grammar(rules);
			Match m = g.matches("a\tb").match();
			assertNotNull("found \\t", m);
		} catch (GrammarException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void newline() {
		String[] rules = { "ROOT = 'a\\nb'",//
		};
		try {
			Grammar g = new Grammar(rules);
			Match m = g.matches("a\nb").match();
			assertNotNull("found \\n", m);
		} catch (GrammarException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void formFeed() {
		String[] rules = { "ROOT = 'a\\fb'",//
		};
		try {
			Grammar g = new Grammar(rules);
			Match m = g.matches("a\fb").match();
			assertNotNull("found \\f", m);
		} catch (GrammarException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void returnChar() {
		String[] rules = { "ROOT = 'a\\rb'",//
		};
		try {
			Grammar g = new Grammar(rules);
			Match m = g.matches("a\rb").match();
			assertNotNull("found \\r", m);
		} catch (GrammarException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void backspace() {
		String[] rules = { "ROOT = 'a\\bb'",//
		};
		try {
			Grammar g = new Grammar(rules);
			Match m = g.matches("a\bb").match();
			assertNotNull("found \\b", m);
		} catch (GrammarException e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void backslash() {
		String[] rules = { "ROOT = 'a\\\\b'",//
		};
		try {
			Grammar g = new Grammar(rules);
			Match m = g.matches("a\\b").match();
			assertNotNull("found \\", m);
		} catch (GrammarException e) {
			fail(e.getMessage());
		}
	}
}
