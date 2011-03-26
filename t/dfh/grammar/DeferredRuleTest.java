package dfh.grammar;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Map;
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
		"<ROOT> = (q) (text) 1",//
		};
		Grammar g = new Grammar(rules);
		g.defineTerminal("q", Pattern.compile("[\"']"));
		g.defineTerminal("text", Pattern.compile("\\w++"));
		String s = "'ned'";
		Matcher m = g.find(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("found ned", n);
	}

	@Test
	public void arbitraryRuleTest() throws GrammarException, IOException {
		class ARule extends Rule {
			class AMatcher extends Matcher {
				private final Map<Integer, CachedMatch> cache;
				private boolean fresh = true;

				public AMatcher(CharSequence s, Integer offset, Match parent,
						Map<Label, Map<Integer, CachedMatch>> cache,
						Matcher master) {
					super(s, offset, parent, master);
					this.cache = cache.get(label);
				}

				@Override
				public Match match() {
					if (fresh) {
						fresh = false;
						CachedMatch cm = cache.get(offset);
						if (cm == null) {
							Match n = null;
							boolean found = false;
							int i = offset;
							for (; i < s.length(); i++) {
								if (Character.isLetterOrDigit(s.charAt(i)))
									found = true;
								else
									break;
							}
							if (found)
								n = new Match(ARule.this, offset, i, parent);
							cm = new CachedMatch(n);
							cache.put(offset, cm);
						}
						return cm.m;
					}
					return null;
				}

				@Override
				boolean mightHaveNext() {
					return fresh;
				}

				@Override
				Rule rule() {
					return ARule.this;
				}

			}

			private static final long serialVersionUID = 1L;

			ARule(Label label) {
				super(label);
			}

			@Override
			protected String uniqueId() {
				return label.toString();
			}

			@Override
			public Matcher matcher(CharSequence s, Integer offset,
					Match parent, Map<Label, Map<Integer, CachedMatch>> cache,
					Matcher master) {
				return new AMatcher(s, offset, parent, cache, master);
			}

			@Override
			public String description() {
				return "foo bar";
			}
		}
		String[] rules = {
		//
		"<ROOT> = (q) (text) 1",//
		};
		Grammar g = new Grammar(rules);
		g.defineTerminal("q", Pattern.compile("[\"']"));
		g.defineTerminal("text", new ARule(new Label(Type.terminal, "text")));
		String s = "'ned'";
		Matcher m = g.find(s);
		Match n = m.match();
		System.out.println(n);
		assertNotNull("found ned", n);
	}
}
