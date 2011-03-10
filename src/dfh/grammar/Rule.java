package dfh.grammar;

import java.io.Serializable;

/**
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class Rule implements Serializable {
	private static final long serialVersionUID = 1L;

	private final String label;

	public Rule(String label) {
		this.label = label;
	}

	public String label() {
		return label;
	}

}
