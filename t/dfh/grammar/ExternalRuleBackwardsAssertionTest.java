package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import dfh.grammar.Label.Type;

/**
 * Makes sure ad hoc rules can be reversible by including them as a pre-compiled
 * list.
 * <p>
 * <b>Creation date:</b> Oct 16, 2011
 * 
 * @author David Houghton
 * 
 */
public class ExternalRuleBackwardsAssertionTest {
	@SuppressWarnings("serial")
	@Reversible
	private static class TestRule extends LiteralRule {

		public TestRule(Label label, String literal) {
			super(label, literal);
		}

	}

	@Test
	public void compilationTest() {
		Map<String, Rule> map = new HashMap<String, Rule>(1);
		map.put("r", new TestRule(new Label(Type.literal, "foo"), "foo"));
		String[] rules = {
		//
		"ROOT = ~- <r> 'bar'",//
		};
		try {
			new Grammar(rules, map);
		} catch (Exception e) {
			fail("could not compile grammar with pre-compiled rule");
		}
	}

	@Test
	public void simpleTest() {
		Map<String, Rule> map = new HashMap<String, Rule>(1);
		map.put("r", new TestRule(new Label(Type.literal, "foo"), "foo"));
		String[] rules = {
		//
		"ROOT = ~- <r> 'bar'",//
		};
		try {
			Grammar g = new Grammar(rules, map);
			Match m = g.find("foobar").match();
			assertNotNull("positive backwards assertion works", m);
		} catch (Exception e) {
			fail("could not compile grammar with pre-compiled rule");
		}
	}

	@Test
	public void negativeTest() {
		Map<String, Rule> map = new HashMap<String, Rule>(1);
		map.put("r", new TestRule(new Label(Type.literal, "foo"), "foo"));
		String[] rules = {
		//
		"ROOT = !- <r> 'bar'",//
		};
		try {
			Grammar g = new Grammar(rules, map);
			Match m = g.find("bar").match();
			assertNotNull("negative backwards assertion works", m);
		} catch (Exception e) {
			fail("could not compile grammar with pre-compiled rule");
		}
	}

	@Test
	public void variableWidthTest1() {
		Map<String, Rule> map = new HashMap<String, Rule>(1);
		map.put("r", new TestRule(new Label(Type.literal, "foo"), "foo"));
		String[] rules = {
		//
		"ROOT = ~- <r>{2,} 'bar'",//
		};
		try {
			Grammar g = new Grammar(rules, map);
			Match m = g.find("foofoofoobar").match();
			assertNotNull("positive backwards variable-width assertion works",
					m);
		} catch (Exception e) {
			fail("could not compile grammar with pre-compiled rule");
		}
	}

	@Test
	public void variableWidthTest2() {
		Map<String, Rule> map = new HashMap<String, Rule>(1);
		map.put("r", new TestRule(new Label(Type.literal, "foo"), "foo"));
		String[] rules = {
		//
		"ROOT = ~- <r>{4,} 'bar'",//
		};
		try {
			Grammar g = new Grammar(rules, map);
			Match m = g.find("foofoofoobar").match();
			assertNull("positive backwards variable-width assertion works", m);
		} catch (Exception e) {
			fail("could not compile grammar with pre-compiled rule");
		}
	}

	@Test
	public void variableWidthTest3() {
		Map<String, Rule> map = new HashMap<String, Rule>(1);
		map.put("r", new TestRule(new Label(Type.literal, "foo"), "foo"));
		String[] rules = {
		//
		"ROOT = !- <r>{4,} 'bar'",//
		};
		try {
			Grammar g = new Grammar(rules, map);
			Match m = g.find("foofoofoobar").match();
			assertNotNull("negative backwards variable-width assertion works",
					m);
		} catch (Exception e) {
			fail("could not compile grammar with pre-compiled rule");
		}
	}
}
