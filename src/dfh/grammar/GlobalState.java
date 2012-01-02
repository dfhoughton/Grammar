package dfh.grammar;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable* data structure holding match options.
 * <p>
 * <b>Creation date:</b> Apr 3, 2011
 * <p>
 * * For a backwards match a temporary mutable matching cache is created, but
 * this should be treated as immutable.
 * 
 * @author David Houghton
 * 
 */
public class GlobalState {
	public final boolean allowOverlap, study, containsCycles, reversed,
			keepRightmost;
	public final int start, end, rcsEnd;
	public final PrintStream trace;
	public final boolean debug;
	public final Map<Integer, CachedMatch>[] backwardsCache;
	public final int maxDepth;
	public final CharSequence cs;
	public final ReversedCharSequence rcs;
	public final boolean isReversed;

	/**
	 * Constructor called in {@link Grammar} only.
	 * 
	 * @param o
	 * @param containsCycles
	 */
	GlobalState(CharSequence cs, Options o, boolean containsCycles) {
		this(cs, new ReversedCharSequence(cs), false, o.allowOverlap, o.start,
				o.end, o.maxRecursionDepth, o.trace, o.study, containsCycles,
				o.keepRightmost, null);
	}

	/**
	 * Constructor called in {@link Assertion} only.
	 * 
	 * @param end
	 * 
	 * @param o
	 * @param backwardsCache
	 */
	GlobalState(GlobalState gs, Map<Integer, CachedMatch>[] backwardsCache) {
		this(gs.cs, gs.rcs, !gs.isReversed, gs.allowOverlap, 0, gs.end,
				gs.maxDepth, gs.trace, false, false, gs.keepRightmost,
				backwardsCache);
	}

	@SuppressWarnings("unchecked")
	private GlobalState(CharSequence cs, ReversedCharSequence rcs,
			boolean isReversed, boolean allowOverlap, int start, int end,
			int maxDepth, PrintStream trace, boolean study,
			boolean containsCycles, boolean keepRightmost,
			Map<Integer, CachedMatch>[] backwardsCache) {
		this.cs = cs;
		this.rcs = rcs;
		this.isReversed = isReversed;
		this.allowOverlap = allowOverlap;
		this.start = start;
		this.end = end;
		this.maxDepth = maxDepth;
		this.trace = trace;
		this.containsCycles = containsCycles;
		this.keepRightmost = keepRightmost;
		this.debug = trace != null;
		if (backwardsCache != null) {
			this.study = false;
			this.reversed = true;
			this.backwardsCache = new Map[backwardsCache.length];
			for (int i = 0; i < backwardsCache.length; i++) {
				this.backwardsCache[i] = new HashMap<Integer, CachedMatch>();
			}
		} else {
			this.study = study;
			this.reversed = false;
			this.backwardsCache = null;
		}
		this.rcsEnd = rcs.translate(start) + 1;
	}

	/**
	 * Returns character sequence appropriate to current match context:
	 * {@link #cs} or {@link #rcs}.
	 * 
	 * @return character sequence appropriate to current match context
	 */
	public CharSequence seq() {
		return isReversed ? rcs : cs;
	}

	/**
	 * Returns end offset appropriate to current match context: {@link #end} or
	 * {@link #rcsEnd}.
	 * 
	 * @return end offset appropriate to current match context
	 */
	public int end() {
		return isReversed ? rcsEnd : end;
	}

	/*
	 * for debugging
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[overlap: " + allowOverlap + "; study: " + study + "; start: "
				+ start + "; end: " + end + "; debug: " + debug + "; cycles: "
				+ containsCycles + "; reversed: " + reversed + "]";
	}
}