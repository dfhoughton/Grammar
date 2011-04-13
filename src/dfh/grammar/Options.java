package dfh.grammar;

import java.io.PrintStream;

/**
 * A structure to hold matching options and define defaults.
 * <p>
 * <b>Creation date:</b> Mar 28, 2011
 * 
 * @author David Houghton
 * 
 */
public class Options {

	/**
	 * Whether matches may overlap.
	 */
	public static final boolean ALLOW_OVERLAP = false;
	/**
	 * Whether to study the character sequence before matching to accelerate the
	 * matching of terminal rules.
	 */
	public static final boolean STUDY = true;
	/**
	 * Character offset at which to begin matching.
	 */
	public static final int START_OFFSET = 0;
	boolean allowOverlap = ALLOW_OVERLAP;
	boolean study = STUDY;
	int start = START_OFFSET;
	int end = -1;
	PrintStream trace;

	/**
	 * All defaults.
	 */
	public Options() {
	}

	/**
	 * Clone existing global state.
	 * 
	 * @param options
	 */
	public Options(GlobalState options) {
		this.allowOverlap = options.allowOverlap;
		this.end = options.end;
		this.start = options.start;
		this.study = options.study;
		this.trace = options.trace;
	}

	/**
	 * @return whether matches iterated over may overlap
	 */
	public boolean allowOverlap() {
		return allowOverlap;
	}

	/**
	 * @param allowOverlap
	 *            whether matches iterated over may overlap
	 */
	public void allowOverlap(boolean allowOverlap) {
		this.allowOverlap = allowOverlap;
	}

	/**
	 * @return whether the {@link CharSequence} will be studied before matching
	 */
	public boolean study() {
		return study;
	}

	/**
	 * @param study
	 *            whether the {@link CharSequence} will be studied before
	 *            matching
	 */
	public void study(boolean study) {
		this.study = study;
	}

	/**
	 * @return point in {@link CharSequence} at which to begin matching
	 */
	public int start() {
		return start;
	}

	/**
	 * @param start
	 *            point in {@link CharSequence} at which to begin matching
	 */
	public void start(int start) {
		if (start < 0)
			throw new GrammarException("text offsets must be positive");
		this.start = start;
	}

	/**
	 * Turn match debugging output on or off.
	 * 
	 * @param trace
	 *            data sink for debugging
	 */
	public void trace(PrintStream trace) {
		this.trace = trace;
	}

	/**
	 * @return data sink for debugging
	 */
	public PrintStream trace() {
		return trace;
	}

	/**
	 * @param end
	 *            end of region to match
	 */
	public void end(int end) {
		if (end <= start)
			throw new GrammarException("end offset must follow start");
		this.end = end;
	}

	/**
	 * @return end of region to match; returns -1 if the end is the end of the
	 *         sequence to match
	 */
	public int end() {
		return end;
	}
}