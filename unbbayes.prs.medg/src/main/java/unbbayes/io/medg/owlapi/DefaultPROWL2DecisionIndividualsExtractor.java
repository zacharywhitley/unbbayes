/**
 * 
 */
package unbbayes.io.medg.owlapi;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;

import unbbayes.io.mebn.owlapi.DefaultPROWL2IndividualsExtractor;
import unbbayes.io.mebn.owlapi.DefaultPROWL2ModelUser;
import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;

/**
 * This class extends {@link DefaultPROWL2IndividualsExtractor}
 * in order to support extraction of individuals of PR-OWL 2 decision profile.
 * @author Shou Matsumoto
 *
 */
public class DefaultPROWL2DecisionIndividualsExtractor extends DefaultPROWL2IndividualsExtractor implements IPROWL2DecisionModelUser {

	private IPROWL2ModelUser prowl2ModelUserDelegator = DefaultPROWL2ModelUser.getInstance();
	
	/**
	 * @deprecated use {@link #getInstance()} instead.
	 */
	protected DefaultPROWL2DecisionIndividualsExtractor() {}
	
	/**
	 * Default constructor method.
	 * @return new instance of {@link DefaultPROWL2DecisionIndividualsExtractor}
	 */
	public static DefaultPROWL2DecisionIndividualsExtractor newInstance() {
		return new DefaultPROWL2DecisionIndividualsExtractor();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.IPROWL2ModelUser#getOntologyPrefixManager(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public PrefixManager getOntologyPrefixManager(OWLOntology ontology) {
		// if ontology was not specified, then return the prefix of PR-OWL 2 Decision definition/scheme.
		if (ontology == null) {
			return PROWL2_DECISION_DEFAULTPREFIXMANAGER;
		}
		return getPROWL2ModelUserDelegator().getOntologyPrefixManager(ontology);
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.IPROWL2ModelUser#extractName(org.semanticweb.owlapi.model.OWLObject)
	 */
	public String extractName(OWLObject owlObject) {
		return getPROWL2ModelUserDelegator().extractName(owlObject);
	}


	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.DefaultPROWL2IndividualsExtractor#isIndividualInPROWL2Definition(org.semanticweb.owlapi.model.OWLIndividual)
	 */
	public boolean isIndividualInPROWL2Definition(OWLIndividual owlIndividual) {
		// simply check if namespace contains PR-OWL 2 Decision URI
		return owlIndividual.asOWLNamedIndividual().getIRI().toString().startsWith(PROWL2_DECISION_URI)
				|| super.isIndividualInPROWL2Definition(owlIndividual);
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.DefaultPROWL2IndividualsExtractor#isPROWLClass(org.semanticweb.owlapi.model.OWLClass)
	 */
	public boolean isPROWLClass(OWLClass owlClass) {
		// simply check if namespace contains PR-OWL 2 Decision URI
		return owlClass.getIRI().toString().startsWith(PROWL2_DECISION_URI)
				|| super.isPROWLClass(owlClass);
	}

	/**
	 * @return the prowl2ModelUserDelegator: requests for methods in {@link IPROWL2ModelUser} will be delegated to this instance.
	 */
	public IPROWL2ModelUser getPROWL2ModelUserDelegator() {
		return prowl2ModelUserDelegator;
	}

	/**
	 * @param prowl2ModelUserDelegator the prowl2ModelUserDelegator to set : requests for methods in {@link IPROWL2ModelUser} will be delegated to this instance.
	 */
	public void setPROWL2ModelUserDelegator(IPROWL2ModelUser prowl2ModelUserDelegator) {
		this.prowl2ModelUserDelegator = prowl2ModelUserDelegator;
	}

	
}
