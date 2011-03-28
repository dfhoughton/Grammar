package dfh.grammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * {@link Rule} defined over sequence of terminal objects rather than other
 * <code>Rules</code>.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class LeafRule extends Rule {
	private class LeafMatcher extends Matcher {
		private Map<Integer, CachedMatch> cache;
		private boolean fresh = true;

		public LeafMatcher(CharSequence s, int offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(s, offset, master);
			this.cache = cache.get(label);
		}

		@Override
		public Match match() {
			LeafRule.this.matchTrace(this);
			if (fresh) {
				fresh = false;
				CachedMatch cm = cache.get(offset);
				if (cm != null) {
					LeafRule.this.matchTrace(this, cm.m);
					return cm.m;
				}
				Match n = null;
				java.util.regex.Matcher m = p.matcher(s);
				m.region(offset, s.length());
				m.useTransparentBounds(true);
				m.useAnchoringBounds(false);
				if (m.lookingAt())
					n = new Match(LeafRule.this, offset, m.end());
				cm = new CachedMatch(n);
				cache.put(offset, cm);
				LeafRule.this.matchTrace(this, n);
				return n;
			}
			LeafRule.this.matchTrace(this, null);
			return null;
		}

		@Override
		public boolean mightHaveNext() {
			return fresh;
		}

		@Override
		public String toString() {
			return "M:" + LeafRule.this;
		}

		@Override
		Rule rule() {
			return LeafRule.this;
		}
	}

	private static final long serialVersionUID = 1L;
	private final Pattern p;

	public LeafRule(Label label, Pattern p) {
		super(label);
		this.p = p;
	}

	@Override
	public Matcher matcher(CharSequence s, final Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new LeafMatcher(s, offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		return "(?:" + p.toString() + ")";
	}

	@Override
	public String description() {
		return p.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache, int offset,
			Set<Rule> studiedRules) {
		studiedRules.add(this);
		Map<Integer, CachedMatch> subCache = cache.get(label);
		Set<Integer> startOffsets = new HashSet<Integer>();
		if (!subCache.keySet().isEmpty()) {
			startOffsets.addAll(subCache.keySet());
		} else {
			java.util.regex.Matcher m = p.matcher(s);
			m.useAnchoringBounds(false);
			m.useTransparentBounds(true);
			m.region(offset, s.length());
			while (m.find()) {
				Integer i = m.start();
				startOffsets.add(i);
				Match n = new Match(this, m.start(), m.end());
				subCache.put(i, new CachedMatch(n));
				m.region(m.start() + 1, s.length());
			}
		}
		return startOffsets;
	}
}
