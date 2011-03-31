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
	 * A structure to hold matching options and define defaults.
	 * <p>
	 * <b>Creation date:</b> Mar 28, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	public static class Options implements Cloneable {
		/**
		 * Clones options from a prototype. Use this instead of {@link #clone()}
		 * and you don't have to cast.
		 * 
		 * @param opt
		 *            prototype
		 */
		public Options(Options opt) {
			this.allowOverlap = opt.allowOverlap;
			this.study = opt.study;
			this.startOffset = opt.startOffset;
		}

		/**
		 * Gives you all the defaults.
		 */
		public Options() {
		}

		@Override
		public Object clone() {
			return new Options(this);
		}

		/**
		 * Whether matches may overlap.
		 */
		public static final boolean ALLOW_OVERLAP = false;
		/**
		 * Whether to study the character sequence before matching to accelerate
		 * the matching of terminal rules.
		 */
		public static final boolean STUDY = true;
		/**
		 * Character offset at which to begin matching.
		 */
		public static final int START_OFFSET = 0;
		boolean allowOverlap = ALLOW_OVERLAP;
		boolean study = STUDY;
		int startOffset = START_OFFSET;

		public boolean allowOverlap() {
			return allowOverlap;
		}

		public void allowOverlap(boolean allowOverlap) {
			this.allowOverlap = allowOverlap;
		}

		public boolean study() {
			return study;
		}

		public void study(boolean study) {
			this.study = study;
		}

		public int startOffset() {
			return startOffset;
		}

		public void startOffset(int startOffset) {
			if (startOffset < 0)
				throw new GrammarException("text offsets must be positive");
			this.startOffset = startOffset;
		}
	}

	/**
	 * Special debugging API.
	 * <p>
	 * <b>Creation date:</b> Mar 21, 2011
	 * 
	 * @author David Houghton
	 * 
	 */
	private abstract class GrammarMatcher extends Matcher {

		protected GrammarMatcher(CharSequence s, final Options options,
				Map<Rule, RuleState> ruleStates) {
			super(s, options.startOffset, null, ruleStates);
		}

		protected abstract String name();

		@Override
		protected Rule rule() {
			return null;
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
		DummyMatcher(CharSequence s, final Options options,
				Map<Rule, RuleState> ruleStates) {
			super(s, options.startOffset, null, ruleStates);
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

	private class FindMatcher extends GrammarMatcher {
		private int index;
		private boolean firstMatch;
		private Matcher m;
		private Options options;
		private LinkedList<Integer> startOffsets;
		private Map<Label, Map<Integer, CachedMatch>> cache;
		private Match next;

		FindMatcher(CharSequence s, Options options,
				LinkedList<Integer> startOffsets,
				Map<Label, Map<Integer, CachedMatch>> cache,
				Map<Rule, RuleState> ruleStates) {
			super(s, options, ruleStates);
			this.options = options;
			this.startOffsets = startOffsets;
			this.cache = cache;
			index = options.study && !startOffsets.isEmpty() ? startOffsets
					.removeFirst() : options.startOffset;
			firstMatch = true;
			m = rules.get(root).matcher(s, index, cache, this);
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
				Match n;
				if (firstMatch) {
					n = m.match();
					firstMatch = false;
				} else if (firstNull && !options.allowOverlap)
					n = null;
				else
					n = m.match();
				if (n != null) {
					if (!options.allowOverlap) {
						if (options.study) {
							index = -1;
							while (!startOffsets.isEmpty()) {
								index = startOffsets.removeFirst();
								if (index >= n.end())
									break;
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
				if (index == s.length())
					break;
				m = rules.get(root).matcher(s, index, cache, this);
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
	protected Label root;
	protected final Map<Label, Rule> rules;
	protected final Map<String, Label> terminalLabelMap;
	/**
	 * Keeps track of terminals not defined in initial rule set.
	 */
	protected final HashSet<Label> undefinedRules;
	transient PrintStream trace;

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
		for (Rule r : rules.values())
			r.g = this;
		terminalLabelMap = c.terminalLabelMap();
		undefinedRules = c.undefinedTerminals();
	}

	/**
	 * For defining a terminal rule left undefined by the initial rule set.
	 * Useful when terminals are unwieldy regular expressions such as TRIEs.
	 * 
	 * @param label
	 * @param p
	 * @throws GrammarException
	 */
	public synchronized void defineRule(String label, Pattern p)
			throws GrammarException {
		DeferredDefinitionRule r = checkRuleDefinition(label);
		Label l = new Label(Type.terminal, label);
		LeafRule lr = new LeafRule(l, p);
		Map<String, Label> idMap = new HashMap<String, Label>(rules.size());
		for (Entry<Label, Rule> e : rules.entrySet())
			idMap.put(e.getValue().uniqueId(), e.getKey());
		String id = lr.uniqueId();
		l = idMap.get(id);
		if (l == null)
			r.setRule(lr);
		else
			r.setRule(rules.get(idMap.get(id)));
		undefinedRules.remove(r.label());
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
	}

	public Matcher lookingAt(CharSequence s) throws GrammarException {
		return lookingAt(s, new Options());
	}

	public Matcher lookingAt(final CharSequence cs, Options opt)
			throws GrammarException {
		checkComplete();
		final Options options = verifyOptions(cs, opt);
		final Set<Integer> startOffsets = new HashSet<Integer>();
		Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
		final Map<Rule, RuleState> rs = new HashMap<Rule, RuleState>();
		if (options.study) {
			Set<Rule> studiedRules = new HashSet<Rule>();
			startOffsets.addAll(rules.get(root).study(cs, cache,
					options.startOffset, studiedRules, rs));
		}
		final Matcher m = rules.get(root).matcher(cs, options.startOffset,
				cache, new DummyMatcher(cs, options, rs));
		abstract class LookingAtMatcher extends GrammarMatcher {
			LookingAtMatcher() {
				super(cs, options, rs);
			}

			@Override
			public String name() {
				return "lookingAt";
			}
		}
		// synchronization wrappers
		return !options.allowOverlap ? new LookingAtMatcher() {
			boolean matchedOnce = false;

			@Override
			public synchronized boolean mightHaveNext() {
				if (options.study && startOffsets.isEmpty())
					return false;
				return matchedOnce ? false : m.mightHaveNext();
			}

			@Override
			public synchronized Match match() {
				Match n = null;
				if (!(matchedOnce || options.study && startOffsets.isEmpty())) {
					matchedOnce = true;
					n = m.match();
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
				return m.mightHaveNext();
			}

			@Override
			public synchronized Match match() {
				Match n = null;
				if (!(options.study && startOffsets.isEmpty())) {
					n = m.match();
					if (n != null)
						n.establishParentage();
				}
				return n;
			}
		};
	}

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
		final Options options = verifyOptions(s, opt);
		final Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
		final LinkedList<Integer> startOffsets = new LinkedList<Integer>();
		final Map<Rule, RuleState> ruleStates = new HashMap<Rule, RuleState>();
		if (options.study) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			Set<Rule> studiedRules = new HashSet<Rule>();
			list.addAll(rules.get(root).study(s, cache, options.startOffset,
					studiedRules, ruleStates));
			Collections.sort(list);
			startOffsets.addAll(list);
		}
		return new FindMatcher(s, options, startOffsets, cache, ruleStates);
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
	 * Setting this non-null turns on match debugging output.
	 * 
	 * @param trace
	 *            sink for debugging trace
	 */
	public void setTrace(PrintStream trace) {
		this.trace = trace;
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
		List<Rule> ruleList = new ArrayList<Rule>(rules.values());
		int maxLabel = -1;
		for (Iterator<Rule> i = ruleList.iterator(); i.hasNext();) {
			Rule r = i.next();
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
		Collections.sort(ruleList, new Comparator<Rule>() {
			@Override
			public int compare(Rule o1, Rule o2) {
				int comparison = o2.generation - o1.generation;
				if (comparison == 0)
					comparison = o1.label().toString()
							.compareTo(o2.label().toString());
				return comparison;
			}
		});

		StringBuilder b = new StringBuilder();
		b.append(String.format(format, root));
		b.append(' ');
		b.append(rules.get(root).description());
		b.append("\n\n");
		for (Rule r : ruleList) {
			b.append(String.format(format, r.label()));
			b.append(' ');
			b.append(r.description());
			b.append("\n");
		}
		return b.toString();
	}

	/**
	 * Assigns arbitrary rule to given label.
	 * 
	 * @param label
	 * @param rule
	 */
	public synchronized void defineRule(String label, Rule rule) {
		if (rule instanceof DeferredDefinitionRule)
			throw new GrammarException(
					"you cannot define a rule to be a DeferredDefinitionRule");
		DeferredDefinitionRule r = checkRuleDefinition(label);
		Map<String, Label> idMap = new HashMap<String, Label>(rules.size());
		for (Entry<Label, Rule> e : rules.entrySet())
			idMap.put(e.getValue().uniqueId(), e.getKey());
		String id = rule.uniqueId();
		Label l = idMap.get(id);
		if (l == null)
			r.setRule(rule);
		else
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
		final Options options = verifyOptions(s, opt);
		final Map<Label, Map<Integer, CachedMatch>> cache = offsetCache();
		final Set<Integer> startOffsets = new HashSet<Integer>();
		final Map<Rule, RuleState> ruleStates = new HashMap<Rule, RuleState>();
		if (options.study) {
			Set<Rule> studiedRules = new HashSet<Rule>();
			startOffsets.addAll(rules.get(root).study(s, cache,
					options.startOffset, studiedRules, ruleStates));
		}
		final Matcher m = rules.get(root).matcher(s, options.startOffset,
				cache, new DummyMatcher(s, options, ruleStates));
		return new GrammarMatcher(s, options, ruleStates) {
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

	/**
	 * Validates options and clones object to make it thread safe.
	 * 
	 * @param s
	 * @param opt
	 * @return clone of given options
	 */
	private Options verifyOptions(CharSequence s, Options opt) {
		if (opt.startOffset() >= s.length())
			throw new GrammarException(
					"start offset specified beyond end of string");
		return new Options(opt);
	}

	/**
	 * Assign a complete grammar to an undefined symbol.
	 * 
	 * @param label
	 * @param g
	 */
	public synchronized void defineRule(String label, Grammar g) {
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
			Rule nru = Compiler.fixLabel(l, ru);
			fix(g, ru, nru);
			g.rules.put(l, nru);
			if (isRoot)
				g.root = l;
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
				Rule nru = rules.get(newMap.get(uid));
				fix(g, oru, nru);
			}
		}
		// finally, we augment the original rule set
		for (Entry<Label, Rule> e : g.rules.entrySet()) {
			rules.put(e.getKey(), e.getValue());
		}
		// and we re-assign the grammar field of the cloned rules
		for (Rule ru : g.rules.values())
			ru.g = this;
		// and we define the rule
		r.setRule(g.rules.get(g.root));
		// and fix it so it can't be redefined in the future
		undefinedRules.remove(r.label());
	}

	/**
	 * Replace all instances of old {@link Rule} with new.
	 * 
	 * @param g
	 * @param ru
	 * @param nru
	 */
	private void fix(Grammar g, Rule ru, Rule nru) {
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
			ruleClone.g = clone;
			if (ruleClone instanceof DeferredDefinitionRule)
				((DeferredDefinitionRule) ruleClone).rules = clone.rules;
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
		return clone;
	}
}
