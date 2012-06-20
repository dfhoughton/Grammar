package dfh.grammar;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Handles scenario where one rule merely adds a condition to another. E.g.,
 * 
 * <pre>
 * rule = &lt;foo&gt; (condition)
 * </pre>
 * <p>
 * 
 * @author David F. Houghton - Jun 20, 2012
 * 
 */
@Reversible
public class ConditionalizedLabel extends Rule implements Serializable,
		NonterminalRule {
	private static final long serialVersionUID = 1L;
	protected final Rule r;
	protected Condition c;

	class ConditionalizedLabelMatcher extends NonterminalMatcher {
		protected final Matcher submatcher;

		protected ConditionalizedLabelMatcher(Integer offset,
				Map<Integer, CachedMatch>[] cache, Matcher master) {
			super(offset, cache, ConditionalizedLabel.this, master);
			submatcher = r.matcher(offset, cache, master);
		}

		@Override
		protected void fetchNext() {
			next = null;
			while (submatcher.mightHaveNext()) {
				Match n = submatcher.match();
				if (n == null) {
					done = true;
					break;
				}
				next = new Match(ConditionalizedLabel.this, n.start(), n.end());
				Match[] children = new Match[] { n };
				next.setChildren(children);
				if (testCondition(c, next))
					break;
			}
		}

	}

	public ConditionalizedLabel(Label label, Rule r) {
		super(label);
		this.r = r;
	}

	@Override
	public Matcher matcher(Integer offset, Map<Integer, CachedMatch>[] cache,
			Matcher master) {
		return new ConditionalizedLabelMatcher(offset, cache, master);
	}

	@Override
	protected String uniqueId() {
		StringBuilder b = new StringBuilder();
		b.append(r.uniqueId());
		b.append('(').append(condition).append(')');
		return b.toString();
	}

	@Override
	public String description(boolean inBrackets) {
		StringBuilder b = new StringBuilder();
		b.append(r.label());
		b = new StringBuilder(wrap(b));
		if (c.visible())
			b.append(" (").append(c.describe()).append(')');
		return b.toString();
	}

	@Override
	public Set<Integer> study(CharSequence s,
			Map<Integer, CachedMatch>[] cache, GlobalState options) {
		// non-terminal rules don't study
		return null;
	}

	@Override
	public boolean zeroWidth() {
		return r.zeroWidth();
	}

	@Override
	protected Boolean mayBeZeroWidth(Map<String, Boolean> cache) {
		return r.mayBeZeroWidth(cache);
	}

	@Override
	public Rule conditionalize(Condition c, String id) {
		this.c = c;
		this.condition = id;
		return this;
	}
}
