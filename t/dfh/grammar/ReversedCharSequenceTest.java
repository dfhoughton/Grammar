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

	@Test
	public void oneArgConstructorTest() {
		String s = "abc";
		ReversedCharSequence rcs = new ReversedCharSequence(s);
		assertTrue("one arg constructor worked", "cba".equals(rcs.toString()));
	}

	@Test
	public void subsequenceTest() {
		String s = "abc";
		ReversedCharSequence rcs = new ReversedCharSequence(s);
		assertTrue("returned correct subsequence", rcs.subSequence(0, 1)
				.toString().equals("c"));
	}

	@Test
	public void doubleReversalTest() {
		String s = "abc";
		ReversedCharSequence rcs = new ReversedCharSequence(s);
		rcs = new ReversedCharSequence(rcs);
		assertTrue("double reversal worked", rcs.toString().equals("abc"));
	}
}
