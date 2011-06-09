package dfh.grammar;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

/**
 * Tests compilation and matching of in-line regular expressions.
 * 
 * <b>Creation date:</b> Jun 9, 2011
 * 
 * @author David Houghton
 * 
 */
public class InlineRegexTest {

	@Test
	public void basicTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> /\\s++/ <b>",//
				"<a> = 'a'",//
				"<b> = 'b'",//
		};
		Grammar g = new Grammar(rules);
		String s = "a  b";
		Matcher m = g.matches(s);
		assertNotNull(m.match());
	}
	
	@Test
	public void test2() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> /g++/i <b>",//
				"<a> = 'a'",//
				"<b> = 'b'",//
		};
		Grammar g = new Grammar(rules);
		String s = "agGb";
		Matcher m = g.matches(s);
		assertNotNull(m.match());
	}

	
	@Test
	public void test3() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> /\\s++/ <b> /foo/ #comment",//
				"<a> = 'a'",//
				"<b> = 'b'",//
		};
		Grammar g = new Grammar(rules);
		String s = "a  bfoo";
		Matcher m = g.matches(s);
		assertNotNull(m.match());
	}

}
