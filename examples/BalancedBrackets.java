import dfh.grammar.Condition;
import dfh.grammar.Grammar;
import dfh.grammar.Match;
import dfh.grammar.Matcher;
import dfh.grammar.Options;

/**
 * Illustrates how to match balanced brackets.
 * 
 * @author David Houghton
 */
public class BalancedBrackets {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		parenthesisExample();
		System.out.println();
		medleyExample();
	}

	/**
	 * Simple example.
	 */
	private static void parenthesisExample() {
		Grammar g = new Grammar("ROOT = '(' [ /[^()]++/ | <ROOT> ]*+ ')'");
		System.out.println("balanced parentheses:\n");
		System.out.println(g.describe());
		String s = "this (is (an example))  isn't (it)";
		System.out.println("string: " + s);
		System.out.println("\nmatches:\n");
		Matcher m = g.find(s, new Options().study(true).allowOverlap(true));
		Match n;
		while ((n = m.match()) != null)
			System.out.println(n.group());
	}

	/**
	 * More thorough example with escaping.
	 */
	private static void medleyExample() {
		Grammar g = new Grammar(
				new String[] {
						//
						"ROOT = <parens> | <square> | <curly> | <angled>",//
						"parens = not after <escape> '(' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ ')'",//
						"square = not after <escape> '[' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ ']'",//
						"curly = not after <escape> '{' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ '}'",//
						"angled = not after <escape> '<' [ /(?:\\\\.|[^()\\[\\]{}<>])++/ | <ROOT> ]*+ '>'",//
						"escape = /(?<!\\\\)(?:\\\\)++/r (odd)",//
				});
		g.defineCondition("odd", new Condition() {
			@Override
			public boolean passes(CharSequence subsequence) {
				return subsequence.length() % 2 == 1;
			}
		});
		System.out.println("balanced brackets of various sorts:\n");
		System.out.println(g.describe());
		String s = "this \\\\(is \\{ <an example\\>>)  { [ isn't ( \\) [{it}]) \\( testing escapes)";
		System.out.println("string: " + s);
		System.out.println("\nmatches:\n");
		Matcher m = g.find(s, new Options().study(true).allowOverlap(true));
		Match n;
		while ((n = m.match()) != null)
			System.out.println(n.group());
	}

}
