package dfh.grammar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
	private static final long serialVersionUID = 1L;
	private final Label root;
	private final Map<Label, Rule> rules;
	private final Map<String, Label> terminalLabelMap;

	public Grammar(InputStream in) throws GrammarException, IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line = null;
		Map<Label, List<RuleFragment>> map = new HashMap<Label, List<RuleFragment>>();
		int lineNumber = 1;
		Label r = null;
		while ((line = reader.readLine()) != null) {
			List<RuleFragment> list = RuleParser.parse(line);
			Label l = (Label) list.remove(0);
			if (map.containsKey(l))
				throw new GrammarException("rule " + l + " redefined at line "
						+ lineNumber);
			map.put(l, list);
			if (l.t == Type.root)
				r = l;
			lineNumber++;
		}
		if (r == null)
			throw new GrammarException("no root rule found");
		this.root = r;
		rules = new HashMap<Label, Rule>(map.size());
		Set<Label> allLabels = new HashSet<Label>(map.size()), terminals = new HashSet<Label>(
				map.size()), knownLabels = new HashSet<Label>(map.keySet());
		for (Entry<Label, List<RuleFragment>> e : map.entrySet()) {
			rules.put(e.getKey(), parseRule(e.getKey(), e.getValue()));
			Set<Label> labels = allLabels(e.getValue());
			allLabels.addAll(labels);
			for (Label l : labels) {
				if (l.t == Type.terminal)
					terminals.add(l);
			}
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
		// TODO finish other types
		return null;
	}

	public Node matches(String s) throws GrammarException {
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
	private Node matches(String s, int offset) throws GrammarException {
		Map<Label, Map<Integer, Node>> cache = offsetCache();
		Matcher m = rules.get(root).matcher(s.toCharArray(), offset, null);
		do {
			Node n = m.match(cache);
			if (n.end() == s.length())
				return n;
			if (m.hasNext())
				m.iterate();
			else
				break;
		} while (true);
		return null;
	}

	public Node lookingAt(String s) {
		return lookingAt(s, 0);
	}

	public Node lookingAt(String s, final int offset) {
		Matcher m = rules.get(root).matcher(s.toCharArray(), offset, null);
		return m.match(offsetCache());
	}

	public Node find(String s) {
		return find(s, 0);
	}

	/**
	 * Finds first match of grammar to string at or after offset.
	 * 
	 * @param s
	 * @param offset
	 * @return
	 */
	public Node find(String s, final int offset) {
		Map<Label, Map<Integer, Node>> cache = offsetCache();
		char[] chars = s.toCharArray();
		for (int i = offset; i < s.length(); i++) {
			Matcher m = rules.get(root).matcher(chars, i, null);
			Node n = m.match(cache);
			if (n != null)
				return n;
		}
		return null;
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
