package dfh.grammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

		public LiteralMatcher(CharSequence s, Integer offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(s, offset, master);
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
					if (matched)
						m = new Match(LiteralRule.this, offset, end);
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
		public String toString() {
			return '"' + literal + '"';
		}

		@Override
		Rule rule() {
			return LiteralRule.this;
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
	public Matcher matcher(CharSequence s, Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new LiteralMatcher(s, offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		return '"' + literal + '"';
	}

	@Override
	public String description() {
		boolean b1 = literal.indexOf('\\') > -1;
		boolean b2 = literal.indexOf('"') > -1;
		if (b1 || b2) {
			String s = literal;
			if (b2)
				s = '\'' + s.replaceAll("([\\\\'])", "\\\\$1") + '\'';
			else
				s = '"' + s.replaceAll("([\\\\])", "\\\\$1") + '"';
			return s;
		}
		return uniqueId();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache, int offset,
			Set<Rule> studiedRules) {
		studiedRules.add(this);
		Map<Integer, CachedMatch> subCache = cache.get(label);
		Set<Integer> startOffsets = new HashSet<Integer>();
		if (subCache.isEmpty()) {
			int index, o = 0;
			String string = s.subSequence(offset, s.length()).toString();
			while ((index = string.indexOf(literal, o)) > -1) {
				Integer i = index + offset;
				Match n = new Match(this, i, i + literal.length());
				subCache.put(i, new CachedMatch(n));
				startOffsets.add(i);
				o = index + 1;
			}
		} else {
			startOffsets.addAll(subCache.keySet());
		}
		return startOffsets;
	}

	@Override
	public boolean zeroWidth() {
		return false;
	}
}
