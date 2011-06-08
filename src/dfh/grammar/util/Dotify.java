package dfh.grammar.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import dfh.grammar.Match;

/**
 * For making graphs showing the structure of matches. The output must be
 * processed with GraphViz or something else that can make graph images from
 * .dot files.
 * <p>
 * <b>Creation date:</b> Apr 2, 2011
 * 
 * @author David Houghton
 * 
 */
public class Dotify {

	/**
	 * Non-OO class.
	 */
	private Dotify() {
	}

	/**
	 * Completes GraphViz notation for collection of graphs.
	 * 
	 * @param b
	 *            {@link StringBuilder} accumulating GraphViz notation
	 */
	public static void endDot(StringBuilder b) {
		b.append("}\n");
	}

	/**
	 * Appends a new subgraph to GraphViz notation.
	 * 
	 * @param b
	 *            {@link StringBuilder} accumulating GraphViz notation
	 * @param n
	 *            match to graph
	 * @param text
	 *            text matched against
	 * @param index
	 *            single-element integer array used as a source of unique
	 *            identifiers for graph nodes; each time a new node is created
	 *            the stored value is used and incremented
	 * @param caption
	 *            any label to attach to graph; <code>text</code> will be used
	 *            if left <code>null</code>
	 */
	public static void appendGraph(StringBuilder b, Match n, CharSequence text,
			int[] index, String caption) {
		if (caption == null)
			caption = cleanText(n, text);
		b.append("subgraph cluster").append(index[0]).append(" {\n");
		b.append("label = ");
		b.append(id(caption)).append("\n");
		Map<Object, String> idMap = new HashMap<Object, String>();
		Set<String> sameRank = new TreeSet<String>();
		List<Match> matchList = n.get(Match.WIDE);
		for (Match m : matchList) {
			String id = getId(index, idMap, m);
			b.append(id).append(' ').append("[label=");
			b.append(id(m.explicit() ? m.rule().label().toString() : m.rule()
					.description()));
			if (m.explicit())
				b.append(",shape=box");
			b.append("]\n");
			if (m.isTerminal()) {
				String id2 = "n" + index[0]++;
				sameRank.add(id2);
				String s = cleanText(m, text);
				idMap.put(s, id2);
				b.append(id2).append(' ').append("[label=");
				b.append(id(s)).append("]\n");
				b.append(id).append(" -- ").append(id2).append('\n');
			} else {
				b.append(id).append(" -- ");
				int count = 0;
				for (Match child : m.children()) {
					if (!child.zeroWidth())
						count++;
				}
				if (count == 1) {
					for (Match child : m.children()) {
						if (!child.zeroWidth()) {
							String id2 = getId(index, idMap, child);
							b.append(id2);
							break;
						}
					}
				} else {
					b.append('{');
					for (Match child : m.children()) {
						if (!child.zeroWidth()) {
							String id2 = getId(index, idMap, child);
							b.append(' ').append(id2);
						}
					}
					b.append('}');
				}
				b.append('\n');
			}
		}
		if (sameRank.size() > 1) {
			b.append("{ rank = same; ");
			for (String s : sameRank) {
				b.append(s).append("; ");
			}
			b.append("}\n");
		}
		b.append("}\n");
	}

	private static String getId(int[] index, Map<Object, String> idMap, Match m) {
		String id = idMap.get(m);
		if (id == null) {
			id = "n" + index[0]++;
			idMap.put(m, id);
		}
		return id;
	}

	private static String cleanText(Match n, CharSequence text) {
		String suffix = "(" + n.start() + ", " + n.end() + ")";
		if (n.zeroWidth())
			return suffix;
		String s = text.subSequence(n.start(), n.end()).toString().trim();
		if (s.length() == 0)
			s = "\" \"";
		else
			s = s.replaceAll("\\s++", " ");
		return s + ' ' + suffix;
	}

	/**
	 * Begins GraphViz notation for a collection of matches.
	 * 
	 * @param graphName
	 *            name assigned to graph; default is "match"
	 * @return {@link StringBuilder} in which to accumulate GraphViz notation
	 */
	public static StringBuilder startDot(String graphName) {
		if (graphName == null)
			graphName = "match";
		String id = id(graphName);
		StringBuilder b = new StringBuilder();
		b.append("graph ");
		b.append(id);
		b.append(" {\n");
		b.append("label=").append(id).append('\n');
		b.append("node [shape=plaintext,fontsize=10]\n");
		return b;
	}

	/**
	 * @param name
	 * @return GraphViz ID
	 */
	private static String id(String name) {
		StringBuilder b = new StringBuilder();
		b.append('"');
		for (char c : name.toCharArray()) {
			if (c == '\\' || c == '"')
				b.append('\\');
			b.append(c);
		}
		b.append('"');
		return b.toString();
	}

	/**
	 * Convenience method wrapping others. This returns a string one can paste
	 * in as the entire contents of a .dot file.
	 * 
	 * @param text
	 *            {@link CharSequence} matched against
	 * @param n
	 *            match; empty graph returned for <code>null</code>
	 * @param message
	 *            optional message to display with graph
	 * @return single graph for match
	 */
	public static String dot(CharSequence text, Match n, String message) {
		if (n == null) {
			String label = message == null ? "match" : message;
			label = id(label);
			return "graph " + label + " {\nlabel=\"no match\"\n}";
		}
		StringBuilder b = startDot(message);
		appendGraph(b, n, text, new int[] { 0 }, message);
		endDot(b);
		return b.toString();
	}
}
