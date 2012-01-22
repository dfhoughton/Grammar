package dfh.grammar;

import static org.junit.Assert.*;

import org.junit.Test;

public class ExplicitRuleTest {

	@Test
	public void deferred() {
		Grammar g = new Grammar("ROOT = <a> | <b>");
		g.defineRule("a", "a");
		g.defineRule("b", "b");
		Match m = g.matches("a").match();
		assertNotNull(m);
		m = m.first("a");
		assertNotNull(m.first(Match.NAMED));
	}

	@Test
	public void cyclic() {
		Grammar g = new Grammar(new String[] {//
				"ROOT = <a>",//
						"a = 'b' <a> | 'a'",//
				});
		Match m = g.matches("ba").match();
		assertNotNull(m);
		assertTrue(m.get(Match.ANONYMOUS).size() == 3);
	}

}
