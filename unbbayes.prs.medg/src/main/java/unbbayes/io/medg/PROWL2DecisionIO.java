/**
 * 
 */
package unbbayes.io.medg;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.RemoveImport;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.PROWLModelUser;
import unbbayes.io.mebn.SaverPrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;
import unbbayes.io.mebn.owlapi.OWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.protege.Protege41CompatiblePROWL2IO;
import unbbayes.io.medg.owlapi.DefaultPROWL2DecisionIndividualsExtractor;
import unbbayes.io.medg.owlapi.IPROWL2DecisionModelUser;
import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IMEBNElementFactory;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.entity.ontology.owlapi.OWLAPIObjectEntityContainer;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.prs.mebn.extension.IMEBNPluginNode;
import unbbayes.prs.medg.IMEDGElementFactory;
import unbbayes.prs.medg.MultiEntityDecisionNode;
import unbbayes.prs.medg.MultiEntityUtilityNode;
import unbbayes.prs.medg.PROWL2MEDGFactory;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class PROWL2DecisionIO extends Protege41CompatiblePROWL2IO implements IPROWL2DecisionModelUser {
	
	/** Default place to look for PR-OWL 2 decision scheme file */
	public static final String PROWL2_DECISION_MODEL_FILEPATH = "pr-owl/pr-owl-decision.owl";
	
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
	
	/**
	 * @return an instance of {@link IMEDGElementFactory} to be used in order to instantiate 
	 * {@link MultiEntityBayesianNetwork}, {@link MFrag}, {@link MultiEntityDecisionNode},
	 * {@link MultiEntityUtilityNode}, {@link ResidentNode}, etc.
	 * @see #getMEBNFactory()
	 */
	public IMEDGElementFactory getMEDGFactory() {
		IMEBNElementFactory factory = getMEBNFactory();
		if (factory instanceof IMEDGElementFactory) {
			return (IMEDGElementFactory) factory;
		}
		return null;
	}
	
	/**
	 * This method simply calls {@link #setMEBNFactory(IMEBNElementFactory)}
	 * @param factory
	 */
	public void setMEDGFactory (IMEDGElementFactory factory) {
		this.setMEBNFactory(factory);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO#initialize()
	 */
	protected void initialize() {
		super.initialize();
		
		// force instances of nodes/MEBN/mFrags to be instantiated by this factory
		this.setMEBNFactory(PROWL2MEDGFactory.getInstance());
		
		// this extractor will be able to look for OWL individuals and classes in PR-OWL 2 Decision definition file/scheme
		setPROWL2IndividualsExtractor(DefaultPROWL2DecisionIndividualsExtractor.newInstance());
		
		// add PR-OWL 2 Decision profile's URI to the collection of PR-OWL schemes, so that classes in PR-OWL 2 Decision scheme are not considered as object entities
		// substitute the old collection instead of calling Collection#add(Object), because it may not be a mutable collection
		// now, there is at least 2 URIs to be considered as part of PR-OWL scheme: the old (PR-OWL 2) one, and the new (Decision) one
		Collection<String> prowlOntologyNamespaceURIs = new HashSet<String>(getNonPROWLClassExtractor().getPROWLOntologyNamespaceURIs());	// don't forget to keep the old (PR-OWL 2) URIs as well
		prowlOntologyNamespaceURIs.add(PROWL2_DECISION_URI);	
		prowlOntologyNamespaceURIs.add(PROWL_DECISION_URI);		
		getNonPROWLClassExtractor().setPROWLOntologyNamespaceURIs(prowlOntologyNamespaceURIs);

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
	
	/**
	 * This method overwrites method in superclass in order to avoid auto-inserting absurd state to decision or utility nodes
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO#loadDomainResidentNode(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, ResidentNode> loadDomainResidentNode(OWLOntology ontology, MultiEntityBayesianNetwork mebn)  throws IOMebnException{
	
		// TODO this method contains copy-pasted code from superclass. Avoid it
		
		Map<String, ResidentNode> ret = new HashMap<String, ResidentNode>();
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		OWLClass owlClassDomainResidentNode = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAINRESIDENT, prefixManager); 
		if (!ontology.containsClassInSignature(owlClassDomainResidentNode.getIRI(), true)) {
			// use old definition
			owlClassDomainResidentNode = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.DOMAIN_RESIDENT, prefixManager); 
		}
		
		if (owlClassDomainResidentNode != null) {
			for (OWLIndividual residentNodeIndividual :  this.getOWLIndividuals(owlClassDomainResidentNode, ontology)){
				// ignore anonymous
				if (!residentNodeIndividual.isNamed()) {
					try {
						System.err.println(residentNodeIndividual + " is anonymous and it is going to be ignored.");
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;
				}
				// extract node (we assume the nodes were loaded at loadDomainMFragContent)
				INode mappedNode = this.getMapLoadedNodes().get(this.extractName(ontology, residentNodeIndividual.asOWLNamedIndividual()));
				if (mappedNode == null || !(mappedNode instanceof ResidentNode)){
					try {
						Debug.println(this.getClass(), this.getResource().getString("DomainResidentNotExistsInMTheory") + ". Resident = " + residentNodeIndividual); 
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;
				}
				ResidentNode domainResidentNode = (ResidentNode)mappedNode;
				
				Debug.println(this.getClass(), "Domain Resident loaded: " + residentNodeIndividual); 			
				
				domainResidentNode.setDescription(getDescription(ontology, residentNodeIndividual)); 
				
				/* -> isResidentNodeIn  */
				
				OWLObjectProperty isResidentNodeIn = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isResidentNodeIn", prefixManager); 			
				Collection<OWLIndividual> instances = this.getObjectPropertyValues(residentNodeIndividual,isResidentNodeIn, ontology); 	
				MFrag mFragOfNode = null;	// extract the first MFrag
				if (instances.size() > 0) {
					// use only the 1st value
					Iterator<OWLIndividual> itAux = instances.iterator();
					OWLIndividual individualTwo = itAux.next();
					MFrag domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
					if(domainMFrag.containsDomainResidentNode(domainResidentNode) == false){
						throw new IOMebnException(this.getResource().getString("DomainResidentNotExistsInDomainMFrag") ); 
					}
					mFragOfNode = domainMFrag; 
					Debug.println(this.getClass(), "-> " + residentNodeIndividual + ": " + isResidentNodeIn + " = " + individualTwo);			
				}
				
				/* -> hasMExpression -> hasArgument */
				OWLObjectProperty hasMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasMExpressionPropertyName(), prefixManager); 
				
				// there should be only 1 MExpression per node. Extract it
				Collection<OWLIndividual> mExpressions =  this.getObjectPropertyValues(residentNodeIndividual,hasMExpression, ontology);
				if (mExpressions.size() > 0 ) {
					OWLIndividual mExpressionIndividual = mExpressions.iterator().next();
					// Node -> hasMExpression exactly 1 MExpression
					// extract arguments from mExpression using hasArgument property
					OWLObjectProperty hasArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasArgumentPropertyName(), prefixManager);
					for (OWLIndividual argumentIndividual : this.getObjectPropertyValues(mExpressionIndividual, hasArgument, ontology)) {
						Argument argument = this.getMEBNFactory().createArgument(this.extractName(ontology, argumentIndividual.asOWLNamedIndividual()), domainResidentNode); 
						domainResidentNode.addArgument(argument); 
						this.getMapArgument().put(this.extractName(ontology, argumentIndividual.asOWLNamedIndividual()), argument); 
						Debug.println(this.getClass(), "-> " + residentNodeIndividual + "-> ... -> " + hasArgument + " = " + argumentIndividual); 
						
						try {
							IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, argument, hasArgument.getIRI());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					/* hasMExpression -> typeOfMExpression-> hasPossibleValues */
					// extract the typeOfMExpression property
					OWLObjectProperty typeOfMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("typeOfMExpression", prefixManager); 
					
					// iterate over random variables related to MExpression by typeOfMExpression
					for (OWLIndividual randomVariableIndividual: this.getObjectPropertyValues(mExpressionIndividual,typeOfMExpression, ontology)){
						// ignore anonymous
						if (!randomVariableIndividual.isNamed()) {
							try {
								Debug.println(this.getClass(), randomVariableIndividual + " is anonymous and is going to be ignored.");
							} catch (Throwable t) {
								t.printStackTrace();
							}
							continue;
						}
						
						// add randomVariableIndividual to cache
						if (!this.getRandomVariableIndividualToResidentNodeSetCache().containsKey(randomVariableIndividual)) {
							// initialize entry
							this.getRandomVariableIndividualToResidentNodeSetCache().put(randomVariableIndividual, new HashSet<ResidentNode>());
						}
						this.getRandomVariableIndividualToResidentNodeSetCache().get(randomVariableIndividual).add(domainResidentNode);
						
						// extract BOOLEANRANDOMVARIABLE so that we can test if this is boolean
						// This code was added because reasoners like Hermit could not retrieve OWL literals from individuals by solving class axioms
						// (it requires that an individual to be explicitly linked to such OWL literal, instead of solving data property axioms in its types)
						OWLClass booleanRandomVariable = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.BOOLEANRANDOMVARIABLE, prefixManager);

						// check if this is boolean variable
						Boolean isBooleanRandomVariable = null;
						if (this.getLastOWLReasoner() != null) {
							// use reasoner to decide whether this is a boolean variable
							isBooleanRandomVariable = this.getLastOWLReasoner().getTypes(randomVariableIndividual.asOWLNamedIndividual(), false).containsEntity(booleanRandomVariable);
						} else {
							// only use explicit info to see if this is boolean variable
							isBooleanRandomVariable = randomVariableIndividual.getTypes(ontology.getOWLOntologyManager().getOntologies()).contains(booleanRandomVariable);
						}
						// special case: if BOOLEANRANDOMVARIABLE, then force boolean values for node
						if (isBooleanRandomVariable ) {
							try {
								Debug.println(this.getClass(), randomVariableIndividual + " is " + IPROWL2ModelUser.BOOLEANRANDOMVARIABLE + " and will be forced as boolean RV.");
							} catch (Throwable t) {
								t.printStackTrace();
							}
							// try boolean datatype
							domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());   
							domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());  
							
							// force node to have Absurd, if it is neither a decision nor utility node
							domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());
							
							// TODO stop using type of states because this is mutable
							domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
							// do not solve the possible values of this random variable anymore, because we forced it to be boolean
							continue;
						} 
						
						// extract hasPossibleValues data property
						OWLDataProperty hasPossibleValues = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HASPOSSIBLEVALUES, prefixManager); 			
						
						for (OWLLiteral symbolicLink: this.getDataPropertyValues(randomVariableIndividual,hasPossibleValues, ontology)){
							try {
								// the symbolicLink is a text representing URI (usually, a boolean, number or an owl class)
								IRI linkIRI = IRI.create(this.extractName(symbolicLink)); 
								
								// extract the element actually pointed (it is either a owl class or an owl literal)
								if (ontology.containsClassInSignature(linkIRI, true)) {
									// the possible values are individuals (classes)
									OWLClass pointedObject = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(linkIRI);
									/* case 2:object entities */
									Entity state = this.getMapLabelToObjectEntity().get(this.extractName(pointedObject));
									if (state == null) {
										// maybe getMapLabelToObjectEntity is still using "_label" as suffix (like old PR-OWL I/O)
										state = this.getMapLabelToObjectEntity().get(this.extractName(pointedObject) + TYPE_LABEL_SUFFIX);
									}
									if (state != null) {
										domainResidentNode.addPossibleValueLink(state); 
										// TODO stop using type of states because this is mutable
										domainResidentNode.setTypeOfStates(ResidentNode.OBJECT_ENTITY);
										try {
											Debug.println(this.getClass(), "Added possible class " + pointedObject);
										} catch (Exception e) {
											e.printStackTrace();
										}
									} else {
										System.err.println("Entity " + pointedObject + " was not found");
									}
								} else if (ontology.containsDatatypeInSignature(linkIRI, true)) {
									// the possible values are literals (datatypes)
									OWLDatatype pointedObject = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDatatype(linkIRI);
									if (pointedObject.isBoolean()) {
										// try boolean datatype
										domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());   
										domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());  
										domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());
										// TODO stop using type of states because this is mutable
										domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
									} else  {
										// non-boolean datatypes are not supported yet
										System.err.println("Non-boolean datatypes are not supported yet: " + pointedObject);
										// force node to have Absurd
										if (!domainResidentNode.hasPossibleValue(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity())) {
											domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());
										}
									}
									// TODO add support for continuous/numeric nodes
									try {
										Debug.println(this.getClass(), "Added possible datatype " + pointedObject);
									} catch (Exception e) {
										e.printStackTrace();
									}
								} else if (ontology.containsIndividualInSignature(linkIRI, true)) {
									/* the possible values are mere individuals (this may be representing categorical states) */
									OWLNamedIndividual pointedObject = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(linkIRI);
									CategoricalStateEntity state = mebn.getCategoricalStatesEntityContainer().getCategoricalState(this.extractName(ontology, pointedObject)) ;
									// force node to have Absurd
									if (!(domainResidentNode instanceof MultiEntityDecisionNode)
											&& !(domainResidentNode instanceof MultiEntityUtilityNode)) {
										if (!domainResidentNode.hasPossibleValue(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity())) {
											domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());
										}
									}
									if (state != null) {
										domainResidentNode.addPossibleValueLink(state); 
										domainResidentNode.setTypeOfStates(ResidentNode.CATEGORY_RV_STATES);
										try {
											Debug.println(this.getClass(), "Added possible individual " + pointedObject);
										} catch (Exception e) {
											e.printStackTrace();
										}
										
									} else {
										System.err.println("Categorical entity " + pointedObject + " was not found");
									}
								} else {
									throw new RuntimeException("The possible value " + linkIRI + " in " + domainResidentNode + " is unknown.");
								}
								
							} catch (Exception e) {
								System.err.println("There was a problem extracting possible values of " + domainResidentNode + ", but we'll keep loading other values.");
								e.printStackTrace();
								continue;// ignore and continue
							}
							
						}// for symbolic link
					}	// for randomVariableIndividual
				}	// if mExpressions.size() > 0
				
				/* -> hasParent */
				OWLObjectProperty hasParent = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasParent", prefixManager); 
				for (OWLIndividual parentIndividual : this.getObjectPropertyValues(residentNodeIndividual,hasParent, ontology)){
					INode mappedParent = this.getMapLoadedNodes().get(this.extractName(ontology, parentIndividual.asOWLNamedIndividual()));
					if (mappedParent != null) {
						if (mappedParent instanceof ResidentNode){
							ResidentNode aux = (ResidentNode)mappedParent; 
							Edge auxEdge = this.getMEBNFactory().createEdge(aux, domainResidentNode);
							try{
								mFragOfNode.addEdge(auxEdge); 
								try {
									IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, auxEdge, isResidentNodeIn.getIRI());
								} catch (Exception e) {
									e.printStackTrace();
								}
							} catch(Exception e){
								e.printStackTrace();
							}
						} else if (mappedParent instanceof InputNode){
							InputNode aux = (InputNode)mappedParent;
							Edge auxEdge = this.getMEBNFactory().createEdge(aux, domainResidentNode);
							try{
								mFragOfNode.addEdge(auxEdge); 
								try {
									IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, auxEdge, isResidentNodeIn.getIRI());
								} catch (Exception e) {
									e.printStackTrace();
								}
							} catch(Exception e){
								e.printStackTrace();
							}
						} else {
							throw new IOMebnException(this.getResource().getString("DomainResidentNotExistsInMTheory") + " : " + parentIndividual + ". "
									+ this.getResource().getString("GenerativeInputNodeNotExistsInMTheory") + " : " + parentIndividual + ". "	
							); 
						}
					} else {
						throw new IOMebnException(this.getResource().getString("NodeNotFound"), "" + parentIndividual); 
					}
					Debug.println(this.getClass(), "-> " + residentNodeIndividual + ": " + isResidentNodeIn + " = " + parentIndividual); 
				}	
				
				
				
				/* TODO hasProbabilityDistribution (default - for resident nodes - and non-default - for random variables) */
				
				OWLObjectProperty hasProbDist = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(HASPROBABILITYDISTRIBUTION, prefixManager);
				OWLDataProperty hasDeclaration = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HASDECLARATION, prefixManager); 
				for (OWLIndividual element : this.getObjectPropertyValues(residentNodeIndividual,hasProbDist, ontology)) {
					String cpt = "";
					try {
						Collection<OWLLiteral> owlLiterals = this.getDataPropertyValues(element,hasDeclaration, ontology);
						if (!owlLiterals.isEmpty()) {
							cpt = owlLiterals.iterator().next().getLiteral();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					domainResidentNode.setTableFunction(cpt);
					
					// Probably we do not need to trace where the CPT is declared in the ontology...
//					try {
//						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, cpt, element.asOWLNamedIndividual().getIRI());
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
				}
				
				// create the link from domain resident node to owl property (definesUncertaintyOf)
				// unfortunately, we cannot link ArgumentMapping to ResidentNode's Arguments before actually filling the content of arguments (i.e. we should call loadSimpleArgRelationships before this method)
//				this.loadLinkFromResidentNodeToOWLProperty(domainResidentNode, residentNodeIndividual, ontology);
				
				// fill return (extract name again, because the extracted name may be different from the node's name)
				ret.put(this.extractName(ontology, residentNodeIndividual.asOWLNamedIndividual()), domainResidentNode);
				
			}
		}
		return ret;
	}
	
	
	/**
	 * This method simply overwrites the method in superclass in order to instantiate {@link MultiEntityDecisionNode}
	 * and {@link MultiEntityUtilityNode} instead of {@link ResidentNode}, when necessary.
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO#loadDomainMFragContents(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 * @see #getMEDGFactory()
	 */
	protected Map<String, INode> loadDomainMFragContents(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{

		// TODO this method contains copy-pasted code from superclass. Avoid it (e.g. we may use MEBN factory that are sensitive to names).
		
		// extract the prefix managers (they are used to specify the prefixes of xml tags and URIs in OWL ontologies)
		PrefixManager prefixManager = this.getDefaultPrefixManager();	// this is the PR-OWL 2 prefix manager
		PrefixManager prowlDecisionPrefixManager = this.getPROWLDecisionPrefixManager();	// this is the PR-OWL 2 Decision profile prefix manager
		
		// the return value
		Map<String, INode> mapMultiEntityNode = new HashMap<String, INode>();
		
		// extract mfrag class
		OWLClass owlClassDomainMFrag = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAINMFRAG, prefixManager); 
		if (!ontology.containsClassInSignature(owlClassDomainMFrag.getIRI(),true)) {
			// use the old PR-OWL definition
			owlClassDomainMFrag = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.DOMAIN_MFRAG, prefixManager); 
		}
		
		// extract decision node owl class, because they will be used later to check if the type of node individuals are either decision of utility nodes
		OWLClass decisionClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(DOMAINDECISION, prowlDecisionPrefixManager);
		// similarly, extract utility node owl class
		OWLClass utilityClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(DOMAINUTILITY, prowlDecisionPrefixManager);
		
		
		if (owlClassDomainMFrag != null) {
			// iterate on individuals
			for (OWLIndividual domainMFragIndividual : this.getOWLIndividuals(owlClassDomainMFrag, ontology)){
				// getMapNameToMFrag is initialized when loadMTheoryAndMFrags is called in loadMEBNFromOntology
				MFrag domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, domainMFragIndividual.asOWLNamedIndividual())); 
				if (domainMFrag == null){
					System.err.println(this.getResource().getString("DomainMFragNotExistsInMTheory") + ". MFrag = " + domainMFragIndividual); 
					continue;	// ignore the case when multiple MTheory reside in the same ontology.
				}
				
				Debug.println(this.getClass(), "DomainMFrag loaded: " + domainMFragIndividual); 
				
				// fill comments
				domainMFrag.setDescription(this.getDescription(ontology, domainMFragIndividual)); 
				
				/* -> hasResidentNode */
				OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasResidentNodeObjectPropertyName(), prefixManager); 

				for (OWLIndividual owlIndividualResidentNode : this.getObjectPropertyValues(domainMFragIndividual, objectProperty, ontology) ){
					// remove prefixes from the name
					String name = this.extractName(ontology, owlIndividualResidentNode.asOWLNamedIndividual());
					if (name.startsWith(SaverPrOwlIO.RESIDENT_NAME_PREFIX)) {
						try {
							name = name.substring(SaverPrOwlIO.RESIDENT_NAME_PREFIX.length());
						} catch (Exception e) {
							// ignore, because we can still try the original name
							e.printStackTrace();
						}
					}
					// create resident node
					ResidentNode domainResidentNode = null;
					for (OWLClassExpression nodeClass : owlIndividualResidentNode.getTypes(ontology)) {
						if (nodeClass.equals(decisionClass)) {
							// create a decision node
							domainResidentNode = getMEDGFactory().createDecisionNode(name, domainMFrag);
							// we assume that utility and decision nodes are disjoint, so if we found a decision node, we don't need to test if it is an utility node
							break;
						} else if (nodeClass.equals(utilityClass)) {
							// create utility node
							domainResidentNode = getMEDGFactory().createUtilityNode(name, domainMFrag);
							// we assume that utility and decision nodes are disjoint, so if we found an utility node, we don't need to test if it is a decision node
							break;
						} 
					}
					// If the type of this owl individual was neither decision nor utility, then use probabilistic resident node by default
					if (domainResidentNode == null) {
						// this should instantiate a resident node
						domainResidentNode =  getMEDGFactory().createResidentNode(name, domainMFrag); 
						domainMFrag.addResidentNode(domainResidentNode); 
					} else if (domainResidentNode instanceof IMEBNPluginNode) {
						try {
							// notify the node that it was included to a new mfrag
							((IMEBNPluginNode)domainResidentNode).onAddToMFrag(domainMFrag);
						} catch (MFragDoesNotExistException e) {
							throw new IOMebnException(e);
						}
						// add node to mfrag
//						if (!domainMFrag.getResidentNodeList().contains(domainResidentNode)) {
//							domainMFrag.addResidentNode(domainResidentNode); 
//						}
						// the above code is performed in onAddToMFrag
					}
					mebn.getNamesUsed().add(name);  // mark name as "used"
					// the mappings uses the original names (no prefix removal)
					mapMultiEntityNode.put(this.extractName(ontology, owlIndividualResidentNode.asOWLNamedIndividual()), domainResidentNode); 
					Debug.println(this.getClass(), "-> " + domainMFragIndividual + ": " + objectProperty + " = " + owlIndividualResidentNode); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, domainResidentNode, owlIndividualResidentNode.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				
				/* -> hasInputNode */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasInputNodeObjectPropertyName(), prefixManager); 
				for (OWLIndividual owlIndividualInputNode : this.getObjectPropertyValues(domainMFragIndividual, objectProperty, ontology)){
					// instantiate input node
					String inputNodeName = this.extractName(ontology, owlIndividualInputNode.asOWLNamedIndividual());
					InputNode generativeInputNode = this.getMEDGFactory().createInputNode(inputNodeName, domainMFrag); 
					mebn.getNamesUsed().add(inputNodeName); // mark name as used
					domainMFrag.addInputNode(generativeInputNode);  	 // add to mfrag
					mapMultiEntityNode.put(inputNodeName, generativeInputNode); 				
					Debug.println(this.getClass(), "-> " + domainMFragIndividual + ": " + objectProperty + " = " + owlIndividualInputNode); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, generativeInputNode, owlIndividualInputNode.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				
				/* -> hasContextNode */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasContextNodeObjectPropertyName(), prefixManager); 
				for (OWLIndividual owlIndividualContextNode : this.getObjectPropertyValues(domainMFragIndividual, objectProperty, ontology)){
					String contextNodeName = this.extractName(ontology, owlIndividualContextNode.asOWLNamedIndividual());
					ContextNode contextNode = this.getMEDGFactory().createContextNode(contextNodeName, domainMFrag); 
					mebn.getNamesUsed().add(contextNodeName);  	// mark name as used
					domainMFrag.addContextNode(contextNode); 				// add to mfrag
					mapMultiEntityNode.put(contextNodeName, contextNode); 				
					Debug.println(this.getClass(), "-> " + domainMFragIndividual + ": " + objectProperty + " = " + owlIndividualContextNode); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, contextNode, owlIndividualContextNode.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				
				/* -> hasOVariable */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasOVariableObjectPropertyName(), prefixManager); 
				if (!ontology.containsObjectPropertyInSignature(objectProperty.getIRI(),true)) {
					// use the old PR-OWL definition
					objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasOVariable", prefixManager); 
				}
				String ovName = null;
				for (OWLIndividual owlIndividualOrdinaryVariable : this.getObjectPropertyValues(domainMFragIndividual, objectProperty, ontology)){
					ovName = this.extractName(ontology, owlIndividualOrdinaryVariable.asOWLNamedIndividual());	// Name of the OV individual
					String originalOVName = ovName;			// we are going to split a name, but we must reuse the original one in case of errors.
					// Remove MFrag name from ovName. MFrag name is a scope identifier
					try {
						ovName = ovName.split(domainMFrag.getName() + this.getWrappedLoaderPrOwlIO().getOrdinaryVarScopeSeparator())[1];
					} catch (Exception e) {
						e.printStackTrace();
						//Use the original name... 
						ovName = originalOVName;	// If its impossible to split, then no Scope id was found
					}
					// Create instance of OV w/o scope identifier
					OrdinaryVariable oVariable = this.getMEDGFactory().createOrdinaryVariable(ovName, mebn.getTypeContainer().getDefaultType(), domainMFrag); 
					domainMFrag.addOrdinaryVariable(oVariable); 
					// let's map objects w/ scope identifier included (use the original name)
					mapMultiEntityNode.put(originalOVName, oVariable); 
					Debug.println(this.getClass(), "-> " + domainMFragIndividual + ": " + objectProperty + " = " + owlIndividualOrdinaryVariable); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, oVariable, owlIndividualOrdinaryVariable.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}	
		}
		
		
		return mapMultiEntityNode;
	}
	
	


}
