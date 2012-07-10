package dfh.grammar;

import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import dfh.grammar.Label.Type;

/**
 * Rule representing space that may occur between non-trivial constituents in a
 * rule such as
 * 
 * <pre/>
 * rule ::= 'my'? 'cat'
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
 * rule  := 'my'? 'cat'
 * </pre>
 * 
 * which becomes equivalent to
 * 
 * <pre/>
 * rule   = 'my'? /\s++/r? 'cat'
 * </pre>
 * 
 * <p>
 * 
 * @author David F. Houghton - Jun 18, 2012
 * 
 */
@Reversible
public class Space extends RepetitionRule {
	private static final long serialVersionUID = 2L;

	@Reversible
	private static class SS extends LeafRule {
		private static final long serialVersionUID = 2L;
		public static final Pattern p = Pattern.compile("\\s");
		public static final Label lss = new Label(Type.implicit, ".ss");
		private static final String fixedIdSS = '/' + p.toString() + "/r";

		SS() {
			super(lss, p, true);
		}

		@Override
		protected String uniqueId() {
			return fixedIdSS;
		}

	}

	private static final Set<String> noTags = Collections.emptySet();
	/**
	 * The label for a space rule when it is not to show up in a description.
	 * The value which will be displayed in logs and so forth is <code>.s</code>
	 * . .
	 */
	public static final Label HIDDEN_SPACE = new Label(Type.implicit, ".s");
	/**
	 * The label for a space rule when it is to show up in a description. The
	 * value which will be displayed is <code>.</code> -- a single dot.
	 */
	public static final Label VISIBLE_SPACE = new Label(Type.implicit, ".");
	private static final String fixedId = SS.fixedIdSS + Repetition.ASTERISK;

	Space(boolean hidden) {
		super(hidden ? HIDDEN_SPACE : VISIBLE_SPACE, new SS(),
				Repetition.ASTERISK, noTags);
	}

	@Override
	protected String uniqueId() {
		return fixedId;
	}

	@Override
	public String description(boolean inBrackets) {
		return (label.id == VISIBLE_SPACE.id) ? VISIBLE_SPACE.id : "";
	}
}
