package dfh.grammar;

import java.util.Map;

import dfh.grammar.SequenceRule.SequenceMatcher;

/**
 * A non-reversible variant on {@link BackReferenceRule} that matches against a
 * match at a particular index in a rule several generations up the hierarchy.
 * It is signified in a rule definition by a caret after the backreference
 * index, like so,
 * 
 * <pre>
 * rule = [ 'foo' | 'bar' ] 'baz' [ 1^ | 'quux' ]
 * </pre>
 * 
 * This rule matches 'foobazfoo', 'barbazbar', 'foobazquux', and 'barbazquux',
 * but not 'foobazbar', 'barbazfoo', 'barbazcorge', etc. The caret indicates
 * that the index refers to the larger rule body, not the alternation
 * immediately dominating the index.
 * <p>
 * <b>Creation date:</b> Dec 9, 2011
 * 
 * @author David Houghton
 * 
 */
public class UpLevelBackReferenceRule extends BackReferenceRule {
	private static final long serialVersionUID = 1L;
	protected final int level;

	private class UpLevelBackReferenceMatcher extends Matcher {

		private boolean fresh = true;

		public UpLevelBackReferenceMatcher(CharSequence s, Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			super(s, offset, master);
		}

		@Override
		public Match match() {
			if (options.debug)
				UpLevelBackReferenceRule.this.matchTrace(this);
			if (fresh) {
				fresh = false;
				Matcher mm = master;
				for (int i = 0; i < level; i++)
					mm = mm.master;
				Match m = ((SequenceMatcher) mm).matched.get(index), n = null;
				int delta = m.end() - m.start();
				if (delta == 0) {
					// zero-width matches always match
					n = new Match(UpLevelBackReferenceRule.this, offset, offset);
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
							n = new Match(UpLevelBackReferenceRule.this,
									offset, end);
					}
				}
				if (options.debug)
					UpLevelBackReferenceRule.this.matchTrace(this, n);
				return register(n);
			}
			if (options.debug)
				UpLevelBackReferenceRule.this.matchTrace(this, null);
			return null;
		}

		@Override
		public boolean mightHaveNext() {
			return fresh;
		}

		@Override
		protected Rule rule() {
			return UpLevelBackReferenceRule.this;
		}
	}

	/**
	 * Generates the back reference rule using the given index.
	 * 
	 * @param label
	 * @param index
	 */
	public UpLevelBackReferenceRule(Label label, int index, int level) {
		super(label, index);
		this.level = level;
	}

	@Override
	public Matcher matcher(CharSequence s, Integer offset,
			Map<Integer, CachedMatch>[] cache, Matcher master) {
		return new UpLevelBackReferenceMatcher(s, offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		return Integer.toString(index + 1) + '^';
	}

	@Override
	public Rule shallowClone() {
		UpLevelBackReferenceRule brr = new UpLevelBackReferenceRule(
				(Label) label.clone(), index, level);
		return brr;
	}

}
