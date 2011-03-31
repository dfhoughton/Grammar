package dfh.grammar;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * {@link Rule} defined over sequence of terminal objects rather than other
 * <code>Rules</code>.
 * <p>
 * <b>Creation date:</b> Feb 19, 2011
 * 
 * @author David Houghton
 */
public class LeafRule extends Rule {
	/**
	 * For normalizing the unique ids of regexes that allow comments.
	 */
	private static final Pattern NORMALIZATION_PATTERN = Pattern.compile(
			"#.*$", Pattern.MULTILINE);

	private class LeafMatcher extends Matcher {
		private Map<Integer, CachedMatch> cache;
		private boolean fresh = true;

		public LeafMatcher(CharSequence s, int offset,
				Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
			super(s, offset, master);
			this.cache = cache.get(label);
		}

		@Override
		public Match match() {
			LeafRule.this.matchTrace(this);
			if (fresh) {
				fresh = false;
				CachedMatch cm = cache.get(offset);
				if (cm == null) {
					if (ruleStates.containsKey(rule())) {
						LeafRule.this.matchTrace(this, null);
						return null;
					}
				} else {
					LeafRule.this.matchTrace(this, cm.m);
					return cm.m;
				}
				java.util.regex.Matcher m = p.matcher(s);
				m.region(offset, s.length());
				m.useTransparentBounds(true);
				m.useAnchoringBounds(false);
				if (m.lookingAt()) {
					Match n = new Match(LeafRule.this, offset, m.end());
					cm = new CachedMatch(n);
				} else
					cm = CachedMatch.MISMATCH;
				cache.put(offset, cm);
				LeafRule.this.matchTrace(this, cm.m);
				return cm.m;
			}
			LeafRule.this.matchTrace(this, null);
			return null;
		}

		@Override
		public boolean mightHaveNext() {
			return fresh;
		}

		@Override
		public String toString() {
			return "M:" + LeafRule.this;
		}

		@Override
		protected Rule rule() {
			return LeafRule.this;
		}
	}

	private static final long serialVersionUID = 1L;
	final Pattern p;

	public LeafRule(Label label, Pattern p) {
		super(label);
		this.p = p;
	}

	@Override
	public Matcher matcher(CharSequence s, final Integer offset,
			Map<Label, Map<Integer, CachedMatch>> cache, Matcher master) {
		return new LeafMatcher(s, offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		String s = description();
		if ((p.flags() & Pattern.COMMENTS) == Pattern.COMMENTS) {
			// must normalize away spaces and comments
			int index = s.lastIndexOf('/');
			String suffix = s.substring(index);
			s = s.substring(1, index);
			StringBuilder b = new StringBuilder();
			java.util.regex.Matcher m = NORMALIZATION_PATTERN.matcher(s);
			int start = 0;
			while (m.find()) {
				b.append(s.substring(start, m.start()));
				start = m.end();
			}
			if (start < s.length())
				b.append(s.substring(start));
			s = b.toString();
			s = s.replaceAll("\\s++", "");
			return '/' + s + suffix;
		}
		return description();
	}

	@Override
	public String description() {
		StringBuilder b = new StringBuilder();
		b.append('/');
		b.append(p.toString());
		b.append('/');
		if ((p.flags() & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE)
			b.append('i');
		if ((p.flags() & Pattern.DOTALL) == Pattern.DOTALL)
			b.append('s');
		if ((p.flags() & Pattern.MULTILINE) == Pattern.MULTILINE)
			b.append('m');
		if ((p.flags() & Pattern.UNIX_LINES) == Pattern.UNIX_LINES)
			b.append('d');
		if ((p.flags() & Pattern.UNICODE_CASE) == Pattern.UNICODE_CASE)
			b.append('u');
		if ((p.flags() & Pattern.COMMENTS) == Pattern.COMMENTS)
			b.append('x');
		return b.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Label, Map<Integer, CachedMatch>> cache, int offset,
			Set<Rule> studiedRules, Map<Rule, RuleState> ruleStates) {
		studiedRules.add(this);
		ruleStates.put(this, new TerminalState(true));
		Map<Integer, CachedMatch> subCache = cache.get(label);
		Set<Integer> startOffsets = new HashSet<Integer>();
		if (!subCache.keySet().isEmpty()) {
			startOffsets.addAll(subCache.keySet());
		} else {
			java.util.regex.Matcher m = p.matcher(s);
			m.useAnchoringBounds(false);
			m.useTransparentBounds(true);
			m.region(offset, s.length());
			while (m.find()) {
				Integer i = m.start();
				startOffsets.add(i);
				Match n = new Match(this, m.start(), m.end());
				subCache.put(i, new CachedMatch(n));
				int newStart = m.start() + 1;
				if (newStart == s.length())
					break;
				m.region(newStart, s.length());
			}
		}
		return startOffsets;
	}

	@Override
	public boolean zeroWidth() {
		return false;
	}

	@Override
	public Rule shallowClone() {
		LeafRule lr = new LeafRule((Label) label.clone(), p);
		return lr;
	}
}
