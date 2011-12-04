import java.io.IOException;

import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;
import dfh.grammar.IntegerCondition;
import dfh.grammar.Match;
import dfh.grammar.Matcher;

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
		g.defineCondition("less_than_100", new IntegerCondition() {
			@Override
			public boolean passes(int i) {
				return i < 100;
			}
		});
		String s = "99 100 1000";
		Matcher m = g.find(s);
		Match n;
		while ((n = m.match()) != null)
			System.out.println(n.group());
	}

}
