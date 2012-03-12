package dfh.grammar;

import java.io.Serializable;
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
public class LiteralRule extends Rule implements Serializable {
	private class LiteralMatcher extends Matcher {
		private final Map<Integer, CachedMatch> cache;
		private boolean fresh = true;

		public LiteralMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			super(offset, master);
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
				if (end <= options.end()) {
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
						if (testCondition(c, m))
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
	private static final long serialVersionUID = 6L;
	protected final String literal;
	protected Condition c;

	public LiteralRule(Label label, String literal) {
		super(label);
		this.literal = literal;
	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		return new LiteralMatcher(offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		StringBuilder b = new StringBuilder();
		b.append('"').append(literal).append('"');
		if (condition != null)
			b.append('(').append(c.describe()).append(')');
		return b.toString();
	}

	@Override
	public String description(boolean inBrackets) {
		boolean b1 = literal.indexOf('\\') > -1;
		boolean b2 = literal.indexOf('"') > -1;
		StringBuilder b;
		if (b1 || b2) {
			String s = literal;
			if (b2)
				s = '\'' + s.replaceAll("([\\\\'])", "\\\\$1") + '\'';
			else
				s = '"' + s.replaceAll("([\\\\])", "\\\\$1") + '"';
			b = new StringBuilder(s);
		} else
			b = new StringBuilder(uniqueId());
		b = new StringBuilder(wrap(b));
		if (c != null)
			b.append(" (").append(c.describe()).append(')');
		return b.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, GlobalState options) {
		Map<Integer, CachedMatch> subCache = cache[cacheIndex];
		Set<Integer> startOffsets = new HashSet<Integer>();
		if (subCache.isEmpty()) {
			int index, o = 0;
			String string = s.subSequence(options.start, options.end())
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

	@Override
	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
		Boolean b = literal.length() == 0;
		cache.put(uid(), b);
		return b;
	}

	@Override
	public Set<String> conditionNames() {
		if (c instanceof LogicalCondition)
			return ((LogicalCondition) c).conditionNames();
		Set<String> set = new HashSet<String>(1);
		set.add(c.getName());
		return set;
	}

	@Override
	public Rule deepCopy(String nameBase, Map<String, Rule> cycleMap) {
		LiteralRule lr = (LiteralRule) cycleMap.get(label().id);
		if (lr == null) {
			String id = generation == -1 ? label().id : nameBase + ':'
					+ label().id;
			Label l = new Label(label().t, id);
			lr = new LiteralRule(l, literal);
			if (c != null) {
				lr.condition = nameBase + ':' + condition;
				lr.c = c.copy(nameBase);
			}
			lr.setUid();
			cycleMap.put(label().id, lr);
			lr.generation = generation;
		}
		return lr;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
