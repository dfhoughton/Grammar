package dfh.grammar;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

/**
 * Makes sure logging of matching process is working as expected.
 * <p>
 * 
 * @author David F. Houghton - Jun 11, 2012
 * 
 */
public class LoggingTest {

	@Test
	public void quantifiedLabels() {
		Grammar r = new Grammar("rule = 'a' <s>? 'a'\ns = /\\s++/");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream stream = new PrintStream(baos);
		Match m = r.find("aa", new Options().log(stream)).match();
		assertNotNull(m);
		stream.close();
		String s = baos.toString();
		assertTrue(s.contains("matching <s>?"));
	}

}
