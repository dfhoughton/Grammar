package dfh.grammar;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a set of alternates.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 */
public class GroupFragment extends RepeatableRuleFragment {
	List<List<RuleFragment>> alternates = new LinkedList<List<RuleFragment>>();
	private List<RuleFragment> currentSequence = new LinkedList<RuleFragment>();

	public GroupFragment(List<RuleFragment> list) {
		currentSequence.addAll(list);
		alternates.add(currentSequence);
		currentSequence = new LinkedList<RuleFragment>();
	}

	public void add(RuleFragment fragment) {
		currentSequence.add(fragment);
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
			currentSequence = new LinkedList<RuleFragment>();
		}
	}

	/**
	 * Called when we find a closing bracket or the end of the input.
	 * 
	 * @throws GrammarException
	 */
	public void done() throws GrammarException {
		if (currentSequence.isEmpty())
			throw new GrammarException("empty alternate");
		alternates.add(currentSequence);
		currentSequence = null;
	}

	@Override
	public String stringify() {
		StringBuilder b = new StringBuilder();
		boolean nonInitial = false;
		b.append('[');
		for (List<RuleFragment> alternate : alternates) {
			if (nonInitial)
				b.append('|');
			else
				nonInitial = true;
			stringifySequence(b, alternate);
		}
		b.append(']');
		b.append(rep);
		return b.toString();
	}

	private void stringifySequence(StringBuilder b, List<RuleFragment> alternate) {
		boolean nonInitial = false;
		for (RuleFragment rf : alternate) {
			if (nonInitial)
				b.append(' ');
			else
				nonInitial = true;
			b.append(rf.stringify());
		}
	}

	/**
	 * @return a lightweight clone of this fragment with the repetition set to
	 *         <code>NONE</code>
	 */
	public GroupFragment noRep() {
		Iterator<List<RuleFragment>> i = alternates.iterator();
		GroupFragment clone = new GroupFragment(i.next());
		clone.rep = Repetition.NONE;
		while (i.hasNext())
			clone.alternates.add(i.next());
		return clone;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (List<RuleFragment> alternate : alternates) {
			if (b.length() > 1)
				b.append('|');
			appendAlternate(b, alternate);
		}
		b.append(']');
		b.append(rep);
		return b.toString();
	}

	private void appendAlternate(StringBuilder b, List<RuleFragment> alternate) {
		boolean nonFirst = false;
		for (RuleFragment rf : alternate) {
			if (nonFirst)
				b.append(' ');
			else
				nonFirst = true;
			b.append(rf);
		}
	}
}
