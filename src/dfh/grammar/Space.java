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
public class Space extends RepetitionRule {
	private static final long serialVersionUID = 1L;

	private static class SS extends LeafRule {
		private static final long serialVersionUID = 1L;
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
	public static final Label l = new Label(Type.implicit, ".s");
	private static final String fixedId = SS.fixedIdSS + Repetition.ASTERISK;

	Space() {
		super(l, new SS(), Repetition.ASTERISK, noTags);
	}

	@Override
	protected String uniqueId() {
		return fixedId;
	}
}
