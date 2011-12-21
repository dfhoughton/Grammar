package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

public class Regression {

	@Test
	public void optionReuse() {
		String[] rules = { "ROOT = /\\w++/" };
		Grammar g = null;
		try {
			g = new Grammar(rules);
		} catch (Exception e) {
			fail("threw exception");
		}
		Options opt = new Options().keepRightmost(true);
		g.matches("foo", opt);
		g.matches("alphabet", opt);
	}

	@Test
	public void finalNonmatch() {
		String[] rules = {
				//
				"ROOT = <word> /\\s*+/",//
				"word = /\\w++/",//
		};
		try {
			Grammar g = new Grammar(rules);
			Matcher m = g.find("foo", new Options());
			assertNotNull(m.match());
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}

	@Test
	public void missingFirstOffset() {
		String[] rules = {
				//
				"ROOT = /[*_]/+ <word> 1 /(?![\\p{L}\\d])/",// works if you use
															// native assertion
				"word = /\\w++/",//
		};
		try {
			Grammar g = new Grammar(rules);
			Matcher m = g.find("*terrible*", new Options().allowOverlap(true));
			assertNotNull(m.match());
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}

	@Test
	public void labelsAndDeferredRuleDefinition() {
		try {
			String[] rules = { "ROOT = <foo> | 'quux'" };
			Grammar g = new Grammar(rules);
			g.defineRule("foo", "bar");
			Match m = g.matches("bar").match();
			m.choose("foo");
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}
}
