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
 * {@link LogicalCondition} implementing boolean exclusive or.
 * <p>
 * 
 * @author David F. Houghton - Mar 15, 2012
 * 
 */
public class XORCondition extends LogicalCondition {
	private static final long serialVersionUID = 6L;

	public XORCondition(List<Condition> conditions) {
		super(conditions);
	}

	private XORCondition(int length) {
		super(length);
	}

	@Override
	protected boolean allPass(Match n, Matcher m, CharSequence s) {
		int count = 0;
		for (Condition c : subconditions) {
			if (c.passes(n, m, s))
				count++;
		}
		return count % 2 == 1;
	}

	@Override
	protected LogicalCondition duplicate() {
		XORCondition cj = new XORCondition(subconditions.length);
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
		return describe(" ^ ");
	}

}
