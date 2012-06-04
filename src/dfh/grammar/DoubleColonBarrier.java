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
 * {@link Exception} thrown when a rule attempts to backtrack into a double
 * colon backtracking barrier. This error will be caught by the root
 * {@link Matcher} and the entire match will fail.
 * <p>
 * <b>Creation date:</b> Apr 6, 2011
 * 
 * @author David Houghton
 * 
 */
public class DoubleColonBarrier extends BarrierException {
	private static final long serialVersionUID = 7L;

	/**
	 * @param m
	 *            barrier rule's matcher
	 */
	public DoubleColonBarrier(Matcher m) {
		super(m, "::");
	}
}
