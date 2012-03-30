package dfh.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import dfh.grammar.tokens.StringTagTest;
import dfh.grammar.tokens.StringTaggedToken;
import dfh.grammar.tokens.TagTest;
import dfh.grammar.tokens.TaggedToken;
import dfh.grammar.tokens.TokenSequence;

/**
 * Makes sure the token handling classes work as expected.
 * <p>
 * 
 * @author David F. Houghton - Mar 30, 2012
 * 
 */
public class TokenizationTest {
	private enum Tags {
		s, a, b, c
	}

	private static Set<String> stringTags = new HashSet<String>();
	static {
		for (String s : new String[] { "s", "a", "b", "c" })
			stringTags.add(s);
	}

	private static TokenSequence<TaggedToken<Tags>> seq;
	private static TokenSequence<StringTaggedToken> seq2;
	private static Map<String, Rule> ruleMap;

	@BeforeClass
	public static void tokenize() {
		String s = "a b a c c a";
		Pattern p = Pattern.compile("[abc]|\\s++");
		List<TaggedToken<Tags>> tokens = new ArrayList<TaggedToken<Tags>>();
		List<StringTaggedToken> tokens2 = new ArrayList<StringTaggedToken>();
		java.util.regex.Matcher m = p.matcher(s);
		while (m.find()) {
			String tok = m.group();
			Tags t;
			String t2;
			if (Character.isWhitespace(tok.charAt(0)))
				t = Tags.s;
			else
				t = Tags.valueOf(tok);
			t2 = t.name();
			tokens.add(new TaggedToken<Tags>(m.start(), m.end(), t));
			tokens2.add(new StringTaggedToken(m.start(), m.end(), t2));
		}
		seq = new TokenSequence<TaggedToken<Tags>>(s, tokens);
		seq2 = new TokenSequence<StringTaggedToken>(s, tokens2);
		ruleMap = TagTest.precompile(Tags.class);
	}

	@Test
	public void test1() {
		Grammar g = new Grammar("ROOT = <c> <s> <c>", ruleMap);
		Matcher m = g.find(seq);
		Match n = m.match();
		assertNotNull(n);
		assertEquals(6, n.start());
		assertEquals(9, n.end());
	}

	@Test
	public void reversalWithoutStudying() {
		Grammar g = new Grammar("ROOT = notafter [ <a> <s>? ] <c>", ruleMap);
		Matcher m = g.find(seq, new Options().study(false));
		Match n = m.match();
		assertNotNull(n);
		assertEquals(8, n.start());
		assertEquals(9, n.end());
	}

	@Test
	public void reversalWithStudying() {
		Grammar g = new Grammar("ROOT = notafter [ <a> <s>? ] <c>", ruleMap);
		Matcher m = g.find(seq, new Options().study(true));
		Match n = m.match();
		assertNotNull(n);
		assertEquals(8, n.start());
		assertEquals(9, n.end());
	}

	@Test
	public void stringTaggedTokens() {
		Grammar g = new Grammar("ROOT = <c> <s> <c>",
				StringTagTest.precompile(stringTags));
		Matcher m = g.find(seq2);
		Match n = m.match();
		assertNotNull(n);
		assertEquals(6, n.start());
		assertEquals(9, n.end());
	}

}
