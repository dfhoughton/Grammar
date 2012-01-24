package dfh.grammar;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.TreeMap;

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
	public static final boolean LONGEST_MATCH = true;
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
	 * as one finds in the English phrase <i>The Queen's cousin's father's
	 * friend's hat</i>.
	 */
	public static final int MAX_RECURSION_DEPTH = 3;
	/**
	 * Whether to keep the rightmost successful submatch by default. This
	 * information assists in debugging a failed match but adds overhead during
	 * matching.
	 */
	public static final boolean KEEP_RIGHTMOST = false;
	/**
	 * The default value of {@link #leanMemory()}: whether or not to use an
	 * offset matching cache optimized for minimal memory usage. There are three
	 * options: {@link MatchCache}, {@link HashMap}, and {@link TreeMap}. The
	 * last uses the least memory; the first, the most. If you are matching
	 * against very long character sequences and are in a memory constrained
	 * environment, setting {@link #leanMemory()} to true enforces the use of a
	 * {@link TreeMap}.
	 */
	public static final boolean LEAN_MEMORY = false;
	/**
	 * The default value of {@link #fatMemory()}: whether or not to always use
	 * the fastest, least memory-efficient offset cache. See
	 * {@link #LEAN_MEMORY}.
	 */
	public static final boolean FAT_MEMORY = false;
	/**
	 * If both {@link #leanMemory()} and {@link #fatMemory()} are false, the
	 * grammar will choose whether to use a fast, fat cache --
	 * {@link MatchCache} -- or a slower, leaner one -- {@link HashMap} --
	 * depending on how long the sequence is it's matching against. This is the
	 * default length threshold. If the sequence is longer than this, a
	 * {@link HashMap} is used, otherwise, a {@link MatchCache}.
	 */
	public static final int LONG_STRING_LENGTH = 100000;
	boolean allowOverlap = ALLOW_OVERLAP;
	boolean study = STUDY;
	boolean longestMatch = LONGEST_MATCH;
	boolean keepRightmost = KEEP_RIGHTMOST;
	boolean leanMemory = LEAN_MEMORY;
	boolean fatMemory = FAT_MEMORY;
	int start = START_OFFSET;
	int end = -1;
	int longStringLength = LONG_STRING_LENGTH;
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
	 * @return self to allow chaining of methods
	 */
	public Options maxRecursionDepth(int maxRecursionDepth) {
		this.maxRecursionDepth = maxRecursionDepth;
		return this;
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
	 * @return self to allow chaining of methods
	 */
	public Options allowOverlap(boolean allowOverlap) {
		this.allowOverlap = allowOverlap;
		return this;
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
	 * @return self to allow chaining of methods
	 */
	public Options study(boolean study) {
		this.study = study;
		return this;
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
	 * @return self to allow chaining of methods
	 */
	public Options start(int start) {
		if (start < 0)
			throw new GrammarException("text offsets must be positive");
		this.start = start;
		return this;
	}

	/**
	 * Turn match debugging output on or off.
	 * 
	 * @param log
	 *            data sink for debugging
	 * @return self to allow chaining of methods
	 */
	public Options log(PrintStream log) {
		this.trace = log;
		return this;
	}

	/**
	 * @return data sink for debugging
	 */
	public PrintStream log() {
		return trace;
	}

	/**
	 * @param end
	 *            end of region to match
	 * @return self to allow chaining of methods
	 */
	public Options end(int end) {
		if (end > 0 && end <= start)
			throw new GrammarException("end offset must follow start");
		this.end = end;
		return this;
	}

	/**
	 * @return end of region to match; returns -1 if the end is the end of the
	 *         sequence to match
	 */
	public int end() {
		return end;
	}

	/**
	 * See {@link #LONGEST_MATCH}.
	 * 
	 * @return whether only the longest matches at a particular offset will be
	 *         returned
	 */
	public boolean longestMatch() {
		return longestMatch;
	}

	/**
	 * See {@link #LONGEST_MATCH}.
	 * 
	 * @param longestMatch
	 * @return self to allow chaining of methods
	 */
	public Options longestMatch(boolean longestMatch) {
		this.longestMatch = longestMatch;
		return this;
	}

	/**
	 * This is equivalent to setting {@link #longestMatch(boolean)} to
	 * <code>false</code> and {@link #allowOverlap(boolean)} to
	 * <code>true</code>.
	 * 
	 * @return self to allow chaining of methods
	 */
	public Options matchAll() {
		allowOverlap(true);
		longestMatch(false);
		return this;
	}

	/**
	 * Returns whether the rightmost successful submatch will be retained by the
	 * {@link Matcher}. This information is useful in debugging but adds a
	 * slight overhead. The default values is {@value #KEEP_RIGHTMOST}.
	 * 
	 * @return whether the rightmost successful submatch will be retained by the
	 *         {@link Matcher}
	 */
	public boolean keepRightmost() {
		return keepRightmost;
	}

	/**
	 * Sets whether the rightmost successful submatch will be retained by the
	 * {@link Matcher}. This information is useful in debugging but adds a
	 * slight overhead. The default values is {@value #KEEP_RIGHTMOST}.
	 * 
	 * @param keepRightmost
	 *            whether the rightmost successful submatch should be retained
	 *            by the {@link Matcher}
	 * @return self to allow chaining of methods
	 */
	public Options keepRightmost(boolean keepRightmost) {
		this.keepRightmost = keepRightmost;
		return this;
	}

	/**
	 * Whether to use a slightly slower, leaner memory cache, or one of the
	 * faster ones that consumes more memory. See {@link #LEAN_MEMORY},
	 * {@link #FAT_MEMORY}, and {@value #LONG_STRING_LENGTH}.
	 * 
	 * @return whether to use a memory-efficient cache
	 */
	public boolean leanMemory() {
		return leanMemory;
	}

	/**
	 * Sets whether to use a slightly slower, leaner memory cache, or one of the
	 * faster ones that consumes more memory. See {@link #LEAN_MEMORY},
	 * {@link #FAT_MEMORY}, and {@value #LONG_STRING_LENGTH}.
	 * 
	 * @param leanMemory
	 *            whether to use a memory-efficient cache
	 * @return self to allow chaining of methods
	 */
	public Options leanMemory(boolean leanMemory) {
		this.leanMemory = leanMemory;
		if (leanMemory)
			fatMemory = false;
		return this;
	}

	/**
	 * Whether to always use the fastest, least memory-inefficient cache. See
	 * {@link #FAT_MEMORY},{@link #LEAN_MEMORY}, and {@link #LONG_STRING_LENGTH}
	 * .
	 * 
	 * @return whether to always use the fastest, least memory-efficient cache
	 */
	public boolean fatMemory() {
		return fatMemory;
	}

	/**
	 * Sets whether to always use the fastest, least memory-inefficient cache.
	 * See {@link #FAT_MEMORY},{@link #LEAN_MEMORY}, and
	 * {@link #LONG_STRING_LENGTH}.
	 * 
	 * @param fatMemory
	 *            whether to use the fastest, least memory-efficient cache
	 * @return self to allow chaining of methods
	 */
	public Options fatMemory(boolean fatMemory) {
		this.fatMemory = fatMemory;
		if (fatMemory)
			leanMemory = false;
		return this;
	}

	/**
	 * @return end of region to match; returns -1 if the end is the end of the
	 *         sequence to match
	 */
	public int longStringLength() {
		return longStringLength;
	}

	/**
	 * See {@link #LONG_STRING_LENGTH}.
	 * 
	 * @param longStringLength
	 *            threshold length for switching from {@link MatchCache} to
	 *            {@link HashMap}
	 * @return self to allow chaining of methods
	 */
	public Options longStringLength(int longStringLength) {
		if (longStringLength < 1)
			throw new GrammarException("longStringLength must be positive");
		this.longStringLength = longStringLength;
		return this;
	}
}