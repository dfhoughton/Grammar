package dfh.grammar;

import java.util.Collections;
import java.util.Set;

import dfh.grammar.Label.Type;
import dfh.grammar.SequenceRule.SequenceMatcher;

/**
 * Rule representing space that may occur between non-trivial constituents in a
 * rule such as
 * 
 * <pre/>
 * rule := 'my'? 'cat'
 * </pre>
 * 
 * which becomes equivalent to
 * 
 * <pre/>
 * rule   = ['my' /\s++/r]? 'cat'
 * </pre>
 * 
 * or
 * 
 * <pre/>
 * rule  .= 'my'? 'cat'
 * </pre>
 * 
 * which becomes equivalent to
 * 
 * <pre/>
 * rule   = [ 'my' /\s++/r? ]? 'cat'
 * </pre>
 * 
 * TODO: make this description more accurate; in particular, cover the cases in
 * which one needs a visible space.
 * <p>
 * 
 * @author David F. Houghton - Jul 15, 2012
 * 
 */
@Reversible
public class HiddenSpace extends ConditionalRule {
	private static final long serialVersionUID = 2L;
	private static final Condition c = new Condition() {
		private static final long serialVersionUID = 2L;
		/**
		 * Identifier for this condition.
		 */
		public static final String ID = ".hs";
		{
			name = ID;
		}

		@Override
		public boolean passes(Match n, Matcher m, CharSequence s) {
			if (n.zeroWidth())
				return true;
			SequenceMatcher sm = (SequenceMatcher) m.master;
			for (Match c : sm.matched) {
				if (c == n)
					break;
				if (!c.zeroWidth())
					return true;
			}
			return false;
		}

		@Override
		public boolean visible() {
			return false;
		}
	};

	static final Set<String> noTags = Collections.emptySet();
	/**
	 * The main label for a space rule when it is not to show up in a
	 * description. The value which will be displayed in logs and so forth is
	 * <code>.s</code> .
	 */
	public static final Label LABEL = new Label(Type.implicit, ".s");
	/**
	 * The label for a space rule inserted into an assertion to cover space
	 * between the remainder of the assertion an immediately adjacent
	 * constituents. The value which will be displayed in logs and so forth is
	 * <code>.as</code> .
	 */
	public static final Label ASSERTION_LABEL = new Label(Type.implicit, ".as");
	/**
	 * The label for the conditionalized repetition rule.
	 */
	public static final Label REP_LABEL = new Label(Type.implicit, SS.fixedIdSS
			+ Repetition.ASTERISK);
	private static final String fixedId = REP_LABEL.id + '(' + c.name + ')';
	/**
	 * Used for hidden spaces injected into assertions.
	 */
	private static final Condition emptyC = new Condition() {
		private static final long serialVersionUID = 2L;
		/**
		 * Identifier for this condition.
		 */
		public static final String ID = ".ehs";
		{
			name = ID;
		}

		@Override
		public boolean passes(Match n, Matcher m, CharSequence s) {
			return true;
		}

		@Override
		public boolean visible() {
			return false;
		}
	};

	static HiddenSpace ordinary() {
		return new HiddenSpace(LABEL);
	}

	static HiddenSpace assertion() {
		return new HiddenSpace(ASSERTION_LABEL);
	}

	private HiddenSpace(Label l) {
		super(l, new RepetitionRule(REP_LABEL, new SS(), Repetition.ASTERISK,
				noTags), l == LABEL ? c : emptyC);
	}

	@Override
	protected String uniqueId() {
		return fixedId;
	}
}
