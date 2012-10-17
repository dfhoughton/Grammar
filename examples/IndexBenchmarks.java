import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import dfh.cli.Cli;
import dfh.cli.rules.Range;
import dfh.grammar.CharacterIndexer;
import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;
import dfh.grammar.Indexer;
import dfh.grammar.Matcher;
import dfh.grammar.Options;
import dfh.grammar.PatternIndexer;
import dfh.grammar.StringIndexer;

public class IndexBenchmarks {

	private static int warmup;
	private static Integer trials;
	private static Integer group;
	private static double trim;

	/**
	 * <pre>
	 * {@code
	 * USAGE: EXECUTABLE [options]
	 * 
	 *   A basic set of benchmark tests comparing grammars using various parameters to 
	 *   equivalent regular expressions.
	 * 
	 *     --group -g   <val>  number or iterations to time together to overcome the
	 *                         limitations of millisecond time granularity; default: 
	 *                         2000
	 *     --trials -t  <val>  number of groups to time; default: 50
	 *     --trim       <val>  fraction of time-sorted trials to discard from the high
	 *                         and low ends of the sort to eliminate outliers; default:
	 *                         0.1
	 *     --warmup -w  <val>  number to iterations to warm up the JIT compiler;
	 *                         default: 50000
	 * 
	 *     --help -? -h        print usage information
	 * }
	 * </pre>
	 * 
	 * @param args
	 * @throws IOException
	 * @throws GrammarException
	 */
	public static void main(String[] args) throws GrammarException, IOException {
		Object[][][] spec = {
				{ {
						Cli.Opt.USAGE,
						"A basic set of benchmark tests comparing grammars using various parameters to equivalent regular expressions." } },//
				{ { Cli.Opt.ARGS } },//
				{
						{ "group", 'g', Integer.class, 2000 },
						{ "number or iterations to time together to overcome the limitations of millisecond time granularity" },
						{ Range.positive() } },//
				{ { "trials", 't', Integer.class, 50 },
						{ "number of groups to time" }, { Range.positive() } },//
				{
						{ "trim", Double.class, .1D },
						{ "fraction of time-sorted trials to discard from the high and low ends of the sort to eliminate outliers" },
						{ Range.lowIncl(0, .25) } },//
				{ { "warmup", 'w', Integer.class, 50000 },
						{ "number to iterations to warm up the JIT compiler" },
						{ Range.nonNegative() } },//
		};
		Cli cli = new Cli(spec);
		cli.parse(args);
		System.out.println(cli.dump());
		trials = cli.integer("trials");
		warmup = cli.integer("warmup");
		group = cli.integer("group");
		trim = cli.dbl("trim");
		test1();
		test2();
		test3();
		test4();
		longStringTest();
	}

	private static void test1() throws IOException {
		String[] rules = {
		//
		"<ROOT> = 'a' | 'b'",//
		};
		CharacterIndexer p = new CharacterIndexer('a');
		StringIndexer q = new StringIndexer("a");
		PatternIndexer r = new PatternIndexer(Pattern.compile("a"));
		String s = "qewrqewrqewraqwreqewr";
		Grammar g = new Grammar(rules);
		iterate(p, q, r, g, s, false);
	}

	private static void test2() {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = <foo> <s> <bar>",//
				"<b> = <quux> <s> <baz>",//
				"<s> = /\\s++/",//
				"<foo> = /foo/",//
				"<bar> = /bar/",//
				"<quux> = /quux/",//
				"<baz> = /baz/",//
		};
		String s = "foo bar";
		Grammar g = new Grammar(rules);
		iterate(null, null, new PatternIndexer(Pattern.compile("foo|quux")), g,
				s, false);
	}

	private static void test3() {
		String[] rules = {
				//
				"<ROOT> = [ <c> | <d> ]{2} <d>",//
				"<c> = <a>{,2}",//
				"<d> = <a> <b>",//
				"<a> = /a/",//
				"<b> = /b/",//
		};
		CharacterIndexer p = new CharacterIndexer('a');
		StringIndexer q = new StringIndexer("a");
		PatternIndexer r = new PatternIndexer(Pattern.compile("a"));
		String s = "aabb";
		Grammar g = new Grammar(rules);
		iterate(p, q, r, g, s, false);
	}

	private static void longStringTest() throws IOException {
		String[] rules = {
		//
		"<ROOT> = 'cat' | 'dog' | 'monkey'",//
		};
		Pattern p = Pattern.compile("cat|dog|monkey");
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < 1000; i++) {
			b.append("__________");
			switch (i % 3) {
			case 0:
				b.append("cat");
				break;
			case 1:
				b.append("dog");
				break;
			case 2:
				b.append("monkey");
			}
		}
		String s = b.toString();
		Grammar g = new Grammar(rules);
		iterate(null, null, new PatternIndexer(p), g, s, false);
	}

	private static void iterate(CharacterIndexer p, StringIndexer q,
			PatternIndexer r, Grammar g, String s, boolean allMatches) {
		Options opt = new Options();
		opt.fatMemory(true);
		opt.longestMatch(false);
		System.out.println("=============\n");
		System.out.println("string: "
				+ (s.length() > 60 ? s.substring(0, 60) + "... (length "
						+ s.length() + ")" : s));
		System.out.println();
		System.out.println(g.describe());
		iterate(g, s, allMatches, opt, p);
		iterate(g, s, allMatches, opt, q);
		iterate(g, s, allMatches, opt, r);
	}

	private static void iterate(Grammar g, String s, boolean allMatches,
			Options opt, Indexer i) {
		if (i == null)
			return;
		System.out.println();
		System.out.println("indexer: " + i);
		System.out.println();
		List<Long> times = new ArrayList<Long>();
		System.out.println("with studying");
		timeGrammar(g, s, allMatches, times, opt, i);
		System.out.println("without studying");
		timeGrammar(g, s, allMatches, times, opt, i);
		System.out.println("with studying using LTM");
		opt.study(true);
		opt.longestMatch(true);
		timeGrammar(g, s, allMatches, times, opt, i);
	}

	private static void timeGrammar(Grammar g, String s, boolean allMatches,
			List<Long> times, Options opt, Indexer p) {
		Options indexed = new Options(opt);
		indexed.indexer(p);
		timeGrammar(g, s, allMatches, times, opt);
		System.out.println("with index");
		timeGrammar(g, s, allMatches, times, indexed);
		System.out.println();
	}

	private static void timeGrammar(Grammar g, String s, boolean allMatches,
			List<Long> times, Options opt) {
		for (int i = 0; i < warmup; i++) {
			if (allMatches) {
				Matcher m = g.find(s, opt);
				while (m.match() != null)
					;
			} else {
				g.find(s, opt).match();
			}
		}
		for (int i = 0, lim = trials; i < lim; i++) {
			long t = System.currentTimeMillis();
			for (int j = 0; j < group; j++) {
				if (allMatches) {
					Matcher m = g.find(s, opt);
					while (m.match() != null)
						;
				} else {
					g.find(s, opt).match();
				}
			}
			times.add(System.currentTimeMillis() - t);
		}
		trimmedMean(times);
	}

	private static void trimmedMean(List<Long> times) {
		Collections.sort(times);
		int start = (int) Math.round(times.size() * trim);
		int end = times.size() - start;
		List<Long> sublist = times.subList(start, end);
		double avg = 0;
		for (long l : sublist)
			avg += l;
		avg /= sublist.size() * 2000;
		System.out.printf("%.5f milliseconds per sequence%n", avg);
	}

	private static void test4() {
		String[] rules = {
				//
				"<ROOT> = <c> | <d>",//
				"<c> = /a/",//
				"<d> = /b/",//
		};
		Pattern p = Pattern.compile("[ab]");
		String s = "qewrqewrqewraqwreqewr";
		Grammar g = new Grammar(rules);
		iterate(null, null, new PatternIndexer(p), g, s, false);
	}
}
