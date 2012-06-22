package dfh.grammar;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.junit.Test;

public class SubrulesTest {

	@Test
	public void test1() {
		String[] rules = { "ROOT = /[ab]/ 'foo' 1^{2}" };
		Grammar g = new Grammar(rules);
		Set<Rule> set = new HashSet<Rule>();
		g.root.subRules(set, new HashSet<Rule>(), false);
		for (Iterator<Rule> i = set.iterator(); i.hasNext();) {
			Rule r = i.next();
			if (r.generation == -1)
				i.remove();
		}
		assertEquals(1, set.size());
	}

	@Test
	public void test2() {
		String[] rules = {
				//
				"<ROOT> = <joe>",//
				"<joe> = /\\bjoe\\b/",//
		};
		Grammar g = new Grammar(rules);
		Set<Rule> set = new HashSet<Rule>();
		g.root.subRules(set, new HashSet<Rule>(), false);
		for (Iterator<Rule> i = set.iterator(); i.hasNext();) {
			Rule r = i.next();
			if (r.generation == -1)
				i.remove();
		}
		assertEquals(1, set.size());
	}

	@Test
	public void test3() {
		String[] rules = {
				//
				"<ROOT> = 'foo' ~<b>",//
				"<b> = 'bar' ~<a>",//
				"<a> = /(?<!\\d)\\d++(?!\\d)/r",//
		};
		Grammar g = new Grammar(rules);
		Set<Rule> set = new HashSet<Rule>();
		g.root.subRules(set, new HashSet<Rule>(), false);
		for (Iterator<Rule> i = set.iterator(); i.hasNext();) {
			Rule r = i.next();
			if (r.generation == -1)
				i.remove();
		}
		assertEquals(3, set.size());
	}

	@Test
	public void test4() {
		String[] rules = {
				//
				"<ROOT> = <q> <text> 1",//
				"<q> = /[\"']/",//
				"<text> = /\\w++/",//
		};
		Grammar g = new Grammar(rules);
		Set<Rule> set = new HashSet<Rule>();
		g.root.subRules(set, new HashSet<Rule>(), false);
		for (Iterator<Rule> i = set.iterator(); i.hasNext();) {
			Rule r = i.next();
			if (r.generation == -1)
				i.remove();
		}
		assertEquals(3, set.size());
	}

	@Test
	public void test5() {
		String[] rules = {
		//
		"<ROOT> = /[ab]/ 'foo' 1^{2,}",//
		};
		Grammar g = new Grammar(rules);
		Set<Rule> set = new HashSet<Rule>();
		g.root.subRules(set, new HashSet<Rule>(), false);
		for (Iterator<Rule> i = set.iterator(); i.hasNext();) {
			Rule r = i.next();
			if (r.generation == -1)
				i.remove();
		}
		assertEquals(1, set.size());
	}

	@Test
	public void test6() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>  # first comment", //
				"",// blank line
				"<a> = <foo> <s> <bar>",//
				"  # comment line",//
				"<b> = <quux> <s> <baz> # another comment",//
				"<s> = /\\s++/",//
				"<foo> = /foo/",//
				"<bar> = /bar/",//
				"<quux> = /quux/",//
				"<baz> = /baz/",//
		};
		Grammar g = new Grammar(rules);
		Set<Rule> set = new HashSet<Rule>();
		g.root.subRules(set, new HashSet<Rule>(), false);
		for (Iterator<Rule> i = set.iterator(); i.hasNext();) {
			Rule r = i.next();
			if (r.generation == -1)
				i.remove();
		}
		assertEquals(8, set.size());
	}

	@Test
	public void test7() {
		String[] rules = {
				//
				"<ROOT> = <NP>",//
				"<NP> = [<DP> <s>]? <N>",//
				"<N> = [<AP> <s>]? 'n'",//
				"<AP> = <AP>? <A>",//
				"<A> = ['adv' <s>]* 'a'",//
				"<DP> = <POS> | 'the'",//
				"<POS> = <NP> \"'s\"",//
				"<s> = ' '++",//
		};
		Grammar g = new Grammar(rules);
		Set<Rule> set = new HashSet<Rule>();
		g.root.subRules(set, new HashSet<Rule>(), false);
		for (Iterator<Rule> i = set.iterator(); i.hasNext();) {
			Rule r = i.next();
			if (r.generation == -1)
				i.remove();
		}
		assertEquals(8, set.size());
	}

}
