import java.io.IOException;

import dfh.grammar.Condition;
import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;
import dfh.grammar.Match;
import dfh.grammar.Matcher;
import dfh.grammar.Options;


public class ConditionExample {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws GrammarException 
	 */
	@SuppressWarnings("serial")
	public static void main(String[] args) throws GrammarException, IOException {
		String[] rules = {
		//
		"ROOT = /\\b\\d++\\b/ (less_than_100)",//
		};
		Grammar g = new Grammar(rules);
		g.defineCondition("less_than_100", new Condition() {
			@Override
			public boolean passes(Match m, Matcher n, CharSequence s) {
				int i = Integer.parseInt(s.subSequence(m.start(), m.end())
						.toString());
				return i < 100;
			}
		});
		String s = "99 100 1000";
		Matcher m = g.find(s);
		Match n;
		while ((n = m.match()) != null)
			System.out.println(s.substring(n.start(), n.end()));
	}

}
