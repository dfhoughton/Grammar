package dfh.grammar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.junit.BeforeClass;
import org.junit.Test;

import dfh.grammar.tokens.TagTest;
import dfh.grammar.tokens.TaggedToken;
import dfh.grammar.tokens.TokenRule;
import dfh.grammar.tokens.TokenSequence;

public class TokenizationTest {
	private enum Tags {
		s, a, b, c
	}

	private static TokenSequence<TaggedToken<Tags>> seq;
	private static Map<String, Rule> ruleMap;

	@BeforeClass
	public static void tokenize() {
		String s = "a b a c c a";
		Pattern p = Pattern.compile("[abc]|\\s++");
		List<TaggedToken<Tags>> tokens = new ArrayList<TaggedToken<Tags>>();
		java.util.regex.Matcher m = p.matcher(s);
		while (m.find()) {
			String tok = m.group();
			Tags t;
			if (Character.isWhitespace(tok.charAt(0)))
				t = Tags.s;
			else
				t = Tags.valueOf(tok);
			tokens.add(new TaggedToken<TokenizationTest.Tags>(m.start(), m
					.end(), t));
		}
		seq = new TokenSequence<TaggedToken<Tags>>(s, tokens);
		ruleMap = new TreeMap<String, Rule>();
		ruleMap.put("s", new TokenRule<TaggedToken<Tags>>(
				new TagTest<TokenizationTest.Tags>(Tags.s)));
		ruleMap.put("a", new TokenRule<TaggedToken<Tags>>(
				new TagTest<TokenizationTest.Tags>(Tags.a)));
		ruleMap.put("b", new TokenRule<TaggedToken<Tags>>(
				new TagTest<TokenizationTest.Tags>(Tags.b)));
		ruleMap.put("c", new TokenRule<TaggedToken<Tags>>(
				new TagTest<TokenizationTest.Tags>(Tags.c)));
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
		Matcher m = g.find(seq, new Options().study(true).log(System.out));
		Match n = m.match();
		assertNotNull(n);
		assertEquals(8, n.start());
		assertEquals(9, n.end());
	}

}
