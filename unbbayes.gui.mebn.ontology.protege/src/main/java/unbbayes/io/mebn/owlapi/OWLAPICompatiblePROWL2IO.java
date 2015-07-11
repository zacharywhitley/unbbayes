/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.OWLEntityRemover;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.PROWLModelUser;
import unbbayes.io.mebn.SaverPrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.entity.BooleanStateEntity;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.TypeContainer;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.entity.ontology.owlapi.OWLAPIObjectEntityContainer;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.ArgumentOVariableAlreadySetException;
import unbbayes.util.Debug;

/**
 * It implements PR-OWL2 support.
 * The methods' names may not be very intuitive, but this is because this class
 * follows some naming patterns inherited from {@link unbbayes.io.mebn.PrOwlIO}, {@link unbbayes.io.mebn.SaverPrOwlIO}
 * and {@link unbbayes.io.mebn.LoaderPrOwlIO}. 
 * TODO stop following template methods inherent from {@link unbbayes.io.mebn.PrOwlIO}, {@link unbbayes.io.mebn.SaverPrOwlIO}
 * and {@link unbbayes.io.mebn.LoaderPrOwlIO}, because their architectures are extremely sensitive to object states, thus
 * thread-unsafe and hard to extend.
 * @author Shou Matsumoto
 *
 */
public class OWLAPICompatiblePROWL2IO extends OWLAPICompatiblePROWLIO implements IPROWL2ModelUser {
	
	// TODO refactor all direct references to IPROWL2ModelUser as references to fields in this class (using respective getters and setters), so that we can change the expected OWL entity's names at runtime instead of compile/linkage time.

	private Map<OWLIndividual, Set<ResidentNode>> randomVariableIndividualToResidentNodeSetCache;
	
	private Map<String, Argument> recursivelyAddedArgumentsOfMExpression;
	
	private IPROWL2IndividualsExtractor prowl2IndividualsExtractor;

	private Map<MFrag, OWLIndividual> mfragCache;

	private Map<ResidentNode, OWLIndividual> domainResidentCache;

	private Map<InputNode, OWLIndividual> generativeInputCache;

	private Map<ContextNode, OWLIndividual> contextCache;

	private Map<String, String> metaEntityCache;

	private Map<Entity, OWLClassExpression> objectEntityClassesCache;

	private Map<OrdinaryVariable, OWLIndividual> ordinaryVariableCache;

	private Map<CategoricalStateEntity, OWLIndividual> categoricalStatesCache;

	private Map<ResidentNode, OWLIndividual> randomVariableCache;
	
	private String hasMExpressionPropertyName = IPROWL2ModelUser.HASMEXPRESSION;
	
	private String isMExpressionOfPropertyName = IPROWL2ModelUser.ISMEXPRESSIONOF;

	private Map<String, OWLIndividual> builtInRVCache;
	
	private OWLOntologyIRIMapper prowl2DefinitionIRIMapper;
	
	private String prowl2ModelFilePath = "pr-owl/pr-owl2.owl";

	private Map<ResidentNode, OWLIndividual> mappingArgumentCache;
	
	/**
	 * @deprecated
	 */
	protected OWLAPICompatiblePROWL2IO() {
		super();
		try {
			this.initialize();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	

	/**
	 * This is the constructor method to be used in order to create new instances of {@link OWLAPICompatiblePROWL2IO}
	 * @return
	 */
	public static MebnIO newInstance() {
		return new OWLAPICompatiblePROWL2IO();
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#initialize()
	 */
	@Override
	protected void initialize() {
		try {
			// this will call resetCache as well
			super.initialize();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// because the PR-OWL1's "hasOVariable" object property has changed to "hasOrdinaryVariable" in PR-OWL2, set to it.
			this.setHasOVariableObjectPropertyName(IPROWL2ModelUser.HASOVARIABLE);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// because the PR-OWL1's "isSubsBy" object property has changed to "isSubstitutedBy" in PR-OWL2, set to it.
			this.setIsSubsByObjectPropertyName(IPROWL2ModelUser.ISSUBSTITUTEDBY);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			this.setHasMFragObjectProperty(IPROWL2ModelUser.HASMFRAG);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// use PR-OWL2 namespace instead of the default (PR-OWL1) one
			this.setProwlOntologyNamespaceURI(IPROWL2ModelUser.PROWL2_NAMESPACEURI); 
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			// use another extractor to extract OWL classes that are specific to PR-OWL 2 (the default is for PR-OWL1).
			this.setNonPROWLClassExtractor(DefaultNonPROWL2ClassExtractor.getInstance());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			// initialize delegator responsible for getting individuals of a PR-OWL2 ontology 
			// (but it is supposed to ignore individuals in PR-OWL2 definition file, such as built in random variables)
			this.setPROWL2IndividualsExtractor(DefaultPROWL2IndividualsExtractor.newInstance());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		// resetCache is called by super.initialize()
//		try {
//			// initialize cache elements (mostly, maps which stores MEBN elements temporary)
//			this.resetCache();
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
		// explicitly initialize prowl2ModelFilePath as "pr-owl/pr-owl2.owl" before initializing IRI mapper
		try {
			this.setPROWL2ModelFilePath("pr-owl/pr-owl2.owl");
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		// initialize IRI mapper, so that requests for PR-OWL2 IRIs is delegated to local files
		try {
			// extract local file
			File prowl2DefinitionFile = null;	// this is going to be the file containing PR-OWL2 definitions
			try {
				// load file assuming that it is a plug-in resource (in such case, we must tell plug-in classloaders to look for files).
				prowl2DefinitionFile = new File(this.getClass().getClassLoader().getResource(this.getPROWL2ModelFilePath()).toURI());
			} catch (Exception e1) {
				try {
					Debug.println(this.getClass(), e1.getMessage() + " - Could not load pr-owl2 definitions from " + this.getPROWL2ModelFilePath() + " in plug-in's resource folder. Retry using project's root folder...", e1);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				try {
					// retrying using file in project's root folder
					prowl2DefinitionFile = new File(this.getPROWL2ModelFilePath());
					if (!prowl2DefinitionFile.exists()) {
						prowl2DefinitionFile = null;
					}
				} catch (Exception e) {
					try {
						Debug.println(this.getClass(), e.getMessage() + " - Could not load pr-owl2 definitions from " + this.getPROWL2ModelFilePath() + " in project's root folder.", e);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
			// check if local definition was found
			if (prowl2DefinitionFile != null) {
				// initialize default IRI mapper, so that it uses local file
				this.setProwl2DefinitionIRIMapper(
						new SimpleIRIMapper(
								IRI.create(IPROWL2ModelUser.PROWL2_NAMESPACEURI), 	// the PR-OWL2 IRI will be translated to...
								IRI.create(prowl2DefinitionFile)					// ...this IRI (local file)
						)
				);
			} else {
				// no local file was found
				try {
					Debug.println(this.getClass(), "Could not initialize PR-OWL2 IRI mapper using local file. Calls to PR-OWL2 definitions will be requested to " + IPROWL2ModelUser.PROWL2_NAMESPACEURI);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				// instead of using null, use identity (i.e. IPROWL2ModelUser.PROWL2_NAMESPACEURI maps to itself)
				IRI prowl2IRI = IRI.create(IPROWL2ModelUser.PROWL2_NAMESPACEURI);
				this.setProwl2DefinitionIRIMapper(new SimpleIRIMapper(prowl2IRI, prowl2IRI));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.IOWLAPIOntologyUser#loadMEBNFromOntology(unbbayes.prs.mebn.MultiEntityBayesianNetwork, org.semanticweb.owlapi.model.OWLOntology, org.semanticweb.owlapi.reasoner.OWLReasoner)
	 */
	public void loadMEBNFromOntology(MultiEntityBayesianNetwork mebn, OWLOntology ontology, OWLReasoner reasoner) {
		// assertion
		if (mebn == null) {
			Debug.println(this.getClass(), "There is no MEBN to fill...");
			return;
		}
		
		if (ontology == null) {
			Debug.println(this.getClass(), "There is no ontology to load...");
			return;
		}
		
		// fill the storage implementor of MEBN (a reference to an object that loaded/saved the mebn last time)
		mebn.setStorageImplementor(OWLAPIStorageImplementorDecorator.newInstance(ontology));

		// use an ObjectEntityContainer which also handles OWL classes and individuals
		mebn.setObjectEntityContainer(new OWLAPIObjectEntityContainer(mebn));
		
		
		// Reset the non-PR-OWL classes extractor (this)
		this.resetNonPROWLClassExtractor();
		
		// force ontology to delegate requisitions by adding a mapper (usually, it will redirect PR-OWL2 IRIs to a local file)
		ontology.getOWLOntologyManager().removeIRIMapper(this.getProwl2DefinitionIRIMapper());	// just to avoid duplicate mapper
		ontology.getOWLOntologyManager().addIRIMapper(this.getProwl2DefinitionIRIMapper());
		
		// update last owl reasoner
		if (reasoner != null) {
			this.setLastOWLReasoner(reasoner);
		}
		
		// this is a instance of MEBN to be filled. The name will be updated after loadMTheoryAndMFrags
		IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, mebn, ontology.getOntologyID().getOntologyIRI());
		
		// Start loading ontology. This is template method design pattern.

		// load MTheory. The nameToMFragMap maps a name to a MFrag.
		try {
			this.setMapNameToMFrag(this.loadMTheoryAndMFrags(ontology, mebn));
		} catch (IOMebnException e) {
			// the ontology does not contain PR-OWL specific elements. Stop loading PR-OWL and return an empty mebn.
			e.printStackTrace();
			return;
		}
		
		try {
			// load object entities and fill the mapping of object entities
			this.setMapLabelToObjectEntity(this.loadObjectEntity(ontology, mebn));
			
			// Load individuals of object entities (ObjectEntityInstances)
			this.setMapLoadedObjectEntityIndividuals(loadObjectEntityIndividuals(ontology, mebn));
			
			// Meta Entities, categorical entities and boolean states are not loaded as PR-OWL specific classes anymore 
			// (because they became either non-PROWL2 classes or owl:datatypes), so we are commenting them out.
			
//			this.setMapNameToType(this.loadMetaEntitiesClasses(ontology, mebn));
			this.setMapCategoricalStates(this.loadCategoricalStateEntity(ontology, mebn));
//			this.setMapBooleanStates(this.loadBooleanStateEntity(ontology, mebn));

			// load content of MFrag (nodes, ordinary variables, etc...)
			this.setMapLoadedNodes(this.loadDomainMFragContents(ontology, mebn));
			
			// load built in random variables. Reuse mapLoadedNodes.
			this.setMapBuiltInRV(this.loadBuiltInRV(ontology, mebn));
			
			// load content of resident nodes
			this.setMapFilledResidentNodes(this.loadDomainResidentNode(ontology, mebn));

			// load content of input nodes
			this.setMapFilledInputNodes(this.loadGenerativeInputNode(ontology, mebn));

			// load content of context nodes. The mapIsContextInstanceOf maps Context nodes to either BuiltInRV or ResidentNode. The mapArgument mapps a name to an argument
			this.setMapTopLevelContextNodes(this.loadContextNode( ontology, mebn));
			
			
			// load content of ordinary variables
			this.setMapFilledOrdinaryVariables(this.loadOrdinaryVariable(ontology, mebn));
			
			// load generic arguments relationships
			this.setMapFilledArguments(this.loadArgRelationship(ontology, mebn));
			
			// load simple arguments
			this.setMapFilledSimpleArguments(this.loadSimpleArgRelationship(ontology, mebn));
			
			// load mappings from MEBN elements to OWL elements
			this.loadMappingsBetweenMEBNAndOWL(ontology, mebn);
			
			// adjust the order of arguments (the appearance order of arguments may not be the correct order). 
			// TODO this seems to be a magic method and should be avoided. This is only used now because of how ancestor classes were implemented
			this.ajustArgumentOfNodes(mebn);
			
			// load the content of the formulas inside context nodes
			this.buildFormulaTrees(this.getMapTopLevelContextNodes(), mebn);
			
			
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to load ontology " + ontology, e);
		}
		
		
	}
	
	/**
	 * This method iterates on {@link #loadLinkFromResidentNodeToOWLProperty(ResidentNode, OWLObject, OWLOntology)}
	 * for all owl individuals of DomainResidentNode and instances of {@link ResidentNode} in {@link #getMapFilledResidentNodes()}
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @see #getMapFilledResidentNodes()
	 * @see #loadLinkFromResidentNodeToOWLProperty(ResidentNode, OWLObject, OWLOntology)
	 */
	protected void loadMappingsBetweenMEBNAndOWL(OWLOntology ontology, MultiEntityBayesianNetwork mebn) {
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();	// this is the PR-OWL2 prefix
		
		// extract owl class DomainResidentNode
		OWLClass owlClassDomainResidentNode = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAINRESIDENT, prefixManager); 
		if (!ontology.containsClassInSignature(owlClassDomainResidentNode.getIRI(), true)) {
			// use old definition
			owlClassDomainResidentNode = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.DOMAIN_RESIDENT, prefixManager); 
		}
		
		// iterate on individuals
		if (owlClassDomainResidentNode != null) {
			for (OWLIndividual residentNodeIndividual :  this.getOWLIndividuals(owlClassDomainResidentNode, ontology)){
				ResidentNode resident = this.getMapFilledResidentNodes().get(this.extractName(residentNodeIndividual));
				if (resident == null) {
					try {
						Debug.println(this.getClass(), residentNodeIndividual + " was not previously loaded. This is an error, but we'll try other resident nodes as well.");
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
				// load link
				this.loadLinkFromResidentNodeToOWLProperty(resident, residentNodeIndividual, ontology);
			}
		}
	}



	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadMTheoryAndMFrags(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, MFrag> loadMTheoryAndMFrags(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException {
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// prepare the value to return
		Map<String, MFrag> mapDomainMFrag = new HashMap<String, MFrag>();
		OWLClass owlNamedClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.MTHEORY, prefixManager);
		
		OWLObject mTheoryObject = null;
		
		// extract MTheory as subclass
		Set<OWLClassExpression> classes = this.getOWLSubclasses(owlNamedClass, ontology);
		if (classes != null && !classes.isEmpty()) {
			mTheoryObject = classes.iterator().next();
		} else {
			// extract MTheory as individual
			Set<OWLIndividual> instances =  this.getOWLIndividuals(owlNamedClass, ontology); // owlNamedClass.getIndividuals(ontology);
			if (instances == null || instances.size() <= 0) {
				System.err.println(this.getResource().getString("MTheoryNotExist") + ontology);
			} else {
				mTheoryObject = instances.iterator().next();
			}
		}
		
		// extract name
		String mtheoryName = null;
		
		if (mTheoryObject != null) {
			if (mTheoryObject instanceof OWLEntity) {
				mtheoryName = this.extractName(ontology, ((OWLEntity)mTheoryObject));
				mebn.setName(mtheoryName); 
			} 
		}
		mebn.getNamesUsed().add(mebn.getName()); 
		
		Debug.println(this.getClass(), "MTheory loaded: " + mTheoryObject); 
		
		// start filling some properties
		
		// comments (description of mebn and mfrags)
		String mTheoryDescription = null;
		if (mTheoryObject instanceof OWLEntity) {
			mTheoryDescription = this.getDescription(ontology, (OWLEntity)mTheoryObject);
		} 
		mebn.setDescription(mTheoryDescription); 
		
		// extract instances related to the MTheory by hasMFrag and iterate over obtained individuals
		for (OWLObject owlObject : this.getMFragsRelatedToMTheory(mTheoryObject, ontology)) {
			if (!(owlObject instanceof OWLEntity)) {
				Debug.println(this.getClass(), "A non-OWLEntity was declared as MFrag: " + owlObject);
				continue;	// ignore objects that does not have an ID
			}
			// remove prefixes from name
			String nameWithoutPrefix = this.extractName(ontology, (OWLEntity)owlObject);
			if (nameWithoutPrefix.startsWith(SaverPrOwlIO.MFRAG_NAME_PREFIX)) {
				try {
					nameWithoutPrefix = nameWithoutPrefix.substring(SaverPrOwlIO.MFRAG_NAME_PREFIX.length());
				} catch (Exception e) {
					// We can still try the name with the prefixes.
					e.printStackTrace();
				}
			}
			// instantiate the MFrag and add it to the MTheory
			Debug.println(this.getClass(), "hasDomainMFrag: " + nameWithoutPrefix); 
			MFrag domainMFrag = this.getMEBNFactory().createMFrag(nameWithoutPrefix, mebn); 
			mebn.addDomainMFrag(domainMFrag); 
			mebn.getNamesUsed().add(nameWithoutPrefix); 
			
			try {
				IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, domainMFrag, ((OWLEntity)owlObject).getIRI());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			// the mapping still contains the original name (with no prefix removal)
			mapDomainMFrag.put(this.extractName(ontology, (OWLEntity)owlObject), domainMFrag); 
		}	
		return mapDomainMFrag;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadObjectEntity(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, Entity> loadObjectEntity(OWLOntology ontology, MultiEntityBayesianNetwork mebn){
		
//		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// this map will be returned by this method
		Map<String, Entity> mapObjectEntityLabels = new HashMap<String, Entity>();
		
		// this is a list that will contain all the object entities (classes that are not in PR-OWL 2 definition)
		Collection<OWLClassExpression> subClassesOfObjectEntities = this.getNonPROWLClasses(ontology);
		if (subClassesOfObjectEntities == null) {
			subClassesOfObjectEntities = new HashSet<OWLClassExpression>();
		}
		
		// Explicitly add "owl:Thing" as a generic entity
		if (!subClassesOfObjectEntities.contains(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing())) {
			subClassesOfObjectEntities.add(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
		}
		
		// iterate on subclasses of object entities
		for (OWLClassExpression owlClassExpression : subClassesOfObjectEntities){
			OWLClass subClass = owlClassExpression.asOWLClass(); 
			if (subClass == null) {
				// it was a unknown type of class (maybe anonymous)
				continue;
			}
			
			try{
				String objectEntityName = this.extractName(ontology, subClass);
				if (!mebn.getNamesUsed().contains(objectEntityName)) {
					// create object entity if it is not already used
					ObjectEntity objectEntityMebn = mebn.getObjectEntityContainer().createObjectEntity(objectEntityName); 	
					mapObjectEntityLabels.put(objectEntityMebn.getType().getName(), objectEntityMebn); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, objectEntityMebn, subClass.getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					// set the name as "used", in order for UnBBayes to avoid duplicate names.
					mebn.getNamesUsed().add(objectEntityName); 
				} else {
					System.err.println(objectEntityName + " is duplicated.");
				}
			} catch(Exception e){
				// perform a exception translation because the method's signature does not allow non-runtime exceptions
				throw new RuntimeException("ObjectEntity: " + subClass, e);
			}
		}	
		
		return mapObjectEntityLabels;
	}

	
	/**
	 * This method overwrites the superclass just to add individuals of owl:Thing (nothing else) as an object entity's individual.
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadObjectEntityIndividuals(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, ObjectEntityInstance> loadObjectEntityIndividuals(
			OWLOntology ontology, MultiEntityBayesianNetwork mebn)
			throws TypeException {
		// reuse individuals extracted from the methods in superclass
		Map<String, ObjectEntityInstance> ret = super.loadObjectEntityIndividuals(ontology, mebn);
		
		// make sure mebn contains owl:Thing as an object entity
		ObjectEntity owlThingAsMEBNEntity = mebn.getObjectEntityContainer().getObjectEntityByName(
			this.extractName(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing())
		);
		if (owlThingAsMEBNEntity == null) {
			throw new IllegalStateException("owl:Thing is expected to be an object entity, but it is not.");
		}
		
		// iterate on individuals that are direct instances of owl:Thing
		Set<OWLNamedIndividual> individuals = new HashSet<OWLNamedIndividual>();
		// use reasoner to load instances, if possible
		if (this.getLastOWLReasoner() != null) {
			for (OWLNamedIndividual owlNamedIndividual : this.getLastOWLReasoner().getInstances(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing(), true).getFlattened()) {
				//We want to extract individuals that are only owl:Thing (because the other cases will be treated by other methods)
				if (this.getLastOWLReasoner().getTypes(owlNamedIndividual, true).getFlattened().size() <= 1) {
					// this is an owl:Thing and not "something else". 
					individuals.add(owlNamedIndividual);
				}
			}
		} else {
			// no reasoner is specified, so load explicit instances of owl:Thing
			OWLClass owlThing = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing();	// just for comparison
			OWLOntologyManager manager = ontology.getOWLOntologyManager();	// just to check what other classes an instance belongs to
			for (OWLIndividual individual : owlThing.getIndividuals(ontology)) {
				// how many classes this individual explicitly belongs to
				int howManyClassesItBelongs = individual.getTypes(manager.getOntologies()).size();	
				if (howManyClassesItBelongs== 0		// nothing explicitly stated, so this is owl:Thing
						|| ( howManyClassesItBelongs == 1 && individual.getTypes(manager.getOntologies()).contains(owlThing) )) {	//  this is only owl:Thing and nothing else
					try {
						individuals.add(individual.asOWLNamedIndividual());
					} catch (OWLRuntimeException e) {
						Debug.println(getClass(), "Anonymous non-prowl2 individual found", e);
					}
				}
			}
		}
		if (individuals == null) {
			// there is nothing to add
			return ret;
		}
		for (OWLNamedIndividual individualOfThing : individuals) {
			
			// creates a object entity instance and adds it into the mebn entity container
			try {
				String individualName = this.extractName(ontology, individualOfThing.asOWLNamedIndividual());
				ObjectEntityInstance addedInstance = owlThingAsMEBNEntity.addInstance(individualName);
				mebn.getObjectEntityContainer().addEntityInstance(addedInstance);
				ret.put(individualName, addedInstance);
				
				try {
					IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, addedInstance, individualOfThing.asOWLNamedIndividual().getIRI());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (EntityInstanceAlreadyExistsException eiaee) {
				// Duplicated instance/individuals are not a major problem for now
				Debug.println("Duplicated instance/individual declaration found at OWL Loader");
			}
		}
		
		return ret;
	}



	/**
	 * This method overwrites {@link #getMFragsRelatedToMTheory(OWLObject, OWLOntology)}
	 * so that it does not load MFrags as OWL classes and it ignores finding MFrags
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#getMFragsRelatedToMTheory(org.semanticweb.owlapi.model.OWLObject, org.semanticweb.owlapi.model.OWLOntology)
	 */
	protected Collection<OWLObject> getMFragsRelatedToMTheory(OWLObject mTheoryObject, OWLOntology ontology) {
		// overwrite this method so that it does not load classes or finding mfrags
		
		Set<OWLObject> ret = new HashSet<OWLObject>();
		
		if (ontology == null) {
			Debug.println(this.getClass(), "No ontology was specified in getMFragsRelatedToMTheory");
			return ret;
		}
		
		
		// use reasoner to extract individuals
		if (this.getLastOWLReasoner() != null) {
			if ((mTheoryObject instanceof OWLIndividual) 
					&& (mTheoryObject != null ) ) {
				try {
					//query: DomainMFrag and inverse hasMFrag value <NAME OF THE MTHEORY>
					ret.addAll(this.getLastOWLReasoner().getInstances(this.parseExpression(IPROWL2ModelUser.DOMAINMFRAG + " and inverse " + this.getHasMFragObjectProperty() + " value " + this.extractName(mTheoryObject)), false).getFlattened());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
		} else {
//			throw new IllegalStateException("Reasoner is not initialized");
			ret.addAll(this.getObjectPropertyValues((OWLIndividual)mTheoryObject, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(getHasMFragObjectProperty(), getDefaultPrefixManager()), ontology));
		}
		
		return ret;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadDomainMFragContents(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, INode> loadDomainMFragContents(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{

		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// the return value
		Map<String, INode> mapMultiEntityNode = new HashMap<String, INode>();
		
		// extract mfrag class
		OWLClass owlClassDomainMFrag = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAINMFRAG, prefixManager); 
		if (!ontology.containsClassInSignature(owlClassDomainMFrag.getIRI(),true)) {
			// use the old PR-OWL definition
			owlClassDomainMFrag = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.DOMAIN_MFRAG, prefixManager); 
		}
		
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
					ResidentNode domainResidentNode = this.getMEBNFactory().createResidentNode(name, domainMFrag); 
					mebn.getNamesUsed().add(name);  // mark name as "used"
					// add node to mfrag
					domainMFrag.addResidentNode(domainResidentNode); 
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
					InputNode generativeInputNode = this.getMEBNFactory().createInputNode(inputNodeName, domainMFrag); 
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
					ContextNode contextNode = this.getMEBNFactory().createContextNode(contextNodeName, domainMFrag); 
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
					OrdinaryVariable oVariable = this.getMEBNFactory().createOrdinaryVariable(ovName, mebn.getTypeContainer().getDefaultType(), domainMFrag); 
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
	

	/**
	 * This method was overwritten in order to load built in random variables (i.e. individuals of RandomVariable that are built in to PR-OWL2 definition, such as
	 * iff, equalsTo, not, and, ...) and initialize them. 
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadBuiltInRV(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, BuiltInRV> loadBuiltInRV(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{

		PrefixManager prefixManager = this.getDefaultPrefixManager();	// PR-OWL2 prefix
		
		// return value
		Map<String, BuiltInRV> ret = new HashMap<String, BuiltInRV>();
		
		// iterate on individuals
		for (OWLIndividual builtInRVIndividual : this.getBuiltInRVIndividuals(ontology)){
			if (!builtInRVIndividual.isNamed()) {
				// ignore anonymous
				try{
					Debug.println(this.getClass(), builtInRVIndividual + " is anonymous. It is going to be ignored by built in RV loader.");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
			// ignore it if it is not declared in PR-OWL2 definition. Use string comparison to prefixes.
			if (!builtInRVIndividual.asOWLNamedIndividual().getIRI().toString().startsWith(IPROWL2ModelUser.PROWL2_NAMESPACEURI)) {
				try{
					Debug.println(this.getClass(), builtInRVIndividual + " is not in " + IPROWL2ModelUser.PROWL2_NAMESPACEURI+ " namespace. It is going to be ignored by built in RV loader.");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
			String nameBuiltIn = this.extractName(ontology, builtInRVIndividual.asOWLNamedIndividual());  // this is the name of the built-in RV
			BuiltInRV builtInRV = this.getMEBNFactory().createBuiltInRV(nameBuiltIn);				// this variable will hold the instantiated BuiltInRV
			if (builtInRV == null) {
				try {
					System.err.println("Unknown builtin RV found: " + builtInRVIndividual + ". It is going to be ignored...");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;	// let's just ignore unknown elements...
			}	
			// by now, builtInRV != null, but let's just assert it...
			if(builtInRV != null){
				mebn.addBuiltInRVList(builtInRV); 				// add to mebn
				ret.put(nameBuiltIn, builtInRV); // add to return
				
				Debug.println(this.getClass(), "BuiltInRV loaded: " + builtInRVIndividual); 		
				
				try {
					IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, builtInRV, builtInRVIndividual.asOWLNamedIndividual().getIRI());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// extract links to nodes (-> isTypeOfMExpression)
				OWLObjectProperty isTypeOfMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(ISTYPEOFMEXPRESSION, prefixManager); 
				for (OWLIndividual mExpressionIndividual : this.getObjectPropertyValues(builtInRVIndividual,isTypeOfMExpression, ontology)){
					// extract the node related to MExpression (-> isMExpressionOf)
					OWLObjectProperty isMExpressionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getIsMExpressionOfPropertyName(), prefixManager);
					for (OWLIndividual nodeIndividual : this.getObjectPropertyValues(mExpressionIndividual,isMExpressionOf, ontology)){
						if (!nodeIndividual.isNamed()) {
							// ignore anonymous
							try {
								Debug.println(this.getClass(), nodeIndividual + " is anonymous");
							} catch (Throwable t) {
								t.printStackTrace();							}
							continue;
						}
						// extract the related node. It assumes the node was already loaded previously (i.e. loadMFragContents has loaded at least the name and types of the available nodes)
						INode mappedNode = this.getMapLoadedNodes().get(this.extractName(ontology, nodeIndividual.asOWLNamedIndividual()));
						if (mappedNode != null ) {
							if (mappedNode instanceof InputNode) {
								InputNode generativeInputNode = (InputNode)mappedNode;
								builtInRV.addInputInstance(generativeInputNode); // add relation to builtInRV
								try {
									Debug.println(this.getClass(), "-> " + builtInRVIndividual + " -> " + isTypeOfMExpression + " : "+ mExpressionIndividual + " ->" + isMExpressionOf + " : " + nodeIndividual); 
								} catch (Throwable t) {
									t.printStackTrace();
								}
							} else if (mappedNode instanceof ContextNode) {
								ContextNode contextNode = (ContextNode)mappedNode;
								builtInRV.addContextInstance(contextNode); // add relation to builtInRV
								try {
									Debug.println(this.getClass(), "-> " + builtInRVIndividual + " -> " + isTypeOfMExpression + " : "+ mExpressionIndividual + " ->" + isMExpressionOf + " : " + nodeIndividual); 
								} catch (Throwable t) {
									t.printStackTrace();
								}
							} else if (mappedNode instanceof ResidentNode) {
								// built in RV for a resident node is an unknown relationship
								throw new IOMebnException(this.getResource().getString("BuiltInDontImplemented"),  
										"BuiltInRV = " + builtInRVIndividual + ", MExpression = " + mExpressionIndividual + ", Resident = " + nodeIndividual);
							} else {
								// all nodes should be present in mapLoadedNodes.
								throw new IOMebnException(this.getResource().getString("NodeNotFound"),  
										"BuiltInRV = " + builtInRVIndividual + ", MExpression = " + mExpressionIndividual + ", Node = " + mappedNode + "(" + mappedNode.getClass() + ")"); 
							}
						} else {
							// all nodes should be present in mapLoadedNodes, except the findings.
							try {
								if (this.getLastOWLReasoner().getTypes(nodeIndividual.asOWLNamedIndividual(), false).containsEntity(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass("FindingResidentNode", prefixManager))
										|| this.getLastOWLReasoner().getTypes(nodeIndividual.asOWLNamedIndividual(), false).containsEntity(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass("FindingInputNode", prefixManager))) {
									// findings can be ignored
									continue;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
							throw new IOMebnException(this.getResource().getString("NodeNotFound"),  
									"BuiltInRV = " + builtInRVIndividual + ", MExpression = " + mExpressionIndividual + ", Node = " + nodeIndividual ); 
						}
					}
					
					
				}
			}
			
		}		
		return ret;
	}


	/**
	 * @param ontology : ontology to be used to find individuals. If {@link #getLastOWLReasoner()} is non-null, then
	 * the reasoner will be used instead.
	 * @return : OWL individuals representing Random variables that are built-in in the PR-OWL 2 definition. 
	 * For example, boolean operators, like "and", "or", "not", "ForAll", "equals", etc.
	 * @see #getLastOWLReasoner()
	 */
	protected Collection<OWLIndividual> getBuiltInRVIndividuals(OWLOntology ontology) {
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();	// PR-OWL2 prefix
		
		// return value
		Collection<OWLIndividual> ret = new HashSet<OWLIndividual>();
		
		// extract built in random variables (we assume they are only boolean random variables)
		OWLClass builtInOWLClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.BOOLEANRANDOMVARIABLE, prefixManager); 
		if (!ontology.containsClassInSignature(builtInOWLClass.getIRI(),true)) {
			// use the old PR-OWL definition
			builtInOWLClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(BUILTIN_RV, prefixManager); 
		}
		
		// TODO Auto-generated method stub
		if (getLastOWLReasoner() != null) {
			// if the reasoner is on, then getOWLIndividuals can return all possible individuals of the built-in RV.
			return this.getOWLIndividuals(builtInOWLClass, ontology);
		}
		
		// at this point, there is no DL reasoner, so we need to include instances of subclasses too, and for all ontologies being loaded
		for (OWLOntology currentOntology : ontology.getOWLOntologyManager().getOntologies()) {
			ret.addAll(this.getOWLIndividuals(builtInOWLClass, currentOntology));
			for (OWLClassExpression subClass : builtInOWLClass.getSubClasses(currentOntology)) {
				// the subclasses are supposedly LogicalOperator and Quantifier
				ret.addAll(this.getOWLIndividuals(subClass, currentOntology));
			}
		}
		
		return ret;
	}



	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadDomainResidentNode(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, ResidentNode> loadDomainResidentNode(OWLOntology ontology, MultiEntityBayesianNetwork mebn)  throws IOMebnException{

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
							domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());  
							domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());   
							// force node to have Absurd
							if (!domainResidentNode.hasPossibleValue(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity())) {
								domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());
							}
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
									// force node to have Absurd
									if (!domainResidentNode.hasPossibleValue(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity())) {
										domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());
									}
									if (pointedObject.isBoolean()) {
										// try boolean datatype
										domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());  
										domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());   
										// absurd was added previously
										// TODO stop using type of states because this is mutable
										domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
									} else  {
										// non-boolean datatypes are not supported yet
										System.err.println("Non-boolean datatypes are not supported yet: " + pointedObject);
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
									if (!domainResidentNode.hasPossibleValue(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity())) {
										domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());
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
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadGenerativeInputNode(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, InputNode> loadGenerativeInputNode(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{
	    
		Map<String, InputNode> ret = new HashMap<String, InputNode>();

		PrefixManager prefixManager = this.getDefaultPrefixManager();	// prowl2 prefix
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();		
		
		OWLClass inputNodePr = factory.getOWLClass(IPROWL2ModelUser.GENERATIVEINPUT, prefixManager); 
		if (!ontology.containsClassInSignature(inputNodePr.getIRI(), true)) {
			inputNodePr = factory.getOWLClass(PROWLModelUser.GENERATIVE_INPUT, prefixManager); 
		}
		
		if (inputNodePr != null) {
			for (OWLIndividual inputIndividual : this.getOWLIndividuals(inputNodePr, ontology)){
				if (!inputIndividual.isNamed()) {
					Debug.println(this.getClass(), inputIndividual + " ignored because it is anonymous.");
					continue;	// ignore anonymous
				}
				try {
					Debug.println(this.getClass(), "  - Input Node to be loaded: " + inputIndividual); 			
				} catch (Throwable t) {
					t.printStackTrace();
				}
				
				String inputIndividualName = this.extractName(ontology, inputIndividual.asOWLNamedIndividual());
				
				// get node from cache
				INode loadedNode = this.getMapLoadedNodes().get(inputIndividualName);
				if (loadedNode == null){
					throw new IOMebnException(this.getResource().getString("GenerativeInputNodeNotExistsInMTheory"), "Input = " + inputIndividual ); 				
				}
				InputNode generativeInputNode = (InputNode)loadedNode; 
				
				//add comment
				generativeInputNode.setDescription(this.getDescription(ontology, inputIndividual)); 
				
				// extract MExpression
				OWLIndividual mExpressionIndividual = null;
				try {
					mExpressionIndividual = this.getObjectPropertyValues(inputIndividual, factory.getOWLObjectProperty(HASMEXPRESSION, prefixManager), ontology).iterator().next();
				} catch (NullPointerException e) {
					throw new IllegalArgumentException("Could not find an MExpression for " + inputIndividual, e);
				}
				if (mExpressionIndividual == null) {
					throw new IllegalArgumentException("Could not find an MExpression for " + inputIndividual);
				}
				
				/* -> typeOfMExpression */
				
				Collection<OWLIndividual> auxCollection = this.getObjectPropertyValues(mExpressionIndividual,factory.getOWLObjectProperty(TYPEOFMEXPRESSION, prefixManager), ontology);
				
				if(!auxCollection.isEmpty()){
					// get resident node 
					OWLIndividual rvIndividual = auxCollection.iterator().next();// use only the 1st one
					
					// check if RV is pointing to a random variable (it may be pointing to severals, but let's just use the 1st)
					if (this.getRandomVariableIndividualToResidentNodeSetCache().containsKey(rvIndividual)){
						try{
							ResidentNode domainResidentNode = this.getRandomVariableIndividualToResidentNodeSetCache().get(rvIndividual).iterator().next(); 
							generativeInputNode.setInputInstanceOf(domainResidentNode); 
						} catch(Exception e){
							e.printStackTrace(); 
						}
						try {
							Debug.println(this.getClass(), "   - type of " + generativeInputNode + " is " + rvIndividual); 
						}catch (Throwable t) {
							t.printStackTrace();
						}
					} else {
						String rvName = this.extractName(ontology, rvIndividual.asOWLNamedIndividual());
						if (this.getMapBuiltInRV().containsKey(rvName)){
							BuiltInRV builtInRV = this.getMapBuiltInRV().get(rvName); 
							generativeInputNode.setInputInstanceOf(builtInRV); 
							try {
								Debug.println(this.getClass(), "   - isInputInstanceOf " + builtInRV.getName()); 
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
					}
				}
				
				/* mExpressionIndividual -> hasArgument  */
				for (OWLIndividual argumentIndividual : this.getObjectPropertyValues(mExpressionIndividual, factory.getOWLObjectProperty(HASARGUMENT, prefixManager), ontology) ){
					if (!argumentIndividual.isNamed()) {
						continue;	// ignore anonymous individuals
					}
					String argumentName = this.extractName(ontology, argumentIndividual.asOWLNamedIndividual());
					Argument argument = this.getMEBNFactory().createArgument(argumentName, generativeInputNode); 
					generativeInputNode.addArgument(argument); 
					this.getMapArgument().put(argumentName, argument); 
					Debug.println(this.getClass(), "-> " + inputIndividual + " -> " + mExpressionIndividual + ": " + HASARGUMENT + " = " + argumentIndividual); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, argument, argumentIndividual.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}		
				
//				/* -> isInnerTermOf */
//				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isInnerTermOf", prefixManager); 			
//				for (OWLIndividual individualTwo : this.getObjectPropertyValues(inputIndividual,objectProperty, ontology) ){
//					String individualTwoName = this.extractName(ontology, individualTwo.asOWLNamedIndividual());
//					
//					INode multiEntityNode = this.getMapLoadedNodes().get(individualTwoName);
//					if (multiEntityNode instanceof MultiEntityNode) {
//						generativeInputNode.addInnerTermFromList((MultiEntityNode)multiEntityNode); 
//						((MultiEntityNode)multiEntityNode).addInnerTermOfList(generativeInputNode); 
//						Debug.println(this.getClass(), "-> " + inputIndividual + ": " + objectProperty + " = " + individualTwo);			
//					} else {
//						Debug.println(this.getClass(), individualTwo + " is not an multi entity node expected by " + objectProperty);
//					}
//				}	
				
				
				ret.put(inputIndividualName, generativeInputNode);	// fill return
			}		
		}
		return ret;
	}

	/**
	 * This method loads the definesUncertaintyOf and MappingArguments (isSubjectIn and isObjectIn).
	 * It requires that the arguments of resident nodes were filled previously.
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadLinkFromResidentNodeToOWLProperty(unbbayes.prs.mebn.ResidentNode, org.semanticweb.owlapi.model.OWLObject, org.semanticweb.owlapi.model.OWLOntology)
	 * @see #loadSimpleArgRelationship(OWLOntology, MultiEntityBayesianNetwork)
	 */
	protected void loadLinkFromResidentNodeToOWLProperty(ResidentNode domainResidentNode, OWLObject owlObject,OWLOntology ontology) {
		if (ontology == null || owlObject == null) {
			System.err.println("Warning: attempted to load \"definesUncertaintyOf\" from object " + owlObject + " in ontology " + ontology);
			return;
		}
		// ignore if owlObject is not a named individual
		if (!(owlObject instanceof OWLIndividual)
				|| !((OWLIndividual)owlObject).isNamed()) {
			System.err.println(owlObject + " != OWLNamedIndividual" );
			return;
		}
		
//		// this method only works if a reasoner is available
//		if (this.getLastOWLReasoner() == null) {
//			throw new NullPointerException("Reasoner == null");
//		}
		
		// extract MEBN
		MultiEntityBayesianNetwork mebn = null;
		try {
			mebn = domainResidentNode.getMFrag().getMultiEntityBayesianNetwork();
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not obtain MEBN node from resident node", e);
		}
		if (mebn == null) {
			throw new IllegalArgumentException("Invalid resident node: MultiEntityBayesianNetwork == null");
		}
		
		// extract default prefix (PR-OWL2)
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// extract MExpression property
		OWLObjectProperty hasMExpression = factory.getOWLObjectProperty(HASMEXPRESSION, prefixManager);
		
		// extract MExpression
		for (OWLIndividual mExpression : this.getObjectPropertyValues(((OWLIndividual)owlObject).asOWLNamedIndividual(), hasMExpression , ontology)) {
			// extract the object property for type of MExpression 
			OWLObjectProperty typeOfMExpression = factory.getOWLObjectProperty(TYPEOFMEXPRESSION, prefixManager);
			
			// extract the type of the MExpression (this should be a random variable)
			for (OWLIndividual randomVariable : this.getObjectPropertyValues(mExpression, typeOfMExpression, ontology)) {
				// TODO maybe we should check if this is really a randomVariable...
				
				// extract definesUncertaintyOfIRI property
				OWLDataProperty definesUncertaintyOf = factory.getOWLDataProperty(DEFINESUNCERTAINTYOF, prefixManager);
				
				// extract value from inferred values
				Collection<OWLLiteral> values = this.getDataPropertyValues(randomVariable, definesUncertaintyOf,ontology);
				OWLLiteral value = null;
				if (values != null && !values.isEmpty()) {
					// use only the first value
					value = values.iterator().next();
				}
				
				// add value to mebn
				if (value != null) {
					// extract IRI
					String iriString = value.getLiteral();
					if (iriString != null && (iriString.trim().length() > 0)) {
						IRIAwareMultiEntityBayesianNetwork.addDefineUncertaintyToMEBN(mebn, domainResidentNode, IRI.create(iriString));
					}
				}
				
				// extract MappingArguments of RV, so that we can map them to ResidentNode's arguments by using IRIAwareMultiEntityBayesianNetwork
				for (OWLIndividual arg : this.getObjectPropertyValues(randomVariable, factory.getOWLObjectProperty(HASARGUMENT, prefixManager), ontology)) {
					
					// the arguments of resident nodes and random variables are synchronized by argument number (i.e. the order of arguments).
					int argNumber = -1;	// this variable will store the argumentNumber of arg
					
					// extract argNumber from arg
					Collection<OWLLiteral> argNumberLiterals = this.getDataPropertyValues(arg, factory.getOWLDataProperty(HASARGUMENTNUMBER, prefixManager), ontology);
					if (argNumberLiterals != null && !argNumberLiterals.isEmpty()) {
						try {
							// use only the 1st argument number (PR-OWL2 definition should not allow more than 1 argument number)
							argNumber = Integer.parseInt(argNumberLiterals.iterator().next().getLiteral());
						} catch (NumberFormatException e) {
							try {
								Debug.println(this.getClass(), e.getMessage() + ". The argument number of " + arg + " could not be converted to an integer.", e);
								// keep trying other arguments...
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
					}
					
					// check argNumber consistency
					if (argNumber < 1) {	// argNumber should start from 1
						try {
							Debug.println(this.getClass(), "The argument " + arg + " of " + randomVariable + " has an invalid argument number " + argNumber);
						} catch (Throwable t) {
							t.printStackTrace();
						}
						continue;	// keep trying other arguments
					}
					
					// extract argument (from resident node) by argNumber
					Argument residentNodeArgument = domainResidentNode.getArgumentNumber(argNumber);
					if (residentNodeArgument == null) {
						try {
							Debug.println(this.getClass(), "INCONSISTENT!!! " + domainResidentNode + " has no argument in position " + argNumber 
									+ ", but its associated RV " + randomVariable + " has " + arg + " in such position. But we'll keep searching other arguments...");
						} catch (Throwable t) {
							t.printStackTrace();
						}
						continue;	// ignore and try other arguments
					}
					
					// We are not interested in whether arg is a MappingArgument or not. 
					// Instead, if it has either isSubjectIn or isObjectIn, there is a mapping from an argument to an owl:property
					
					// extract symbolic links of isSubjectIn
					Collection<OWLLiteral> symbolicLinks = this.getDataPropertyValues(arg, factory.getOWLDataProperty(ISSUBJECTIN, prefixManager), ontology);
					if (symbolicLinks != null ) {
						for (OWLLiteral link : symbolicLinks) {
							// extract IRI
							String iriString = link.getLiteral();
							if (iriString != null && (iriString.trim().length() > 0)) {
								IRIAwareMultiEntityBayesianNetwork.addSubjectToMEBN(mebn, residentNodeArgument, IRI.create(iriString));
							}
						}
					}
					
					// extract symbolic links of isObjectIn
					symbolicLinks = this.getDataPropertyValues(arg, factory.getOWLDataProperty(ISOBJECTIN, prefixManager), ontology);
					if (symbolicLinks != null ) {
						for (OWLLiteral link : symbolicLinks) {
							// extract IRI
							String iriString = link.getLiteral();
							if (iriString != null && (iriString.trim().length() > 0)) {
								IRIAwareMultiEntityBayesianNetwork.addObjectToMEBN(mebn, residentNodeArgument, IRI.create(iriString));
							}
						}
					}
					
				}
				
			}	// for randomVariable
			
		}	// for MExpression
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadOrdinaryVariable(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, OrdinaryVariable> loadOrdinaryVariable(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, OrdinaryVariable> ret = new HashMap<String, OrdinaryVariable>();
		
		OWLClass owlClassOrdinaryVariable = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.ORDINARYVARIABLE, prefixManager); 
		if (!ontology.containsClassInSignature(owlClassOrdinaryVariable.getIRI(), true)) {
			// use the old definition
			owlClassOrdinaryVariable = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.ORDINARY_VARIABLE, prefixManager); 
		}
		
		if (owlClassOrdinaryVariable != null) {
			for (OWLIndividual ordinaryVariableIndividual : this.getOWLIndividuals(owlClassOrdinaryVariable, ontology) ){
				if (!ordinaryVariableIndividual.isNamed()) {
					try {
						Debug.println(this.getClass(), ordinaryVariableIndividual + " ignored because it is anonymous.");
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;	// ignore anonymous
				}
				
				String ordinaryVariableName = this.extractName(ontology, ordinaryVariableIndividual.asOWLNamedIndividual());
				
				INode loadedNode = this.getMapLoadedNodes().get(ordinaryVariableName);
				if (loadedNode == null || !(loadedNode instanceof OrdinaryVariable)){
					throw new IOMebnException(this.getResource().getString("OVariableNotExistsInMTheory"),  "Ovariable = " + ordinaryVariableIndividual); 
				}
				OrdinaryVariable oVariable = (OrdinaryVariable)loadedNode;
				
				Debug.println(this.getClass(), "Ordinary Variable loaded: " + ordinaryVariableIndividual); 				
				
				oVariable.setDescription(this.getDescription(ontology, ordinaryVariableIndividual)); 
				
				/* -> isOrdinaryVariableIn (it was commented because it was only performing consistency check (which is already done by reasoner))  */
//				OWLObjectProperty isOrdinaryVariableIn = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isOrdinaryVariableIn", prefixManager); 			
//				Collection<OWLIndividual> mFragIndividuals = this.getObjectPropertyValues(ordinaryVariableIndividual,isOrdinaryVariableIn, ontology);
//				if (mFragIndividuals != null && !mFragIndividuals.isEmpty()) {
//					// there should be only 1 home MFrag for an ordinary variable. Let's just use the first one
//					OWLIndividual mFragIndividual = mFragIndividuals.iterator().next();
//					MFrag domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, mFragIndividual.asOWLNamedIndividual())); 
//					if(domainMFrag != oVariable.getMFrag()){
//						throw new IOMebnException(this.getResource().getString("isOVariableInError"),  "Ordinary variable = " + ordinaryVariableIndividual); 
//					}
//					Debug.println(this.getClass(), "-> " + ordinaryVariableIndividual + ": " + isOrdinaryVariableIn + " = " + mFragIndividual);			
//				}
				
				/* -> isSubstitutedBy */
				
				OWLDataProperty isSubstitutedBy = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(this.getIsSubsByObjectPropertyName(), prefixManager); 			
				Collection<OWLLiteral> ordinaryVariableTypeLiteral = this.getDataPropertyValues(ordinaryVariableIndividual,isSubstitutedBy, ontology); 	
				if(ordinaryVariableTypeLiteral!= null && !ordinaryVariableTypeLiteral.isEmpty()){
					// an OV should have only 1 type	
					
					// the value of isSubstitutedBy is a text representing URI (usually, a boolean or an owl class)
					IRI linkIRI = IRI.create(this.extractName(ordinaryVariableTypeLiteral.iterator().next())); 
					
					// extract the element actually pointed (it is either a owl class or an owl literal)
					if (ontology.containsClassInSignature(linkIRI, true)) {
						// the ov can be substituted by individuals of OWL classes
						OWLClass pointedObject = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(linkIRI);
						Type type = mebn.getTypeContainer().getType(this.extractName(ontology, pointedObject) + "_label"); 
						if (type == null) {
							// maybe it does not use suffix "_label"
							type = mebn.getTypeContainer().getType(this.extractName(ontology, pointedObject)); 
						}
						if (type != null){
							oVariable.setValueType(type); 
							oVariable.updateLabel(); 
						} else{
							// the pr-owl file is erroneous
							throw new IOMebnException(this.getResource().getString("isOVariableInError"),  ordinaryVariableIndividual + " -> " + isSubstitutedBy + " = " + linkIRI); 
						}
					} else if (ontology.containsDatatypeInSignature(linkIRI, true)) {
						// the ov can be substituted by literals (usually, boolean)
						OWLDatatype pointedObject = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDatatype(linkIRI);
						if (pointedObject.isBoolean()) {
							Type type = mebn.getTypeContainer().getType(this.extractName(ontology, pointedObject)); 
							if (type != null){
								oVariable.setValueType(type); 
								oVariable.updateLabel(); 
							} else{
								// the pr-owl file is erroneous
								throw new IOMebnException(this.getResource().getString("isOVariableInError"),  ordinaryVariableIndividual + " -> " + isSubstitutedBy + " = " + linkIRI); 
							}
						} else {
							throw new IOMebnException(this.getResource().getString("isOVariableInError"),  ordinaryVariableIndividual + " -> " + isSubstitutedBy + " = " + linkIRI);
						}
					}
					
				}
				
				/* isRepBySkolen don't checked */ 
				ret.put(ordinaryVariableName, oVariable);
			}		
		}
		return ret;
	}

	
	/**
	 * This method loads OrdinaryVariableArgument 
	 * @see IPROWL2ModelUser#ORDINARYVARIABLEARGUMENT 
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadSimpleArgRelationship(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, Argument> loadSimpleArgRelationship(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, Argument> ret = new HashMap<String, Argument>();
		
		OWLClass owlClassOrdinaryVariableArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.ORDINARYVARIABLEARGUMENT, prefixManager); 
		for (OWLIndividual ordinaryVariableArgumentIndividual : this.getOWLIndividuals(owlClassOrdinaryVariableArgument,ontology) ){
			if (!ordinaryVariableArgumentIndividual.isNamed()) {
				try {
					Debug.println(this.getClass(), "Ignoring anonymous individual " + ordinaryVariableArgumentIndividual);
				}catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
			
			String ordinaryVariableArgumentName = this.extractName(ontology, ordinaryVariableArgumentIndividual.asOWLNamedIndividual());
			
			// mapArgument was previously initialized by some methods that load nodes (like loadDomainResidentNode)
			Argument argument = this.getMapArgument().get(ordinaryVariableArgumentName); 
			if (argument == null){
				// there may be unknown arguments (e.g. arguments of finding nodes or arguments of random variables that has no correspondent Resident Node)
				System.err.println(this.getResource().getString("ArgumentNotFound") +  ". Argument = " + ordinaryVariableArgumentIndividual); 
				continue;
			}
			Debug.println(this.getClass(), "SimpleArgRelationship loaded: " + ordinaryVariableArgumentIndividual); 
			
			/* -> typeOfArgument  */
			
			OWLObjectProperty typeOfArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(TYPEOFARGUMENT, prefixManager); 			
			Collection<OWLIndividual> instances = this.getObjectPropertyValues(ordinaryVariableArgumentIndividual,typeOfArgument, ontology); 	
			if(!instances.isEmpty()){
				// assume there is only 1 type
				OWLIndividual ordinaryVariableIndividual = instances.iterator().next();
				if (!ordinaryVariableIndividual.isNamed()) {
					Debug.println(this.getClass(), "Ignoring anonymous individual " + ordinaryVariableIndividual) ;
				} else {
					// extract the ordinary variable (getmapFilledOrdinaryVariables was filled previously in loadOrdinaryVariable)
					OrdinaryVariable oVariable = this.getMapFilledOrdinaryVariables().get(this.extractName(ontology, ordinaryVariableIndividual.asOWLNamedIndividual())); 
					if (oVariable == null){
						throw new IOMebnException(this.getResource().getString("ArgumentTermInError"), "Ordinary Variable = " + ordinaryVariableIndividual); 	
					}
					
					try{
						argument.setOVariable(oVariable); 
						argument.setType(Argument.ORDINARY_VARIABLE);
					} catch(Exception e){
						throw new IOMebnException(this.getResource().getString("ArgumentTermInError"),  "Ordinary Variable = " + ordinaryVariableIndividual); 	
					}
					Debug.println(this.getClass(), "-> " + ordinaryVariableArgumentIndividual + ": " + typeOfArgument + " = " + ordinaryVariableIndividual);			
				}
			}
			
			/* -> hasArgumentNumber */
			
			OWLDataProperty hasArgNumber = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("hasArgumentNumber", prefixManager);
	        Collection<OWLLiteral> owlLiterals = this.getDataPropertyValues(ordinaryVariableArgumentIndividual,hasArgNumber, ontology);
	        try {
	        	// assume there is only 1 value
	        	argument.setArgNumber(Integer.parseInt(owlLiterals.iterator().next().getLiteral()));
	        } catch (Exception e) {
	        	// hasArgNumber was null or a non-integer. This is an error, but lets try going on....
	        	Debug.println(this.getClass(), ordinaryVariableArgumentIndividual + " - " + hasArgNumber + " = ERROR");
	        	e.printStackTrace();
			}
			
			
			/* -> isArgumentOf (ignored)  */
			
	        ret.put(ordinaryVariableArgumentName, argument);
		}		
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadContextNode(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, ContextNode> loadContextNode( OWLOntology ontology, MultiEntityBayesianNetwork mebn) 
														throws IOMebnException {

		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, ContextNode> ret = new HashMap<String, ContextNode>();
		
		OWLClass owlClassContextNode = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.CONTEXTNODE, prefixManager); 
		if (!ontology.containsClassInSignature(owlClassContextNode.getIRI(), true)) {
			// use old definition
			owlClassContextNode = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.CONTEXT_NODE, prefixManager); 
		}
		
		if (owlClassContextNode != null) {
			for (OWLIndividual contextNodeIndividual : this.getOWLIndividuals(owlClassContextNode, ontology)){
				
				// find context node (which is already loaded) in loadDomainMFragContents
				ContextNode contextNode = null;
				if (this.getMapLoadedNodes() != null) {
					INode mappedNode = this.getMapLoadedNodes().get(this.extractName(ontology, contextNodeIndividual.asOWLNamedIndividual())); 
					if (mappedNode == null || !(mappedNode instanceof ContextNode)){
						throw new IOMebnException(this.getResource().getString("ContextNodeNotExistsInMTheory"), "Context = " + contextNodeIndividual); 
					}
					contextNode = (ContextNode)mappedNode;
				}
				
				Debug.println(this.getClass(), "Context Node loaded: " + contextNodeIndividual); 				
				
				if (contextNode == null) {
					throw new IOMebnException(this.getResource().getString("ContextNodeNotExistsInMTheory"));
				}
				
				// add comment
				contextNode.setDescription(this.getDescription(ontology, contextNodeIndividual)); 
				
				// load MExpression -> hasMExpression
				OWLObjectProperty hasMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(HASMEXPRESSION, prefixManager);
				
				// extract MExpressions
				Collection<OWLIndividual> mExpressions = this.getObjectPropertyValues(contextNodeIndividual , hasMExpression , ontology);
				
				// nodes are supposed to have at least 1 MExpression (there are axioms forcing exactly 1 MExpression per node)
				if (mExpressions == null || mExpressions.isEmpty()) {
					try {
						System.err.println(this.getResource().getString("ArgumentNotFound") + " : " + contextNodeIndividual);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					// let this context node to be empty and keep loading other nodes.
					continue;
				}
				
				OWLIndividual mExpressionIndividual = mExpressions.iterator().next();
				
				/* mExpression -> hasArgument */
				OWLObjectProperty hasArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(HASARGUMENT, prefixManager); 
				for (OWLIndividual argumentIndividual : this.getObjectPropertyValues(mExpressionIndividual , hasArgument , ontology) ){
					Argument argument = this.getMEBNFactory().createArgument(this.extractName(ontology, argumentIndividual.asOWLNamedIndividual()), contextNode); 
					contextNode.addArgument(argument); 
					this.getMapArgument().put(this.extractName(ontology, argumentIndividual.asOWLNamedIndividual()), argument); 
					try {
						Debug.println(this.getClass(), "-> " + contextNodeIndividual + ": " + hasArgument + " = " + argumentIndividual); 
					} catch (Throwable t) {
						t.printStackTrace();
					}
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, argument, argumentIndividual.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					
					
				}
				
				/* mExpression -> typeOfMExpression */
				
				OWLObjectProperty typeOfMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("typeOfMExpression", prefixManager); 			
				Collection<OWLIndividual> randomVariables = this.getObjectPropertyValues(mExpressionIndividual , typeOfMExpression, ontology); 	
				if(!randomVariables.isEmpty()){
					// mExpression is supposed to have only 1 type (RandomVariable)
					OWLIndividual randomVariableIndividual = randomVariables.iterator().next();
					String rvName = this.extractName(ontology, randomVariableIndividual.asOWLNamedIndividual());
					if(this.getMapBuiltInRV().containsKey(rvName)){
						this.getMapIsContextInstanceOf().put(contextNode, this.getMapBuiltInRV().get(rvName)); 
					} else {
						// RV is not built in. Find node related to it.
						Collection<ResidentNode> nodesPointingToRV = this.getRandomVariableIndividualToResidentNodeSetCache().get(randomVariableIndividual);
						if (nodesPointingToRV == null || nodesPointingToRV.isEmpty()) {
							// MExpression may be pointing directly to a node instead of RV. This is not a valid PR-OWL2 ontology, but let's add backward compatibility to PR-OWL1 ontologies.
							INode auxNode = this.getMapLoadedNodes().get(rvName);
							try {
								Debug.println(this.getClass(), rvName + " may be a node instead of a RV. Found " + auxNode);
							}catch (Throwable t) {
								t.printStackTrace();
							}
							if(auxNode != null && (auxNode instanceof ResidentNode)){
								this.getMapIsContextInstanceOf().put(contextNode, auxNode); 
							} else {
								try {
									Debug.println(this.getClass(), rvName + " was not found at all...");
								} catch (Throwable t) {
									t.printStackTrace();
								}
							}
						} else {
							// use the first one
							// TODO implement polymorphism
							INode auxNode = nodesPointingToRV.iterator().next();
							if(auxNode != null && (auxNode instanceof ResidentNode)){
								this.getMapIsContextInstanceOf().put(contextNode, auxNode); 
							} else {
								try {
									Debug.println(this.getClass(), rvName + " was not found at all...");
								} catch (Throwable t) {
									t.printStackTrace();
								}
							}
						}
					}
					
				}		
				
				ret.put(this.extractName(ontology, contextNodeIndividual.asOWLNamedIndividual()), contextNode); 
				
			}
		}
		
		return ret;
	}

	
	/**
	 * This method was extended so that it loads MExpressionArguments as well.
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadArgRelationship(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, Argument> loadArgRelationship(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{

		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, Argument> ret = new HashMap<String, Argument>();
		
		// TODO extract other types of arguments (e.g. ConstantArgument, MappingArgument and ExemplarArgument)
		
		// TODO ConstantArgument should be searched in order to extract arguments whose type is CategoricalEntity
		
		OWLClass owlClassMExpressionArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.MEXPRESSIONARGUMENT, prefixManager); 
		
		for (OWLIndividual mExpressionArgumentIndividual : this.getOWLIndividuals(owlClassMExpressionArgument, ontology) ){	
			if (!mExpressionArgumentIndividual.isNamed()) {
				try {
					Debug.println(this.getClass(), "Ignoring anonymous individual " + mExpressionArgumentIndividual);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;	// ignore anonymous
			}
			
			// extract 1st level arguments (arguments directly connected to a node) by name
			String argumentName = this.extractName(ontology, mExpressionArgumentIndividual.asOWLNamedIndividual());
			
			Argument argument = this.getMapArgument().get(argumentName); 
			if (argument == null){
				// if an argument is not in getMapArgument, it means that it was not loaded previously.
				// Since we assume all arguments related to nodes were loaded,
				// this is not an argument directly related to an existing node.
				try {
					Debug.println(this.getClass(), this.getResource().getString("ArgumentNotFound") +  " Argument = " + mExpressionArgumentIndividual); 
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;
			}
			
			//  by now, this is a 1st level argument (it is directly referencing a node)
			
			try{
				Debug.println(this.getClass(), "-> MExpressionArgument loaded: " + mExpressionArgumentIndividual); 
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
			/* -> typeOfArgument  */
			OWLObjectProperty typeOfArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(TYPEOFARGUMENT, prefixManager); 			
			
			Collection<OWLIndividual> typesOfArgument = this.getObjectPropertyValues(mExpressionArgumentIndividual,typeOfArgument, ontology);
			if(typesOfArgument.isEmpty()){
				// this is an erroneous state, but let's keep going on.
				try {
					Debug.println(this.getClass(), mExpressionArgumentIndividual + " - " + typeOfArgument + " = null" );
				} catch (Throwable t) {
					t.printStackTrace();
				}
			} else {
				// arguments are supposed to have only 1 type and it is unfortunately a owl:Thing
				OWLIndividual typeOf1stLevelArgument = typesOfArgument.iterator().next();
				if (!typeOf1stLevelArgument.isNamed()) {
					try {
						Debug.println(this.getClass(), "Ignoring anonymous individual " + typeOf1stLevelArgument);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				} else {
					try {
						this.getRecursivelyAddedArgumentsOfMExpression().putAll(this.fillTypeOfArgumentRecursivelly(typeOf1stLevelArgument, argument, ontology, mebn));
					} catch (ArgumentOVariableAlreadySetException e) {
						// Perform Exception translation in order to match signature
						throw new IllegalArgumentException(TYPEOFARGUMENT + " = " + typeOf1stLevelArgument,e);
					} catch (ArgumentNodeAlreadySetException e) {
						// Perform Exception translation in order to match signature
						throw new IllegalArgumentException(TYPEOFARGUMENT + " = " + typeOf1stLevelArgument,e);
					}
				}
			}
			
			/* has Arg Number */
			OWLDataProperty hasArgNumber = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HASARGUMENTNUMBER, prefixManager);
			Collection<OWLLiteral> literals = this.getDataPropertyValues(mExpressionArgumentIndividual, hasArgNumber, ontology);			
			try {
				argument.setArgNumber(Integer.parseInt(literals.iterator().next().getLiteral()));
			} catch (Exception e) {
				// the argnumber was empty or invalid... This is an error, but lets try going on...
				e.printStackTrace();
			}
			
			ret.put(argumentName, argument);
		}
		return ret;
	}
	
	/**
	 * This method handles arguments that loads MExpressions of arguments recursively.
	 * This method differs from {@link #loadArgRelationship(OWLOntology, MultiEntityBayesianNetwork)} because it
	 * creates instances of nodes and arguments instead of reusing them, and because it uses MExpressions which are not
	 * related to random variables or nodes directly.
	 * @param typeOfArgument : this is the type of argument, which may be an MExpression, Constant, OrdinaryVariable, RandomVariable or an Exemplar.
	 * @param argument : argument where typeOfArgument belongs
	 * @param ontology
	 * @param mebn
	 * @return
	 * @throws ArgumentOVariableAlreadySetException = when there are duplicate arguments
	 * @throws ArgumentNodeAlreadySetException  = when argument is a duplicate ordinary variable 
	 */
	protected Map<String, Argument> fillTypeOfArgumentRecursivelly( OWLIndividual typeOfArgument, Argument argument,OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws ArgumentOVariableAlreadySetException, ArgumentNodeAlreadySetException {
		
		Map<String, Argument>ret = new HashMap<String, Argument>();
		
		// use PR-OWL 2 prefix to extract object properties and classes
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		String nameOfTypeOfArgument = this.extractName(ontology, typeOfArgument.asOWLNamedIndividual());
		
		/*  Arguments may be
		 * - MExpression
		 * - Constants : it is not checked in this version
		 * - OrdinaryVariable
		 * - RandomVariable : it is not checked in this version
		 * - Exemplar : it is not checked in this version
		 */

		// check if this is an ordinary variable
		OrdinaryVariable oVariable = this.getMapFilledOrdinaryVariables().get(nameOfTypeOfArgument);
		if( oVariable != null) {
			argument.setOVariable(oVariable);
			argument.setType(Argument.ORDINARY_VARIABLE); 		
			ret.put(nameOfTypeOfArgument, argument);
		} else {
			// check if this is an MExpression
			Boolean isMExpression = null;
			if (this.getLastOWLReasoner() != null) {
				isMExpression = this.getLastOWLReasoner().getTypes(typeOfArgument.asOWLNamedIndividual(), false).containsEntity(
						ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.MEXPRESSION, prefixManager));
			} else {
				isMExpression = typeOfArgument.getTypes(ontology.getOWLOntologyManager().getOntologies()).contains(
						ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.MEXPRESSION, prefixManager));
			}
			if (isMExpression) {
				// this is an MExpression. Create arguments recursively
				// assure this is an argument for a context node
				if (argument.getMultiEntityNode() != null 
						&& (argument.getMultiEntityNode() instanceof ContextNode)) {
					
					// extract typeOfMExpression so that we can use it to fill inner node/argument (i.e. the argumentTerm of argument)
					OWLObjectProperty typeOfMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(TYPEOFMEXPRESSION, prefixManager);
					
					// extract the type of mexpression
					Collection<OWLIndividual> typeOfMExpressionIndividuals = this.getObjectPropertyValues(typeOfArgument, typeOfMExpression, ontology);
					
					// we assume mexpression to have only 1 type
					if (!typeOfMExpressionIndividuals.isEmpty()) {
						
						// extract individual and its name
						OWLIndividual typeOfMExpressionIndividual = typeOfMExpressionIndividuals.iterator().next();
						String nameOfType = this.extractName(typeOfMExpressionIndividual);
						
						// instantiate inner context node
						ContextNode innerNode = this.getMEBNFactory().createContextNode("CX_" + ((int)(Math.random()*10000)) + this.extractName(typeOfArgument.asOWLNamedIndividual()),argument.getMultiEntityNode().getMFrag());
						
						// the type of mexpression may be a RandomVariable (resident node) or a LogicalOperator (BuiltInRV)
						// TODO check by type instead of name
						BuiltInRV builtInRV = this.getMapBuiltInRV().get(nameOfType);
						if (builtInRV != null) {
							// the argument is a built in RV
							
							// set the content of inner node in a map, so that it can be updated in buildFormulaTrees
							this.getMapIsContextInstanceOf().put(innerNode, builtInRV);
							
							argument.setArgumentTerm(innerNode); 
							argument.setType(Argument.CONTEXT_NODE); 
							
						} else {
							// the typeOfMExpressionIndividual is a RandomVariable (usually connected to a resident node)
							
							// extract the resident node from a random variable (assumed to be only 1)
							// TODO implement polymorphism (stop assuming that there is only 1 resident node for a random variable)
							Collection<OWLIndividual> residentNodeIndividuals = this.getResidentNodeIndividualsFromRandomVariable(nameOfType, ontology, 1);
							if (residentNodeIndividuals != null && !residentNodeIndividuals.isEmpty()) {
								// use the first resident node
								INode auxNode = this.getMapLoadedNodes().get(this.extractName(ontology, residentNodeIndividuals.iterator().next().asOWLNamedIndividual()));
								if(auxNode != null && (auxNode instanceof ResidentNode)){
									this.getMapIsContextInstanceOf().put(innerNode, auxNode); 
									argument.setArgumentTerm(innerNode); 
									argument.setType(Argument.RESIDENT_NODE);  
								}
							}
							
						}
						
						// solve arguments recursively
						/* mExpression -> hasArgument */
						OWLObjectProperty hasArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(HASARGUMENT, prefixManager); 
						for (OWLIndividual argumentIndividual : this.getObjectPropertyValues(typeOfArgument , hasArgument , ontology) ){
							Argument innerArgument = this.getMEBNFactory().createArgument(this.extractName(ontology, argumentIndividual.asOWLNamedIndividual()), innerNode); 
							innerNode.addArgument(innerArgument); 
							this.getMapArgument().put(this.extractName(ontology, argumentIndividual.asOWLNamedIndividual()), innerArgument); 
							try {
								Debug.println(this.getClass(), "-> " + typeOfArgument + ": " + hasArgument + " = " + argumentIndividual); 
							} catch (Throwable t) {
								t.printStackTrace();
							}
							
							try {
								IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, innerArgument, argumentIndividual.asOWLNamedIndividual().getIRI());
							} catch (Exception e) {
								e.printStackTrace();
							}
							
							// extract type of inner argument
							OWLObjectProperty typeOfArgumentProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(TYPEOFARGUMENT, prefixManager);
							Collection<OWLIndividual> typesOfInnerArgument = this.getObjectPropertyValues(argumentIndividual, typeOfArgumentProperty, ontology);
							// assume there is only 1 type
							if (!typesOfInnerArgument.isEmpty()) {
								// solve arguments recursively
								ret.putAll(this.fillTypeOfArgumentRecursivelly(typesOfInnerArgument.iterator().next(), innerArgument, ontology, mebn));
							} else {
								System.err.println("There is an argument with no type: " + argumentIndividual);
							}
						}
					}
				}
			} else {
				System.err.println("This type of argument is not supported yet: " + typeOfArgument);
			}
		}	// end of else of if(oVariable != null)
		
		
		return ret;
	}
	
	/**
	 * Uses the relationship "RandomVariable -[isTypeOfMExpression]-> MExpression -[isMExpressionOf]-> ResidentNode"
	 * in order to find a list of resident nodes from the name of a random variable.
	 * In Manchester OWL Syntax, this query is "DomainResidentNode that hasMExpression some (typeOfMExpression value [nameOfRandomVariable])".
	 * It will attempt to use a reasoner if {@link #getLastOWLReasoner()} is not null.
	 * @param nameOfRandomVariable : name of the random variable
	 * @param ontology : ontology to search
	 * @param countToStop : for optimization, this method will stop looking for more individuals if this number is reached.
	 * This is not a hard limit on the size of the list to be returned, because implementations may ignore this number.
	 * @return list of OWL individuals representing the resident nodes related to the specified random variable
	 * @see #getLastOWLReasoner()
	 */
	protected Collection<OWLIndividual> getResidentNodeIndividualsFromRandomVariable( String nameOfRandomVariable, OWLOntology ontology, int countToStop) {
		if (getLastOWLReasoner() != null) {
			// search for resident node by using a reasoner
			return this.getOWLIndividuals(this.parseExpression("DomainResidentNode that hasMExpression some (typeOfMExpression value " + nameOfRandomVariable + ")"), ontology);
		} else {
			if (countToStop <= 0) {
				return Collections.emptySet();
			}
			// if we did never use a reasoner, then the expression parser would not be able to correctly parse the above expression anyway, so we need to search manually.
			
			// Prepare objects of OWL API so that we can manually search for the resident node
			OWLOntologyManager manager = ontology.getOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();
			
			// extract the owl properties that will connects RandomVariables to ResidentNodes
			OWLObjectPropertyExpression typeOfMExpression = factory.getOWLObjectProperty(TYPEOFMEXPRESSION, getDefaultPrefixManager());	// MExpression to RandomVariable
			OWLObjectPropertyExpression isMExpresionOf = factory.getOWLObjectProperty(ISMEXPRESSIONOF, getDefaultPrefixManager());	// MExpression to ResidentNode
			
			// obtain the owl individual that represents the random variable that we are looking for
			OWLIndividual randomVariable = factory.getOWLNamedIndividual(nameOfRandomVariable, getOntologyPrefixManager(ontology));
			
			// we need instances of MExpression. Additionally, all its direct subclasses are also needed, for backward compatibility.
			Set<OWLClassExpression> mExpressionClasses = new HashSet<OWLClassExpression>();	// OWLClassExpression is a super-interface of OWLClass
			OWLClass mExpressionClass = factory.getOWLClass(MEXPRESSION, getDefaultPrefixManager()); // the MExpresion class
			mExpressionClasses.add(mExpressionClass);	
			mExpressionClasses.addAll(mExpressionClass.getSubClasses(manager.getOntologies()));	// all direct subclasses of MExpression. This is probably SimpleMExpression and BooleanMExpression
			
			// get all individuals of resident node that has MExpressions that has hasTypeOfMExpression == <name of the type>.
			Set<OWLIndividual> residentNodeIndividuals = new HashSet<OWLIndividual>();	// this will hold the individuals of resident node
			for (OWLClassExpression owlClass : mExpressionClasses) {
				if (!owlClass.isAnonymous()) {	// only consider named classes
					// find individuals that matches with the criteria 
					for (OWLIndividual mExpressionIndividual : this.getOWLIndividuals(owlClass, ontology)) {
						// if this individual satisfies [mExpressionIndividual] -> typeOfMExpression -> [randomVariable], then it satisfies constraint
						if (this.getObjectPropertyValues(mExpressionIndividual, typeOfMExpression, ontology).contains(randomVariable) ) {
							residentNodeIndividuals.addAll(this.getObjectPropertyValues(mExpressionIndividual, isMExpresionOf, ontology));
							if (residentNodeIndividuals.size() >= countToStop) {
								return residentNodeIndividuals;
							}
						}
					}
				}
			}
			return residentNodeIndividuals;
		}	// end of the case when we don't have any reasoner to use
		
		// unreachable code
//		return null;
	}



	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.MebnIO#saveMebn(java.io.File, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	public void saveMebn(File file, MultiEntityBayesianNetwork mebn) throws IOException, IOMebnException {
		// TODO update IRIAwareMEBN, because user may want to keep editting mebn after save
		// assertions
		if (mebn == null ) {
			throw new IOException(this.getResource().getString("MTheoryNotExist"));
		}
		if (file == null) {
			throw new IOException(this.getResource().getString("ErrorReadingFile") + " : file == null");
		}
		
		// check mebn name consistency
		if (mebn.getName() == null ) {
			throw new IllegalArgumentException("Invalid name: null");
		}
		
		// use matcher to check format
		Matcher matcher = Pattern.compile("[a-zA-Z_0-9]*").matcher(mebn.getName());
		if (!matcher.matches()) {
			throw new IllegalArgumentException("Invalid name: " + mebn.getName());
		}
		
		// ontology where MEBN will be stored
		OWLOntology ontology = null;
		
		// extract storage implementor of MEBN 
		// (a reference to an object that loaded/saved the mebn last time - such object is likely to contain a reference to owl ontology)
		if (mebn.getStorageImplementor() != null  && mebn.getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator) {
//			try {
//				// extract reasoner from storage implementor 
//				this.setLastOWLReasoner(((IOWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getOWLReasoner());
//			} catch (Throwable t) {
//				Debug.println(this.getClass(), "A reasoner is not so important to save an ontology, so we can ignore errors on extracting reasoners.", t);
//			}
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
		
		// do not allow current ontology to use pr-owl (1) URI
		if (ontology.getOntologyID().getOntologyIRI().toURI().toString().equalsIgnoreCase(OLD_PROWL_NAMESPACEURI)) {
			try {
				Debug.println(this.getClass(), ontology + " is using PR-OWL URI as its ontology URI. This is going to be changed, because it may cause some errors.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			OWLOntologyChange updateURI = new SetOntologyID(ontology, new OWLOntologyID(IRI.create(file)));
			ontology.getOWLOntologyManager().applyChange(updateURI);
			try {
				Debug.println(this.getClass(), "Ontology IRI changed to " + ontology);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		// force ontology to delegate requisitions by adding a mapper (usually, it will redirect PR-OWL2 IRIs to a local file)
		ontology.getOWLOntologyManager().removeIRIMapper(this.getProwl2DefinitionIRIMapper());	// just to avoid duplicate mapper
		ontology.getOWLOntologyManager().addIRIMapper(this.getProwl2DefinitionIRIMapper());

		
		// Check if PR-OWL 2 definition is imported.
		OWLImportsDeclaration importsPROWL2 = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLImportsDeclaration(IRI.create(IPROWL2ModelUser.PROWL2_NAMESPACEURI));
		if (!ontology.getOWLOntologyManager().getImportsClosure(ontology).contains(ontology.getOWLOntologyManager().getImportedOntology(importsPROWL2))) {
			// add the import declaration to the ontology.
			ontology.getOWLOntologyManager().applyChange(new AddImport(ontology, importsPROWL2));
		}
		
		
		// clear PR-OWL2 individuals (so that we can add only the ones which were not deleted from ontology during MEBN edition)
		this.clearAllPROWLOntologyObjects(ontology, mebn);
		
		// do actual save (i.e. fill the ontology object with mebn contents)
		
		/* Definitions */
		// definitions will not be touched, because they must be directly edited in Ontology editor
		this.saveBooleanRVStates(mebn,ontology); 
		this.saveBuiltInRV(mebn,ontology);  
		try {
			Debug.println(this.getClass(), "The definitions were successfully saved. ");
		} catch (Throwable t) {
			t.printStackTrace();
		}

		/* Entities as OWL classes and individuals */
		// entities will not be touched, because they must be directly edited in Ontology editor
		this.saveObjectEntitiesClasses(mebn,ontology); 
		this.saveMetaEntities(mebn,ontology);
		this.saveEntityIndividuals(mebn,ontology);
		this.saveCategoricalStates(mebn,ontology); 
		try {
			Debug.println(this.getClass(), "The entities were successfully saved. ");
		} catch (Throwable t) {
			t.printStackTrace();
		}

		/* MTheory (and MFrag) */
		this.saveMTheoryAndMFrags(mebn,ontology); 
		
		/* MFrag content */
		this.saveDomainResidentNodes(mebn,ontology); 
		this.saveContextNodes(mebn,ontology); 
		this.saveGenerativeInputNodes(mebn,ontology); 
		
		// save the ontology itself as owl xml file
		try {
			ontology.getOWLOntologyManager().saveOntology(ontology, new OWLXMLOntologyFormat(), IRI.create(file));
		} catch (OWLOntologyStorageException e) {
			// perform exception translation to match method signature
			throw new IOMebnException(e);
		}
		
		// mark it as the last used ontology
		this.setLastOWLOntology(ontology);
		
	}


	/**
	 * This method fills the ontology with individuals of entities (e.g. individuals of non-PR-OWL2 classes).
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : object representing OWL ontology, where MEBN data will be filled in.
	 */
	protected void saveEntityIndividuals(MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		// this is the prefix of PR-OWL2 definitions
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();
		
		// this is the prefix of this ontology
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);
		
		
		// auxiliary variables
		OWLIndividual individual = null;
		OWLClass currentOWLEntity = null;
		// extracts all entities found inside this MTheory
		for (ObjectEntity entity : mebn.getObjectEntityContainer().getListEntity()) {
			 currentOWLEntity = this.getObjectEntityClassesCache().get(entity).asOWLClass();
			 // ignore owl:Thing, because it is too generic (and it represents actually everything)
			 if (currentOWLEntity.isOWLThing()) {
				 continue;
			 }
			 if (currentOWLEntity != null) {
				 // create OWL individuals (or reuse them) for each object entity instance found for that entity
				 for ( ObjectEntityInstance entityInstance : entity.getInstanceList()) {
					 // ignore entityInstances that were loaded from this ontology (we are not going to save them twice)
					 IRI individualIRIOnLoadTime = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(mebn, entityInstance);
					 if (individualIRIOnLoadTime != null && ontology.containsIndividualInSignature(individualIRIOnLoadTime, true)) {
						try {
							Debug.println(this.getClass(), entityInstance + " is already included in " + ontology + ". IRI = " + individualIRIOnLoadTime);
						} catch (Throwable t) {
							t.printStackTrace();
						}
						continue;
					 }
					 // extract/create individuals
					 individual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(entityInstance.getName(), currentPrefix);
					 // if it does not exist, create it as an individual of entity
					 if (!ontology.containsIndividualInSignature(individual.asOWLNamedIndividual().getIRI(), true)) {
						 try {
							Debug.println(this.getClass(), individual + ", and individual of " + currentOWLEntity + " is going to be added into " + ontology);
						} catch (Throwable t) {
							t.printStackTrace();
						}
						 // create axiom asserting that individual's type is currentOWLEntity and commit change
						 ontology.getOWLOntologyManager().addAxiom(	// add axiom and commit change
								 ontology, 	// ontology where axiom will be inserted
								 ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(currentOWLEntity, individual)	// individual's type is currentOWLEntity
						 );
					 }
					 // hasType is not required anymore
//					 individual.addPropertyValue( owlModel.getOWLObjectProperty("hasType"),
//												 this.mapMetaEntity.get(entityInstance.getType().getName()));
					 // fill required properties (e.g. hasUID)
					 try {
						 ontology.getOWLOntologyManager().addAxiom(	// add axiom and commit
								 ontology, 	// where to add axiom
								 ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(	// axiom to add datatype property to an individual
										 ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IPROWL2ModelUser.HASUID, prowl2Prefix), // get hasUID datatype property
										 individual, 					// individual having the hasUID property
										 "!" + entityInstance.getName()	// value
								 )
						 );
					 } catch (Exception e) {
						 // Current version of this IO should work with or without the unique ID
						 try {
							 Debug.println(this.getClass(), "Could not set UID for " + individual + " : " + e.getMessage(), e);
						 } catch (Throwable t) {
							 t.printStackTrace();
						 }
					 }
				}
			 }
		}
	}

	/**
	 * This method fills the ontology with categorical state entities (e.g. individuals of OWL classes)
	 * and saves them if they are not present in ontology.
	 * It uses individuals of owl:Thing to represent categorical elements having no equivalent
	 * object entity.
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : object representing OWL ontology, where MEBN data will be filled in.
	 */
	protected void saveCategoricalStates(MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		
		// this is the current ontology's prefix
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);
		
		// iterate over categorical entities
		for(CategoricalStateEntity entity: mebn.getCategoricalStatesEntityContainer().getListEntity()){
			if(entity.getName().equalsIgnoreCase("absurd")) {
				// ignore absurd, because it is implicitly added by default
				continue;
			} 
			IRI iriWhenLoaded = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(mebn, entity);
			if (iriWhenLoaded != null && ontology.containsIndividualInSignature(iriWhenLoaded, true)) {
				try {
					Debug.println(this.getClass(), "Categorical state " + entity + " is already present in " + ontology
							+ ", thus it will not be saved again. IRI = " + iriWhenLoaded);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				// update cache anyway
				this.getCategoricalStatesCache().put(entity, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iriWhenLoaded)); 
			} else {
				// This is a new individual. Get it.
				OWLIndividual categoricalStateIndividual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(entity.getName(), currentPrefix);
				// if individual does not exist, create axiom and add it to ontology
				if (!ontology.containsIndividualInSignature(categoricalStateIndividual.asOWLNamedIndividual().getIRI(), true)) {
					ontology.getOWLOntologyManager().addAxiom(	// add axiom and commit
							ontology, // where to add axiom
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom ( // associate individual and its type
									ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing(), // class = owl:Thing
									categoricalStateIndividual	// individual having owl:Thing as its type
							)
					);
				}
				// update cache
				this.getCategoricalStatesCache().put(entity, categoricalStateIndividual); 
			}
		}
		
	}

	/**
	 * This method fills the ontology with Entities (e.g. OWL classes and individuals).
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : object representing OWL ontology, where MEBN data will be filled in.
	 */
	protected void saveObjectEntitiesClasses(MultiEntityBayesianNetwork mebn,OWLOntology ontology) {
		// assertion
		if (mebn == null || ontology == null) {
			try {
				Debug.println(this.getClass(), "Invalid arguments for saveObjectEntitiesClasses: mebn = " + mebn + ", ontology = " + ontology  );
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return;
		}
		
		// this is the prefix of this ontology
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// iterate over entities
		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()) {
			if (entity == null) {
				continue;	// ignore null
			}
			// check if entity is new or it it was loaded from the current ontology (we do not need to save to this ontology the classes loaded from this ontology)
			IRI entityIRIWhenLoaded = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(mebn, entity);
			if (entityIRIWhenLoaded != null && ontology.containsClassInSignature(entityIRIWhenLoaded, true)) {
				// do not add entity if it is already present in ontology. 
				try {
					Debug.println(this.getClass(), entity + " is already in this ontology, so it is not going to be saved again. IRI = " + entityIRIWhenLoaded);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				// just update cache
				this.getObjectEntityClassesCache().put(entity, factory.getOWLClass(entityIRIWhenLoaded)); 
				continue;
			}
			
			// the entity is new and must be saved.
			
			// MEBN entities are represented as OWL classes. Get them
			OWLClass newEntityClass = null;
			if ("Thing".equalsIgnoreCase(entity.getName())) {
				// Entity with "Thing" as its name must be considered as an owl:Thing
				newEntityClass = factory.getOWLThing();
				// update cache
				this.getObjectEntityClassesCache().put(entity, newEntityClass); 
				continue;
			} else {
				newEntityClass = factory.getOWLClass(entity.getName(), currentPrefix);
			}
			// check if class exists. If not, create it.
			if (!ontology.containsClassInSignature(newEntityClass.getIRI(), true)
					&& !newEntityClass.isOWLThing() ) {		// just in case of string comparison failing to detect owl:Thing...
				// create an axiom explicitly specifying that newEntityClass is an owl:Thing, and commit axiom.
				ontology.getOWLOntologyManager().addAxiom(	// add axiom and commit
						ontology, 	// ontology where axiom will be added
						factory.getOWLSubClassOfAxiom(	// create "subClassOf" axiom
								newEntityClass, 	// subclass
								factory.getOWLThing()	// superclass = owl:Thing
						)
				);
			}
			
			// update cache
			this.getObjectEntityClassesCache().put(entity, newEntityClass); 
		}
	}

	/**
	 * This method fills {@link #getMetaEntityCache()} with correct values.
	 * It must be called after {@link #saveObjectEntitiesClasses(MultiEntityBayesianNetwork, OWLOntology)}
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : this parameter will be ignored.
	 * @deprecated meta entities are not used in PR-OWL2 definitions anymore
	 */
	protected void saveMetaEntities(MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		// ontology is ignored, because it will not be updated		
		
		// fill cache with default types (at least XSD:boolean is necessary)
		this.getMetaEntityCache().put(TypeContainer.typeBoolean.getName() , OWL2Datatype.XSD_BOOLEAN.getURI().toString());
		
		// fill cache with user-provided types
		for( Type type : mebn.getTypeContainer().getListOfTypes() ) {
			if (TypeContainer.typeBoolean.equals(type)) {
				// ignore boolean, because it was already added
				continue;
			}
			try {
				// obtain the Entity related to type
				ObjectEntity objectEntity = mebn.getObjectEntityContainer().getObjectEntityByType(type);
				if (objectEntity == null) {
					try {
						Debug.println(this.getClass(), type + " was not considered an ObjectEntity, so it's going to be ignored in meta-entity's cache.");
					} catch (Throwable t) {
						t.printStackTrace();
					}
					continue;
				}
				// getMetaEntityCache maps the name of type to a string representation of the URI of an Entity related to such type
				this.getMetaEntityCache().put(
						type.getName(), 	// key of getMetaEntityCache
						this.getObjectEntityClassesCache().get(objectEntity).asOWLClass().getIRI().toURI().toString()	// use cache to extract URI of entity
				); 	
			} catch (NullPointerException e) {
				throw new IllegalArgumentException("No URI for the Entity related to " + type + " was found.", e);
			}
		}	
		
	}

	/**
	 * This method fills the cache with built in RV data. We need to fill this cache because
	 * the cache works as a map from MEBN built in RVs to PR-OWL2 built in RVs (which may)
	 * have different names. 
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : ontology containing built in rv individuals
	 */
	protected void saveBuiltInRV(MultiEntityBayesianNetwork mebn, OWLOntology ontology) { 
		// prefix of PR-OWL2 definition
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// TODO avoid hard coding like this
		
		this.getBuiltInRVCache().put("and", factory.getOWLNamedIndividual("and", prowl2Prefix));
		this.getBuiltInRVCache().put("equalto", factory.getOWLNamedIndividual("equalTo", prowl2Prefix));	// the case is different
		this.getBuiltInRVCache().put("iff", factory.getOWLNamedIndividual("iff", prowl2Prefix));
		this.getBuiltInRVCache().put("implies", factory.getOWLNamedIndividual("implies", prowl2Prefix));
		this.getBuiltInRVCache().put("not", factory.getOWLNamedIndividual("not", prowl2Prefix));
		this.getBuiltInRVCache().put("or", factory.getOWLNamedIndividual("or", prowl2Prefix));
		this.getBuiltInRVCache().put("exists", factory.getOWLNamedIndividual("exists", prowl2Prefix));
		this.getBuiltInRVCache().put("forAll", factory.getOWLNamedIndividual("forAll", prowl2Prefix));	// the case is different
	}
	
	/**
	 * This method fills the ontology with boolean RV data.
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : object representing OWL ontology, where MEBN data will be filled in.
	 */
	protected void saveBooleanRVStates(MultiEntityBayesianNetwork mebn,
			OWLOntology ontology) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * This method fills the ontology with input node's data.
	 * This version does not allow complex input nodes yet, so it uses only SimpleMExpression
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : object representing OWL ontology, where MEBN data will be filled in.
	 */
	protected void saveGenerativeInputNodes(MultiEntityBayesianNetwork mebn, OWLOntology ontology) {

		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);
		
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
    	for(MFrag mfrag : mebn.getDomainMFragList()){
    		for (InputNode generativeInputNode: mfrag.getInputNodeList()){  
    			OWLIndividual generativeInputNodeIndividual = this.getGenerativeInputCache().get(generativeInputNode);	
    			
    			// get MExpression related to generativeInputNode. Create axiom if it does not exist
    			OWLIndividual mExpressionIndividual = factory.getOWLNamedIndividual(IPROWL2ModelUser.MEXPRESSION_PREFIX + generativeInputNode.getName(), currentPrefix);
    			
    			// assert that MExpression exists.
    			if (!ontology.containsIndividualInSignature(mExpressionIndividual.asOWLNamedIndividual().getIRI(), true)) {
    				// add axiom to create mExpressionIndividual as individual of SimpleMExpression
    				ontology.getOWLOntologyManager().addAxiom(
    						ontology, 
    						factory.getOWLClassAssertionAxiom(	 
    								factory.getOWLClass(IPROWL2ModelUser.MEXPRESSION, prowl2Prefix), 
    								mExpressionIndividual
    						)
    				);
    				// we cannot use SimpleMExpression because some input nodes may have compound arguments 
    			}
    			
    			// hasMExpression
    			
    			// Add and commit axiom which asserts generativeInputNode -> hasMExpressionProperty -> mExpressionIndividual
    			ontology.getOWLOntologyManager().addAxiom (	// add axiom and commit
    					ontology, // where to add axiom
    					factory.getOWLObjectPropertyAssertionAxiom( // create axiom linking 2 individuals with a property
    							factory.getOWLObjectProperty(this.getHasMExpressionPropertyName(), prowl2Prefix), // hasMExpression
    							generativeInputNodeIndividual, 	// subject
    							mExpressionIndividual	// object
						)
				);
    			
    			// add axiom: mExpressionIndividual -> isMExpressionOf -> generativeInputNodeIndividual
    			ontology.getOWLOntologyManager().addAxiom(
    					ontology, // where to add axiom
    					factory.getOWLObjectPropertyAssertionAxiom(
    							factory.getOWLObjectProperty(this.getIsMExpressionOfPropertyName(), prowl2Prefix), //isMExpressionOf 
    							mExpressionIndividual, 	// subject
    							generativeInputNodeIndividual	// object
						)
				);
    			
    			/* has Argument */
    			if (generativeInputNode.getInputInstanceOf() != null){
    				if(generativeInputNode.getInputInstanceOf() instanceof ResidentNode){
    					ResidentNodePointer pointer = generativeInputNode.getResidentNodePointer(); 
    					OrdinaryVariable[] ovArray = pointer.getOrdinaryVariableArray(); 
    					for(int i = 0; i < ovArray.length; i++){
    						this.saveOrdinaryVariableArgument( ovArray[i],mExpressionIndividual, generativeInputNode, i + 1,mebn,ontology); 
    					}
    				} else{
    					//TODO instanceof BuiltInRV
    				}
    			}
    			
    			/* has Possible Values */
    			
    			if (generativeInputNode.getInputInstanceOf() != null){
    				this.saveInputPossibleValues(mExpressionIndividual, generativeInputNode, mebn, ontology); 
    			}
    			
    			// comments are already included in saveMTheoryAndMFrags
    		}
    	}

	}

	/**
	 * This method handles the possible values of an input node. This is done basically
	 * by linking an input node to an existing random variable (e.g. a random variable
	 * referenced by an resident node or an built-in node).
	 * If the input node points to a single resident node (this is the default), 
	 * it uses the possible values of the pointed resident node.
	 * It assumes {@link #getCategoricalStatesCache()} and {@link #getRandomVariableCache()} are already filled.
	 * This version does not handle 
	 * @param mExpressionIndividual : individual (of MExpression) to be updated. This MExpression is linked to
	 * a individual of GenerativeInputNode by isMExpressionOf object property.
	 * @param generativeInputNode : this is the input node being read
	 * @param mebn : object containing residentNode and generativeInputNode
	 * @param ontology : object containing generativeInputNodeIndividual
	 */
	protected void saveInputPossibleValues(OWLIndividual mExpressionIndividual, 
			InputNode generativeInputNode, MultiEntityBayesianNetwork mebn, OWLOntology ontology) {

		// extract prefixes
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();
		
		// extract factory to get axioms and other references to owl elements
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// check if input node is pointing to a single resident node, and if so, extract the resident node
		if( (generativeInputNode.getInputInstanceOf() != null ) && (generativeInputNode.getInputInstanceOf() instanceof ResidentNode) ){
			// this is pointing to a single resident node, so extract it
			ResidentNode residentNode = (ResidentNode)generativeInputNode.getInputInstanceOf(); 
			// use cache to extract the random variable related to the resident node
			OWLIndividual randomVariableIndividual = this.getRandomVariableCache().get(residentNode);	
			if (randomVariableIndividual == null) {
				throw new IllegalStateException("This method expects the random variable of " + residentNode + " to be cached, but it is not.");
			}
			// link mExpressionIndividual (input node) to the extracted random variable (resident). This should automatically input node's type
			ontology.getOWLOntologyManager().addAxiom(
					ontology, 
					factory.getOWLObjectPropertyAssertionAxiom(
							factory.getOWLObjectProperty(TYPEOFMEXPRESSION, prowl2Prefix), 
							mExpressionIndividual, 
							randomVariableIndividual
					)
			);
			// link inverse object property as well
			ontology.getOWLOntologyManager().addAxiom(
					ontology, 
					factory.getOWLObjectPropertyAssertionAxiom(
							factory.getOWLObjectProperty(ISTYPEOFMEXPRESSION, prowl2Prefix), 
							randomVariableIndividual,
							mExpressionIndividual 
					)
			);
		} else {
			//TODO handle Built-in or complex FOL expressions... 
			try {
				Debug.println(this.getClass(), generativeInputNode + " is not pointing to a single resident node. This version is going to ignore such input nodes nodes.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} 
	}


//	/**
//	 * This is an utility method that extracts the possible values from an input node.
//	 * This may become particularly interesting when an input node may either point to a single
//	 * resident node (in this case, the possible values are inherent from the resident node being pointed)
//	 * or complex FOL expressions (in this case, the input node may be an Entity or boolean).
//	 * @param generativeInputNode
//	 * @return a collection of possible values or an empty collection if nothing was extracted. The returned collection is not
//	 * a direct reference to resident node's states, so changing its values should not affect the original nodes.
//	 */
//	protected Collection<Entity> extractPossibleStatesFromInputNode( InputNode generativeInputNode) {
//		Collection<Entity> ret = new ArrayList<Entity>();	// use list so that the order is not impacted
//		
//		// check if input node is pointing to a single resident node, and if so, extract the resident node
//		ResidentNode residentNode = null; 
//		if( (generativeInputNode.getInputInstanceOf() != null ) && (generativeInputNode.getInputInstanceOf() instanceof ResidentNode) ){
//			residentNode = (ResidentNode)generativeInputNode.getInputInstanceOf();
//		} else {
//			//TODO handle Built-in or complex FOL expressions... 
//			try {
//				Debug.println(this.getClass(), generativeInputNode + " is not pointing to a single resident node. This version is going to ignore such input nodes nodes.");
//			} catch (Throwable t) {
//				t.printStackTrace();
//			}
//		}
//		// only save states if input node is pointing to a single resident node
//		if (residentNode != null) {
//			// Although ResidentNode does not return direct references to the list of possible states (because such reference is a list of StateLink),
//			// we do not want to return residentNode.getPossibleValueList() directly, because subclasses of ResidentNode may behave differently
//			ret.addAll(residentNode.getPossibleValueList());	
//		}
//		
//		return ret;
//	}



	/**
	 * This method fills the ontology with context node's data.
	 * This method expects that {@link #getContextCache()} is filled.
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : object representing OWL ontology, where MEBN data will be filled in.
	 * @see #saveMTheoryAndMFrags(MultiEntityBayesianNetwork, OWLOntology)
	 */
	protected void saveContextNodes(MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();	// prefix for PR-OWL2 definitions
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);	// prefix for this ontology
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// iterate on context nodes of each mfrag
    	for(MFrag mfrag: mebn.getDomainMFragList()){
    		for (ContextNode contextNode: mfrag.getContextNodeList()){
    			// extract from cache
    			OWLIndividual contextNodeIndividual = this.getContextCache().get(contextNode);	
    			
    			// extract formula and save it
    			NodeFormulaTree formulaNode = contextNode.getFormulaTree(); 
    			if (formulaNode != null){
    				// create individual of mexpression in current ontology
    				OWLIndividual mExpressionIndividual = factory.getOWLNamedIndividual(MEXPRESSION_PREFIX + contextNode.getName(), currentPrefix);
    				// assure that individual exists
    				ontology.getOWLOntologyManager().addAxiom(
    						ontology, // where to add axiom
    						factory.getOWLClassAssertionAxiom( // type of mExpressionIndividual is MExpression
    								factory.getOWLClass(MEXPRESSION, prowl2Prefix), 
    								mExpressionIndividual
    						)
    				);
    				// link individual to context node using hasMExpression
    				ontology.getOWLOntologyManager().addAxiom(
    						ontology, 
    						factory.getOWLObjectPropertyAssertionAxiom(
    								factory.getOWLObjectProperty(HASMEXPRESSION, prowl2Prefix), 
    								contextNodeIndividual, 
    								mExpressionIndividual
    						)
    				);
    				// inverse property of hasMExpression is isMExpressionOf
    				ontology.getOWLOntologyManager().addAxiom(
    						ontology, 
    						factory.getOWLObjectPropertyAssertionAxiom(
    								factory.getOWLObjectProperty(ISMEXPRESSIONOF, prowl2Prefix), 
    								mExpressionIndividual,
    								contextNodeIndividual
    						)
    				);
    				// fill mExpression with FOL expressions
    				this.saveNodeFormula(formulaNode, mExpressionIndividual, contextNode, mebn, ontology); 
    			}		
    			
    			// save possible values
    			this.saveContextPossibleValues(contextNodeIndividual, contextNode, mebn, ontology);
    			
    			// comments were already saved in saveMTheoryAndMFrags
    		}		
    	}
	}
	
	/**
	 * This method is just a placeholder. In PR-OWl2, we do not
	 * need to explicitly define the possible values of a context node
	 * or input node, because it inherits it from the random variable.
	 * 
	 * @param contextNodeIndividual The individual owl for the node
	 * @param contextNode The node that have the possible values
	 * @param mebn
	 * @param ontology
	 */
    protected void saveContextPossibleValues(OWLIndividual contextNodeIndividual,
			ContextNode contextNode, MultiEntityBayesianNetwork mebn,
			OWLOntology ontology) {
//    	// check if context node is pointing to a single resident node, and if so, extract the resident node
//		if( (generativeInputNode.getInputInstanceOf() != null ) && (generativeInputNode.getInputInstanceOf() instanceof ResidentNode) ){
//			// this is pointing to a single resident node, so extract it
//			ResidentNode residentNode = (ResidentNode)generativeInputNode.getInputInstanceOf(); 
//			// use cache to extract the random variable related to the resident node
//			OWLIndividual randomVariableIndividual = this.getRandomVariableCache().get(residentNode);	
//			if (randomVariableIndividual == null) {
//				throw new IllegalStateException("This method expects the random variable of " + residentNode + " to be cached, but it is not.");
//			}
//			// link mExpressionIndividual (input node) to the extracted random variable (resident). This should automatically input node's type
//			ontology.getOWLOntologyManager().addAxiom(
//					ontology, 
//					factory.getOWLObjectPropertyAssertionAxiom(
//							factory.getOWLObjectProperty(TYPEOFMEXPRESSION, prowl2Prefix), 
//							mExpressionIndividual, 
//							randomVariableIndividual
//					)
//			);
//			// link inverse object property as well
//			ontology.getOWLOntologyManager().addAxiom(
//					ontology, 
//					factory.getOWLObjectPropertyAssertionAxiom(
//							factory.getOWLObjectProperty(ISTYPEOFMEXPRESSION, prowl2Prefix), 
//							randomVariableIndividual,
//							mExpressionIndividual 
//					)
//			);
//		} else {
//			//TODO handle Built-in or complex FOL expressions... 
//			try {
//				Debug.println(this.getClass(), generativeInputNode + " is not pointing to a single resident node. This version is going to ignore such input nodes nodes.");
//			} catch (Throwable t) {
//				t.printStackTrace();
//			}
//		} 
		
//    	/*has possible values */
//		OWLObjectProperty hasPossibleValuesProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 	
//		for(Entity possibleValue: node.getPossibleValueList()){
//			if(possibleValue instanceof CategoricalStateEntity)
//			individual.addPropertyValue(hasPossibleValuesProperty, this.mapCategoricalStates.get(possibleValue)); 
//			else{ //boolean states entity
//				individual.addPropertyValue(hasPossibleValuesProperty, this.mapBooleanStatesEntity.get(possibleValue)); 
//			}
//		}
	}



	/**
     * Create the PR-OWL structure for the formula of a context node. 
     * It expects {@link #getDomainResidentCache()} and {@link #getBuiltInRVCache()}
     * are filled.
     * @param formulaNode Root of the tree of the formula
     * @param mExpressionIndividual OWL individual for the context node
     * @param node : node containing formulaNode. This parameter and formulaNode are mostly used as a pivot for recursive calls. 
     * This parameter is particularly used by this method only for extracting names.
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : object representing OWL ontology, where MEBN data will be filled in.
	 * @see #saveBuiltInRV(MultiEntityBayesianNetwork, OWLOntology)
	 * @see #saveMTheoryAndMFrags(MultiEntityBayesianNetwork, OWLOntology)
     */
	protected void saveNodeFormula(NodeFormulaTree formulaNode, OWLIndividual mExpressionIndividual, INode node,
			MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();	// prefix for PR-OWL2 definitions
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);	// prefix for this ontology
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
    	if((formulaNode.getTypeNode() == EnumType.SIMPLE_OPERATOR)||
    			(formulaNode.getTypeNode() == EnumType.QUANTIFIER_OPERATOR)){
    		
    		if(formulaNode.getNodeVariable() instanceof BuiltInRV){
    			
    			// because built in rvs are boolean nodes, assure that mexpression is boolean mexpression
    			ontology.getOWLOntologyManager().addAxiom(
    					ontology, // where to add axiom
    					factory.getOWLClassAssertionAxiom( // type of mExpressionIndividual is MExpression
    							factory.getOWLClass(BOOLEANMEXPRESSION, prowl2Prefix), 
    							mExpressionIndividual
    					)
    			);
    			
    			// extract Built-In from cache
    			OWLIndividual builtInIndividual = this.getBuiltInRVCache().get(((BuiltInRV)(formulaNode.getNodeVariable())).getName()); 
    			
    			// add axiom: mExpressionIndividual -> typeOfMexpression -> builtInIndividual
    			ontology.getOWLOntologyManager().addAxiom(
    					ontology, 
    					factory.getOWLObjectPropertyAssertionAxiom(
    							factory.getOWLObjectProperty(TYPEOFMEXPRESSION, prowl2Prefix), 
    							mExpressionIndividual, 
    							builtInIndividual
						)
				);
    			// add inverse property too
    			ontology.getOWLOntologyManager().addAxiom(
    					ontology, 
    					factory.getOWLObjectPropertyAssertionAxiom(
    							factory.getOWLObjectProperty(ISTYPEOFMEXPRESSION, prowl2Prefix), 
    							builtInIndividual,
    							mExpressionIndividual 
						)
				);
    			
    			
    			int argNumber = 0; // this is a counter for arguments of context nodes
    			
    			// fill Arguments (i.e. children of context node's formula tree)
    			for(NodeFormulaTree child: formulaNode.getChildren()){
    				
    				/*
    				 * An argument of a context node can be one of the following types: 
    				 * - Ordinary Variable
    				 * - Node (Domain Resident Node)
    				 * - Entity
    				 * - Exemplar
    				 */
    				argNumber++; 		// arguments in PR-OWL ontologies starts from 1
    				if(child.getTypeNode() == EnumType.OPERAND){
    					
    					switch(child.getSubTypeNode()){
    					
    					case NOTHING:
    						// do not save empty argument, because an argument with no content results in an invalide OWL ontology
//    						this.saveEmptySimpleArgRelationship(contextIndividual, contextNode.getName(), argNumber , mebn, ontology);
    						break; 
    						
    					case OVARIABLE: 
    						this.saveOrdinaryVariableArgument((OrdinaryVariable)(child.getNodeVariable()), mExpressionIndividual, node, argNumber , mebn, ontology); 
    						break; 
    						
    					case NODE: 
    						if(child.getNodeVariable() instanceof ResidentNodePointer){
    							this.saveArgumentUsingResidentNodePointer((ResidentNodePointer)child.getNodeVariable(), mExpressionIndividual, node, argNumber, mebn, ontology );
    						} else{
    							//TODO other cases? 
    						}
    						break; 
    						
    					case ENTITY:	// we assume that if an argument is an Entity, then it must be a constant value
    						this.saveConstantArgument((Entity)child.getNodeVariable(), mExpressionIndividual, node, argNumber, mebn, ontology); 
    						break; 
    						
    					default: 
//    						this.saveEmptySimpleArgRelationship(mExpressionIndividual, contextNode, argNumber, mebn, ontology );
    					break; 
    					
    					}
    					
    				} else if((child.getTypeNode() == EnumType.SIMPLE_OPERATOR ) || (child.getTypeNode() == EnumType.QUANTIFIER_OPERATOR)) {
						this.saveArgumentAsBuiltInRV(mExpressionIndividual, node, argNumber, child, mebn, ontology );
					} else{
						//TODO evaluate other cases... 
//						saveEmptySimpleArgRelationship(contextNodeIndividual, contextNode.getName(), argNumber );
					}
    			}
    		} else{ //it is not using a built-in node... 
    			try {
					Debug.println(this.getClass(), node + " is not using a built in node.");
				} catch (Throwable t) {
					t.printStackTrace();
				}
    		} 
    	} else if((formulaNode.getTypeNode() == EnumType.OPERAND)&&
    			(formulaNode.getSubTypeNode() == EnumSubType.NODE)){
			
    		// the type is a random variable linked to a resident node. Extract the resident node
    		ResidentNodePointer pointer = (ResidentNodePointer)formulaNode.getNodeVariable();
    		ResidentNode residentNode = pointer.getResidentNode();
    		
    		//Extract such random variable using cache
    		OWLIndividual randomVariableIndividual = this.getRandomVariableCache().get(residentNode);
    		if (randomVariableIndividual == null) {
    			throw new IllegalStateException(residentNode + " is not cached. Please, call saveDomainResidentNodes before calling saveNodeFormula.");
    		}
    		
    		// axiom: mExpressionIndividual -> typeOfMExpression -> RANDOMVARIABLE
    		ontology.getOWLOntologyManager().addAxiom(
    				ontology, 
    				factory.getOWLObjectPropertyAssertionAxiom(
    						factory.getOWLObjectProperty(TYPEOFMEXPRESSION, prowl2Prefix), 
    						mExpressionIndividual, 
    						randomVariableIndividual
					)
			);
    		// fill inverse as well
    		ontology.getOWLOntologyManager().addAxiom(
    				ontology, 
    				factory.getOWLObjectPropertyAssertionAxiom(
    						factory.getOWLObjectProperty(ISTYPEOFMEXPRESSION, prowl2Prefix), 
    						randomVariableIndividual,
    						mExpressionIndividual
					)
			);
			
			// the possible values are inherent from the resident node's random variable. 
			Collection<Entity> possibleValues = residentNode.getPossibleValueList();
			
			// check if resident is boolean. We assume no node can be boolean and categorical at same time (so we just check the first possible state)
			if (possibleValues != null 
					&& !possibleValues.isEmpty()
					&&  possibleValues.iterator().next().getType().equals(TypeContainer.typeBoolean) ) {
				// If resident node is a boolean node, so the mExpressionIndividual is a BooleanMExpression
    			ontology.getOWLOntologyManager().addAxiom(
    					ontology, // where to add axiom
    					factory.getOWLClassAssertionAxiom( // type of mExpressionIndividual is MExpression
    							factory.getOWLClass(BOOLEANMEXPRESSION, prowl2Prefix), 
    							mExpressionIndividual
    					)
    			);
			}
			
	        // Save the arguments
			OrdinaryVariable[] oVariableArray = pointer.getOrdinaryVariableArray(); 
			for(int i = 0; i < oVariableArray.length; i++){
				if(oVariableArray[i] != null){
					// by using null as "node", we force this method to use mExpressionIndividual to generate unique name
					this.saveOrdinaryVariableArgument(oVariableArray[i], mExpressionIndividual, null, i + 1, mebn, ontology);
				} else {
					// do not create empty argument, because it will generate inconsistent OWL ontology
//					this.saveEmptySimpleArgRelationship(contextNodeIndividual, contextNodeIndividual.getName(), i + 1); 
				}
			}
		} else {
			try {
				Debug.println(this.getClass(), node + " is using an unknown formula.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

	/**
	 * This method creates and fills arguments of mExpressionIndividual using builtInRVs
	 * @param mExpressionIndividual : owl individual to be updated
	 * @param node : node linked to mExpressionIndividual 
	 * @param argNumber	: it represents the order of appearance of the current argument
	 * @param formulaNode : it represents a FOL formula (in a tree-like abstraction)
	 * @param mebn : mebn containing node and formulaTree
	 * @param ontology : ontology containing mExpressionIndividual
	 */
	protected void saveArgumentAsBuiltInRV(OWLIndividual mExpressionIndividual,
			INode node, int argNumber, NodeFormulaTree formulaNode,
			MultiEntityBayesianNetwork mebn, OWLOntology ontology) {

		// prepare prefixes
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();	// prefix for PR-OWL2 definitions
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);	// prefix for this ontology
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// create argument as an MExpressionArgument
		OWLIndividual argumentIndividual = factory.getOWLNamedIndividual(node.getName() + ARGUMENT_NUMBER_SEPARATOR + argNumber, currentPrefix);
		
		// assure argumentIndividual's type is MExpressionArgument (arguments representing a node of a FOL expression)
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLClassAssertionAxiom(
						factory.getOWLClass(MEXPRESSIONARGUMENT, prowl2Prefix), 
						argumentIndividual
				)
		);
		
		// axiom: mExpressionIndividual -> hasArgument -> argumentIndividual
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(HASARGUMENT, prowl2Prefix), 
						mExpressionIndividual,
						argumentIndividual
				)
		);
		// fill inverse as well
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(ISARGUMENTOF, prowl2Prefix), 
						argumentIndividual,
						mExpressionIndividual
				)
		);
		
		// set hasArgumentNumber (which specifies the order of the argument)
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLDataPropertyAssertionAxiom(
						factory.getOWLDataProperty(HASARGUMENTNUMBER, prowl2Prefix), 
						argumentIndividual,
						argNumber
				)
		);
		
		// this is the name of the inner mExpression, which will be used as a parameter for an inner class posteriory (that's why its set as final)
		final String innerMExpressionName = node.getName() + ARGUMENT_NUMBER_SEPARATOR + argNumber + INNERMEXPRESSION_SUFFIX; 
		
		// create inner  MExpression 
		OWLIndividual innerMExpression = factory.getOWLNamedIndividual(innerMExpressionName, currentPrefix);
		// we do not need to set it as BooleanMExpression, because the recursive call to saveNodeFormula will do so, but assure it is an MExpression
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLClassAssertionAxiom(
						factory.getOWLClass(MEXPRESSION, prowl2Prefix), 
						innerMExpression
				)
		);
		
		// axiom: innerMExpression -> isTypeOfArgumentIn -> argumentIndividual
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(ISTYPEOFARGUMENTIN, prowl2Prefix), 
						innerMExpression,
						argumentIndividual
				)
		);
		// fill inverse as well
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(TYPEOFARGUMENT, prowl2Prefix), 
						argumentIndividual,
						innerMExpression
				)
		);
		
		// do recursive call to fill inner MExpression using a simple stub node as pivot
		this.saveNodeFormula(
				formulaNode, 
				innerMExpression, 
				new INode() {	//a stub node using default values and name == innerMExpressionName
					public String getName() {return innerMExpressionName;}	// this is the only important data to be passed recursivelly
					// other fields/methods are not going to be used
					public void setStates(List<String> states) {}		 public void setStateAt(String state, int index) {}
					public void setParentNodes(List<INode> parents) {}	 public void setName(String name) {}
					public void setDescription(String text) {}			 public void setChildNodes(List<INode> children) {}
					public void removeStateAt(int index) {}				 public void removeParentNode(INode parent) {}
					public void removeLastState() {}					 public void removeChildNode(INode child) {}
					public int getType() {return 0;}					 public int getStatesSize() {return 0;}
					public String getStateAt(int index) {return null;}	 public List<INode> getParentNodes() {return null;}
					public String getDescription() {return null;}		 public List<INode> getChildNodes() {return null;}
					public List<INode> getAdjacentNodes() {return null;} public void appendState(String state) {}
					public void addParentNode(INode parent) throws InvalidParentException {}
					public void addChildNode(INode child) throws InvalidParentException {}
				}, 
				mebn, 
				ontology
		);
	}



	/**
	 * Fill constant argument using categorical entities or boolean states.
	 * It assumes {@link #getCategoricalStatesCache()} is already filled.
	 * @param constantState	: state to be used as a content of the argument. E.g. {@link CategoricalStateEntity}, {@link BooleanStateEntity}, etc.
	 * @param mExpressionIndividual : MExpression representing node
	 * @param node : node containing the original argument (categoricalState).
	 * @param argNumber : a number for ordering.
	 * @param mebn	: mebn containing node and categoricalState
	 * @param ontology : ontology containing mExpressionIndividual
	 * @see #saveCategoricalStates(MultiEntityBayesianNetwork, OWLOntology)
	 */
	protected void saveConstantArgument(
			Entity constantState,
			OWLIndividual mExpressionIndividual, INode node,
			int argNumber, MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		
		// TODO create a method so that we can save literals as constant arguments as well
		
		// prepare prefixes
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();	// prefix of PR-OWL2 definition ontology
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);	// prefix of current ontology
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();

		// get argument (it will create a new one if nothing is found)
		OWLIndividual argumentIndividual = factory.getOWLNamedIndividual(node.getName() + ARGUMENT_NUMBER_SEPARATOR + argNumber, currentPrefix);
		
		// assure that argumentIndividual is a constant argument
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLClassAssertionAxiom(
						factory.getOWLClass(CONSTANTARGUMENT, prowl2Prefix), 
						argumentIndividual
				)
		);
		
		// axiom: mExpressionIndividual -> hasArgument -> argumentIndividual
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(HASARGUMENT, prowl2Prefix), 
						mExpressionIndividual,
						argumentIndividual
				)
		);
		// fill inverse as well
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(ISARGUMENTOF, prowl2Prefix), 
						argumentIndividual,
						mExpressionIndividual
				)
		);
		
		// constant states a
		if (constantState instanceof CategoricalStateEntity) {
			// axiom: argumentIndividual -> typeOfArgument -> constantState
			ontology.getOWLOntologyManager().addAxiom(
					ontology, 
					factory.getOWLObjectPropertyAssertionAxiom(
							factory.getOWLObjectProperty(TYPEOFARGUMENT, prowl2Prefix), 
							argumentIndividual,
							this.getCategoricalStatesCache().get(constantState)
					)
			);
			// fill inverse as well
			ontology.getOWLOntologyManager().addAxiom(
					ontology, 
					factory.getOWLObjectPropertyAssertionAxiom(
							factory.getOWLObjectProperty(ISTYPEOFARGUMENTIN, prowl2Prefix), 
							this.getCategoricalStatesCache().get(constantState),
							argumentIndividual
					)
			);
		} else if (constantState instanceof BooleanStateEntity) {
			// test if we are explicitly setting a value as absurd (this is semantically invalid, but sintatically valid)
			if (mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity().equals(constantState)) {
				// absurd cannot be converted to XSD:boolean data type. In such case, we should link to "absurd" (an individual of Absurd)
				OWLNamedIndividual absurdIndividual = factory.getOWLNamedIndividual(ABSURD_INDIVIDUAL, prowl2Prefix);
				ontology.getOWLOntologyManager().addAxiom(
						ontology, 
						factory.getOWLObjectPropertyAssertionAxiom(
								factory.getOWLObjectProperty(TYPEOFARGUMENT, prowl2Prefix), 
								argumentIndividual,
								absurdIndividual
						)
				);
				// fill inverse as well
				ontology.getOWLOntologyManager().addAxiom(
						ontology, 
						factory.getOWLObjectPropertyAssertionAxiom(
								factory.getOWLObjectProperty(ISTYPEOFARGUMENTIN, prowl2Prefix), 
								absurdIndividual,
								argumentIndividual
						)
				);
			} else {	// this is not "absurd" -> use XSD:boolean datatype
				// axiom: argumentIndividual -> typeOfDataArgument -> (boolean)constantState
				ontology.getOWLOntologyManager().addAxiom(
						ontology, 
						factory.getOWLDataPropertyAssertionAxiom(
								factory.getOWLDataProperty(TYPEOFDATAARGUMENT, prowl2Prefix), 
								argumentIndividual,
								Boolean.parseBoolean(((BooleanStateEntity)constantState).getName()) // convert booleanStateEntity to boolean
						)	
				);
			}
		} else {
			// TODO handle other types of constant arguments
		}
			
		// axiom: argumentIndividual -> hasArgumentNumber -> argNumber
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLDataPropertyAssertionAxiom(
						factory.getOWLDataProperty(HASARGUMENTNUMBER, prowl2Prefix), 
						argumentIndividual,
						argNumber
				)
		);
	}



	/**
	 * This method fills an argument using data from a resident node pointer.
	 * It assumes {@link #getRandomVariableCache()} and {@link #getDomainResidentCache()} are already filled.
	 * @param residentNodePointer : a indirect reference to a resident node
	 * @param mExpressionIndividual : owl individual to be filled with an argument (this MExpression is linked to node)
	 * @param node : node containing the arguments to be read
	 * @param argNumber : a number identifying the order of the current argument
	 * @param mebn : mebn containing node and residentNodePointer
	 * @param ontology : ontology containing mExpressionIndividual
	 * @see #saveDomainResidentNodes(MultiEntityBayesianNetwork, OWLOntology)
	 * @see #saveMTheoryAndMFrags(MultiEntityBayesianNetwork, OWLOntology)
	 */
	protected void saveArgumentUsingResidentNodePointer (
			ResidentNodePointer residentNodePointer,
			OWLIndividual mExpressionIndividual, INode node,
			int argNumber, MultiEntityBayesianNetwork mebn, OWLOntology ontology) {

		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();	// prefix for PR-OWL2 definitions
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);	// prefix of this ontology
		
		// get factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// get OWL individual for argument (it will create a new one if nothing is found)
		OWLIndividual argumentIndividual = factory.getOWLNamedIndividual(node.getName() + ARGUMENT_NUMBER_SEPARATOR + argNumber, currentPrefix);
		
		// Only MExpressionArgument can hold references to random variables related to a resident node
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLClassAssertionAxiom(
						factory.getOWLClass(MEXPRESSIONARGUMENT, prowl2Prefix),  // assure argumentIndividual is a subclass of MExpressionArgument 
						argumentIndividual
				)
		);
		
		// add axiom: argumentIndividual -> isArgumentOf -> mExpressionIndividual
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(ISARGUMENTOF, prowl2Prefix), 
						argumentIndividual,
						mExpressionIndividual
				)
		);
		// add inverse property as well
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(HASARGUMENT, prowl2Prefix), 
						mExpressionIndividual,
						argumentIndividual
				)
		);
		
		// fill hasArgumentNumber data property
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLDataPropertyAssertionAxiom(
						factory.getOWLDataProperty(HASARGUMENTNUMBER, prowl2Prefix), 
						argumentIndividual, 
						argNumber
				)
		);
		
		// the type of this argument (MExpressionArgument) is another MExpression
		OWLIndividual innerMExpression = factory.getOWLNamedIndividual(
				node.getName() + ARGUMENT_NUMBER_SEPARATOR + argNumber + INNERMEXPRESSION_SUFFIX, prowl2Prefix);
		
		// assure innerMExpression is a MExpression
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLClassAssertionAxiom(
						factory.getOWLClass(MEXPRESSION, prowl2Prefix), 
						innerMExpression
				)
		);
		
		// axiom: argumentIndividual -> typeOfArgument -> innerMExpression
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(TYPEOFARGUMENT, prowl2Prefix), 
						argumentIndividual, 
						innerMExpression
				)
		);
		// fill inverse property too
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(ISTYPEOFARGUMENTIN, prowl2Prefix), 
						innerMExpression,
						argumentIndividual
				)
		);
		
		// extract resident node's random variable's individual from cache
		OWLIndividual innerRandomVariableIndividual = this.getRandomVariableCache().get(residentNodePointer.getResidentNode());
		if (innerRandomVariableIndividual == null) {
			throw new IllegalStateException(residentNodePointer.getResidentNode() + "'s random variable is not cached. Call saveDomainResidentNodes before saving context nodes.");
		}
		
		// axiom: innerMExpression -> typeOfMExpression -> RESIDENT
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(TYPEOFMEXPRESSION, prowl2Prefix), 
						innerMExpression, 
						innerRandomVariableIndividual
				)
		);
		// fill inverse property too
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(ISTYPEOFMEXPRESSION, prowl2Prefix), 
						innerRandomVariableIndividual,
						innerMExpression
				)
		); 
		
		// there is no need to save possible values, because it is inherent from the type of mexpression
		
        // Save the arguments of the new (inner) MExpression
		OrdinaryVariable[] oVariableArray = residentNodePointer.getOrdinaryVariableArray(); 
		for(int i = 0; i < oVariableArray.length; i++){
			if(oVariableArray[i] == null){
				// do not attempt to save empty argument, because it results in an invalid owl ontology
//				this.saveEmptySimpleArgRelationship(innerContextNode, innerContextNode.getName(), i + 1); 
			} else{
				this.saveOrdinaryVariableArgument(oVariableArray[i], innerMExpression, null, i + 1, mebn, ontology ); 
			}
		}
	}



//	/**
//	 * Save an empty argument.
//	 * @param contextIndividual
//	 * @param name: The name of the node that has the argument. 
//	 * @param argNumber
//	 * @param mebn
//	 * @param ontology
//	 */
//	protected void saveEmptyArgument(
//			OWLIndividual individual, String name, int argNumber,
//			MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
//		// TODO Auto-generated method stub
//		
//	}



	/**
	 * This method fills the ontology with resident node's data.
	 * It assumes {@link #getDomainResidentCache()} and {@link #getGenerativeInputCache()} are filled.
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : object representing OWL ontology, where MEBN data will be filled in.
	 * @see #saveMTheoryAndMFrags(MultiEntityBayesianNetwork, OWLOntology)
	 */
	protected void saveDomainResidentNodes(MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
    	
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();	// prefix of PR-OWL2 definition ontology
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);	// prefix of current ontology
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// iterate over resident nodes in mfrags 
    	for(MFrag mfrag : mebn.getDomainMFragList()){
    		for (ResidentNode residentNode: mfrag.getResidentNodeList()) {
    			// use cache to access OWL individuals
    			OWLIndividual domainResIndividual = this.getDomainResidentCache().get(residentNode);	
    			
    			// solve MExpression. 
    			// TODO this version assumes that Resident nodes can only have SimpleMExpression, which may not true in a generic MEBN
//    			OWLClass simpleMExpressionClass = factory.getOWLClass(IPROWL2ModelUser.SIMPLEMEXPRESSION, prowl2Prefix);
    			
    			
    			// get MExpression related to domainResIndividual. Create axiom if it does not exist
    			OWLIndividual mExpressionIndividual = factory.getOWLNamedIndividual(IPROWL2ModelUser.MEXPRESSION_PREFIX + residentNode.getName(), currentPrefix);
    			
    			// assert that MExpression exists.
    			if (!ontology.containsIndividualInSignature(mExpressionIndividual.asOWLNamedIndividual().getIRI(), true)) {
    				// add axiom to create mExpressionIndividual as individual of SimpleMExpression
    				ontology.getOWLOntologyManager().addAxiom(
    						ontology, 
    						factory.getOWLClassAssertionAxiom(	 
    								factory.getOWLClass(IPROWL2ModelUser.SIMPLEMEXPRESSION, prowl2Prefix), 
    								mExpressionIndividual
    						)
    				);
    				// we assume resident nodes have only ordinary variables as arguments (so we can safely use SimpleMExpression without asserting arguments)
    			}
    			
    			// hasMExpression
    			
    			// Add and commit axiom which asserts domainResIndividual -> hasMExpressionProperty -> mExpressionIndividual
    			ontology.getOWLOntologyManager().addAxiom (	// add axiom and commit
    					ontology, // where to add axiom
    					factory.getOWLObjectPropertyAssertionAxiom( // create axiom linking 2 individuals with a property
    							factory.getOWLObjectProperty(this.getHasMExpressionPropertyName(), prowl2Prefix), // hasMExpression
    							domainResIndividual, 	// subject
    							mExpressionIndividual	// object
						)
				);
    			
    			// add axiom: mExpressionIndividual -> isMExpressionOf -> domainResIndividual
    			ontology.getOWLOntologyManager().addAxiom(
    					ontology, // where to add axiom
    					factory.getOWLObjectPropertyAssertionAxiom(
    							factory.getOWLObjectProperty(this.getIsMExpressionOfPropertyName(), prowl2Prefix), //isMExpressionOf 
    							mExpressionIndividual, 	// subject
    							domainResIndividual	// object
						)
				);

    			/* hasArgument */
    			try{
    				Debug.println("Filling arguments of " + domainResIndividual);
    			} catch (Throwable t) {
    				t.printStackTrace();
				}
    			int argumentNumber = 1; // this number is used in PR-OWL2 ontologies to store the order of arguments
    			for(OrdinaryVariable ovArgument : residentNode.getOrdinaryVariableList()) {
    				this.saveOrdinaryVariableArgument(ovArgument, mExpressionIndividual, residentNode, argumentNumber, mebn, ontology);
    				argumentNumber++; 
    			}			
    			
    			/* hasParent */
    			try {
    				Debug.println(this.getClass(), "Filling parents of " + domainResIndividual);
    			} catch (Throwable t) {
    				t.printStackTrace();
				}
    			OWLObjectProperty hasParentProperty = factory.getOWLObjectProperty(HASPARENT, prowl2Prefix); 				
    			
    			// add parents that are resident nodes
    			for(ResidentNode residentNodeParent: residentNode.getResidentNodeFatherList()){
    				OWLIndividual residentNodeParentIndividual = this.getDomainResidentCache().get(residentNodeParent); 
    				// add axiom
    				ontology.getOWLOntologyManager().addAxiom(
    						ontology, 
    						factory.getOWLObjectPropertyAssertionAxiom(hasParentProperty, domainResIndividual, residentNodeParentIndividual)
					);
    			}

    			// add parents that are input nodes
    			for(InputNode inputNodeParent: residentNode.getParentInputNodesList()){
    				OWLIndividual inputNodeParentIndividual = this.getGenerativeInputCache().get(inputNodeParent); 
    				// add axiom
    				ontology.getOWLOntologyManager().addAxiom(
    						ontology, 
    						factory.getOWLObjectPropertyAssertionAxiom(hasParentProperty, domainResIndividual, inputNodeParentIndividual)
					);
    			}		
    			
    			// typeOfMExpression (RandomVariable or BooleanRandomVariable)
    			this.saveResidentPossibleValues(mExpressionIndividual, residentNode, mebn, ontology); 	        
    			
    			/* hasInputInstance */
    			OWLObjectProperty hasInputInstanceProperty = factory.getOWLObjectProperty(HASINPUTINSTANCE, prowl2Prefix); 	
    			try {
    				Debug.println(this.getClass(), "Verifying input instances of " + residentNode);
    			} catch (Throwable t) {
    				t.printStackTrace();
				}
    			// get all input nodes related to this resident node
    			for(InputNode inputInstance: residentNode.getInputInstanceFromList()){
    				// use cache to extract input nodes
    				OWLIndividual inputInstanceIndividual = this.getGenerativeInputCache().get(inputInstance);
    				if (inputInstanceIndividual != null) {
    					// axiom: domainResIndividual -> hasInputInstanceProperty -> inputInstanceIndividual
    					ontology.getOWLOntologyManager().addAxiom(
    							ontology, 
    							factory.getOWLObjectPropertyAssertionAxiom(hasInputInstanceProperty, domainResIndividual, inputInstanceIndividual)
						);
    				}
    			}	
    			
    			/* hasProbabilityDistribution */
    			OWLObjectProperty hasProbDist = factory.getOWLObjectProperty(HASPROBABILITYDISTRIBUTION, prowl2Prefix);
    			try {
    				Debug.println(this.getClass(), "Verifying probability distros of " + residentNode);
    			} catch (Throwable t) {
    				t.printStackTrace();
				}
    			
    			// extract class for LPDs using pseudocode (DeclarativeDistribution)
    			OWLClass declarativeDist = factory.getOWLClass(IPROWL2ModelUser.DECLARATIVEDISTRIBUTION, prowl2Prefix);
    			
    			// get individual of DeclarativeDist
    			OWLIndividual declarativeDistThisNode = factory.getOWLNamedIndividual(residentNode.getName() + DECLARATIVE_DISTRO_SUFIX, prowl2Prefix);
    			
    			// remove individual if it exists, because we want to overwrite an existing one, instead of reusing all distributions
    			if (ontology.containsIndividualInSignature(declarativeDistThisNode.asOWLNamedIndividual().getIRI(), true)) {
    				OWLEntityRemover remover = new OWLEntityRemover(ontology.getOWLOntologyManager(), Collections.singleton(ontology));
    				declarativeDistThisNode.asOWLNamedIndividual().accept(remover);
    				ontology.getOWLOntologyManager().applyChanges(remover.getChanges());
    				declarativeDistThisNode = factory.getOWLNamedIndividual(residentNode.getName() + DECLARATIVE_DISTRO_SUFIX, prowl2Prefix);
    			}
    			
    			// add axiom forcing that declarativeDistThisNode has declarativeDist as its type
    			ontology.getOWLOntologyManager().addAxiom(
    					ontology, 
    					factory.getOWLClassAssertionAxiom(declarativeDist, declarativeDistThisNode)
				);
    			
    			/* hasDeclaration */
    			OWLDataProperty hasDeclaration = factory.getOWLDataProperty(HASDECLARATION, prowl2Prefix); 
    			if(residentNode.getTableFunction() != null){
    				// add axiom: declarativeDistThisNode -> hasDeclaration -> residentNode.getTableFunction()
    				ontology.getOWLOntologyManager().addAxiom(
    						ontology, 
    						factory.getOWLDataPropertyAssertionAxiom(hasDeclaration, declarativeDistThisNode, residentNode.getTableFunction())
					);
    				
    				// add axiom: domainResIndividual -> hasProbabilityDistribution -> declarativeDistThisNode
    				ontology.getOWLOntologyManager().addAxiom(
    						ontology, 
    						factory.getOWLObjectPropertyAssertionAxiom(hasProbDist, domainResIndividual, declarativeDistThisNode)
					);
    			}
    			
    			// OBS. comments are already inserted in saveMTheoryAndMFrags
    			
    		} 	
    	}
	}

	/**
	 * This method creates the possible values of a resident node, which is encapsulated in a MExpression.
	 * It assumes {@link #getCategoricalStatesCache()}, {@link #getMetaEntityCache()} and 
	 * {@link #getObjectEntityClassesCache()} are filled.
	 * This version does not allow boolean states, categorical states and other states living togather in a single node
	 * (i.e. if a node is declared as a boolean node, it must contain only boolean values; if it points to an entity,
	 * it must not point to anything else).
	 * @param mExpressionIndividual : MExpression to be updated
	 * @param resident : node containing possible values
	 * @param mebn : MEBN containing the node
	 * @param ontology : ontology containing mExpressionIndividual
	 * @see #saveCategoricalStates(MultiEntityBayesianNetwork, OWLOntology)
	 * @see #saveMetaEntities(MultiEntityBayesianNetwork, OWLOntology)
	 * @see #saveObjectEntitiesClasses(MultiEntityBayesianNetwork, OWLOntology)
	 */
	protected void saveResidentPossibleValues( OWLIndividual mExpressionIndividual, ResidentNode resident,
			MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		try {
			Debug.println(this.getClass(), "Verifying possible values of " + resident);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();	// prefix of PR-OWl2 ontology definition
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);	// prefix of current ontology
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();

		// extract owl properties to be used
		
		// typeOfMExpression (it points to either RandomVariable or BooleanRandomVariable) and its inverse
		OWLObjectProperty typeOfMExpression  = factory.getOWLObjectProperty(TYPEOFMEXPRESSION, prowl2Prefix);
		OWLObjectProperty isTypeOfMExpression  = factory.getOWLObjectProperty(ISTYPEOFMEXPRESSION, prowl2Prefix);
		
		// hasPossibleValues
		OWLDataProperty hasPossibleValues = factory.getOWLDataProperty(HASPOSSIBLEVALUES, prowl2Prefix); 	
		
		// OBS. globally exclusivenesses are not present in PR-OWL 2 ontologies
		
		/* Get RandomVariable class */
		OWLClass randomVariableClass = factory.getOWLClass(RANDOMVARIABLE, prowl2Prefix);
		/* Get BooleanRandomVariable class */
		OWLClass booleanRandomVariableClass = factory.getOWLClass(BOOLEANRANDOMVARIABLE, prowl2Prefix);
		
		// get an instance of RandomVariable. This object will hold the currently evaluated rv
		OWLIndividual rv = factory.getOWLNamedIndividual(RANDOMVARIABLE_PREFIX + resident.getName(), currentPrefix);
		// specify that randomVariableClass is the class of rv, if it is not done already
		if (!ontology.containsIndividualInSignature(rv.asOWLNamedIndividual().getIRI())
				|| !rv.getTypes(ontology).contains(randomVariableClass)) {
			ontology.getOWLOntologyManager().addAxiom(
					ontology, 
					factory.getOWLClassAssertionAxiom(randomVariableClass, rv)
			);
		}
		// add rv to cache
		this.getRandomVariableCache().put(resident, rv);
		
		
		// save the default probability distribution in RandomVariable instead of saving in ResidentNode
		this.saveRVDefaultProbabilityDistribution(rv, resident, mebn, ontology);
		
		// save mappings related to definesUncertaintyOf, isSubjectOf and isObjectOf
		this.saveMappingBetweenMEBNAndOWL(rv, resident, mebn, ontology);
		
		
		// prepare a set of data property axioms (hasPossibleValues) to remove (so that we do not have to check if the old ones should be removed or not)
		Set<OWLDataPropertyAssertionAxiom> axiomsToRemove = new HashSet<OWLDataPropertyAssertionAxiom>();
		for (OWLDataPropertyAssertionAxiom axiom : ontology.getDataPropertyAssertionAxioms(rv)) {
			if (hasPossibleValues.getIRI().equals(axiom.getProperty().asOWLDataProperty().getIRI())) {
				axiomsToRemove.add(axiom);
			}
		}
		
		// remove the axioms
		if (!axiomsToRemove.isEmpty()) {
			ontology.getOWLOntologyManager().applyChanges(ontology.getOWLOntologyManager().removeAxioms(ontology, axiomsToRemove));
		}
		
		
		// iterate over links to possible values
		for(StateLink stateLink: resident.getPossibleValueLinkList()){
			boolean isCategorical = false;	// if it is using categorical entity, set it to true
			
			// solve link
			Entity state = stateLink.getState(); 
			
			// ignore the absurd "boolean" state because in PR-OWL2 the "absurd" is not a boolean state anymore and it is an implicit state (so we do not need to explicitly add it to ontology)
			if (mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity().equals(state)) {
				// the "absurd" state is implicitly added to all RVs
				continue;
			}
			
			if( ( state instanceof CategoricalStateEntity ) || (state instanceof ObjectEntity)){
				if (mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity().equals(state)) {
					continue;
				}
				// do different behavior depending on the type of state
				if ( state instanceof CategoricalStateEntity ) {
					isCategorical = true;
					if (!mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity().getName().equalsIgnoreCase(state.getName())) {
						// this is a "normal" categorical state. Extract it
						OWLIndividual owlState = this.getCategoricalStatesCache().get(state); 
						if (owlState == null) {
							throw new IllegalStateException("getCategoricalStatesCache must be filled before calling this method: saveResidentPossibleValues" );
						}
						// fill hasPossibleValues as an URI
						ontology.getOWLOntologyManager().addAxiom ( // add axiom and commit
								ontology, 	// where to add axiom
								factory.getOWLDataPropertyAssertionAxiom(
										hasPossibleValues, // data property to use
										rv, // where to add data property
										factory.getOWLLiteral(owlState.asOWLNamedIndividual().getIRI().toURI().toString(), OWL2Datatype.XSD_ANY_URI)	// URI literal
								)
						);
					} else {
						// this is a categorical state representing the "absurd" state  
						// the "absurd" state is implicitly added to all RVs
					}
				} else { // ObjectEntity, thus it points to an unknown quantity of possible values
					// Extract uri of the owl object being pointed
					String uriString =  this.getMetaEntityCache().get(((ObjectEntity)state).getType().getName()); 
					if (uriString != null) {
						// fill hasPossibleValues as an URI
						ontology.getOWLOntologyManager().addAxiom ( // add axiom and commit
								ontology, 	// where to add axiom
								factory.getOWLDataPropertyAssertionAxiom(
										hasPossibleValues, // data property to use
										rv, // where to add data property
										factory.getOWLLiteral(uriString, OWL2Datatype.XSD_ANY_URI)	// URI literal
								)
						);
					}
				}
			} else if(state instanceof BooleanStateEntity){	// boolean
				// specify that booleanRandomVariableClass is the class of rv, if it is not done already
				if (!ontology.containsIndividualInSignature(rv.asOWLNamedIndividual().getIRI())
						|| !rv.getTypes(ontology).contains(booleanRandomVariableClass)) {
					ontology.getOWLOntologyManager().addAxiom(
							ontology, 
							factory.getOWLClassAssertionAxiom(booleanRandomVariableClass, rv)
					);
				}
				// it is not necessary to specify possible value, because BOOLEANRANDOMVARIABLE adds XSD:boolean automatically
			}  else {
				try {
					Debug.println(this.getClass(), "Error: Invalid State - " + state.getName() + " in resident node " + resident); 
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;	// ignore and go on
			}
			
			if (!isCategorical) {
				// OBS. we break the loop because this version does not allow mixed types of states in a single node (except categorical or absurd)
				break;
			}
		}
		
		// link rv to mExpressionIndividual using typeOfMExpression
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(typeOfMExpression, mExpressionIndividual, rv)
		);
		// link mExpressionIndividual to rv  using isTypeOfMExpression (inverse of typeOfMExpression)
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(isTypeOfMExpression, rv, mExpressionIndividual)
		);
	}

	/**
	 * This method saves the "definesUncertaintyOf", "isSubjectIn" and "isObjectIn" properties.
	 * This method will fill the contents of {@link #getMappingArgumentCache()}
	 * @param rvIndividual  : random variable to be updated
	 * @param resident : node related to random variable
	 * @param mebn : MEBN containing the resident node
	 * @param ontology : ontology containing rvIndividual
	 * @see #saveResidentPossibleValues(OWLIndividual, ResidentNode, MultiEntityBayesianNetwork, OWLOntology)
	 * @see IPROWL2ModelUser#DEFINESUNCERTAINTYOF
	 * @see IPROWL2ModelUser#ISSUBJECTIN
	 * @see IPROWL2ModelUser#ISOBJECTIN
	 * @see #getMappingArgumentCache()
	 */
	protected void saveMappingBetweenMEBNAndOWL(OWLIndividual rvIndividual, ResidentNode resident, MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		
		// assertion
		if (ontology == null || resident == null || rvIndividual == null) {
			try {
				Debug.println(this.getClass(), "Attempted to call \"saveMappingBetweenMEBNAndOWL\" with a null argument: rv = "
						 + rvIndividual 
						 + ", resident = " + resident
						 + ", mebn = " + mebn);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return;
		}
		
		// if mebn == null, extract mebn from resident
		if (resident.getMFrag() != null) {
			mebn = resident.getMFrag().getMultiEntityBayesianNetwork();
		}
		if (mebn == null) {
			try {
				Debug.println(this.getClass(), "Could not extract MEBN in \"saveMappingBetweenMEBNAndOWL\" because argument was null and " 
						+ resident + " was not connected to a MEBN.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return;
		}
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// extract prefix
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager();	// prefix of prowl2 definitions
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);	// prefix of current ontology
		
		// save definesUncertaintyOf if it exists
		if (mebn != null) {
			IRI owlPropertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(mebn, resident);
			if (owlPropertyIRI != null) {
				ontology.getOWLOntologyManager().addAxiom(
						ontology, 
						factory.getOWLDataPropertyAssertionAxiom(
								factory.getOWLDataProperty(DEFINESUNCERTAINTYOF, prowl2Prefix), // definesUncertaintyOf
								rvIndividual, // subject
								factory.getOWLLiteral(owlPropertyIRI.toURI().toString(), OWL2Datatype.XSD_ANY_URI) // literal^anyURI
						)
				);
			}
		}
		
		// iterate on arguments in order to extract mappings from IRIAwareMultiEntityBayesianNetwork
		for (Argument argument : resident.getArgumentList()) {
			// Note: we are not inserting arguments in order of argNumber, but argument.getArgNumber() and argumentIndividual->hasArgumentNumber must be the same
			// check argument and argNumber consistency
			if (argument == null || argument.getArgNumber() < 1) {
				try {
					Debug.println(this.getClass(), "Could not extract argNumber from argument = " + argument + " in node = " + resident
							+ ", but we'll keep trying to save other MappingArguments.");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				continue;	// ignore
			}
			
			// create individual of MappingArgument
			OWLNamedIndividual argumentIndividual = factory.getOWLNamedIndividual(
					rvIndividual.asOWLNamedIndividual().getIRI().getFragment()
						+ ARGUMENT_NUMBER_SEPARATOR + argument.getArgNumber(),	// <nameOfRV>_<Number> 
					currentPrefix
			);
			
			// extract MappingArgument class
			OWLClass mappingArgumentClass = factory.getOWLClass(MAPPINGARGUMENT, prowl2Prefix);
			
			// make sure argumentIndividual is a MappingArgument
			if (!ontology.containsIndividualInSignature(argumentIndividual.getIRI(), true)
					|| !argumentIndividual.getTypes(ontology).contains(mappingArgumentClass)) {
				ontology.getOWLOntologyManager().addAxiom(
						ontology, 
						factory.getOWLClassAssertionAxiom(mappingArgumentClass, argumentIndividual)	// typeOf argumentIndividual = MappingArgument
				);
			}
			
			// set random rvIndividual -> hasArgument -> argumentIndividual
			ontology.getOWLOntologyManager().addAxiom(
					ontology, 
					factory.getOWLObjectPropertyAssertionAxiom(
							factory.getOWLObjectProperty(HASARGUMENT, prowl2Prefix), 	// hasArgument
							rvIndividual, 				// subject
							argumentIndividual			// object
					)
			);
			// set inverse: argumentIndividual -> isArgumentOf -> rvIndividual 
			ontology.getOWLOntologyManager().addAxiom(
					ontology, 
					factory.getOWLObjectPropertyAssertionAxiom(
							factory.getOWLObjectProperty(ISARGUMENTOF, prowl2Prefix), 	// isArgumentOf
							argumentIndividual,			// subject
							rvIndividual 				// object
					)
			);
			
			// set argumentIndividual -> hasArgumentNumber -> argument.getArgNumber()
			ontology.getOWLOntologyManager().addAxiom(
					ontology, 
					factory.getOWLDataPropertyAssertionAxiom(
							factory.getOWLDataProperty(HASARGUMENTNUMBER, prowl2Prefix), 	// hasArgumentNumber
							argumentIndividual,			// subject
							factory.getOWLLiteral(argument.getArgNumber()) 				// data
					)
			);
			
			// extract IRI of the mapped owl property (try isObjectIn first)
			Collection<IRI> mappedIRIs = IRIAwareMultiEntityBayesianNetwork.getIsObjectFromMEBN(mebn, argument);
			if (mappedIRIs != null) {
				// we expect that size of mappedIRIs is 1, but we can stil save all of them anyway
				for (IRI iri : mappedIRIs) {
					if (iri != null) {
						ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								factory.getOWLDataPropertyAssertionAxiom(
										factory.getOWLDataProperty(ISOBJECTIN, prowl2Prefix), // isObjectIn
										argumentIndividual, // subject
										factory.getOWLLiteral(iri.toURI().toString(), OWL2Datatype.XSD_ANY_URI) // literal^anyURI
								)
						);
					}
				}
			}
			
			// extract other IRIs of the mapped owl property (now, let's try isSubjectIn)
			mappedIRIs = IRIAwareMultiEntityBayesianNetwork.getIsSubjectFromMEBN(mebn, argument);
			if (mappedIRIs != null) {
				// we expect that size of mappedIRIs is 1, but we can stil save all of them anyway
				for (IRI iri : mappedIRIs) {
					if (iri != null) {
						ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								factory.getOWLDataPropertyAssertionAxiom(
										factory.getOWLDataProperty(ISSUBJECTIN, prowl2Prefix), // isSubjectIn
										argumentIndividual, // subject
										factory.getOWLLiteral(iri.toURI().toString(), OWL2Datatype.XSD_ANY_URI) // literal^anyURI
								)
						);
					}
				}
			}
			
			// add to cache
			if (this.getMappingArgumentCache() != null) {
				this.getMappingArgumentCache().put(resident, argumentIndividual);
			}
		}
	}



	/**
	 * This method saves the default probability distribution of a random variable.
	 * @param rvIndividual  : random variable to be updated
	 * @param resident : node related to random variable
	 * @param mebn : MEBN containing the resident node
	 * @param ontology : ontology containing rvIndividual
	 * @see #saveResidentPossibleValues(OWLIndividual, ResidentNode, MultiEntityBayesianNetwork, OWLOntology)
	 */
	protected void saveRVDefaultProbabilityDistribution(OWLIndividual rvIndividual, ResidentNode resident, MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		// TODO Auto-generated method stub
		Debug.println(getClass(), "saveRVDefaultProbabilityDistribution is not implemented yet");
	}



	/**
	 * This is a utility method for saving simple arguments (i.e. arguments consisting of an ordinary variable only).
	 * It assumes {@link #getOrdinaryVariableCache()} is filled.
	 * @param ovArgument : ordinary variable of the argument
	 * @param mExpressionIndividual : owl individual to update. It is supposed to be an individual of MExpression.
	 * @param node : original node containing argument (e.g. {@link ResidentNode}, {@link InputNode}...). This is used maily to extract the name.
	 * If null,  mExpressionIndividual will be used to create the name.
	 * @param argumentNumber : a number for ordering purpose.
	 * @param mebn : MEBN containing the node.
	 * @param ontology	: ontology containing owlIndividual
	 * This version only allows OrdinaryVariableArgument. TODO allow ConstantArgument, MExpressionArgument or ExemplarArgument
	 * @see #saveMTheoryAndMFrags(MultiEntityBayesianNetwork, OWLOntology)
	 */
	protected void saveOrdinaryVariableArgument(OrdinaryVariable ovArgument, OWLIndividual mExpressionIndividual, INode node, int argumentNumber,MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		// TODO initial assertions
		
		PrefixManager prowl2Prefix = this.getDefaultPrefixManager(); // prefix of PR-OWL2 definition ontology
		PrefixManager currentPrefix = this.getOntologyPrefixManager(ontology);	// prefix of current ontology
		
		// the name of new argument
		String nameOfArgumentIndividual = "";
		if (node == null) {
			nameOfArgumentIndividual = this.extractName(mExpressionIndividual); // use MExpression's name as a base
		} else {
			nameOfArgumentIndividual = node.getName();	// use node's name as a base
		}
		nameOfArgumentIndividual += IPROWL2ModelUser.ARGUMENT_NUMBER_SEPARATOR + argumentNumber;	// add suffix to name of argument
		
		// extract factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// hasArgument
    	OWLObjectProperty hasArgumentProperty = factory.getOWLObjectProperty(this.getHasArgumentPropertyName(), prowl2Prefix); 
    	
    	// this version only allows OrdinaryVariableArgument
		OWLClass argumentClass = factory.getOWLClass(ORDINARYVARIABLEARGUMENT, prowl2Prefix); 
		
		// get new argument
		OWLIndividual argumentIndividual = factory.getOWLNamedIndividual(nameOfArgumentIndividual, currentPrefix);
		
		// specify that argumentClass is the type of argumentIndividual
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLClassAssertionAxiom(argumentClass, argumentIndividual)
		);
		
		// add axiom: mExpressionIndividual -> hasArgument -> argumentIndividual
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(hasArgumentProperty, mExpressionIndividual, argumentIndividual)
		);
		
		// add axiom: argumentIndividual -> typeOfArgument -> ovArgument
		if(ovArgument != null){
			// extract owl individual representing the ordinary variable ovArgument
			OWLIndividual oVariableIndividual = this.getOrdinaryVariableCache().get(ovArgument); 
			if (oVariableIndividual == null) {
				throw new IllegalStateException("Ordinary variable " + ovArgument + " is expected to be in cache. Please call saveMTheoryAndMFrags before saveSimpleArgRelationship");
			}
			// create and commit axiom
			ontology.getOWLOntologyManager().addAxiom(
					ontology, 
					factory.getOWLObjectPropertyAssertionAxiom(
							factory.getOWLObjectProperty(TYPEOFARGUMENT, prowl2Prefix), // typeOfArgument
							argumentIndividual, // subject
							oVariableIndividual	// object
					)
			);
			// create and commit inverse
			ontology.getOWLOntologyManager().addAxiom(
					ontology, 
					factory.getOWLObjectPropertyAssertionAxiom(
							factory.getOWLObjectProperty(ISTYPEOFARGUMENTIN, prowl2Prefix), // typeOfArgument
							oVariableIndividual	,
							argumentIndividual
					)
			);
		}
		
		// add axiom: argumentIndividual -> isArgumentOf -> mExpressionIndividual
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLObjectPropertyAssertionAxiom(
						factory.getOWLObjectProperty(ISARGUMENTOF, prowl2Prefix), // isArgumentOf
						argumentIndividual, // subject
						mExpressionIndividual	// object
				)
		);
		
		// add axiom: argumentIndividual -> hasArgumentNumber = argNumber
		ontology.getOWLOntologyManager().addAxiom(
				ontology, 
				factory.getOWLDataPropertyAssertionAxiom(
						factory.getOWLDataProperty(HASARGUMENTNUMBER, prowl2Prefix), // hasArgumentNumber
						argumentIndividual, // subject
						argumentNumber	// object
				)
		);
		
	}



	/**
	 * This method fills the ontology with mebn data (the MTheory, MFrags, and nodes), but the contents themselves will be saved at {@link #saveContextNodes(MultiEntityBayesianNetwork, OWLOntology)},
	 * {@link #saveDomainResidentNodes(MultiEntityBayesianNetwork, OWLOntology)}, {@link #saveGenerativeInputNodes(MultiEntityBayesianNetwork, OWLOntology)}, etc.
	 * @param mebn : object where MEBN data will be extracted from.
	 * @param ontology : object representing OWL ontology, where MEBN data will be filled in.
	 */
	protected void saveMTheoryAndMFrags(MultiEntityBayesianNetwork mebn, OWLOntology ontology) {
		
		// extract PR-OWL 2 prefix
		PrefixManager prowl2PrefixManager = this.getDefaultPrefixManager();
		
		// create prefix for this ontology
		PrefixManager currentPrefixManager = this.getOntologyPrefixManager(ontology);
		
		// extract mtheory class
		OWLClass mTheoryClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.MTHEORY, prowl2PrefixManager); 
		
		// check if individual exists
		// OBS. it is easier to return all instances of MTheory and perform a search, because OWLAPI's owl data factory only looks for an individual given its prefix
		// (thus it does not look for individuals of all possible prefixes/context).
		OWLIndividual mTheoryIndividual = null;
		for (OWLIndividual individual : mTheoryClass.getIndividuals(ontology)) {
			if (mebn.getName().equals(this.extractName(individual))) {
				mTheoryIndividual = individual;
				break;
			}
		}
		if (mTheoryIndividual == null) {
			// if this individual is new, create it using current prefix manager (prefix of THIS ontology - not PR-OWL2 prefix)
			mTheoryIndividual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(mebn.getName(), currentPrefixManager); 
			// Create an axiom that assures that mTheoryIndividual is an individual of mTheoryClass
			OWLClassAssertionAxiom classAssertion = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(mTheoryClass, mTheoryIndividual);
			// add axiom to ontology
			ontology.getOWLOntologyManager().addAxiom(ontology, classAssertion);
		}
		
		Debug.println(this.getClass(), "MTheory = " + mebn.getName());
		
		// add comment 
		if(mebn.getDescription() != null){
			// create comment annotation
			OWLAnnotation commentAnno = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(
					ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
					ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(mebn.getDescription()));
			// create axiom to add annotation to mTheoryIndividual
			OWLAxiom commentAxiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationAssertionAxiom(mTheoryIndividual.asOWLNamedIndividual().getIRI(), commentAnno);
			// Add the axiom to the ontology
			ontology.getOWLOntologyManager().addAxiom(ontology, commentAxiom);
		}
		
		/* hasMFrag */
		
		// extract hasMFrag object property
		OWLObjectProperty hasMFragProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasMFragObjectProperty(), prowl2PrefixManager); 	
		
		// iterate over mfrags in mebn
		if (mebn.getDomainMFragList() != null) {
			for(MFrag domainMFrag: mebn.getDomainMFragList()){
				if (domainMFrag == null || domainMFrag.getName() == null || (domainMFrag.getName().trim().length() <= 0)) {
					continue;	// avoid saving anonymous MFrag
				}
				// extract DomainMFrag Class
				OWLClass domainMFragClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAINMFRAG, prowl2PrefixManager); 
				
				try{
					Debug.println(this.getClass(), "Domain_MFrag = " + domainMFrag.getName());
				}catch (Throwable t) {
					t.printStackTrace();
				}
				
				// check if individual exists in cache.
				OWLIndividual domainMFragIndividual = this.getMFragCache().get(domainMFrag);
				if (domainMFragIndividual == null) {
					// if new, create it
					domainMFragIndividual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(SaverPrOwlIO.MFRAG_NAME_PREFIX + domainMFrag.getName(), currentPrefixManager);
					// Create an axiom that assures that domainMFragIndividual is an individual of domainMFragClass
					OWLClassAssertionAxiom classAssertion = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(domainMFragClass, domainMFragIndividual);
					// add axiom to ontology
					ontology.getOWLOntologyManager().addAxiom(ontology, classAssertion);
				}
				
				// put to cache
				this.getMFragCache().put(domainMFrag, domainMFragIndividual); 
				
				// link mTheoryIndividual to domainMFragIndividual using hasMFragProperty property and commit axiom
				ontology.getOWLOntologyManager().addAxiom(
						ontology, 
						ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
							hasMFragProperty, 
							mTheoryIndividual, 
							domainMFragIndividual
						)
				);
				// link inverse
				ontology.getOWLOntologyManager().addAxiom(
						ontology, 
						ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(ISMFRAGOF, prowl2PrefixManager),
							domainMFragIndividual,
							mTheoryIndividual
						)
				);
				
				// add comment
				if(domainMFrag.getDescription()!=null){
					// create comment annotation
					OWLAnnotation commentAnno = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(domainMFrag.getDescription()));
					// create axiom to add annotation to mTheoryIndividual
					OWLAxiom commentAxiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationAssertionAxiom(domainMFragIndividual.asOWLNamedIndividual().getIRI(), commentAnno);
					// Add the axiom to the ontology
					ontology.getOWLOntologyManager().addAxiom(ontology, commentAxiom);
				}
				
				/* hasResidentNode */
				OWLObjectProperty hasResidentNodeProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasResidentNodeObjectPropertyName(), prowl2PrefixManager); 	
				OWLClass domainResClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAINRESIDENT, prowl2PrefixManager); 
				for(ResidentNode residentNode: domainMFrag.getResidentNodeList()){
					try {
						Debug.println(this.getClass(), "Domain_Res = " + residentNode.getName());	
					} catch (Throwable t) {
						t.printStackTrace();
					}
					if (residentNode == null
							|| residentNode.getName() == null
							|| residentNode.getName().trim().length() <= 0) {
						// avoid anonimous 
						continue;
					}
					// get individual 
					OWLIndividual domainResIndividual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(SaverPrOwlIO.RESIDENT_NAME_PREFIX + residentNode.getName(), currentPrefixManager);
					if (!domainResIndividual.getTypes(ontology).contains(domainResClass)) {
						// if new, add it to ontology and commit change
						ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(domainResClass, domainResIndividual)
							);
					}
					// link domainMFragIndividual to domainResIndividual using hasResidentNodeProperty and commit axiom
					ontology.getOWLOntologyManager().addAxiom(
							ontology, 
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
								hasResidentNodeProperty, 
								domainMFragIndividual, 
								domainResIndividual
							)
					);
					// link inverse
					ontology.getOWLOntologyManager().addAxiom(
							ontology, 
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(ISRESIDENTNODEIN, prowl2PrefixManager), 
								domainResIndividual,
								domainMFragIndividual
							)
					);
					// add comment
					if(residentNode.getDescription()!=null){
						// create comment annotation
						OWLAnnotation commentAnno = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(residentNode.getDescription()));
						// create axiom to add annotation to individual
						OWLAxiom commentAxiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationAssertionAxiom(domainResIndividual.asOWLNamedIndividual().getIRI(), commentAnno);
						// Add the axiom to the ontology
						ontology.getOWLOntologyManager().addAxiom(ontology, commentAxiom);
					}
					// add node to cache
					this.getDomainResidentCache().put(residentNode, domainResIndividual); 
					
				}	
				
				/* hasInputNode */
				OWLObjectProperty hasInputNodeProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasInputNodeObjectPropertyName(), prowl2PrefixManager); 	
				OWLClass generativeInputClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.GENERATIVEINPUT, prowl2PrefixManager); 
				for(InputNode inputNode: domainMFrag.getInputNodeList()){
					try{
						Debug.println(this.getClass(), "Generative_input = " + inputNode.getName());
					} catch (Throwable t) {
						t.printStackTrace();
					}
					// get individual 
					OWLIndividual generativeInputIndividual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(inputNode.getName(), currentPrefixManager);
					if (!generativeInputIndividual.getTypes(ontology).contains(generativeInputClass)) {
						// if new, add it to ontology and commit change
						ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(generativeInputClass, generativeInputIndividual)
							);
					}
					// link domainMFragIndividual to generativeInputIndividual using hasInputNodeProperty and commit axiom
					ontology.getOWLOntologyManager().addAxiom(
							ontology, 
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
								hasInputNodeProperty, 
								domainMFragIndividual, 
								generativeInputIndividual
							)
					);
					// link inverse
					ontology.getOWLOntologyManager().addAxiom(
							ontology, 
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(ISINPUTNODEIN, prowl2PrefixManager), 
								generativeInputIndividual,
								domainMFragIndividual
							)
					);
					// add comment
					if(inputNode.getDescription()!=null){
						// create comment annotation
						OWLAnnotation commentAnno = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(inputNode.getDescription()));
						// create axiom to add annotation to individual
						OWLAxiom commentAxiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationAssertionAxiom(generativeInputIndividual.asOWLNamedIndividual().getIRI(), commentAnno);
						// Add the axiom to the ontology
						ontology.getOWLOntologyManager().addAxiom(ontology, commentAxiom);
					}
					// update cache
					this.getGenerativeInputCache().put(inputNode, generativeInputIndividual); 		
				}				
				
				
				/* hasContextNode */
				OWLObjectProperty hasContextNodeProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasContextNodeObjectPropertyName(), prowl2PrefixManager); 	
				OWLClass contextClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.CONTEXTNODE, prowl2PrefixManager); 
				for(ContextNode contextNode: domainMFrag.getContextNodeList()){
					// get individual 
					OWLIndividual contextIndividual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(contextNode.getName(), currentPrefixManager);
					if (!contextIndividual.getTypes(ontology).contains(contextClass)) {
						// if new, add it to ontology and commit change
						ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(contextClass, contextIndividual)
							);
					}
					// link domainMFragIndividual to contextIndividual using hasContextNodeProperty and commit axiom
					ontology.getOWLOntologyManager().addAxiom(
							ontology, 
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
								hasContextNodeProperty, 
								domainMFragIndividual, 
								contextIndividual
							)
					);
					// link inverse
					ontology.getOWLOntologyManager().addAxiom(
							ontology, 
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(ISCONTEXTNODEIN, prowl2PrefixManager), 
								contextIndividual,
								domainMFragIndividual
							)
					);
					// add comment
					if(contextNode.getDescription()!=null){
						// create comment annotation
						OWLAnnotation commentAnno = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(contextNode.getDescription()));
						// create axiom to add annotation to individual
						OWLAxiom commentAxiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationAssertionAxiom(contextIndividual.asOWLNamedIndividual().getIRI(), commentAnno);
						// Add the axiom to the ontology
						ontology.getOWLOntologyManager().addAxiom(ontology, commentAxiom);
					}
					// update cache
					this.getContextCache().put(contextNode, contextIndividual); 	
				}				
				
				/* hasOVariable */
				OWLObjectProperty hasOVariableProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasOVariableObjectPropertyName(), prowl2PrefixManager); 	
				OWLClass oVariableClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.ORDINARYVARIABLE, prowl2PrefixManager); 
				
				/* isSubstitutedBy*/
				OWLDataProperty isSubsByProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(this.getIsSubsByObjectPropertyName(),prowl2PrefixManager); 	
				
				for(OrdinaryVariable oVariable: domainMFrag.getOrdinaryVariableList()){
					// Set variable name as "MFragName.OVName"
					OWLIndividual oVariableIndividual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(
							oVariable.getMFrag().getName() + this.getOVariableScopeSeparator() + oVariable.getName() , 
							currentPrefixManager
					);
					if (!oVariableIndividual.getTypes(ontology).contains(oVariableClass)) {
						// if new, add it to ontology and commit change
						ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(oVariableClass, oVariableIndividual)
							);
					}
					// link domainMFragIndividual to oVariableIndividual using hasOVariableProperty and commit axiom
					ontology.getOWLOntologyManager().addAxiom(
							ontology, 
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectPropertyAssertionAxiom(
								hasOVariableProperty, 
								domainMFragIndividual, 
								oVariableIndividual
							)
					);
					
					// update type
					if (oVariable.getValueType() != null){
						// add axiom which links oVariableIndividual to <EntityName>^^AnyURI
						ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(
									isSubsByProperty, 
									oVariableIndividual, 
									ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(
											( (this.getMetaEntityCache().get(oVariable.getValueType().getName()) == null)?this.extractName(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing()):(this.getMetaEntityCache().get(oVariable.getValueType().getName())) ) , 
											OWL2Datatype.XSD_ANY_URI
									)
								)
					    );
					}
					
					// add comment
					if(oVariable.getDescription()!=null){
						// create comment annotation
						OWLAnnotation commentAnno = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotation(
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI()),
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLLiteral(oVariable.getDescription()));
						// create axiom to add annotation to individual
						OWLAxiom commentAxiom = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationAssertionAxiom(oVariableIndividual.asOWLNamedIndividual().getIRI(), commentAnno);
						// Add the axiom to the ontology
						ontology.getOWLOntologyManager().addAxiom(ontology, commentAxiom);
					}
					
					// update cache
					this.getOrdinaryVariableCache().put(oVariable, oVariableIndividual); 				
				}				
			}    	
		}
		
	}

	/**
	 * This method clears all PR-OWL2 individuals, and all PR-OWL classes and properties, so that new ones can be inserted without
	 * forgetting to remove the old ones.
	 * @param ontology
	 * @param mebn
	 */
	protected void clearAllPROWLOntologyObjects(OWLOntology ontology, MultiEntityBayesianNetwork mebn) {
		// prepare entity remover
		OWLEntityRemover entityRemover = new OWLEntityRemover(ontology.getOWLOntologyManager(), Collections.singleton(ontology));
		
		// clear PR-OWL individuals
		if (this.getPROWL2IndividualsExtractor() != null) {
			// initialize individuals extractor
			this.getPROWL2IndividualsExtractor().resetPROWL2IndividualsExtractor();
			// we call getPROWL2Individuals setting reasoner as null so that only asserted individuals are returned
			for (OWLIndividual individual : this.getPROWL2IndividualsExtractor().getPROWL2Individuals(ontology, null)) {
				// mark individual as removed if it is a named individual.
				if (individual.isNamed()) {
					individual.asOWLNamedIndividual().accept(entityRemover);
					try {
						Debug.println(this.getClass(), "Warning: " + individual + " was considered a PR-OWL2 individual (thus, a MEBN component) and was removed before update.");
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
			}
		}
		
		// clear PR-OWL classes too
		for (OWLClassExpression prowlClass : this.getPROWLClasses(ontology)) {
			if (prowlClass instanceof OWLEntity) {
				OWLEntity owlEntity = (OWLEntity) prowlClass;
				owlEntity.accept(entityRemover);
				try {
					Debug.println(this.getClass(), "Warning: " + owlEntity + " was considered a PR-OWL class (thus, a MEBN component) and was removed before update.");
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
		
		// clear pr-owl object properties including the imports
		for (OWLObjectProperty property : ontology.getObjectPropertiesInSignature(true)) {
			if (property.getIRI().toURI().toString().startsWith(OLD_PROWL_NAMESPACEURI)) {
				property.accept(entityRemover);
			}
		}

		// clear pr-owl data properties as well
		for (OWLDataProperty property : ontology.getDataPropertiesInSignature(true)) {
			if (property.getIRI().toURI().toString().startsWith(OLD_PROWL_NAMESPACEURI)) {
				property.accept(entityRemover);
			}
		}
		
		// commit changes
		ontology.getOWLOntologyManager().applyChanges(entityRemover.getChanges());
	}


	/**
	 * @return the recursivelyAddedArgumentsOfMExpression
	 */
	protected Map<String, Argument> getRecursivelyAddedArgumentsOfMExpression() {
		return recursivelyAddedArgumentsOfMExpression;
	}

	/**
	 * @param recursivelyAddedArgumentsOfMExpression the recursivelyAddedArgumentsOfMExpression to set
	 */
	protected void setRecursivelyAddedArgumentsOfMExpression(
			Map<String, Argument> recursivelyAddedArgumentsOfMExpression) {
		this.recursivelyAddedArgumentsOfMExpression = recursivelyAddedArgumentsOfMExpression;
	}

	/**
	 * Requests for PR-OWL2-specific individuals will be delegated to this object.
	 * {@link #saveMebn(File, MultiEntityBayesianNetwork)} will use this object
	 * in order to clear old PR-OWL2 individuals before setting the new ones.
	 * @return the prowl2IndividualsExtractor
	 */
	public IPROWL2IndividualsExtractor getPROWL2IndividualsExtractor() {
		return prowl2IndividualsExtractor;
	}

	/**
	 * Requests for PR-OWL2-specific individuals will be delegated to this object.
	 * {@link #saveMebn(File, MultiEntityBayesianNetwork)} will use this object
	 * in order to clear old PR-OWL2 individuals before setting the new ones.
	 * @param prowl2IndividualsExtractor the prowl2IndividualsExtractor to set
	 */
	public void setPROWL2IndividualsExtractor(
			IPROWL2IndividualsExtractor prowl2IndividualsExtractor) {
		this.prowl2IndividualsExtractor = prowl2IndividualsExtractor;
	}
	
	/**
	 * This method is a facilitator to clear and initialize internal cache.
	 * @see #getMapFilledInputNodes()
	 * @see #getRecursivelyAddedArgumentsOfMExpression()
	 * @see #getMFragCache()
	 * @see #getDomainResidentCache() 
	 */
	public void resetCache() {
		try {
			super.resetCache();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize some maps
			this.setMapFilledInputNodes(new HashMap<String, InputNode>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize some maps
			this.setRecursivelyAddedArgumentsOfMExpression(new HashMap<String, Argument>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		try {
			// initialize MFrag cache
			this.setMFragCache(new HashMap<MFrag, OWLIndividual>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize ResidentNode cache
			this.setDomainResidentCache(new HashMap<ResidentNode, OWLIndividual>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize InputNode cache
			this.setGenerativeInputCache(new HashMap<InputNode, OWLIndividual>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize ContextNode cache
			this.setContextCache(new HashMap<ContextNode, OWLIndividual>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize MetaEntity cache
			this.setMetaEntityCache(new HashMap<String, String>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize Entity cache
			this.setObjectEntityClassesCache(new HashMap<Entity, OWLClassExpression>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize ordinary variable cache
			this.setOrdinaryVariableCache(new HashMap<OrdinaryVariable, OWLIndividual>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize categorical state entity's cache
			this.setCategoricalStatesCache(new HashMap<CategoricalStateEntity, OWLIndividual>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize random variable's cache
			this.setRandomVariableCache(new HashMap<ResidentNode, OWLIndividual>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize built in random variable's cache
			this.setBuiltInRVCache(new HashMap<String, OWLIndividual>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// initialize built in random variable's cache
			this.setRandomVariableIndividualToResidentNodeSetCache(new HashMap<OWLIndividual, Set<ResidentNode>>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		try {
			// MappingArgument's cache
			this.setMappingArgumentCache(new HashMap<ResidentNode, OWLIndividual>());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	

	/**
	 * @return the randomVariableIndividualToResidentNodeSetCache
	 */
	protected Map<OWLIndividual, Set<ResidentNode>> getRandomVariableIndividualToResidentNodeSetCache() {
		return randomVariableIndividualToResidentNodeSetCache;
	}



	/**
	 * @param randomVariableIndividualToResidentNodeSetCache the randomVariableIndividualToResidentNodeSetCache to set
	 */
	protected void setRandomVariableIndividualToResidentNodeSetCache(
			Map<OWLIndividual, Set<ResidentNode>> randomVariableIndividualToResidentNodeSetCache) {
		this.randomVariableIndividualToResidentNodeSetCache = randomVariableIndividualToResidentNodeSetCache;
	}



	/**
	 * This map contains a temporary cache for MFrags and OWL individuals representing them.
	 * @return the mfragCache
	 */
	protected Map<MFrag, OWLIndividual> getMFragCache() {
		return mfragCache;
	}

	/**
	 * This map contains a temporary cache for MFrags and OWL individuals representing them.
	 * @param mfragCache the mfragCache to set
	 */
	protected void setMFragCache(Map<MFrag, OWLIndividual> mfragCache) {
		this.mfragCache = mfragCache;
	}

	/**
	 * This map contains a temporary cache for DomainResidentNodes and OWL individuals representing them.
	 * @return the domainResidentCache
	 */
	protected Map<ResidentNode, OWLIndividual> getDomainResidentCache() {
		return domainResidentCache;
	}

	/**
	 * This map contains a temporary cache for DomainResidentNodes and OWL individuals representing them.
	 * @param domainResidentCache the domainResidentCache to set
	 */
	protected void setDomainResidentCache(
			Map<ResidentNode, OWLIndividual> domainResidentCache) {
		this.domainResidentCache = domainResidentCache;
	}

	/**
	 * This map contains a temporary cache for {@link InputNode} and OWL individuals representing them.
	 * @return the generativeInputCache
	 */
	protected Map<InputNode, OWLIndividual> getGenerativeInputCache() {
		return generativeInputCache;
	}

	/**
	 * This map contains a temporary cache for {@link InputNode} and OWL individuals representing them.
	 * @param generativeInputCache the generativeInputCache to set
	 */
	protected void setGenerativeInputCache(
			Map<InputNode, OWLIndividual> generativeInputCache) {
		this.generativeInputCache = generativeInputCache;
	}

	/**
	 * This map contains a temporary cache for {@link ContextNode} and OWL individuals representing them.
	 * @return the contextCache
	 */
	protected Map<ContextNode, OWLIndividual> getContextCache() {
		return contextCache;
	}

	/**
	 * This map contains a temporary cache for {@link ContextNode} and OWL individuals representing them.
	 * @param contextCache the contextCache to set
	 */
	protected void setContextCache(Map<ContextNode, OWLIndividual> contextCache) {
		this.contextCache = contextCache;
	}

	/**
	 * This is just a map from meta entity label to its corresponding URI value.
	 * @return the metaEntityCache
	 */
	protected Map<String, String> getMetaEntityCache() {
		return metaEntityCache;
	}

	/**
	 * This is just a map from meta entity label to its corresponding URI value.
	 * @param metaEntityCache the metaEntityCache to set
	 */
	protected void setMetaEntityCache(Map<String, String> metaEntityCache) {
		this.metaEntityCache = metaEntityCache;
	}

	/**
	 * This map contains a temporary cache for {@link Entity} and OWL classes representing them.
	 * @return the objectEntityClassesCache
	 */
	protected Map<Entity, OWLClassExpression> getObjectEntityClassesCache() {
		return objectEntityClassesCache;
	}

	/**
	 * This map contains a temporary cache for {@link Entity} and OWL classes representing them.
	 * @param objectEntityClassesCache the objectEntityClassesCache to set
	 */
	protected void setObjectEntityClassesCache(
			Map<Entity, OWLClassExpression> objectEntityClassesCache) {
		this.objectEntityClassesCache = objectEntityClassesCache;
	}

	/**
	 * This map contains a temporary cache for {@link OrdinaryVariable} and OWL individuals representing them.
	 * @return the ordinaryVariableCache
	 */
	protected Map<OrdinaryVariable, OWLIndividual> getOrdinaryVariableCache() {
		return ordinaryVariableCache;
	}

	/**
	 * This map contains a temporary cache for {@link OrdinaryVariable} and OWL individuals representing them.
	 * @param ordinaryVariableCache the ordinaryVariableCache to set
	 */
	protected void setOrdinaryVariableCache(
			Map<OrdinaryVariable, OWLIndividual> ordinaryVariableCache) {
		this.ordinaryVariableCache = ordinaryVariableCache;
	}






	/**
	 * This map contains a temporary cache for {@link CategoricalStateEntity} and OWL individuals representing them.
	 * @return the categoricalStatesCache
	 */
	protected Map<CategoricalStateEntity, OWLIndividual> getCategoricalStatesCache() {
		return categoricalStatesCache;
	}



	/**
	 * This map contains a temporary cache for {@link CategoricalStateEntity} and OWL individuals representing them.
	 * @param categoricalStatesCache the categoricalStatesCache to set
	 */
	protected void setCategoricalStatesCache(
			Map<CategoricalStateEntity, OWLIndividual> categoricalStatesCache) {
		this.categoricalStatesCache = categoricalStatesCache;
	}



	/**
	 * This is the name of hasMExpression object property.
	 * The default value is {@link IPROWL2ModelUser#HASMEXPRESSION}
	 * @return the hasMExpressionPropertyName
	 */
	public String getHasMExpressionPropertyName() {
		return hasMExpressionPropertyName;
	}



	/**
	 * This is the name of hasMExpression object property.
	 * The default value is {@link IPROWL2ModelUser#HASMEXPRESSION}
	 * @param hasMExpressionPropertyName the hasMExpressionPropertyName to set
	 */
	public void setHasMExpressionPropertyName(String hasMExpressionPropertyName) {
		this.hasMExpressionPropertyName = hasMExpressionPropertyName;
	}



	/**
	 * @return the isMExpressionOfPropertyName
	 */
	public String getIsMExpressionOfPropertyName() {
		return isMExpressionOfPropertyName;
	}



	/**
	 * @param isMExpressionOfPropertyName the isMExpressionOfPropertyName to set
	 */
	public void setIsMExpressionOfPropertyName(String isMExpressionOfPropertyName) {
		this.isMExpressionOfPropertyName = isMExpressionOfPropertyName;
	}



	/**
	 * This is a cache for individuals of RandomVariable linked to a resident node.
	 * @return the randomVariableCache
	 */
	protected Map<ResidentNode, OWLIndividual> getRandomVariableCache() {
		return randomVariableCache;
	}



	/**
	 * This is a cache for individuals of RandomVariable linked to a resident node.
	 * @param randomVariableCache the randomVariableCache to set
	 */
	protected void setRandomVariableCache(
			Map<ResidentNode, OWLIndividual> randomVariableCache) {
		this.randomVariableCache = randomVariableCache;
	}



	/**
	 * This is a cache of individuals of built in RVs. It mapps a built in RV's name
	 * to an owl individual.
	 * This is filled in {@link #saveBuiltInRV(MultiEntityBayesianNetwork, OWLOntology)}
	 * @return the builtInRVCache
	 */
	protected Map<String, OWLIndividual> getBuiltInRVCache() {
		return builtInRVCache;
	}



	/**
	 * This is a cache of individuals of built in RVs. It mapps a built in RV's name
	 * to an owl individual.
	 * This is filled in {@link #saveBuiltInRV(MultiEntityBayesianNetwork, OWLOntology)}
	 * @param builtInRVCache the builtInRVCache to set
	 */
	protected void setBuiltInRVCache(Map<String, OWLIndividual> builtInRVCache) {
		this.builtInRVCache = builtInRVCache;
	}



	/**
	 * This mapper is used by this class in order to delegate requisitions associated to 
	 * PR-OWL2 definition ontology (the PR-OWL2 base ontology) to another
	 * IRI (URI). If set to null, requisitions to the base ontology will be
	 * redirected to the default URI (i.e. {@link IPROWL2ModelUser#PROWL2_NAMESPACEURI},
	 * which is availabe on Web).
	 * Use this IRI mapper if you want to indicate a local file (or another URI) as the default PR-OWL2 definition
	 * ontology.
	 * @return the prowl2DefinitionIRIMapper
	 */
	public OWLOntologyIRIMapper getProwl2DefinitionIRIMapper() {
		return prowl2DefinitionIRIMapper;
	}



	/**
	 * This mapper is used by this class in order to delegate requisitions associated to 
	 * PR-OWL2 definition ontology (the PR-OWL2 base ontology) to another
	 * IRI (URI). If set to null, requisitions to the base ontology will be
	 * redirected to the default URI (i.e. {@link IPROWL2ModelUser#PROWL2_NAMESPACEURI},
	 * which is availabe on Web).
	 * Use this IRI mapper if you want to indicate a local file (or another URI)  the default PR-OWL2 definition
	 * ontology.
	 * @param prowl2DefinitionIRIMapper the prowl2DefinitionIRIMapper to set
	 */
	public void setProwl2DefinitionIRIMapper(
			OWLOntologyIRIMapper prowl2DefinitionIRIMapper) {
		this.prowl2DefinitionIRIMapper = prowl2DefinitionIRIMapper;
	}



	/**
	 * This is a path to an OWL file containing PR-OWL2 definitions.
	 * This path will be used in {@link #initialize()} so that {@link #getProwl2DefinitionIRIMapper()} 
	 * will be pointing to a file in this path.
	 * @return the prowl2ModelFilePath
	 */
	public String getPROWL2ModelFilePath() {
		return prowl2ModelFilePath;
	}



	/**
	 * This is a path to an OWL file containing PR-OWL2 definitions.
	 * This path will be used in {@link #initialize()} so that {@link #getProwl2DefinitionIRIMapper()} 
	 * will be pointing to a file in this path.
	 * @param prowl2ModelFilePath the prowl2ModelFilePath to set
	 */
	public void setPROWL2ModelFilePath(String prowl2ModelFilePath) {
		this.prowl2ModelFilePath = prowl2ModelFilePath;
	}



	/**
	 * This method will be used to fill {@link MultiEntityBayesianNetwork#getCategoricalStatesEntityContainer()}
	 * using values from {@link #getMapLoadedObjectEntityIndividuals()}. 
	 * This is done in order to instruct MEBN to consider all object entity individuals as categorical entities as well.
	 * Note that owl:Thing is considered an entity as well, so direct instances of owl:Thing will also
	 * be inserted as categorical state entity
	 * @see OWLAPICompatiblePROWLIO#loadCategoricalStateEntity(OWLOntology, MultiEntityBayesianNetwork)
	 */
	protected Map<String, CategoricalStateEntity> loadCategoricalStateEntity(
			OWLOntology ontology, MultiEntityBayesianNetwork mebn) {
		
		Map<String, CategoricalStateEntity> ret = new HashMap<String, CategoricalStateEntity>();
		
		// extract individuals of object entities and iterate
		for (ObjectEntityInstance instance : this.getMapLoadedObjectEntityIndividuals().values()){
			
			// create a categorical state
			CategoricalStateEntity state = mebn.getCategoricalStatesEntityContainer().createCategoricalEntity(instance.getName()); 

			try {
				IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(
						mebn, 
						state, 
						IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(mebn, instance)
				);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// we know name is supposed to be be marked as "used" already, but let's just make it sure
			mebn.getNamesUsed().add(instance.getName()); 
			
			
			// fill the returning value
			ret.put(instance.getName(), state);
			
			try {
				Debug.println(this.getClass(), "Categorical State Entity Loaded: " + state); 
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}	
		return ret;
	}



	/**
	 * This is a cache for arguments (individuals of prowl2:MappingArgument) linked indirectly to a resident node
	 * (i.e. ResidentNode -> RandomVariable -> MappingArgument).
	 * @return the mappingArgumentCache
	 */
	protected Map<ResidentNode, OWLIndividual> getMappingArgumentCache() {
		return mappingArgumentCache;
	}



	/**
	 * This is a cache for arguments (individuals of prowl2:MappingArgument) linked indirectly to a resident node
	 * (i.e. ResidentNode -> RandomVariable -> MappingArgument).
	 * @param mappingArgumentCache the mappingArgumentCache to set
	 */
	protected void setMappingArgumentCache(
			Map<ResidentNode, OWLIndividual> mappingArgumentCache) {
		this.mappingArgumentCache = mappingArgumentCache;
	}



	
	
}
