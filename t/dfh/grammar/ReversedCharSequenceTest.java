package dfh.grammar;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Just to make sure {@link ReversedCharSequence} class works as intended.
 * <p>
 * <b>Creation date:</b> Apr 10, 2011
 * 
 * @author David Houghton
 * 
 */
public class ReversedCharSequenceTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void toStringTest() {
		String s = "abc";
		ReversedCharSequence rcs = new ReversedCharSequence(s, s.length());
		assertTrue("reversed string", rcs.toString().equals("cba"));
	}

	@Test
	public void lengthTest() {
		String s = "abc";
		ReversedCharSequence rcs = new ReversedCharSequence(s, s.length());
		assertTrue("figured out length", rcs.length() == 3);
	}

	@Test
	public void charAtTest0() {
		String s = "abc";
		ReversedCharSequence rcs = new ReversedCharSequence(s, s.length());
		assertTrue("got char 0", rcs.charAt(0) == 'c');
	}

	@Test
	public void charAtTest1() {
		String s = "abc";
		ReversedCharSequence rcs = new ReversedCharSequence(s, s.length());
		assertTrue("got char 1", rcs.charAt(1) == 'b');
	}

	@Test
	public void substringTest1() {
		String s = "abcdefghijklmnopqrstuvwxyz";
		ReversedCharSequence rcs = new ReversedCharSequence(s, 5);
		assertTrue("reversed string", rcs.toString().equals("edcba"));
	}

	@Test
	public void substringTest2() {
		String s = "abcdefghijklmnopqrstuvwxyz";
		ReversedCharSequence rcs = new ReversedCharSequence(s, 5, 1);
		assertTrue("reversed string", rcs.toString().equals("edcb"));
	}
}
