package dfh.grammar;

import java.util.ArrayList;
import java.util.List;

import dfh.grammar.util.Dotify;

/**
 * Base class for conditions representing logical combinations of other
 * conditions.
 * <p>
 * <b>Creation date:</b> Dec 5, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class LogicalCondition extends Condition {
	private static final long serialVersionUID = 2L;
	protected final Condition[] subconditions;
	public static final MatchTest expOrGroupTest = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return m.hasLabel("exp") || m.hasLabel("group")
					|| m.hasLabel("cnd");
		}
	};

	public LogicalCondition(List<Condition> conditions) {
		subconditions = conditions.toArray(new Condition[conditions.size()]);
	}

	@Override
	public boolean passes(Match n, Matcher m, CharSequence s) {
		return allPass(n, m, s);
	}

	/**
	 * Trims off the root node of the match tree then recursively parses the
	 * remainder.
	 * 
	 * @param m
	 *            root node of parse tree for condition
	 * @return complex condition
	 */
	static Condition manufacture(Match m) {
		m = m.children()[0];
		return mfct(m);
	}

	private static Condition mfct(Match m) {
		if (m.hasLabel("cnd"))
			return new LeafCondition(m.group());
		if (m.hasLabel("group"))
			return mfct(m.first("exp").children()[0]);
		if (m.hasLabel("exp"))
			return mfct(m.children()[0]);
		try {
			List<Condition> list = new ArrayList<Condition>();
			List<Match> constituents = m.closest(expOrGroupTest);
			for (Match sm : constituents)
				list.add(mfct(sm));
			if (m.hasLabel("conj"))
				return new ConjunctionCondition(list);
			if (m.hasLabel("disj"))
				return new DisjunctionCondition(list);
			if (m.hasLabel("xor"))
				return new XORCondition(list);
			if (m.hasLabel("neg"))
				return new NegationCondition(list);
		} catch (Exception e) {
			System.err.println("unable to parse condition " + m.group());
			System.out.println(Dotify.dot(m));
		}
		return null;
	}

	/**
	 * Find where this condition belongs in the logic tree and plug it in.
	 * 
	 * @param name
	 *            condition name as given in the grammar
	 * @param c
	 *            actual condition
	 * @return whether the condition was used
	 */
	boolean replace(String name, Condition c) {
		boolean used = false;
		for (int i = 0; i < subconditions.length; i++) {
			Condition sc = subconditions[i];
			if (sc instanceof LeafCondition) {
				LeafCondition lc = (LeafCondition) sc;
				if (lc.cnd.equals(name)) {
					subconditions[i] = c;
					used = true;
				}
			} else if (sc instanceof LogicalCondition) {
				boolean b = ((LogicalCondition) subconditions[i]).replace(name,
						c);
				used |= b;
			}
		}
		return used;
	}

	protected abstract boolean allPass(Match n, Matcher m, CharSequence s);
}
