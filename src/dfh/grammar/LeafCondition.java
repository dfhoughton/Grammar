package dfh.grammar;

class LeafCondition extends Condition {
	private static final long serialVersionUID = 1L;
	protected String cnd;

	LeafCondition(String cnd) {
		this.cnd = cnd;
	}
}
