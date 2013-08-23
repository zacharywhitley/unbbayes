/**
 * 
 */
package unbbayes.prs.bn.valueTree;

import java.util.List;

/**
 * Listener to be invoked when the
 * factions of elements in {@link IValueTree}
 * changes.
 * @author Shou Matsumoto
 */
public interface IValueTreeFactionChangeListener {
	/**
	 * This will be invoked by {@link IValueTree#changeProb(IValueTreeNode, IValueTreeNode, float, List)}
	 * @param changes : nodes which {@link IValueTreeNode#getFaction()} have changed, and values
	 * before and after the changes.
	 */
	public void onFactionChange(List<IValueTreeFactionChangeEvent> changes);
}
