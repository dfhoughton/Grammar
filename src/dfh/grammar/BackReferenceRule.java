package dfh.grammar;

import java.util.Map;

import dfh.grammar.SequenceRule.SequenceMatcher;

/**
 * Checks a back reference. Note that back reference matching can't be cached.
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
				Match parent, Map<Label, Map<Integer, CachedMatch>> cache,
				Matcher master) {
			super(s, offset, parent, master);
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
					n = new Match(BackReferenceRule.this, offset, parent);
					n.setEnd(offset);
				} else {
					int end = offset + delta;
					if (end <= s.length()) {
						boolean matched = true;
						for (int i = offset, j = m.start(); i < end; i++, j++) {
							if (s.charAt(i) != s.charAt(j)) {
								matched = false;
								break;
							}
						}
						if (matched) {
							n = new Match(BackReferenceRule.this, offset,
									parent);
							n.setEnd(end);
						}
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
		public String identify() {
			return label.toString();
		}
	}

	public BackReferenceRule(Label label, int index) {
		super(label);
		this.index = index;
	}

	@Override
	public Matcher matcher(CharSequence s, Integer offset, Match parent,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new BackReferenceMatcher(s, offset, parent, cache, master);
	}

	@Override
	protected String uniqueId() {
		return Integer.toString(index + 1);
	}

}
