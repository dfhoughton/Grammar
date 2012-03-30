package dfh.grammar.tokens;

/**
 * Represents the common case where tokens bear tags from a closed class. The
 * tag class in this case is represented by an enum for the various efficiencies
 * this brings.
 * <p>
 * 
 * @author David F. Houghton - Mar 29, 2012
 * 
 * @param <T>
 */
public class TaggedToken<T extends Enum<T>> extends Token {

	private final T t;

	public TaggedToken(int start, int end, T t) {
		super(start, end);
		this.t = t;
	}

	public T tag() {
		return t;
	}
}
