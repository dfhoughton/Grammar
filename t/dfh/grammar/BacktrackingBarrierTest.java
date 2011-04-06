package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

/**
 * Makes sure backtracking barriers work to spec.
 * <p>
 * <b>Creation date:</b> Apr 6, 2011
 * 
 * @author David Houghton
 * 
 */
public class BacktrackingBarrierTest {

	@Test
	public void compilationTest1() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = : 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			fail("didn't catch initial colon error");
		} catch (Exception e) {
		}
	}

	@Test
	public void compilationTest2() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = ::",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			fail("didn't catch singleton '::' error");
		} catch (Exception e) {
		}
	}

	@Test
	public void compilationTest3() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = :: {foo}",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			fail("didn't catch singleton '::' error");
		} catch (Exception e) {
		}
	}

	@Test
	public void compilationTest4() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = :: | 'a' {foo}",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			fail("didn't catch singleton '::' error");
		} catch (Exception e) {
		}
	}

	@Test
	public void compilationTest5() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'a' | :",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			fail("didn't catch initial colon error");
		} catch (Exception e) {
		}
	}

	@Test
	public void compilationTest6() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'a' :::",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			fail("didn't too many colons error");
		} catch (Exception e) {
		}
	}

	@Test
	public void compilationTest7() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'a' ::",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
		} catch (Exception e) {
			fail("didn't compile double colon barrier");
		}
	}

	@Test
	public void compilationTest8() throws GrammarException, IOException {
		String[] rules = {
		//
		"<ROOT> = 'a' :",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
		} catch (Exception e) {
			fail("didn't compile single colon barrier");
		}
	}

	@Test
	public void singleColonTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a>+ : <b>",//
				"<a> = 'a'",//
				"<b> = 'ab'",//
		};
		Grammar g = new Grammar(rules);
		assertNull("couldn't shrink initial match", g.matches("aab").match());
	}

	@Test
	public void doubleColonMatchesTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>",//
				"<a> = :: 'a'",//
				"<b> = 'b'",//
		};
		Grammar g = new Grammar(rules);
		assertNull("couldn't shrink initial match", g.matches("b").match());
	}

	@Test
	public void doubleColonLookingAtTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>",//
				"<a> = :: 'a'",//
				"<b> = 'b'",//
		};
		Grammar g = new Grammar(rules);
		assertNull("couldn't shrink initial match", g.lookingAt("b").match());
	}

	@Test
	public void doubleColonFindTest1() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>",//
				"<a> = :: 'a'",//
				"<b> = 'b'",//
		};
		Grammar g = new Grammar(rules);
		assertNull("couldn't shrink initial match", g.find("b").match());
	}

	@Test
	public void doubleColonFindTest2() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>",//
				"<a> = :: 'a'",//
				"<b> = 'b'",//
		};
		Grammar g = new Grammar(rules);
		assertNotNull("match at new offset", g.find("ba").match());
	}

}
