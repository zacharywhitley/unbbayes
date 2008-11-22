/**
 * 
 */
package unbbayes.io.oobn.builder;


import unbbayes.io.builder.impl.DefaultProbabilisticNetworkBuilder;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.oobn.impl.DefaultOOBNClass;


/**
 * Builder used by OOBN module in order to let NetIO load .net files
 * using DefaultOOBNClass as ProbabilisticNetwork and OOBNNodeGraphicalWrapper
 * as ProbabilisticNode.
 * @author Shou Matsumoto
 * @see unbbayes.prs.oobn.impl.DefaultOOBNClass
 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper
 */
public class DefaultOOBNClassBuilder extends DefaultProbabilisticNetworkBuilder {

	/**
	 * Builder used by OOBN module in order to let NetIO load .net files
	 * using DefaultOOBNClass as ProbabilisticNetwork and OOBNNodeGraphicalWrapper
	 * as ProbabilisticNode.
	 * @see unbbayes.prs.oobn.impl.DefaultOOBNClass
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper
	 */
	protected DefaultOOBNClassBuilder() {
		super();
		this.setProbabilisticNodeBuilder(DefaultOOBNNodeGraphicalWrapperBuilder.newInstance());
	}
	
	/**
	 * Builder used by OOBN module in order to let NetIO load .net files
	 * using DefaultOOBNClass as ProbabilisticNetwork and OOBNNodeGraphicalWrapper
	 * as ProbabilisticNode.
	 * @see unbbayes.prs.oobn.impl.DefaultOOBNClass
	 * @see unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper
	 */
	public static DefaultOOBNClassBuilder newInstance() {
		return new DefaultOOBNClassBuilder();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.builder.impl.DefaultProbabilisticNetworkBuilder#buildNetwork(java.lang.String)
	 */
	@Override
	public DefaultOOBNClass buildNetwork(String name) {
		return DefaultOOBNClass.newInstance(name);
	}
	
	
	

}
