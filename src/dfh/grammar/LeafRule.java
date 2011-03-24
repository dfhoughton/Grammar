package dfh.grammar;

import java.util.Map;
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
	private class LeafMatcher implements Matcher {
		private final Match parent;
		private final Integer offset;
		private final CharSequence chars;
		private Map<Integer, CachedMatch> cache;
		private boolean fresh = true;

		public LeafMatcher(CharSequence s, int offset, Match parent,
				Map<Label, Map<Integer, CachedMatch>> cache) {
			this.chars = s;
			this.parent = parent;
			this.offset = offset;
			this.cache = cache.get(label);
		}

		@Override
		public Match match() {
			fresh = false;
			CachedMatch cm = cache.get(offset);
			if (cm != null) {
				return cm.m;
			}
			java.util.regex.Matcher m = p.matcher(chars);
			m.region(offset, chars.length());
			m.useTransparentBounds(true);
			m.useAnchoringBounds(false);
			Match n = null;
			if (m.lookingAt())
				n = new Match(LeafRule.this, offset, m.end(), parent);
			cm = new CachedMatch(n);
			cache.put(offset, cm);
			return n;
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
		public String identify() {
			return label.toString();
		}
	}

	private static final long serialVersionUID = 1L;
	private final Pattern p;

	public LeafRule(Label label, Pattern p) {
		super(label);
		this.p = p;
	}

	@Override
	public Matcher matcher(CharSequence s, final Integer offset, Match parent,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new LeafMatcher(s, offset, parent, cache);
	}

	@Override
	protected String uniqueId() {
		return "(?:" + p.toString() + ")";
	}
}
