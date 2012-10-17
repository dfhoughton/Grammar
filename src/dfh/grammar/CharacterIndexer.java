/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link Indexer} that uses {@link String#indexOf(int, int)}.
 * <p>
 * 
 * @author David F. Houghton - Oct 17, 2012
 * 
 */
public class CharacterIndexer implements Indexer {
	private static final long serialVersionUID = 1L;
	private final char clue;

	public CharacterIndexer(char clue) {
		this.clue = clue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dfh.grammar.Indexer#index(java.lang.CharSequence, int, int)
	 */
	@Override
	public List<Integer> index(CharSequence s, int start, int end) {
		List<Integer> list = new LinkedList<Integer>();
		String st = s.toString();
		int index;
		while ((index = st.indexOf(clue, start)) > -1) {
			list.add(index);
			start = index + 1;
		}
		return list;
	}

}
