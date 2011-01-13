/**
 * 
 */
package unbbayes.prs.mebn;

import java.util.Properties;

import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.entity.Type;

/**
 * Classes implementing this interface can generate instances
 * of MEBN and its related nodes and MFrags.
 * This is useful when a class (e.g. IO class) must be able to instantiate subclasses
 * of MEBN elements (e.g. {@link MultiEntityBayesianNetwork}, {@link MFrag}, {@link ResidentNode}, etc.)
 * depending on what implementation of this factory is passed to such class (thus, less coupling is introduced).
 * @author Shou Matsumoto
 *
 */
public interface IMEBNFactory {

	public void setInstantiationProperties(Properties properties);
	
	public MultiEntityBayesianNetwork createMEBN(String name);
	
	public MFrag createMFrag(String name, MultiEntityBayesianNetwork mebn);
	
	public ResidentNode createResidentNode(String name, MFrag mfrag);
	
	public InputNode createInputNode(String name, MFrag mfrag);
	
	public ContextNode createContextNode(String name, MFrag mfrag);
	
	public OrdinaryVariable createOrdinaryVariable(String name, Type type, MFrag mfrag);
	
	public BuiltInRV createBuiltInRV(String operation);
	
	public Argument createArgument(String name, MultiEntityNode multiEntityNode);
	
	public Edge createEdge(INode from, INode to);
	
	public ResidentNodePointer createResidentNodePointer(ResidentNode residentNode, INode node);
	
	public NodeFormulaTree createNodeFormulaTree(String name, EnumType type, EnumSubType subType, Object nodeVariable);
}
