package dfh.grammar;

/**
 * Condition applicable to sequences that need spaces between their
 * constituents.
 * <p>
 * 
 * @author David F. Houghton - Jun 18, 2012
 * 
 */
public class SpaceCondition extends Condition {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier for this condition.
	 */
	public static final String ID = ".s";

	@Override
	public boolean passes(Match n, Matcher m, CharSequence s) {
		boolean needS = false;
		for (Match c : n.children()) {
			if (c.rule().label.id.equals(Space.l.id)) {
				// if this node represents whitespace, we stop needing space
				needS &= n.end() == n.start();
			} else if (n.end() > n.start()) {
				// do we need space? If so, can some be found either in the
				// preceding non-trivial nodes or the node itself?
				if (needS && !Character.isWhitespace(s.charAt(n.start())))
					return false;
				// if this node ends in whitespace, we have already accounted
				// for any space we may need immediately following this node
				needS = !Character.isWhitespace(s.charAt(n.end() - 1));
			}
		}
		return true;
	}
}
