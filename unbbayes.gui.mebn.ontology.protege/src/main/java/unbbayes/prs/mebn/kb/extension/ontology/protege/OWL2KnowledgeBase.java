/**
 * 
 */
package unbbayes.prs.mebn.kb.extension.ontology.protege;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.exception.UBIOException;
import unbbayes.io.mebn.owlapi.OWLAPIStorageImplementorDecorator;
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
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.util.Debug;

/**
 * This knowledge base delegates inference to {@link OWLReasoner}.
 * This class reuses {@link OWLReasoner} from {@link MultiEntityBayesianNetwork#getStorageImplementor()} if necessary (when no reasoner is provided).
 * The ability to specify a reasoner will make users able to call this Knowledge Base no matter what kind of {@link MultiEntityBayesianNetwork#getStorageImplementor()}
 * is provided. This aspect must be dealt with caution, because it means that changing a reasoner of {@link MultiEntityBayesianNetwork#getStorageImplementor()}
 * would not change the reasoner of this class.
 * @author Shou Matsumoto
 *
 */
public class OWL2KnowledgeBase implements KnowledgeBase {

	private OWLReasoner defaultOWLReasoner;
	
	private MultiEntityBayesianNetwork defaultMEBN;
	
	private IMEBNMediator defaultMediator;
	
	/**
	 * The default constructor is only visible in order to allow inheritance
	 * @deprecated use {@link #getInstance(OWLReasoner, MultiEntityBayesianNetwork, IMEBNMediator)} instead
	 */
	protected OWL2KnowledgeBase() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Constructor method initializing fields
	 * @param reasoner : explicitly sets the value of {@link #getDefaultOWLReasoner()}; If null, {@link #getDefaultOWLReasoner()} will be extracted from {@link MultiEntityBayesianNetwork#getStorageImplementor()}
	 * of {@link #getDefaultMEBN()}.
	 * @param mebn : this value will be set to {@link #setDefaultMEBN(MultiEntityBayesianNetwork)}
	 * @param mediator : this value will be set to {@link #setDefaultMediator(IMEBNMediator)}
	 * @return
	 */
	public static KnowledgeBase getInstance(OWLReasoner reasoner, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		OWL2KnowledgeBase ret = new OWL2KnowledgeBase();
		ret.setDefaultOWLReasoner(reasoner);
		ret.setDefaultMediator(mediator);
		ret.setDefaultMEBN(mebn);
		return ret;
	}
	
	/**
	 * This is just a call to {@link #getInstance(null, MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		return getInstance(null, mebn, mediator);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#clearKnowledgeBase()
	 */
	public void clearKnowledgeBase() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#clearFindings()
	 */
	public void clearFindings() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#createGenerativeKnowledgeBase(unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	public void createGenerativeKnowledgeBase(MultiEntityBayesianNetwork mebn) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#createEntityDefinition(unbbayes.prs.mebn.entity.ObjectEntity)
	 */
	public void createEntityDefinition(ObjectEntity entity) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#createRandomVariableDefinition(unbbayes.prs.mebn.ResidentNode)
	 */
	public void createRandomVariableDefinition(ResidentNode resident) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#insertEntityInstance(unbbayes.prs.mebn.entity.ObjectEntityInstance)
	 */
	public void insertEntityInstance(ObjectEntityInstance entityInstance) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#insertRandomVariableFinding(unbbayes.prs.mebn.RandomVariableFinding)
	 */
	public void insertRandomVariableFinding(
			RandomVariableFinding randomVariableFinding) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#saveGenerativeMTheory(unbbayes.prs.mebn.MultiEntityBayesianNetwork, java.io.File)
	 */
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#saveFindings(unbbayes.prs.mebn.MultiEntityBayesianNetwork, java.io.File)
	 */
	public void saveFindings(MultiEntityBayesianNetwork mebn, File file) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#loadModule(java.io.File, boolean)
	 */
	public void loadModule(File file, boolean findingModule)
			throws UBIOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public Boolean evaluateContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateSingleSearchContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public List<String> evaluateSingleSearchContextNodeFormula(
			ContextNode context, List<OVInstance> ovInstances)
			throws OVInstanceFaultException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateSearchContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public SearchResult evaluateSearchContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateMultipleSearchContextNodeFormula(java.util.List, java.util.List)
	 */
	public Map<OrdinaryVariable, List<String>> evaluateMultipleSearchContextNodeFormula(
			List<ContextNode> contextList, List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#existEntity(java.lang.String)
	 */
	public boolean existEntity(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#searchFinding(unbbayes.prs.mebn.ResidentNode, java.util.Collection)
	 */
	public StateLink searchFinding(ResidentNode randonVariable,
			Collection<OVInstance> listArguments) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#getEntityByType(java.lang.String)
	 */
	public List<String> getEntityByType(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#fillFindings(unbbayes.prs.mebn.ResidentNode)
	 */
	public void fillFindings(ResidentNode resident) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#supportsLocalFile(boolean)
	 */
	public boolean supportsLocalFile(boolean isLoad) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#getSupportedLocalFileExtension(boolean)
	 */
	public String[] getSupportedLocalFileExtension(boolean isLoad) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#getSupportedLocalFileDescription(boolean)
	 */
	public String getSupportedLocalFileDescription(boolean isLoad) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This is the reasoner to be used by this knowledge base in order to perform inference.
	 * If set to null, {@link #getDefaultOWLReasoner()} will begin returning an {@link OWLReasoner} extracted
	 * from {@link MultiEntityBayesianNetwork#getStorageImplementor()} of {@link #getDefaultMEBN()}.
	 * @return the defaultOWLReasoner
	 */
	public OWLReasoner getDefaultOWLReasoner() {
		if (defaultOWLReasoner == null) {
			OWLReasoner reasoner = null;
			try {
				if (this.getDefaultMEBN() != null
						&& this.getDefaultMEBN().getStorageImplementor() != null 
						&& this.getDefaultMEBN().getStorageImplementor() instanceof OWLAPIStorageImplementorDecorator) {
					reasoner = ((OWLAPIStorageImplementorDecorator)this.getDefaultMEBN().getStorageImplementor()).getOWLReasoner();
				}
			} catch (Throwable t) {
				// it is OK, because we can try extracting the reasoner when KB methods are called and MEBN is passed as arguments
				try {
					Debug.println(this.getClass(), "Could not extract reasoner from mebn " + this.getDefaultMEBN(), t);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			// if reasoner is not available, use the one extracted from MEBN
			Debug.println(this.getClass(), "Extracted reasoner from MEBN: " + reasoner);
			return reasoner;
		}
		return defaultOWLReasoner;
	}

	/**
	 * This is the reasoner to be used by this knowledge base in order to perform inference.
	 * If set to null, {@link #getDefaultOWLReasoner()} will begin returning an {@link OWLReasoner} extracted
	 * from {@link MultiEntityBayesianNetwork#getStorageImplementor()} of {@link #getDefaultMEBN()}.
	 * @param defaultOWLReasoner the defaultOWLReasoner to set
	 */
	public void setDefaultOWLReasoner(OWLReasoner defaultOWLReasoner) {
		this.defaultOWLReasoner = defaultOWLReasoner;
	}


	/**
	 * A {@link MultiEntityBayesianNetwork} to be used by this knowledge base if none was specified
	 * @return the defaultMEBN
	 */
	public MultiEntityBayesianNetwork getDefaultMEBN() {
		return defaultMEBN;
	}


	/**
	 * A {@link MultiEntityBayesianNetwork} to be used by this knowledge base if none was specified
	 * @param defaultMEBN the defaultMEBN to set
	 */
	public void setDefaultMEBN(MultiEntityBayesianNetwork defaultMEBN) {
		this.defaultMEBN = defaultMEBN;
	}


	/**
	 * A {@link IMEBNMediator} to be used by this knowledge base if GUI or IO commands needs to be accessed.
	 * @return the defaultMediator
	 */
	public IMEBNMediator getDefaultMediator() {
		return defaultMediator;
	}


	/**
	 * A {@link IMEBNMediator} to be used by this knowledge base if GUI or IO commands needs to be accessed.
	 * @param defaultMediator the defaultMediator to set
	 */
	public void setDefaultMediator(IMEBNMediator defaultMediator) {
		this.defaultMediator = defaultMediator;
	}

}
