package dfh.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

/**
 * Make sure we can define {@link DeferredDefinitionRule deferred rules} using
 * {@link Grammar grammars} instead of patterns or {@link Rule Rules}.
 * <p>
 * <b>Creation date:</b> Mar 31, 2011
 * 
 * @author David Houghton
 * 
 */
@SuppressWarnings("serial")
public class TerminalGrammarTest {

	@Test
	public void basicTest() throws GrammarException, IOException {
		String[] rules1 = {
		//
		"<ROOT> = <a>",//
		};
		String[] rules2 = {
		//
		"<ROOT> = 'a'",//
		};
		Grammar g1 = new Grammar(rules1);
		Grammar g2 = new Grammar(rules2);
		g1.defineRule("a", g2);
		String s = "a";
		Matcher m = g1.find(s);
		assertNotNull("found joe", m.match());
	}

	@Test
	public void renamingTest() throws GrammarException, IOException {
		String[] rules1 = {
		//
		"<ROOT> = <a> | <b>",//
		};
		String[] rulesA = {
				//
				"<ROOT> = <a>{2} <b>",//
				"<a> = 'a'",//
				"<b> = 'b'",//
		};
		String[] rulesB = {
				//
				"<ROOT> = <a> <b>{3}",//
				"<a> = 'a'",//
				"<b> = 'b'",//
		};
		Grammar g1 = new Grammar(rules1);
		Grammar gA = new Grammar(rulesA);
		Grammar gB = new Grammar(rulesB);
		g1.defineRule("a", gA);
		g1.defineRule("b", gB);
		String s = "aab";
		Matcher m = g1.find(s);
		assertNotNull("found joe", m.match());
	}

	@Test
	public void cyclicTest() throws GrammarException, IOException {
		String[] rules1 = {
				//
				"<ROOT> = <foo>",//
				"<foo> = <bar> | <quux>",//
				"<bar> = <a>",//
				"<quux> = <b> <foo> 1",//
		};
		String[] rulesA = {
				//
				"<ROOT> = <a>{2} <b>",//
				"<a> = 'a'",//
				"<b> = 'b'",//
		};
		String[] rulesB = {
				//
				"<ROOT> = <a> <b>{1,2}",//
				"<a> = 'a'",//
				"<b> = 'b'",//
		};
		Grammar g1 = new Grammar(rules1);
		Grammar gA = new Grammar(rulesA);
		Grammar gB = new Grammar(rulesB);
		g1.defineRule("a", gA);
		g1.defineRule("b", gB);
		String s = "aab";
		Matcher m = g1.find(s);
		assertNotNull("found joe", m.match());
	}

	@Test
	public void conditionTest() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = /\\b\\d++\\b/ (lt100)");
		gb.defineCondition("lt100", new Condition() {
			@Override
			public boolean passes(CharSequence subsequence) {
				return Integer.parseInt(subsequence.toString()) < 100;
			}
		});
		int count = 0;
		Matcher m = gb.find("1 10 100");
		while (m.match() != null)
			count++;
		assertTrue("found only first two", count == 2);
		ga.defineRule("b", gb);
		count = 0;
		m = gb.find("1 10 100");
		while (m.match() != null) {
			count++;
		}
		assertTrue("found only first two", count == 2);
	}

	@Test
	public void conditionDescriptionTest() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = /\\b\\d++\\b/ (lt100)");
		gb.defineCondition("lt100", new Condition() {
		});
		ga.defineRule("b", gb);
		String description = ga.describe();
		assertTrue("condition name appears in description",
				description.indexOf("lt100") > -1);
	}

	@Test
	public void logicalConditionTest() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = /\\b\\d++\\b/ (lt100 gt10)");
		gb.defineCondition("lt100", new IntegerCondition() {
			@Override
			public boolean passes(int i) {
				return i < 100;
			}
		});
		gb.defineCondition("gt10", new IntegerCondition() {
			@Override
			public boolean passes(int i) {
				return i > 10;
			}
		});
		ga.defineRule("b", gb);
		Matcher m = ga.find("1 10 12 24 100 1000");
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals("logical condition worked after copying", 2, count);
	}

	@Test
	public void tagTest1() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = [{foo} /\\b\\d++\\b/ ]");
		ga.defineRule("b", gb);
		String s = ga.describe();
		assertTrue("tag copied", s.indexOf("foo") > -1);
	}

	@Test
	public void tagTest2() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = [{foo} 'bar' ]");
		ga.defineRule("b", gb);
		String s = ga.describe();
		assertTrue("tag copied", s.indexOf("foo") > -1);
	}

	@Test
	public void tagTest3() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = [{foo} 'bar' | 'quux' ]");
		ga.defineRule("b", gb);
		String s = ga.describe();
		assertTrue("tag copied", s.indexOf("foo") > -1);
	}

	@Test
	public void tagTest4() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = [{foo} [ 'bar' | 'quux' ] ]");
		ga.defineRule("b", gb);
		String s = ga.describe();
		assertTrue("tag copied", s.indexOf("foo") > -1);
	}

	@Test
	public void tagTest5() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = [{foo} 'bar' ]+");
		ga.defineRule("b", gb);
		String s = ga.describe();
		assertTrue("tag copied", s.indexOf("foo") > -1);
	}

	@Test
	public void tagTest6() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = [{foo} 'bar'+ ]");
		ga.defineRule("b", gb);
		String s = ga.describe();
		assertTrue("tag copied", s.indexOf("foo") > -1);
	}

	@Test
	public void tagTest7() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = [{foo} 'bar' 'quux' ]");
		ga.defineRule("b", gb);
		String s = ga.describe();
		assertTrue("tag copied", s.indexOf("foo") > -1);
	}

	@Test
	public void tagTest8() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = [{foo} 'bar' ] 'quux'");
		ga.defineRule("b", gb);
		String s = ga.describe();
		assertTrue("tag copied", s.indexOf("foo") > -1);
	}

	@Test
	public void tagTest9() {
		Grammar ga = new Grammar("ROOT = <b>");
		Grammar gb = new Grammar("ROOT = [{foo} /\\b\\d++\\b/ ] (gt10)");
		gb.defineCondition("gt10", new IntegerCondition() {
			@Override
			public boolean passes(int i) {
				return i > 10;
			}
		});
		ga.defineRule("b", gb);
		String s = ga.describe();
		assertTrue("tag copied", s.indexOf("foo") > -1);
	}

	@Test
	public void tagTest10() {
		Grammar ga = new Grammar("ROOT = 'quux' <b>");
		Grammar gb = new Grammar("ROOT = 'quux' !-/.?::/r");
		ga.defineRule("b", gb);
		String s = ga.describe();
		assertTrue("tag copied", s.indexOf("null") == -1);
	}

	@Test
	public void labelTest() {
		Grammar ga = new Grammar("foo = <a> 'bar'");
		Grammar gb = new Grammar("foo = 'quux'");
		ga.defineRule("a", gb);
		String s = "quuxbar";
		Match m = ga.matches(s).match();
		assertNotNull(m);
		assertTrue(m.children()[0].has("foo"));
	}

}
