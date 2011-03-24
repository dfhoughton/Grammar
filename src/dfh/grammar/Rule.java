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

	/**
	 * Creates a {@link Matcher} to keep track of backtracking and the matching
	 * cache at this offset.
	 * 
	 * @param s
	 *            sequence to match against
	 * @param offset
	 *            offset at which to begin the match
	 * @param parent
	 *            parent node for use in constructing the match tree
	 * @param cache
	 *            collection of offset matching caches
	 * @param master
	 *            reference to enclosing {@link Matcher} for use in
	 *            backreference testing
	 * @return
	 */
	public abstract Matcher matcher(CharSequence s, Integer offset, Match parent,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master);

	@Override
	public String toString() {
		return label.id;
	}

	/**
	 * @return label-free id used to recognize redundancy during compilation
	 */
	protected abstract String uniqueId();
}
