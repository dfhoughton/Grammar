package dfh.grammar;

import java.util.regex.Pattern;

import org.junit.Test;

/**
 * These tests make sure the error catching mechanisms in {@link Compiler} are
 * doing what they should.
 * <p>
 * <b>Creation date:</b> Mar 23, 2011
 * 
 * @author David Houghton
 * 
 */
public class MisCompilationTest {
	@Test
	public void redefinitionError() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"<b> = (quux) (s) (baz)",//
				"(s) =\\s++",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover redefinition");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("found redefinition", e.getMessage()
					.indexOf("rule <b> redefined") > -1);
		}
	}

	@Test
	public void incompleteError() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"(s) =\\s++",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover missing rule");
		} catch (Exception e) {
			org.junit.Assert
					.assertTrue("found missing definition", e.getMessage()
							.indexOf("could not satisfy all dependencies") > -1);
		}
	}

	@Test
	public void noRootError() {
		String[] rules = {
				//
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(s) =\\s++",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover the lack of a root rule");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("no root rule found", e.getMessage()
					.indexOf("no root rule found") > -1);
		}
	}

	@Test
	public void doubleAssignmentError() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.defineTerminal("s", Pattern.compile("\\s++"));
			g.defineTerminal("s", Pattern.compile("\\s++"));
			org.junit.Assert.fail("did not discover missing rule");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("found redefinition", e.getMessage()
					.indexOf("already defined") > -1);
		}
	}

	@Test
	public void falseAssignmentError() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.defineTerminal("x", Pattern.compile("\\s++"));
			org.junit.Assert.fail("did not discover missing rule");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("unknown terminal rule", e.getMessage()
					.indexOf("unknown terminal rule") > -1);
		}
	}

	@Test
	public void incompleteError2() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.find("foo bar");
			g.defineTerminal("x", Pattern.compile("\\s++"));
			org.junit.Assert.fail("did not discover missing rule");
		} catch (Exception e) {
			org.junit.Assert
					.assertTrue(
							"terminal rules remaining undefined",
							e.getMessage().indexOf(
									"terminal rules remaining undefined") > -1);
		}
	}

	@Test
	public void cycleTest() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = <b>",//
				"<b> = <a>",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.find("foo bar");
			g.defineTerminal("x", Pattern.compile("\\s++"));
			org.junit.Assert.fail("did not discover missing rule");
		} catch (Exception e) {
			org.junit.Assert
					.assertTrue(
							"could not satisfy all dependencies",
							e.getMessage().indexOf(
									"could not satisfy all dependencies") > -1);
		}
	}

	@Test
	public void mismatchedBracketTest1() {
		String[] rules = {
				//
				"<ROOT> = <a) | <b>", //
				"<a> = <b>",//
				"<b> = <a>",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover bad bracket");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("ill-formed rule identifier", e
					.getMessage().indexOf("ill-formed rule identifier") > -1);
		}
	}

	@Test
	public void mismatchedBracketTest2() {
		String[] rules = {
				//
				"<ROOT> = (a> | <b>", //
				"<a> = <b>",//
				"<b> = <a>",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover bad bracket");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("ill-formed rule identifier", e
					.getMessage().indexOf("ill-formed rule identifier") > -1);
		}
	}

	@Test
	public void mismatchedBracketTest3() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"(a> = 'b'",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover bad bracket");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("mismatched brackets in label", e
					.getMessage().indexOf("mismatched brackets in label") > -1);
		}
	}

	@Test
	public void mismatchedBracketTest4() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a) = 'b'",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover bad bracket");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("mismatched brackets in label", e
					.getMessage().indexOf("mismatched brackets in label") > -1);
		}
	}

	@Test
	public void illFormedRule() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = asdfasdf",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not ill-formed rule");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("ill-formed rule", e.getMessage()
					.indexOf("ill-formed rule") > -1);
		}
	}

	@Test
	public void emptyGroup() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = 'b' []",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover empty group");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("empty rule body", e.getMessage()
					.indexOf("empty rule body") > -1);
		}
	}

	@Test
	public void emptyRuleBody() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = ",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover empty group");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("empty rule body", e.getMessage()
					.indexOf("empty rule body") > -1);
		}
	}

	@Test
	public void backReferenceRepetition() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = <b> 1++",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover repeated back reference");
		} catch (Exception e) {
			org.junit.Assert
					.assertTrue(
							"back reference cannot be modified with repetition suffixes",
							e.getMessage()
									.indexOf(
											"back reference cannot be modified with repetition suffixes") > -1);
		}
	}

	@Test
	public void backReferenceRepetitionMiscount1() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = <b> 0",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover back reference miscount");
		} catch (Exception e) {
			org.junit.Assert.assertTrue(
					"back references must be greater than 0",
					e.getMessage().indexOf(
							"back references must be greater than 0") > -1);
		}
	}

	@Test
	public void backReferenceRepetitionMiscount2() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = <b> 2",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover back reference miscount");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("back reference too big", e
					.getMessage().indexOf("too big") > -1);
		}
	}

	@Test
	public void backReferenceRepetitionMiscount3() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = [ <b> 0 ]++",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover back reference miscount");
		} catch (Exception e) {
			org.junit.Assert.assertTrue(
					"back references must be greater than 0",
					e.getMessage().indexOf(
							"back references must be greater than 0") > -1);
		}
	}

	@Test
	public void backReferenceRepetitionMiscount4() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = [ <b> 2 ]++",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover back reference miscount");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("back reference too big", e
					.getMessage().indexOf("too big") > -1);
		}
	}

	@Test
	public void closingBracket() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = [ <b> 'q'",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover missing bracket");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("could not find closing", e
					.getMessage().indexOf("could not find closing") > -1);
		}
	}

	@Test
	public void closingQuote1() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = 'b",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover missing quote");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("could not find closing quote", e
					.getMessage().indexOf("could not find closing") > -1);
		}
	}

	@Test
	public void badCurlies1() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = 'b'{}",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover bad curly repetition");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("bad repetition modifier", e
					.getMessage().indexOf("bad repetition modifier") > -1);
		}
	}

	@Test
	public void badCurlies2() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = 'b'{,}",//
				"<b> = 'a'",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			org.junit.Assert.fail("did not discover bad curly repetition");
		} catch (Exception e) {
			org.junit.Assert.assertTrue("bad repetition modifier", e
					.getMessage().indexOf("bad repetition modifier") > -1);
		}
	}

}
