/**
 * 
 */
package unbbayes.draw.extension.impl;

import unbbayes.draw.UCanvas;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeDecisionNode;
import unbbayes.draw.extension.IPluginUShape;
import unbbayes.prs.Node;

/**
 * This is a sample of plugin ushape.
 * @author Shou Matsumoto
 *
 */
public class DefaultPluginUShape extends UShapeDecisionNode implements
		IPluginUShape {

	/**
	 * This is a sample of plugin ushape.
	 */
	public DefaultPluginUShape() {
		this(null, null, 0,0,0,0);
	}
	
	/**
	 * This is a sample of plugin ushape.
	 * @param c
	 * @param node
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public DefaultPluginUShape(UCanvas c, Node node, int x, int y, int w, int h) {
		super(c, node, x, y, w, h);
	}

	/* (non-Javadoc)
	 * @see unbbayes.draw.extension.IPluginUShape#getUShape(unbbayes.prs.Node)
	 */
	public UShape getUShape(Node node) {
		this.setNode(node);
		this.setLocation(
				(int) node.getPosition().x - node.getWidth() / 2, 
				(int) node.getPosition().y - node.getHeight() / 2
			);
		this.setSize(node.getWidth(), node.getHeight());
		this.setName(node.getName());
		this.updateUI();
		return this;
	}

}
