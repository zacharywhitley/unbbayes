/**
 * 
 */
package unbbayes.prs.bn.valueTree;

/**
 * A default implementation of {@link IValueTreeFactionChangeEvent}
 * @author Shou Matsumoto
 *
 */
public class ValueTreeFactionChangeEvent implements
		IValueTreeFactionChangeEvent {

	private IValueTreeNode node = null;
	private float factionBefore = Float.NaN;
	private float factionAfter = Float.NaN;

	/**
	 * Default constructor is kept invisible for public, but
	 * still visible from subclass in order to allow easy inheritance.
	 */
	protected ValueTreeFactionChangeEvent() {}
	

	/**
	 * Default constructor initializing fields.
	 * It is kept invisible for public, but visible from subclass
	 * to allow inheritance.
	 * @param node
	 * @param factionBefore
	 * @param factionAfter
	 */
	protected ValueTreeFactionChangeEvent(IValueTreeNode node,
			float factionBefore, float factionAfter) {
		this();
		this.node = node;
		this.factionBefore = factionBefore;
		this.factionAfter = factionAfter;
	}

	/**
	 * Default constructor method initializing fields
	 * @param node : node whose faction has changed
	 * @param factionBefore : faction before change
	 * @param factionAfter : faction after change
	 * @return : new instance of this class.
	 */
	public static IValueTreeFactionChangeEvent getInstance (IValueTreeNode node,
			float factionBefore, float factionAfter) {
		return new ValueTreeFactionChangeEvent(node, factionBefore, factionAfter);
	}


	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.valueTree.IValueTreeFactionChangeEvent#getNode()
	 */
	public IValueTreeNode getNode() {
		return this.node;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.valueTree.IValueTreeFactionChangeEvent#getFactionBefore()
	 */
	public float getFactionBefore() {
		return this.factionBefore;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.valueTree.IValueTreeFactionChangeEvent#getFactionAfter()
	 */
	public float getFactionAfter() {
		return this.factionAfter;
	}

}
