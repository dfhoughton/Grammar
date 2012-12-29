import java.io.File;
import java.io.IOException;

import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;

/**
 * A snippet of code useful for debugging grammars.
 * <p>
 * 
 * @author David F. Houghton - Dec 20, 2012
 * 
 */
public class Winnow {

	public static void main(String[] args) throws GrammarException, IOException {
		String problem = "by Fred, the director";
		Grammar g = new Grammar(new File("examples/debug_grammar.txt"));
		System.out.println(g.describe());
		System.out.println("# success? " + test(problem, g));
	}

	/**
	 * replace this with the appropriate test
	 */
	private static Object test(String problem, Grammar g) {
		return g.matches(problem).match() != null;
	}

}
