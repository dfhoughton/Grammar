package dfh.grammar;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.Test;

import dfh.grammar.Grammar.Options;
import dfh.grammar.Grammar.ConstantOptions;
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

	@Test
	public void arbitraryRuleTest() throws GrammarException, IOException {
		class ARule extends Rule implements Cloneable {
			class AMatcher extends Matcher {
				private final Map<Integer, CachedMatch> cache;
				private boolean fresh = true;

				public AMatcher(CharSequence s, Integer offset,
						Map<Label, Map<Integer, CachedMatch>> cache,
						Matcher master) {
					super(s, offset, master);
					this.cache = cache.get(label);
				}

				@Override
				public Match match() {
					if (fresh) {
						fresh = false;
						CachedMatch cm = cache.get(offset);
						if (cm == null) {
							boolean found = false;
							int i = offset;
							for (; i < s.length(); i++) {
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
					Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
				return new AMatcher(s, offset, cache, master);
			}

			@Override
			public String description() {
				return "foo bar";
			}

			@Override
			public Set<Integer> study(CharSequence s,
					Map<Label, Map<Integer, CachedMatch>> cache,
					Set<Rule> studiedRules, ConstantOptions options) {
				// we won't study for this
				return null;
			}

			@Override
			public boolean zeroWidth() {
				return false;
			}

			@Override
			public Rule shallowClone() {
				return this;
			}

			@Override
			public Object clone() {
				return this;
			}
		}
		String[] rules = {
		//
		"<ROOT> = <q> <text> 1",//
		};
		Grammar g = new Grammar(rules);
		g.defineRule("q", Pattern.compile("[\"']"));
		g.defineRule("text", new ARule(new Label(Type.terminal, "text")));
		String s = "'ned'";
		Options options = new Options();
		options.study = false;
		Matcher m = g.find(s, options);
		Match n = m.match();
		assertNotNull("found ned", n);
	}
}
