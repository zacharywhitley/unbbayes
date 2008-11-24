/**
 * 
 */
package unbbayes.io.oobn.builder;

import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.prs.Node;
import unbbayes.prs.oobn.IOOBNNode;

/**
 * @author Shou Matsumoto
 *
 */
public class DefaultPrivateOOBNNodeGraphicalWrapperBuilder extends
		DefaultOOBNNodeGraphicalWrapperBuilder {

	/**
	 * 
	 */
	protected DefaultPrivateOOBNNodeGraphicalWrapperBuilder() {
		// TODO Auto-generated constructor stub
	}
	
	public static DefaultPrivateOOBNNodeGraphicalWrapperBuilder newInstance() {
		return new DefaultPrivateOOBNNodeGraphicalWrapperBuilder();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.oobn.builder.DefaultOOBNNodeGraphicalWrapperBuilder#buildNode()
	 */
	@Override
	public Node buildNode() {
		// Does the same as the default builder, but sets the type as private node (the default expected by hugin)
		OOBNNodeGraphicalWrapper ret =  (OOBNNodeGraphicalWrapper)super.buildNode();
		ret.getWrappedNode().setType(IOOBNNode.TYPE_PRIVATE);
		return ret;
	}
	
	
	

}
