import java.io.IOException;

import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;
import dfh.grammar.Match;
import dfh.grammar.Matcher;

public class BackwardsAssertionExample {

	/**
	 * @param args
	 * @throws IOException
	 * @throws GrammarException
	 */
	public static void main(String[] args) throws GrammarException, IOException {
		String[] rules = {
				//
				"ROOT = !- [ <fred> /\\s++/r ] <fred>",//
				"fred = 'fred'",//
		};
		Grammar g = new Grammar(rules);
		String s = "fred fred bob";
		Matcher m = g.find(s);
		Match n;
		while ((n = m.match()) != null)
			System.out.println(n);
	}

}
