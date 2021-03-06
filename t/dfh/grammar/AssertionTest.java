package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

/**
 * Makes sure zero-width assertions work as advertised.
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
	public void nativePositiveAssertionSimple() throws GrammarException,
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
	public void nativePositiveAssertionComplex() throws GrammarException,
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
	public void nativePositiveAssertionCompilationComplex2()
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
	public void nativePositiveAssertionCompilationSimple2()
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
	public void nativePositiveBackwardsAssertion() throws GrammarException,
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
	public void nativePositiveBackwardsWithBackReference()
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
	public void nativePositiveBackwardsWithDoubleBackReference()
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
	public void doubleReversedAssertionTest1() throws GrammarException,
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
	public void doubleReversedAssertionTest2() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = ~- <b> 'foo'",//
				"<b> = ~- <a> 'bar'",//
				"<a> = 'quux'",//
		};
		Grammar g = new Grammar(rules);
		String s = "quuxbarfoo";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found match", count == 1);
	}

	@Test
	public void tripleReversedAssertionTest() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = ~- <b> 'foo'",//
				"<b> = ~- <a> 'bar'",//
				"<a> = ~- <c> 'quux'",//
				"<c> = 'baz'",//
		};
		Grammar g = new Grammar(rules);
		String s = "bazquuxbarfoo";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found match", count == 1);
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

	@Test
	public void forwardsBackReferenceTest() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = 'foo' ~<b>",//
				"<b> = /[\"']/ 'bar' 1",//
		};
		Grammar g = new Grammar(rules);
		String s = "foo'bar' foo\"bar\"";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found match", count == 2);
	}

	@Test
	public void forwardsRepetitionTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = 'foo' ~<b>",//
				"<b> = 'bar'{1,3} !'bar'",//
		};
		Grammar g = new Grammar(rules);
		String s = "foobar foobarbar foobarbarbarbar";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found match", count == 2);
	}

	@Test
	public void backwardsRepetitionTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = ~-<b> 'foo'",//
				"<b> = !- 'bar' 'bar'{1,3}",//
		};
		Grammar g = new Grammar(rules);
		String s = "barfoo barbarfoo barbarbarbarfoo";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found match", count == 2);
	}

	@Test
	public void forwardsBacktrackingBarrier() throws GrammarException,
			IOException {
		String[] rules = {
				//
				"<ROOT> = 'a' ~ <b>",//
				"<b> = 'b'+ : 'bc'",//
		};
		Grammar g = new Grammar(rules);
		String s = "abbc";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("could not match due to barrier", count == 0);
	}

	@Test
	public void beforeTest() {
		Grammar g = new Grammar("ROOT = before /\\d/ /\\w/");
		assertNotNull(g.matches("1").match());
	}

	@Test
	public void notBeforeTest() {
		Grammar g = new Grammar("ROOT = not before /\\d/ /\\w/");
		assertNotNull(g.matches("a").match());
	}

	@Test
	public void bangBeforeTest1() {
		Grammar g = new Grammar("ROOT = ! before /\\d/ /\\w/");
		assertNotNull(g.matches("a").match());
	}

	@Test
	public void bangBeforeTest2() {
		Grammar g = new Grammar("ROOT = !before /\\d/ /\\w/");
		assertNotNull(g.matches("a").match());
	}

	@Test
	public void afterTest() {
		Grammar g = new Grammar("ROOT = after /\\b/r /\\w/");
		assertNotNull(g.matches("1").match());
	}

	@Test
	public void notAfterTest() {
		Grammar g = new Grammar("ROOT = not after /\\B/r /\\w/");
		assertNotNull(g.matches("a").match());
	}

	@Test
	public void bangAfterTest() {
		Grammar g = new Grammar("ROOT = ! after /\\B/r /\\w/");
		assertNotNull(g.matches("a").match());
	}
}
