package dfh.grammar;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * Tests the various uses of the <code>n^</code> construct.
 * <p>
 * <b>Creation date:</b> Dec 10, 2011
 * 
 * @author David Houghton
 * 
 */
public class UplevelBackreferenceTest {

	@Test
	public void compilation1() {
		try {
			new Grammar(new String[] { "ROOT = /[ab]/ 'foo' 1^{2}" });
		} catch (GrammarException e) {
			fail("threw exception while compiling 1^{2}");
		}
	}

	@Test
	public void compilation2() {
		try {
			new Grammar(new String[] { "ROOT = /[ab]/ 'foo' [ 1^ | 'c' ]" });
		} catch (GrammarException e) {
			fail("threw exception while compiling [ 1^ | 'c' ]");
		}
	}

	@Test
	public void compilation3() {
		try {
			new Grammar(new String[] { "ROOT = /[ab]/ 'foo' [ 0^ | 'c' ]" });
			fail("should have thrown exception");
		} catch (GrammarException e) {
			assertTrue(e.getCause().getMessage()
					.equals("back references must be greater than 0"));
		}
	}

	@Test
	public void compilation4() {
		try {
			new Grammar(new String[] { "ROOT = /[ab]/ 'foo' [ 3^ | 'c' ]" });
			fail("should have thrown exception");
		} catch (GrammarException e) {
			assertTrue(e.getMessage().indexOf("references its own position") > -1);
		}
	}

	@Test
	public void compilation5() {
		try {
			new Grammar(new String[] { "ROOT = /[ab]/ 'foo' 3^++" });
			fail("should have thrown exception");
		} catch (GrammarException e) {
			assertTrue(e.getMessage().indexOf("references its own position") > -1);
		}
	}

	@Test
	public void compilation6() {
		try {
			new Grammar(new String[] { "ROOT = /[ab]/ 'foo' [ 4^ | 'c' ]" });
			fail("should have thrown exception");
		} catch (GrammarException e) {
			assertTrue(e.getMessage().indexOf(
					"references position after its own") > -1);
		}
	}

	@Test
	public void compilation7() {
		try {
			new Grammar(new String[] { "ROOT = /[ab]/ 'foo' 4^++" });
			fail("should have thrown exception");
		} catch (GrammarException e) {
			assertTrue(e.getMessage().indexOf(
					"references position after its own") > -1);
		}
	}

	@Test
	public void repetition() {
		Grammar g = new Grammar(new String[] { "ROOT = /[ab]/ 'foo' 1^++" });
		int count = 0;
		Matcher m = g.find("afooaa bfoobb afoob bfooa");
		while (m.match() != null)
			count++;
		assertTrue("found repetition", count == 2);
	}

	@Test
	public void group1() {
		Grammar g = new Grammar(
				new String[] { "ROOT = /[ab]/ 'foo' [ 1^ | 'c' ]" });
		int count = 0;
		Matcher m = g.find("afooa bfoob afoob bfooc");
		while (m.match() != null)
			count++;
		assertTrue("found repetition", count == 3);
	}

	@Test
	public void group2() {
		Grammar g = new Grammar(
				new String[] { "ROOT = /[ab]/ 'foo' [ 1^ | 'c' ]{2}" });
		int count = 0;
		Matcher m = g.find("afooac bfoocb afoob bfoocc");
		while (m.match() != null)
			count++;
		assertTrue("found repetition", count == 3);
	}

	@Test
	public void group3() {
		Grammar g = new Grammar(
				new String[] { "ROOT = /[ab]/ 'foo' [ [ 1^ | 'c' ]{2} | 'd' ]" });
		int count = 0;
		Matcher m = g.find("afooac bfoocb afoob bfood");
		while (m.match() != null)
			count++;
		assertTrue("found repetition", count == 3);
	}

	@Test
	public void group4() {
		Grammar g = new Grammar(
				new String[] { "ROOT = /[ab]/ 'foo' [ [ 1^{2} | 'c' ]{2} | 'd' ]" });
		int count = 0;
		Matcher m = g.find("afooaac bfoocbb afoob bfood");
		while (m.match() != null)
			count++;
		assertTrue("found repetition", count == 3);
	}
}
