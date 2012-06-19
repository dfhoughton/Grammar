package dfh.grammar;

import org.junit.Test;

/**
 * Holds (some of the) tests for {@link Options}
 * <p>
 * 
 * @author David F. Houghton - May 16, 2012
 */
public class OptionsTest {

	@Test
	public void reuseTest1() {
		Grammar g = new Grammar("rule = 'bar'");
		Options opt = new Options();
		g.find("a bar", opt).match();
		g.find("another bar", opt).match();
	}

	@Test
	public void reuseTest2() {
		Grammar g = new Grammar("rule = 'bar'");
		Options opt = new Options();
		g.find("a microscopic bar", opt).match();
		g.find("a big bar", opt).match();
	}
}
