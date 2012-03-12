package dfh.grammar;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * The object implementing rules such as
 * 
 * <pre>
 * &lt;a&gt; = &lt;b&gt; | &lt;c&gt;
 * </pre>
 * 
 * @author David Houghton
 * 
 */
@Reversible
public class AlternationRule extends Rule implements Serializable,
		NonterminalRule {
	private static final long serialVersionUID = 6L;

	private class AlternationMatcher extends NonterminalMatcher {
		int index = 0;
		Matcher mostRecent = null;

		public AlternationMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			super(offset, cache, AlternationRule.this, master);
		}

		@Override
		protected void fetchNext() {
			if (mostRecent == null) {
				mostRecent = alternates[index].matcher(offset, cache, this);
			}
			Match child = null;
			OUTER: while (mostRecent.mightHaveNext()
					|| index < alternates.length) {
				while (mostRecent.mightHaveNext()) {
					child = mostRecent.match();
					if (child != null) {
						if (testCondition(c, child))
							break OUTER;
						else
							child = null;
					}
				}
				if (++index == alternates.length)
					break;
				mostRecent = alternates[index].matcher(offset, cache, this);
			}
			if (child == null) {
				done = true;
				next = null;
			} else {
				next = new Match(AlternationRule.this, offset, child.end());
				Match[] children = new Match[] { child };
				next.setChildren(children);
			}
		}
	}

	protected final Rule[] alternates;
	Map<String, Set<String>> tagMap;
	protected Condition c;

	/**
	 * Generates a rule from the given label and alternates.
	 * 
	 * @param label
	 * @param alternates
	 * @param tagMap2
	 */
	public AlternationRule(Label label, Rule[] alternates,
			Map<String, Set<String>> tagMap) {
		super(label);
		this.alternates = alternates;
		this.tagMap = tagMap;
	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		return new AlternationMatcher(offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		StringBuilder b = new StringBuilder();
		b.append('[');
		boolean nonInitial = false;
		for (Rule r : alternates) {
			if (nonInitial)
				b.append('|');
			else
				nonInitial = true;
			b.append(r.uniqueId());
		}
		b.append(']');
		if (condition != null)
			b.append('(').append(c.describe()).append(')');
		return b.toString();
	}

	@Override
	public String description(boolean inBrackets) {
		StringBuilder b = new StringBuilder();
		boolean nonInitial = false;
		for (Rule r : alternates) {
			Set<String> tags;
			if (r instanceof CyclicRule)
				tags = tagMap.get(((CyclicRule) r).r.uniqueId());
			else if (r instanceof DeferredDefinitionRule)
				tags = tagMap.get(((DeferredDefinitionRule) r).r.uniqueId());
			else
				tags = tagMap.get(r.uniqueId());
			if (nonInitial)
				b.append(" | ");
			else
				nonInitial = true;
			if (!(tags == null || tags.isEmpty())) {
				if (!inBrackets)
					b.append('[');
				b.append("{");
				boolean ni2 = false;
				for (String label : tags) {
					if (ni2)
						b.append(',');
					else
						ni2 = true;
					b.append(label);
				}
				b.append("} ");
			}
			if (r.generation == -1) {
				b.append(r.description(false));
			} else
				b.append(r.label());
			if (!(tags == null || tags.isEmpty())) {
				b.append(' ');
				if (!inBrackets)
					b.append(']');
			}
		}
		b = new StringBuilder(wrap(b));
		if (condition != null)
			b.append(" (").append(c.describe()).append(')');
		return b.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, GlobalState options) {
		// non-terminal rules don't study
		return null;
	}

	@Override
	public boolean zeroWidth() {
		for (Rule r : alternates) {
			if (r.zeroWidth())
				return true;
		}
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
	public void addLabels(Match match, Set<String> labels) {
		labels.addAll(tagMap.get(match.rule().uid()));
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
			for (Rule r : alternates)
				r.setCacheIndex(uids);
		}

	}

	@Override
	protected int maxCacheIndex(int currentMax, Set<Rule> visited) {
		if (visited.contains(this))
			return currentMax;
		visited.add(this);
		int max = Math.max(cacheIndex, currentMax);
		for (Rule r : alternates)
			max = Math.max(max, r.maxCacheIndex(max, visited));
		return max;
	}

	@Override
	protected void rules(Map<String, Rule> map) {
		if (!map.containsKey(uid())) {
			map.put(uid(), this);
			for (Rule r : alternates)
				r.rules(map);
		}
	}

	@Override
	protected void fixAlternation() {
		for (Rule r : alternates) {
			if (r instanceof CyclicRule) {
				Set<String> set = tagMap.remove(r.uid());
				if (set != null) {
					r = ((CyclicRule) r).r;
					tagMap.put(r.uid(), set);
				}
			} else if (r instanceof DeferredDefinitionRule) {
				Set<String> set = tagMap.remove(r.uid());
				if (set != null) {
					r = ((DeferredDefinitionRule) r).r;
					tagMap.put(r.uid(), set);
				}
			}
			r.fixAlternation();
		}
	}

	@Override
	protected void subRules(Set<Rule> set, Set<Rule> all, boolean explicit) {
		if (!all.contains(this)) {
			all.add(this);
			if (!(set.contains(this))) {
				if (explicit) {
					if (generation > -1) {
						set.add(this);
					}
					if (unreversed != null)
						unreversed.subRules(set, all, explicit);
				} else
					set.add(this);
				for (Rule r : alternates)
					r.subRules(set, all, explicit);
			}
		}
	}

	@Override
	protected void initialRules(Set<String> initialRules) {
		if (!initialRules.contains(uid())) {
			initialRules.add(uid());
			for (Rule r : alternates)
				r.initialRules(initialRules);
		}
	}

	@Override
	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
		if (cache.containsKey(uid())) {
			Boolean b = cache.get(uid());
			if (b == null) {
				// recursion; we bail
				b = true;
				cache.put(uid(), b);
			}
			return b;
		} else {
			cache.put(uid(), null);
			boolean anyZero = false;
			for (Rule r : alternates) {
				anyZero |= r.mayBeZeroWidth(cache);
				if (anyZero)
					cache.put(uid(), true);
			}
			cache.put(uid(), anyZero);
			return anyZero;
		}
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
		Map<String, Set<String>> tmCopy = new HashMap<String, Set<String>>(
				tagMap.size());
		Rule[] copies = new Rule[alternates.length];
		String id = generation == -1 ? label().id : nameBase + ':' + label().id;
		Label l = new Label(label().t, id);
		for (int i = 0; i < copies.length; i++) {
			Rule r = alternates[i];
			Set<String> copySet = new HashSet<String>(tagMap.get(r.uid()));
			Rule copy = cycleMap.get(r.label().id);
			if (copy == null)
				copy = r.deepCopy(nameBase, cycleMap);
			tmCopy.put(copy.uid(), copySet);
			copies[i] = copy;
		}
		AlternationRule r = new AlternationRule(l, copies, tmCopy);
		if (c != null) {
			r.condition = nameBase + ':' + condition;
			r.c = c.copy(nameBase);
		}
		if (labels != null)
			r.labels = new TreeSet<String>(labels);
		r.setUid();
		cycleMap.put(label().id, r);
		r.generation = generation;
		return r;
	}
}
