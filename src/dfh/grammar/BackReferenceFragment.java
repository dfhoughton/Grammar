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
 * Holds a back reference in a rule.
 * <p>
 * Used in rules such as
 * 
 * <pre>
 * {@code
 * <a> = <b> 'foo' 1
 * }
 * </pre>
 * 
 * where 1 in this case refers back to whatever string was matched by
 * {@code <b>}.
 * <p>
 * <b>Creation date:</b> Mar 22, 2011
 * 
 * @author David Houghton
 * 
 */
public class BackReferenceFragment implements RuleFragment {
	protected int reference;

	/**
	 * Generates a fragment from the given reference number.
	 * 
	 * @param reference
	 */
	public BackReferenceFragment(int reference) {
		this.reference = reference - 1;
	}

	@Override
	public String toString() {
		return Integer.toString(reference + 1);
	}
}
