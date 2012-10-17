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
import java.util.regex.Pattern;

/**
 * {@link Indexer} that uses a {@link Pattern}.
 * <p>
 * 
 * @author David F. Houghton - Oct 17, 2012
 * 
 */
public class PatternIndexer implements Indexer {
	private static final long serialVersionUID = 1L;
	private final Pattern p;

	public PatternIndexer(Pattern p) {
		this.p = p;
	}

	@Override
	public String toString() {
		return PatternIndexer.class.getSimpleName() + '(' + p + ')';
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see dfh.grammar.Indexer#index(java.lang.CharSequence, int, int)
	 */
	@Override
	public List<Integer> index(CharSequence s, int start, int end) {
		java.util.regex.Matcher m = p.matcher(s);
		m.region(start, end);
		List<Integer> list = new LinkedList<Integer>();
		while (m.find()) {
			list.add(m.start());
			m.region(m.start() + 1, end);
		}
		return list;
	}

}
