package dfh.grammar;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import dfh.grammar.util.Dotify;
import static org.junit.Assert.*;

/**
 * Tests whether we can produce left-branching parse trees such as
 * 
 * <pre>
 *         AP
 *        /  \
 *       DP   a
 *      /  \
 *     AP   s
 *    /  \
 *   DP   a
 *  /  \
 * AP   s
 * |
 * a
 * </pre>
 * 
 * <b>Creation date:</b> May 16, 2011
 * 
 * @author David Houghton
 * 
 */
public class DeepRecursionTest {

	private static Grammar gQuantification, gAlternation;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String[] rulesQuantification = {
				//
				"<ROOT> = <AP>", //
				"<AP> = <DP>? 'a'",//
				"<DP> = <AP> 's'",//
		};
		gQuantification = new Grammar(rulesQuantification);
		String[] rulesAlternation = {
				//
				"<ROOT> = <AP>", //
				"<AP> = <DP> 'a' | 'a'",//
				"<DP> = <AP> 's'",//
		};
		gAlternation = new Grammar(rulesAlternation);
	}

	@Test
	public void test0Quantification() throws GrammarException, IOException {
		String s = "asa";
		Matcher m = gQuantification.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void test1Quantification() throws GrammarException, IOException {
		String s = "asa";
		Matcher m = gQuantification.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void test2Quantification() throws GrammarException, IOException {
		String s = "asasa";
		Matcher m = gQuantification.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void test3NullQuantification() throws GrammarException, IOException {
		String s = "asasasa";
		Matcher m = gQuantification.matches(s);
		Match n = m.match();
		assertNull(n);
	}

	@Test
	public void test3NotNullQuantification() throws GrammarException,
			IOException {
		String s = "asasasa";
		Options opt = new Options();
		opt.maxRecursionDepth(4);
		Matcher m = gQuantification.matches(s, opt);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void test0Alternation() throws GrammarException, IOException {
		String s = "asa";
		Matcher m = gAlternation.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void test1Alternation() throws GrammarException, IOException {
		String s = "asa";
		Matcher m = gAlternation.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void test2Alternation() throws GrammarException, IOException {
		String s = "asasa";
		Matcher m = gAlternation.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void test3Alternation() throws GrammarException, IOException {
		String s = "asasasa";
		Matcher m = gAlternation.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void test3NullAlternation() throws GrammarException, IOException {
		String s = "asasasasa";
		Matcher m = gAlternation.matches(s);
		Match n = m.match();
		assertNull(n);
	}

	@Test
	public void test3NotNullAlternation() throws GrammarException, IOException {
		String s = "asasasasa";
		Options opt = new Options();
		opt.maxRecursionDepth(4);
		Matcher m = gAlternation.matches(s, opt);
		Match n = m.match();
		assertNotNull(n);
	}
}
