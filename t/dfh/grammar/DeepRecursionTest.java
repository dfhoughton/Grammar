package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

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

	private static Grammar gQuantification, gAlternation, gNLP;

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
		String[] npRules = {
				//
				"<ROOT> = <NP>",//
				"<NP> = [<DP> <s>]? <N>",//
				"<N> = [<AP> <s>]? 'n'",//
				"<AP> = <AP>? <A>",//
				"<A> = ['adv' <s>]* 'a'",//
				"<DP> = <POS> | 'the'",//
				"<POS> = <NP> \"'s\"",//
				"<s> = ' '++",//
		};
		gNLP = new Grammar(npRules);
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

	@Test
	public void npTestBareNoun() {
		String s = "n";
		Matcher m = gNLP.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void npTestDtNoun() {
		String s = "the n";
		Matcher m = gNLP.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void npTestDtAdjNoun() {
		String s = "the a n";
		Matcher m = gNLP.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void npTestPosDetNP() {
		String s = "the n's n";
		Matcher m = gNLP.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void npTestPosDetAPNP() {
		String s = "the n's adv a n";
		Matcher m = gNLP.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

	@Test
	public void npTestPosDetAPNPNP() {
		String s = "the n's adv a n's n";
		Matcher m = gNLP.matches(s);
		Match n = m.match();
		assertNotNull(n);
	}

}
