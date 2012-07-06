/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
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
	private static final long serialVersionUID = 8L;

	/**
	 * Type of {@link Rule} associated with {@link Label}.
	 * <p>
	 * Note: this enum is a relic of an earlier conception of how grammar
	 * compilation and description was going to proceed. I believe it serves no
	 * purpose at this point but I have not yet removed it. It would probably be
	 * wise to do so at some point. Compilation has gotten complex enough.
	 * 
	 * @author David Houghton
	 * 
	 */
	public enum Type {
		/**
		 * A rule defined on its own line in the specification.
		 */
		explicit(0),
		/**
		 * A rule defined as part of another rule.
		 */
		implicit(1),
		/**
		 * Rule awaiting further identification during parsing.
		 */
		indeterminate(-1);
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
	 * Possible treatments of inter-rule whitespace in sequences which are
	 * constituents of a given labeled rule.
	 * <p>
	 * 
	 * @author David F. Houghton - Jun 19, 2012
	 * 
	 */
	public enum Whitespace {
		/**
		 * no whitespace is assumed between tokens in a sequence constituent of
		 * this rule
		 */
		none("="),
		/**
		 * whitespace is assumed between tokens in a sequence constituent of
		 * this rule
		 */
		required(":="),
		/**
		 * whitespace may occur between tokens in a sequence constituent of this
		 * rule
		 */
		maybe(".=");
		private final String symbol;

		private Whitespace(String symbol) {
			this.symbol = symbol;
		}

		/**
		 * @return the assignment symbol corresponding to this rule
		 */
		public String symbol() {
			return symbol;
		}

		/**
		 * @return the length of the assignment symbol
		 */
		public int length() {
			return symbol.length();
		}
	}

	/**
	 * {@link Type} of the associated {@link Rule}.
	 */
	public final Type t;
	Whitespace ws = Whitespace.none;

	/**
	 * Returns treatment of inter-rule whitespace in sequences which are
	 * constituents of a rule bearing this label.
	 * 
	 * @return treatment of inter-rule whitespace in sequences which are
	 *         constituents of a rule bearing this label
	 */
	public Whitespace whitespace() {
		return ws;
	}

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
		case explicit:
			return '<' + id + '>' + rep;
		default:
			return id;
		}
	}

	@Override
	public Object clone() {
		return new Label(t, id);
	}
}
