package dfh.grammar;

import java.util.Map;

/**
 * For matching a rule literal. E.g., <fred> = "Fred".
 * <p>
 * <b>Creation date:</b> Mar 22, 2011
 * 
 * @author David Houghton
 * 
 */
public class LiteralRule extends Rule {
	private class LiteralMatcher extends Matcher {
		private final Map<Integer, CachedMatch> cache;
		private boolean fresh = true;

		public LiteralMatcher(CharSequence s, Integer offset, Match parent,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(s, offset, parent, master);
			this.cache = cache.get(label);
		}

		@Override
		public Match match() {
			LiteralRule.this.matchTrace(this);
			if (fresh) {
				fresh = false;
				CachedMatch cm = cache.get(offset);
				if (cm != null) {
					LiteralRule.this.matchTrace(this, cm.m);
					return cm.m;
				}
				Match m = null;
				int end = offset + literal.length();
				if (end <= s.length()) {
					boolean matched = true;
					for (int i = offset, j = 0; i < end; i++, j++) {
						if (literal.charAt(j) != s.charAt(i)) {
							matched = false;
							break;
						}
					}
					if (matched) {
						m = new Match(LiteralRule.this, offset, parent);
						m.setEnd(end);
					}
				}
				cm = new CachedMatch(m);
				cache.put(offset, cm);
				LiteralRule.this.matchTrace(this, m);
				return m;
			}
			LiteralRule.this.matchTrace(this, null);
			return null;
		}

		@Override
		public boolean mightHaveNext() {
			return fresh;
		}

		@Override
		public String identify() {
			return '"' + literal + '"';
		}

		@Override
		public String toString() {
			return identify();
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String literal;

	public LiteralRule(Label label, String literal) {
		super(label);
		this.literal = literal;
	}

	@Override
	public Matcher matcher(CharSequence s, Integer offset, Match parent,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new LiteralMatcher(s, offset, parent, cache, master);
	}

	@Override
	protected String uniqueId() {
		return '"' + literal + '"';
	}

}
