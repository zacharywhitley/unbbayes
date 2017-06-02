/**
 * 
 */
package unbbayes.draw;

import unbbayes.draw.extension.IPluginUShape;
import unbbayes.prs.Node;

/**
 * @author Shou Matsumoto
 *
 */
public class UShapeIdentityNode extends UShapeInputNode implements IPluginUShape {



	private static final long serialVersionUID = 6943713828045982062L;

	public UShapeIdentityNode(){
		this(null,null, 0,0,100,70);
	} 
	
	/**
	 * @param c
	 * @param pNode
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	public UShapeIdentityNode(UCanvas c, Node pNode, int x, int y, int w, int h) {
		super(c, pNode, x, y, w, h);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.draw.extension.IPluginUShape#getUShape(unbbayes.prs.Node, unbbayes.draw.UCanvas)
	 */
	public UShape getUShape(Node node, UCanvas canvas) {
		this.setNode(node);
		this.setCanvas(canvas);
		return this;
	}

}
