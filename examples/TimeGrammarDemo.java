import java.io.IOException;
import java.io.InputStream;

import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;

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
//		g.setTrace(System.out);
		String[] phrases = {
				//
				"yesterday",//
				"last Friday at 4:00 am",//
				"February 16",//
				"last week",//
				"Tuesday",//
				"2011/4/24",//
		};
		for (String phrase : phrases) {
			System.out.println(g.matches(phrase).match());
		}
	}

}
