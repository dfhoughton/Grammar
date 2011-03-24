import java.io.IOException;
import java.util.regex.Pattern;

import dfh.grammar.Grammar;
import dfh.grammar.GrammarException;

public class Benchmarks {

	private static final int DEFAULT_ITERATIONS = 50000;
	private static final int DEFAULT_WARMUP = 20000;
	private static int iterations;
	private static int warmup;

	/**
	 * @param args
	 * @throws IOException
	 * @throws GrammarException
	 */
	public static void main(String[] args) throws GrammarException, IOException {
		iterations = DEFAULT_ITERATIONS;
		warmup = DEFAULT_WARMUP;
		if (args.length > 0) {
			iterations = Integer.parseInt(args[0]);
			if (args.length > 1)
				warmup = Integer.parseInt(args[1]);
		}
		System.out.println(warmup + " JIT warmup iterations");
		System.out.println(iterations + " test iterations\n");
		test1();
		test2();
		test3();
		test4();
		test5();
	}

	private static void test1() throws IOException {
		String[] rules = {
		//
		"<ROOT> = 'a' | 'b'",//
		};
		Pattern p = Pattern.compile("[ab]");
		String s = "qewrqewrqewraqwreqewr";
		Grammar g = new Grammar(rules);
		iterate(p, g, s);
	}

	private static void iterate(Pattern p, Grammar g, String s) {
		System.out.println("pattern: " + p);
		for (int i = 0; i < warmup; i++) {
			p.matcher(s).find();
		}
		long start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			p.matcher(s).find();
		}
		long end = System.currentTimeMillis();
		System.out.println(end - start + " milliseconds\n");
		System.out.println(g.describe().replaceAll("\\s++$", ""));
		for (int i = 0; i < warmup; i++) {
			g.find(s).match();
		}
		start = System.currentTimeMillis();
		for (int i = 0; i < iterations; i++) {
			g.find(s).match();
		}
		end = System.currentTimeMillis();
		System.out.println(end - start + " milliseconds\n");
	}

	private static void test2() throws IOException {
		String[] rules = {
				//
				"<ROOT> = <a> | <b>", //
				"<a> = (foo) (s) (bar)",//
				"<b> = (quux) (s) (baz)",//
				"(s) =\\s++",//
				"(foo) =foo",//
				"(bar) =bar",//
				"(quux) =quux",//
				"(baz) =baz",//
		};
		Pattern p = Pattern.compile("foo\\s++bar|quux\\s++baz");
		String s = "foo bar";
		Grammar g = new Grammar(rules);
		iterate(p, g, s);
	}

	private static void test3() throws IOException {
		String[] rules = {
				//
				"<ROOT> = [ <a> | <b> ]{2} <b>",//
				"<a> = (a){,2}",//
				"<b> = (a) (b)",//
				"(a) =a",//
				"(b) =b",//
		};
		Pattern p = Pattern.compile("(?:a{0,2}|a{0,2}b){2}b");
		String s = "aabb";
		Grammar g = new Grammar(rules);
		iterate(p, g, s);
	}

	private static void test4() throws IOException {
		String[] rules = {
				//
				"<ROOT> = (a) | (b)",//
				"(a) =a",//
				"(b) =b",//
		};
		Pattern p = Pattern.compile("[ab]");
		String s = "qewrqewrqewraqwreqewr";
		Grammar g = new Grammar(rules);
		iterate(p, g, s);
	}

	private static void test5() throws IOException {
		String[] rules = {
				//
				"<ROOT> = [ <a> | <b> ]{2} <b>",//
				"<a> = (a){,2}",//
				"<b> = (a) (b)",//
				"(a) = 'a'",//
				"(b) = 'b'",//
		};
		Pattern p = Pattern.compile("(?:a{0,2}|a{0,2}b){2}b");
		String s = "aabb";
		Grammar g = new Grammar(rules);
		iterate(p, g, s);
	}

}
