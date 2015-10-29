/**
 * 
 */
package unbbayes.io.medg;

import java.io.File;
import java.io.IOException;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.SaverPrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.owlapi.OWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.protege.Protege41CompatiblePROWL2IO;
import unbbayes.io.medg.owlapi.DefaultPROWL2DecisionIndividualsExtractor;
import unbbayes.io.medg.owlapi.IPROWL2DecisionModelUser;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.entity.ontology.owlapi.OWLAPIObjectEntityContainer;
import unbbayes.prs.medg.MultiEntityDecisionNode;
import unbbayes.prs.medg.MultiEntityUtilityNode;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class PROWL2DecisionIO extends Protege41CompatiblePROWL2IO implements IPROWL2DecisionModelUser {
	
	/** Default place to look for PR-OWL 2 decision scheme file */
	public static final String PROWL2_DECISION_MODEL_FILEPATH = "pr-owl/pr-owl2-decision.owl";
	
	private OWLOntologyIRIMapper prowl2DecisionDefinitionIRIMapper = null;

	/**
	 * Default constructor is not public, because we prefer constructor method {@link #getInstance()},
	 * but it is kept protected to allow easy extension.
	 * @deprecated use {@link #getInstance()} instead
	 */
	protected PROWL2DecisionIO() {}
	
	/**
	 * Default constructor method.
	 * @return a new instance of {@link PROWL2DecisionIO}
	 * @see DefaultPROWL2DecisionIndividualsExtractor
	 */
	public static MebnIO getInstance() {
		return new PROWL2DecisionIO();
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO#initialize()
	 */
	protected void initialize() {
		super.initialize();
		
		// this extractor will be able to look for OWL individuals and classes in PR-OWL 2 Decision definition file/scheme
		setPROWL2IndividualsExtractor(DefaultPROWL2DecisionIndividualsExtractor.newInstance());

		// initialize IRI mapper, so that requests for PR-OWL2 Decision IRIs are delegated to local files;
		try {
			// extract local file
			File prowl2DecisionDefinitionFile = null;	// this is going to be the file containing PR-OWL2 Decision definitions
			try {
				// load file assuming that it is a plug-in resource (in such case, we must tell plug-in classloaders to look for files).
				prowl2DecisionDefinitionFile = new File(this.getClass().getClassLoader().getResource(PROWL2_DECISION_MODEL_FILEPATH).toURI());
			} catch (Exception e1) {
				try {
					Debug.println(this.getClass(), e1.getMessage() + " - Could not load pr-owl2-decision.owl from " + PROWL2_DECISION_MODEL_FILEPATH + " in plug-in's resource folder. Retry using project's root folder...", e1);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				try {
					// retrying using file in project's root folder
					prowl2DecisionDefinitionFile = new File(PROWL2_DECISION_MODEL_FILEPATH);
					if (!prowl2DecisionDefinitionFile.exists()) {
						prowl2DecisionDefinitionFile = null;
					}
				} catch (Exception e) {
					try {
						Debug.println(this.getClass(), e.getMessage() + " - Could not load pr-owl2-decision.owl from " + PROWL2_DECISION_MODEL_FILEPATH + " in project's root folder.", e);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
			// check if local definition was found
			if (prowl2DecisionDefinitionFile != null) {
				// initialize default IRI mapper, so that it uses local file
				this.setPROWL2DecisionDefinitionIRIMapper(new SimpleIRIMapper(
							IRI.create(PROWL2_DECISION_URI), 					// the PR-OWL2 Decision IRI will be translated to...
							IRI.create(prowl2DecisionDefinitionFile)			// ...this IRI (local file)
						));
			} else {
				// no local file was found
				try {
					Debug.println(this.getClass(), "Could not initialize PR-OWL2 Decision IRI mapper using local file. Calls to PR-OWL2 Decision definitions will be requested to " + PROWL2_DECISION_URI);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				// instead of using null, use identity (i.e. IPROWL2ModelUser.PROWL2_NAMESPACEURI maps to itself)
				IRI prowl2DecisionIRI = IRI.create(PROWL2_DECISION_URI);
				this.setPROWL2DecisionDefinitionIRIMapper(new SimpleIRIMapper(prowl2DecisionIRI, prowl2DecisionIRI));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO#saveMebn(java.io.File, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	public void saveMebn(File file, MultiEntityBayesianNetwork mebn)throws IOException, IOMebnException {
		// TODO check that we can re-save existing ontology

		// ontology where MEBN will be stored
		OWLOntology ontology = null;
		// TODO avoid duplicate code regarding extraction/initialization of instance of OWLOntology...
		
		// extract storage implementor of MEBN 
		// (a reference to an object that loaded/saved the mebn last time - such object is likely to contain a reference to owl ontology)
		if (mebn.getStorageImplementor() != null  && mebn.getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator) {
			// reuse ontology 
			ontology = ((IOWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee();
		} 
		
		// create new ontology if we could not extract it from MEBN object
		if (ontology == null) {
			
			try{
				Debug.println(this.getClass(), "Could not extract OWL ontology from " + mebn.getStorageImplementor() + ". Some non-PR-OWL ontology data may be lost.");
			} catch (Throwable t) {
				t.printStackTrace();
			}

			// Get hold of an ontology manager
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			
			// create new empty ontology
			try {
				ontology = manager.createOntology(IRI.create(file));
			} catch (OWLOntologyCreationException e) {
				// perform an exception translation to match the method signature
				throw new IOMebnException(e);
			}

			// create a reference from MEBN to the new ontology
			mebn.setStorageImplementor(OWLAPIStorageImplementorDecorator.newInstance(ontology));
			
			// replace the manager of object entity to an instance which also manages OWL entities.
			mebn.setObjectEntityContainer(new OWLAPIObjectEntityContainer(mebn));
		}
		
		// force ontology to delegate requisitions by adding a mapper (by doing this, it will redirect PR-OWL2 Decision IRIs to a local file)
		ontology.getOWLOntologyManager().removeIRIMapper(this.getPROWL2DecisionDefinitionIRIMapper());	// just to avoid duplicate mapper
		ontology.getOWLOntologyManager().addIRIMapper(this.getPROWL2DecisionDefinitionIRIMapper());

		
		// Check that PR-OWL 2 Decision profile definition/scheme is imported.
		OWLImportsDeclaration importsPROWL2Decision = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLImportsDeclaration(IRI.create(PROWL2_DECISION_URI));
		if (!ontology.getOWLOntologyManager().getImportsClosure(ontology).contains(ontology.getOWLOntologyManager().getImportedOntology(importsPROWL2Decision))) {
			// add the import declaration to the ontology.
			ontology.getOWLOntologyManager().applyChange(new AddImport(ontology, importsPROWL2Decision));
		}
		
		// the rest is identical to superclass' method
		super.saveMebn(file, mebn);
		
		// now, we don't need the ontology to import PR-OWL 2 directly, 
		// because by importing PR-OWL 2 Decision, it should be automatically included/imported indirectly, by transitivity.
		IRI prowl2IRI = IRI.create(PROWL2_NAMESPACEURI);	// keep the pr-owl 2 definition IRI handy, because we'll use it now for comparison
		for (OWLImportsDeclaration importsDeclaration : ontology.getImportsDeclarations()) {
			if (importsDeclaration.getIRI().equals(prowl2IRI)) {
				// remove the direct import declaration from the ontology.
				ontology.getOWLOntologyManager().applyChange(new RemoveImport(ontology, importsDeclaration));
			}
		}
		
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO#saveMTheoryAndMFrags(unbbayes.prs.mebn.MultiEntityBayesianNetwork, org.semanticweb.owlapi.model.OWLOntology)
	 */
	protected void saveMTheoryAndMFrags(MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		// TODO check if we can convert PR-OWL Decision to PR-OWL 2 decision.
		
		
		// now that individuals of decision/utility nodes are already created, the rest is the same of superclass' method
		super.saveMTheoryAndMFrags(mebn, ontology);
		
		
		// set owl individuals of decision/utility nodes as instances of decision/utility node owl classes
		if (mebn != null && mebn.getDomainResidentNodes() != null) {

			// extract PR-OWL 2 Decision prefix. We'll use it to access OWL entities in the PR-OWL 2 Decision definition ontology/scheme
			PrefixManager prowlDecisionPrefixManager = this.getPROWLDecisionPrefixManager();
			
			// extract PR-OWL 2 prefix (using null as argument of this method should do it).
			PrefixManager prowl2PrefixManager = this.getOntologyPrefixManager(null);
					
			// get the prefix of this ontology (the ontology to be saved now)
			PrefixManager currentPrefixManager = this.getOntologyPrefixManager(ontology);
			
			// Extract in advance the OWL class which represents the decision nodes
			OWLClass decisionClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(
					DOMAINDECISION, prowlDecisionPrefixManager); 
			// Similarly, extract the OWL class which represents the utility nodes
			OWLClass utilityClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(
					DOMAINUTILITY, prowlDecisionPrefixManager); 
			// Similarly, extract the OWL class which represents probabilistic resident nodes
			OWLClass residentClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(
					DOMAINRESIDENT, prowl2PrefixManager); 
			
			// look for decision and utility nodes
			for (ResidentNode residentNode : mebn.getDomainResidentNodes()) {
				// get individual 
				OWLIndividual medgNodeIndividual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(
						SaverPrOwlIO.RESIDENT_NAME_PREFIX + residentNode.getName(), currentPrefixManager);
				// check if we are adding a decision node or utility node
				if (residentNode instanceof MultiEntityDecisionNode) {
					// this is a decision node, so we need to include individual to decision node class
					if (!medgNodeIndividual.getTypes(ontology).contains(decisionClass)) {
						// if new, add it to ontology and commit change
						ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(decisionClass, medgNodeIndividual)
							);
					}
				} else if (residentNode instanceof MultiEntityUtilityNode) {
					// this is a decision node, so we need to include individual to utility node class
					if (!medgNodeIndividual.getTypes(ontology).contains(utilityClass)) {
						// if new, add it to ontology and commit change
						ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(utilityClass, medgNodeIndividual)
							);
					}
					
				} else {
					// do not change probabilistic resident nodes
					continue;
				}
				
				// Note: if we reached this line, then current node is a MEDG node (either a decision or utility node)
				
				// make sure the individual's direct class is decision/utility owl classes, instead of resident node owl class.
				for (OWLClassAssertionAxiom axiom : ontology.getClassAssertionAxioms(medgNodeIndividual)) {
					if (axiom.getClassExpression().equals(residentClass)) {
						// remove direct assertions that indicates that this individual is a resident node 
						// (this is not supposed to remove assertions that indicates this individual is a decision/utility node).
						ontology.getOWLOntologyManager().removeAxiom(ontology,axiom);
					}
				}
			}
		}
	}
	
	/**
	 * @return a prefix manager for PR-OWL 2 Decision profile. This prefix manager represents the prefix
	 * of PR-OWL 2 Decision definition ontology/scheme
	 * @see IPROWL2DecisionModelUser#PROWL2_DECISION_DEFAULTPREFIXMANAGER
	 * @see #saveMTheoryAndMFrags(MultiEntityBayesianNetwork, OWLOntology)
	 */
	public PrefixManager getPROWLDecisionPrefixManager (){
//		try {
//			// extract the PR-OWL decision ontology namespaces with '#'
//			String defaultPrefix = this.getPROWLDecisionOntologyNamespaceURI();
//			if (defaultPrefix != null) {
//				if (!defaultPrefix.endsWith("#")) {
//					defaultPrefix += "#";
//				}
//				PrefixManager prefixManager = new DefaultPrefixManager(defaultPrefix);
//				return prefixManager;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		// use the PROWL2-decision default prefix manager
		return PROWL2_DECISION_DEFAULTPREFIXMANAGER;
	
	}

	/**
	 * @return the prowl2DecisionDefinitionIRIMapper
	 */
	public OWLOntologyIRIMapper getPROWL2DecisionDefinitionIRIMapper() {
		return prowl2DecisionDefinitionIRIMapper;
	}

	/**
	 * @param prowl2DecisionDefinitionIRIMapper the prowl2DecisionDefinitionIRIMapper to set
	 */
	public void setPROWL2DecisionDefinitionIRIMapper(
			OWLOntologyIRIMapper prowl2DecisionDefinitionIRIMapper) {
		this.prowl2DecisionDefinitionIRIMapper = prowl2DecisionDefinitionIRIMapper;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO#saveCategoricalStates(unbbayes.prs.mebn.MultiEntityBayesianNetwork, org.semanticweb.owlapi.model.OWLOntology)
	 */
	protected void saveCategoricalStates(MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		
		
		// update categorical state cache, so that the categorical state "utility" is handled in a special manner;
		try {
			// extract the utility category state
			CategoricalStateEntity utilityCategoryState = mebn.getCategoricalStatesEntityContainer().getCategoricalState(UTILITY_CATEGORY_STATE_NAME);
			if (utilityCategoryState != null) {
				OWLNamedIndividual utilityOWLIndividual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(utilityCategoryState.getName(), getPROWLDecisionPrefixManager());
				getCategoricalStatesCache().put(utilityCategoryState, utilityOWLIndividual);
				
				// also add iri mapping to mebn;
				IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, utilityCategoryState, utilityOWLIndividual.getIRI());
			}
		} catch (CategoricalStateDoesNotExistException e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		
		// the rest is the same of superclass' method
		super.saveCategoricalStates(mebn, ontology);
	}

//	/* (non-Javadoc)
//	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO#saveObjectEntitiesClasses(unbbayes.prs.mebn.MultiEntityBayesianNetwork, org.semanticweb.owlapi.model.OWLOntology)
//	 */
//	protected void saveObjectEntitiesClasses(MultiEntityBayesianNetwork mebn,OWLOntology ontology) {
//		update cache, so that decision nodes and utility nodes are not considered as entities
//		
//		also add iri mapping to mebn;
//		IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, key, value);
//		// TODO Auto-generated method stub
//		super.saveObjectEntitiesClasses(mebn, ontology);
//	}

	

//	/**
//	 * @return the prowlDecisionOntologyNamespaceURI
//	 */
//	public String getPROWLDecisionOntologyNamespaceURI() {
//		return prowlDecisionOntologyNamespaceURI;
//	}
//
//	/**
//	 * @param prowlDecisionOntologyNamespaceURI the prowlDecisionOntologyNamespaceURI to set
//	 */
//	public void setPROWLDecisionOntologyNamespaceURI(
//			String prowlDecisionOntologyNamespaceURI) {
//		this.prowlDecisionOntologyNamespaceURI = prowlDecisionOntologyNamespaceURI;
//	}

}
