/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
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
							cm = new CachedMatch(m);
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
	private static final long serialVersionUID = 8L;
	protected final String literal;
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
		if (uid != null)
			return uid;
		StringBuilder b = new StringBuilder();
		b.append('"').append(literal).append('"');
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
		} else {
			b = new StringBuilder(literal.length() * 2);
			b.append('"').append(literal).append('"');
		}
		b = new StringBuilder(wrap(b));
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
	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
		Boolean b = literal.length() == 0;
		cache.put(uid(), b);
		return b;
	}

	@Override
	public Rule deepCopy(Label l, String nameBase, Map<String, Rule> cycleMap,
			Set<String> knownLabels, Set<String> knownConditions) {
		LiteralRule lr = new LiteralRule(l, literal);
		return lr;
	}

	@Override
	public boolean isTerminal() {
		return true;
	}
}
