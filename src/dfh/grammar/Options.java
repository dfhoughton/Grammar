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
	/**
	 * Whether the {@link Matcher} returned by {@link Grammar} should only
	 * return the longest subsequences that match instead of returning all
	 * matches in order of discovery. This is only considered by
	 * {@link Grammar#find(CharSequence, Options)} and
	 * {@link Grammar#lookingAt(CharSequence, Options)}.
	 */
	public static final boolean LONGEST_TOKEN_MATCHING = true;
	/**
	 * Maximum number of times the parser will visit the same rule without any
	 * forward progress before it gives up on the pattern. So if you have a
	 * grammar like
	 * 
	 * <pre>
	 * &lt;ROOT&gt; = &lt;AP&gt;
	 * 
	 *   &lt;AP&gt; = &lt;DP&gt;? "a"
	 *   &lt;DP&gt; = &lt;AP&gt; "s"
	 * </pre>
	 * 
	 * which produces left-branching parse trees like
	 * 
	 * <pre>
	 *         AP
	 *        /  \
	 *       DP   a
	 *      /  \
	 *     AP   s
	 *    /  \
	 *   DP   a
	 *  /  \
	 * AP   s
	 * |
	 * a
	 * </pre>
	 * 
	 * this value determines how deep the tree can grow. Without some limit like
	 * this the grammar will recurse infinitely while trying to determine how to
	 * match <code>&lt;AP&gt;</code>.
	 * 
	 * The smaller this recursion limit, the faster recursive patterns will
	 * match (and the less chance one has of overflowing the stack). However,
	 * you will be unable to parse deeply-recursive left-branching patterns such
	 * as one finds in the English phrase <i>The Queen's cousins father's
	 * friend's hat</i>.
	 */
	public static final int MAX_RECURSION_DEPTH = 3;
	boolean allowOverlap = ALLOW_OVERLAP;
	boolean study = STUDY;
	boolean longestTokenMatching = LONGEST_TOKEN_MATCHING;
	int start = START_OFFSET;
	int end = -1;
	int maxRecursionDepth = MAX_RECURSION_DEPTH;

	/**
	 * @return see {@link #MAX_RECURSION_DEPTH}
	 */
	public int maxRecursionDepth() {
		return maxRecursionDepth;
	}

	/**
	 * See {@link #MAX_RECURSION_DEPTH}
	 * 
	 * @param maxRecursionDepth
	 */
	public void maxRecursionDepth(int maxRecursionDepth) {
		this.maxRecursionDepth = maxRecursionDepth;
	}

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

	/**
	 * See {@link #LONGEST_TOKEN_MATCHING}.
	 * 
	 * @return whether only the longest matches at a particular offset will be
	 *         returned
	 */
	public boolean longestTokenMatching() {
		return longestTokenMatching;
	}

	/**
	 * See {@link #LONGEST_TOKEN_MATCHING}.
	 * 
	 * @param longestTokenMatching
	 */
	public void longestTokenMatching(boolean longestTokenMatching) {
		this.longestTokenMatching = longestTokenMatching;
	}

	/**
	 * This is equivalent to setting {@link #longestTokenMatching(boolean)} to
	 * <code>false</code> and {@link #allowOverlap(boolean)} to
	 * <code>true</code>.
	 */
	public void matchAll() {
		allowOverlap(true);
		longestTokenMatching(false);
	}
}