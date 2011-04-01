package dfh.grammar;

import java.io.Serializable;

/**
 * A rule identifier.
 * <p>
 * <b>Creation date:</b> Mar 14, 2011
 * 
 * @author David Houghton
 * 
 */
public class Label extends RepeatableRuleFragment implements Comparable<Label>,
		Serializable, Cloneable {
	private static final long serialVersionUID = 1L;
	/**
	 * label reserved for root rule label
	 */
	public static final String ROOT = "ROOT";

	/**
	 * Type of {@link Rule} associated with {@link Label}.
	 * <p>
	 * <b>Creation date:</b> Apr 1, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	public enum Type {
		/**
		 * A regular expression. The grammar cannot backtrack within whatever
		 * this rule matches. As far as the grammar is concerned, it is atomic.
		 */
		terminal(2),
		/**
		 * Basically a string that must match literally.
		 */
		literal(3),
		/**
		 * This is a non-repeating identical match with an early constituent of
		 * a sequence.
		 */
		backreference(4),
		/**
		 * The root rule of the grammar. Every grammar must have one and only
		 * one root and it can parse a string only if it passes the root rule.
		 */
		root(0),
		/**
		 * Rule awaiting further identification during parsing.
		 */
		indeterminate(-1),
		/**
		 * Any non-terminal rule except the root rule.
		 */
		nonTerminal(1);
		private final int ranking;

		private Type(int ranking) {
			this.ranking = ranking;
		}

		/**
		 * Used in sorting.
		 * 
		 * @param other
		 * @return order of types
		 */
		public int cmp(Type other) {
			return ranking - other.ranking;
		}
	}

	/**
	 * {@link Type} of the associated {@link Rule}.
	 */
	public final Type t;
	/**
	 * Identifier of associated {@link Rule}.
	 */
	public final String id;

	/**
	 * Constructs {@link Label} with given id and {@link Type}.
	 * 
	 * @param t
	 * @param id
	 */
	public Label(Type t, String id) {
		this.t = t;
		this.id = id;
	}

	@Override
	public int compareTo(Label o) {
		int comparison = t.cmp(o.t);
		if (comparison == 0)
			comparison = id.compareTo(o.id);
		return comparison;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;
		if (o == this)
			return true;
		if (o instanceof Label) {
			Label l = (Label) o;
			return l.id.equals(id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		switch (t) {
		case backreference:
			return id;
		default:
			return '<' + id + '>' + rep;
		}
	}

	@Override
	public Object clone() {
		return new Label(t, id);
	}
}
