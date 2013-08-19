/**
 * 
 */
package unbbayes.prs.bn.valueTree.plugin;

import unbbayes.draw.UCanvas;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeProbabilisticNode;
import unbbayes.draw.extension.IPluginUShape;
import unbbayes.draw.extension.IPluginUShapeBuilder;
import unbbayes.prs.Node;

/**
 * @author Shou Matsumoto
 *
 */
public class ValueTreeNodeUShapeBuilder implements IPluginUShapeBuilder {

	/**
	 * Default constructor is made public to allow instantiation by plugin mechanism.
	 */
	public ValueTreeNodeUShapeBuilder() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.draw.extension.IPluginUShapeBuilder#build()
	 */
	public IPluginUShape build() throws IllegalAccessException,
			InstantiationException {
		return new ValueTreeNodeUShape();
	}
	
	public class ValueTreeNodeUShape implements IPluginUShape {
		private UShape delegator;
		/*
		 * (non-Javadoc)
		 * @see unbbayes.draw.extension.IPluginUShape#getUShape(unbbayes.prs.Node, unbbayes.draw.UCanvas)
		 */
		public UShape getUShape(Node node, UCanvas canvas) {
			setDelegator(new UShapeProbabilisticNode(canvas, node, (int)node.getPosition().getX(), (int)node.getPosition().getY(), node.getWidth(), node.getHeight()));
			return getDelegator();
		}
		/*
		 * (non-Javadoc)
		 * @see unbbayes.draw.INodeHolderShape#getNode()
		 */
		public Node getNode() {
			return getDelegator().getNode();
		}
		/**
		 * @return the delegator
		 */
		public UShape getDelegator() {
			return delegator;
		}
		/**
		 * @param delegator the delegator to set
		 */
		public void setDelegator(UShape delegator) {
			this.delegator = delegator;
		}
	}

}
