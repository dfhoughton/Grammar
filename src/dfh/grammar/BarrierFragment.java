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
 * Holds a backtracking barrier in a rule.
 * <p>
 * Used in rules such as
 * 
 * <pre>
 * {@code
 * <a> = <b> <c> : <d>
 * }
 * </pre>
 * 
 * where {@code :} means "given that {@code <b>} has matched somehow or other,
 * if we can't find a match for {@code <d>} don't tinker with the first two
 * elements any further: the whole rule fails." Similarly,
 * 
 * <pre>
 * {@code 
 * <a> = <b> <c> :: <d>
 * }
 * </pre>
 * 
 * where we have {@code ::} instead of {@code :}, means that if we can't
 * continue the match after the barrier then the entire grammar fails to match
 * at the offset it is now considering.
 * <p>
 * <b>Creation date:</b> Apr 6, 2011
 * 
 * @author David Houghton
 * 
 */
public class BarrierFragment implements RuleFragment {
	protected final String id;

	/**
	 * Generates a fragment from the given reference number.
	 * 
	 * @param single
	 *            whether it is a single or double colon barrier
	 */
	public BarrierFragment(boolean single) {
		this.id = single ? ":" : "::";
	}

	@Override
	public String toString() {
		return id;
	}
}
