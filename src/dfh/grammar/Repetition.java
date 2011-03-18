package dfh.grammar;

/**
 * Repetition rule to be attached to a rule.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 * 
 */
public class Repetition {
	public enum Type {
		greedy, stingy, possessive
	}

	/**
	 * The repetition type of unmodified rules.
	 */
	public static final Repetition NONE = new Repetition(Type.possessive, 1, 1);
	public static final Repetition ASTERISK = new Repetition(Type.greedy, 0,
			Integer.MAX_VALUE);
	public static final Repetition PLUS = new Repetition(Type.greedy, 1,
			Integer.MAX_VALUE);
	public static final Repetition QMARK = new Repetition(Type.greedy, 0, 1);
	public static final Repetition ASTERISK_Q = new Repetition(Type.stingy, 0,
			Integer.MAX_VALUE);
	public static final Repetition PLUS_Q = new Repetition(Type.stingy, 1,
			Integer.MAX_VALUE);
	public static final Repetition QMARK_Q = new Repetition(Type.stingy, 0, 1);
	public static final Repetition ASTERISK_P = new Repetition(Type.possessive,
			0, Integer.MAX_VALUE);
	public static final Repetition PLUS_P = new Repetition(Type.possessive, 1,
			Integer.MAX_VALUE);
	public static final Repetition QMARK_P = new Repetition(Type.possessive, 0,
			1);

	public final Type t;
	public final int top, bottom;

	public Repetition(Type t, int bottom, int top) {
		this.t = t;
		this.top = top;
		this.bottom = bottom;
	}

	@Override
	public String toString() {
		if (top == bottom) {
			if (top == 1)
				return "";
			return "{" + top + "}";
		}
		StringBuilder b = new StringBuilder();
		if (top == Integer.MAX_VALUE) {
			if (bottom == 0)
				b.append('*');
			else if (bottom == 1)
				b.append('+');
			else
				b.append('{').append(bottom).append(",}");
		} else if (top == 1)
			b.append('?');
		else {
			if (bottom == 0)
				b.append("{,").append(top).append('}');
			else
				b.append('{').append(bottom).append(',').append(top)
						.append('}');
		}
		if (t == Type.possessive)
			b.append('+');
		else if (t == Type.stingy)
			b.append('?');
		return b.toString();
	}
}
