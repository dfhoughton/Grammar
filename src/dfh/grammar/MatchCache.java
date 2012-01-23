package dfh.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * The {@link Map} interface wrapped around an array. This implementation of a
 * map is optimized for getting and putting. It has a fixed size and will throw
 * an error if any integer is put into it outside its range of possible indices.
 * It is great as a cache for grammars but isn't all that flexible. If you want
 * flexibility, try <a href="http://trove.starlight-systems.com/">Trove</a>.
 * Methods in {@link MatchCache} other than {@link #get(Object)},
 * {@link #put(Integer, CachedMatch)}, and {@link #putAll(Map)} are *not*
 * optimized. In particular, {@link #size()}, {@link #keySet()}, and
 * {@link #values()} will all take considerably longer than their equivalent in
 * {@link HashMap}, say, or {@link TreeMap}.
 * 
 * @author David Houghton
 */
public class MatchCache implements Map<Integer, CachedMatch> {
	private CachedMatch[] cache;

	public MatchCache(int size) {
		cache = new CachedMatch[size + 1];
	}

	@Override
	public void clear() {
		Arrays.fill(cache, null);
	}

	@Override
	public boolean containsKey(Object arg0) {
		return cache[((Integer) arg0).intValue()] != null;
	}

	@Override
	public boolean containsValue(Object arg0) {
		if (arg0 == null)
			return false;
		for (CachedMatch cm : cache) {
			if (cm != null && cm.equals(arg0))
				return true;
		}
		return false;
	}

	@Override
	public Set<Entry<Integer, CachedMatch>> entrySet() {
		Set<java.util.Map.Entry<Integer, CachedMatch>> set = new LinkedHashSet<Map.Entry<Integer, CachedMatch>>(
				cache.length);
		for (int i = 0; i < cache.length; i++) {
			CachedMatch cm = cache[i];
			if (cm != null) {
				final Integer k = i;
				Entry<Integer, CachedMatch> e = new Entry<Integer, CachedMatch>() {

					private CachedMatch value;

					@Override
					public CachedMatch setValue(CachedMatch value) {
						return this.value = value;
					}

					@Override
					public CachedMatch getValue() {
						return value;
					}

					@Override
					public Integer getKey() {
						return k;
					}
				};
				e.setValue(cm);
				set.add(e);
			}
		}
		return set;
	}

	@Override
	public CachedMatch get(Object arg0) {
		return cache[((Integer) arg0).intValue()];
	}

	@Override
	public boolean isEmpty() {
		for (CachedMatch cm : cache) {
			if (cm != null)
				return false;
		}
		return true;
	}

	@Override
	public Set<Integer> keySet() {
		Set<Integer> set = new TreeSet<Integer>();
		for (int i = 0; i < cache.length; i++) {
			if (cache[i] != null)
				set.add(i);
		}
		return set;
	}

	@Override
	public CachedMatch put(Integer arg0, CachedMatch arg1) {
		CachedMatch old = cache[arg0.intValue()];
		cache[arg0.intValue()] = arg1;
		return old;
	}

	@Override
	public void putAll(Map<? extends Integer, ? extends CachedMatch> arg0) {
		for (Entry<? extends Integer, ? extends CachedMatch> e : arg0
				.entrySet()) {
			cache[e.getKey()] = e.getValue();
		}
	}

	@Override
	public CachedMatch remove(Object arg0) {
		int i = ((Integer) arg0).intValue();
		CachedMatch cm = cache[i];
		cache[i] = null;
		return cm;
	}

	@Override
	public int size() {
		int count = 0;
		for (CachedMatch cm : cache)
			if (cm != null)
				count++;
		return count;
	}

	@Override
	public Collection<CachedMatch> values() {
		Collection<CachedMatch> values = new ArrayList<CachedMatch>(
				cache.length);
		for (CachedMatch cm : cache) {
			if (cm != null)
				values.add(cm);
		}
		return values;
	}

}
