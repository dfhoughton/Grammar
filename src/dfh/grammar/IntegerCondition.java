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
 * Convenience base class for defining conditions on rules that match integers.
 * <p>
 * <b>Creation date:</b> Nov 20, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class IntegerCondition extends Condition implements
		Serializable {
	private static final long serialVersionUID = 8L;

	@Override
	public boolean passes(Match n, Matcher m, CharSequence s) {
		CharSequence ss = Condition.subsequence(n, s);
		try {
			int i = Integer.parseInt(ss.toString());
			return passes(i);
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Determines which integers pass the condition. This is the only method you
	 * must implement when defining an {@link IntegerCondition}.
	 * 
	 * @param i
	 *            integer to test
	 * @return whether the integer passes the test
	 */
	public abstract boolean passes(int i);
}
