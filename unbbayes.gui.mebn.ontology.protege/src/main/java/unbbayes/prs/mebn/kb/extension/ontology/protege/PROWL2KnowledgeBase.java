/**
 * 
 */
package unbbayes.prs.mebn.kb.extension.ontology.protege;

import java.util.List;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.mebn.owlapi.DefaultNonPROWL2ClassExtractor;
import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.entity.ontology.owlapi.OWLReasonerInfo;
import unbbayes.prs.mebn.kb.KnowledgeBase;

/**
 * This class extends {@link OWL2KnowledgeBase} in order to fulfill
 * PR-OWL2 specific reasoning requirements.
 * @author Shou Matsumoto
 *
 */
public class PROWL2KnowledgeBase extends OWL2KnowledgeBase {

	/**
	 * @deprecated use {@link #getInstance(MultiEntityBayesianNetwork, IMEBNMediator)} instead
	 */
	public PROWL2KnowledgeBase() {
		super();
		// change prowl class extractor so that it uses PROWL2-specific routines
		this.setNonPROWLClassExtractor(DefaultNonPROWL2ClassExtractor.getInstance());
	}
	/**
	 * This is just a call to {@link #getInstance(null, MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		return getInstance(null, mebn, mediator);
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
		OWL2KnowledgeBase ret = new PROWL2KnowledgeBase();
		ret.setDefaultOWLReasoner(reasoner);
		ret.setDefaultMediator(mediator);
		ret.setDefaultMEBN(mebn);
		return ret;
	}
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getDefaultOWLReasoner()
	 */
	public OWLReasoner getDefaultOWLReasoner() {
		// TODO Auto-generated method stub
		return super.getDefaultOWLReasoner();
	}
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getOWLModelManager()
	 */
	public OWLModelManager getOWLModelManager() {
		// TODO Auto-generated method stub
		return super.getOWLModelManager();
	}
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getProwlModelUserDelegator()
	 */
	public IPROWL2ModelUser getProwlModelUserDelegator() {
		// TODO Auto-generated method stub
		return super.getProwlModelUserDelegator();
	}
	
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#setDefaultOWLReasoner(org.semanticweb.owlapi.reasoner.OWLReasoner)
	 */
	public void setDefaultOWLReasoner(OWLReasoner defaultOWLReasoner) {
		// TODO Auto-generated method stub
		super.setDefaultOWLReasoner(defaultOWLReasoner);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getAvailableOWLReasonersInfo()
	 */
	public List<OWLReasonerInfo> getAvailableOWLReasonersInfo() {
		// TODO Auto-generated method stub
		return super.getAvailableOWLReasonersInfo();
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#buildOWLReasoner(unbbayes.prs.mebn.entity.ontology.owlapi.OWLReasonerInfo)
	 */
	public OWLReasoner buildOWLReasoner(OWLReasonerInfo reasonerInfo) {
		// TODO Auto-generated method stub
		return super.buildOWLReasoner(reasonerInfo);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#supportsLocalFile(boolean)
	 */
	public boolean supportsLocalFile(boolean isLoad) {
		// TODO Auto-generated method stub
		return super.supportsLocalFile(isLoad);
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getSupportedLocalFileExtension(boolean)
	 */
	public String[] getSupportedLocalFileExtension(boolean isLoad) {
		// TODO Auto-generated method stub
		return super.getSupportedLocalFileExtension(isLoad);
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase#getSupportedLocalFileDescription(boolean)
	 */
	public String getSupportedLocalFileDescription(boolean isLoad) {
		// TODO Auto-generated method stub
		return super.getSupportedLocalFileDescription(isLoad);
	}
	
	
	
	
}
