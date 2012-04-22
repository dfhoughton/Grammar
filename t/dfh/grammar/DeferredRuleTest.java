package dfh.grammar;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import dfh.grammar.Label.Type;

/**
 * Makes sure we can define rules after {@link Grammar} compilation.
 * <p>
 * <b>Creation date:</b> Mar 23, 2011
 * 
 * @author David Houghton
 * 
 */
public class DeferredRuleTest {

	@Test
	public void goodTest() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = <q> <text> 1",//
		};
		Grammar g = new Grammar(rules);
		g.defineRule("q", Pattern.compile("[\"']"));
		g.defineRule("text", Pattern.compile("\\w++"));
		String s = "'ned'";
		Matcher m = g.find(s);
		Match n = m.match();
		assertNotNull("found ned", n);
	}

	public static class ARule extends Rule implements Cloneable {
		class AMatcher extends Matcher {
			private final Map<Integer, CachedMatch> cache;
			private boolean fresh = true;

			public AMatcher(Integer offset, Map<Integer, CachedMatch>[] cache,
					Matcher master) {
				super(offset, master);
				this.cache = cache[cacheIndex];
			}

			@Override
			public Match match() {
				if (fresh) {
					fresh = false;
					CachedMatch cm = cache.get(offset);
					if (cm == null) {
						boolean found = false;
						int i = offset;
						for (; i < options.end(); i++) {
							if (Character.isLetterOrDigit(s.charAt(i)))
								found = true;
							else
								break;
						}
						if (found) {
							Match n = new Match(ARule.this, offset, i);
							cm = new CachedMatch(n);
						} else
							cm = CachedMatch.MISMATCH;
						cache.put(offset, cm);
					}
					return cm.m;
				}
				return null;
			}

			@Override
			protected boolean mightHaveNext() {
				return fresh;
			}

			@Override
			protected Rule rule() {
				return ARule.this;
			}

		}

		ARule(Label label) {
			super(label);
		}

		@Override
		protected String uniqueId() {
			return label.toString();
		}

		@Override
		public Matcher matcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			return new AMatcher(offset, cache, master);
		}

		@Override
		public String description(boolean inBrackets) {
			return "foo bar";
		}

		@Override
		public Set<Integer> study(CharSequence s,
				Map<Integer, CachedMatch>[] cache, GlobalState options) {
			// we won't study for this
			return null;
		}

		@Override
		public boolean zeroWidth() {
			return false;
		}

		@Override
		public Object clone() {
			return this;
		}

		@Override
		protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
			return false;
		}
	}

	@Test
	public void arbitraryRuleTest1() throws GrammarException, IOException {

		String[] rules = {
		//
		"<ROOT> = <q> <text> 1",//
		};
		Grammar g = new Grammar(rules);
		g.defineRule("q", Pattern.compile("[\"']"));
		g.defineRule("text", new ARule(new Label(Type.explicit, "text")));
		String s = "'ned'";
		Options options = new Options();
		options.study(false);
		Matcher m = g.find(s, options);
		Match n = m.match();
		assertNotNull("found ned", n);
	}

	@Test
	public void arbitraryRuleTest2() throws GrammarException, IOException {

		String[] rules = {
		//
		"<ROOT> = <q> <text> 1",//
		};
		Grammar g = new Grammar(rules);
		g.defineRule("q", Pattern.compile("[\"']"));
		g.defineRule("text", new ARule(new Label(Type.explicit, "foo")));
		String s = "'ned'";
		Options options = new Options();
		options.study(false);
		Matcher m = g.find(s, options);
		Match n = m.match();
		assertNotNull("found ned", n);
	}

	@Test
	public void literalDeferredRuleTest() throws GrammarException, IOException {

		String[] rules = {
		//
		"<ROOT> = <q> <text> 1",//
		};
		Grammar g = new Grammar(rules);
		g.defineRule("q", Pattern.compile("[\"']"));
		g.defineRule("text", "text");
		String s = "'text'";
		Options options = new Options();
		options.study(false);
		Matcher m = g.find(s, options);
		Match n = m.match();
		assertNotNull("found text", n);
	}

}
