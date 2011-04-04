package dfh.grammar;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dfh.grammar.Grammar.ConstantOptions;

/**
 * The object implementing rules such as
 * 
 * <pre>
 * &lt;a&gt; = &lt;b&gt; | &lt;c&gt;
 * </pre>
 * 
 * @author David Houghton
 * 
 */
public class AlternationRule extends Rule {
	private static final long serialVersionUID = 1L;

	private class AlternationMatcher extends NonterminalMatcher {
		int index = 0;
		Matcher mostRecent = null;

		public AlternationMatcher(CharSequence cs, int offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(cs, offset, cache, AlternationRule.this, master);
		}

		@Override
		protected void fetchNext() {
			if (mostRecent == null) {
				mostRecent = alternates[index].matcher(s, offset, cache, this);
			}
			Match child = null;
			OUTER: while (mostRecent.mightHaveNext()
					|| index < alternates.length) {
				while (mostRecent.mightHaveNext()) {
					child = mostRecent.match();
					if (child != null)
						break OUTER;
				}
				if (++index == alternates.length)
					break;
				mostRecent = alternates[index].matcher(s, offset, cache, this);
			}
			if (child == null) {
				done = true;
				next = null;
			} else {
				next = new Match(AlternationRule.this, offset, child.end());
				Match[] children = new Match[] { child };
				next.setChildren(children);
			}
		}
	}

	protected final Rule[] alternates;

	/**
	 * Generates a rule from the given label and alternates.
	 * 
	 * @param label
	 * @param alternates
	 */
	public AlternationRule(Label label, Rule[] alternates) {
		super(label);
		this.alternates = alternates;
	}

	@Override
	public Matcher matcher(CharSequence cs, Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new AlternationMatcher(cs, offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		StringBuilder b = new StringBuilder();
		b.append('[');
		boolean nonInitial = false;
		for (Rule r : alternates) {
			if (nonInitial)
				b.append('|');
			else
				nonInitial = true;
			b.append(r.uniqueId());
		}
		b.append(']');
		return b.toString();
	}

	@Override
	public String description() {
		StringBuilder b = new StringBuilder();
		boolean nonInitial = false;
		for (Rule r : alternates) {
			if (nonInitial)
				b.append(" | ");
			else
				nonInitial = true;
			if (r.generation == -1) {
				b.append(r.description());
			} else
				b.append(r.label());
		}
		return b.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache,
			Set<Rule> studiedRules, ConstantOptions options) {
		Set<Integer> startOffsets = new HashSet<Integer>();
		for (Rule r : alternates)
			startOffsets.addAll(r.study(s, cache, studiedRules, options));
		return startOffsets;
	}

	@Override
	public boolean zeroWidth() {
		for (Rule r : alternates) {
			if (r.zeroWidth())
				return true;
		}
		return false;
	}

	@Override
	public Rule shallowClone() {
		AlternationRule ar = new AlternationRule((Label) label.clone(),
				Arrays.copyOf(alternates, alternates.length));
		return ar;
	}
}
