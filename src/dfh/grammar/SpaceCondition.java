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
	private static final long serialVersionUID = 2L;
	/**
	 * Identifier for this condition.
	 */
	public static final String ID = ".s";
	{
		name = ID;
	}

	@Override
	public boolean passes(Match n, Matcher m, CharSequence s) {
		boolean needS = false, foundNothing = true, lastVisible = false;
		// check each match in the conditionalized sequence in turn
		Match[] sequence = n.children()[0].children();
		for (Match c : sequence) {
			if (c.rule() instanceof HiddenSpace
					|| c.rule() instanceof VisibleSpace) {
				// if this node represents whitespace, we stop needing space
				if (c.end() > c.start()) {
					needS = false;
					if (c.rule() instanceof VisibleSpace)
						lastVisible = true;
				}
			} else if (c.end() > c.start()) {
				lastVisible = foundNothing = false;
				// do we need space? If so, can some be found either in the
				// preceding non-trivial nodes or the node itself?
				if (needS && !Character.isWhitespace(s.charAt(c.start())))
					return false;
				// if this node ends in whitespace, we have already accounted
				// for any space we may need immediately following this node
				needS = !Character.isWhitespace(s.charAt(c.end() - 1));
			}
		}
		return foundNothing || needS || lastVisible;
	}

	@Override
	public boolean visible() {
		return false;
	}
}
