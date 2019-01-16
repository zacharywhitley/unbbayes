/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import unbbayes.gui.InternalErrorDialog;
import unbbayes.io.mebn.LoaderPrOwlIO;
import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.PROWLModelUser;
import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.SaverPrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Edge;
import unbbayes.prs.Graph;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IMEBNElementFactory;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.PROWL2MEBNFactory;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
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
import unbbayes.prs.mebn.entity.BooleanStateEntity;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.entity.ontology.owlapi.OWLAPIObjectEntityContainer;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
import unbbayes.prs.mebn.ontology.IOWLClassExpressionParserFacade;
import unbbayes.prs.mebn.ontology.protege.OWLClassExpressionParserFacade;
import unbbayes.util.Debug;

/**
 * This class extends {@link PrOwlIO} in order to
 * support OWL2 ontologies. Some refactories were made in order to improve extensibility (e.g. template method design pattern).
 * @author Shou Matsumoto
 *
 */
public class OWLAPICompatiblePROWLIO extends PrOwlIO implements IOWLAPIOntologyUser, IOWLClassExpressionParserFacade, INonPROWLClassExtractor {
	
	private String prowlOntologyNamespaceURI = PROWLMODELURI; // "http://www.pr-owl.org/pr-owl.owl" is the default
	
	private OWLOntology lastOWLOntology;
	
	private LoaderPrOwlIO wrappedLoaderPrOwlIO;
	
	private String hasMFragObjectProperty = "hasMFrag";
	
	private ResourceBundle resource;

	private Map<String, MFrag> mapNameToMFrag = new HashMap<String, MFrag>();

	private Map<String, Entity> mapLabelToObjectEntity = new HashMap<String, Entity>();

	private Map<String, Type> mapNameToType = new HashMap<String, Type>();

	private Map<String, Set<String>> mapCategoricalStateGloballyExclusiveNodes = new HashMap<String, Set<String>>();

	private Map<String, CategoricalStateEntity> mapCategoricalStates = new HashMap<String, CategoricalStateEntity>();

	private Map<String , Entity> mapBooleanStates = new HashMap<String, Entity>();

	private Map<String, INode> mapLoadedNodes = new HashMap<String, INode>();

	private Map<String, BuiltInRV> mapBuiltInRV = new HashMap<String, BuiltInRV>();

	private Map<String, ContextNode> mapTopLevelContextNodes = new HashMap<String, ContextNode>();

	private Map<String,Argument> mapArgument = new HashMap<String, Argument>();

	@Deprecated
	private Map<ContextNode, Object> mapIsContextInstanceOf = new HashMap<ContextNode, Object>();

	private Map<String, ResidentNode> mapFilledResidentNodes;

	private Map<String, InputNode> mapFilledInputNodes;

	private Map<String, OrdinaryVariable> mapFilledOrdinaryVariables;

	private Map<String, Argument> mapFilledArguments;

	private Map<String, Argument> mapFilledSimpleArguments;

	private Map<String, ObjectEntityInstance> mapLoadedObjectEntityIndividuals;
	
	private IMEBNElementFactory mebnFactory;
	
	private OWLReasoner owlReasoner;

	private IOWLClassExpressionParserFacade owlClassExpressionParserDelegator;

	private Collection<OWLClassExpression> nonPROWLClassesCache = new HashSet<OWLClassExpression>();
	
	private IPROWL2ModelUser prowlModelUserDelegator;
	
	private INonPROWLClassExtractor nonPROWLClassExtractor;
	
	private String hasResidentNodeObjectPropertyName = "hasResidentNode";
	private String hasInputNodeObjectPropertyName = "hasInputNode";
	private String hasContextNodeObjectPropertyName = "hasContextNode";
	private String hasOVariableObjectPropertyName = "hasOVariable";
	private String isSubsByObjectPropertyName = "isSubsBy";
	private String hasArgumentPropertyName = "hasArgument";
	
	private String oVariableScopeSeparator = ".";

	private Map<OWLOntology, PrefixManager> ontologyPrefixCache;
	

	private boolean initializeReasoner = false;
	
	
	/**
	 * The default constructor is public only because
	 * plug-in infrastructure may require default constructor
	 * (with no parameters) to be visible.
	 * It won't initialize complex fields (e.g. resource classes, wrapped classes, etc.).
	 * @deprecated use {@link #newInstance()} instead
	 */
	public OWLAPICompatiblePROWLIO() {
		super();
		try {
			this.initialize();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This is the constructor method to be used in order to create new instances of {@link OWLAPICompatiblePROWLIO}
	 * @return
	 */
	public static MebnIO newInstance() {
		return new OWLAPICompatiblePROWLIO();
	}
	
	/**
	 * Initialize default values
	 */
	protected void initialize() {
		this.setResource(unbbayes.util.ResourceController.newInstance().getBundle(
				unbbayes.io.mebn.resources.IoMebnResources.class.getName(),
				Locale.getDefault(),
				OWLAPICompatiblePROWLIO.class.getClassLoader()
			));
		this.setNonPROWLClassExtractor(DefaultNonPROWLClassExtractor.getInstance());
		IPROWL2ModelUser modelUser = DefaultPROWL2ModelUser.getInstance();
		// configure it to use http://www.pr-owl.org/pr-owl.owl as prowl default prefix
		((DefaultPROWL2ModelUser)modelUser).setProwlOntologyNamespaceURI("http://www.pr-owl.org/pr-owl.owl");
		this.setProwlModelUserDelegator(modelUser);
		this.setWrappedLoaderPrOwlIO(new LoaderPrOwlIO());	 // initialize default
		this.setMEBNFactory(PROWL2MEBNFactory.getInstance()); // initialize default
		this.resetCache();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.MebnIO#loadMebn(java.io.File)
	 */
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException,
			IOMebnException {
//		System.gc();
		// the main access point to ontologies is the OWLOntology and OWLOntologyManager (both from OWL API)
		if (this.getLastOWLOntology()  == null) {
			// load ontology from file and set as active
			try {
				this.setLastOWLOntology(OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file));
			} catch (OWLOntologyCreationException e) {
				if (file != null) {
					throw new IllegalArgumentException(file.toString(), e);
				} else {
					throw new IllegalArgumentException(e);
				}
			}
		}
		
		// specify reasoner if it is not set
		try {
			if (isToInitializeReasoner()) {
				if (this.getLastOWLReasoner() == null && this.getLastOWLOntology() != null) {
					this.setLastOWLReasoner(new Reasoner.ReasonerFactory().createReasoner(this.getLastOWLOntology()));
					this.getLastOWLReasoner().precomputeInferences();	// initialize
				}
			} else {
				// explicitly indicate that we don't want to use reasoners
				this.setLastOWLReasoner(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// extract default name
		String defaultMEBNName = "MEBN";
		try {
			defaultMEBNName = this.getLastOWLOntology().getOntologyID().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// instantiate MEBN
		MultiEntityBayesianNetwork mebn = this.getMEBNFactory().createMEBN(defaultMEBNName);
		
		// populate MEBN
		this.loadMEBNFromOntology(mebn, this.getLastOWLOntology(), this.getLastOWLReasoner());
		
		
		return mebn;
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
		OWLAPIObjectEntityContainer owlapiObjectEntityContainer = new OWLAPIObjectEntityContainer(mebn);
		owlapiObjectEntityContainer.setToCreateOWLEntity(false); // temporary disable automatic creation of entities
		mebn.setObjectEntityContainer(owlapiObjectEntityContainer);
		
		try {
			// Reset the non-PR-OWL classes extractor (this)
			this.resetNonPROWLClassExtractor();
			
			// update last owl reasoner
			if (reasoner != null) {
				this.setLastOWLReasoner(reasoner);
			}
			
			// this is a instance of MEBN to be filled. The name will be updated after loadMTheoryAndMFrags
//			MultiEntityBayesianNetwork mebn = this.getMEBNFactory().createMEBN(defaultMEBNName);
			IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, mebn, ontology.getOntologyID().getOntologyIRI());
			
		} catch (RuntimeException e) {
			owlapiObjectEntityContainer.setToCreateOWLEntity(true); // re-enable automatic creation of entities
			throw e;
		}
		
		// Start loading ontology. This is template method design pattern.

		// load MTheory. The nameToMFragMap maps a name to a MFrag.
		try {
			this.setMapNameToMFrag(this.loadMTheoryAndMFrags(ontology, mebn));
		} catch (IOMebnException e) {
			// the ontology does not contain PR-OWL specific elements. Stop loading PR-OWL and return an empty mebn.
			e.printStackTrace();
			owlapiObjectEntityContainer.setToCreateOWLEntity(true); // re-enable automatic creation of entities
			return;
		}
		
		try {
			// load object entities and fill the mapping of object entities
			this.setMapLabelToObjectEntity(this.loadObjectEntity(ontology, mebn));
			
			// load meta entities and fill the mapping of types
			this.setMapNameToType(this.loadMetaEntitiesClasses(ontology, mebn));
			
			// load categorical entities. The mapCategoricalStateGloballyExclusiveNodes stores globally exclusive nodes (a map of state -> nodes)
			this.setMapCategoricalStates(this.loadCategoricalStateEntity(ontology, mebn));
			
			// load boolean states. Reuse mapCategoricalStateGloballyExclusiveNodes.
			this.setMapBooleanStates(this.loadBooleanStateEntity(ontology, mebn));
			
			// load content of MFrag (nodes, ordinary variables, etc...)
			
			this.setMapLoadedNodes(this.loadDomainMFragContents(ontology, mebn));
			// load built in random variables. Reuse mapLoadedNodes.
			this.setMapBuiltInRV(this.loadBuiltInRV(ontology, mebn));
			
			// load content of context nodes. The mapIsContextInstanceOf mapps Context nodes to either BuiltInRV or ResidentNode. The mapArgument mapps a name to an argument
			this.setMapTopLevelContextNodes(this.loadContextNode( ontology, mebn));
			
			// load content of resident nodes
			this.setMapFilledResidentNodes(this.loadDomainResidentNode(ontology, mebn));
			
			// load content of input nodes
			this.setMapFilledInputNodes(this.loadGenerativeInputNode(ontology, mebn));
			
			// load content of ordinary variables
			this.setMapFilledOrdinaryVariables(this.loadOrdinaryVariable(ontology, mebn));
			
			// load generic arguments relationships
			this.setMapFilledArguments(this.loadArgRelationship(ontology, mebn));
			
			// load simple arguments
			this.setMapFilledSimpleArguments(this.loadSimpleArgRelationship(ontology, mebn));
			
			// adjust the order of arguments (the appearance order of arguments may not be the correct order)
			this.ajustArgumentOfNodes(mebn);
			
			// load the content of the formulas inside context nodes
			this.buildFormulaTrees(this.getMapTopLevelContextNodes(), mebn);
			
			// Load individuals of object entities (ObjectEntityInstances)
			this.setMapLoadedObjectEntityIndividuals(loadObjectEntityIndividuals(ontology, mebn));
		} catch (Exception e) {
			owlapiObjectEntityContainer.setToCreateOWLEntity(true); // re-enable automatic creation of entities
			throw new IllegalArgumentException("Failed to load ontology " + ontology, e);
		}
		

		owlapiObjectEntityContainer.setToCreateOWLEntity(true); // re-enable automatic creation of entities
		
//		return mebn;
	}


	

	
	

	/**
	 * Extract a simple name from an owl entity. If the ID of an entity is in [URI]#[Name] format, then it will extract the [Name].
	 * @param ontology : the ontology where the owlEntity belongs.
	 * @param owlEntity : the owlEntity from where we are going to extract a name.
	 * @return the name extracted from owlEntity.
	 */
	protected String extractName(OWLOntology ontology, OWLEntity owlEntity) {
		// TODO remove redundant calls to this method (some methods are calling this method more than once despite only 1 call was really necessary).
		return this.extractName(owlEntity);
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.IPROWL2ModelUser#extractName(org.semanticweb.owlapi.model.OWLObject)
	 */
	public String extractName(OWLObject owlObject) {
		return this.getProwlModelUserDelegator().extractName(owlObject);
	}
	

	/**
	 * Load the MTheory and the MFrags objects
	 * 
	 * Pre-requisites:
	 * - Only one MTheory per file 
	 * - The MFrags have different names
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return  a mapping from MFrag's name to a object of MFrag. This is useful to track
	 * MFrags if the name in ontologies differs from names visible to users. This is a input and output argument
	 * @throws IOMebnException when the ontology does not contain PR-OWL specific tags.
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
//				throw new IOMebnException(this.getResource().getString("MTheoryNotExist") , ontology + "");
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
			// we commented the following code because we want to ignore OWLIndividuals that are not OWLNamedIndividuas (which is a sub-interface of OWLEntity)
//			else if (mTheoryObject instanceof OWLIndividual) {
//				mtheoryName = this.extractName(ontology, ((OWLIndividual)mTheoryObject).asOWLNamedIndividual()); 
//				mebn.setName(mtheoryName); 
//			}
		}
		mebn.getNamesUsed().add(mebn.getName()); 
		
		Debug.println(this.getClass(), "MTheory loaded: " + mTheoryObject); 
		
		// start filling some properties
		
		// comments (description of mebn and mfrags)
		String mTheoryDescription = null;
		if (mTheoryObject instanceof OWLEntity) {
			mTheoryDescription = this.getDescription(ontology, (OWLEntity)mTheoryObject);
		} 
		// we commented the following code because we want to ignore OWLIndividuals that are not OWLNamedIndividuas (which is a sub-interface of OWLEntity)
//		else if (mTheoryObject instanceof OWLIndividual) {
//			mTheoryDescription = this.getDescription(ontology, mTheoryObject);
//		}
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
	
	/**
	 * Extracts MFrags from an MTheory. MFrags may be described as OWLClass or OWLIndividual. If no MTheory was specified,
	 * then all MFrags will be considered.
	 * @param mTheoryObject
	 * @param ontology
	 * @return
	 */
	protected Collection<OWLObject> getMFragsRelatedToMTheory(OWLObject mTheoryObject, OWLOntology ontology) {
		
		Set<OWLObject> ret = new HashSet<OWLObject>();
		
		if (ontology == null) {
			Debug.println(this.getClass(), "No ontology was specified in getMFragsRelatedToMTheory");
			return ret;
		}
		
		// look for the hasMFrag object property
		OWLObjectProperty hasMFragObjectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasMFragObjectProperty(), this.getDefaultPrefixManager());
		
		if (mTheoryObject == null || hasMFragObjectProperty == null) {
			// assume that if no MTheory was specified, all MFrags belong to the 1 anonymous MTheory
			// TODO make sure if this is really consistent (no MTheory in a ontology)
			OWLClass mfragClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAINMFRAG, this.getDefaultPrefixManager());
			if (!ontology.containsClassInSignature(mfragClass.getIRI(), true)) {
				// use old
				mfragClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.DOMAIN_MFRAG, this.getDefaultPrefixManager());
			}
			ret.addAll(this.getOWLSubclasses(mfragClass, ontology));
		} else {
			// extract only the related subclasses
			if (this.getLastOWLReasoner() != null) {
				// TODO extract only the related mfrags
			}
			
			// use asserted subclasses
			// TODO extract only the related mfrags
		}
		
		
		// use reasoner to extract individuals
		if (this.getLastOWLReasoner() != null
				&& mTheoryObject instanceof OWLIndividual ) {
			if (mTheoryObject != null 
					&& hasMFragObjectProperty != null) {
				// use property to obtain individuals
				try {
					ret.addAll(this.getObjectPropertyValues(((OWLIndividual)mTheoryObject), hasMFragObjectProperty,ontology));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// assume that if no MTheory was specified, all MFrags belong to the 1 anonymous MTheory
				// TODO make sure if this is really consistent (no MTheory in a ontology)
				OWLClass mfragClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAINMFRAG, this.getDefaultPrefixManager());
				if (!ontology.containsClassInSignature(mfragClass.getIRI(),true)) {
					mfragClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.DOMAIN_MFRAG, this.getDefaultPrefixManager());
				}
				ret.addAll(this.getOWLIndividuals(mfragClass, ontology));
			}
		}
		
		// use asserted individuals
		if (mTheoryObject != null 
				&& hasMFragObjectProperty != null
				&& mTheoryObject instanceof OWLIndividual ) {
			ret.addAll(((OWLIndividual)mTheoryObject).getObjectPropertyValues(hasMFragObjectProperty, ontology));
		}
		
		return ret;
	}

	/**
	 * Uses {@link #getLastOWLReasoner()} in order to obtain subclasses of a class expression from an ontology.
	 * If {@link #getLastOWLReasoner()} is null, then only the asserted individuals in ontology will be returned.
	 * @param owlClassExpression
	 * @param ontology
	 * @return
	 * @see #getOWLSuperclasses(OWLClassExpression, OWLOntology)
	 */
	public Set<OWLClassExpression> getOWLSubclasses(OWLClassExpression owlClassExpression, OWLOntology ontology) {
		Set<OWLClassExpression> ret = new HashSet<OWLClassExpression>();
		
		// try using reasoner first
		try {
			if (this.getLastOWLReasoner() != null) {
				NodeSet<OWLClass> subclasses = this.getLastOWLReasoner().getSubClasses(owlClassExpression, false);	// obtain indirect subclasses as well
				if (subclasses != null) {
					ret.addAll(subclasses.getFlattened());
					// comment the following try-catch and return if you want this method to work even when reasoner detects inconsistencies (unresolvable subclasses)
					try {
						// remove the nothing from returned subclasses
						ret.removeAll(this.getLastOWLReasoner().getUnsatisfiableClasses().getEntities());
//						ret.remove(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNothing());
					} catch (Exception e) {
						e.printStackTrace();
					}
					return ret;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Try asserted subclasses as well
		if (owlClassExpression instanceof OWLClass) {
			// if expression is exactly an OWL class, use this method
			try{
				ret.addAll(((OWLClass)owlClassExpression).getSubClasses(ontology));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
//		else {
//			// expression is a complex expression. use another method
//			try{
//				ret.addAll( (Set)owlClassExpression.getClassesInSignature());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		
		try {
			// remove the nothing from returned subclasses
			ret.remove(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNothing());
			ret.removeAll((ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNothing().getEquivalentClasses(ontology)));
			ret.removeAll((ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNothing().getSubClasses(ontology)));
//			ret.remove(ontology.getOWLOntologyManager().getOWLDataFactory().getOWLThing());
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return ret;
	}
	
	/**
	 * Uses {@link #getLastOWLReasoner()} in order to obtain superclasses of a class expression from an ontology.
	 * If {@link #getLastOWLReasoner()} is null, then only the asserted individuals in ontology will be returned.
	 * @param owlClassExpression
	 * @param ontology
	 * @param isDirect : if true, then only direct superclasses will be considered. If false, then all ancestors will be considered.
	 * @return
	 * @see #getOWLSubclasses(OWLClassExpression, OWLOntology)
	 */
	public Set<OWLClassExpression> getOWLSuperclasses(OWLClassExpression owlClassExpression, OWLOntology ontology, boolean isDirect) {
		Set<OWLClassExpression> ret = new HashSet<OWLClassExpression>();
		
		// try using reasoner first
		try {
			if (this.getLastOWLReasoner() != null) {
				NodeSet<OWLClass> superclasses = this.getLastOWLReasoner().getSuperClasses(owlClassExpression, isDirect);	
				if (superclasses != null) {
					ret.addAll(superclasses.getFlattened());
					return ret;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Try asserted subclasses as well
		if (owlClassExpression instanceof OWLClass) {
			// if expression is exactly an OWL class, use this method
			try{
				ret.addAll(((OWLClass)owlClassExpression).getSuperClasses(ontology));
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Debug.println(getClass(), "Attempted to obtain ancestors of anonymous class: " + owlClassExpression);
		}
		
		
		return ret;
	}

	/**
	 * Uses {@link #getLastOWLReasoner()} in order to obtain individuals of a class expression from an ontology.
	 * If {@link #getLastOWLReasoner()} is null, then only the asserted individuals in ontology will be returned.
	 * @param owlClassExpression
	 * @param ontology
	 * @return
	 */
	public Set<OWLIndividual> getOWLIndividuals(OWLClassExpression owlClassExpression, OWLOntology ontology) {
		Set<OWLIndividual> ret = new HashSet<OWLIndividual>();
		
		// try using reasoner first
		try {
			if (this.getLastOWLReasoner() != null) {
				NodeSet<OWLNamedIndividual> individuals = this.getLastOWLReasoner().getInstances(owlClassExpression, false);	// obtain indirect individuals as well
				if (individuals != null) {
					ret.addAll(individuals.getFlattened());
					// comment the following return if you want this method to be safer (returns individuals even when the reasoner detects unsolvable individuals)
					return ret;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Try asserted individuals as well
		if (owlClassExpression instanceof OWLClass) {
			// if expression is exactly an OWL class, use this method
			try{
				ret.addAll(((OWLClass)owlClassExpression).getIndividuals(ontology));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return ret;
		} else {
			// expression is a complex expression. use another method
			try{
				ret.addAll( (Set)owlClassExpression.getIndividualsInSignature());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return ret;
	}

	/**
	 * Load the Object Entities from the ontology. 
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from an Object Entity's name to an instance of {@link Entity}
	 */
	protected Map<String, Entity> loadObjectEntity(OWLOntology ontology, MultiEntityBayesianNetwork mebn){
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// TODO stop using subclasses of OBJECT_ENTITY and start using subclasses of Thing (except classes from PR-OWL definition)
		
		// this map will be returned by this method
		Map<String, Entity> mapObjectEntityLabels = new HashMap<String, Entity>();
		
		// look for object entities
		OWLClass objectEntityClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(OBJECT_ENTITY, prefixManager);
		
		// this is a list that will contain all the object entities
		List<OWLClassExpression> subClassesOfObjectEntities = new ArrayList<OWLClassExpression>();
		
		// actual subclasses of ObjectEntity
		// TODO remove ObjectEntity from PR-OWL definition
		subClassesOfObjectEntities.addAll(this.getOWLSubclasses(objectEntityClass, ontology));

		// also add classes that are not in PR-OWL definition
		subClassesOfObjectEntities.addAll(this.getNonPROWLClasses(ontology));
		
		// iterate on subclasses of object entities
		while (!subClassesOfObjectEntities.isEmpty()) {
			OWLClassExpression owlClassExpression = subClassesOfObjectEntities.remove(0);	// retrieve 1st element and delete it from list
			if (owlClassExpression == null) {
				Debug.println(getClass(), "Null class expression found as object entity. This may be caused by inconsistent DL reasoner...");
				continue;
			}
			OWLClass subClass = owlClassExpression.asOWLClass(); 
			if (subClass == null) {
				// it was a unknown type of class (maybe anonymous)
				Debug.println(getClass(), owlClassExpression + " was anonymous.");
				continue;
			}
			
			// It looks like the old loaderPrOwlIO was not solving hasType object property. We are following such decision.

			try{
				String objectEntityName = this.extractName(ontology, subClass);

				// try to handle parent of current object entity
				ObjectEntity parentObjectEntity = null;
				
				// get parent (direct ancestor) of this object property
				Set<OWLClassExpression> ancestors = this.getOWLSuperclasses(owlClassExpression, ontology, true);	// true = direct parent
				// disconsider owl:Thing
				ancestors.remove(getLastOWLOntology().getOWLOntologyManager().getOWLDataFactory().getOWLThing());
				// if the only direct ancestor was owl:Thing, then this is a root object entity right below owl:Thing (keep parent as null). Otherwise, there is a superclass
				if (!(ancestors.isEmpty())) {
					// TODO Current version allows only 1 superclass. Must allow multiple inheritance.
					String superEntityName = this.extractName(ontology, ancestors.iterator().next().asOWLClass());	// extract the 1st parent
					parentObjectEntity = mebn.getObjectEntityContainer().getObjectEntityByName(superEntityName);
					if (parentObjectEntity == null) {
						// parent was not loaded yet. Load later
						Debug.println(getClass(), objectEntityName + " has superclass " + superEntityName +". Attempting to load superclass before it.");
						subClassesOfObjectEntities.add(owlClassExpression);	// undo removal (add at end of list), so that it is considered after all root entities were handled.
						continue;
					}
				}
				
				
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
	 * Simply delegates to {@link #getNonPROWLClassExtractor()}
	 * @see unbbayes.io.mebn.owlapi.INonPROWLClassExtractor#getNonPROWLClasses(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public Collection<OWLClassExpression> getNonPROWLClasses(OWLOntology ontology) {
		return this.getNonPROWLClassExtractor().getNonPROWLClasses(ontology);
	}
	
	/**
	 * Simply delegates to {@link #getNonPROWLClassExtractor()}
	 * @see unbbayes.io.mebn.owlapi.INonPROWLClassExtractor#getPROWLClasses(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public Collection<OWLClassExpression> getPROWLClasses(OWLOntology ontology) {
		return this.getNonPROWLClassExtractor().getPROWLClasses(ontology);
	}
	
	/**
	 * Simply delegates to {@link #getNonPROWLClassExtractor()}
	 * @see unbbayes.io.mebn.owlapi.INonPROWLClassExtractor#getPROWLOntologyNamespaceURIs()
	 */
	public Collection<String> getPROWLOntologyNamespaceURIs() {
		return this.getNonPROWLClassExtractor().getPROWLOntologyNamespaceURIs();
	}

	/**
	 * Simply delegates to {@link #getNonPROWLClassExtractor()}
	 * @see unbbayes.io.mebn.owlapi.INonPROWLClassExtractor#setPROWLOntologyNamespaceURIs(java.util.Collection)
	 */
	public void setPROWLOntologyNamespaceURIs(Collection<String> prowlOntologyNamespaceURIs) {
		this.getNonPROWLClassExtractor().setPROWLOntologyNamespaceURIs(prowlOntologyNamespaceURIs);
	}

	/**
	 * Load the MetaEntities for types of the mebn structure. 
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from type's name to an instance of {@link Type}.
	 */
	protected Map<String, Type> loadMetaEntitiesClasses(OWLOntology ontology, MultiEntityBayesianNetwork mebn){

		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// the value to return
		Map<String, Type> ret = new HashMap<String, Type>();
		
		// extract meta entity
		OWLClass metaEntityClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(META_ENTITY, prefixManager);
		
		// this is a collection containing all meta-entities
		Collection<OWLIndividual> metaEntityIndividuals = this.getOWLIndividuals(metaEntityClass, ontology);
		
		// Actually, because the loadObjectEntities method is now loading non-PR-OWL classes as well, one would think that we should  also
		// automatically create labels for those classes here, but because the constructors of Object Entities  are doing this automatic
		// creation of labels (i.e individuals of Type), we do not need to explicitly generate them.
		// Also, it seems that the returned value of this method (value of ret) is never 
		// read by the other methods, so we don't have to be worried about this issue here.
		
		// extract individuals of meta entities
		for (OWLIndividual owlIndividual : metaEntityIndividuals){
			
			try{
				// create the meta entity (an object of Type)
			    Type type = mebn.getTypeContainer().createType(this.extractName(ontology, owlIndividual.asOWLNamedIndividual())); 
			    
			    try {
					IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, type, owlIndividual.asOWLNamedIndividual().getIRI());
				} catch (Exception e) {
					e.printStackTrace();
				}
			    
			    // fill the return 
			    ret.put(this.extractName(ontology, owlIndividual.asOWLNamedIndividual()), type);
			    
			    // mark name as "used"
				mebn.getNamesUsed().add(this.extractName(ontology, owlIndividual.asOWLNamedIndividual())); 
			} catch (TypeAlreadyExistsException e){
				// its OK to continue, because it is a known fact that some basic types were loaded in #loadObjectEntity method
				Debug.println(this.getClass(), "The type already exsists, but it is OK. " + owlIndividual, e);
			}
			
			Debug.println(this.getClass(), "Meta Entity Loaded: " + owlIndividual); 
						
		}
		
		return ret;
	}
	
	/**
	 * Load categorical entities. 
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from a name of categorical entity to an instance of CategoricalStateEntity.
	 */
	protected Map<String, CategoricalStateEntity> loadCategoricalStateEntity(OWLOntology ontology, MultiEntityBayesianNetwork mebn){

		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// the returning value
		Map<String, CategoricalStateEntity> ret = new HashMap<String, CategoricalStateEntity>();
		
		
		// extract the categorical states class
		OWLClass categoricalStateClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(CATEGORICAL_STATE, prefixManager);
		
		// extract individuals of categorical state class
		Collection<OWLIndividual> categoricalStateIndividuals = this.getOWLIndividuals(categoricalStateClass, ontology);
		
		// assume that non-PR-OWL classes are both object entities and categorical entities (so, let's add individuals of them as well)
		Collection<OWLClassExpression> nonPROWLClasses = this.getNonPROWLClasses(ontology);
		for (OWLClassExpression owlClassExpression : nonPROWLClasses) {
			// TODO make sure that the SSBN algorithm and the save method can handle individuals that are both categorical and object entities
			categoricalStateIndividuals.addAll(this.getOWLIndividuals(owlClassExpression, ontology));
		}
		
		// extract the globally exclusive property
		OWLObjectProperty globallyExclusiveObjectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isGloballyExclusive", prefixManager);
		
		// extract individuals and iterate
		for (OWLIndividual owlIndividual : categoricalStateIndividuals){
			
			// create a categorical state
			CategoricalStateEntity state = mebn.getCategoricalStatesEntityContainer().createCategoricalEntity(this.extractName(ontology, owlIndividual.asOWLNamedIndividual())); 

			try {
				IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, state, owlIndividual.asOWLNamedIndividual().getIRI());
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// mark name as "used"
			mebn.getNamesUsed().add(this.extractName(ontology, owlIndividual.asOWLNamedIndividual())); 
			
			// extract globally exclusive nodes related to this categorical entity
			Collection<OWLIndividual> globallyExclusiveObjects = this.getObjectPropertyValues(owlIndividual,globallyExclusiveObjectProperty, ontology);
			Set<String> globallyExclusiveNodeNames = new HashSet<String>();	// prepare the list of node's names. 
			for (OWLIndividual globallyExclusiveNode : globallyExclusiveObjects){
				globallyExclusiveNodeNames.add(this.extractName(ontology, globallyExclusiveNode.asOWLNamedIndividual())); 
			}
			
			// fill the map (the input/output argument of this method).
			this.getMapCategoricalStateGloballyExclusiveNodes().put(state.getName(), globallyExclusiveNodeNames); 
			
			// fill the returning value
			ret.put(this.extractName(ontology, owlIndividual.asOWLNamedIndividual()), state);
			
			Debug.println(this.getClass(), "Categorical State Entity Loaded: " + owlIndividual); 
		}	
		
		return ret;
	}
	
	/**
	 * Load boolean entities. 
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from a name of boolan entity to an instance of BooleanStateEntity.
	 */
	protected Map<String , Entity> loadBooleanStateEntity(OWLOntology ontology, MultiEntityBayesianNetwork mebn){
		
		// TODO start using xsd:boolean
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// return value
		Map<String , Entity> ret = new HashMap<String, Entity>();
		
		// extract globally exclusive property
		OWLObjectProperty globallyExclusiveObjectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isGloballyExclusive", prefixManager); 	

		// extract boolean states
		OWLClass booleanStateClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(BOOLEAN_STATE, prefixManager);
		
		// extract individuals and iterate
		for (OWLIndividual owlIndividual : booleanStateClass.getIndividuals(ontology)){
			
			//ignore anonymous individuals
			if (!owlIndividual.isNamed()) {
				continue;
			}
			
			// the boolean state 
			BooleanStateEntity state = null; 
			
			String owlIndividualName = this.extractName(ontology, owlIndividual.asOWLNamedIndividual());
			
			// use string comparison to translate string names to boolean entity
			if (owlIndividualName.equalsIgnoreCase("true") || owlIndividualName.endsWith("true")){
				state = mebn.getBooleanStatesEntityContainer().getTrueStateEntity(); 
				mebn.getNamesUsed().add(owlIndividualName); 	// mark name as "used"
			} else if (owlIndividualName.equalsIgnoreCase("false") || owlIndividualName.endsWith("false")){
				state = mebn.getBooleanStatesEntityContainer().getFalseStateEntity(); 
				mebn.getNamesUsed().add(owlIndividualName);  	// mark name as "used"
			} else if (owlIndividualName.equalsIgnoreCase("absurd") || owlIndividualName.endsWith("absurd")){
				state = mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity(); 
				mebn.getNamesUsed().add(owlIndividualName);  	// mark name as "used"
			} else {
				// There is something wrong (a boolean value with more than true/false/absurd...)
				Debug.println(this.getClass(), "A boolean state with a non-boolean value was found: " + owlIndividual);
				continue; // let's just ignore
			}
			
			if (state != null) {
				try {
					IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, state, owlIndividual.asOWLNamedIndividual().getIRI());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// fill globally exclusive node names
				// TODO the global exclusion can be simulated in open-world assumption by using (inverse <nameOfProperty> max 1) as super class
				Collection<OWLIndividual> globallyExclusiveIndividuals = this.getObjectPropertyValues(owlIndividual,globallyExclusiveObjectProperty, ontology); 
				Set<String> globallyExclusiveNodeNames = new HashSet<String>(); 
				for (OWLIndividual globallyExclusiveIndividual : globallyExclusiveIndividuals){
					globallyExclusiveNodeNames.add(this.extractName(ontology, globallyExclusiveIndividual.asOWLNamedIndividual())); 
				}
				this.getMapCategoricalStateGloballyExclusiveNodes().put(state.getName(), globallyExclusiveNodeNames); 
				
				// fill return
				ret.put(this.extractName(ontology, owlIndividual.asOWLNamedIndividual()), state);
			}
			
			Debug.println(this.getClass(), "Boolean State Entity Loaded: " + owlIndividual); 
		}	
		return ret;
	}
	

	/**
	 * Loads the contents (nodes) of domain mfrags from ontology
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return the loaded nodes (resident nodes, input nodes, context nodes, and ordinary variables)
	 * @throws IOMebnException
	 */
	protected Map<String, INode> loadDomainMFragContents(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{

		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// the return value
		Map<String, INode> mapMultiEntityNode = new HashMap<String, INode>();
		
		// extract mfrag class
		OWLClass owlNamedClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAINMFRAG, prefixManager); 
		if (!ontology.containsClassInSignature(owlNamedClass.getIRI(),true)) {
			// use the old PR-OWL definition
			owlNamedClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.DOMAIN_MFRAG, prefixManager); 
		}
		
		if (owlNamedClass != null) {
			// iterate on individuals
			for (OWLIndividual individualOne : this.getOWLIndividuals(owlNamedClass, ontology)){
				MFrag domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, individualOne.asOWLNamedIndividual())); 
				if (domainMFrag == null){
					System.err.println(this.getResource().getString("DomainMFragNotExistsInMTheory") + ". MFrag = " + individualOne); 
					continue;	// ignore the case when multiple MTheory reside in the same ontology.
				}
				
				Debug.println(this.getClass(), "DomainMFrag loaded: " + individualOne); 
				
				// fill comments
				domainMFrag.setDescription(this.getDescription(ontology, individualOne)); 
				
				/* -> hasResidentNode */
				OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasResidentNodeObjectPropertyName(), prefixManager); 
//				individualOne.getObjectPropertyValues(objectProperty, ontology)
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne, objectProperty, ontology) ){
					// remove prefixes from the name
					String name = this.extractName(ontology, individualTwo.asOWLNamedIndividual());
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
					mapMultiEntityNode.put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), domainResidentNode); 
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, domainResidentNode, individualTwo.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				
				/* -> hasInputNode */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasInputNodeObjectPropertyName(), prefixManager); 
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne, objectProperty, ontology)){
					// instantiate input node
					InputNode generativeInputNode = this.getMEBNFactory().createInputNode(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), domainMFrag); 
					mebn.getNamesUsed().add(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); // mark name as used
					domainMFrag.addInputNode(generativeInputNode);  	 // add to mfrag
					mapMultiEntityNode.put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), generativeInputNode); 				
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, generativeInputNode, individualTwo.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				
				/* -> hasContextNode */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasContextNodeObjectPropertyName(), prefixManager); 
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne, objectProperty, ontology)){
					ContextNode contextNode = this.getMEBNFactory().createContextNode(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), domainMFrag); 
					mebn.getNamesUsed().add(this.extractName(ontology, individualTwo.asOWLNamedIndividual()));  	// mark name as used
					domainMFrag.addContextNode(contextNode); 				// add to mfrag
					mapMultiEntityNode.put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), contextNode); 				
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, contextNode, individualTwo.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				
				/* -> hasOVariable */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasOVariableObjectPropertyName(), prefixManager); 
				String ovName = null;
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne, objectProperty, ontology)){
					ovName = this.extractName(ontology, individualTwo.asOWLNamedIndividual());	// Name of the OV individual
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
					// let's map objects w/ scope identifier included
					mapMultiEntityNode.put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), oVariable); 
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, oVariable, individualTwo.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}	
		}
		
		
		return mapMultiEntityNode;
	}
	
	/**
	 * This method checks if {@link #getLastOWLModel()} != null. If not null, then it will use reasoner to resolve object property values. If
	 * null, then it will return the asserted individuals.
	 * @param individual
	 * @param property
	 * @param ontology
	 * @return a non null collection
	 */
	public Collection<OWLIndividual> getObjectPropertyValues(OWLIndividual individual, OWLObjectPropertyExpression property, OWLOntology ontology) {
//		try {
//			OWLReasoner reasoner = this.getLastOWLReasoner();
//			if (reasoner != null) {
//				// query using expression
//				String expression = "inverse " + this.extractName(property) + " value " + this.extractName(individual);
//				Debug.println(this.getClass(), "Expression: " + expression);
//				return (Collection)reasoner.getInstances(this.parseExpression(expression), false).getFlattened();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		if (!property.isAnonymous()
				&& ontology.containsObjectPropertyInSignature(property.asOWLObjectProperty().getIRI(), true)) {
			// this ontology has this object property and it is not anonymous
			try {
				if (this.getLastOWLReasoner() != null) {
					return (Collection)this.getLastOWLReasoner().getObjectPropertyValues(individual.asOWLNamedIndividual(), property).getFlattened();
				}
				return individual.getObjectPropertyValues(property, ontology);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// unsupported
			Debug.println(this.getClass(), property + " is either anonymous or not declared as an object property in " + ontology);
		}
		return new HashSet<OWLIndividual>();
	}
	
	/**
	 * This method checks if {@link #getLastOWLModel()} != null. If not null, then it will use reasoner to resolve data property values. If
	 * null, then it will return the asserted values.
	 * @param individual
	 * @param property
	 * @param ontology
	 * @return a non null collection
	 */
	public Collection<OWLLiteral> getDataPropertyValues(OWLIndividual individual, OWLDataPropertyExpression property, OWLOntology ontology) {
		if (!property.isAnonymous()
				&& ontology.containsDataPropertyInSignature(property.asOWLDataProperty().getIRI(), true)) {
			// this ontology has this object property and it is not anonymous
			try {
				if (this.getLastOWLReasoner() != null) {
					return this.getLastOWLReasoner().getDataPropertyValues(individual.asOWLNamedIndividual(), property.asOWLDataProperty());
				}
				return individual.getDataPropertyValues(property, ontology);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			// unsupported
			Debug.println(this.getClass(), property + " is either anonymous or not declared as an data property in " + ontology);
		}
		return new HashSet<OWLLiteral>();
	}
	
	/**
	 * Loads the content of build in random variables (BuiltInRV). It also resolves references from such BuiltInRVs to input nodes.
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from a built in random variable name to its actual object.
	 * @throws IOMebnException
	 * @see {@link #loadDomainMFragContents(OWLModelManager, MultiEntityBayesianNetwork, Map)}
	 */
	protected Map<String, BuiltInRV> loadBuiltInRV(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{

		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// return value
		Map<String, BuiltInRV> ret = new HashMap<String, BuiltInRV>();
		
		// extract class
		OWLClass builtInPr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(BUILTIN_RV, prefixManager); 
		
		// iterate on individuals
		for (OWLIndividual individualOne : this.getOWLIndividuals(builtInPr, ontology)){
			String nameBuiltIn = this.extractName(ontology, individualOne.asOWLNamedIndividual());  // this is the name of the built-in RV
			BuiltInRV builtInRV = this.getMEBNFactory().createBuiltInRV(nameBuiltIn);				// this variable will hold the instantiated BuiltInRV
			if (builtInRV == null) {
				Debug.println(this.getClass(), "Unknown builtin RV found: " + individualOne);
				continue;	// let's just ignore unknown elements...
			}	
			// by now, builtInRV != null, but let's just assert it...
			if(builtInRV != null){
				mebn.addBuiltInRVList(builtInRV); 				// add to mebn
				ret.put(this.extractName(ontology, individualOne.asOWLNamedIndividual()), builtInRV); // add to return
				
				Debug.println(this.getClass(), "BuiltInRV loaded: " + individualOne); 		
				
				try {
					IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, builtInRV, individualOne.asOWLNamedIndividual().getIRI());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				/* -> hasInputInstance */
				OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasInputInstance", prefixManager); 
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne,objectProperty, ontology)){
					// extract the related input node
					INode mappedNode = this.getMapLoadedNodes().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual()));
					if (mappedNode != null && (mappedNode instanceof InputNode)) {
						InputNode generativeInputNode = (InputNode)mappedNode;
						builtInRV.addInputInstance(generativeInputNode); // add relation to builtInRV
						Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
					} else {
						// all nodes should be present in mapLoadedNodes, and only input nodes should be using hasInputInstance.
						throw new IOMebnException(this.getResource().getString("GenerativeInputNodeNotExistsInMTheory"),  
								"BuiltInRV = " + individualOne + ", Input = " + individualTwo); 
					}
				}
			}
			
		}		
		return ret;
	}
	

	/**
	 * Load content of context nodes, except the formulas.
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return : a mapping from top level context node name to top level context node. A context node is in a top level if it is 
	 * not a inner term of another node.
	 * @throws IOMebnException
	 */
	protected Map<String, ContextNode> loadContextNode( OWLOntology ontology, MultiEntityBayesianNetwork mebn) 
														throws IOMebnException {

		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, ContextNode> ret = new HashMap<String, ContextNode>();
		
		OWLClass contextNodePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.CONTEXTNODE, prefixManager); 
		if (!ontology.containsClassInSignature(contextNodePr.getIRI(), true)) {
			// use old definition
			contextNodePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.CONTEXT_NODE, prefixManager); 
		}
		
		if (contextNodePr != null) {
			for (OWLIndividual individualOne : this.getOWLIndividuals(contextNodePr, ontology)){
				
				// find context node (which is already loaded)
				ContextNode contextNode = null;
				if (this.getMapLoadedNodes() != null) {
					INode mappedNode = this.getMapLoadedNodes().get(this.extractName(ontology, individualOne.asOWLNamedIndividual())); 
					if (mappedNode == null || !(mappedNode instanceof ContextNode)){
						throw new IOMebnException(this.getResource().getString("ContextNodeNotExistsInMTheory"), "Context = " + individualOne); 
					}
					contextNode = (ContextNode)mappedNode;
				}
				
				Debug.println(this.getClass(), "Context Node loaded: " + individualOne); 				
				
				contextNode.setDescription(this.getDescription(ontology, individualOne)); 
				
				/* -> isContextNodeIn  */
				MFrag domainMFrag = null;
				OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isContextNodeIn", prefixManager); 			
				Collection<OWLIndividual> instances = this.getObjectPropertyValues(individualOne,objectProperty, ontology);	
				if (instances.size() <= 0) {
					throw new IOMebnException(this.getResource().getString("DomainMFragNotExistsInMTheory"), individualOne + " : " + objectProperty + " = null"); 
				} else {
					OWLIndividual individualMFragOfContextNode = instances.iterator().next();
					domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, individualMFragOfContextNode.asOWLNamedIndividual())); 
					if(domainMFrag == null || domainMFrag.containsContextNode(contextNode) == false){
						throw new IOMebnException(this.getResource().getString("ContextNodeNotExistsInMFrag"), individualOne + ", " + individualMFragOfContextNode); 
					}
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualMFragOfContextNode);			
				}
				/* -> isNodeFrom */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isNodeFrom", prefixManager); 			
				for(OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne,objectProperty, ontology) ){
					domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
					if(domainMFrag.containsNode(contextNode) == false){
						throw new IOMebnException(this.getResource().getString("NodeNotExistsInMFrag"), individualOne + ", " + individualTwo); 
					}
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);				
				}
				
				/* -> hasArgument */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasArgumentPropertyName(), prefixManager); 
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne,objectProperty, ontology) ){
					Argument argument = this.getMEBNFactory().createArgument(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), contextNode); 
					contextNode.addArgument(argument); 
					this.getMapArgument().put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), argument); 
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, argument, individualTwo.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				/* -> isContextInstanceOf */
				// TODO this is a very dirty code. It needs cleanup. Stop reusing variables in different points
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isContextInstanceOf", prefixManager); 			
				instances = this.getObjectPropertyValues(individualOne,objectProperty, ontology); 	
				Iterator<OWLIndividual> itAux = instances.iterator();
				if(itAux.hasNext() != false){
					OWLIndividual auxIndividual = itAux.next();
					if(this.getMapBuiltInRV().containsKey(this.extractName(ontology, auxIndividual.asOWLNamedIndividual()))){
						this.getMapIsContextInstanceOf().put(contextNode, this.getMapBuiltInRV().get(this.extractName(ontology, auxIndividual.asOWLNamedIndividual()))); 
					} else {
						INode auxNode = this.getMapLoadedNodes().get(this.extractName(ontology, auxIndividual.asOWLNamedIndividual()));
						if(auxNode != null && (auxNode instanceof ResidentNode)){
							this.getMapIsContextInstanceOf().put(contextNode, auxNode); 
						}
					}
					
				}			
				
				/* -> isInnerTermOf */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isInnerTermOf", prefixManager); 			
				instances = this.getObjectPropertyValues(individualOne,objectProperty, ontology); 	
				
				// the context is only inner term of other... 
				if(instances.size() > 0){
					domainMFrag.removeContextNode(contextNode); 
					for (OWLIndividual auxIndividual : instances){
						INode mappedNode = this.getMapLoadedNodes().get(this.extractName(ontology, auxIndividual.asOWLNamedIndividual())); 
						if (mappedNode instanceof MultiEntityNode) {
							contextNode.addInnerTermFromList((MultiEntityNode)mappedNode); 
							((MultiEntityNode)mappedNode).addInnerTermOfList(contextNode); 
							Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + auxIndividual);			
						}
					}				
				} else {
					ret.put(this.extractName(ontology, individualOne.asOWLNamedIndividual()), contextNode); 
				}
			}
		}
		
		/* the property isContextIntanceOf is filled in loadBuiltInRV method  */ 
		return ret;
	}

	
	/**
	 * It loads the contents of resident nodes.
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from a name to the loaded resident nodes.
	 * @throws IOMebnException
	 */
	protected Map<String, ResidentNode> loadDomainResidentNode(OWLOntology ontology, MultiEntityBayesianNetwork mebn)  throws IOMebnException{

		Map<String, ResidentNode> ret = new HashMap<String, ResidentNode>();
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		OWLClass domainResidentNodePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAINRESIDENT, prefixManager); 
		if (!ontology.containsClassInSignature(domainResidentNodePr.getIRI(), true)) {
			// use old definition
			domainResidentNodePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.DOMAIN_RESIDENT, prefixManager); 
		}
		
		if (domainResidentNodePr != null) {
			for (OWLIndividual individualOne :  domainResidentNodePr.getIndividuals(ontology)){
				INode mappedNode = this.getMapLoadedNodes().get(this.extractName(ontology, individualOne.asOWLNamedIndividual()));
				if (mappedNode == null || !(mappedNode instanceof ResidentNode)){
					throw new IOMebnException(this.getResource().getString("DomainResidentNotExistsInMTheory"), "Resident = " + individualOne); 
				}
				ResidentNode domainResidentNode = (ResidentNode)mappedNode;
				
				Debug.println(this.getClass(), "Domain Resident loaded: " + individualOne); 			
				
				domainResidentNode.setDescription(getDescription(ontology, individualOne)); 
				
				// create the link from domain resident node to owl property
				this.loadLinkFromResidentNodeToOWLProperty(domainResidentNode, (OWLObject)individualOne, ontology);
				
				/* -> isResidentNodeIn  */
				// TODO this code is dirty. Stop using the same variable in different contexts...
				OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isResidentNodeIn", prefixManager); 			
				Collection<OWLIndividual> instances = this.getObjectPropertyValues(individualOne,objectProperty, ontology); 	
				MFrag mFragOfNode = null;	// extract the first MFrag
				if (instances.size() > 0) {
					Iterator<OWLIndividual> itAux = instances.iterator();
					OWLIndividual individualTwo = itAux.next();
					MFrag domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
					if(domainMFrag.containsDomainResidentNode(domainResidentNode) == false){
						throw new IOMebnException(this.getResource().getString("DomainResidentNotExistsInDomainMFrag") ); 
					}
					mFragOfNode = domainMFrag; 
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);			
				}
				
				/* -> hasArgument */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasArgumentPropertyName(), prefixManager); 
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne,objectProperty, ontology)){
					Argument argument = this.getMEBNFactory().createArgument(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), domainResidentNode); 
					domainResidentNode.addArgument(argument); 
					this.getMapArgument().put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), argument); 
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, argument, individualTwo.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}		
				
				/* -> hasParent */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasParent", prefixManager); 
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne,objectProperty, ontology)){
					INode mappedParent = this.getMapLoadedNodes().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual()));
					if (mappedParent != null) {
						if (mappedParent instanceof ResidentNode){
							ResidentNode aux = (ResidentNode)mappedParent; 
							Edge auxEdge = this.getMEBNFactory().createEdge(aux, domainResidentNode);
							try{
								mFragOfNode.addEdge(auxEdge); 
								try {
									IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, auxEdge, objectProperty.getIRI());
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
									IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, auxEdge, objectProperty.getIRI());
								} catch (Exception e) {
									e.printStackTrace();
								}
							} catch(Exception e){
								e.printStackTrace();
							}
						} else {
							throw new IOMebnException(this.getResource().getString("DomainResidentNotExistsInMTheory") + " : " + individualTwo + ". "
									+ this.getResource().getString("GenerativeInputNodeNotExistsInMTheory") + " : " + individualTwo + ". "	
							); 
						}
					} else {
						throw new IOMebnException(this.getResource().getString("NodeNotFound"), "" + individualTwo); 
					}
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
				}	
				
				/* -> hasInputInstance  */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasInputInstance", prefixManager); 			
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne,objectProperty, ontology)){
					INode generativeInputNode = this.getMapLoadedNodes().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
					try{
						((InputNode)generativeInputNode).setInputInstanceOf(domainResidentNode); 
					} catch(Exception e){
						e.printStackTrace(); 
					}
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);			
				}
				
				/* -> isInnerTermOf */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isInnerTermOf", prefixManager); 			
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne,objectProperty, ontology)){
					INode multiEntityNode = this.getMapLoadedNodes().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
					domainResidentNode.addInnerTermFromList((MultiEntityNode)multiEntityNode); 
					((MultiEntityNode)multiEntityNode).addInnerTermOfList(domainResidentNode); 
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);			
				}				
				
				/* -> hasPossibleValues */
				{
					CategoricalStateEntity state = null; 
					objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasPossibleValues", prefixManager); 			
					for (OWLIndividual individualTwo: this.getObjectPropertyValues(individualOne,objectProperty, ontology)){
						String stateName = this.extractName(ontology, individualTwo.asOWLNamedIndividual()); 
						/* case 1: booleans states */
						if(stateName.equals("true")){
							StateLink link = domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());   
							Set<String> globallyObjects = this.getMapCategoricalStateGloballyExclusiveNodes().get("true"); 
							if(globallyObjects.contains(domainResidentNode.getName())){
								link.setGloballyExclusive(true); 
							} else {
								link.setGloballyExclusive(false); 
							}
							domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
						}
						else{
							if(stateName.equals("false")){
								StateLink link = domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());   
								Set<String> globallyObjects = this.getMapCategoricalStateGloballyExclusiveNodes().get("false"); 
								if(globallyObjects.contains(domainResidentNode.getName())){
									link.setGloballyExclusive(true); 
								}else{
									link.setGloballyExclusive(false); 
								}
								domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
							}
							else{
								if(stateName.equals("absurd")){
									StateLink link = domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());   
									Set<String> globallyObjects = this.getMapCategoricalStateGloballyExclusiveNodes().get("absurd"); 
									if(globallyObjects.contains(domainResidentNode.getName())){
										link.setGloballyExclusive(true); 
									}else{
										link.setGloballyExclusive(false); 
									}
									domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES);
								}
								else{
									if(this.getMapLabelToObjectEntity().get(stateName) != null){
										/* case 2:object entities */
										StateLink link = domainResidentNode.addPossibleValueLink(this.getMapLabelToObjectEntity().get(stateName)); 
										domainResidentNode.setTypeOfStates(ResidentNode.OBJECT_ENTITY);
										try {
											// this debug is in try-catch because the toString in link may throw exceptions
											Debug.println(this.getClass(), "Added state link " + link);
										} catch (Exception e) {
											Debug.println(this.getClass(), "Added state link", e);
										}
									} else{
										/* case 3: categorical states */
										try {
											state = mebn.getCategoricalStatesEntityContainer().getCategoricalState(this.extractName(ontology, individualTwo.asOWLNamedIndividual())) ;
											StateLink link = domainResidentNode.addPossibleValueLink(state); 
											Set<String> globallyObjects = this.getMapCategoricalStateGloballyExclusiveNodes().get(state.getName()); 
											if(globallyObjects.contains(domainResidentNode.getName())){
												link.setGloballyExclusive(true); 
											}else{
												link.setGloballyExclusive(false); 
											}
											domainResidentNode.setTypeOfStates(ResidentNode.CATEGORY_RV_STATES);
										} catch (CategoricalStateDoesNotExistException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} 
									}
									
								}
							}
						}
					} /* for */
					
				}
				
				/* hasProbDist */
				
				OWLObjectProperty hasProbDist = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasProbDist", prefixManager);
				OWLDataProperty hasDeclaration = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("hasDeclaration", prefixManager); 
				for (OWLIndividual element : this.getObjectPropertyValues(individualOne,hasProbDist, ontology)) {
					String cpt = "";
					try {
						Collection<OWLLiteral> owlLiterals = this.getDataPropertyValues(element, hasDeclaration, ontology);
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
				
				/* isArgTermIn is not checked */
				
				// fill return (extract name again, because the extracted name may be different from the node's name)
				ret.put(this.extractName(ontology, individualOne.asOWLNamedIndividual()), domainResidentNode);
				
			}
		}
		return ret;
	}
	
	/**
	 * Fills the domainResidentNode with a link to the OWL property it is representing
	 * @param domainResidentNode : node to be filled
	 * @param individualOne
	 * @param ontology
	 */
	protected void loadLinkFromResidentNodeToOWLProperty(ResidentNode domainResidentNode, OWLObject owlObject,OWLOntology ontology) {
		if (ontology == null || owlObject == null) {
			System.err.println("Warning: attempted to load \"definesUncertaintyOf\" from object " + owlObject + " in ontology " + ontology);
			return;
		}
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
		
		// extract current ontology prefix
		PrefixManager prefixManager = this.getOntologyPrefixManager(ontology);	// this prefix may be different from PR-OWL
		
		// extract property
		OWLDataProperty definesUncertaintyOfIRI = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("definesUncertaintyOf", prefixManager);
		
		// extract value
		OWLLiteral value = null;
		// get inferred or asserted values
		if (owlObject instanceof OWLIndividual) {
			Collection<OWLLiteral> values = this.getDataPropertyValues((OWLIndividual)owlObject, definesUncertaintyOfIRI, ontology);
			if (values != null && !values.isEmpty()) {
				// use only the first value
				value = values.iterator().next();
			}
		} else {
			// TODO read object as a class
		}
		
		// add value to mebn
		if (value != null) {
			// extract IRI
			String iriString = value.getLiteral();
			if (iriString != null && (iriString.length() > 0)) {
				IRIAwareMultiEntityBayesianNetwork.addDefineUncertaintyToMEBN(mebn, domainResidentNode, IRI.create(iriString));
			}
		}
	}

	/**
	 * It loads the contents of input nodes.
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from a name to the loaded input nodes.
	 * @throws IOMebnException
	 */
	protected Map<String, InputNode> loadGenerativeInputNode(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{
	    
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, InputNode> ret = new HashMap<String, InputNode>();
		
		OWLClass inputNodePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.GENERATIVEINPUT, prefixManager); 
		if (!ontology.containsClassInSignature(inputNodePr.getIRI(), true)) {
			inputNodePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.GENERATIVE_INPUT, prefixManager); 
		}
		
		if (inputNodePr != null) {
			for (OWLIndividual individualOne : inputNodePr.getIndividuals(ontology)){
				if (!individualOne.isNamed()) {
					Debug.println(this.getClass(), individualOne + " ignored because it is anonymous.");
					continue;	// ignore anonymous
				}
				Debug.println(this.getClass(), "  - Input Node loaded: " + individualOne); 			
				
				String individualOneName = this.extractName(ontology, individualOne.asOWLNamedIndividual());
				
				INode loadedNode = this.getMapLoadedNodes().get(individualOneName);
				if (loadedNode == null){
					throw new IOMebnException(this.getResource().getString("GenerativeInputNodeNotExistsInMTheory"), "Input = " + individualOne ); 				
				}
				InputNode generativeInputNode = (InputNode)loadedNode; 
				
				generativeInputNode.setDescription(this.getDescription(ontology, individualOne)); 
				
				//loadHasPositionProperty(individualOne, generativeInputNode); 
				
				/* -> isInputInstanceOf  */
				
				OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isInputInstanceOf", prefixManager); 			
				Iterator<OWLIndividual> itAux = this.getObjectPropertyValues(individualOne,objectProperty, ontology).iterator();
				
				if(itAux.hasNext() != false){
					OWLIndividual individualTwo = itAux.next();
					String individualTwoName = this.extractName(ontology, individualTwo.asOWLNamedIndividual());
					
					if (this.getMapFilledResidentNodes().containsKey(individualTwoName)){
						ResidentNode domainResidentNode = this.getMapFilledResidentNodes().get(individualTwoName); 
						try{
							generativeInputNode.setInputInstanceOf(domainResidentNode); 
						} catch(Exception e){
							e.printStackTrace(); 
						}
						Debug.println(this.getClass(), "   - isInputInstanceOf " + domainResidentNode.getName()); 
					} else if (this.getMapBuiltInRV().containsKey(individualTwoName)){
						BuiltInRV builtInRV = this.getMapBuiltInRV().get(individualTwoName); 
						generativeInputNode.setInputInstanceOf(builtInRV); 
						Debug.println(this.getClass(), "   - isInputInstanceOf " + builtInRV.getName()); 
					}
				}
				
				/* -> hasArgument */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasArgumentPropertyName(),prefixManager); 
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne,objectProperty, ontology) ){
					if (!individualTwo.isNamed()) {
						continue;	// ignore anonymous individuals
					}
					String individualTwoName = this.extractName(ontology, individualTwo.asOWLNamedIndividual());
					Argument argument = this.getMEBNFactory().createArgument(individualTwoName, generativeInputNode); 
					generativeInputNode.addArgument(argument); 
					this.getMapArgument().put(individualTwoName, argument); 
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
					
					try {
						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, argument, individualTwo.asOWLNamedIndividual().getIRI());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}		
				
				/* -> isInnerTermOf */
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isInnerTermOf", prefixManager); 			
				for (OWLIndividual individualTwo : this.getObjectPropertyValues(individualOne,objectProperty, ontology) ){
					String individualTwoName = this.extractName(ontology, individualTwo.asOWLNamedIndividual());
					
					INode multiEntityNode = this.getMapLoadedNodes().get(individualTwoName);
					if (multiEntityNode instanceof MultiEntityNode) {
						generativeInputNode.addInnerTermFromList((MultiEntityNode)multiEntityNode); 
						((MultiEntityNode)multiEntityNode).addInnerTermOfList(generativeInputNode); 
						Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);			
					} else {
						Debug.println(this.getClass(), individualTwo + " is not an multi entity node expected by " + objectProperty);
					}
				}	
				
				/* hasProbDist is not checked */
				
				ret.put(individualOneName, generativeInputNode);	// fill return
			}		
		}
		return ret;
	}
	

	/**
	 * It loads the contents of ordinary variables.
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from a name to the loaded ordinary variables.
	 * @throws IOMebnException
	 */
	protected Map<String, OrdinaryVariable> loadOrdinaryVariable(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, OrdinaryVariable> ret = new HashMap<String, OrdinaryVariable>();
		
		OWLClass ordinaryVariablePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.ORDINARYVARIABLE, prefixManager); 
		if (!ontology.containsClassInSignature(ordinaryVariablePr.getIRI(), true)) {
			// use the old definition
			ordinaryVariablePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.ORDINARY_VARIABLE, prefixManager); 
		}
		
		if (ordinaryVariablePr != null) {
			for (OWLIndividual individualOne : ordinaryVariablePr.getIndividuals(ontology) ){
				if (!individualOne.isNamed()) {
					Debug.println(this.getClass(), individualOne + " ignored because it is anonymous.");
					continue;	// ignore anonymous
				}
				String individualOneName = this.extractName(ontology, individualOne.asOWLNamedIndividual());
				INode loadedNode = this.getMapLoadedNodes().get(individualOneName);
				if (loadedNode == null || !(loadedNode instanceof OrdinaryVariable)){
					throw new IOMebnException(this.getResource().getString("OVariableNotExistsInMTheory"),  "Ovariable = " + individualOne); 
				}
				OrdinaryVariable oVariable = (OrdinaryVariable)loadedNode;
				
				Debug.println(this.getClass(), "Ordinary Variable loaded: " + individualOne); 				
				
				oVariable.setDescription(this.getDescription(ontology, individualOne)); 
				
				/* -> isOVariableIn  */
				OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isOVariableIn", prefixManager); 			
				Collection<OWLIndividual> instances = this.getObjectPropertyValues(individualOne,objectProperty, ontology);
				if (!instances.isEmpty()) {
					OWLIndividual individualTwo = instances.iterator().next();
					MFrag domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
					if(domainMFrag != oVariable.getMFrag()){
						throw new IOMebnException(this.getResource().getString("isOVariableInError"),  "Ordinary variable = " + individualOne); 
					}
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);			
				}
				
				/* -> isSubsBy */
				
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getIsSubsByObjectPropertyName(), prefixManager); 			
				instances = this.getObjectPropertyValues(individualOne,objectProperty, ontology); 	
				if(!instances.isEmpty()){
					OWLIndividual individualTwo = instances.iterator().next();
					if (individualTwo.isNamed()) {
						Type type = mebn.getTypeContainer().getType(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
						if (type != null){
							oVariable.setValueType(type); 
							oVariable.updateLabel(); 
						} else{
							// the pr-owl file is erroneous
							throw new IOMebnException(this.getResource().getString("isOVariableInError"),  individualOne + " - " + objectProperty + " - " + individualTwo); 
						}
					} else {
						Debug.println(this.getClass(), "Ignoring anonymous individual " + individualTwo);
					}
				}
				
				/* isRepBySkolen don't checked */ 
				ret.put(individualOneName, oVariable);
			}		
		}
		return ret;
	}
	
	/**
	 * It loads the contents of arguments.
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from a name to the loaded arguments.
	 * @throws IOMebnException
	 */
	protected Map<String, Argument> loadArgRelationship(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{

		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, Argument> ret = new HashMap<String, Argument>();
		
		OWLClass argRelationshipPr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(ARGUMENT_RELATIONSHIP, prefixManager); 
		
		for (OWLIndividual individualOne : argRelationshipPr.getIndividuals(ontology) ){	
			if (!individualOne.isNamed()) {
				Debug.println(this.getClass(), "Ignoring anonymous individual " + individualOne);
				continue;	// ignore anonymous
			}
			
			String individualOneName = this.extractName(ontology, individualOne.asOWLNamedIndividual());
			Argument argument = this.getMapArgument().get(individualOneName); 
			if (argument == null){
				throw new IOMebnException(this.getResource().getString("ArgumentNotFound"),  "Argument = " + individualOne); 
			}
			
			Debug.println(this.getClass(), "-> ArgRelationship loaded: " + individualOne); 
			
			/* -> hasArgTerm  */
			OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasArgTerm", prefixManager); 			
			
			Collection<OWLIndividual> individuals = this.getObjectPropertyValues(individualOne,objectProperty, ontology);
			if(individuals.isEmpty()){
				Debug.println(this.getClass(), individualOne + " - " + objectProperty + " = null" );
				// this is an erroneous state, but let's keep going on.
			} else {
				OWLIndividual individualTwo = individuals.iterator().next();
				if (!individualTwo.isNamed()) {
					Debug.println(this.getClass(), "Ignoring anonymous individual " + individualTwo);
				} else {
					String individualTwoName = this.extractName(ontology, individualTwo.asOWLNamedIndividual());
					
					/* check: 
					 * - node
					 * - entity : it is not checked in this version
					 * - oVariable
					 * - skolen : it is not checked in this version
					 */
					INode multiEntityNode = null;
					if (( multiEntityNode = this.getMapLoadedNodes().get(individualTwoName)) != null){
						try{
							if (multiEntityNode instanceof ResidentNode){
								argument.setArgumentTerm((ResidentNode)multiEntityNode);
								argument.setType(Argument.RESIDENT_NODE);  
							}else{
								if(multiEntityNode instanceof ContextNode){
									argument.setArgumentTerm((ContextNode)multiEntityNode); 
									argument.setType(Argument.CONTEXT_NODE); 
								}
							}
						}
						catch(Exception e){
							throw new IOMebnException(this.getResource().getString("ArgumentTermError"),  "Argument = " + individualTwo); 				   
						}
					}
					else{
						OrdinaryVariable oVariable = null;
						if( (oVariable = this.getMapFilledOrdinaryVariables().get(individualTwoName)) != null) {
							try{
								argument.setOVariable(oVariable);
								argument.setType(Argument.ORDINARY_VARIABLE); 					
							}
							catch(Exception e){
								throw new IOMebnException(this.getResource().getString("ArgumentTermError"),  individualTwoName); 				   
							}
						}
						else{
							CategoricalStateEntity state; 
							if((state = this.getMapCategoricalStates().get(individualTwoName)) != null){
								argument.setEntityTerm(state); 	
								argument.setType(Argument.ORDINARY_VARIABLE); 
							}
							else{
								if(individualTwoName.equalsIgnoreCase("true")){
									argument.setEntityTerm(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());
									argument.setType(Argument.BOOLEAN_STATE); 
								}else{
									if(individualTwoName.equalsIgnoreCase("false")){
										argument.setEntityTerm(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());
										argument.setType(Argument.BOOLEAN_STATE); 
									}else{
										if(individualTwoName.equalsIgnoreCase("absurd")){
											argument.setEntityTerm(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());
											argument.setType(Argument.BOOLEAN_STATE); 
										}
									}
								}
								
							}
						}
					}
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);			
					
				}
			}
			
			/* has Arg Number */
			OWLDataProperty hasArgNumber = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("hasArgNumber", prefixManager);
			Collection<OWLLiteral> literals = this.getDataPropertyValues(individualOne, hasArgNumber, ontology);
			try {
				argument.setArgNumber(Integer.parseInt(literals.iterator().next().getLiteral()));
			} catch (Exception e) {
				// the argnumber was empty or invalid... This is an error, but lets try going on...
				e.printStackTrace();
			}
			
			/* -> isArgumentOf  */
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isArgumentOf", prefixManager); 			
			Collection<OWLIndividual> instances = this.getObjectPropertyValues(individualOne,objectProperty, ontology); 	
			if (instances.isEmpty()) {
				Debug.println(this.getClass(), individualOne + " - " + objectProperty + " = null");
			} else {
				OWLIndividual individualTwo = instances.iterator().next();
				if (individualTwo.isNamed()) {
					INode multiEntityNode = this.getMapLoadedNodes().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
					if (argument.getMultiEntityNode() != multiEntityNode){
						throw new IOMebnException(this.getResource().getString("isArgumentOfError"),  "isArgumentOf = " + individualTwo); 				   
					}
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);					
				} else {
					Debug.println(this.getClass(), "Ignoring anonymous individual " + individualTwo);
				}
			}
			ret.put(individualOneName, argument);
		}
		return ret;
	}
	
	/**
	 * It loads the contents of simple arguments (usually, simple arguments are composed only of ordinary variables).
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from a name to the loaded simple arguments.
	 * @throws IOMebnException
	 */
	protected Map<String, Argument> loadSimpleArgRelationship(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, Argument> ret = new HashMap<String, Argument>();
		
		OWLClass argRelationshipPr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(SIMPLE_ARGUMENT_RELATIONSHIP, prefixManager); 
		for (OWLIndividual individualOne : argRelationshipPr.getIndividuals(ontology) ){
			if (!individualOne.isNamed()) {
				Debug.println(this.getClass(), "Ignoring anonymous individual " + individualOne);
				continue;
			}
			
			String individualOneName = this.extractName(ontology, individualOne.asOWLNamedIndividual());
			
			Argument argument = this.getMapArgument().get(individualOneName); 
			if (argument == null){
				throw new IOMebnException(this.getResource().getString("ArgumentNotFound"),  "Argument = " + individualOne); 
			}
			Debug.println(this.getClass(), "SimpleArgRelationship loaded: " + individualOne); 
			
			/* -> hasArgTerm  */
			
			OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasArgTerm", prefixManager); 			
			Collection<OWLIndividual> instances = this.getObjectPropertyValues(individualOne,objectProperty, ontology); 	
			if(!instances.isEmpty()){
				OWLIndividual individualTwo = instances.iterator().next();
				if (!individualTwo.isNamed()) {
					Debug.println(this.getClass(), "Ignoring anonymous individual " + individualTwo) ;
				} else {
					OrdinaryVariable oVariable = this.getMapFilledOrdinaryVariables().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
					if (oVariable == null){
						throw new IOMebnException(this.getResource().getString("ArgumentTermError"), "Ordinary Variable = " + individualTwo); 	
					}
					
					try{
						argument.setOVariable(oVariable); 
					}
					catch(Exception e){
						throw new IOMebnException(this.getResource().getString("ArgumentTermError"),  "Ordinary Variable = " + individualTwo); 	
					}
					Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);			
				}
			}
			
			/* -> hasArgNumber */
			
			OWLDataProperty hasArgNumber = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("hasArgNumber", prefixManager);
	        Collection<OWLLiteral> owlLiterals = this.getDataPropertyValues(individualOne,hasArgNumber, ontology);
	        try {
	        	argument.setArgNumber(Integer.parseInt(owlLiterals.iterator().next().getLiteral()));
	        } catch (Exception e) {
	        	// hasArgNumber was null or a non-integer. This is an error, but lets try going on....
	        	Debug.println(this.getClass(), individualOne + " - " + hasArgNumber + " = ERROR");
	        	e.printStackTrace();
			}
			
			
			/* -> isArgumentOf (ignored)  */
			
	        ret.put(individualOneName, argument);
		}		
		return ret;
	}
	
	/**
	 * Reads the OWL's ObjectEntity's individuals and fills the MEBN's OrdinaryVariableInstance
	 * @param ontology : the OWL ontology (from OWL API) to be used to build the MEBN.
	 * @param mebn : the loaded (but incomplete) MEBN object. This is an input and output argument (i.e. its values will be updated)
	 * @return a mapping from a name to the loaded individuals.
	 * @throws IOMebnException
	 * TODO this methos is not considering hasUID data property yet.
	 */
	protected Map<String, ObjectEntityInstance> loadObjectEntityIndividuals(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws TypeException {
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, ObjectEntityInstance> ret = new HashMap<String, ObjectEntityInstance>();
		
		OWLClass objectEntityClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(OBJECT_ENTITY, prefixManager);
		
		// a collection containing all subclasses of object entities
		List<OWLClassExpression> objectEntities = new ArrayList<OWLClassExpression>();
		objectEntities.addAll(objectEntityClass.getSubClasses(ontology));
		
		// Non-PR-OWL classes are now considered as Entities.
		// TODO remove object entities from PR-OWL definition
		objectEntities.addAll(this.getNonPROWLClasses(ontology));
		
		ObjectEntity mebnEntity = null;
		Set<ObjectEntity> handledEntities = new HashSet<ObjectEntity>();	// entities that were handled
		while (!objectEntities.isEmpty()) {
			OWLClassExpression subclassExpression = objectEntities.remove(0);	// retrieve 1st element and delete it from list
			if (!(subclassExpression instanceof OWLEntity)) {
				Debug.println(this.getClass(), "Could not convert class expression to entity: " + subclassExpression);
				continue;	// let's ignore it and keep going on.
			}
			
			mebnEntity = mebn.getObjectEntityContainer().getObjectEntityByName(this.extractName(ontology, (OWLEntity)subclassExpression));
			
			// handle entities closer to root later
			boolean isAllChildrenHandled = true;	// if all children were already handled, then handle this entity. Otherwise, leave it for later
			for (ObjectEntity childEntity : mebn.getObjectEntityContainer().getChildrenOfObjectEntity(mebnEntity)) {
				if (!handledEntities.contains(childEntity)) {
					isAllChildrenHandled = false;
					break;
				}
			}
			if (!isAllChildrenHandled) {
				objectEntities.add(subclassExpression);	// readd at end of list, so that it is handled later
				continue;
			}
			
			handledEntities.add(mebnEntity);
			
			// The individuals to load are initialized as individuals of non-PR-OWL2 elements.
			Set<OWLIndividual> individuals = this.getOWLIndividuals(subclassExpression, ontology);	
			
			// TODO add non-PR-OWL2 individuals of owl:Thing that were not added yet. Use asserted individuals
			// TODO load individuals of owl:Thing that was not classified as as any other class
			
			// iterate over individuals
			for (OWLIndividual individual : individuals) { 
				if (individual.isNamed()) {
					String individualName = this.extractName(ontology, individual.asOWLNamedIndividual());
					
					// creates a object entity instance and adds it into the mebn entity container
					try {
						// do not add individual if it was already added.
						// TODO use UID instead of individual IRI
						if (mebn instanceof IRIAwareMultiEntityBayesianNetwork) {
							if (((IRIAwareMultiEntityBayesianNetwork) mebn).getIriMap().containsValue(individual.asOWLNamedIndividual().getIRI())) {

								// the individual was previously added to MEBN
								try {
									Debug.println(getClass(), individual + " is already in " + mebn);
								} catch (Throwable t) {
									t.printStackTrace();
								}
								
								// check if individual with same name exists
								ObjectEntityInstance objectEntityInstance = mebn.getObjectEntityContainer().getEntityInstanceByName(individualName);
								if (objectEntityInstance != null) {
									try {
										Debug.println(getClass(), "Instance was already inserted for another entity: " + objectEntityInstance + ". Avoiding duplicates.");
									} catch (Throwable t) {
										t.printStackTrace();
									}
									mebnEntity.getInstanceList().add(objectEntityInstance);
//									mebn.getObjectEntityContainer().addEntityInstance(objectEntityInstance);
									continue;
								}
								
								// make sure this is not punning (an individual with same IRI as its class)
								
								// check if object entity with same name exists
								ObjectEntity entitySameName = mebn.getObjectEntityContainer().getObjectEntityByName(individualName);
								if (entitySameName == null) {
									try {
										Debug.println(getClass(), "No entity with same name found. It's not punning. Do not add new individual.");
									} catch (Throwable t) {
										t.printStackTrace();
									}
									continue;
								}
								
								IRI entityIRI = ((IRIAwareMultiEntityBayesianNetwork) mebn).getIriMap().get(entitySameName);
								if (entityIRI != null && !entityIRI.equals(individual.asOWLNamedIndividual().getIRI())) {
									try {
										Debug.println(getClass(), "No entity with same IRI found. It's not punning. Do not add new individual.");
									} catch (Throwable t) {
										t.printStackTrace();
									}
									continue;
								}
								
								// it's punning, and it's the 1st time the individual is included in MEBN. Add to mebn now
							}
						}

						ObjectEntityInstance addedInstance = mebnEntity.addInstance(individualName);
						mebn.getObjectEntityContainer().addEntityInstance(addedInstance);
						ret.put(individualName, addedInstance);
						
						try {
							IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, addedInstance, individual.asOWLNamedIndividual().getIRI());
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (EntityInstanceAlreadyExistsException eiaee) {
						// Duplicated instance/individuals are not a major problem for now
						Debug.println("Duplicated instance/individual declaration found at OWL Loader");
					} 
				} else {
					Debug.println(this.getClass(), "Ignoring anonymous individual " + individual);
				}
			}
		}
		
		return ret;
	}
	
	
	/**
	 * This damn complex mechanism must be executed because the arguments may not
	 * be inserted into resident nodes in the correct order. Since these changes
	 * in order may cause input nodes to fail, we must adjust or reorder them.
	 * Additionally, it looks like resident nodes are using {@link ResidentNode#getOrdinaryVariableList()} to manage
	 * arguments instead of using only {@link ResidentNode#getArgumentList()}. This method synchronizes
	 * the content of {@link ResidentNode#getOrdinaryVariableList()} and {@link ResidentNode#getArgumentList()} too.
	 * @deprecated it sounds like a tremendously dirty workaround. Argument's order can be adjusted more consistently
	 * allocating all arguments first (this is possible by calculating the total quantity of arguments)
	 * and then filling their contents in order (depending to the value of hasArgumentNumber).
	 * This method is used in this class only because super classes were doing this in the same way (and we are
	 * trying to use template methods), but it should be fixed in future releases, because this is extremely
	 * thread-unsafe and hard to extend.
	 */
	protected void ajustArgumentOfNodes(MultiEntityBayesianNetwork mebn){
		// TODO This is extremely inefficient. Optimize it.
		
		for(ResidentNode resident: this.getMapFilledResidentNodes().values()){
			int argNumberActual = 1; 
			int tamArgumentList = resident.getArgumentList().size(); 
			
			while(argNumberActual <= tamArgumentList){
				boolean find = false; 
				Argument argumentOfPosition = null; 
				for(Argument argument: resident.getArgumentList()){
					if(argument.getArgNumber() == argNumberActual){
						find = true; 
						argumentOfPosition = argument; 
						break; 
					}
				}
				if(!find){
					throw new IllegalStateException(resident + " has no argument in position " + argNumberActual);
				}
				else{
					try{
					   resident.addArgument(argumentOfPosition.getOVariable(), false);
					}
					catch(Exception e){
						throw new IllegalStateException(""+resident,e);
					}
				}
				argNumberActual++; 
			}
		}
		
		for(InputNode input: this.getMapFilledInputNodes().values()){
			
			if(input.getInputInstanceOf() instanceof ResidentNode){
				input.updateResidentNodePointer(); 
				for(Argument argument: input.getArgumentList()){
					try{
					   input.getResidentNodePointer().addOrdinaryVariable(
							   argument.getOVariable(), argument.getArgNumber() - 1);
					   input.updateLabel(); 
					}
					catch(OVDontIsOfTypeExpected e){
						new InternalErrorDialog(); 
						e.printStackTrace(); 
					}
					catch(Exception e){
						e.printStackTrace();
						Debug.println(this.getClass(), "Error: the argument " + argument.getName() 
								+ " in node " + input.getName() + " is not set..."); 
						// TODO... problems when the arguments of the resident node aren't set... 
					}
					
				}
			}
			
		}
	}
	

	/**
	 * Builds formula trees for all context nodes in mapTopLevelContextNodes.
	 * @param mapTopLevelContextNodes : a map containing all top level context nodes (context nodes that are not inner nodes)
	 * @param mebn : the multi-entity bayesian network to be updated
	 */
	protected void buildFormulaTrees( Map<String, ContextNode> mapTopLevelContextNodes, MultiEntityBayesianNetwork mebn) {
		for(ContextNode context: mapTopLevelContextNodes.values()){
			context.setFormulaTree(buildFormulaTree(context)); 
		}
	}
	
	/**
	 * Builds up a context node's formula tree.
	 * @param contextNode : a context node to be updated
	 * @return a formula tree
	 * 
	 * @deprecated TODO this method should be moved to {@link ContextNode} in the future.
	 */
	protected NodeFormulaTree buildFormulaTree(ContextNode contextNode){
		
		
		NodeFormulaTree nodeFormulaRoot; 
		NodeFormulaTree nodeFormulaChild; 
		
		nodeFormulaRoot = this.getMEBNFactory().createNodeFormulaTree("formula", EnumType.FORMULA, 	EnumSubType.NOTHING, null);  
    	
		Debug.println("Entrou no build " +  contextNode.getName()); 
		
		// the root is a builtIn 
		
		Object obj = this.getMapIsContextInstanceOf().get(contextNode); 
		
		if((obj instanceof BuiltInRV)){
			BuiltInRV builtIn = (BuiltInRV) obj; 
			
			EnumType type = EnumType.EMPTY;
			EnumSubType subType = EnumSubType.NOTHING; 
			
			if(builtIn instanceof BuiltInRVForAll){
				type = EnumType.QUANTIFIER_OPERATOR;
				subType = EnumSubType.FORALL; 
			}
			else			
			if(builtIn instanceof BuiltInRVExists){
				type = EnumType.QUANTIFIER_OPERATOR;
				subType = EnumSubType.EXISTS; 
			}
			else			
				if(builtIn instanceof BuiltInRVAnd){
					type = EnumType.SIMPLE_OPERATOR;
					subType = EnumSubType.AND; 
				}
				else			
					if(builtIn instanceof BuiltInRVOr){
						type = EnumType.SIMPLE_OPERATOR;
						subType = EnumSubType.OR; 
					}
					else			
						if(builtIn instanceof BuiltInRVNot){
							type = EnumType.SIMPLE_OPERATOR;
							subType = EnumSubType.NOT; 
						}
						else			
							if(builtIn instanceof BuiltInRVEqualTo){
								type = EnumType.SIMPLE_OPERATOR;
								subType = EnumSubType.EQUALTO; 
							}
							else			
								if(builtIn instanceof BuiltInRVIff){
									type = EnumType.SIMPLE_OPERATOR;
									subType = EnumSubType.IFF; 
								}
								else			
									if(builtIn instanceof BuiltInRVImplies){
										type = EnumType.SIMPLE_OPERATOR;
										subType = EnumSubType.IMPLIES; 
									}; 
			
			
			nodeFormulaRoot = this.getMEBNFactory().createNodeFormulaTree(builtIn.getName(), type, subType, builtIn); 
		    nodeFormulaRoot.setMnemonic(builtIn.getMnemonic()); 
			
			List<Argument> argumentList = putArgumentListInOrder(contextNode.getArgumentList()); 
		  		    
		    for(Argument argument: argumentList){
		    	if(argument.getOVariable()!= null){
		    		OrdinaryVariable ov = argument.getOVariable(); 
		    		nodeFormulaChild = this.getMEBNFactory().createNodeFormulaTree(ov.getName(), EnumType.OPERAND, EnumSubType.OVARIABLE, ov); 
		    		nodeFormulaRoot.addChild(nodeFormulaChild); 
		    	}
		    	else{
		    		if(argument.getArgumentTerm() != null){
		    			
		    			MultiEntityNode multiEntityNode = argument.getArgumentTerm(); 
		    			
		    			if(multiEntityNode instanceof ResidentNode){
		    				ResidentNodePointer residentNodePointer = this.getMEBNFactory().createResidentNodePointer((ResidentNode)multiEntityNode, contextNode); 
		    				nodeFormulaChild = this.getMEBNFactory().createNodeFormulaTree(multiEntityNode.getName(), EnumType.OPERAND, EnumSubType.NODE, residentNodePointer); 
		    				nodeFormulaRoot.addChild(nodeFormulaChild); 
		    				
		    				//Adjust the arguments of the resident node 
		    				
		    				
		    			}
		    			else{
		    				if(multiEntityNode instanceof ContextNode){
		    					NodeFormulaTree child = buildFormulaTree((ContextNode)multiEntityNode);
		    					nodeFormulaRoot.addChild(child); 
		    				}
		    			}
		    		}
		    		else{
						if(argument.getEntityTerm() != null){
							nodeFormulaChild = this.getMEBNFactory().createNodeFormulaTree(argument.getEntityTerm().getName(), EnumType.OPERAND, EnumSubType.ENTITY, argument.getEntityTerm());
							nodeFormulaRoot.addChild(nodeFormulaChild); 
						}
		    		}
		    	}
		    	
		    }
		    
		}
		else{
			if((obj instanceof ResidentNode)){
				ResidentNodePointer residentNodePointer = this.getMEBNFactory().createResidentNodePointer((ResidentNode)obj, contextNode); 
				nodeFormulaRoot = this.getMEBNFactory().createNodeFormulaTree(((ResidentNode)obj).getName(), EnumType.OPERAND, EnumSubType.NODE, residentNodePointer); 
				
				List<Argument> argumentList = putArgumentListInOrder(contextNode.getArgumentList()); 
			  	for(Argument argument: argumentList){
					
					if(argument.getOVariable()!= null){
						OrdinaryVariable ov = argument.getOVariable(); 
						try{
						    residentNodePointer.addOrdinaryVariable(ov, argument.getArgNumber() - 1); 
						}
						catch(Exception e){
							e.printStackTrace(); 
						}
					}
					else{
						
					}		
				}
				
			}
		}
		
		return nodeFormulaRoot; 
	}
	
	/**
	 * Put the list of argument in order (for the argNumber atribute of the <Argument>. 
	 * 
	 * pos-conditions: the <argumentListOriginal> will be empty
	 * 
	 * @param argumentListOriginal the original list
	 * @return a new list with the arguments in order
	 * @deprecated TODO this method should be moved to {@link ContextNode} in the future.
	 */
	private List<Argument> putArgumentListInOrder(List<Argument> argumentListOriginal){
//		System.gc();
	    ArrayList<Argument> argumentList = new ArrayList<Argument>(); 
	    int i = 1; /* number of the actual argument */
	    while(argumentListOriginal.size() > 0){
	    	Argument argumentActual = null; 
	    	for(Argument argument: argumentListOriginal){
	    		if(argument.getArgNumber() == i){
	    			argumentActual = argument;
	    			break; 
	    		}
	    	}
	    	argumentList.add(argumentActual);
	    	argumentListOriginal.remove(argumentActual);
	    	i++; 
	    }
	    
	    return argumentList; 
		
	}

	/**
	 * Extracts the comments from an owl entity
	 * @param entity 
	 * @param ontology : the ontology being manipulated
	 * @return a comment as a string or a null value if it is not found
	 */
	public String getDescription(OWLOntology ontology, OWLEntity entity) {
		// initial assertion
		if (entity == null || ontology == null) {
			return null;
		}

		// obtains the descriptor of the comment annotation
		OWLAnnotationProperty commentProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
		
		// extracts comments
		Set<OWLAnnotation> comments = entity.getAnnotations(ontology, commentProperty);
	
		// the comment as a string value
		String comment = "";
		
		// concatenate all comments
		for (OWLAnnotation owlAnnotation : comments) {
			try {
				comment += ((OWLLiteral)owlAnnotation.getValue()).getLiteral();
			} catch (Exception e) {
				// errors on comments are not fatal
				e.printStackTrace();
			}
		}
		Debug.println(this.getClass(), "Comment loaded: " + comment + " for individual " + entity);
		return comment;
	}
	
	/**
	 * Extracts the comments from a owl individual
	 * @param individual
	 * @param ontology : the ontology being manipulated
	 * @return a comment as a string or a null value if it is not found
	 * @see #getDescription(OWLOntology, OWLEntity)
	 */
	public String getDescription(OWLOntology ontology, OWLIndividual individual) {
		try {
			return this.getDescription(ontology, (OWLEntity)(individual.asOWLNamedIndividual()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * @return {@link #getOntologyPrefixManager(null)}
	 * @see #getOntologyPrefixManager(OWLOntology)
	 */
	protected PrefixManager getDefaultPrefixManager() {
		return this.getOntologyPrefixManager(null);
	}
	
	/**
	 * This method uses cache (i.e. {@link #getOntologyPrefixCache()} ) to get the prefix manager.
	 * If {@link #getOntologyPrefixCache()} is set to null, it will not use a cache.
	 * If prefix manager is not cached, then it will delegate to {@link #getOntologyPrefixManager(OWLOntology)}
	 * @return the cached prefix manager or the one obtained from {@link #getProwlModelUserDelegator()}
	 * @see unbbayes.io.mebn.IPROWL2ModelUser#getOntologyPrefixManager(org.semanticweb.owlapi.model.OWLOntology)
	 */
	public PrefixManager getOntologyPrefixManager(OWLOntology ontology) {
		// value to return
		PrefixManager ret = null;
		// use cache if available
		if (this.getOntologyPrefixCache() != null ) {
			ret = this.getOntologyPrefixCache().get(ontology);
			if (ret != null) {
				// found in cache. Return immediately
				return ret;
			}
		}
		// It was not found in cache. Just delegate...
		if (this.getProwlModelUserDelegator() != null) {
			ret = this.getProwlModelUserDelegator().getOntologyPrefixManager(ontology);
			// update cache if cache is available
			if (this.getOntologyPrefixCache() != null) {
				this.getOntologyPrefixCache().put(ontology, ret);
			}
			// return ret;
		}
		
		return ret;
	}
	
	
	/**
	 * @return the resource. A new instance will be instantiated if no one was specified.
	 * @see unbbayes.util.ResourceController
	 */
	public ResourceBundle getResource() {
		if (resource == null) {
			unbbayes.util.ResourceController.newInstance().getBundle(
					unbbayes.io.mebn.resources.IoMebnResources.class.getName(),
					Locale.getDefault(),
					this.getClass().getClassLoader()
				);
		}
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}

	/**
	 * The name of the hasMFrag object property.
	 * You may change this value if you want this class to look for
	 * another object property when searching a MFrag in a MTheory.
	 * @return the hasMFragObjectProperty
	 */
	public String getHasMFragObjectProperty() {
		return hasMFragObjectProperty;
	}

	/**
	 * The name of the hasMFrag object property.
	 * You may change this value if you want this class to look for
	 * another object property when searching a MFrag in a MTheory.
	 * @param hasMFragObjectProperty the hasMFragObjectProperty to set
	 */
	public void setHasMFragObjectProperty(String hasMFragObjectProperty) {
		this.hasMFragObjectProperty = hasMFragObjectProperty;
	}

	
	/**
	 * {@link OWLAPICompatiblePROWLIO} implements delegator design patter without directly extending LoaderPrOwlIO (because of incompatible interfaces).
	 * Some functionalities will be delegated to this wrapped object when necessary (e.g. to extract naming patterns like scope separators, etc.).
	 * @return the wrappedLoaderPrOwlIO. A new instance will be returned if nothing is specified.
	 */
	public LoaderPrOwlIO getWrappedLoaderPrOwlIO() {
		if (wrappedLoaderPrOwlIO == null) {
			wrappedLoaderPrOwlIO = new LoaderPrOwlIO();
		}
		return wrappedLoaderPrOwlIO;
	}

	/**
	 * {@link OWLAPICompatiblePROWLIO} implements delegator design patter without directly extending LoaderPrOwlIO (because of incompatible interfaces).
	 * Some functionalities will be delegated to this wrapped object when necessary (e.g. to extract naming patterns like scope separators, etc.)
	 * @param wrappedLoaderPrOwlIO the wrappedLoaderPrOwlIO to set
	 */
	public void setWrappedLoaderPrOwlIO(LoaderPrOwlIO wrappedLoaderPrOwlIO) {
		this.wrappedLoaderPrOwlIO = wrappedLoaderPrOwlIO;
	}

	/**
	 * It stores the value returned from {@link #loadMTheoryAndMFrags(OWLModelManager, OWLOntology, MultiEntityBayesianNetwork)}.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the nameToMFragMap
	 */
	protected Map<String, MFrag> getMapNameToMFrag() {
		return mapNameToMFrag;
	}

	/**
	 * 
	 * It stores the value returned from {@link #loadMTheoryAndMFrags(OWLModelManager, OWLOntology, MultiEntityBayesianNetwork)}.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param nameToMFragMap the nameToMFragMap to set
	 */
	protected void setMapNameToMFrag(Map<String, MFrag> nameToMFragMap) {
		this.mapNameToMFrag = nameToMFragMap;
	}

	/**
	 * It maps a name to an instance of ObjectEntities.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapLabelToObjectEntity
	 */
	protected Map<String, Entity> getMapLabelToObjectEntity() {
		return mapLabelToObjectEntity;
	}

	/**
	 * A mapping from name to instances of object entities.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapLabelToObjectEntity the mapLabelToObjectEntity to set
	 */
	protected void setMapLabelToObjectEntity(
			Map<String, Entity> mapLabelToObjectEntity) {
		this.mapLabelToObjectEntity = mapLabelToObjectEntity;
	}

	/**
	 * A mapping from name to types (meta entities).
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapNameToType
	 */
	protected Map<String, Type> getMapNameToType() {
		return mapNameToType;
	}

	/**
	 * A mapping from name to types (meta entities).
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapNameToType the mapNameToType to set
	 */
	protected void setMapNameToType(Map<String, Type> mapNameToType) {
		this.mapNameToType = mapNameToType;
	}

	/**
	 * A mapping from names of categorical states (entities or boolean entities) to a set of names of nodes using it. It stores informations
	 * about globally exclusive elements.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapCategoricalStateGloballyExclusiveNodes
	 */
	protected Map<String, Set<String>> getMapCategoricalStateGloballyExclusiveNodes() {
		return mapCategoricalStateGloballyExclusiveNodes;
	}

	/**
	 * A mapping from names of categorical states (entities or boolean entities) to a set of names of nodes using it. It stores informations
	 * about globally exclusive elements.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapCategoricalStateGloballyExclusiveNodes the mapCategoricalStateGloballyExclusiveNodes to set
	 */
	protected void setMapCategoricalStateGloballyExclusiveNodes(
			Map<String, Set<String>> mapCategoricalStateGloballyExclusiveNodes) {
		this.mapCategoricalStateGloballyExclusiveNodes = mapCategoricalStateGloballyExclusiveNodes;
	}

	/**
	 * A mapping from names to categorical state entities.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapCategoricalStates
	 */
	protected Map<String, CategoricalStateEntity> getMapCategoricalStates() {
		return mapCategoricalStates;
	}

	/**
	 * A mapping from names to categorical state entities.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapCategoricalStates the mapCategoricalStates to set
	 */
	protected void setMapCategoricalStates(
			Map<String, CategoricalStateEntity> mapCategoricalStates) {
		this.mapCategoricalStates = mapCategoricalStates;
	}

	/**
	 * A mapping from names to boolean state entities.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapBooleanStates
	 */
	protected Map<String, Entity> getMapBooleanStates() {
		return mapBooleanStates;
	}

	/**
	 * A mapping from names to boolean state entities.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapBooleanStates the mapBooleanStates to set
	 */
	protected void setMapBooleanStates(Map<String, Entity> mapBooleanStates) {
		this.mapBooleanStates = mapBooleanStates;
	}

	/**
	 * A mapping from names to nodes (resident, input, context or ordinary variables).
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapLoadedNodes
	 */
	protected Map<String, INode> getMapLoadedNodes() {
		return mapLoadedNodes;
	}

	/**
	 * A mapping from names to nodes (resident, input, context or ordinary variables).
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapLoadedNodes the mapLoadedNodes to set
	 */
	protected void setMapLoadedNodes(Map<String, INode> mapLoadedNodes) {
		this.mapLoadedNodes = mapLoadedNodes;
	}

	/**
	 * A mapping from names to built in random variables (which could not be stored in {@link #getMapLoadedNodes()}) because built in RVs do not share a common interface.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapBuiltInRV
	 */
	protected Map<String, BuiltInRV> getMapBuiltInRV() {
		return mapBuiltInRV;
	}

	/**
	 * A mapping from names to built in random variables (which could not be stored in {@link #getMapLoadedNodes()}) because built in RVs do not share a common interface.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapBuiltInRV the mapBuiltInRV to set
	 */
	protected void setMapBuiltInRV(Map<String, BuiltInRV> mapBuiltInRV) {
		this.mapBuiltInRV = mapBuiltInRV;
	}

	/**
	 * A mapping storing onty top level context nodes (context nodes that are not inner nodes).
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapTopLevelContextNodes
	 */
	protected Map<String, ContextNode> getMapTopLevelContextNodes() {
		return mapTopLevelContextNodes;
	}

	/**
	 * A mapping storing onty top level context nodes (context nodes that are not inner nodes).
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapTopLevelContextNodes the mapTopLevelContextNodes to set
	 */
	protected void setMapTopLevelContextNodes(
			Map<String, ContextNode> mapTopLevelContextNodes) {
		this.mapTopLevelContextNodes = mapTopLevelContextNodes;
	}

	/**
	 * A mapping from argument name to argument.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * This map is usually updated in {@link #loadGenerativeInputNode(OWLOntology, MultiEntityBayesianNetwork)}, {@link #loadDomainResidentNode(OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadContextNode(OWLOntology, MultiEntityBayesianNetwork)}
	 * @return the mapArgument
	 */
	protected Map<String, Argument> getMapArgument() {
		return mapArgument;
	}

	/**
	 * A mapping from argument name to argument.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * This map is usually updated in {@link #loadGenerativeInputNode(OWLOntology, MultiEntityBayesianNetwork)}, {@link #loadDomainResidentNode(OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadContextNode(OWLOntology, MultiEntityBayesianNetwork)}
	 * @param mapArgument the mapArgument to set
	 */
	protected void setMapArgument(Map<String, Argument> mapArgument) {
		this.mapArgument = mapArgument;
	}

	/**
	 * A mapping from a context node's name to either an instance of {@link ResidentNode} or {@link BuiltInRV}.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapIsContextInstanceOf
	 * @deprecated move this map to {@link ContextNode}
	 * @see #buildFormulaTree(ContextNode)
	 */
	protected Map<ContextNode, Object> getMapIsContextInstanceOf() {
		return mapIsContextInstanceOf;
	}

	/**
	 * A mapping from a context node's name to either an instance of {@link ResidentNode} or {@link BuiltInRV}.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapIsContextInstanceOf the mapIsContextInstanceOf to set
	 * @deprecated move this map to {@link ContextNode}
	 * @see #buildFormulaTree(ContextNode)
	 */
	protected void setMapIsContextInstanceOf(
			Map<ContextNode, Object> mapIsContextInstanceOf) {
		this.mapIsContextInstanceOf = mapIsContextInstanceOf;
	}

	
	/**
	 * This is the ontology used by this class in the last time {@link #loadMebn(File)} was called.
	 * @return the lastOWLOntology
	 */
	public OWLOntology getLastOWLOntology() {
		return lastOWLOntology;
	}
	
	/**
	 * 
	 * This is the ontology used by this class in the last time {@link #loadMebn(File)} was called.
	 * @param lastOWLOntology the lastOWLOntology to set
	 */
	public void setLastOWLOntology(OWLOntology lastOWLOntology) {
		this.lastOWLOntology = lastOWLOntology;
	}

	/**
	 * A mapping from names to instances of resident nodes that its content were already filled in.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapFilledResidentNodes
	 * @see #loadDomainResidentNode(OWLOntology, MultiEntityBayesianNetwork)
	 */
	protected Map<String, ResidentNode> getMapFilledResidentNodes() {
		return mapFilledResidentNodes;
	}

	/**
	 * A mapping from names to instances of resident nodes that its content were already filled in.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapFilledResidentNodes the mapFilledResidentNodes to set
	 * @see #loadDomainResidentNode(OWLOntology, MultiEntityBayesianNetwork)
	 */
	protected void setMapFilledResidentNodes(
			Map<String, ResidentNode> mapFilledResidentNodes) {
		this.mapFilledResidentNodes = mapFilledResidentNodes;
	}

	/**
	 * A mapping from names to instances of input nodes that its content were already filled in.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @return the mapFilledInputNodes
	 * @see #loadGenerativeInputNode(OWLOntology, MultiEntityBayesianNetwork)
	 */
	protected Map<String, InputNode> getMapFilledInputNodes() {
		return mapFilledInputNodes;
	}

	/**
	 * A mapping from names to instances of input nodes that its content were already filled in.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapFilledInputNodes the mapFilledInputNodes to set
	 * @see #loadGenerativeInputNode(OWLOntology, MultiEntityBayesianNetwork)
	 */
	protected void setMapFilledInputNodes(Map<String, InputNode> mapFilledInputNodes) {
		this.mapFilledInputNodes = mapFilledInputNodes;
	}

	/**
	 * A mapping from names to instances of ordinary variables that its content were already filled in.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @see #loadOrdinaryVariable(OWLOntology, MultiEntityBayesianNetwork)
	 * @return the mapFilledOrdinaryVariables
	 */
	protected Map<String, OrdinaryVariable> getMapFilledOrdinaryVariables() {
		return mapFilledOrdinaryVariables;
	}

	/**
	 * A mapping from names to instances of ordinary variables that its content were already filled in.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @see #loadOrdinaryVariable(OWLOntology, MultiEntityBayesianNetwork)
	 * @param mapFilledOrdinaryVariables the mapFilledOrdinaryVariables to set
	 */
	protected void setMapFilledOrdinaryVariables(
			Map<String, OrdinaryVariable> mapFilledOrdinaryVariables) {
		this.mapFilledOrdinaryVariables = mapFilledOrdinaryVariables;
	}

	/**
	 * A mapping from names to instances of generic arguments that its content were already filled in.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @see #loadArgRelationship(OWLOntology, MultiEntityBayesianNetwork)
	 * @return the mapFilledArguments
	 */
	protected Map<String, Argument> getMapFilledArguments() {
		return mapFilledArguments;
	}

	/**
	 * A mapping from names to instances of generic arguments that its content were already filled in.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @see #loadArgRelationship(OWLOntology, MultiEntityBayesianNetwork)
	 * @param mapFilledArguments the mapFilledArguments to set
	 */
	protected void setMapFilledArguments(Map<String, Argument> mapFilledArguments) {
		this.mapFilledArguments = mapFilledArguments;
	}

	/**
	 * A mapping from names to instances of simple arguments that its content were already filled in.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @see #loadSimpleArgRelationship(OWLOntology, MultiEntityBayesianNetwork)
	 * @return the mapFilledSimpleArguments
	 */
	protected Map<String, Argument> getMapFilledSimpleArguments() {
		return mapFilledSimpleArguments;
	}

	/**
	 * A mapping from names to instances of simple arguments that its content were already filled in.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @see #loadSimpleArgRelationship(OWLOntology, MultiEntityBayesianNetwork)
	 * @param mapFilledSimpleArguments the mapFilledSimpleArguments to set
	 */
	protected void setMapFilledSimpleArguments(
			Map<String, Argument> mapFilledSimpleArguments) {
		this.mapFilledSimpleArguments = mapFilledSimpleArguments;
	}

	/**
	 * A mapping from names to instances of object entities' individuals (a particular value of an entity - values of ordinary variables).
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @see #loadObjectEntityIndividuals(OWLOntology, MultiEntityBayesianNetwork)
	 * @return the mapLoadedObjectEntityIndividuals
	 */
	protected Map<String, ObjectEntityInstance> getMapLoadedObjectEntityIndividuals() {
		return mapLoadedObjectEntityIndividuals;
	}

	/**
	 * A mapping from names to instances of object entities' individuals (a particular value of an entity - values of ordinary variables).
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @see #loadObjectEntityIndividuals(OWLOntology, MultiEntityBayesianNetwork)
	 * @param mapLoadedObjectEntityIndividuals the mapLoadedObjectEntityIndividuals to set
	 */
	protected void setMapLoadedObjectEntityIndividuals(
			Map<String, ObjectEntityInstance> mapLoadedObjectEntityIndividuals) {
		this.mapLoadedObjectEntityIndividuals = mapLoadedObjectEntityIndividuals;
	}

	/**
	 * This string identifies the URI of pr-owl elements (http://www.pr-owl.org/pr-owl.owl).
	 * The default value should  be {@value #PROWL2_NAMESPACEURI}
	 * @return the prowlOntologyNamespaceURI
	 */
	public String getProwlOntologyNamespaceURI() {
		return prowlOntologyNamespaceURI;
	}

	/**
	 * This string identifies the URI of pr-owl elements (http://www.pr-owl.org/pr-owl.owl).
	 * The default value should  be {@value #PROWL2_NAMESPACEURI}
	 * @param prowlOntologyNamespaceURI the prowlOntologyNamespaceURI to set
	 */
	public void setProwlOntologyNamespaceURI(String prowlOntologyNamespaceURI) {
		if (this.getProwlModelUserDelegator() != null) {
			((DefaultPROWL2ModelUser)this.getProwlModelUserDelegator()).setProwlOntologyNamespaceURI(prowlOntologyNamespaceURI);
		}
		this.prowlOntologyNamespaceURI = prowlOntologyNamespaceURI;
	}

	/**
	 * This factory instantiates the MEBN elements loaded from an OWL ontology.
	 * This is useful when the IO class should instantiate subclasses of
	 * {@link MultiEntityBayesianNetwork}, {@link MFrag}, {@link ResidentNode}, etc.
	 * @return the mebnFactory
	 */
	public IMEBNElementFactory getMEBNFactory() {
		return mebnFactory;
	}

	/**
	 * This factory instantiates the MEBN elements loaded from an OWL ontology.
	 * This is useful when the IO class should instantiate subclasses of
	 * {@link MultiEntityBayesianNetwork}, {@link MFrag}, {@link ResidentNode}, etc.
	 * @param mebnFactory the mebnFactory to set
	 */
	public void setMEBNFactory(IMEBNElementFactory mebnFactory) {
		this.mebnFactory = mebnFactory;
	}

	/**
	 * This is the last OWLReasoner used by {@link #loadMEBNFromOntology(OWLOntology, OWLReasoner)}.
	 * This reasoner will be used if none is specified in {@link #loadMEBNFromOntology(OWLOntology, OWLReasoner)}.
	 * @return
	 */
	public OWLReasoner getLastOWLReasoner() {
		return owlReasoner;
	}

	/**
	 * This is the last OWLReasoner used by {@link #loadMEBNFromOntology(OWLOntology, OWLReasoner)}.
	 * This reasoner will be used if none is specified in {@link #loadMEBNFromOntology(OWLOntology, OWLReasoner)}.
	 * @param owlReasoner
	 */
	public void setLastOWLReasoner(OWLReasoner owlReasoner) {
		this.owlReasoner = owlReasoner;
	}
	
	/**
	 * It will lazily instantiate OWLClassExpressionParserFacade if none was specified.
	 * @return the owlClassExpressionParserDelegator
	 */
	public IOWLClassExpressionParserFacade getOwlClassExpressionParserDelegator() {
		if (owlClassExpressionParserDelegator == null) {
			// instantiate the default OWL expression parser
			owlClassExpressionParserDelegator = OWLClassExpressionParserFacade.getInstance(this.getLastOWLOntology());
		}
		return owlClassExpressionParserDelegator;
	}

	/**
	 * @param owlClassExpressionParserDelegator the owlClassExpressionParserDelegator to set
	 */
	public void setOwlClassExpressionParserDelegator(
			IOWLClassExpressionParserFacade owlClassExpressionParserDelegator) {
		this.owlClassExpressionParserDelegator = owlClassExpressionParserDelegator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IOWLClassExpressionParserFacade#parseExpression(java.lang.String)
	 */
	public OWLClassExpression parseExpression(String expression) {
		return this.getOwlClassExpressionParserDelegator().parseExpression(expression);
	}


	/**
	 * Calls to {@link IPROWL2ModelUser} will be delegated to this object.
	 * @return the prowlModelUserDelegator
	 */
	public IPROWL2ModelUser getProwlModelUserDelegator() {
		return prowlModelUserDelegator;
	}

	/**
	 * Calls to {@link IPROWL2ModelUser} will be delegated to this object.
	 * @param prowlModelUserDelegator the prowlModelUserDelegator to set
	 */
	public void setProwlModelUserDelegator(
			IPROWL2ModelUser prowlModelUserDelegator) {
		this.prowlModelUserDelegator = prowlModelUserDelegator;
	}

	/**
	 * Calls to {@link #getNonPROWLClasses(OWLOntology)} and {@link #resetNonPROWLClassExtractor()} will be delegated to this object
	 * @return the nonPROWLClassExtractor
	 */
	public INonPROWLClassExtractor getNonPROWLClassExtractor() {
		return nonPROWLClassExtractor;
	}

	/**
	 * Calls to {@link #getNonPROWLClasses(OWLOntology)} and {@link #resetNonPROWLClassExtractor()} will be delegated to this object
	 * @param nonPROWLClassExtractor the nonPROWLClassExtractor to set
	 */
	public void setNonPROWLClassExtractor(
			INonPROWLClassExtractor nonPROWLClassExtractor) {
		this.nonPROWLClassExtractor = nonPROWLClassExtractor;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.INonPROWLClassExtractor#resetNonPROWLClassExtractor()
	 */
	public void resetNonPROWLClassExtractor() {
		if (this.getNonPROWLClassExtractor() != null) {
			this.getNonPROWLClassExtractor().resetNonPROWLClassExtractor();
		}
	}



	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.PrOwlIO#save(java.io.File, unbbayes.prs.Graph)
	 */
	@Override
	public void save(File output, Graph net) throws IOException {
		
		if (net instanceof MultiEntityBayesianNetwork) {
			this.saveMebn(output, (MultiEntityBayesianNetwork)net);
		} else {
			throw new IOException(net + " is not an instance of MultiEntityBayesianNetwork" );
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.PrOwlIO#getFileExtension()
	 */
	@Override
	public String getFileExtension() {
		// TODO Auto-generated method stub
		return super.getFileExtension();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.PrOwlIO#getSupportedFilesDescription(boolean)
	 */
	@Override
	public String getSupportedFilesDescription(boolean isLoadOnly) {
		if (!isLoadOnly) {
			return "";
		}
		return super.getSupportedFilesDescription(isLoadOnly);
	}
	
	/**
	 * This method is a facilitator to clear and initialize internal cache.
	 * @see #getOntologyPrefixCache()
	 */
	public void resetCache() {
		try {
			this.setOntologyPrefixCache(new HashMap<OWLOntology, PrefixManager>());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is the name of a property linking a MFrag to a resident node.
	 * Default value is {@link IPROWL2ModelUser#HASRESIDENTNODE}, because
	 * the PR-OWL2 definition and PR-OWL1 definitions are the same.
	 * @return the hasResidentNodeObjectPropertyName
	 */
	public String getHasResidentNodeObjectPropertyName() {
		return hasResidentNodeObjectPropertyName;
	}

	/**
	 * This is the name of a property linking a MFrag to a resident node.
	 * Default value is {@link IPROWL2ModelUser#HASRESIDENTNODE}, because
	 * the PR-OWL2 definition and PR-OWL1 definitions are the same.
	 * @param hasResidentNodeObjectPropertyName the hasResidentNodeObjectPropertyName to set
	 */
	public void setHasResidentNodeObjectPropertyName(
			String hasResidentNodeObjectPropertyName) {
		this.hasResidentNodeObjectPropertyName = hasResidentNodeObjectPropertyName;
	}

	/**
	 * This is the name of a property linking a MFrag to an input node.
	 * Default value is {@link IPROWL2ModelUser#HASINPUTNODE}, because
	 * the PR-OWL2 definition and PR-OWL1 definitions are the same.
	 * @return the hasInputNodeObjectPropertyName
	 */
	public String getHasInputNodeObjectPropertyName() {
		return hasInputNodeObjectPropertyName;
	}

	/**
	 * This is the name of a property linking a MFrag to an input node.
	 * Default value is {@link IPROWL2ModelUser#HASINPUTNODE}, because
	 * the PR-OWL2 definition and PR-OWL1 definitions are the same.
	 * @param hasInputNodeObjectPropertyName the hasInputNodeObjectPropertyName to set
	 */
	public void setHasInputNodeObjectPropertyName(
			String hasInputNodeObjectPropertyName) {
		this.hasInputNodeObjectPropertyName = hasInputNodeObjectPropertyName;
	}

	/**
	 * This is the name of a property linking a MFrag to a context node.
	 * Default value is {@link IPROWL2ModelUser#HASCONTEXTNODE}, because
	 * the PR-OWL2 definition and PR-OWL1 definitions are the same.
	 * @return the hasContextNodeObjectPropertyName
	 */
	public String getHasContextNodeObjectPropertyName() {
		return hasContextNodeObjectPropertyName;
	}

	/**
	 * This is the name of a property linking a MFrag to a context node.
	 * Default value is {@link IPROWL2ModelUser#HASCONTEXTNODE}, because
	 * the PR-OWL2 definition and PR-OWL1 definitions are the same.
	 * @param hasContextNodeObjectPropertyName the hasContextNodeObjectPropertyName to set
	 */
	public void setHasContextNodeObjectPropertyName(
			String hasContextNodeObjectPropertyName) {
		this.hasContextNodeObjectPropertyName = hasContextNodeObjectPropertyName;
	}

	/**
	 * This is the name of a property linking a MFrag to an ordinary variable.
	 * Default value is "hasOVariable".
	 * @return the hasOVariableObjectPropertyName
	 */
	public String getHasOVariableObjectPropertyName() {
		return hasOVariableObjectPropertyName;
	}

	/**
	 * This is the name of a property linking a MFrag to an ordinary variable.
	 * Default value is "hasOVariable".
	 * @param hasOVariableObjectPropertyName the hasOVariableObjectPropertyName to set
	 */
	public void setHasOVariableObjectPropertyName(
			String hasOVariableObjectPropertyName) {
		this.hasOVariableObjectPropertyName = hasOVariableObjectPropertyName;
	}

	/**
	 * This is the name of a property linking an ordinary variable to its actual value/type.
	 * Default value is "isSubsBy".
	 * @return the isSubsByObjectPropertyName
	 */
	public String getIsSubsByObjectPropertyName() {
		return isSubsByObjectPropertyName;
	}

	/**
	 * This is the name of a property linking an ordinary variable to its actual value/type.
	 * Default value is "isSubsBy".
	 * @param isSubsByObjectPropertyName the isSubsByObjectPropertyName to set
	 */
	public void setIsSubsByObjectPropertyName(String isSubsByObjectPropertyName) {
		this.isSubsByObjectPropertyName = isSubsByObjectPropertyName;
	}

	/**
	 * In a PR-OWL ontology, an ordinary variable is saved using the following naming pattern:
	 * {MFragName}{SEPARATOR}{OVName}.
	 * This field contains the value of {SEPARATOR}.
	 * @return the oVariableScopeSeparator
	 */
	public String getOVariableScopeSeparator() {
		return oVariableScopeSeparator;
	}

	/**
	 * In a PR-OWL ontology, an ordinary variable is saved using the following naming pattern:
	 * {MFragName}{SEPARATOR}{OVName}.
	 * This field contains the value of {SEPARATOR}.
	 * @param oVariableScopeSeparator the oVariableScopeSeparator to set
	 */
	public void setOVariableScopeSeparator(String oVariableScopeSeparator) {
		this.oVariableScopeSeparator = oVariableScopeSeparator;
	}

	/**
	 * This map caches the prefix managers obtainable from {@link #getOntologyPrefixManager(OWLOntology)}.
	 * @return the ontologyPrefixCache
	 */
	protected Map<OWLOntology, PrefixManager> getOntologyPrefixCache() {
		return ontologyPrefixCache;
	}

	/**
	 * This map caches the prefix managers obtainable from {@link #getOntologyPrefixManager(OWLOntology)}.
	 * @param ontologyPrefixCache the ontologyPrefixCache to set
	 */
	protected void setOntologyPrefixCache(
			Map<OWLOntology, PrefixManager> ontologyPrefixCache) {
		this.ontologyPrefixCache = ontologyPrefixCache;
	}

	/**
	 * This is the name of hasArgument property name.
	 * The default value is "hasArgument"
	 * @return the hasArgumentPropertyName
	 */
	public String getHasArgumentPropertyName() {
		return hasArgumentPropertyName;
	}

	/**
	 * This is the name of hasArgument property name.
	 * The default value is "hasArgument"
	 * @param hasArgumentPropertyName the hasArgumentPropertyName to set
	 */
	public void setHasArgumentPropertyName(String hasArgumentPropertyName) {
		this.hasArgumentPropertyName = hasArgumentPropertyName;
	}
	

	/**
	 * If true, it will initialize and use the reasoner in order to extract PR-OWL elements from the ontology
	 * @return the initializeReasoner
	 */
	public boolean isToInitializeReasoner() {
		return initializeReasoner;
	}

	/**
	 * If true, it will initialize and use the reasoner in order to extract PR-OWL elements from the ontology
	 * @param initializeReasoner the initializeReasoner to set
	 */
	public void setToInitializeReasoner(boolean initializeReasoner) {
		this.initializeReasoner = initializeReasoner;
	}

	
}
