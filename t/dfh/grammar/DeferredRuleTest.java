package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Makes sure we can define regexes after {@link Grammar} compilation.
 * <p>
 * <b>Creation date:</b> Mar 23, 2011
 * 
 * @author David Houghton
 * 
 */
public class DeferredRuleTest {

	@Test
	public void goodTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = (q) (text) 1",//
		};
		Grammar g = new Grammar(rules);
		g.defineTerminal("q", Pattern.compile("[\"']"));
		g.defineTerminal("text", Pattern.compile("\\w++"));
		String s = "'ned'";
		Matcher m = g.find(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("found ned", n);
	}
}
