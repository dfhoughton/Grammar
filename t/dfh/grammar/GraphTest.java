package dfh.grammar;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests some graph-theoretic methods in {@link Match}.
 * <p>
 * 
 * @author David F. Houghton - Jan 12, 2013
 * 
 */
public class GraphTest {

	@Test
	public void widthTest() {
		Grammar g = new Grammar("rule = 'a'++");
		String s = "aaa";
		Match m = g.matches(s).match();
		assertEquals(3, m.width());
	}

	@Test
	public void sizeTest() {
		Grammar g = new Grammar("rule = 'a'++");
		String s = "aaa";
		Match m = g.matches(s).match();
		assertEquals(4, m.size());
	}

	@Test
	public void depthTest() {
		Grammar g = new Grammar("rule = 'a'++");
		String s = "aaa";
		Match m = g.matches(s).match();
		assertEquals(0, m.depth());
		assertEquals(1, m.child(0).depth());
	}

	@Test
	public void heightTest() {
		Grammar g = new Grammar("rule = 'a'++");
		String s = "aaa";
		Match m = g.matches(s).match();
		assertEquals(2, m.height());
		assertEquals(1, m.child(0).height());
	}
}
