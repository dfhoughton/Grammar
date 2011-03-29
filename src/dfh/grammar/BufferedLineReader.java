package dfh.grammar;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Implementation of {@link LineReader} to cover non-array data sources.
 * <p>
 * <b>Creation date:</b> Mar 29, 2011
 * 
 * @author David Houghton
 * 
 */
public class BufferedLineReader implements LineReader {
	private final BufferedReader reader;
	int lineNumber = 0;

	public BufferedLineReader(BufferedReader reader) {
		this.reader = reader;
	}

	@Override
	public String readLine() throws IOException {
		lineNumber++;
		return reader.readLine();
	}

	@Override
	public int lineNumber() {
		return lineNumber;
	}
}
