package dfh.grammar;

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
 * TODO: make cyclic rules reversible
 * 
 * <b>Creation date:</b> Mar 25, 2011
 * 
 * @author David Houghton
 * 
 */
public class CyclicRule extends Rule {
	private static final long serialVersionUID = 1L;
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
	public Matcher matcher(CharSequence cs, Integer offset,
			Map<Integer, CachedMatch>[] cache, Matcher master) {
		return r.matcher(cs, offset, cache, master);
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
	}

	@Override
	public String description() {
		return label.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, Set<Rule> studiedRules,
			GlobalState options) {
		return r.study(s, cache, studiedRules, options);
	}

	@Override
	public boolean zeroWidth() {
		return r.zeroWidth();
	}

	@Override
	public Rule shallowClone() {
		CyclicRule cr = new CyclicRule((Label) label.clone());
		cr.r = r;
		return cr;
	}

	@Override
	protected void setUid() {
		if (uid == null) {
			uid = uniqueId();
			r.setUid();
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
}
