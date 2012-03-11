package dfh.grammar;

import static org.junit.Assert.*;

import org.junit.Test;

public class RoundTripTest {

	@Test
	public void literal() {
		String[] rules = { "ROOT = 'a'",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.matches("a").match());
	}

	@Test
	public void regex() {
		String[] rules = { "ROOT = /a/i",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.matches("A").match());
	}

	@Test
	public void alternation() {
		String[] rules = { "ROOT = 'a' | 'b'",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.matches("b").match());
	}

	@Test
	public void sequence() {
		String[] rules = { "ROOT = 'a' 'b'",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.matches("ab").match());
	}

	@Test
	public void cycle() {
		String[] rules = { "ROOT = 'a' | <ROOT> 'b'",//
		};
		Grammar g = new Grammar(rules);
		assertNotNull(g.matches("ab").match());
		String s = g.describe();
		g = new Grammar(s);
		assertNotNull(g.matches("ab").match());
	}

	@Test
	public void tag() {
		String[] rules = { "ROOT = [{foo} 'a']",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.matches("a").match().first("foo"));
	}

	@Test
	public void backreference() {
		String[] rules = { "ROOT = 'a' 'b' 1",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.matches("aba").match());
	}

	@Test
	public void forwardAssertion1() {
		String[] rules = { "ROOT = 'a' ~ /\\b/",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.matches("a").match());
	}

	@Test
	public void forwardAssertion2() {
		String[] rules = { "ROOT = 'a' ~+ /\\b/",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.matches("a").match());
	}

	@Test
	public void negativeForwardAssertion1() {
		String[] rules = { "ROOT = 'a' ! /\\B/",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.matches("a").match());
	}

	@Test
	public void negativeForwardAssertion2() {
		String[] rules = { "ROOT = 'a' !+ /\\B/",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.matches("a").match());
	}

	@Test
	public void backwardAssertion() {
		String[] rules = { "ROOT = ~- 'b' 'a'",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.find("ba").match());
	}

	@Test
	public void negativeBackwardAssertion() {
		String[] rules = { "ROOT = !- 'b' 'a'",//
		};
		Grammar g = new Grammar(new Grammar(rules).describe());
		assertNotNull(g.find("a").match());
	}
}
