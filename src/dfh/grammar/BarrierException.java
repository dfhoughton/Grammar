/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

/**
 * {@link Exception} thrown when a rule attempts to backtrack into a
 * backtracking barrier. This error will be caught by the root {@link Matcher}
 * and the entire match will fail.
 * <p>
 * <b>Creation date:</b> Jul 14, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class BarrierException extends GrammarException {
	private static final long serialVersionUID = 6L;

	/**
	 * Generates error message appropriate to the barrier breached.
	 * 
	 * @param m
	 *            backtracking barrier matcher
	 * @param type
	 *            barrier type -- ':' or '::'
	 */
	protected BarrierException(Matcher m, String type) {
		super("hit " + type + " barrier in " + m.master.rule().label);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Throwable#fillInStackTrace()
	 * 
	 * speeds things up
	 */
	@Override
	public Throwable fillInStackTrace() {
		return null;
	}

}
