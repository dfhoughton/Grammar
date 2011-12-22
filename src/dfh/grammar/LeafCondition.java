package dfh.grammar;

class LeafCondition extends Condition {
	private static final long serialVersionUID = 2L;
	protected String cnd;

	LeafCondition(String cnd) {
		this.cnd = cnd;
	}
}
