import java.io.IOException;

import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;
import dfh.grammar.Match;

/**
 * Find a {@link Match} and stringify it.
 * <p>
 * <b>Creation date:</b> Nov 28, 2011
 * 
 * @author David Houghton
 * 
 */
public class ToString {

	/**
	 * @param args
	 *            none
	 * @throws IOException
	 * @throws GrammarException
	 */
	public static void main(String[] args) throws GrammarException, IOException {
		String[] rules;
		Grammar g;
		Match m;

		rules = new String[] {
				//
				"ROOT = <foo>",//
				"foo = 'f' 'oo'",//
		};
		g = new Grammar(rules);
		m = g.find("foo barquux foofoofoo quuxbar").match();
		System.out.println(m);

		rules = new String[] {
				//
				"ROOT = <foo> /\\s++/ [ <bar> | <quux> ]",//
				"foo = 'foo'{1,2}",//
				"bar = 'bar' ! 'quux'",//
				"quux = 'quux' ~ 'bar'",//
		};
		g = new Grammar(rules);
		m = g.find("foo barquux foofoofoo quuxbar").match();
		System.out.println(m);
	}

}
