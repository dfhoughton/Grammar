package dfh.grammar;

import java.util.regex.Pattern;

public class Regex implements RuleFragment {
	private final Pattern re;
	public Regex(String re) {
		this.re = Pattern.compile(re);
	}
}
