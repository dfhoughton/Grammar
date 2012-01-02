package dfh.grammar;

import java.io.Serializable;

/**
 * Place holder for undefined condition.
 * 
 * @author David F. Houghton
 */
class LeafCondition extends Condition implements Serializable {
	private static final long serialVersionUID = 2L;
	protected String cnd;

	LeafCondition(String cnd) {
		this.cnd = cnd;
	}
}
