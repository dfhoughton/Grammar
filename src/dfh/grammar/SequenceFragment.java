/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Represents a sequence of rules such as
 * 
 * <pre>
 * &lt;a&gt; = &lt;b&gt; &lt;c&gt;
 * </pre>
 * 
 * <p>
 * 
 * @author David Houghton
 */
public class SequenceFragment extends RepeatableRuleFragment {
	LinkedList<RuleFragment> sequence;

	public SequenceFragment() {
		sequence = new LinkedList<RuleFragment>();
	}

	public SequenceFragment(LinkedList<RuleFragment> subList) {
		sequence = subList;
	}

	public void add(RuleFragment fragment) {
		sequence.add(fragment);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (RuleFragment rf : sequence) {
			if (b.length() > 1)
				b.append(' ');
			b.append(rf);
		}
		return b.toString();
	}

	public boolean isEmpty() {
		return sequence.isEmpty();
	}

	public int size() {
		return sequence.size();
	}

	public RuleFragment first() {
		return sequence.peek();
	}

	public void addAll(SequenceFragment sf) {
		sequence.addAll(sf.sequence);
	}

	public RuleFragment last() {
		return sequence.peekLast();
	}

	public SequenceFragment nonConditions() {
		return new SequenceFragment(new LinkedList<RuleFragment>(
				sequence.subList(0, sequence.size() - 1)));
	}

	public RuleFragment get(int i) {
		return sequence.get(i);
	}

	public void set(int i, BackReferenceFragment f) {
		sequence.set(i, f);
	}

	public void clear() {
		sequence.clear();
	}

	public Iterator<RuleFragment> iterator() {
		return sequence.iterator();
	}

	public SequenceFragment copy() {
		// TODO Auto-generated method stub
		return new SequenceFragment(new LinkedList<RuleFragment>(sequence));
	}
}
