package dfh.grammar;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public abstract class Rule implements Serializable {
	private static final long serialVersionUID = 1L;

	final Label label;

	public Rule(Label label) {
		this.label = label;
	}

	public Label label() {
		return label;
	}

	public abstract Matcher matcher(CharSequence s, int offset, Match parent,
			Map<Label, Map<Integer, Match>> cache);

	@Override
	public String toString() {
		return label.id;
	}
}
