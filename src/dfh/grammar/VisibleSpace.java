package dfh.grammar;

import java.util.Collections;
import java.util.Set;

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
public class VisibleSpace extends RepetitionRule {
	private static final long serialVersionUID = 2L;

	private static final Set<String> noTags = Collections.emptySet();
	/**
	 * The label for a space rule when it is to show up in a description. The
	 * value which will be displayed is <code>.</code> -- a single dot.
	 */
	public static final Label LABEL = new Label(Type.implicit, ".");
	private static final String fixedId = SS.fixedIdSS + Repetition.ASTERISK;

	VisibleSpace() {
		super(LABEL, new SS(), Repetition.ASTERISK, noTags);
	}

	@Override
	protected String uniqueId() {
		return fixedId;
	}

	@Override
	public String description(boolean inBrackets) {
		return LABEL.id;
	}
}
