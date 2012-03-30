package dfh.grammar.tokens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public class TokenSequence<K extends Token> implements CharSequence {

	private static Comparator<Integer> reverseComparator = new Comparator<Integer>() {
		@Override
		public int compare(Integer i1, Integer i2) {
			return i2 - i1;
		}
	};
	private final CharSequence base;
	private NavigableMap<Integer, List<K>> startMap, endMap;

	public TokenSequence(CharSequence base, Collection<K> tokens) {
		this.base = base;
		startMap = new TreeMap<Integer, List<K>>();
		endMap = new TreeMap<Integer, List<K>>(reverseComparator);
		for (K token : tokens) {
			Integer start = token.start(), end = token.end();
			List<K> slist = startMap.get(start), elist = endMap.get(end);
			if (slist == null) {
				slist = new ArrayList<K>(5);
				startMap.put(start, slist);
			}
			slist.add(token);
			if (elist == null) {
				elist = new ArrayList<K>(5);
				endMap.put(end, elist);
			}
			elist.add(token);
		}
	}

	public List<K> startingAt(Integer i) {
		return startMap.get(i);
	}

	public List<K> endingAt(Integer i) {
		return endMap.get(i);
	}

	@Override
	public char charAt(int i) {
		return base.charAt(i);
	}

	@Override
	public int length() {
		return base.length();
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		Collection<K> tokens = tokensWithin(start, end);
		return new TokenSequence<K>(base.subSequence(start, end), tokens);
	}

	/**
	 * Tokens fully contained within the given interval but not necessarily
	 * coterminous with it.
	 * 
	 * @param start
	 *            start offset of interval relative to base sequence
	 * @param end
	 *            end offset of interval relative to base sequence
	 * @return all tokens contained within the interval specified
	 */
	public Collection<K> tokensWithin(int start, int end) {
		List<K> tokens = new LinkedList<K>();
		for (List<K> sublist : startMap.tailMap(start, true)
				.headMap(end, false).values()) {
			for (K k : sublist) {
				if (k.end() >= end)
					tokens.add(k);
			}
		}
		return tokens;
	}

	/**
	 * Finds those tokens coterminous with a given interval.
	 * 
	 * @param start
	 *            token start
	 * @param end
	 *            token end
	 * @return just those tokens beginning at the start offset and ending at the
	 *         end offset
	 */
	public Collection<K> tokensAt(Integer start, Integer end) {
		List<K> list = startMap.get(start);
		if (list == null)
			return null;
		List<K> matches = new ArrayList<K>(list.size());
		for (K k : list) {
			if (k.end() == end.intValue())
				matches.add(k);
		}
		return matches;
	}

	public boolean beginsToken(Integer i) {
		return startMap.containsKey(i);
	}

	public boolean endsToken(Integer i) {
		return endMap.containsKey(i);
	}

	/**
	 * @return start offsets of all tokens
	 */
	public Set<Integer> tokenOffsets() {
		return startMap.keySet();
	}

	@Override
	public String toString() {
		return base.toString();
	}

	public Collection<Entry<Integer, List<K>>> starts() {
		return startMap.entrySet();
	}
}
