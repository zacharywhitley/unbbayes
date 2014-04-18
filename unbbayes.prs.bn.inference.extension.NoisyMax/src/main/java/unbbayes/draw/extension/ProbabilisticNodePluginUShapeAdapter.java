package unbbayes.draw.extension;
import unbbayes.draw.UCanvas;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeProbabilisticNode;
import unbbayes.prs.Node;

/**
 * This is just an adapter of {@link UShapeProbabilisticNode} to {@link IPluginUShape}
 * @author Shou Matsumoto
 */
public class ProbabilisticNodePluginUShapeAdapter implements IPluginUShape {
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