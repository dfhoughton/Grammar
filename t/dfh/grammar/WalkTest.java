package dfh.grammar;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests whether {@link Match#walk(MatchTest)} is working to spec.
 * 
 * @author houghton
 */
public class WalkTest {

	@Test
	public void transformTest() {
		String[] rules = {
				//
				"ROOT = <b> [ /\\s++/ <b> ]++",//
				"b = 'b'",//
		};
		Grammar g = new Grammar(rules);
		Match m = g.matches("b b b").match();
		assertNotNull(m);
		final StringBuilder b = new StringBuilder();
		m.walk(new MatchTest() {
			@Override
			public boolean test(Match m) {
				if (m.hasLabel("b"))
					b.append('a');
				else if (m.isTerminal())
					b.append(m.group());
				return false;
			}
		});
		assertTrue("transformed appropriate nodes", b.toString()
				.equals("a a a"));
	}

	@Test
	public void retValTest() {
		String[] rules = {
				//
				"ROOT = <b> [ /\\s++/ <b> ]++",//
				"b = 'b'",//
		};
		Grammar g = new Grammar(rules);
		Match m = g.matches("b b b").match();
		assertNotNull(m);
		boolean retVal = m.walk(new MatchTest() {
			@Override
			public boolean test(Match m) {
				if (m.hasLabel("b"))
					return true;
				return false;
			}
		});
		assertTrue("returned appropriate value", retVal);
	}

}
