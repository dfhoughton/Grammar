
import java.io.IOException;
import java.io.InputStream;

import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;

/**
 * This code implements almost the entire grammar used by <a
 * href="http://search.cpan.org/search?m=module&q=App%3A%3AJobLog&s=21">
 * <code>App::JobLog</code></a>. All that is leaves out is the patterns which
 * refer explicitly to the log.
 * <p>
 * <b>Creation date:</b> Mar 24, 2011
 * 
 * @author David Houghton
 * 
 */
public class TimeGrammarDemo {

	/**
	 * @param args
	 * @throws IOException
	 * @throws GrammarException
	 */
	public static void main(String[] args) throws GrammarException, IOException {
		InputStream is = TimeGrammarDemo.class.getClassLoader()
				.getResourceAsStream("time.txt");
		Grammar g = new Grammar(is);
		String[] phrases = {
				//
				"yesterday",//
				"last Friday at 4:00 am",//
				"February 16",//
				"last week",//
				"Tuesday",//
				"2011/4/24",//
				"5/6/1969",//
		};
		for (String phrase : phrases) {
			System.out.println(phrase);
			System.out.println(g.matches(phrase).match());
			System.out.println();
		}
		System.out.println(g.describe());
	}

}
