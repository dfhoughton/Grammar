package dfh.grammar;

import static org.junit.Assert.*;

import java.util.regex.Pattern;

import org.junit.Test;

public class DescriptionTest {

	@Test
	public void basic() {
		String[] rules = {
		//
		"ROOT = 'foo'" };
		Grammar g = new Grammar(rules);
		assertTrue(
				"basic description",
				Pattern.compile("\\s*+ROOT = \"foo\"\\s*+")
						.matcher(g.describe()).matches());
	}

	@Test
	public void tagAll() {
		String[] rules = {
		//
		"ROOT = [{bar} 'foo']" };
		Grammar g = new Grammar(rules);
		assertTrue(
				"basic description",
				Pattern.compile(
						"\\s*+ROOT = \\[\\{bar\\}\\s*+\"foo\"\\s*+\\]\\s*+")
						.matcher(g.describe()).matches());
	}
}
