/**
 * 
 */
package unbbayes.prs.bn.valueTree;

/**
 * Event object holding information provided in {@link IValueTreeFactionChangeListener}
 * @author Shou Matsumoto
 */
public interface IValueTreeFactionChangeEvent {
	/**
	 * @return the node which {@link IValueTreeNode#getFaction()} has changed.
	 */
	public IValueTreeNode getNode();
	
	/**
	 * @return value of {@link IValueTreeNode#getFaction()} before change
	 */
	public float getFactionBefore();
	
	/**
	 * @return value of {@link IValueTreeNode#getFaction()} after change
	 */
	public float getFactionAfter();
}
