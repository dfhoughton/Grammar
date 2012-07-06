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
 * Facilitates the renaming of conditions for
 * {@link Grammar#defineRule(String, Grammar, String, Condition)}.
 * <p>
 * 
 * @author David F. Houghton - Mar 11, 2012
 * 
 */
public class WrappedCondition extends Condition {
	private static final long serialVersionUID = 2L;
	private Condition inner;

	WrappedCondition(String namebase, Condition inner) {
		this.inner = inner;
		name = namebase + ':' + inner.getName();
	}

	@Override
	public boolean passes(Match n, Matcher m, CharSequence s) {
		return inner.passes(n, m, s);
	}
}
