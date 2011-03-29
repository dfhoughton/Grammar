package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.Test;

/**
 * Make sure backreferences work as advertised.'
 * <p>
 * <b>Creation date:</b> Mar 29, 2011
 * 
 * @author David Houghton
 * 
 */
public class BackReferenceTest {

	@Test
	public void goodTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = (q) (text) 1",//
				"(q) =[\"']",//
				"(text) =\\w++",//
		};
		Grammar g = new Grammar(rules);
		String s = "'ned'";
		Matcher m = g.find(s);
		Match n = m.match();
		assertNotNull("found ned", n);
	}

	@Test
	public void badTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = (q) (text) 1",//
				"(q) =[\"']",//
				"(text) =\\w++",//
		};
		Grammar g = new Grammar(rules);
		String s = "'ned\"";
		Matcher m = g.find(s);
		Match n = m.match();
		assertNull("didn't find ned", n);
	}
}
