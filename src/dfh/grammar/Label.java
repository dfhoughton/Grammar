package dfh.grammar;

import java.io.Serializable;

public class Label extends RepeatableRuleFragment implements Comparable<Label>,
		Serializable {
	private static final long serialVersionUID = 1L;

	public enum Type {
		/**
		 * A regular expression. The grammar cannot backtrack within whatever
		 * this rule matches. As far as the grammar is concerned, it is atomic.
		 */
		terminal(2),
		/**
		 * The root rule of the grammar. Every grammar must have one and only
		 * one root and it can parse a string only if it passes the root rule.
		 */
		root(0),
		/**
		 * Any non-terminal rule except the root rule.
		 */
		nonTerminal(1);
		private final int ranking;

		private Type(int ranking) {
			this.ranking = ranking;
		}

		public int cmp(Type other) {
			return ranking - other.ranking;
		}
	}

	public final Type t;
	public final String id;

	public Label(Type t, String id) {
		this.t = t;
		this.id = t == Type.root ? null : id;
	}

	@Override
	public int compareTo(Label o) {
		int comparison = t.cmp(o.t);
		if (comparison == 0)
			comparison = id.compareTo(o.id);
		return comparison;
	}
}
