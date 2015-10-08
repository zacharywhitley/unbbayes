/**
 * 
 */
package unbbayes.draw.medg;

import unbbayes.draw.INodeHolderShape;
import unbbayes.draw.UCanvas;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeUtilityNode;
import unbbayes.draw.extension.IPluginUShape;
import unbbayes.prs.Node;
import unbbayes.prs.medg.MultiEntityUtilityNode;

/**
 * This is a shape class (for drawing shapes in the canvas) for {@link MultiEntityUtilityNode}
 * @author Shou Matsumoto
 */
public class UShapeMultiEntityUtilityNode extends UShapeUtilityNode implements IPluginUShape, INodeHolderShape {
	 

		private static final long serialVersionUID = -7226633772207669905L;


		public UShapeMultiEntityUtilityNode(){
			this(null,null, 0,0,100,70);
		} 

		public UShapeMultiEntityUtilityNode(UCanvas c, Node pNode, int x, int y, int w, int h){
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
