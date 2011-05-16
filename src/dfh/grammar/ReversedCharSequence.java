package dfh.grammar;

/**
 * {@link CharSequence} that allows the iteration over another
 * {@link CharSequence} in reverse. This is required for variable-length look
 * behind assertions.
 * 
 * Note that this class merely tinkers with the index math to retrieve
 * characters from the underlying sequence.
 * 
 * <b>Creation date:</b> Apr 10, 2011
 * 
 * @author David Houghton
 * 
 */
public class ReversedCharSequence implements CharSequence {

	private final int zero, end, length;
	private final CharSequence s;

	/**
	 * @param s
	 * @param offset
	 *            the index of the character in the parent sequence immediately
	 *            after the zero index character in the reversed sequence
	 * @param end
	 *            the index of the character in the parent sequence which is the
	 *            earliest character that should appear in the reversed
	 *            sequence; this is 0 if the reversed sequence should cover the
	 *            entire beginning of the parent sequence
	 */
	public ReversedCharSequence(CharSequence s, int offset, int end) {
		this.s = s;
		this.end = end - 1;
		if (end < -1 || offset > s.length()) {
			String baseError = "reversed character sequence must be a subsequence of parent sequence";
			if (end < -1)
				throw new IndexOutOfBoundsException(baseError
						+ "; specified end is before parent sequence beginning");
			else
				throw new IndexOutOfBoundsException(
						baseError
								+ "; specified start offset is after parent sequence end");
		}
		zero = offset - 1;
		length = zero - this.end;
	}

	public ReversedCharSequence(CharSequence s, int offset) {
		this(s, offset, 0);
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public char charAt(int index) {
		return s.charAt(zero - index);
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		return new ReversedCharSequence(s, start - zero, end - zero);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (int i = zero; i > end; i--)
			b.append(s.charAt(i));
		return b.toString();
	}

	/**
	 * @return {@link CharSequence} that has been reversed
	 */
	public CharSequence underlyingSequence() {
		return s;
	}
}
