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
	 */
	protected ValueTreeFactionChangeEvent(IValueTreeNode node,
			float factionBefore ) {
		this();
		this.node = node;
		this.factionBefore = factionBefore;
	}

	/**
	 * Default constructor method initializing fields
	 * @param node : node whose faction has changed
	 * @param factionBefore : faction before change
	 * @return : new instance of this class.
	 */
	public static IValueTreeFactionChangeEvent getInstance (IValueTreeNode node,
			float factionBefore ) {
		return new ValueTreeFactionChangeEvent(node, factionBefore);
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


}
