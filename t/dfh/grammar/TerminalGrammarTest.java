package dfh.grammar;

import static org.junit.Assert.assertNotNull;

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
		System.out.println(g1.describe());
		Grammar gA = new Grammar(rulesA);
		System.out.println(gA.describe());
		Grammar gB = new Grammar(rulesB);
		System.out.println(gB.describe());
		g1.defineRule("a", gA);
		g1.defineRule("b", gB);
		System.out.println(g1.describe());
		String s = "aab";
		Matcher m = g1.find(s);
		assertNotNull("found joe", m.match());
	}
}
