package dfh.grammar;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.junit.Test;

public class Regression {

	@Test
	public void optionReuse() {
		String[] rules = { "ROOT = /\\w++/" };
		Grammar g = null;
		try {
			g = new Grammar(rules);
		} catch (Exception e) {
			fail("threw exception");
		}
		Options opt = new Options().keepRightmost(true);
		g.matches("foo", opt);
		g.matches("alphabet", opt);
	}

	@Test
	public void finalNonmatch() {
		String[] rules = {
				//
				"ROOT = <word> /\\s*+/",//
				"word = /\\w++/",//
		};
		try {
			Grammar g = new Grammar(rules);
			Matcher m = g.find("foo", new Options());
			assertNotNull(m.match());
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}

	@Test
	public void missingFirstOffset() {
		String[] rules = {
				//
				"ROOT = /[*_]/+ <word> 1 /(?![\\p{L}\\d])/",// works if you use
															// native assertion
				"word = /\\w++/",//
		};
		try {
			Grammar g = new Grammar(rules);
			Matcher m = g.find("*terrible*", new Options().allowOverlap(true));
			assertNotNull(m.match());
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}

	@Test
	public void labelsAndDeferredRuleDefinition() {
		try {
			String[] rules = { "ROOT = <foo> | 'quux'" };
			Grammar g = new Grammar(rules);
			g.defineRule("foo", "bar");
			Match m = g.matches("bar").match();
			m.first("foo");
		} catch (Exception e) {
			fail("threw exception: " + e);
		}
	}

	@Test
	public void backwardAssertionWordBreak() {
		Grammar g = new Grammar("ROOT = ~- /\\b/r 'a'");
		assertNotNull("found initial word break in backwards assertion", g
				.matches("a").match());
	}

	@Test
	public void quantifiedRegex() {
		Grammar g = new Grammar("rule = /a/? '1'");
		assertNotNull(g.matches("1").match());
	}

	@Test
	public void infiniteQuantification() {
		Grammar g = new Grammar("rule = [ 'a'* | 'b'* ]+");
		try {
			g.matches("c").match();
		} catch (GrammarException e) {
			assertTrue(e.getMessage().startsWith("non-advancing repetition"));
		}
	}

	@Test
	public void asteriskQTest() throws GrammarException, IOException {
		final boolean[] success = { false };
		Runnable r = new Runnable() {

			@Override
			public void run() {
				String[] rules = {
						//
						"<ROOT> = <a>*? (2)",//
						"<a> = 'a'",//
				};
				Grammar g = new Grammar(rules);
				g.defineCondition("2", new Condition() {
					@Override
					public boolean passes(Match m, Matcher n, CharSequence s) {
						return m.end() - m.start() == 2;
					}
				});
				String s = "aaaaaa a";
				Options opt = new Options().study(false);
				opt.longestMatch(false);
				Matcher m = g.find(s, opt);
				while (m.match() != null)
					;
				synchronized (success) {
					success[0] = true;
				}
			}
		};
		final Thread t = new Thread(r);
		t.start();
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				System.out.println("ping");
				if (t.isAlive()) {
					t.interrupt();
				}
			}
		}, 2000);
		synchronized (success) {
			assertTrue(success[0]);
		}
	}
}
