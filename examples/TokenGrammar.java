import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import dfh.grammar.Grammar;
import dfh.grammar.Match;
import dfh.grammar.Matcher;
import dfh.grammar.Options;
import dfh.grammar.Rule;
import dfh.grammar.tokens.TagTest;
import dfh.grammar.tokens.TaggedToken;
import dfh.grammar.tokens.TokenSequence;

/**
 * A class illustrating how to write grammars over sequences other than, or more
 * than, character sequences.
 * <p>
 * 
 * @author David F. Houghton - Mar 31, 2012
 * 
 */
public class TokenGrammar {
	/**
	 * Some annotations. Imagine these stand for "adjective", "noun", "verb",
	 * "determiner", and "space"
	 */
	enum Types {
		n, a, v, d,
		/**
		 * whitespace
		 */
		s
	};

	/**
	 * Rules to handle tokens with these annotations.
	 */
	static Map<String, Rule> rules = TagTest.precompile(Types.class);
	/**
	 * The specific patterns we will look for.
	 */
	static Grammar g = new Grammar(
			"phrase = [ <a> <s> ]* <n> | <v> [ <s> <n> ]?", rules);

	/**
	 * Code to annotated a character sequence.
	 * 
	 * @param s
	 * @return annotations
	 */
	static Collection<TaggedToken<Types>> tokenize(String s) {
		Pattern p = Pattern.compile("[navd]|\\s++");
		java.util.regex.Matcher m = p.matcher(s);
		List<TaggedToken<Types>> list = new ArrayList<TaggedToken<Types>>();
		while (m.find()) {
			String tok = m.group();
			Types t;
			int start = m.start(), end = m.end();
			if (Character.isWhitespace(tok.charAt(0)))
				t = Types.s;
			else
				t = Types.valueOf(tok);
			list.add(new TaggedToken<Types>(start, end, t));

		}
		return list;
	}

	public static void main(String[] args) {
		String s = "d n v n a a n";
		TokenSequence<TaggedToken<Types>> seq = new TokenSequence<TaggedToken<Types>>(
				s, tokenize(s));
		Matcher m = g.find(seq, new Options().allowOverlap(true));
		Match n;
		while ((n = m.match()) != null)
			System.out.printf("[%d, %d] %s%n", n.start(), n.end(), n.group());
	}
}
