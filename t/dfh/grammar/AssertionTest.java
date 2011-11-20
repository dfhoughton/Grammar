package dfh.grammar;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

/**
 * Makes sure zero-width assertions work as advertised.
 * 
 * TODO: make forwards and backwards tests for {@link BacktrackingBarrier},
 * {@link RepetitionRule}; forward test for {@link BackReferenceRule}
 * 
 * <b>Creation date:</b> Mar 28, 2011
 * 
 * @author David Houghton
 * 
 */
@SuppressWarnings("unused")
public class AssertionTest {

	@Test
	public void basicTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <joe>",//
				"<joe> = /\\bjoe\\b/",//
		};
		Grammar g = new Grammar(rules);
		String s = "  joe _joe_ ";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found joe", count == 1);
	}

	@Test
	public void backwardsTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <not_space> <joe>",//
				"<joe> = /joe/i",//
				"<not_space> = /(?<!\\s)/i",//
		};
		Grammar g = new Grammar(rules);
		String s = "  maryjoe susie joe ";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found joe", count == 1);
	}

	@Test
	public void forwardTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <joe> <s>?+ <is_sue>",//
				"<joe> = /\\bjoe\\b/i",//
				"<is_sue> = /(?=sue)/i",//
				"<s> = /\\s++/",//
		};
		Grammar g = new Grammar(rules);
		String s = " joe mary joe sue ";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found joe", count == 1);
	}

	@Test
	public void nativePositiveAssertionCompilationSimple()
			throws GrammarException, IOException {
		try {
			String[] rules = {
					//
					"<ROOT> = ~ '1' <b>",//
					"<b> = /\\b\\d++/",//
			};
			Grammar g = new Grammar(rules);
		} catch (Exception e) {
			fail("could not compile a simple positive assertion");
		}
	}

	@Test
	public void nativeNegativeAssertionCompilationSimple()
			throws GrammarException, IOException {
		try {
			String[] rules = {
					//
					"<ROOT> = ! '1' <b>",//
					"<b> = /\\b\\d++/",//
			};
			Grammar g = new Grammar(rules);
		} catch (Exception e) {
			fail("could not compile a simple negative assertion");
		}
	}

	@Test
	public void nativePositiveAssertionCompilationComplex()
			throws GrammarException, IOException {
		try {
			String[] rules = {
					//
					"<ROOT> = ~['1'|'2'] <b>",//
					"<b> = /\\b\\d++/",//
			};
			Grammar g = new Grammar(rules);
		} catch (Exception e) {
			fail("could not compile a complex positive assertion");
		}
	}

	@Test
	public void nativeNegativeAssertionCompilationComplex()
			throws GrammarException, IOException {
		try {
			String[] rules = {
					//
					"<ROOT> = !['1'|'2'] <b>",//
					"<b> = /\\b\\d++/",//
			};
			Grammar g = new Grammar(rules);
		} catch (Exception e) {
			fail("could not compile a complex negative assertion");
		}
	}

	@Test
	public void nativePostiveAssertionSimple() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = ~ '1' <b>",//
				"<b> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("1 2");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("used simple positive native assertion", count == 1);
	}

	@Test
	public void nativeNegativeAssertionSimple() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = ! '1' <b>",//
				"<b> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("1 2");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("used simple negative native assertion", count == 1);
	}

	@Test
	public void nativePostiveAssertionComplex() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = ~['1'|'2'] <b>",//
				"<b> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("1 2 3");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("used complex positive native assertion", count == 2);
	}

	@Test
	public void nativeNegativeAssertionComplex() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = !['1'|'2'] <b>",//
				"<b> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("1 2 3");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("used complex negative native assertion", count == 1);
	}

	@Test
	public void nativeNegativeAssertionCompilationComplex2()
			throws GrammarException, IOException {
		try {
			String[] rules = {
					//
					"<ROOT> = !+['1'|'2'] <b>",//
					"<b> = /\\b\\d++/",//
			};
			Grammar g = new Grammar(rules);
		} catch (Exception e) {
			fail("could not compile a complex negative assertion");
		}
	}

	@Test
	public void nativeNegativeAssertionCompilationSimple2()
			throws GrammarException, IOException {
		try {
			String[] rules = {
					//
					"<ROOT> = !+ '1' <b>",//
					"<b> = /\\b\\d++/",//
			};
			Grammar g = new Grammar(rules);
		} catch (Exception e) {
			fail("could not compile a simple negative assertion");
		}
	}

	@Test
	public void nativePostiveAssertionCompilationComplex2()
			throws GrammarException, IOException {
		try {
			String[] rules = {
					//
					"<ROOT> = ~+['1'|'2'] <b>",//
					"<b> = /\\b\\d++/",//
			};
			Grammar g = new Grammar(rules);
		} catch (Exception e) {
			fail("could not compile a complex positive assertion");
		}
	}

	@Test
	public void nativePostiveAssertionCompilationSimple2()
			throws GrammarException, IOException {
		try {
			String[] rules = {
					//
					"<ROOT> = ~+ '1' <b>",//
					"<b> = /\\b\\d++/",//
			};
			Grammar g = new Grammar(rules);
		} catch (Exception e) {
			fail("could not compile a simple positive assertion");
		}
	}

	@Test
	public void nativePostiveBackwardsAssertion() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = ~- '@' <b>",//
				"<b> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("@1 2");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("used simple positive native assertion", count == 1);
	}

	@Test
	public void nativeNegativeBackwardsAssertion() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = !- '@' <b>",//
				"<b> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("@1 2");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("used simple positive native assertion", count == 1);
	}

	@Test
	public void nativeNegativeBackwardsAssertionComplex()
			throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = !- ['@'|'#'] <b>",//
				"<b> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("@1 2");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("used simple positive native assertion", count == 1);
	}

	@Test
	public void nativePositiveBackwardsAssertionComplex()
			throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = ~- ['@'|'#'] <b>",//
				"<b> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("@1 2");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("used simple positive native assertion", count == 1);
	}

	@Test
	public void nativePositiveBackwardsAssertionVariable()
			throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = ~- ['@' <s>] <b>",//
				"<s> = /\\s++/r",//
				"<b> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("@ 1 2");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("used simple positive native assertion", count == 1);
	}

	@Test
	public void nativeNegativeBackwardsAssertionVariable()
			throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = !- ['@' <s>] <b>",//
				"<s> = /\\s++/r",//
				"<b> = /\\b\\d++/",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("@ 1 2");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("used simple positive native assertion", count == 1);
	}

	@Test
	public void nativePostiveBackwardsWithBackReference()
			throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = ~-[ ['_'|'-'] 'foo' 1 ] 'bar'",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g.find("-foo-bar _foo_bar -foo_bar _foo-bar");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue(
				"used back reference in simple positive native backwards assertion",
				count == 2);
	}

	@Test
	public void nativePostiveBackwardsWithDoubleBackReference()
			throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = ~-[ ['_'|'-'] 'foo' 1 'quux' 1 ] 'bar'",//
		};
		Grammar g = new Grammar(rules);
		Matcher m = g
				.find("-foo-quux-bar _foo_quux_bar -foo_quux_bar _foo-quux-bar");
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue(
				"used back reference in simple positive native backwards assertion",
				count == 2);
	}

	@Test
	public void doubleReversedAssertionTest() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = ~- <b> 'foo'",//
				"<b> = ~- <a> 'bar'",//
				"<a> = /(?<!\\d)\\d++(?!\\d)/r",//
		};
		Grammar g = new Grammar(rules);
		String s = "5barfoo 11barfoo";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found match", count == 2);
	}

	@Test
	public void doubleForwardAssertionTest() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = 'foo' ~<b>",//
				"<b> = 'bar' ~<a>",//
				"<a> = /(?<!\\d)\\d++(?!\\d)/r",//
		};
		Grammar g = new Grammar(rules);
		String s = "foobar5 foobar5";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found match", count == 2);
	}

}
