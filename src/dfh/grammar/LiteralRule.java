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
@Reversible
public class LiteralRule extends Rule {
	private class LiteralMatcher extends Matcher {
		private final Map<Integer, CachedMatch> cache;
		private boolean fresh = true;

		public LiteralMatcher(CharSequence s, Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			super(s, offset, master);
			this.cache = cache[rule().cacheIndex];
		}

		@Override
		public Match match() {
			if (options.debug)
				LiteralRule.this.matchTrace(this);
			if (fresh) {
				fresh = false;
				CachedMatch cm = cache.get(offset);
				if (cm == null) {
					if (options.study) {
						if (options.debug)
							LiteralRule.this.matchTrace(this, null);
						return null;
					}
				} else {
					if (options.debug)
						LiteralRule.this.matchTrace(this, cm.m);
					return register(cm.m);
				}
				int end = offset + literal.length();
				if (end <= options.end) {
					boolean matched = true;
					for (int i = offset, j = 0; i < end; i++, j++) {
						char c1 = literal.charAt(j), c2 = s.charAt(i);
						if (c1 != c2) {
							matched = false;
							break;
						}
					}
					if (matched) {
						Match m = new Match(LiteralRule.this, offset, end);
						if (c == null || c.passes(m, this, s))
							cm = new CachedMatch(m);
						else
							cm = CachedMatch.MISMATCH;
					} else
						cm = CachedMatch.MISMATCH;
				} else
					cm = CachedMatch.MISMATCH;
				cache.put(offset, cm);
				if (options.debug)
					LiteralRule.this.matchTrace(this, cm.m);
				return register(cm.m);
			}
			if (options.debug)
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
		protected Rule rule() {
			return LiteralRule.this;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	protected final String literal;
	private Condition c;

	public LiteralRule(Label label, String literal) {
		super(label);
		this.literal = literal;
	}

	@Override
	public Matcher matcher(CharSequence s, Integer offset,
			Map<Integer, CachedMatch>[] cache, Matcher master) {
		return new LiteralMatcher(s, offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		StringBuilder b = new StringBuilder();
		b.append('"').append(literal).append('"');
		if (condition != null)
			b.append('(').append(condition).append(')');
		return b.toString();
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
			if (condition != null)
				s += " (" + condition + ')';
			return wrap(new StringBuilder(s));
		}
		return wrap(new StringBuilder(uniqueId()));
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, Set<Rule> studiedRules,
			GlobalState options) {
		Map<Integer, CachedMatch> subCache = cache[cacheIndex];
		Set<Integer> startOffsets = new HashSet<Integer>();
		if (subCache.isEmpty()) {
			if (studiedRules.contains(this))
				return startOffsets;
			else
				studiedRules.add(this);
			int index, o = 0;
			String string = s.subSequence(options.start, options.end)
					.toString();
			while (o <= string.length()
					&& (index = string.indexOf(literal, o)) > -1) {
				Integer i = index + options.start;
				Match n = new Match(this, i, i + literal.length());
				if (c == null || c.passes(n, null, s))
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

	@Override
	public Rule shallowClone() {
		LiteralRule lr = new LiteralRule((Label) label.clone(), literal);
		return lr;
	}

	@Override
	public Rule conditionalize(Condition c, String id) {
		if (this.c == null) {
			this.c = c;
			this.condition = id;
		} else {
			if (this.c instanceof LogicalCondition) {
				if (!((LogicalCondition) this.c).replace(id, c))
					throw new GrammarException("could not define " + id
							+ " in this condition");
			} else if (this.c instanceof LeafCondition) {
				LeafCondition lc = (LeafCondition) this.c;
				if (lc.cnd.equals(id))
					this.c = c;
				else
					throw new GrammarException("rule " + this
							+ " does not carry condition " + id);
			} else
				throw new GrammarException("condition on rule " + this
						+ " cannot be redefined");
		}
		return this;
	}
}
