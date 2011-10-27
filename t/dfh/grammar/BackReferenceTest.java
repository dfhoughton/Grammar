package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
	public void simpleTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = [ '-' | '_' ] 'foo' 1",//
		};
		Grammar g = new Grammar(rules);
		String s = "-foo- _foo_ -foo_ _foo-";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertTrue("found all foo", count == 2);
	}

	@Test
	public void goodTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <q> <text> 1",//
				"<q> = /[\"']/",//
				"<text> = /\\w++/",//
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
				"<ROOT> = <q> <text> 1",//
				"<q> = /[\"']/",//
				"<text> = /\\w++/",//
		};
		Grammar g = new Grammar(rules);
		String s = "'ned\"";
		Matcher m = g.find(s);
		Match n = m.match();
		assertNull("didn't find ned", n);
	}
}
