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
import java.util.Map;
import java.util.Set;

/**
 * A rule that depends on some other or others in a cycle of dependence. These
 * rules must be defined in two steps. For example:
 * 
 * <pre>
 * &lt;a&gt; = '(' [ '*' | &lt;a&gt; ] ')'
 * </pre>
 * 
 * which will match <i>(*)</i>, <i>((*))</i>, <i>(((*)))</i>, etc.
 * 
 * <b>Creation date:</b> Mar 25, 2011
 * 
 * @author David Houghton
 * 
 */
@Reversible
public class CyclicRule extends Rule implements Serializable, NonterminalRule {
	private static final long serialVersionUID = 7L;
	Rule r;

	/**
	 * Generates a {@link CyclicRule} with the given label.
	 * 
	 * @param label
	 */
	public CyclicRule(Label label) {
		super(label);
	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		return r.matcher(offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		return label.toString();
	}

	/**
	 * Used to complete the cycle during {@link Grammar} compilation.
	 * 
	 * @param r
	 */
	protected void setRule(Rule r) {
		if (this.r != null)
			throw new GrammarException("rule " + label + "already defined");
		this.r = r;
		r.cycle = true;
	}

	@Override
	public String description(boolean inBrackets) {
		return wrap(new StringBuilder(label.toString()));
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, GlobalState options) {
		// non-terminal rules don't study
		return null;
	}

	@Override
	public boolean zeroWidth() {
		return r.zeroWidth();
	}

	@Override
	protected void setCacheIndex(Map<String, Integer> uids) {
		if (cacheIndex == -1) {
			Integer i = uids.get(uid());
			if (i == null) {
				i = uids.size();
				uids.put(uid(), i);
			}
			cacheIndex = i;
			r.setCacheIndex(uids);
		}
	}

	@Override
	protected int maxCacheIndex(int currentMax, Set<Rule> visited) {
		if (visited.contains(this))
			return currentMax;
		visited.add(this);
		int max = Math.max(cacheIndex, currentMax);
		return r.maxCacheIndex(max, visited);
	}

	@Override
	protected void rules(Map<String, Rule> map) {
		if (!map.containsKey(uid())) {
			map.put(uid(), this);
			r.rules(map);
		}
	}

	@Override
	protected void subRules(Set<Rule> set, Set<Rule> all, boolean explicit) {
		if (!all.contains(this)) {
			all.add(this);
			if (!set.contains(this)) {
				if (explicit) {
					if (generation > -1)
						set.add(this);
					if (unreversed != null)
						unreversed.subRules(set, all, explicit);
				} else
					set.add(this);
				r.subRules(set, all, explicit);
			}
		}
	}

	@Override
	protected void initialRules(Set<String> initialRules) {
		if (!initialRules.contains(uid())) {
			initialRules.add(uid());
			r.initialRules(initialRules);
		}
	}

	@Override
	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
		if (cache.containsKey(uid())) {
			Boolean b = cache.get(uid());
			if (b == null) {
				// recursion, we bail
				b = true;
				cache.put(uid(), b);
			}
			return b;
		} else {
			cache.put(uid(), null);
			Boolean b = r.mayBeZeroWidth(cache);
			cache.put(uid(), b);
			return b;
		}
	}

	@Override
	public Rule deepCopy(Label l, String nameBase, Map<String, Rule> cycleMap,
			Set<String> knownLabels, Set<String> knownConditions) {
		Rule copy = r
				.deepCopy(nameBase, cycleMap, knownLabels, knownConditions);
		CyclicRule r = new CyclicRule(l);
		r.r = copy;
		return r;
	}

	@Override
	protected boolean findLeftCycle(Rule sought, Set<Rule> cycleCache) {
		cycleCache.add(this);
		if (r == sought)
			return true;
		if (!cycleCache.contains(r)) {
			cycleCache.add(r);
			if (r.findLeftCycle(sought, cycleCache))
				return true;
		}
		return false;
	}
}
