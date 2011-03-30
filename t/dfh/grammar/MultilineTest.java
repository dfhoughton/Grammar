package dfh.grammar;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

/**
 * Makes sure line endings are working correctly.
 * <p>
 * <b>Creation date:</b> Mar 29, 2011
 * 
 * @author David Houghton
 * 
 */
public class MultilineTest {

	@Test
	public void basicTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> (s) <b>",//
				"<a> = 'a'",//
				"<b> = 'b'",//
				"(s) =\\s++",//
		};
		Grammar g = new Grammar(rules);
		String s = "a\nb";
		Matcher m = g.find(s);
		assertNotNull("found joe", m.match());
	}
}
