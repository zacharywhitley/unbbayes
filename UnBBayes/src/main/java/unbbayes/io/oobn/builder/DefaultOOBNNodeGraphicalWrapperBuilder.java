/**
 * 
 */
package unbbayes.io.oobn.builder;

import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.io.builder.impl.DefaultProbabilisticNodeBuilder;
import unbbayes.prs.Node;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.prs.oobn.impl.DefaultOOBNNode;

/**
 * 
 * Builder used by OOBN module in order to let NetIO use
 * OOBNNodeGraphicalWrapper as ProbabilisticNode
 * @author Shou Matsumoto
 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper
 */
public class DefaultOOBNNodeGraphicalWrapperBuilder extends
		DefaultProbabilisticNodeBuilder implements IOOBNInstanceNodeBuilder {

	/**
	 * 
	 * Builder used by OOBN module in order to let NetIO use
	 * OOBNNodeGraphicalWrapper as ProbabilisticNode
	 * @author Shou Matsumoto
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper
	 */
	protected DefaultOOBNNodeGraphicalWrapperBuilder() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 * Builder used by OOBN module in order to let NetIO use
	 * OOBNNodeGraphicalWrapper as ProbabilisticNode
	 * @author Shou Matsumoto
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper
	 */
	public static DefaultOOBNNodeGraphicalWrapperBuilder newInstance() {
		return new DefaultOOBNNodeGraphicalWrapperBuilder();
	}


	/* (non-Javadoc)
	 * @see unbbayes.io.builder.impl.DefaultProbabilisticNodeBuilder#buildNode()
	 */
	@Override
	public Node buildNode() {
		return OOBNNodeGraphicalWrapper.newInstance(DefaultOOBNNode.newInstance());
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.oobn.builder.IOOBNInstanceNodeBuilder#buildInstanceNode(unbbayes.prs.oobn.IOOBNClass)
	 */
	public OOBNNodeGraphicalWrapper buildInstanceNode(IOOBNClass oobnClass) {
		// new oobn node being added
		DefaultOOBNNode wrappedNode = DefaultOOBNNode.newInstance();
		
		// set parameters of the wrapped nodes
		wrappedNode.setParentClass(oobnClass);
		wrappedNode.setType(IOOBNNode.TYPE_INSTANCE);
		
		// Graphical wrapper: Graphical representation of wrappedNode
		return OOBNNodeGraphicalWrapper.newInstance(wrappedNode);
	}
	
	
	

}
