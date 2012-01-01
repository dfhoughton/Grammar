package dfh.grammar;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class StringLineReaderTest {

	@Test
	public void test1() throws IOException {
		String test = "a\nb\nc";
		StringLineReader r = new StringLineReader(test);
		int count = 0;
		while (r.readLine() != null)
			count++;
		assertTrue("found right number of lines", count == 3);
	}

	@Test
	public void test2() throws IOException {
		String test = "a\nb\nc\n";
		StringLineReader r = new StringLineReader(test);
		int count = 0;
		while (r.readLine() != null)
			count++;
		assertTrue("found right number of lines", count == 3);
	}

	@Test
	public void lineNumberTest() throws IOException {
		String test = "a\nb\nc\n";
		StringLineReader r = new StringLineReader(test);
		int count = 0;
		while (r.readLine() != null) {
			assertTrue("correct line number", r.lineNumber() == count);
			count++;
		}
	}

	@Test
	public void contentTest1() throws IOException {
		String test = "a\nb\nc\n";
		StringLineReader r = new StringLineReader(test);
		int count = 0;
		String line;
		while ((line = r.readLine()) != null) {
			switch (count) {
			case 0:
				"a".equals(line);
				break;
			case 1:
				"b".equals(line);
				break;
			case 2:
				"c".equals(line);
				break;
			default:
				fail("too many lines");
			}
			count++;
		}
	}

	@Test
	public void contentTest2() throws IOException {
		String test = " a\n  b\n   c\n";
		StringLineReader r = new StringLineReader(test);
		int count = 0;
		String line;
		while ((line = r.readLine()) != null) {
			switch (count) {
			case 0:
				" a".equals(line);
				break;
			case 1:
				"  b".equals(line);
				break;
			case 2:
				"   c".equals(line);
				break;
			default:
				fail("too many lines");
			}
			count++;
		}
	}

	@Test
	public void contentTest3() throws IOException {
		String test = " a \n  b  \n   c   \n";
		StringLineReader r = new StringLineReader(test);
		int count = 0;
		String line;
		while ((line = r.readLine()) != null) {
			switch (count) {
			case 0:
				" a ".equals(line);
				break;
			case 1:
				"  b  ".equals(line);
				break;
			case 2:
				"   c   ".equals(line);
				break;
			default:
				fail("too many lines");
			}
			count++;
		}
	}

}
