package dfh.grammar;

import java.util.regex.Pattern;

import dfh.grammar.Label.Type;

/**
 * Terminal rule underlying {@link HiddenSpace} and {@link VisibleSpace}.
 * <p>
 * 
 * @author David F. Houghton - Jul 15, 2012
 * 
 */
@Reversible
class SS extends LeafRule {
	private static final long serialVersionUID = 2L;
	public static final Pattern p = Pattern.compile("\\s");
	public static final Label lss = new Label(Type.implicit, ".ss");
	static final String fixedIdSS = '/' + p.toString() + "/r";

	SS() {
		super(lss, p, true);
	}

	@Override
	protected String uniqueId() {
		return fixedIdSS;
	}

}