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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Matches a sequence of sub-rules. E.g.,
 * 
 * <pre>
 * {@code 
 * <a> = <b> <c>
 * }
 * </pre>
 * 
 * @author David Houghton
 * 
 */
@Reversible
public class SequenceRule extends Rule implements Serializable, NonterminalRule {
	private static final long serialVersionUID = 8L;
	final Rule[] sequence;
	final List<Set<String>> tagList;

	class SequenceMatcher extends NonterminalMatcher {
		LinkedList<Matcher> matchers = new LinkedList<Matcher>();
		LinkedList<Match> matched = new LinkedList<Match>();

		public SequenceMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			super(offset, cache, SequenceRule.this, master);
		}

		@Override
		protected void fetchNext() {
			try {
				while (true) {
					if (matched.size() > 0) {
						while (!matched.isEmpty()) {
							matched.removeLast();
							if (matchers.peekLast().mightHaveNext())
								break;
							else
								matchers.removeLast();
						}
						if (matchers.isEmpty()) {
							next = null;
							done = true;
							return;
						}
					}
					next = null;
					boolean found = true;
					while (matched.size() < sequence.length) {
						Matcher m;
						if (matchers.isEmpty()) {
							m = sequence[0].matcher(offset, cache, this);
							matchers.add(m);
						} else
							m = matchers.peekLast();
						Match n = m.mightHaveNext() ? m.match() : null;
						if (n == null) {
							matchers.removeLast();
							if (!matched.isEmpty()) {
								matched.removeLast();
							}
							if (matchers.isEmpty()) {
								done = true;
								found = false;
								break;
							}
						} else {
							matched.add(n);
							if (matched.size() < sequence.length) {
								m = sequence[matched.size()].matcher(n.end(),
										cache, this);
								matchers.add(m);
							}
						}
					}
					if (found) {
						next = new Match(SequenceRule.this, offset, matched
								.peekLast().end());
						Match[] children = matched
								.toArray(new Match[sequence.length]);
						next.setChildren(children);
					}
					break;
				}
			} catch (SingleColonBarrier s) {
				done = true;
				next = null;
			}

		}
	}

	/**
	 * Generates a {@link SequenceRule} with the given sequence.
	 * 
	 * @param l
	 * @param list
	 */
	public SequenceRule(Label l, List<Rule> list, List<Set<String>> tagList) {
		this(l, list.toArray(new Rule[list.size()]), tagList);
	}

	/**
	 * Generates a {@link SequenceRule} with the given sequence.
	 * 
	 * @param label
	 * @param sequence
	 * @param tagList
	 */
	public SequenceRule(Label label, Rule[] sequence, List<Set<String>> tagList) {
		super(label);
		this.sequence = sequence;
		this.tagList = tagList;
	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		return new SequenceMatcher(offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		if (uid != null)
			return uid;
		StringBuilder b = new StringBuilder();
		b.append('[');
		boolean nonInitial = false;
		for (Rule r : sequence) {
			if (nonInitial)
				b.append(' ');
			else
				nonInitial = true;
			b.append(r.uniqueId());
		}
		b.append(']');
		return b.toString();
	}

	@Override
	public String description(boolean inBrackets) {
		StringBuilder b = new StringBuilder();
		boolean nonInitial = false;
		int index = -1;
		for (Rule r : sequence) {
			index++;
			if (r instanceof HiddenSpace)
				continue;
			boolean hasTags = !tagList.get(index).isEmpty();
			if (nonInitial)
				b.append(' ');
			else
				nonInitial = true;
			if (hasTags) {
				if (!inBrackets)
					b.append('[');
				b.append('{');
				boolean ni2 = false;
				for (String label : tagList.get(index)) {
					if (ni2)
						b.append(',');
					else
						ni2 = true;
					b.append(label);
				}
				b.append("} ");
			}
			if (r.generation == -1) {
				boolean alternation = r instanceof AlternationRule;
				if (alternation)
					b.append("[ ");
				b.append(r.description(true));
				if (alternation)
					b.append(" ]");
			} else
				b.append(r.label());
			if (hasTags) {
				b.append(' ');
				if (!inBrackets)
					b.append(']');
			}
		}
		return wrap(b);
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, GlobalState options) {
		// non-terminal rules don't study
		return null;
	}

	@Override
	public boolean zeroWidth() {
		for (Rule r : sequence) {
			if (!r.zeroWidth())
				return false;
		}
		return true;
	}

	@Override
	public void addLabels(Match match, Set<String> labels) {
		for (int i = 0; i < sequence.length; i++) {
			if (match.rule() == sequence[i]) {
				labels.addAll(tagList.get(i));
				break;
			}
		}
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
			for (Rule r : sequence)
				r.setCacheIndex(uids);
		}
	}

	@Override
	protected int maxCacheIndex(int currentMax, Set<Rule> visited) {
		if (visited.contains(this))
			return currentMax;
		visited.add(this);
		int max = Math.max(cacheIndex, currentMax);
		for (Rule r : sequence)
			max = Math.max(max, r.maxCacheIndex(max, visited));
		return max;
	}

	@Override
	protected void rules(Map<String, Rule> map) {
		if (!map.containsKey(uid())) {
			map.put(uid(), this);
			for (Rule r : sequence)
				r.rules(map);
		}
	}

	@Override
	protected void fixAlternation() {
		for (Rule r : sequence)
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
				for (Rule r : sequence)
					r.subRules(set, all, explicit);
			}
		}
	}

	@Override
	protected void initialRules(Set<String> initialRules) {
		if (!initialRules.contains(uid())) {
			initialRules.add(uid());
			for (Rule r : sequence) {
				r.initialRules(initialRules);
				if (!r.mayBeZeroWidth)
					break;
			}
		}
	}

	@Override
	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
		if (cache.containsKey(uid())) {
			Boolean b = cache.get(uid());
			if (b == null) {
				// recursion; we bail out
				b = true;
				cache.put(uid(), b);
			}
			return b;
		} else {
			cache.put(uid(), null);
			boolean allZero = true;
			for (Rule r : sequence) {
				allZero &= r.mayBeZeroWidth(cache);
				if (!allZero)
					cache.put(uid(), false);
			}
			cache.put(uid(), allZero);
			return allZero;
		}
	}

	@Override
	public Rule deepCopy(Label l, String nameBase, Map<String, Rule> cycleMap,
			Set<String> knownLabels, Set<String> knownConditions) {
		Rule[] copies = new Rule[sequence.length];
		List<Set<String>> tlCopy = new ArrayList<Set<String>>(tagList.size());
		SequenceRule r = new SequenceRule(l, copies, tlCopy);
		for (int i = 0; i < copies.length; i++) {
			Set<String> copySet = new HashSet<String>(tagList.get(i));
			tlCopy.add(copySet);
			Rule or = sequence[i];
			Rule copy = cycleMap.get(or.label().id);
			if (copy == null)
				copy = or.deepCopy(nameBase, cycleMap, knownLabels,
						knownConditions);
			copies[i] = copy;
		}
		return r;
	}

	@Override
	protected boolean findLeftCycle(Rule sought, Set<Rule> cycleCache) {
		cycleCache.add(this);
		for (Rule r : sequence) {
			if (r == sought)
				return true;
			if (cycleCache.contains(r)) {
				if (r.mayBeZeroWidth)
					continue;
				return false;
			}
			cycleCache.add(r);
			if (r instanceof CyclicRule) {
				CyclicRule cr = (CyclicRule) r;
				if (cr.r == sought)
					return true;
				if (cr.r.findLeftCycle(sought, cycleCache))
					return true;
			} else if (r.findLeftCycle(sought, cycleCache))
				return true;
			if (!r.mayBeZeroWidth)
				return false;
		}
		return false;
	}

	@Override
	public Match checkCacheSlip(int i, Match m) {
		Rule r = sequence[i];
		while (r instanceof DeferredDefinitionRule)
			r = ((DeferredDefinitionRule) r).r;
		return new Match(r, m.start(), m.end());
	}
}
