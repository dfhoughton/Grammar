package dfh.grammar;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Based on {@link SimpleMatchTest}. Tests comments and blank lines.
 * <p>
 * <b>Creation date:</b> Mar 22, 2011
 * 
 * @author David Houghton
 * 
 */
/**
 * <b>Creation date:</b> Oct 15, 2011
 * 
 * @author David Houghton
 * 
 */
public class MismatchTest {

	private static Grammar g;
	private static Options opt = new Options().keepRightmost(true);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String[] rules = {
		//
		"<ROOT> = 'a'++ /\\b/", //
		};
		g = new Grammar(rules);
	}

	@Test
	public void matchTest() {
		String s = "aaa";
		Matcher m = g.matches(s, opt);
		Match n = m.match();
		assertTrue("in case of a match, it equals rightmost",
				n == m.rightmostMatch());
	}

	@Test
	public void lookingAtTest() {
		String s = "aaa";
		Matcher m = g.lookingAt(s, opt);
		Match n = m.match();
		assertTrue("in case of a match, it equals rightmost",
				n == m.rightmostMatch());
	}

	@Test
	public void findTest() {
		String s = "aaa";
		Matcher m = g.find(s, opt);
		Match n = m.match();
		assertTrue("in case of a match, it equals rightmost",
				n == m.rightmostMatch());
	}

	@Test
	public void matchMismatchTest() {
		String s = "aab";
		Matcher m = g.matches(s, opt);
		Match n = m.match();
		assertNull("no match", n);
		assertNotNull("there is rightmost match", m.rightmostMatch());
		assertTrue("rightmost offset is 2", m.rightmostMatch().end() == 2);
	}

	@Test
	public void lookingAtMismatchTest() {
		String s = "aab";
		Matcher m = g.lookingAt(s, opt);
		Match n = m.match();
		assertNull("no match", n);
		assertNotNull("there is rightmost match", m.rightmostMatch());
		assertTrue("rightmost offset is 2", m.rightmostMatch().end() == 2);
	}

	@Test
	public void findMismatchTest() {
		String s = "aab";
		Matcher m = g.find(s, opt);
		Match n = m.match();
		assertNull("no match", n);
		assertNotNull("there is rightmost match", m.rightmostMatch());
		assertTrue("rightmost offset is 2", m.rightmostMatch().end() == 2);
	}

	@Test
	public void rightmostHasGroup() {
		String s = "aab";
		Matcher m = g.find(s, opt);
		Match n = m.match();
		assertNull("no match", n);
		assertNotNull("rightmost has group", m.rightmostMatch().group());
	}
}
