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
 * Corresponds to a literal expression: "this", for example.
 * <p>
 * <b>Creation date:</b> Mar 22, 2011
 * 
 * @author David Houghton
 * 
 */
public class LiteralFragment extends RepeatableRuleFragment {

	/**
	 * sequence to match
	 */
	public final String literal;

	/**
	 * Generates {@link LiteralFragment} with given sequence.
	 * 
	 * @param literal
	 */
	public LiteralFragment(String literal) {
		this.literal = literal;
	}

	@Override
	public String toString() {
		return '"' + literal + '"' + rep;
	}
}
