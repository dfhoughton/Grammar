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
		System.out.println(g1.describe());
		String s = "a";
		Matcher m = g1.find(s);
		assertNotNull("found joe", m.match());
	}
}
