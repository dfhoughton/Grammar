package dfh.grammar;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

public class NoTerminalTest {

	@Test
	public void basicTest() throws GrammarException, IOException {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>",//
				"<b> = <a>",//
				"<a> = <b>",//
		};
		try {
			new Grammar(rules);
			fail("error should be thrown when compiling a grammar with no possible terminal rules");
		} catch (GrammarException e) {
			System.err.println(e.getMessage());
		}
	}
}
