package dfh.grammar;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
				"<a> = <foo> <s> <bar>",//
				"<b> = <quux> <s> <baz>",//
				"<b> = <quux> <s> <baz>",//
				"<s> = /\\s++/",//
				"<foo> = /foo/",//
				"<bar> = /bar/",//
				"<quux> = /quux/",//
				"<baz> = /baz/",//
		};
		try {
			@SuppressWarnings("unused")
			Grammar g = new Grammar(rules);
			fail("did not discover redefinition");
		} catch (Exception e) {
			assertTrue("found redefinition",
					e.getMessage().indexOf("rule <b> redefined") > -1);
		}
	}

	@Test
	public void doubleAssignmentError() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = <foo> <s> <bar>",//
				"<b> = <quux> <s> <baz>",//
				"<foo> = /foo/",//
				"<bar> = /bar/",//
				"<quux> = /quux/",//
				"<baz> = /baz/",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.defineRule("s", Pattern.compile("\\s++"));
			g.defineRule("s", Pattern.compile("\\s++"));
			fail("did not discover missing rule");
		} catch (Exception e) {
			assertTrue("found redefinition",
					e.getMessage().indexOf("already defined") > -1);
		}
	}

	@Test
	public void falseAssignmentError() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = <foo> <s> <bar>",//
				"<b> = <quux> <s> <baz>",//
				"<foo> = /foo/",//
				"<bar> = /bar/",//
				"<quux> = /quux/",//
				"<baz> = /baz/",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.defineRule("x", Pattern.compile("\\s++"));
			fail("did not discover missing rule");
		} catch (Exception e) {
			assertTrue("unknown terminal rule",
					e.getMessage().indexOf("unknown terminal rule") > -1);
		}
	}

	@Test
	public void incompleteError2() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = <foo> <s> <bar>",//
				"<b> = <quux> <s> <baz>",//
				"<foo> = /foo/",//
				"<bar> = /bar/",//
				"<quux> = /quux/",//
				"<baz> = /baz/",//
		};
		try {
			Grammar g = new Grammar(rules);
			g.find("foo bar");
			g.defineRule("x", Pattern.compile("\\s++"));
			fail("did not discover missing rule");
		} catch (Exception e) {
			assertTrue("terminal rules remaining undefined", e.getMessage()
					.indexOf("terminal rules remaining undefined") > -1);
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
			g.defineRule("x", Pattern.compile("\\s++"));
			fail("did not discover missing rule");
		} catch (Exception e) {
			assertTrue("cycle found in rules",
					e.getMessage().indexOf("cycle found in rules") > -1);
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
			fail("did not ill-formed rule");
		} catch (Exception e) {
			assertTrue("ill-formed rule",
					e.getCause().getMessage().indexOf("ill-formed rule") > -1);
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
			fail("did not discover empty group");
		} catch (Exception e) {
			assertTrue("empty rule body",
					e.getCause().getMessage().indexOf("empty rule body") > -1);
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
			fail("did not discover empty group");
		} catch (Exception e) {
			assertTrue("empty rule body",
					e.getMessage().indexOf("no rule body provided in") > -1);
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
			fail("did not discover back reference miscount");
		} catch (Exception e) {
			assertTrue(
					"back references must be greater than 0",
					e.getCause().getMessage()
							.indexOf("back references must be greater than 0") > -1);
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
			fail("did not discover back reference miscount");
		} catch (Exception e) {
			assertTrue("back reference too big", e.getCause().getMessage()
					.indexOf("too big") > -1);
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
			fail("did not discover back reference miscount");
		} catch (Exception e) {
			assertTrue(
					"back references must be greater than 0",
					e.getCause().getMessage()
							.indexOf("back references must be greater than 0") > -1);
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
			fail("did not discover back reference miscount");
		} catch (Exception e) {
			assertTrue("back reference too big", e.getCause().getMessage()
					.indexOf("too big") > -1);
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
			fail("did not discover missing bracket");
		} catch (Exception e) {
			assertTrue("could not find closing", e.getCause().getMessage()
					.indexOf("could not find closing") > -1);
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
			fail("did not discover missing quote");
		} catch (Exception e) {
			assertTrue("could not find closing quote", e.getCause()
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
			fail("did not discover bad curly repetition");
		} catch (Exception e) {
			assertTrue("bad repetition modifier", e.getCause().getMessage()
					.indexOf("bad repetition modifier") > -1);
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
			fail("did not discover bad curly repetition");
		} catch (Exception e) {
			assertTrue("bad repetition modifier", e.getCause().getMessage()
					.indexOf("bad repetition modifier") > -1);
		}
	}

}
