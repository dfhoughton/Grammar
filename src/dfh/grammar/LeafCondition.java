/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.io.Serializable;

/**
 * Place holder for undefined condition.
 * 
 * @author David F. Houghton
 */
class LeafCondition extends Condition implements Serializable {
	private static final long serialVersionUID = 8L;
	protected String cnd;

	LeafCondition(String cnd) {
		name = this.cnd = cnd;
	}
}
