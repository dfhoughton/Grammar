/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar.tokens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A {@link CharSequence} that knows what tokens it contains.
 * <p>
 * 
 * @author David F. Houghton - Mar 30, 2012
 * 
 * @param <K>
 */
public class TokenSequence<K extends Token> implements CharSequence {
	private final CharSequence base;
	private NavigableMap<Integer, List<K>> startMap, endMap;

	public TokenSequence(CharSequence base, Collection<K> tokens) {
		this.base = base;
		startMap = new TreeMap<Integer, List<K>>();
		endMap = new TreeMap<Integer, List<K>>();
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

	/**
	 * Returns all tokens starting at the given offset.
	 * 
	 * @param i
	 *            offset
	 * @return all tokens starting at the given offset
	 */
	public List<K> startingAt(Integer i) {
		return startMap.get(i);
	}

	/**
	 * Returns all tokens ending at the given offset.
	 * 
	 * @param i
	 *            offset
	 * @return all tokens ending at the given offset
	 */
	public List<K> endingAt(Integer i) {
		return endMap.get(i);
	}

	@Override
	public char charAt(int i) {
		return base.charAt(i);
	}

	/**
	 * Returns character length of the sequence.
	 * 
	 * @return character length of the sequence
	 */
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

	/**
	 * Returns offsets at which tokens start or end.
	 * 
	 * @return offsets at which tokens start or end
	 */
	public Collection<Integer> boundaries() {
		Set<Integer> boundaries = new TreeSet<Integer>();
		boundaries.addAll(startMap.keySet());
		boundaries.addAll(endMap.keySet());
		return boundaries;
	}
}
