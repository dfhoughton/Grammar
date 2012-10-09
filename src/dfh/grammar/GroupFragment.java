/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a set of alternates such as
 * 
 * <pre>
 * {@code 
 * <a> = <b> | <c>
 * }
 * </pre>
 * 
 * This is a companion to {@link AlternationRule}.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 */
public class GroupFragment extends RepeatableRuleFragment {
	List<SequenceFragment> alternates = new LinkedList<SequenceFragment>();
	SequenceFragment currentSequence;
	Set<String> alternateTags = new TreeSet<String>();
	private boolean did = false;

	public GroupFragment(SequenceFragment list, Set<String> alternateTags) {
		currentSequence = list.copy();
		alternates.add(currentSequence);
		currentSequence = new SequenceFragment();
		this.alternateTags.addAll(alternateTags);
	}

	public void add(RuleFragment fragment) {
		currentSequence.add(fragment);
	}

	/**
	 * @return last fragment added
	 */
	public RuleFragment last() {
		return currentSequence.last();
	}

	/**
	 * Called when we find a pipe character.
	 * 
	 * @throws GrammarException
	 */
	public void newSequence() throws GrammarException {
		if (currentSequence.isEmpty())
			throw new GrammarException("empty alternate");
		else {
			alternates.add(currentSequence);
			currentSequence = new SequenceFragment();
		}
	}

	/**
	 * Called when we find a closing bracket or the end of the input.
	 * 
	 * @throws GrammarException
	 */
	public void done() throws GrammarException {
		if (did)
			return;
		did = true;
		if (currentSequence.isEmpty())
			throw new GrammarException("empty alternate");
		alternates.add(currentSequence);
		currentSequence = null;
	}

	@Override
	public String toString() {
		return alternateString() + rep;
	}

	String alternateString() {
		if (alternates.size() == 1 && alternates.get(0).size() == 1)
			return alternates.get(0).first().toString();
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (SequenceFragment alternate : alternates) {
			if (b.length() > 1)
				b.append('|');
			b.append(alternate);
		}
		b.append(']');
		return b.toString();
	}
}
