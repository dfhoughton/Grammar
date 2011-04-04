package dfh.grammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dfh.grammar.Grammar.ConstantOptions;
import dfh.grammar.SequenceRule.SequenceMatcher;

/**
 * The object implementing rules such as
 * 
 * <pre>
 * &lt;a&gt; = &lt;b&gt; 'foo' 1
 * </pre>
 * 
 * where 1 in this case refers back to whatever string was matched by
 * <code>&lt;b&gt;</code>. Note that back reference matching can't easily be
 * cached because it isn't truly context free. A given back reference may or may
 * not match at a given offset depending on the rule of which it is a component.
 * <p>
 * <b>Creation date:</b> Mar 22, 2011
 * 
 * @author David Houghton
 * 
 */
public class BackReferenceRule extends Rule {
	private static final long serialVersionUID = 1L;
	private final int index;

	private class BackReferenceMatcher extends Matcher {

		private boolean fresh = true;

		public BackReferenceMatcher(CharSequence s, Integer offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(s, offset, master);
		}

		@Override
		public Match match() {
			BackReferenceRule.this.matchTrace(this);
			if (fresh) {
				fresh = false;
				Match m = ((SequenceMatcher) master).matched.get(index), n = null;
				int delta = m.end() - m.start();
				if (delta == 0) {
					// zero-width matches always match
					n = new Match(BackReferenceRule.this, offset, offset);
				} else {
					int end = offset + delta;
					if (end <= options.end) {
						boolean matched = true;
						for (int i = offset, j = m.start(); i < end; i++, j++) {
							if (s.charAt(i) != s.charAt(j)) {
								matched = false;
								break;
							}
						}
						if (matched)
							n = new Match(BackReferenceRule.this, offset, end);
					}
				}
				BackReferenceRule.this.matchTrace(this, n);
				return n;
			}
			BackReferenceRule.this.matchTrace(this, null);
			return null;
		}

		@Override
		public boolean mightHaveNext() {
			return fresh;
		}

		@Override
		protected Rule rule() {
			return BackReferenceRule.this;
		}
	}

	/**
	 * Generates the back reference rule using the given index.
	 * 
	 * @param label
	 * @param index
	 */
	public BackReferenceRule(Label label, int index) {
		super(label);
		this.index = index;
	}

	@Override
	public Matcher matcher(CharSequence s, Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new BackReferenceMatcher(s, offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		return Integer.toString(index + 1);
	}

	@Override
	public String description() {
		return label.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache,
			Set<Rule> studiedRules, ConstantOptions options) {
		// one cannot study backreferences
		return new HashSet<Integer>(0);
	}

	@Override
	public boolean zeroWidth() {
		return true;
	}

	@Override
	public Rule shallowClone() {
		BackReferenceRule brr = new BackReferenceRule((Label) label.clone(),
				index);
		return brr;
	}

}
