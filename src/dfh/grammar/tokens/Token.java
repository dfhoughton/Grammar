package dfh.grammar.tokens;

/**
 * Bare minimum representation of a token.
 * <p>
 * 
 * @author David F. Houghton - Mar 29, 2012
 * 
 */
public abstract class Token {
	private final int start;
	private final int end;

	public Token(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int start() {
		return start;
	}

	public int end() {
		return end;
	}
}
