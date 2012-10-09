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
 * Holds an assertion in a rule.
 * <p>
 * Used in rules such as
 * 
 * <pre>
 * {@code
 *    <a> = ~ '1' <rule>
 *    <b> = ! '1' <rule>
 *    <c> = ~ [ '1' | '2'] <rule>
 *    <d> = ~ '1' <rule>
 * <rule> = /\b\d++/
 * }
 * </pre>
 * 
 * The <code>~</code> and <code>!</code> mark positive and negative assertions,
 * respectively. Assertions have a width of zero, so they amount to a test which
 * doesn't consume any content.
 * <p>
 * <b>Creation date:</b> Apr 7, 2011
 * 
 * @author David Houghton
 * 
 */
public class AssertionFragment implements RuleFragment {
	protected final boolean positive;
	protected RuleFragment rf;
	protected final boolean forward;

	/**
	 * Generates a fragment with given assertion status.
	 * 
	 * @param positive
	 *            whether matching the assertion rule constitutes a match of the
	 *            assertion
	 * @param forward
	 *            whether the assertion rule matches what comes after (forward),
	 *            or what comes before
	 */
	public AssertionFragment(boolean positive, boolean forward) {
		this.positive = positive;
		this.forward = forward;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(positive ? '~' : '!');
		b.append(forward ? '+' : '-');
		b.append(rf);
		return b.toString();
	}
}
