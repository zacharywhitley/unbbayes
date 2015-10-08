/**
 * 
 */
package unbbayes.draw.medg;

import unbbayes.draw.INodeHolderShape;
import unbbayes.draw.UCanvas;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeDecisionNode;
import unbbayes.draw.extension.IPluginUShape;
import unbbayes.prs.Node;
import unbbayes.prs.medg.MultiEntityDecisionNode;

/**
 * This is a shape class (for drawing shapes in the canvas) for {@link MultiEntityDecisionNode}
 * @author Shou Matsumoto
 *
 */
public class UShapeMultiEntityDecisionNode extends UShapeDecisionNode implements IPluginUShape, INodeHolderShape {
 
	private static final long serialVersionUID = 6044360894977294386L;

	public UShapeMultiEntityDecisionNode(){
		this(null,null, 0,0,100,70);
	} 

	public UShapeMultiEntityDecisionNode(UCanvas c, Node pNode, int x, int y, int w, int h){
		super(c, pNode, x, y, w, h);  
    }    
	
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.draw.extension.IPluginUShape#getUShape(unbbayes.prs.Node, unbbayes.draw.UCanvas)
	 */
	public UShape getUShape(Node node, UCanvas canvas){
		this.setNode(node);
		this.setCanvas(canvas);
		return this;
	}

}
