package dfh.grammar;

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
	{
		alternates.add(currentSequence);
	}

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
		else
			currentSequence = new LinkedList<RuleFragment>();
	}

	/**
	 * Called when we find a closing bracket or the end of the input.
	 * 
	 * @throws GrammarException
	 */
	public void done() throws GrammarException {
		if (currentSequence.isEmpty())
			throw new GrammarException("empty alternate");
	}
}
