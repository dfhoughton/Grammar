package dfh.grammar;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
	public final boolean allowOverlap, study, containsCycles, reversed;
	public final int start, end;
	public final PrintStream trace;
	public final boolean debug;
	public final Map<Label, Map<Integer, CachedMatch>> backwardsCache;

	/**
	 * Constructor called in {@link Grammar} only.
	 * 
	 * @param o
	 * @param containsCycles
	 */
	GlobalState(Options o, boolean containsCycles) {
		this(o.allowOverlap, o.start, o.end, o.trace, o.study, containsCycles,
				null);
	}

	/**
	 * Constructor called in {@link Assertion} only.
	 * 
	 * @param end
	 * 
	 * @param o
	 * @param backwardsCache
	 */
	GlobalState(GlobalState gs, int end,
			Map<Label, Map<Integer, CachedMatch>> backwardsCache) {
		this(gs.allowOverlap, 0, end, gs.trace, false, false, backwardsCache);
	}

	private GlobalState(boolean allowOverlap, int start, int end,
			PrintStream trace, boolean study, boolean containsCycles,
			Map<Label, Map<Integer, CachedMatch>> backwardsCache) {
		this.allowOverlap = allowOverlap;
		this.start = start;
		this.end = end;
		this.trace = trace;
		this.containsCycles = containsCycles;
		this.debug = trace != null;
		if (backwardsCache != null) {
			this.study = false;
			this.reversed = true;
			this.backwardsCache = new HashMap<Label, Map<Integer, CachedMatch>>(
					backwardsCache.size());
			for (Entry<Label, Map<Integer, CachedMatch>> e : backwardsCache
					.entrySet()) {
				this.backwardsCache.put(e.getKey(),
						new HashMap<Integer, CachedMatch>());
			}
		} else {
			this.study = study;
			this.reversed = false;
			this.backwardsCache = null;
		}
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