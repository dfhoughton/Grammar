package dfh.grammar;

import java.util.ArrayList;
import java.util.List;

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
	private static final long serialVersionUID = 1L;
	protected final Condition[] subconditions;

	public LogicalCondition(List<Condition> conditions) {
		subconditions = conditions.toArray(new Condition[conditions.size()]);
	}

	@Override
	public boolean passes(Match n, Matcher m, CharSequence s) {
		return allPass(n, m, s);
	}
	
	static Condition manufacture(Match m) {
		m = m.children()[0];
		if (m.hasLabel("cnd"))
			return new LeafCondition(m.group());
//		System.out.println(Dotify.dot(m));
		System.exit(1);
		List<Condition> list = new ArrayList<Condition>();
		m = m.choose("exp");
		if (m.hasLabel("conj")) {
			
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
	 */
	void replace(String name, Condition c) {
		for (int i = 0; i < subconditions.length; i++) {
			Condition sc = subconditions[i];
			if (sc instanceof LeafCondition)
				subconditions[i] = c;
			else if (sc instanceof LogicalCondition) {
				((LogicalCondition) subconditions[i]).replace(name, c);
			}
		}
	}

	protected abstract boolean allPass(Match n, Matcher m, CharSequence s);
}
