/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.util.regex.Pattern;

/**
 * Repetition to be attached to a rule. All the patterns understood by
 * {@link Pattern} are possible as well as {@code ,n}}, which is the same as
 * {@code 0,n}}.
 * <p>
 * <b>NOTE:</b> the repetition suffix {@code 1}+} will have the same effect as
 * the {@code (?>...)} non-backtracking pattern, which isn't otherwise
 * representable. This means
 * 
 * <pre>
 * {@code 
 * <a> = [ 'a' | 'b' ]{1}+ [ 'c' 'd'* ]{1}+
 * }
 * </pre>
 * 
 * matches the same sequences as {@code (?>a|b)(?>cd*)}.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 * 
 */
public class Repetition {
	/**
	 * How much the {@link Repetition} holds onto its matches and the order in
	 * which the associated pattern will iterate over them in seeking a match.
	 * 
	 * @author David Houghton
	 * 
	 */
	public enum Type {
		/**
		 * Takes as many matches as possible.
		 */
		greedy,
		/**
		 * Takes as few matches as possible.
		 */
		stingy,
		/**
		 * Takes all possible matches and allows no backtracking.
		 */
		possessive
	}

	/**
	 * The repetition type of unmodified rules.
	 */
	public static final Repetition NONE = new Repetition(Type.greedy, 1, 1);
	/**
	 * {@code *}
	 */
	public static final Repetition ASTERISK = new Repetition(Type.greedy, 0,
			Integer.MAX_VALUE);
	/**
	 * {@code +}
	 */
	public static final Repetition PLUS = new Repetition(Type.greedy, 1,
			Integer.MAX_VALUE);
	/**
	 * {@code ?}
	 */
	public static final Repetition QMARK = new Repetition(Type.greedy, 0, 1);
	/**
	 * {@code *?}
	 */
	public static final Repetition ASTERISK_Q = new Repetition(Type.stingy, 0,
			Integer.MAX_VALUE);
	/**
	 * {@code +?}
	 */
	public static final Repetition PLUS_Q = new Repetition(Type.stingy, 1,
			Integer.MAX_VALUE);
	/**
	 * {@code ??}
	 */
	public static final Repetition QMARK_Q = new Repetition(Type.stingy, 0, 1);
	/**
	 * {@code *+}
	 */
	public static final Repetition ASTERISK_P = new Repetition(Type.possessive,
			0, Integer.MAX_VALUE);
	/**
	 * {@code ++}
	 */
	public static final Repetition PLUS_P = new Repetition(Type.possessive, 1,
			Integer.MAX_VALUE);
	/**
	 * {@code ?+}
	 */
	public static final Repetition QMARK_P = new Repetition(Type.possessive, 0,
			1);

	/**
	 * How much the pattern will hold onto matches.
	 */
	public final Type t;
	/**
	 * lowest acceptable number of matches
	 */
	public final int top;
	/**
	 * Highest number of acceptable matches. {@link Integer#MAX_VALUE}
	 * represents infinity.
	 */
	public final int bottom;

	/**
	 * Generates the given repetition object.
	 * 
	 * @param t
	 * @param bottom
	 * @param top
	 */
	public Repetition(Type t, int bottom, int top) {
		this.t = t;
		this.top = top;
		this.bottom = bottom;
	}

	@Override
	public String toString() {
		if (top == bottom) {
			if (top == 1)
				return "";
			return "{" + top + "}";
		}
		StringBuilder b = new StringBuilder();
		if (top == Integer.MAX_VALUE) {
			if (bottom == 0)
				b.append('*');
			else if (bottom == 1)
				b.append('+');
			else
				b.append('{').append(bottom).append(",}");
		} else if (top == 1)
			b.append('?');
		else {
			if (bottom == 0)
				b.append("{,").append(top).append('}');
			else
				b.append('{').append(bottom).append(',').append(top)
						.append('}');
		}
		if (t == Type.possessive)
			b.append('+');
		else if (t == Type.stingy)
			b.append('?');
		return b.toString();
	}

	/**
	 * @return whether this is a pointless repetition; i.e., top = bottom = 1
	 *         and it isn't possessive.
	 */
	public boolean redundant() {
		return top == 1 && bottom == 1 && t != Type.possessive;
	}
}
