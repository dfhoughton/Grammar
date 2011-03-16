package dfh.grammar;

/**
 * Exception class for exceptions anticipated by this library. This is made an
 * extension of {@link RuntimeException} to reduce the necessity for error
 * catching blocks. Almost all exceptions are thrown during grammar compilation
 * rather than runtime except those caused by incomplete definition of terminal
 * rules.
 * <p>
 * <b>Creation date:</b> Mar 10, 2011
 * 
 * @author David Houghton
 * 
 */
public class GrammarException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public GrammarException(String message) {
		super(message);
	}
}
