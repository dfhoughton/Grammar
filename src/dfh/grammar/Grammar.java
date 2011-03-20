package dfh.grammar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import dfh.grammar.Label.Type;

/**
 * Base class that parses strings according to a given set of rules.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class Grammar implements Serializable {
	/**
	 * Basic line iterator interface so we can write basically the same
	 * constructor for any sort of input.
	 * <p>
	 * <b>Creation date:</b> Mar 18, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	private interface LineReader {
		String readLine() throws IOException;

		int lineNumber();
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
	private Grammar(LineReader reader) throws GrammarException, IOException {
		String line = null;
		Map<Label, List<RuleFragment>> map = new HashMap<Label, List<RuleFragment>>();
		Label r = null;
		while ((line = reader.readLine()) != null) {
			List<RuleFragment> list = RuleParser.parse(line);
			Label l = (Label) list.remove(0);
			if (map.containsKey(l))
				throw new GrammarException("rule " + l + " redefined at line "
						+ reader.lineNumber());
			map.put(l, list);
			if (l.t == Type.root)
				r = l;
		}
		if (r == null)
			throw new GrammarException("no root rule found");
		this.root = r;
		// make space for anonymous rules
		rules = new HashMap<Label, Rule>(map.size() * 2);
		Set<Label> allLabels = new HashSet<Label>(map.size()), terminals = new HashSet<Label>(
				map.size()), knownLabels = new HashSet<Label>(map.keySet());
		boolean firstPass = true; // we collect terminals on first pass
		while (!map.isEmpty()) {
			int size = map.size();
			for (Iterator<Entry<Label, List<RuleFragment>>> i = map.entrySet()
					.iterator(); i.hasNext();) {
				Entry<Label, List<RuleFragment>> e = i.next();
				Set<Label> labels = allLabels(e.getValue());
				boolean defined = labels.isEmpty();
				if (defined) {
					terminals.add(e.getKey());
					allLabels.add(e.getKey());
				} else {
					defined = true;
					for (Label l : labels) {
						if (!(rules.containsKey(l) || l.t == Type.terminal)) {
							defined = false;
							break;
						}
					}
				}
				if (defined) {
					// if all the constituents of a rule are defined, we define
					// the rule
					rules.put(e.getKey(), parseRule(e.getKey(), e.getValue()));
					allLabels.addAll(labels);
					allLabels.add(e.getKey());
					for (Label l : labels) {
						if (l.t == Type.terminal)
							terminals.add(l);
					}
					i.remove();
				}
			}
			if (!firstPass && map.size() == size)
				throw new GrammarException(
						"impossible co-dependencies exist in rule set");
			firstPass = false;
		}
		terminalLabelMap = new HashMap<String, Label>(terminals.size());
		for (Label l : terminals)
			terminalLabelMap.put(l.id, l);
		allLabels.removeAll(knownLabels);
		allLabels.removeAll(terminals);
		if (!allLabels.isEmpty()) {
			// undefined rules; generate error message
			LinkedList<String> list = new LinkedList<String>();
			for (Label l : allLabels)
				list.add(l.id);
			Collections.sort(list);
			StringBuilder b = new StringBuilder(list.pollFirst());
			for (String s : list)
				b.append(", ").append(s);
			throw new GrammarException("undefined rules: " + b);
		}
		terminals.removeAll(rules.keySet());
		undefinedTerminals = new HashSet<Label>(terminals);
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
		if (r != null)
			throw new GrammarException("terminal rule " + label
					+ " already defined");
		rules.put(l, new LeafRule(l, p));
	}

	private Set<Label> allLabels(List<RuleFragment> value) {
		Set<Label> allLabels = new TreeSet<Label>();
		for (RuleFragment rf : value) {
			if (rf instanceof Label)
				allLabels.add((Label) rf);
			else if (rf instanceof GroupFragment) {
				GroupFragment gf = (GroupFragment) rf;
				for (List<RuleFragment> l : gf.alternates) {
					allLabels.addAll(allLabels(l));
				}
			}
		}
		return allLabels;
	}

	private Rule parseRule(Label l, List<RuleFragment> value) {
		if (l.t == Type.terminal)
			return new LeafRule(l, ((Regex) value.get(0)).re);
		return new SequenceRule(l, value, rules);
	}

	public Matcher matches(String s) throws GrammarException {
		return matches(s, 0);
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
	public Matcher matches(final String s, final int offset)
			throws GrammarException {
		checkComplete();
		final Map<Label, Map<Integer, Node>> cache = offsetCache();
		final Matcher m = rules.get(root).matcher(s.toCharArray(), offset,
				null, cache);
		return new Matcher() {
			Node next = fetchNext();

			@Override
			public boolean mightHaveNext() {
				return next != null;
			}

			private Node fetchNext() {
				Node n;
				while ((n = m.match()) != null) {
					if (n.end() == s.length())
						return n;
				}
				return null;
			}

			@Override
			public synchronized Node match() {
				if (mightHaveNext()) {
					Node n = next;
					next = fetchNext();
					return n;
				}
				return null;
			}

			@Override
			public Rule rule() {
				return null;
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

	public Matcher lookingAt(String s) throws GrammarException {
		return lookingAt(s, 0);
	}

	public Matcher lookingAt(final String s, final int offset)
			throws GrammarException {
		checkComplete();
		final Matcher m = rules.get(root).matcher(s.toCharArray(), offset,
				null, offsetCache());
		// synchronization wrapper
		return new Matcher() {

			@Override
			public Rule rule() {
				return m.rule();
			}

			@Override
			public synchronized boolean mightHaveNext() {
				return m.mightHaveNext();
			}

			@Override
			public synchronized Node match() {
				return m.match();
			}
		};
	}

	public Matcher find(String s) throws GrammarException {
		return find(s, 0);
	}

	/**
	 * Finds first match of grammar to string at or after offset.
	 * 
	 * @param s
	 * @param offset
	 * @return
	 * @throws GrammarException
	 */
	public Matcher find(final String s, final int offset)
			throws GrammarException {
		checkComplete();
		final Map<Label, Map<Integer, Node>> cache = offsetCache();
		final char[] chars = s.toCharArray();
		return new Matcher() {
			int index = offset;
			Matcher m = rules.get(root).matcher(chars, index, null, cache);
			Node next = fetchNext();

			@Override
			public synchronized Node match() {
				if (mightHaveNext()) {
					Node n = next;
					next = fetchNext();
					return n;
				}
				return null;
			}

			private Node fetchNext() {
				while (true) {
					Node n = m.match();
					if (n != null)
						return n;
					index++;
					if (index == s.length())
						break;
					m = rules.get(root).matcher(chars, index, null, cache);
				}
				return null;
			}

			@Override
			public boolean mightHaveNext() {
				return next != null;
			}

			@Override
			public Rule rule() {
				return null;
			}
		};
	}

	/**
	 * Generates a cache to keep track of failing offsets for particular rules.
	 * 
	 * @return map from labels to sets of offsets where the associated rules are
	 *         known not to match
	 */
	private Map<Label, Map<Integer, Node>> offsetCache() {
		Map<Label, Map<Integer, Node>> offsetCache = new HashMap<Label, Map<Integer, Node>>(
				rules.size());
		for (Label l : rules.keySet()) {
			offsetCache.put(l, new TreeMap<Integer, Node>());
		}
		return offsetCache;
	}
}
