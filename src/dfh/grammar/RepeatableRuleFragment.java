package dfh.grammar;

import dfh.grammar.Repetition.Type;

/**
 * Represents a portion of a rule that may have a repetition modifier.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 * 
 */
public abstract class RepeatableRuleFragment implements RuleFragment {

	Repetition rep = Repetition.NONE;

	public void setRepetition(Repetition rep) {
		this.rep = rep;
	}

	protected void appendGreedinessModifier(StringBuilder b) {
		if (rep.t == Type.possessive)
			b.append('+');
		else if (rep.t == Type.stingy)
			b.append('?');
	}

	/**
	 * Stringifies the repetition suffix.
	 * 
	 * @param b
	 */
	protected void appendSuffix(StringBuilder b) {
		if (rep.bottom == 1) {
			if (rep.top == Integer.MAX_VALUE) {
				b.append('+');
			} else if (rep.top > 1) {
				b.append('{').append(rep.bottom).append(',').append(rep.top)
						.append('}');
			}
			appendGreedinessModifier(b);
		} else if (rep.bottom == 0) {
			if (rep.top == Integer.MAX_VALUE) {
				b.append('*');
			} else if (rep.top > 1) {
				b.append("{,").append(rep.top).append('}');
			} else {
				b.append('?');
			}
			appendGreedinessModifier(b);
		} else {
			b.append('{').append(rep.bottom);
			if (rep.top == Integer.MAX_VALUE) {
				b.append(",}");
			} else if (rep.bottom == rep.top) {
				b.append('}');
			} else {
				b.append(',').append(rep.top).append('}');
			}
			appendGreedinessModifier(b);
		}
	}

}
