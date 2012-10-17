/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

import java.io.Serializable;
import java.util.List;

/**
 * Finds start indices for matching in a sequence.
 * <p>
 * 
 * @author David F. Houghton - Oct 17, 2012
 * 
 */
public interface Indexer extends Serializable {
	/**
	 * Finds start indices for matching in a sequence.
	 * 
	 * @param s
	 *            sequence to find start indices in
	 * @param start
	 *            earliest possible start index
	 * @param end
	 *            index immediately after last possible start index
	 * @return start indices to use in matching
	 */
	public List<Integer> index(CharSequence s, int start, int end);
}
