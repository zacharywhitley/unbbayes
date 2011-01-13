/**
 * 
 */
package unbbayes.prs.mebn;

import java.util.Properties;

import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVExists;
import unbbayes.prs.mebn.builtInRV.BuiltInRVForAll;
import unbbayes.prs.mebn.builtInRV.BuiltInRVIff;
import unbbayes.prs.mebn.builtInRV.BuiltInRVImplies;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.builtInRV.BuiltInRVOr;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.entity.Type;

/**
 * This is a mebn factory instantiating default classes (using common
 * "new" constructors).
 * @author Shou Matsumoto
 *
 */
public abstract class AbstractMEBNFactory implements IMEBNFactory {

	private Properties properties;

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#setInstantiationProperties(java.util.Properties)
	 */
	public void setInstantiationProperties(Properties properties) {
		this.properties = properties;
	}
	
	/**
	 * @return the property set in {@link #setInstantiationProperties(Properties)}
	 */
	public Properties getInstantiationProperties() {
		return properties;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createMEBN(java.lang.String)
	 */
	public MultiEntityBayesianNetwork createMEBN(String name) {
		return new MultiEntityBayesianNetwork(name);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createMFrag(java.lang.String, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	public MFrag createMFrag(String name, MultiEntityBayesianNetwork mebn) {
		return new MFrag(name, mebn);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createResidentNode(java.lang.String, unbbayes.prs.mebn.MFrag)
	 */
	public ResidentNode createResidentNode(String name, MFrag mfrag) {
		return new ResidentNode(name, mfrag);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createInputNode(java.lang.String, unbbayes.prs.mebn.MFrag)
	 */
	public InputNode createInputNode(String name, MFrag mfrag) {
		return new InputNode(name, mfrag);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createContextNode(java.lang.String, unbbayes.prs.mebn.MFrag)
	 */
	public ContextNode createContextNode(String name, MFrag mfrag) {
		return new ContextNode(name, mfrag);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createOrdinaryVariable(java.lang.String, unbbayes.prs.mebn.entity.Type, unbbayes.prs.mebn.MFrag)
	 */
	public OrdinaryVariable createOrdinaryVariable(String name, Type type, MFrag mfrag) {
		return new OrdinaryVariable(name, type, mfrag);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createBuiltInRV(java.lang.String)
	 */
	public BuiltInRV createBuiltInRV(String operation) {
		// lets virtually perform a huge switch-case command... 
		// TODO find out a more smart way.
		BuiltInRV builtInRV = null;
		try {
			// we cannot handle nameless built-in RV
			if (operation == null || operation.trim().length() <= 0) {
				return null;
			}
			// the name is not empty
			if (operation.equalsIgnoreCase("and") || operation.endsWith("and")) {
				builtInRV = new BuiltInRVAnd(); 
			} else if (operation.equalsIgnoreCase("or") || operation.endsWith("or")) {
				builtInRV = new BuiltInRVOr(); 
			} else if (operation.equalsIgnoreCase("equalto") || operation.endsWith("equalto")) {
				builtInRV = new BuiltInRVEqualTo(); 
			} else if (operation.equalsIgnoreCase("exists") || operation.endsWith("exists")) {
				builtInRV = new BuiltInRVExists(); 
			} else if (operation.equalsIgnoreCase("forall") || operation.endsWith("forall")) {
				builtInRV = new BuiltInRVForAll(); 
			} else if (operation.equalsIgnoreCase("not") || operation.endsWith("not")) {
				builtInRV = new BuiltInRVNot(); 
			} else if (operation.equalsIgnoreCase("iff") || operation.endsWith("iff")) {
				builtInRV = new BuiltInRVIff(); 
			} else if (operation.equalsIgnoreCase("implies") || operation.endsWith("implies")) {
				builtInRV = new BuiltInRVImplies(); 
			} 
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return builtInRV;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createArgument(java.lang.String, unbbayes.prs.mebn.MultiEntityNode)
	 */
	public Argument createArgument(String name, MultiEntityNode multiEntityNode) {
		return new Argument(name, multiEntityNode);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createEdge(unbbayes.prs.INode, unbbayes.prs.INode)
	 */
	public Edge createEdge(INode from, INode to) {
		try {
			return new Edge((Node)from, (Node)to);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createResidentNodePointer(unbbayes.prs.mebn.ResidentNode, unbbayes.prs.INode)
	 */
	public ResidentNodePointer createResidentNodePointer(
			ResidentNode residentNode, INode node) {
		try {
			return new ResidentNodePointer(residentNode, (Node)node);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.IMEBNFactory#createNodeFormulaTree(java.lang.String, unbbayes.prs.mebn.context.EnumType, unbbayes.prs.mebn.context.EnumSubType, java.lang.Object)
	 */
	public NodeFormulaTree createNodeFormulaTree(String name, EnumType type, EnumSubType subType, Object nodeVariable) {
		try {
			return new NodeFormulaTree(name, type, subType, nodeVariable);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
