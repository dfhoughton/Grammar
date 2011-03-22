package dfh.grammar;

import java.io.IOException;

/**
 * Basic line iterator interface so we can parse {@link Grammar Grammars} out of
 * both arrays and various other streamable data sources.
 * <p>
 * <b>Creation date:</b> Mar 18, 2011
 * 
 * @author David Houghton
 * 
 */
interface LineReader {
	String readLine() throws IOException;

	int lineNumber();
}