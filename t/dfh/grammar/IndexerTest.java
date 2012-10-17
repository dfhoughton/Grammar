package dfh.grammar;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Makes sure indexers work as intended.
 * <p>
 * 
 * @author David F. Houghton - Oct 17, 2012
 * 
 */
public class IndexerTest {

	@Test
	public void charTest() {
		Grammar g = new Grammar("foo = 'cat' | 'dog'");
		String s = "cat dog";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals(2, count);
		m = g.find(s, new Options().indexer(new CharacterIndexer('c')));
		count = 0;
		while (m.match() != null)
			count++;
		assertEquals(1, count);
	}

	@Test
	public void stringTest() {
		Grammar g = new Grammar("foo = 'cat' | 'dog'");
		String s = "cat dog";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals(2, count);
		m = g.find(s, new Options().indexer(new StringIndexer("cat")));
		count = 0;
		while (m.match() != null)
			count++;
		assertEquals(1, count);
	}

	@Test
	public void patternTest() {
		Grammar g = new Grammar("foo = 'cat' | 'dog'");
		String s = "cat dogcat";
		Matcher m = g.find(s);
		int count = 0;
		while (m.match() != null)
			count++;
		assertEquals(3, count);
		m = g.find(s, new Options().indexer(new PatternIndexer(Pattern
				.compile("\\bcat\\b"))));
		count = 0;
		while (m.match() != null)
			count++;
		assertEquals(1, count);
	}

}
