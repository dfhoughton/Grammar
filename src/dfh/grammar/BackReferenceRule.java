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

	private class BackReferenceMatcher implements Matcher {

		private final CharSequence s;
		private final int offset;
		private final Match parent;
		private final SequenceMatcher master;
		private boolean fresh = true;

		public BackReferenceMatcher(CharSequence s, int offset, Match parent,
				Map<Label, Map<Integer, Match>> cache, Matcher master) {
			this.s = s;
			this.offset = offset;
			this.parent = parent;
			this.master = (SequenceMatcher) master;
		}

		@Override
		public Match match() {
			if (fresh) {
				fresh = false;
				Match m = master.matched.get(index), n = null;
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
				return n;
			}
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
	public Matcher matcher(CharSequence s, int offset, Match parent,
			Map<Label, Map<Integer, Match>> cache, Matcher master) {
		return new BackReferenceMatcher(s, offset, parent, cache, master);
	}

	@Override
	protected String uniqueId() {
		return Integer.toString(index + 1);
	}

}
