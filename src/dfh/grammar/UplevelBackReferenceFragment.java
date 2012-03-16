/*
 * dfh.grammar -- a recursive descent parser library for Java
 * 
 * Copyright (C) 2012 David F. Houghton
 * 
 * This software is licensed under the LGPL. Please see accompanying NOTICE file
 * and lgpl.txt.
 */
package dfh.grammar;

/**
 * Like {@link BackReferenceFragment} but allows repetition and creates an
 * {@link UpLevelBackReferenceRule}.
 * <p>
 * <b>Creation date:</b> Dec 10, 2011
 * 
 * @author David Houghton
 * 
 */
public class UplevelBackReferenceFragment extends RepeatableRuleFragment {

	protected int reference, level = 0;

	/**
	 * Generates a fragment from the given reference number.
	 * 
	 * @param reference
	 */
	public UplevelBackReferenceFragment(int reference) {
		this.reference = reference - 1;
	}

	@Override
	public String toString() {
		return Integer.toString(reference + 1) + '^';
	}

}
