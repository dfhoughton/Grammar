package dfh.grammar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Base class that parses strings according to a given set of rules.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class Grammar implements Serializable {
	/**
	 * Special debugging API.
	 * <p>
	 * <b>Creation date:</b> Mar 21, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	private abstract class GrammarMatcher extends Matcher {
		private final boolean noOverlap;

		protected GrammarMatcher(CharSequence s, final int offset,
				final boolean noOverlap) {
			super(s, offset, null, null);
			this.noOverlap = noOverlap;
		}

		protected abstract String name();

		@Override
		public String identify() {
			StringBuilder b = new StringBuilder();
			b.append(name());
			if (offset > 0)
				b.append(offset);
			if (noOverlap) {
				b.append("no overlap");
			}
			b.append(')');
			return b.toString();
		}
	}

	private static class BReader implements LineReader {
		private final BufferedReader reader;
		int lineNumber = 0;

		BReader(BufferedReader reader) {
			this.reader = reader;
		}

		@Override
		public String readLine() throws IOException {
			lineNumber++;
			return reader.readLine();
		}

		@Override
		public int lineNumber() {
			return lineNumber;
		}
	}

	private static class AReader implements LineReader {
		private final String[] lines;
		int index = 0;

		AReader(String[] lines) {
			this.lines = lines;
		}

		@Override
		public String readLine() throws IOException {
			if (index == lines.length)
				return null;
			return lines[index++];
		}

		@Override
		public int lineNumber() {
			return index;
		}
	}

	private static final long serialVersionUID = 1L;
	private final Label root;
	private final Map<Label, Rule> rules;
	private final Map<String, Label> terminalLabelMap;
	/**
	 * Keeps track of terminals not defined in initial rule set.
	 */
	private final HashSet<Label> undefinedTerminals;
	transient PrintStream trace;
	/**
	 * For creating a stack trace when debugging.
	 */
	transient LinkedList<Match> stack;

	public Grammar(String[] lines) throws GrammarException, IOException {
		this(new AReader(lines));
	}

	public Grammar(File f) throws GrammarException, FileNotFoundException,
			IOException {
		this(new FileReader(f));
	}

	public Grammar(InputStream is) throws GrammarException, IOException {
		this(new InputStreamReader(is));
	}

	public Grammar(Reader r) throws GrammarException, IOException {
		this(new BufferedReader(r));
	}

	public Grammar(BufferedReader r) throws GrammarException, IOException {
		this(new BReader(r));
	}

	/**
	 * @param in
	 * @throws GrammarException
	 * @throws IOException
	 */
	public Grammar(LineReader reader) throws GrammarException, IOException {
		Compiler c = new Compiler(reader);
		root = c.root();
		rules = c.rules();
		for (Rule r : rules.values())
			r.g = this;
		terminalLabelMap = c.terminalLabelMap();
		undefinedTerminals = c.undefinedTerminals();
	}

	/**
	 * For defining a terminal rule left undefined by the initial rule set.
	 * Useful when terminals are unwieldy regular expressions such as TRIEs.
	 * 
	 * @param label
	 * @param p
	 * @throws GrammarException
	 */
	public void defineTerminal(String label, Pattern p) throws GrammarException {
		Label l = terminalLabelMap.get(label);
		if (l == null)
			throw new GrammarException("unknown terminal rule: " + label);
		Rule r = rules.get(l);
		if (!(r instanceof DeferredDefinitionRule))
			throw new GrammarException("rule " + label + " already defined");
		((DeferredDefinitionRule) r).setRegex(p);
		undefinedTerminals.remove(r.label());
	}

	public Matcher matches(CharSequence s) throws GrammarException {
		return matches(s, 0, true);
	}

	public Matcher matches(CharSequence s, int offset) throws GrammarException {
		return matches(s, offset, true);
	}

	public Matcher matches(CharSequence s, boolean allowOverlap)
			throws GrammarException {
		return matches(s, 0, !allowOverlap);
	}

	/**
	 * Attempts to match the portion of the input string from the offset on to
	 * the grammar.
	 * 
	 * @param s
	 * @param offset
	 * @return
	 * @throws GrammarException
	 */
	public Matcher matches(final CharSequence s, final int offset,
			final boolean noOverlap) throws GrammarException {
		checkComplete();
		final Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
		final Matcher m = rules.get(root).matcher(s, offset, null, cache, null);
		return new GrammarMatcher(s, offset, noOverlap) {
			boolean matchedOnce = false;
			Match next = fetchNext();

			@Override
			public boolean mightHaveNext() {
				return noOverlap && matchedOnce || next != null;
			}

			private Match fetchNext() {
				Match n;
				while ((n = m.match()) != null) {
					if (n.end() == s.length())
						return n;
				}
				return null;
			}

			@Override
			public synchronized Match match() {
				if (mightHaveNext()) {
					Match n = next;
					next = fetchNext();
					matchedOnce = true;
					return n;
				}
				return null;
			}

			@Override
			public String identify() {
				StringBuilder b = new StringBuilder();
				b.append("matches(");
				if (offset > 0)
					b.append(offset);
				if (noOverlap) {
					if (offset > 0)
						b.append(' ');
					b.append("no overlap");
				}
				b.append(')');
				return b.toString();
			}

			@Override
			protected String name() {
				return "matches";
			}
		};
	}

	/**
	 * Checks to make sure all rules have been defined.
	 * 
	 * @throws GrammarException
	 */
	private void checkComplete() throws GrammarException {
		if (!undefinedTerminals.isEmpty()) {
			LinkedList<Label> list = new LinkedList<Label>(undefinedTerminals);
			Collections.sort(list);
			StringBuilder b = new StringBuilder(
					"terminal rules remaining undefined: ");
			b.append(list.pollFirst().id);
			for (Label l : list) {
				b.append(", ");
				b.append(l.id);
			}
			throw new GrammarException(b.toString());
		}
	}

	public Matcher lookingAt(CharSequence s) throws GrammarException {
		return lookingAt(s, 0, true);
	}

	public Matcher lookingAt(CharSequence s, int offset)
			throws GrammarException {
		return lookingAt(s, offset, true);
	}

	public Matcher lookingAt(CharSequence s, boolean allowOverlap)
			throws GrammarException {
		return lookingAt(s, 0, !allowOverlap);
	}

	public Matcher lookingAt(final CharSequence cs, final int offset,
			final boolean noOverlap) throws GrammarException {
		checkComplete();
		final Matcher m = rules.get(root).matcher(cs, offset, null,
				offsetCache(), null);
		abstract class LookingAtMatcher extends GrammarMatcher {
			LookingAtMatcher(final int offset, final boolean NoOvlerlap) {
				super(cs, offset, NoOvlerlap);
			}

			@Override
			public String name() {
				return "lookingAt";
			}
		}
		// synchronization wrappers
		return noOverlap ? new LookingAtMatcher(offset, noOverlap) {
			boolean matchedOnce = false;

			@Override
			public synchronized boolean mightHaveNext() {
				return matchedOnce ? false : m.mightHaveNext();
			}

			@Override
			public synchronized Match match() {
				Match n = m.match();
				matchedOnce = true;
				return n;
			}
		} : new LookingAtMatcher(offset, noOverlap) {

			@Override
			public synchronized boolean mightHaveNext() {
				return m.mightHaveNext();
			}

			@Override
			public synchronized Match match() {
				return m.match();
			}
		};
	}

	public Matcher find(CharSequence s) throws GrammarException {
		return find(s, 0, true);
	}

	public Matcher find(CharSequence s, int offset) throws GrammarException {
		return find(s, offset, true);
	}

	public Matcher find(CharSequence s, boolean allowOverlap)
			throws GrammarException {
		return find(s, 0, !allowOverlap);
	}

	/**
	 * Finds first match of grammar to string at or after offset.
	 * 
	 * @param s
	 * @param offset
	 * @return
	 * @throws GrammarException
	 */
	public Matcher find(final CharSequence s, final int offset,
			final boolean noOverlap) throws GrammarException {
		checkComplete();
		final Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
		return new GrammarMatcher(s, offset, noOverlap) {
			int index = offset;
			boolean firstMatch = true;
			Matcher m = rules.get(root).matcher(s, index, null, cache, null);
			Match next = fetchNext();

			@Override
			public synchronized Match match() {
				if (mightHaveNext()) {
					Match n = next;
					next = fetchNext();
					return n;
				}
				return null;
			}

			private Match fetchNext() {
				boolean firstNull = true;
				while (true) {
					Match n;
					if (firstMatch) {
						n = m.match();
						firstMatch = false;
					} else if (firstNull && noOverlap)
						n = null;
					else
						n = m.match();
					if (n != null) {
						if (noOverlap)
							index = n.end();
						return n;
					}
					if (!(firstNull && noOverlap))
						index++;
					firstNull = false;
					if (index == s.length())
						break;
					m = rules.get(root).matcher(s, index, null, cache, null);
				}
				return null;
			}

			@Override
			public boolean mightHaveNext() {
				return next != null;
			}

			@Override
			protected String name() {
				return "find";
			}
		};
	}

	/**
	 * Generates a cache to keep track of failing offsets for particular rules.
	 * 
	 * @return map from labels to sets of offsets where the associated rules are
	 *         known not to match
	 */
	private Map<Label, Map<Integer, CachedMatch>> offsetCache() {
		Map<Label, Map<Integer, CachedMatch>> offsetCache = new HashMap<Label, Map<Integer, CachedMatch>>(
				rules.size());
		for (Label l : rules.keySet()) {
			// using tree map for memory management reasons and because
			// comparisons of integers is cheap
			offsetCache.put(l, new TreeMap<Integer, CachedMatch>());
		}
		return offsetCache;
	}

	/**
	 * Setting this non-null turns on match debugging output.
	 * 
	 * @param trace
	 *            sink for debugging trace
	 */
	public void setTrace(PrintStream trace) {
		this.trace = trace;
		if (trace == null)
			stack = null;
		else
			stack = new LinkedList<Match>();
	}
}
