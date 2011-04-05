package dfh.grammar;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests to see whether conditions are working to screen matches.
 * <p>
 * <b>Creation date:</b> Apr 5, 2011
 * 
 * @author David Houghton
 * 
 */
public class ConditionTest {

	@Test
	public void leafTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = /\\b\\d++\\b/ {less_than_100}",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("less_than_100", new Condition() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean passes(Match m, CharSequence s) {
				int i = Integer.parseInt(s.subSequence(m.start(), m.end())
						.toString());
				return i < 100;
			}
		});
		String s = "99 100 1000";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("only found value lower than 100", count == 1);
	}

	@Test
	public void leafRenameTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>",//
				"<a> = /\\b\\d++\\b/ {less_than_100}",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("less_than_100", new Condition() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean passes(Match m, CharSequence s) {
				int i = Integer.parseInt(s.subSequence(m.start(), m.end())
						.toString());
				return i < 100;
			}
		});
		String s = "99 100 1000";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("only found value lower than 100", count == 1);
	}

}
