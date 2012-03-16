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
 * {@link LogicalCondition} implementing boolean conjunction.
 * <p>
 * 
 * @author David F. Houghton - Mar 15, 2012
 * 
 */
public class ConjunctionCondition extends LogicalCondition {
	private static final long serialVersionUID = 6L;

	public ConjunctionCondition(List<Condition> conditions) {
		super(conditions);
	}

	private ConjunctionCondition(int length) {
		super(length);
	}

	@Override
	protected boolean allPass(Match n, Matcher m, CharSequence s) {
		for (Condition c : subconditions) {
			if (!c.passes(n, m, s))
				return false;
		}
		return true;
	}

	@Override
	protected LogicalCondition duplicate() {
		ConjunctionCondition cj = new ConjunctionCondition(subconditions.length);
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
		return describe(" ");
	}

}
