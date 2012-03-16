/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.util.List;

/**
 * {@link LogicalCondition} that implements boolean negation.
 * <p>
 * 
 * @author David F. Houghton - Mar 15, 2012
 * 
 */
public class NegationCondition extends LogicalCondition {
	private static final long serialVersionUID = 6L;

	public NegationCondition(List<Condition> conditions) {
		super(conditions);
	}

	private NegationCondition(int length) {
		super(length);
	}

	/**
	 * Despite it's name, only a single condition is tested, as negation is a
	 * unary operator.
	 * 
	 * @see dfh.grammar.LogicalCondition#allPass(dfh.grammar.Match,
	 *      dfh.grammar.Matcher, java.lang.CharSequence)
	 */
	@Override
	protected boolean allPass(Match n, Matcher m, CharSequence s) {
		return !subconditions[0].passes(n, m, s);
	}

	@Override
	protected LogicalCondition duplicate() {
		NegationCondition cj = new NegationCondition(subconditions.length);
		for (int i = 0; i < subconditions.length; i++) {
			Condition c = subconditions[i];
			if (c instanceof LogicalCondition)
				c = ((LogicalCondition) c).duplicate();
			cj.subconditions[i] = c;
		}
		return cj;
	}

	@Override
	protected String describe() {
		Condition cnd = subconditions[0];
		if (cnd instanceof LogicalCondition)
			return "!(" + cnd.describe() + ')';
		return '!' + cnd.describe();
	}

}
