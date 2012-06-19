package dfh.grammar;

import java.util.regex.Pattern;

import dfh.grammar.Label.Type;

/**
 * Rule representing optional space that may occur between rule constituents in
 * a rule such as
 * 
 * <pre/>
 * rule := 'my'? 'cat'
 * </pre>
 * 
 * which becomes equivalent to
 * 
 * <pre/>
 * rule  = 'my'? /\s*+/r 'cat'
 * </pre>
 * <p>
 * 
 * @author David F. Houghton - Jun 18, 2012
 * 
 */
public class MaybeSpace extends LeafRule {
	private static final long serialVersionUID = 1L;
	public static final Pattern p = Pattern.compile("\\s*+");
	public static final Label l = new Label(Type.implicit, ".ms");
	private static final String fixedId = '/' + p.toString() + "/r";

	MaybeSpace() {
		super(l, p, true);
	}

	@Override
	protected String uniqueId() {
		return fixedId;
	}
}
