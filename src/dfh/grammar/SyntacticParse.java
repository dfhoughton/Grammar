package dfh.grammar;

import java.util.Set;
import java.util.TreeSet;

/**
 * Return value struct for {@link RuleParser#next()}.
 * <p>
 * 
 * @author David F. Houghton - Jun 18, 2012
 * 
 */
final class SyntacticParse {
	final Label l;
	final SequenceFragment f;
	final ConditionFragment c;
	final String text;

	SyntacticParse(String text, Label l, SequenceFragment f, ConditionFragment c) {
		this.text = text;
		this.l = l;
		this.f = f;
		this.c = c;
	}

	@Override
	public String toString() {
		return text;
	}

	/**
	 * @return those {@link HiddenSpace} labels used in this sequence
	 */
	Set<Label> getHiddenSpaces() {
		Set<Label> set = new TreeSet<Label>();
		for (RuleFragment rf : f.sequence) {
			addHiddenSpaceLabels(set, rf);
			if (set.size() == 2)
				return set;
		}
		return set;
	}

	public void addHiddenSpaceLabels(Set<Label> set, RuleFragment rf) {
		if (set.size() == 2)
			return;
		if (rf instanceof Label) {
			if (rf == HiddenSpace.LABEL || rf == HiddenSpace.ASSERTION_LABEL)
				set.add((Label) rf);
		} else if (rf instanceof AssertionFragment) {
			AssertionFragment af = (AssertionFragment) rf;
			addHiddenSpaceLabels(set, af.rf);
		} else if (rf instanceof GroupFragment) {
			GroupFragment gf = (GroupFragment) rf;
			for (SequenceFragment sf : gf.alternates)
				addHiddenSpaceLabels(set, sf);
		} else if (rf instanceof SequenceFragment) {
			SequenceFragment sf = (SequenceFragment) rf;
			for (RuleFragment rf2 : sf.sequence)
				addHiddenSpaceLabels(set, rf2);
		}
	}
}
