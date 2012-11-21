package dfh.grammar;

import static org.junit.Assert.*;

import org.junit.Test;

public class IterableTest {

	@Test
	public void basicFunctionality() {
		Grammar g = new Grammar("rule = 'a'");
		String s = "aaaaaaaaaa";
		int count = 0;
		for (@SuppressWarnings("unused")
		Match m : g.find(s).all())
			count++;
		assertEquals(s.length(), count);
	}

	@Test
	public void unrepeatable() {
		Grammar g = new Grammar("rule = 'a'");
		String s = "aaaaaaaaaa";
		int count = 0;
		Iterable<Match> i = g.find(s).all();
		try {
			for (int j = 0; j < 2; j++) {
				for (@SuppressWarnings("unused")
				Match m : i)
					count++;
			}
			fail("should have thrown an exception");
		} catch (UnsupportedOperationException e) {
		}
		assertEquals(s.length(), count);
	}

}
