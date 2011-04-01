package dfh.grammar;

import java.io.IOException;

/**
 * Implementation of {@link LineReader} to cover string arrays.
 * <p>
 * <b>Creation date:</b> Mar 29, 2011
 * 
 * @author David Houghton
 * 
 */
public class ArrayLineReader implements LineReader {
	private final String[] lines;
	private int index = 0;

	/**
	 * Generates a {@link LineReader} that will iterate over the given strings.
	 * 
	 * @param lines
	 */
	public ArrayLineReader(String[] lines) {
		this.lines = lines;
	}

	@Override
	public String readLine() throws IOException {
		if (index == lines.length)
			return null;
		return lines[index++];
	}

	@Override
	public int lineNumber() {
		return index;
	}
}
