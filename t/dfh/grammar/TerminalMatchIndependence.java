package dfh.grammar;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * Makes sure a cached terminal match used in two overlapping match results has
 * a correctly assigned parent node in each.
 * <p>
 * 
 * @author David F. Houghton - Mar 27, 2012
 * 
 */
public class TerminalMatchIndependence {

	@Test
	public void test() {
		Grammar g = new Grammar("a = 'a' | <a> 'b'");
		List<Match> matches = new ArrayList<Match>();
		Matcher m = g.lookingAt("ab", new Options().allowOverlap(true)
				.longestMatch(false));
		Match n;
		while ((n = m.match()) != null)
			matches.add(n);
		assertEquals(2, matches.size());
		assertFalse(countDept(matches.get(0).leftLeaf()) == countDept(matches
				.get(1).leftLeaf()));
	}

	private int countDept(Match n) {
		int count = 0;
		while (n.parent() != null) {
			count++;
			n = n.parent();
		}
		return count;
	}

}
