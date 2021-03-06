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
 * Holds a condition in a rule.
 * <p>
 * Used in rules such as
 * 
 * <pre>
 * {@code
 * <a> = /\d++/ (less_than_20)
 * }
 * </pre>
 * 
 * <b>Creation date:</b> Apr 5, 2011
 * 
 * @author David Houghton
 * 
 */
public class ConditionFragment implements RuleFragment {
	protected String id;

	/**
	 * Generates a fragment representing the specified condition.
	 * 
	 * @param id
	 *            phrase identifying condition
	 */
	public ConditionFragment(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return '(' + id + ')';
	}
}
