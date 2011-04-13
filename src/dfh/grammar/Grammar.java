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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import dfh.grammar.Label.Type;

/**
 * Base class that parses strings according to a given set of rules.
 * <p>
 * A {@link Grammar} is constructed from a set of rules such as
 * 
 * <pre>
 * &lt;ROOT&gt; = &lt;a&gt; | &lt;b&gt;
 * 
 *    &lt;a&gt; = &lt;foo&gt; &lt;s&gt; &lt;bar&gt;
 *    &lt;b&gt; = &lt;quux&gt; &lt;s&gt; &lt;baz&gt;
 *  &lt;bar&gt; = /bar/
 *  &lt;baz&gt; = /baz/
 *  &lt;foo&gt; = /foo/
 * &lt;quux&gt; = /quux/
 *    &lt;s&gt; = /\s++/
 * </pre>
 * 
 * And in turn constructs {@link Matcher} objects that can be applied to a
 * {@link CharSequence} to produce a sequence of {@link Match} objects
 * representing how this pattern matches the characters. A {@link Grammar} has
 * several advantages over a {@link Pattern} and one chief disadvantage.
 * <p>
 * <h3>Advantages</h3>
 * <ul>
 * <li>Ease of composition, reading, maintenance, anddebugging. See
 * {@link #setTrace(PrintStream)} and {@link #describe()}.
 * <li>One can compose grammars from component {@link Rule rules}, regular
 * expressions, and other grammars. See {@link #defineRule(String, Pattern)},
 * {@link #defineRule(String, Rule)}.
 * <li>One can iterate over all possible ways of matching a {@link CharSequence}
 * , not just a non-overlapping subset.
 * <li>Given a {@link Match} one can find the precise way the pattern matches,
 * each rule and subrule that participated in the match and which offsets is
 * applied to.
 * </ul>
 * <h3>Disadvantages</h3>
 * I am certain that someone who didn't write these classes would find
 * infelicities to enumerate in the API. The chief disadvantage that I am aware
 * of is that matchign with a {@link Grammar} is about an order of magnitude
 * slower than matching with a simple {@link Pattern}. For one thing, fewer
 * people have spent less time optimizing the code. But even were Oracle to take
 * over this project the scope for efficiency is less simply because the task
 * attempted is greater.
 * <h2>Rule Definition</h2>
 * <h2>Acknowledgements</h2> This class and its supporting classes was inspired
 * by the recursive regular expressions available in Perl 5.10+, and especially
 * their sugared up form in <a
 * href="http://search.cpan.org/search?query=Regexp%3A%3AGrammars&mode=module"
 * target="_blank">Regexp::Grammars</a> and <a
 * href="http://en.wikipedia.org/wiki/Perl_6_rules" target="_blank">Perl 6
 * rules</a>.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class Grammar implements Serializable, Cloneable {
	/**
	 * Special debugging API.
	 * <p>
	 * <b>Creation date:</b> Mar 21, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	private abstract class GrammarMatcher extends Matcher {

		protected GrammarMatcher(CharSequence s, GlobalState options) {
			super(s, options.start, null, options);
		}

		protected abstract String name();

		@Override
		protected Rule rule() {
			return null;
		}
	}

	/**
	 * {@link Matcher} wrapper to implement LTM.
	 * <p>
	 * <b>Creation date:</b> Apr 13, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	private static class LTMMatcher {
		private final LinkedList<Match> matches = new LinkedList<Match>();

		LTMMatcher(Matcher m) {
			int max = -1;
			try {
				Match n;
				while ((n = m.match()) != null) {
					int w = n.end() - n.start();
					if (w > max) {
						matches.clear();
						max = w;
					}
					if (w == max)
						matches.add(n);
				}
			} catch (DoubleColonBarrier e) {
			}
		}

		Match match() {
			if (hasNext())
				return matches.removeFirst();
			return null;
		}

		boolean hasNext() {
			return !matches.isEmpty();
		}
	}

	/**
	 * For starting the chain of inheritance (in terms of handing down from
	 * parent to child, not OO) of the rule state map.
	 * <p>
	 * <b>Creation date:</b> Mar 30, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	private class DummyMatcher extends Matcher {
		DummyMatcher(CharSequence s, GlobalState options) {
			super(s, options.start, null, options);
		}

		@Override
		public Match match() {
			return null;
		}

		@Override
		protected boolean mightHaveNext() {
			return false;
		}

		@Override
		protected Rule rule() {
			return null;
		}

	}

	/**
	 * {@link Matcher} for {@link Grammar#find(CharSequence, Options)}.
	 * 
	 * @author David Houghton
	 * 
	 */
	private class FindMatcher extends GrammarMatcher {
		private int index;
		private boolean firstMatch;
		private Matcher m;
		private LTMMatcher ltmm;
		private LinkedList<Integer> startOffsets;
		private Map<Label, Map<Integer, CachedMatch>> cache;
		private Match next;
		private final boolean ltm;

		FindMatcher(CharSequence s, LinkedList<Integer> startOffsets,
				Map<Label, Map<Integer, CachedMatch>> cache,
				GlobalState options, boolean ltm) {
			super(s, options);
			this.startOffsets = startOffsets;
			this.cache = cache;
			this.ltm = ltm;
			index = options.study && !startOffsets.isEmpty() ? startOffsets
					.removeFirst() : options.start;
			firstMatch = true;
			m = rules.get(root).matcher(s, index, cache, this);
			if (ltm)
				ltmm = new LTMMatcher(m);
			next = fetchNext();
		}

		@Override
		public synchronized Match match() {
			if (mightHaveNext()) {
				Match n = next;
				next = fetchNext();
				if (n != null)
					n.establishParentage();
				return n;
			}
			return null;
		}

		private Match fetchNext() {
			if (options.study && index == -1)
				return null;
			boolean firstNull = true;
			while (true) {
				Match n = null;
				if (firstMatch) {
					try {
						n = ltm ? ltmm.match() : m.match();
						firstMatch = false;
					} catch (DoubleColonBarrier e) {
					}
				} else if (firstNull && !options.allowOverlap)
					n = null;
				else {
					try {
						n = ltm ? ltmm.match() : m.match();
					} catch (DoubleColonBarrier e) {
					}
				}
				if (n != null) {
					if (!options.allowOverlap) {
						if (options.study) {
							index = -1;
							while (!startOffsets.isEmpty()) {
								index = startOffsets.removeFirst();
								if (index >= n.end())
									break;
								else
									index = -1;
							}
						} else
							index = n.end();
					}
					return n;
				}
				if (!(firstNull && !options.allowOverlap)) {
					if (options.study) {
						if (startOffsets.isEmpty())
							break;
						index = startOffsets.removeFirst();
					} else
						index++;
				}
				firstNull = false;
				if (index >= options.end)
					break;
				m = rules.get(root).matcher(s, index, cache, this);
				if (ltm)
					ltmm = new LTMMatcher(m);
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
	}

	private static final long serialVersionUID = 1L;
	/**
	 * {@link Label} of root {@link Rule}.
	 */
	protected Label root;
	/**
	 * Collection of all {@link Rule rules}.
	 */
	protected final Map<Label, Rule> rules;
	/**
	 * Collection of labels for rules of {@link Type#terminal} or
	 * {@link Type#literal}.
	 */
	protected final Map<String, Label> terminalLabelMap;
	/**
	 * Keeps track of terminals not defined in initial rule set.
	 */
	protected final HashSet<Label> undefinedRules;
	private boolean recursive;
	protected final Map<String, Set<Label>> undefinedConditions;
	private final Map<String, Set<Rule>> knownConditions;
	private final Map<String, Condition> conditionMap = new HashMap<String, Condition>();

	/**
	 * Delegates to {@link #Grammar(LineReader)}.
	 * 
	 * @param lines
	 *            rule source
	 * @throws GrammarException
	 * @throws IOException
	 */
	public Grammar(String[] lines) throws GrammarException, IOException {
		this(new ArrayLineReader(lines));
	}

	/**
	 * Required for cloning.
	 * 
	 * @param root
	 */
	private Grammar(Label root) {
		this.root = root;
		this.rules = new HashMap<Label, Rule>();
		this.terminalLabelMap = new HashMap<String, Label>();
		this.undefinedRules = new HashSet<Label>();
		this.undefinedConditions = new HashMap<String, Set<Label>>();
		this.knownConditions = new HashMap<String, Set<Rule>>();
	}

	/**
	 * Delegates to {@link #Grammar(Reader)} and ultimately
	 * {@link #Grammar(LineReader)}.
	 * 
	 * @param f
	 *            rule source
	 * @throws GrammarException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Grammar(File f) throws GrammarException, FileNotFoundException,
			IOException {
		this(new FileReader(f));
	}

	/**
	 * Delegates to {@link #Grammar(Reader)} and ultimately
	 * {@link #Grammar(LineReader)}.
	 * 
	 * @param is
	 *            rule source
	 * @throws GrammarException
	 * @throws IOException
	 */
	public Grammar(InputStream is) throws GrammarException, IOException {
		this(new InputStreamReader(is));
	}

	/**
	 * Delegates to {@link #Grammar(BufferedReader)} and ultimately
	 * {@link #Grammar(LineReader)}.
	 * 
	 * @param r
	 *            rule source
	 * @throws GrammarException
	 * @throws IOException
	 */
	public Grammar(Reader r) throws GrammarException, IOException {
		this(new BufferedReader(r));
	}

	/**
	 * Delegates to {@link #Grammar(BufferedReader)} and ultimately to
	 * {@link #Grammar(LineReader)}.
	 * 
	 * @param r
	 *            rule source
	 * @throws GrammarException
	 * @throws IOException
	 */
	public Grammar(BufferedReader r) throws GrammarException, IOException {
		this(new BufferedLineReader(r));
	}

	/**
	 * Creates a {@link Compiler} to parse input and prepares a {@link Grammar}
	 * to generate {@link Matcher} objects.
	 * 
	 * @param reader
	 * @throws GrammarException
	 * @throws IOException
	 */
	public Grammar(LineReader reader) throws GrammarException, IOException {
		Compiler c = new Compiler(reader);
		root = c.root();
		rules = c.rules();
		terminalLabelMap = c.terminalLabelMap();
		undefinedRules = c.undefinedTerminals();
		recursive = c.recursive();
		undefinedConditions = c.undefinedConditions();
		knownConditions = new HashMap<String, Set<Rule>>(
				undefinedConditions.size());
	}

	/**
	 * For defining a terminal rule left undefined by the initial rule set. In
	 * this case you are defining the rule to be a {@link LeafRule}. This is
	 * useful when the regular expression is too unwieldy to fit in the grammar
	 * file or when you generate it in the program.
	 * 
	 * @param label
	 * @param p
	 * @param id
	 *            unique identifier for condition, if any
	 * @param c
	 *            optional {@link Condition}
	 * @throws GrammarException
	 */
	public synchronized void defineRule(String label, Pattern p, String id,
			Condition c) throws GrammarException {
		DeferredDefinitionRule r = checkRuleDefinition(label);
		Label l = new Label(Type.terminal, label);
		LeafRule lr = new LeafRule(l, p, false);
		if (c != null) {
			lr = (LeafRule) conditionCheck(id, c, lr);
		}
		redefinitionCheck(r, lr);
	}

	/**
	 * Makes sure we keep things which should be unique unique.
	 * 
	 * @param id
	 * @param c
	 * @param lr
	 * @return
	 */
	private Rule conditionCheck(String id, Condition c, Rule lr) {
		if (c == null) {
			if (id != null)
				throw new GrammarException(
						"null condition given for non-null condition identifier "
								+ id);
			return lr;
		} else {
			Condition other = conditionMap.get(id);
			if (other == null)
				conditionMap.put(id, c);
			else if (!other.equals(c))
				throw new GrammarException("condition identifier " + id
						+ " already in use");
			Set<Rule> set = knownConditions.get(id);
			if (!(set == null || set.isEmpty()))
				throw new GrammarException("condition identifier " + id
						+ " already in use");
			if (set == null) {
				set = new HashSet<Rule>();
				knownConditions.put(id, set);
			} else if (set.contains(lr)) {
				return lr;
			}
			// remove lr from any set already containing it
			for (Set<Rule> s : knownConditions.values())
				s.remove(lr);
			lr = lr.conditionalize(c, id);
			set.add(lr);
			return lr;
		}
	}

	/**
	 * @param r
	 * @param lr
	 * @return whether rule is novel
	 */
	private boolean redefinitionCheck(DeferredDefinitionRule r, Rule lr) {
		Label l;
		Map<String, Label> idMap = new HashMap<String, Label>(rules.size());
		for (Entry<Label, Rule> e : rules.entrySet())
			idMap.put(e.getValue().uniqueId(), e.getKey());
		String id = lr.uniqueId();
		l = idMap.get(id);
		if (l == null) {
			r.setRule(lr);
		} else
			r.setRule(rules.get(idMap.get(id)));
		undefinedRules.remove(r.label());
		return l == null;
	}

	private DeferredDefinitionRule checkRuleDefinition(String label) {
		Label l = terminalLabelMap.get(label);
		if (l == null)
			throw new GrammarException("unknown terminal rule: " + label);
		Rule r = rules.get(l);
		if (!(r instanceof DeferredDefinitionRule))
			throw new GrammarException("rule " + label + " already defined");
		return (DeferredDefinitionRule) r;
	}

	/**
	 * Generates iterator over matches beginning on the first character of the
	 * sequence (or at the offset specified in the {@link Options} object) and
	 * ending on the last character.
	 * 
	 * @param s
	 * @return {@link Matcher} iterating over matches
	 * @throws GrammarException
	 */
	public Matcher matches(CharSequence s) throws GrammarException {
		return matches(s, new Options());
	}

	/**
	 * Checks to make sure all rules have been defined.
	 * 
	 * @throws GrammarException
	 */
	private void checkComplete() throws GrammarException {
		if (!undefinedRules.isEmpty()) {
			LinkedList<Label> list = new LinkedList<Label>(undefinedRules);
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
		if (!undefinedConditions.isEmpty()) {
			LinkedList<String> list = new LinkedList<String>();
			list.addAll(undefinedConditions.keySet());
			Collections.sort(list);
			StringBuilder b = new StringBuilder(
					"conditions remaining undefined: ");
			b.append(list.pollFirst());
			for (String s : list) {
				b.append(", ");
				b.append(s);
			}
			throw new GrammarException(b.toString());
		}
	}

	/**
	 * Generates iterator over matches whose start offset is the beginning of
	 * the given {@link CharSequence}.
	 * 
	 * @param s
	 * @return iterator over matches
	 * @throws GrammarException
	 */
	public Matcher lookingAt(CharSequence s) throws GrammarException {
		return lookingAt(s, new Options());
	}

	/**
	 * Generates iterator over matches whose start offset is the beginning of
	 * the given {@link CharSequence}.
	 * 
	 * @param cs
	 * @param opt
	 *            {@link Options} to apply in matching
	 * @return iterator over matches
	 * @throws GrammarException
	 */
	public Matcher lookingAt(final CharSequence cs, Options opt)
			throws GrammarException {
		checkComplete();
		final GlobalState co = verifyOptions(cs, opt);
		final boolean ltm = opt.longestTokenMatching();
		final Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
		final Set<Integer> startOffsets = startOffsets(cs, co, cache);
		final Matcher m = rules.get(root).matcher(cs, co.start, cache,
				new DummyMatcher(cs, co));
		final LTMMatcher ltmm = ltm ? new LTMMatcher(m) : null;
		abstract class LookingAtMatcher extends GrammarMatcher {
			LookingAtMatcher() {
				super(cs, co);
			}

			@Override
			public String name() {
				return "lookingAt";
			}

		}
		// synchronization wrappers
		return !co.allowOverlap ? new LookingAtMatcher() {
			boolean matchedOnce = false;

			@Override
			public synchronized boolean mightHaveNext() {
				if (options.study && startOffsets.isEmpty())
					return false;
				try {
					return matchedOnce ? false : (ltm ? ltmm.hasNext() : m
							.mightHaveNext());
				} catch (DoubleColonBarrier e) {
					return false;
				}
			}

			@Override
			public synchronized Match match() {
				Match n = null;
				if (!(matchedOnce || options.study && startOffsets.isEmpty())) {
					matchedOnce = true;
					try {
						n = ltm ? ltmm.match() : m.match();
					} catch (DoubleColonBarrier e) {
						return null;
					}
					if (n != null)
						n.establishParentage();
				}
				return n;
			}
		} : new LookingAtMatcher() {

			@Override
			public synchronized boolean mightHaveNext() {
				if (options.study && startOffsets.isEmpty())
					return false;
				try {
					return ltm ? ltmm.hasNext() : m.mightHaveNext();
				} catch (DoubleColonBarrier e) {
					return false;
				}
			}

			@Override
			public synchronized Match match() {
				Match n = null;
				if (!(options.study && startOffsets.isEmpty())) {
					try {
						n = ltm ? ltmm.match() : m.match();
					} catch (DoubleColonBarrier e) {
						return null;
					}
					if (n != null)
						n.establishParentage();
				}
				return n;
			}
		};
	}

	/**
	 * Creates iterator over matches occurring anywhere in given
	 * {@link CharSequence}.
	 * 
	 * @param s
	 * @return object iterating over matches
	 * @throws GrammarException
	 */
	public Matcher find(CharSequence s) throws GrammarException {
		return find(s, new Options());
	}

	/**
	 * Generates iterator over matches anywhere in sequence.
	 * 
	 * @param s
	 *            sequence against which to match
	 * @param opt
	 *            matching parameters
	 * @return {@link Matcher} object for iterating over matches
	 * @throws GrammarException
	 */
	public Matcher find(final CharSequence s, Options opt)
			throws GrammarException {
		checkComplete();
		final GlobalState options = verifyOptions(s, opt);
		final boolean ltm = opt.longestTokenMatching();
		final Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
		List<Integer> list = new ArrayList<Integer>(startOffsets(s, options,
				cache));
		Collections.sort(list);
		final LinkedList<Integer> startOffsets = new LinkedList<Integer>(list);
		return new FindMatcher(s, startOffsets, cache, options, ltm);
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
			// might use TreeMap to save a little memory
			offsetCache.put(l, new HashMap<Integer, CachedMatch>());
		}
		return offsetCache;
	}

	/**
	 * The canonical order, at the moment, is to sort them primarily by their
	 * degree of independence from other rules and secondarily by their label.
	 * The root rule comes first, separated by a blank line, then they descend
	 * in order of abstraction and alphabet. Terminal rules are always last.
	 * <p>
	 * I may change the canonical order in the future. It has the advantage of
	 * putting rules immediately above those rules they depend on most directly
	 * but the disadvantage of making it hard to find an arbitrary rule.
	 * 
	 * @return canonical description of {@link Grammar}
	 */
	public String describe() {
		List<Entry<Label, Rule>> ruleList = new ArrayList<Map.Entry<Label, Rule>>(
				rules.entrySet());
		int maxLabel = -1;
		for (Iterator<Entry<Label, Rule>> i = ruleList.iterator(); i.hasNext();) {
			Entry<Label, Rule> e = i.next();
			Rule r = e.getValue();
			if (r.generation == -1) {
				i.remove();
				continue;
			}
			int l = r.label().toString().length();
			if (l > maxLabel)
				maxLabel = l;
			if (r == rules.get(root))
				i.remove();
		}
		String format = "%" + maxLabel + "s =";
		// put rules in canonical order
		Collections.sort(ruleList, new Comparator<Entry<Label, Rule>>() {
			@Override
			public int compare(Entry<Label, Rule> o1, Entry<Label, Rule> o2) {
				int comparison = o2.getValue().generation
						- o1.getValue().generation;
				if (comparison == 0)
					comparison = o1.getKey().toString()
							.compareTo(o2.getKey().toString());
				return comparison;
			}
		});

		StringBuilder b = new StringBuilder();
		b.append(String.format(format, root));
		b.append(' ');
		b.append(rules.get(root).description());
		b.append('\n');
		if (!ruleList.isEmpty()) {
			b.append('\n');
			for (Entry<Label, Rule> e : ruleList) {
				b.append(String.format(format, e.getKey()));
				b.append(' ');
				b.append(e.getValue().description());
				b.append("\n");
			}
		}
		return b.toString();
	}

	/**
	 * Assigns arbitrary rule to given label. This arbitrary rule must be
	 * clonable, as defining the deferred definition rule requires modifying the
	 * state of the rule object that will replace it.
	 * 
	 * @param label
	 * @param rule
	 */
	public synchronized void defineRule(String label, Rule rule) {
		if (rule instanceof DeferredDefinitionRule)
			throw new GrammarException(
					"you cannot define a rule to be a DeferredDefinitionRule");
		if (!(rule instanceof Cloneable))
			throw new GrammarException("rule must be clonable");
		try {
			Method m = rule.getClass().getMethod("clone");
			rule = (Rule) m.invoke(rule);
		} catch (Exception e1) {
			throw new GrammarException("rule must be clonable: " + e1);
		}
		DeferredDefinitionRule r = checkRuleDefinition(label);
		Map<String, Label> idMap = new HashMap<String, Label>(rules.size());
		for (Entry<Label, Rule> e : rules.entrySet())
			idMap.put(e.getValue().uniqueId(), e.getKey());
		String id = rule.uniqueId();
		Label l = idMap.get(id);
		if (l == null) {
			r.setRule(rule);
		} else
			r.setRule(rules.get(idMap.get(id)));
		undefinedRules.remove(r.label());
	}

	/**
	 * Generates iterator over matches beginning on the first character of the
	 * sequence (or at the offset specified in the {@link Options} object) and
	 * ending on the last character.
	 * 
	 * @param s
	 *            sequence against which to match
	 * @param opt
	 *            matching parameters
	 * @return {@link Matcher} object with which one can iterate over matches
	 * @throws GrammarException
	 */
	public Matcher matches(final CharSequence s, Options opt)
			throws GrammarException {
		checkComplete();
		final GlobalState options = verifyOptions(s, opt);
		final Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
		final Set<Integer> startOffsets = startOffsets(s, options, cache);
		final Matcher m = rules.get(root).matcher(s, options.start, cache,
				new DummyMatcher(s, options));
		return new GrammarMatcher(s, options) {
			boolean matchedOnce = false;
			Match next = fetchNext();

			@Override
			public boolean mightHaveNext() {
				if (options.study && startOffsets.isEmpty())
					return false;
				return !options.allowOverlap && matchedOnce || next != null;
			}

			private Match fetchNext() {
				if (options.study && startOffsets.isEmpty())
					return null;
				Match n;
				try {
					while ((n = m.match()) != null) {
						if (n.end() == options.end)
							return n;
					}
				} catch (DoubleColonBarrier e) {
				}
				return null;
			}

			@Override
			public synchronized Match match() {
				if (mightHaveNext()) {
					Match n = next;
					next = fetchNext();
					matchedOnce = true;
					if (n != null)
						n.establishParentage();
					return n;
				}
				return null;
			}

			@Override
			protected String name() {
				return "matches";
			}
		};
	}

	private Set<Integer> startOffsets(final CharSequence s,
			final GlobalState options,
			final Map<Label, Map<Integer, CachedMatch>> cache) {
		final Set<Integer> startOffsets = new HashSet<Integer>();
		if (options.study) {
			Set<Rule> studiedRules = new HashSet<Rule>();
			if (options.containsCycles) {
				for (Rule r : rules.values()) {
					if (r instanceof AlternationRule
							|| r instanceof SequenceRule
							|| r instanceof RepetitionRule
							|| r instanceof CyclicRule)
						continue;
					startOffsets.addAll(r
							.study(s, cache, studiedRules, options));
				}
			} else
				startOffsets.addAll(rules.get(root).study(s, cache,
						studiedRules, options));
		}
		return startOffsets;
	}

	/**
	 * Validates options and clones object to make it thread safe.
	 * 
	 * @param s
	 * @param opt
	 * @return clone of given options
	 */
	private GlobalState verifyOptions(CharSequence s, Options opt) {
		if (opt.start() >= s.length())
			throw new GrammarException(
					"start offset specified beyond end of string");
		if (opt.end == -1)
			opt.end(s.length());
		return new GlobalState(opt, recursive);
	}

	/**
	 * Assign a complete grammar to an undefined symbol.
	 * <p>
	 * Defining a {@link Rule} as a {@link Grammar} presents namespace
	 * difficulties because the two grammars are compiled independently, so up
	 * to the point of assignment there is no way to ensure there won't be
	 * namespace collisions. To solve this problem, the label that is being
	 * redefined becomes a namespace prefix should there be collisions.
	 * <p>
	 * Furthermore, during compilation redundant rules are consolidated. This
	 * same process is replicated when a symbol is redefined as a grammar.
	 * Putting this together with namespace changes may produce
	 * counter-intuitive results. For example, consider the following
	 * composition of three grammars.
	 * <h2>Root Grammar</h2>
	 * 
	 * <pre>
	 * &lt;ROOT&gt; = &lt;bar&gt; | &lt;quux&gt;
	 * 
	 *  &lt;foo&gt; = &lt;bar&gt; | &lt;quux&gt;
	 * &lt;quux&gt; = &lt;b&gt; &lt;foo&gt; 1
	 *  &lt;bar&gt; = &lt;a&gt;
	 *    &lt;a&gt; = UNDEFINED
	 *    &lt;b&gt; = UNDEFINED
	 * </pre>
	 * 
	 * <h2>Grammar for <code>&lt;a&gt;</code></h2>
	 * 
	 * <pre>
	 * &lt;ROOT&gt; = &lt;a&gt;{2} &lt;b&gt;
	 * 
	 *    &lt;a&gt; = "a"
	 *    &lt;b&gt; = "b"
	 * </pre>
	 * 
	 * <h2>Grammar for <code>&lt;b&gt;</code></h2>
	 * 
	 * <pre>
	 * &lt;ROOT&gt; = &lt;a&gt; &lt;b&gt;{1,2}
	 * 
	 *    &lt;a&gt; = "a"
	 *    &lt;b&gt; = "b"
	 * </pre>
	 * 
	 * <h2>Result</h2>
	 * 
	 * <pre>
	 * &lt;ROOT&gt; = &lt;bar&gt; | &lt;quux&gt;
	 * 
	 * &lt;quux&gt; = &lt;b&gt; &lt;foo&gt; 1
	 *  &lt;foo&gt; = &lt;bar&gt; | &lt;quux&gt;
	 *  &lt;bar&gt; = &lt;a&gt;
	 *  &lt;a:a&gt; = "a"
	 *  &lt;a:b&gt; = "b"
	 *    &lt;a&gt; = &lt;a:a&gt;{2} &lt;a:b&gt;
	 *    &lt;b&gt; = &lt;a:a&gt; &lt;a:b&gt;{1,2}
	 * </pre>
	 * 
	 * The matching pattern is what you would expect but you will see that the
	 * <code>b</code> namespace goes entirely unused because all of the named
	 * elements of the <code>&lt;b&gt;</code> grammar have been reconstituted
	 * from components of <code>&lt;a&gt;</code>.
	 * 
	 * @param label
	 * @param g
	 * @param id
	 *            unique identifier for condition, if any
	 * @param c
	 *            optional {@link Condition}
	 */
	public synchronized void defineRule(String label, Grammar g, String id,
			Condition c) {
		DeferredDefinitionRule r = checkRuleDefinition(label);
		g.checkComplete();
		g = (Grammar) g.clone();
		// now we fix labels
		List<Label> labels = new ArrayList<Label>(g.rules.size());
		for (Label l : g.rules.keySet()) {
			if (rules.containsKey(l) && g.rules.get(l).generation > -1)
				labels.add(l);
		}
		// rename
		for (Label l : labels) {
			Rule ru = g.rules.remove(l);
			boolean isRoot = l.equals(g.root);
			String s = label + ':' + l.id;
			l = new Label(l.t, s);
			Rule nru = Compiler.fixLabel(l, ru, ru.condition);
			fix(g, ru, nru);
			g.rules.put(l, nru);
			if (isRoot) {
				g.root = l;
				// hide root
				nru.generation = -1;
			}
		}
		// and we check for any lingering redundancies
		Map<String, Label> oldMap = new HashMap<String, Label>(rules.size()), newMap = new HashMap<String, Label>(
				g.rules.size());
		for (Entry<Label, Rule> e : rules.entrySet())
			oldMap.put(e.getValue().uniqueId(), e.getKey());
		for (Entry<Label, Rule> e : g.rules.entrySet())
			newMap.put(e.getValue().uniqueId(), e.getKey());
		for (String uid : newMap.keySet()) {
			if (oldMap.containsKey(uid)) {
				Rule oru = g.rules.remove(newMap.get(uid));
				Rule nru = rules.get(oldMap.get(uid));
				fix(g, oru, nru);
			}
		}
		// we augment the original rule set
		for (Entry<Label, Rule> e : g.rules.entrySet()) {
			rules.put(e.getKey(), e.getValue());
		}
		// and we define the rule
		Rule rr = g.rules.get(g.root);
		if (c != null)
			rr = conditionCheck(label, c, rr);
		if (redefinitionCheck(r, rr)) {

			// finally, we reassign generation numbers to better indicate
			// dependency
			Map<Rule, Set<Rule>> dependentMap = new HashMap<Rule, Set<Rule>>(
					rules.size());
			Set<Rule> requireAssignment = new HashSet<Rule>(rules.size());
			for (Rule ru : rules.values()) {
				Set<Rule> dependents = dependents(ru);
				dependentMap.put(ru, dependents);
				if (!ru.label.equals(root) && ru.generation > -1) {
					requireAssignment.add(ru);
				}
			}
			int generation = 0;
			while (!requireAssignment.isEmpty()) {
				generation++;
				List<Rule> reassigned = new ArrayList<Rule>(
						requireAssignment.size());
				OUTER: for (Rule ru : requireAssignment) {
					Set<Rule> dependents = dependentMap.get(ru);
					for (Rule dr : dependents) {
						if (requireAssignment.contains(dr))
							continue OUTER;
					}
					reassigned.add(ru);
				}
				if (reassigned.isEmpty()) {
					// looping, break out
					for (Rule ru : requireAssignment)
						ru.generation = generation;
					break;
				} else {
					for (Rule ru : reassigned)
						ru.generation = generation;
					requireAssignment.removeAll(reassigned);
				}
			}
		}
	}

	private Set<Rule> dependents(Rule ru) {
		if (ru instanceof LeafRule || ru instanceof LiteralRule)
			return new HashSet<Rule>(0);
		if (ru instanceof AlternationRule) {
			AlternationRule ar = (AlternationRule) ru;
			Set<Rule> set = new HashSet<Rule>(ar.alternates.length);
			for (Rule r : ar.alternates)
				set.add(r);
			return set;
		}
		if (ru instanceof SequenceRule) {
			SequenceRule sr = (SequenceRule) ru;
			Set<Rule> set = new HashSet<Rule>(sr.sequence.length);
			for (Rule r : sr.sequence)
				set.add(r);
			return set;
		}
		if (ru instanceof RepetitionRule) {
			RepetitionRule rr = (RepetitionRule) ru;
			Set<Rule> set = new HashSet<Rule>(1);
			set.add(rr.r);
			return set;
		}
		if (ru instanceof DeferredDefinitionRule) {
			DeferredDefinitionRule ddr = (DeferredDefinitionRule) ru;
			Set<Rule> set = new HashSet<Rule>(1);
			set.add(ddr.r);
			return set;
		}
		if (ru instanceof CyclicRule) {
			CyclicRule cr = (CyclicRule) ru;
			Set<Rule> set = new HashSet<Rule>(1);
			set.add(cr.r);
			return set;
		}
		return new HashSet<Rule>(0);
	}

	/**
	 * Replace all instances of old {@link Rule} with new.
	 * 
	 * @param g
	 * @param ru
	 * @param nru
	 */
	private void fix(Grammar g, Rule ru, Rule nru) {
		nru.generation = ru.generation;
		for (Rule r : g.rules.values()) {
			if (r instanceof AlternationRule) {
				AlternationRule ar = (AlternationRule) r;
				for (int i = 0; i < ar.alternates.length; i++) {
					if (ar.alternates[i] == ru)
						ar.alternates[i] = nru;
				}
			} else if (r instanceof SequenceRule) {
				SequenceRule sr = (SequenceRule) r;
				for (int i = 0; i < sr.sequence.length; i++) {
					if (sr.sequence[i] == ru)
						sr.sequence[i] = nru;
				}
			} else if (r instanceof RepetitionRule) {
				RepetitionRule rr = (RepetitionRule) r;
				if (rr.r == ru)
					rr.r = nru;
			} else if (r instanceof DeferredDefinitionRule) {
				DeferredDefinitionRule ddr = (DeferredDefinitionRule) r;
				if (ddr.r == ru)
					ddr.r = nru;
			} else if (r instanceof CyclicRule) {
				CyclicRule cr = (CyclicRule) r;
				if (cr.r == ru)
					cr.r = nru;
			}
		}
	}

	@Override
	public Object clone() {
		Grammar clone = new Grammar((Label) root.clone());
		Map<Label, Label> labelMap = new HashMap<Label, Label>(rules.size());
		Map<Rule, Rule> ruleMap = new HashMap<Rule, Rule>(rules.size());
		for (Entry<Label, Rule> e : rules.entrySet()) {
			Label labelClone = (Label) e.getKey().clone();
			Rule ruleClone = e.getValue().shallowClone();
			ruleClone.generation = e.getValue().generation;
			labelMap.put(e.getKey(), labelClone);
			ruleMap.put(e.getValue(), ruleClone);
			clone.rules.put(labelClone, ruleClone);
		}
		for (Entry<String, Label> e : terminalLabelMap.entrySet())
			clone.terminalLabelMap.put(e.getKey(), labelMap.get(e.getValue()));
		for (Label l : undefinedRules)
			clone.undefinedRules.add(labelMap.get(l));
		for (Entry<Rule, Rule> e : ruleMap.entrySet())
			fix(clone, e.getKey(), e.getValue());
		for (Entry<String, Set<Label>> e : undefinedConditions.entrySet()) {
			Set<Label> set = new HashSet<Label>(e.getValue().size());
			for (Label l : e.getValue())
				set.add(labelMap.get(l));
			clone.undefinedConditions.put(e.getKey(), set);
		}
		for (Entry<String, Set<Rule>> e : knownConditions.entrySet()) {
			Set<Rule> newSet = new HashSet<Rule>(e.getValue().size());
			for (Rule r : e.getValue())
				newSet.add(ruleMap.get(r));
			knownConditions.put(e.getKey(), newSet);
		}
		return clone;
	}

	/**
	 * For defining a terminal rule left undefined by the initial rule set. In
	 * this case you are defining the rule to be a {@link LiteralRule}.
	 * 
	 * @param label
	 *            id of undefined rule
	 * @param literal
	 *            String to match
	 * @param id
	 *            unique identifier for condition, if any
	 * @param c
	 *            optional {@link Condition}
	 * @throws GrammarException
	 */
	public synchronized void defineRule(String label, String literal,
			String id, Condition c) throws GrammarException {
		DeferredDefinitionRule r = checkRuleDefinition(label);
		Label l = new Label(Type.literal, label);
		LiteralRule lr = new LiteralRule(l, literal);
		if (c != null)
			lr = (LiteralRule) conditionCheck(label, c, lr);
		redefinitionCheck(r, lr);
	}

	/**
	 * Assign a condition to an arbitrary {@link Rule}.
	 * 
	 * @param labelId
	 * @param conditionId
	 * @param c
	 */
	public synchronized void assignCondition(String labelId,
			String conditionId, Condition c) {
		if (undefinedConditions.containsKey(conditionId))
			throw new GrammarException(conditionId
					+ " belongs to an as yet undefined condition");
		Label l = null;
		for (Label label : rules.keySet()) {
			if (label.id.equals(labelId)) {
				l = label;
				break;
			}
		}
		if (l == null)
			throw new GrammarException("unknown rule: " + labelId);
		Rule r = rules.remove(l);
		Rule nr = conditionCheck(conditionId, c, r);
		fix(this, r, nr);
		rules.put(l, nr);
	}

	/**
	 * @param label
	 *            condition identifier
	 * @param c
	 */
	public synchronized void defineCondition(String label, Condition c) {
		Set<Label> set = undefinedConditions.remove(label);
		if (set == null)
			throw new GrammarException("no undefined condition " + label);
		Set<Rule> rset = new HashSet<Rule>(set.size());
		knownConditions.put(label, rset);
		for (Label l : set) {
			Rule r = rules.remove(l);
			Rule nr = r.conditionalize(c, label);
			if (r != nr)
				fix(this, r, nr);
			rules.put(l, nr);
			rset.add(nr);
		}
	}

	/**
	 * Assign a complete grammar to an undefined symbol. Delegates to
	 * {@link #defineRule(String, Grammar, String, Condition)}, using
	 * <code>null</code> for last two parameters.
	 * 
	 * @param label
	 * @param g
	 */
	public synchronized void defineRule(String label, Grammar g) {
		defineRule(label, g, null, null);
	}

	/**
	 * For defining a terminal rule left undefined by the initial rule set.
	 * Delegates to {@link #defineRule(String, Pattern, String, Condition)},
	 * using <code>null</code> for last two parameters.
	 * 
	 * @param label
	 * @param p
	 * @throws GrammarException
	 */
	public synchronized void defineRule(String label, Pattern p)
			throws GrammarException {
		defineRule(label, p, null, null);
	}

	/**
	 * For defining a terminal rule left undefined by the initial rule set.
	 * Delegates to {@link #defineRule(String, String, String, Condition)},
	 * using <code>null</code> for last two parameters.
	 * 
	 * @param label
	 *            id of undefined rule
	 * @param literal
	 *            String to match
	 * @throws GrammarException
	 */
	public synchronized void defineRule(String label, String literal)
			throws GrammarException {
		defineRule(label, literal, null, null);
	}
}
