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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dfh.grammar.util.Dotify;

/**
 * Base class for conditions representing logical combinations of other
 * conditions.
 * <p>
 * <b>Creation date:</b> Dec 5, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class LogicalCondition extends Condition implements
		Serializable {
	private static final long serialVersionUID = 6L;
	protected final Condition[] subconditions;
	public static final MatchTest expOrGroupTest = new MatchTest() {
		@Override
		public boolean test(Match m) {
			return m.hasLabel("exp") || m.hasLabel("group")
					|| m.hasLabel("cnd");
		}
	};

	public LogicalCondition(List<Condition> conditions) {
		subconditions = conditions.toArray(new Condition[conditions.size()]);
	}

	protected LogicalCondition(int length) {
		subconditions = new Condition[length];
	}

	@Override
	public boolean passes(Match n, Matcher m, CharSequence s) {
		return allPass(n, m, s);
	}

	/**
	 * Trims off the root node of the match tree then recursively parses the
	 * remainder.
	 * 
	 * @param m
	 *            root node of parse tree for condition
	 * @return complex condition
	 */
	static Condition manufacture(Match m) {
		m = m.children()[0];
		return mfct(m);
	}

	private static Condition mfct(Match m) {
		if (m.hasLabel("cnd"))
			return new LeafCondition(m.group());
		if (m.hasLabel("group"))
			return mfct(m.first("exp").children()[0]);
		if (m.hasLabel("exp"))
			return mfct(m.children()[0]);
		try {
			List<Condition> list = new ArrayList<Condition>();
			List<Match> constituents = m.closest(expOrGroupTest);
			for (Match sm : constituents)
				list.add(mfct(sm));
			if (m.hasLabel("conj"))
				return new ConjunctionCondition(list);
			if (m.hasLabel("disj"))
				return new DisjunctionCondition(list);
			if (m.hasLabel("xor"))
				return new XORCondition(list);
			if (m.hasLabel("neg"))
				return new NegationCondition(list);
		} catch (Exception e) {
			System.err.println("unable to parse condition " + m.group());
			System.out.println(Dotify.dot(m));
		}
		return null;
	}

	/**
	 * Find where this condition belongs in the logic tree and plug it in.
	 * 
	 * @param name
	 *            condition name as given in the grammar
	 * @param c
	 *            actual condition
	 * @return whether the condition was used
	 */
	boolean replace(String name, Condition c) {
		boolean used = false;
		for (int i = 0; i < subconditions.length; i++) {
			Condition sc = subconditions[i];
			if (sc instanceof LeafCondition) {
				LeafCondition lc = (LeafCondition) sc;
				if (lc.cnd.equals(name)) {
					subconditions[i] = c;
					used = true;
				}
			} else if (sc instanceof LogicalCondition) {
				boolean b = ((LogicalCondition) subconditions[i]).replace(name,
						c);
				used |= b;
			}
		}
		return used;
	}

	/**
	 * Returns set of names for {@link LeafCondition conditions} that must be
	 * defined to specify this logical condition.
	 * 
	 * @return set of names for {@link LeafCondition conditions} that must be
	 *         defined to specify this logical condition
	 */
	@Override
	public Set<String> conditionNames() {
		Set<String> set = new HashSet<String>();
		conditionNames(set);
		return set;
	}

	protected void conditionNames(Set<String> set) {
		for (Condition c : subconditions) {
			String name = c.getName();
			if (name != null)
				set.add(name);
			if (c instanceof LogicalCondition)
				((LogicalCondition) c).conditionNames(set);
		}
	}

	protected abstract LogicalCondition duplicate();

	protected abstract boolean allPass(Match n, Matcher m, CharSequence s);

	@Override
	protected abstract String describe();

	/**
	 * Used to implement {@link #describe()} for various logical condition
	 * subtypes.
	 * 
	 * @param operator
	 *            string representing the logical operator at work in this
	 *            condition
	 * @return string representing this logical condition
	 */
	protected String describe(String operator) {
		StringBuilder b = new StringBuilder();
		boolean initial = true;
		for (Condition cnd : subconditions) {
			if (initial)
				initial = false;
			else
				b.append(operator);
			if (cnd instanceof LogicalCondition)
				b.append('(').append(cnd.describe()).append(')');
			else
				b.append(cnd.describe());
		}
		return b.toString();
	}

	@Override
	Condition copy(String namebase, Set<String> knownConditions) {
		List<Condition> copies = new ArrayList<Condition>(subconditions.length);
		for (int i = 0; i < subconditions.length; i++)
			copies.add(subconditions[i].copy(namebase, knownConditions));
		LogicalCondition lc = null;
		if (this instanceof ConjunctionCondition)
			lc = new ConjunctionCondition(copies);
		else if (this instanceof DisjunctionCondition)
			lc = new DisjunctionCondition(copies);
		else if (this instanceof XORCondition)
			lc = new XORCondition(copies);
		else if (this instanceof NegationCondition)
			lc = new NegationCondition(copies);
		lc.setName(lc.describe());
		return lc;
	}
}
