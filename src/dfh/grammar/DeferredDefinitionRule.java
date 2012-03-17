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
 * A rule undefined at the time of grammar compilation. It will always
 * correspond to a terminal rule. This class exists so that a {@link Grammar}
 * can be compiled and then certain rules dynamically loaded in later.
 * 
 * This is similar to Perl's <code>AUTOLOAD</code> mechanism or a function
 * prototype.
 * 
 * TODO: figure out how we can include deferred definition rules in backwards
 * assertions
 * 
 * <b>Creation date:</b> Mar 16, 2011
 * 
 * @author David Houghton
 * 
 */
public class DeferredDefinitionRule extends Rule implements Serializable,
		NonterminalRule {
	private static final long serialVersionUID = 6L;
	/**
	 * Holds rule whose definition is deferred.
	 */
	protected Rule r;

	/**
	 * Generates {@link Rule} from given label.
	 * 
	 * @param label
	 */
	public DeferredDefinitionRule(Label label) {
		super(label);
	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		return r.matcher(offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		return "({DEFERRED}" + label + ")";
	}

	/**
	 * Defines this rule as the given {@link Rule}.
	 * 
	 * @param r
	 */
	public void setRule(Rule r) {
		if (this.r != null)
			throw new GrammarException("rule " + label + "already defined");
		// TODO improve this so that the generation is set correctly
		r.generation = 1;
		this.r = r;
	}

	@Override
	public String description(boolean inBrackets) {
		if (r instanceof DeferredDefinitionRule)
			return r.label.toString();
		return r == null ? "UNDEFINED" : r.description(inBrackets);
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
	protected void fixAlternation() {
		r.fixAlternation();
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
				if (r != null)
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
		DeferredDefinitionRule ddr = new DeferredDefinitionRule(l);
		ddr.r = copy;
		return ddr;
	}
}
