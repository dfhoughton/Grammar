package dfh.grammar;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link LineReader} for multi-line strings.
 * 
 * @author David F. Houghton
 */
public class StringLineReader implements LineReader {
	private static final Pattern linePattern = Pattern.compile("^.*$",
			Pattern.MULTILINE);
	private final Matcher m;
	private int i = -1;

	public StringLineReader(String multiline) {
		m = linePattern.matcher(multiline);
	}

	@Override
	public String readLine() throws IOException {
		if (m.find()) {
			i++;
			return m.group();
		}
		return null;
	}

	@Override
	public int lineNumber() {
		return i;
	}

}
