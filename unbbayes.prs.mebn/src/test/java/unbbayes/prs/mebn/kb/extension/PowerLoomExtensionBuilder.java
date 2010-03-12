/**
 * 
 */
package unbbayes.prs.mebn.kb.extension;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.io.exception.UBIOException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;

/**
 * This is a stub
 * @author Shou Matsumoto
 *
 */
public class PowerLoomExtensionBuilder implements IKnowledgeBaseBuilder {

private String name = "Simplified Power Loom";
	
	/**
	 * Default constructor.
	 */
	public PowerLoomExtensionBuilder() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.jpf.IKnowledgeBaseBuilder#buildKB()
	 */
	public KnowledgeBase buildKB() throws InstantiationException {
		return new PowerLoomKBSimpleExtension();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.jpf.IKnowledgeBaseBuilder#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.jpf.IKnowledgeBaseBuilder#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	protected class PowerLoomKBSimpleExtension extends PowerLoomKB {
		public PowerLoomKBSimpleExtension() {
			super(1000);
		}
	}
	
	protected class SimpleKB implements KnowledgeBase {
		public SimpleKB() {
			super();
		}

		public void clearFindings() {
			System.out.println("clearFindings");
		}

		public void clearKnowledgeBase() {
			System.out.println("clearKnowledgeBase");
		}

		public void createEntityDefinition(ObjectEntity entity) {
			System.out.println("createEntityDefinition " + entity);
		}

		public void createGenerativeKnowledgeBase(
				MultiEntityBayesianNetwork mebn) {
			System.out.println("createGenerativeKnowledgeBase " + mebn);
		}

		public void createRandomVariableDefinition(ResidentNode resident) {
			System.out.println("createRandomVariableDefinition " + resident);
		}

		public Boolean evaluateContextNodeFormula(ContextNode context,
				List<OVInstance> ovInstances) {
			System.out.println("evaluateContextNodeFormula " + context + ", " + ovInstances);
			return Boolean.TRUE;
		}

		public Map<OrdinaryVariable, List<String>> evaluateMultipleSearchContextNodeFormula(
				List<ContextNode> contextList, List<OVInstance> ovInstances) {
			System.out.println("evaluateMultipleSearchContextNodeFormula " + contextList + ", " + ovInstances);
			return new HashMap<OrdinaryVariable, List<String>>();
		}

		public SearchResult evaluateSearchContextNodeFormula(
				ContextNode context, List<OVInstance> ovInstances) {
			System.out.println("evaluateSearchContextNodeFormula " + context + ", " + ovInstances);
			return null;
		}

		public List<String> evaluateSingleSearchContextNodeFormula(
				ContextNode context, List<OVInstance> ovInstances)
				throws OVInstanceFaultException {
			System.out.println("evaluateSingleSearchContextNodeFormula " + context + ", " + ovInstances);
			return new ArrayList<String>();
		}

		public boolean existEntity(String name) {
			System.out.println("existEntity " + name );
			return true;
		}

		public void fillFindings(ResidentNode resident) {
			System.out.println("fillFindings " + resident );
			
		}

		public List<String> getEntityByType(String type) {
			List<String> ret = new ArrayList<String>();
			if (type.equalsIgnoreCase("SENSORREPORT_LABEL")) {
				 ret.add("R0");
			} else if (type.equalsIgnoreCase("STARSHIP_LABEL")) {
				 ret.add("ST0");
			} else if (type.equalsIgnoreCase("ZONE_LABEL")) {
				 ret.add("Z0");
			} else if (type.equalsIgnoreCase("TIMESTEP_LABEL")) {
				 ret.add("T0");
			}
			System.out.println("getEntityByType " + type + ": " + ret   );
			
			return ret;
		}

		public String getSupportedLocalFileDescription(boolean isLoad) {
			return "KB stub text file";
		}

		public String[] getSupportedLocalFileExtension(boolean isLoad) {
			String [] ret = {"txt"};
			return ret;
		}

		public void insertEntityInstance(ObjectEntityInstance entityInstance) {
			System.out.println("insertEntityInstance " + entityInstance   );
			
		}

		public void insertRandomVariableFinding(
				RandomVariableFinding randomVariableFinding) {
			System.out.println("insertRandomVariableFinding " + randomVariableFinding   );
			
		}

		public void loadModule(File file, boolean findingModule)
				throws UBIOException {
			System.out.println("loadModule " + file   );
			
		}

		public void saveFindings(MultiEntityBayesianNetwork mebn, File file) {
			System.out.println("saveFindings " + mebn + ", " +   file );
			
		}

		public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn,
				File file) {
			System.out.println("saveGenerativeMTheory " + mebn + ", " +   file );
			
		}

		public StateLink searchFinding(ResidentNode randonVariable,
				Collection<OVInstance> listArguments) {
			try {
				double magic = Math.random();
				if (magic > 0.4) {
					// 60% of chance not to be a finding
					return null;
				}
				StateLink state = randonVariable.getPossibleValueLinkList().get(0);
				System.out.println("searchFinding " + randonVariable + ", " +   listArguments + ": " + state );
				return state;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public boolean supportsLocalFile(boolean isLoad) {
			System.out.println("supportsLocalFile true");
			
			return true;
		}
	}
}
