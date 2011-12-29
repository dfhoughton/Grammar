package dfh.grammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dfh.grammar.Label.Type;

/**
 * Implements a backtracking barrier. See {@link BarrierFragment}.
 * <p>
 * <b>Creation date:</b> Apr 6, 2011
 * 
 * @author David Houghton
 * 
 */
@Reversible
public class BacktrackingBarrier extends Rule {
	private static final long serialVersionUID = 2L;
	/**
	 * {@link Label} used by all : barriers.
	 */
	public static final Label SINGLE_COLON_LABEL = new Label(Type.nonTerminal,
			":");
	/**
	 * {@link Label} used by all :: barriers.
	 */
	public static final Label DOUBLE_COLON_LABEL = new Label(Type.nonTerminal,
			"::");
	/**
	 * Whether this is a single or double colon barrier.
	 */
	protected final boolean isSingle;

	public BacktrackingBarrier(boolean isSingle) {
		super(isSingle ? SINGLE_COLON_LABEL : DOUBLE_COLON_LABEL);
		this.isSingle = isSingle;
	}

	private class BarrierMatcher extends Matcher {

		private boolean fresh = true;

		protected BarrierMatcher(CharSequence s, Integer offset, Matcher master) {
			super(s, offset, master);
		}

		@Override
		public Match match() {
			if (fresh) {
				fresh = false;
				return register(new Match(BacktrackingBarrier.this, offset,
						offset));
			}
			throwBarrierException();
			return null;
		}

		private void throwBarrierException() {
			rule().event(this, "hit backtracking barrier");
			if (isSingle)
				throw new SingleColonBarrier(this);
			throw new DoubleColonBarrier(this);
		}

		@Override
		protected boolean mightHaveNext() {
			if (fresh)
				return true;
			throwBarrierException();
			return fresh;
		}

		@Override
		protected Rule rule() {
			return BacktrackingBarrier.this;
		}

	}

	@Override
	public Matcher matcher(CharSequence s, Integer offset,
			Map<Integer, CachedMatch>[] cache, Matcher master) {
		return new BarrierMatcher(s, offset, master);
	}

	@Override
	protected String uniqueId() {
		return label.id;
	}

	@Override
	public String description(boolean inBrackets) {
		return label.id;
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, Set<Rule> studiedRules,
			GlobalState options) {
		// one cannot study barriers
		return new HashSet<Integer>(0);
	}

	@Override
	public boolean zeroWidth() {
		return true;
	}

	@Override
	public Rule shallowClone() {
		return new BacktrackingBarrier(isSingle);
	}
}
