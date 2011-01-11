/**
 * 
 */
package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import unbbayes.gui.InternalErrorDialog;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
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
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
import unbbayes.util.Debug;

/**
 * This class extends {@link PrOwlIO} in order to
 * support OWL2 ontologies. Some refactories were made in order to improve extensibility (e.g. template method design pattern).
 * @author Shou Matsumoto
 *
 */
public class OWLAPICompatiblePROWL2IO extends PrOwlIO implements IOWLAPIOntologyUser {
	
	private String prowlOntologyNamespaceURI = "http://www.pr-owl.org/pr-owl.owl";
	
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

	private Map<ContextNode, Object> mapIsContextInstanceOf = new HashMap<ContextNode, Object>();

	private Map<String, ResidentNode> mapFilledResidentNodes;

	private Map<String, InputNode> mapFilledInputNodes;

	private Map<String, OrdinaryVariable> mapFilledOrdinaryVariables;

	private Map<String, Argument> mapFilledArguments;

	private Map<String, Argument> mapFilledSimpleArguments;

	private Map<String, ObjectEntityInstance> mapLoadedObjectEntityIndividuals;

	/**
	 * The default constructor is public only because
	 * plug-in infrastructure may require default constructor
	 * (with no parameters) to be visible.
	 * It won't initialize complex fields (e.g. resource classes, wrapped classes, etc.).
	 * @deprecated use {@link #newInstance()} instead
	 */
	public OWLAPICompatiblePROWL2IO() {
		super();
	}
	
	/**
	 * This is the constructor method to be used in order to create new instances of {@link OWLAPICompatiblePROWL2IO}
	 * @return
	 */
	public static MebnIO newInstance() {
		OWLAPICompatiblePROWL2IO ret = new OWLAPICompatiblePROWL2IO();
		ret.setResource(unbbayes.util.ResourceController.newInstance().getBundle(
				unbbayes.io.mebn.resources.IoMebnResources.class.getName(),
				Locale.getDefault(),
				OWLAPICompatiblePROWL2IO.class.getClassLoader()
			));
		ret.setWrappedLoaderPrOwlIO(new LoaderPrOwlIO());
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.MebnIO#loadMebn(java.io.File)
	 */
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException,
			IOMebnException {
		try {
			// extract file name without extension
			String fileNameNoExtension = file.getName();
			try {
				fileNameNoExtension = fileNameNoExtension.substring(0 , fileNameNoExtension.lastIndexOf('.'));
				if ((fileNameNoExtension == null) || (fileNameNoExtension.length() <= 0)) {
					fileNameNoExtension = file.getName();	// use unmodified file name by default
				}
			} catch (Exception e) {
				Debug.println(this.getClass(), "Could not extract file name with no extension.", e);
				fileNameNoExtension = file.getName(); // // use unmodified file name by default
			}
			
			// this is a instance of MEBN to be filled. The name will be updated after loadMTheoryAndMFrags
			MultiEntityBayesianNetwork mebn = new MultiEntityBayesianNetwork(fileNameNoExtension);
			
			// the main access point to ontologies is the OWLOntology and OWLOntologyManager (both from OWL API)
			if (this.getLastOWLOntology()  == null) {
				// load ontology from file and set as active
				this.setLastOWLOntology(OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file));
			}
			
			// Start loading ontology. This is template method design pattern.

			// load MTheory. The nameToMFragMap maps a name to a MFrag.
			try {
				this.setMapNameToMFrag(this.loadMTheoryAndMFrags(this.getLastOWLOntology(), mebn));
			} catch (IOMebnException e) {
				// the ontology does not contain PR-OWL specific elements. Stop loading PR-OWL and return an empty mebn.
				e.printStackTrace();
				return mebn;
			}
			
			// load object entities and fill the mapping of object entities
			this.setMapLabelToObjectEntity(this.loadObjectEntity(this.getLastOWLOntology(), mebn));

			// load meta entities and fill the mapping of types
			this.setMapNameToType(this.loadMetaEntitiesClasses(this.getLastOWLOntology(), mebn));
			
			// load categorical entities. The mapCategoricalStateGloballyExclusiveNodes stores globally exclusive nodes (a map of state -> nodes)
			this.setMapCategoricalStates(this.loadCategoricalStateEntity(this.getLastOWLOntology(), mebn));
			
			// load boolean states. Reuse mapCategoricalStateGloballyExclusiveNodes.
			this.setMapBooleanStates(this.loadBooleanStateEntity(this.getLastOWLOntology(), mebn));
			
			// load content of MFrag (nodes, ordinary variables...)
			this.setMapLoadedNodes(this.loadDomainMFragContents(this.getLastOWLOntology(), mebn));
			
			// load built in random variables. Reuse mapLoadedNodes.
			this.setMapBuiltInRV(this.loadBuiltInRV(this.getLastOWLOntology(), mebn));

			// load content of context nodes. The mapIsContextInstanceOf mapps Context nodes to either BuiltInRV or ResidentNode. The mapArgument mapps a name to an argument
			this.setMapTopLevelContextNodes(this.loadContextNode( this.getLastOWLOntology(), mebn));
			
			// load content of resident nodes
			this.setMapFilledResidentNodes(this.loadDomainResidentNode(this.getLastOWLOntology(), mebn));
			
			// load content of input nodes
			this.setMapFilledInputNodes(this.loadGenerativeInputNode(this.getLastOWLOntology(), mebn));
			
			// load content of ordinary variables
			this.setMapFilledOrdinaryVariables(this.loadOrdinaryVariable(this.getLastOWLOntology(), mebn));
			
			// load generic arguments relationships
			this.setMapFilledArguments(this.loadArgRelationship(this.getLastOWLOntology(), mebn));
			
			// load simple arguments
			this.setMapFilledSimpleArguments(this.loadSimpleArgRelationship(this.getLastOWLOntology(), mebn));
			
			// adjust the order of arguments (the appearance order of arguments may not be the correct order)
			this.ajustArgumentOfNodes(mebn);
			
			// load the content of the formulas inside context nodes
			this.buildFormulaTrees(this.getMapTopLevelContextNodes(), mebn);
			
			// Load individuals of object entities (ObjectEntityInstances)
			this.setMapLoadedObjectEntityIndividuals(loadObjectEntityIndividuals(this.getLastOWLOntology(), mebn));
			
			// fill the storage implementor of MEBN (a reference to an object that loaded/saved the mebn last time)
			mebn.setStorageImplementor(OWLAPIStorageImplementorDecorator.newInstance(this.getLastOWLOntology()));
			
			return mebn;
		} catch (Exception e) {
			// if we fail to load OWL2 ontology, lets use the old fashioned way
			e.printStackTrace();
			return super.loadMebn(file);
		}
	}


	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.MebnIO#saveMebn(java.io.File, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	public void saveMebn(File file, MultiEntityBayesianNetwork mebn)
			throws IOException, IOMebnException {
		// TODO Auto-generated method stub
		super.saveMebn(file, mebn);
	}

	/**
	 * Extract a simple name from an owl entity. If the ID of an entity is in [URI]#[Name] format, then it will extract the [Name].
	 * @param ontology : the ontology where the owlEntity belongs.
	 * @param owlEntity : the owlEntity from where we are going to extract a name.
	 * @return the name extracted from owlEntity.
	 */
	public String extractName(OWLOntology ontology, OWLEntity owlEntity) {
		// TODO remove redundant calls to this method (some methods are calling this method more than once despite only 1 call was really necessary).
		// assertions
		if (owlEntity == null) {
			return null;
		}
		String name = owlEntity.toStringID();
		// the ID is probably in the following format: <URI>#<Name>
		if (name != null) {
			try {
				while (name.contains("#")) {
					name = name.substring(name.indexOf('#') + 1, name.length());
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return name;
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
		OWLClass owlNamedClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(MTHEORY, prefixManager);
		
		Set<OWLIndividual> instances = owlNamedClass.getIndividuals(ontology);
		if (instances == null || instances.size() <= 0) {
			throw new IOMebnException(this.getResource().getString("MTheoryNotExist"));
		}
		
		OWLIndividual individualOne = instances.iterator().next();
		mebn.setName(this.extractName(ontology, individualOne.asOWLNamedIndividual())); 
		mebn.getNamesUsed().add(this.extractName(ontology, individualOne.asOWLNamedIndividual())); 
		
		Debug.println(this.getClass(), "MTheory loaded: " + individualOne); 
		
		// start filling some properties
		
		// comments (description of mebn and mfrags)
		mebn.setDescription(getDescription(ontology, individualOne)); 
		
		// look for the hasMFrag object property
		OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(this.getHasMFragObjectProperty(), prefixManager);
		
		// extract instances related to the MTheory by hasMFrag and iterate over obtained individuals
		for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology)) {
			// ignore annonimous individuals
			if (!individualTwo.isNamed()) {
				continue;
			}
			// remove prefixes from name
			String name = this.extractName(ontology, individualTwo.asOWLNamedIndividual());
			if (name.startsWith(SaverPrOwlIO.MFRAG_NAME_PREFIX)) {
				try {
					name = name.substring(SaverPrOwlIO.MFRAG_NAME_PREFIX.length());
				} catch (Exception e) {
					// We can still try the name with the prefixes.
					e.printStackTrace();
				}
			}
			// instantiate the MFrag and add it to the MTheory
			Debug.println(this.getClass(), "hasDomainMFrag: " + name); 
			MFrag domainMFrag = new MFrag(name, mebn); 
			mebn.addDomainMFrag(domainMFrag); 
			mebn.getNamesUsed().add(name); 
			
			// the mapping still contains the original name (with no prefix removal)
			mapDomainMFrag.put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), domainMFrag); 
		}	
		return mapDomainMFrag;
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
		
		// iterate on subclasses of object entities
		for (OWLClassExpression owlClassExpression : objectEntityClass.getSubClasses(ontology)){
			OWLClass subClass = owlClassExpression.asOWLClass(); 
			if (subClass == null) {
				// it was a unknown type of class (maybe anonymous)
				continue;
			}
			
			// It looks like the old loaderPrOwlIO was not solving hasType object property. We are following such decision.

			try{
				// create object entity
				ObjectEntity objectEntityMebn = mebn.getObjectEntityContainer().createObjectEntity(this.extractName(ontology, subClass)); 	
			    mapObjectEntityLabels.put(objectEntityMebn.getType().getName(), objectEntityMebn); 

			    // set the name as "used", in order for UnBBayes to avoid duplicate names.
				mebn.getNamesUsed().add(this.extractName(ontology, subClass)); 
			} catch(Exception e){
				// perform a exception translation because the method's signature does not allow non-runtime exceptions
				throw new RuntimeException("ObjectEntity: " + subClass, e);
			}
		}	
		
		return mapObjectEntityLabels;
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
		
		// extract individuals of meta entities
		for (OWLIndividual owlIndividual : metaEntityClass.getIndividuals(ontology)){
			
			try{
				// create the meta entity (an object of Type)
			    Type type = mebn.getTypeContainer().createType(this.extractName(ontology, owlIndividual.asOWLNamedIndividual())); 
			    
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
		
		// extract the globally exclusive property
		OWLObjectProperty globallyExclusiveObjectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isGloballyExclusive", prefixManager);

		// extract the categorical states class
		OWLClass categoricalStateClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(CATEGORICAL_STATE, prefixManager);
		
		// extract individuals and iterate
		for (OWLIndividual owlIndividual : categoricalStateClass.getIndividuals(ontology)){
			
			// create a categorical state
			CategoricalStateEntity state = mebn.getCategoricalStatesEntityContainer().createCategoricalEntity(this.extractName(ontology, owlIndividual.asOWLNamedIndividual())); 

			// mark name as "used"
			mebn.getNamesUsed().add(this.extractName(ontology, owlIndividual.asOWLNamedIndividual())); 
			
			// extract globally exclusive nodes related to this categorical entity
			Set<OWLIndividual> globallyExclusiveObjects = owlIndividual.getObjectPropertyValues(globallyExclusiveObjectProperty, ontology);
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
				// fill globally exclusive node names
				Set<OWLIndividual> globallyExclusiveIndividuals = owlIndividual.getObjectPropertyValues(globallyExclusiveObjectProperty, ontology); 
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
		OWLClass owlNamedClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(DOMAIN_MFRAG, prefixManager); 
		
		// iterate on individuals
		for (OWLIndividual individualOne : owlNamedClass.getIndividuals(ontology)){
			MFrag domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, individualOne.asOWLNamedIndividual())); 
			if (domainMFrag == null){
				throw new IOMebnException(this.getResource().getString("DomainMFragNotExistsInMTheory"), "MFrag = " + individualOne); 
			}
			
			Debug.println(this.getClass(), "DomainMFrag loaded: " + individualOne); 
			
			// fill comments
			domainMFrag.setDescription(this.getDescription(ontology, individualOne)); 
			
			/* -> hasResidentNode */
			OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasResidentNode", prefixManager); 
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology)){
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
				ResidentNode domainResidentNode = new ResidentNode(name, domainMFrag); 
				mebn.getNamesUsed().add(name);  // mark name as "used"
				// add node to mfrag
				domainMFrag.addResidentNode(domainResidentNode); 
				// the mappings uses the original names (no prefix removal)
				mapMultiEntityNode.put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), domainResidentNode); 
				Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
			}	
			
			/* -> hasInputNode */
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasInputNode", prefixManager); 
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology)){
				// instantiate input node
				InputNode generativeInputNode = new InputNode(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), domainMFrag); 
				mebn.getNamesUsed().add(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); // mark name as used
				domainMFrag.addInputNode(generativeInputNode);  	 // add to mfrag
				mapMultiEntityNode.put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), generativeInputNode); 				
				Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
			}	
			
			/* -> hasContextNode */
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasContextNode", prefixManager); 
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology)){
				ContextNode contextNode = new ContextNode(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), domainMFrag); 
				mebn.getNamesUsed().add(this.extractName(ontology, individualTwo.asOWLNamedIndividual()));  	// mark name as used
				domainMFrag.addContextNode(contextNode); 				// add to mfrag
				mapMultiEntityNode.put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), contextNode); 				
				Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
			}	
			
			/* -> hasOVariable */
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasOVariable", prefixManager); 
			String ovName = null;
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology)){
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
				OrdinaryVariable oVariable = new OrdinaryVariable(ovName, mebn.getTypeContainer().getDefaultType(), domainMFrag); 
				domainMFrag.addOrdinaryVariable(oVariable); 
				// let's map objects w/ scope identifier included
				mapMultiEntityNode.put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), oVariable); 
				Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
			}
		}	
		
		return mapMultiEntityNode;
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
		for (OWLIndividual individualOne : builtInPr.getIndividuals(ontology)){
			String nameBuiltIn = this.extractName(ontology, individualOne.asOWLNamedIndividual());  // this is the name of the built-in RV
			BuiltInRV builtInRV = null;						  // this variable will hold the instantiated BuiltInRV
			// lets virtually perform a huge switch-case command... 
			// TODO find out a more smart way.
			if (nameBuiltIn.equalsIgnoreCase("and") || nameBuiltIn.endsWith("and")) {
				builtInRV = new BuiltInRVAnd(); 
			} else if (nameBuiltIn.equalsIgnoreCase("or") || nameBuiltIn.endsWith("or")) {
				builtInRV = new BuiltInRVOr(); 
			} else if (nameBuiltIn.equalsIgnoreCase("equalto") || nameBuiltIn.endsWith("equalto")) {
				builtInRV = new BuiltInRVEqualTo(); 
			} else if (nameBuiltIn.equalsIgnoreCase("exists") || nameBuiltIn.endsWith("exists")) {
				builtInRV = new BuiltInRVExists(); 
			} else if (nameBuiltIn.equalsIgnoreCase("forall") || nameBuiltIn.endsWith("forall")) {
				builtInRV = new BuiltInRVForAll(); 
			} else if (nameBuiltIn.equalsIgnoreCase("not") || nameBuiltIn.endsWith("not")) {
				builtInRV = new BuiltInRVNot(); 
			} else if (nameBuiltIn.equalsIgnoreCase("iff") || nameBuiltIn.endsWith("iff")) {
				builtInRV = new BuiltInRVIff(); 
			} else if (nameBuiltIn.equalsIgnoreCase("implies") || nameBuiltIn.endsWith("implies")) {
				builtInRV = new BuiltInRVImplies(); 
			} else {
				Debug.println(this.getClass(), "Unknown builtin RV found: " + individualOne);
				continue;	// let's just ignore unknown elements...
			}	
			// by now, builtInRV != null, but let's just assert it...
			if(builtInRV != null){
				mebn.addBuiltInRVList(builtInRV); 				// add to mebn
				ret.put(this.extractName(ontology, individualOne.asOWLNamedIndividual()), builtInRV); // add to return
				
				Debug.println(this.getClass(), "BuiltInRV loaded: " + individualOne); 				
				
				/* -> hasInputInstance */
				OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasInputInstance", prefixManager); 
				for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology)){
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
		
		OWLClass contextNodePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(CONTEXT_NODE, prefixManager); 
		
		for (OWLIndividual individualOne : contextNodePr.getIndividuals(ontology)){
			
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
			Set<OWLIndividual> instances = individualOne.getObjectPropertyValues(objectProperty, ontology);	
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
			for(OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology) ){
				domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
				if(domainMFrag.containsNode(contextNode) == false){
					throw new IOMebnException(this.getResource().getString("NodeNotExistsInMFrag"), individualOne + ", " + individualTwo); 
				}
				Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);				
			}
			
			/* -> hasArgument */
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasArgument", prefixManager); 
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology) ){
				Argument argument = new Argument(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), contextNode); 
				contextNode.addArgument(argument); 
				this.getMapArgument().put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), argument); 
				Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
			}
			
			/* -> isContextInstanceOf */
			// TODO this is a very dirty code. It needs cleanup. Stop reusing variables in different points
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isContextInstanceOf", prefixManager); 			
			instances = individualOne.getObjectPropertyValues(objectProperty, ontology); 	
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
			instances = individualOne.getObjectPropertyValues(objectProperty, ontology); 	
			
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
		
		OWLClass domainResidentNodePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(DOMAIN_RESIDENT, prefixManager); 
		
		for (OWLIndividual individualOne :  domainResidentNodePr.getIndividuals(ontology)){
			INode mappedNode = this.getMapLoadedNodes().get(this.extractName(ontology, individualOne.asOWLNamedIndividual()));
			if (mappedNode == null || !(mappedNode instanceof ResidentNode)){
				throw new IOMebnException(this.getResource().getString("DomainResidentNotExistsInMTheory"), "Resident = " + individualOne); 
			}
			ResidentNode domainResidentNode = (ResidentNode)mappedNode;
			
			Debug.println(this.getClass(), "Domain Resident loaded: " + individualOne); 			
			
			domainResidentNode.setDescription(getDescription(ontology, individualOne)); 
			
			/* -> isResidentNodeIn  */
			// TODO this code is dirty. Stop using the same variable in different contexts...
			OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isResidentNodeIn", prefixManager); 			
			Set<OWLIndividual> instances = individualOne.getObjectPropertyValues(objectProperty, ontology); 	
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
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasArgument", prefixManager); 
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology)){
				Argument argument = new Argument(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), domainResidentNode); 
				domainResidentNode.addArgument(argument); 
				this.getMapArgument().put(this.extractName(ontology, individualTwo.asOWLNamedIndividual()), argument); 
				Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
			}		
			
			/* -> hasParent */
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasParent", prefixManager); 
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology)){
				INode mappedParent = this.getMapLoadedNodes().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual()));
				if (mappedParent != null) {
					if (mappedParent instanceof ResidentNode){
						ResidentNode aux = (ResidentNode)mappedParent; 
						Edge auxEdge = new Edge(aux, domainResidentNode);
						try{
							mFragOfNode.addEdge(auxEdge); 
						} catch(Exception e){
							e.printStackTrace();
						}
					} else if (mappedParent instanceof InputNode){
						InputNode aux = (InputNode)mappedParent;
						Edge auxEdge = new Edge(aux, domainResidentNode);
						try{
							mFragOfNode.addEdge(auxEdge); 
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
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology)){
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
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology)){
				INode multiEntityNode = this.getMapLoadedNodes().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
				domainResidentNode.addInnerTermFromList((MultiEntityNode)multiEntityNode); 
				((MultiEntityNode)multiEntityNode).addInnerTermOfList(domainResidentNode); 
				Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);			
			}				

			/* -> hasPossibleValues */
			{
				CategoricalStateEntity state = null; 
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasPossibleValues", prefixManager); 			
				for (OWLIndividual individualTwo: individualOne.getObjectPropertyValues(objectProperty, ontology)){
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
										Debug.println(this.getClass(), "Added state link " + link, e);
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
			for (OWLIndividual element : individualOne.getObjectPropertyValues(hasProbDist, ontology)) {
				String cpt = "";
				try {
					Set<OWLLiteral> owlLiterals = element.getDataPropertyValues(hasDeclaration, ontology);
					if (!owlLiterals.isEmpty()) {
						cpt = owlLiterals.iterator().next().getLiteral();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				domainResidentNode.setTableFunction(cpt);
			}
			
			/* isArgTermIn is not checked */
			
			// fill return (extract name again, because the extracted name may be different from the node's name)
			ret.put(this.extractName(ontology, individualOne.asOWLNamedIndividual()), domainResidentNode);
			
		}
		return ret;
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
		
		OWLClass inputNodePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(GENERATIVE_INPUT, prefixManager); 
		
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
			Iterator<OWLIndividual> itAux = individualOne.getObjectPropertyValues(objectProperty, ontology).iterator();
			
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
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasArgument",prefixManager); 
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology) ){
				if (!individualTwo.isNamed()) {
					continue;	// ignore anonymous individuals
				}
				String individualTwoName = this.extractName(ontology, individualTwo.asOWLNamedIndividual());
				Argument argument = new Argument(individualTwoName, generativeInputNode); 
				generativeInputNode.addArgument(argument); 
				this.getMapArgument().put(individualTwoName, argument); 
				Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo); 
			}		
			
			/* -> isInnerTermOf */
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isInnerTermOf", prefixManager); 			
			for (OWLIndividual individualTwo : individualOne.getObjectPropertyValues(objectProperty, ontology) ){
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
		
		OWLClass ordinaryVariablePr = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(ORDINARY_VARIABLE, prefixManager); 
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
			Set<OWLIndividual> instances = individualOne.getObjectPropertyValues(objectProperty, ontology);
			if (!instances.isEmpty()) {
				OWLIndividual individualTwo = instances.iterator().next();
				MFrag domainMFrag = this.getMapNameToMFrag().get(this.extractName(ontology, individualTwo.asOWLNamedIndividual())); 
				if(domainMFrag != oVariable.getMFrag()){
					throw new IOMebnException(this.getResource().getString("isOVariableInError"),  "Ordinary variable = " + individualOne); 
				}
				Debug.println(this.getClass(), "-> " + individualOne + ": " + objectProperty + " = " + individualTwo);			
			}
			
			/* -> isSubsBy */
			
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isSubsBy", prefixManager); 			
			instances = individualOne.getObjectPropertyValues(objectProperty, ontology); 	
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
			
			Set<OWLIndividual> individuals = individualOne.getObjectPropertyValues(objectProperty, ontology);
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
			Set<OWLLiteral> literals = individualOne.getDataPropertyValues(hasArgNumber, ontology);
			try {
				argument.setArgNumber(Integer.parseInt(literals.iterator().next().getLiteral()));
			} catch (Exception e) {
				// the argnumber was empty or invalid... This is an error, but lets try going on...
				e.printStackTrace();
			}
			
			/* -> isArgumentOf  */
			objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isArgumentOf", prefixManager); 			
			Set<OWLIndividual> instances = individualOne.getObjectPropertyValues(objectProperty, ontology); 	
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
			Set<OWLIndividual> instances = individualOne.getObjectPropertyValues(objectProperty, ontology); 	
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
	        Set<OWLLiteral> owlLiterals = individualOne.getDataPropertyValues(hasArgNumber, ontology);
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
	 */
	protected Map<String, ObjectEntityInstance> loadObjectEntityIndividuals(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws TypeException {
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, ObjectEntityInstance> ret = new HashMap<String, ObjectEntityInstance>();
		
		OWLClass objectEntityClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(OBJECT_ENTITY, prefixManager);
		
		ObjectEntity mebnEntity = null;
		for (OWLClassExpression subclassExpression : objectEntityClass.getSubClasses(ontology)) {
			OWLClass subClass = subclassExpression.asOWLClass();
			if (subClass == null) {
				Debug.println(this.getClass(), "Could not convert class expression to class: " + subclassExpression);
				continue;	// let's ignore it and keep going on.
			}
			mebnEntity = mebn.getObjectEntityContainer().getObjectEntityByName(this.extractName(ontology, subClass));
			for (OWLIndividual individual : subClass.getIndividuals(ontology)) {
				if (individual.isNamed()) {
					// creates a object entity instance and adds it into the mebn entity container
					try {
						String individualName = this.extractName(ontology, individual.asOWLNamedIndividual());
						ObjectEntityInstance addedInstance = mebnEntity.addInstance(individualName);
						mebn.getObjectEntityContainer().addEntityInstance(addedInstance);
						ret.put(individualName, addedInstance);
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
	 * in order may cause input nodes to fail, we must ajust or reorder them.
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
					new InternalErrorDialog(); 
				}
				else{
					try{
					   resident.addArgument(argumentOfPosition.getOVariable(), false);
					}
					catch(Exception e){
						new InternalErrorDialog(); 
						e.printStackTrace(); 
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
	private NodeFormulaTree buildFormulaTree(ContextNode contextNode){
		
		
		NodeFormulaTree nodeFormulaRoot; 
		NodeFormulaTree nodeFormulaChild; 
		
		nodeFormulaRoot = new NodeFormulaTree("formula", EnumType.FORMULA, 	EnumSubType.NOTHING, null);  
    	
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
			
			
			nodeFormulaRoot = new NodeFormulaTree(builtIn.getName(), type, subType, builtIn); 
		    nodeFormulaRoot.setMnemonic(builtIn.getMnemonic()); 
			
			List<Argument> argumentList = putArgumentListInOrder(contextNode.getArgumentList()); 
		  		    
		    for(Argument argument: argumentList){
		    	if(argument.getOVariable()!= null){
		    		OrdinaryVariable ov = argument.getOVariable(); 
		    		nodeFormulaChild = new NodeFormulaTree(ov.getName(), EnumType.OPERAND, EnumSubType.OVARIABLE, ov); 
		    		nodeFormulaRoot.addChild(nodeFormulaChild); 
		    	}
		    	else{
		    		if(argument.getArgumentTerm() != null){
		    			
		    			MultiEntityNode multiEntityNode = argument.getArgumentTerm(); 
		    			
		    			if(multiEntityNode instanceof ResidentNode){
		    				ResidentNodePointer residentNodePointer = new ResidentNodePointer((ResidentNode)multiEntityNode, contextNode); 
		    				nodeFormulaChild = new NodeFormulaTree(multiEntityNode.getName(), EnumType.OPERAND, EnumSubType.NODE, residentNodePointer); 
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
							nodeFormulaChild = new NodeFormulaTree(argument.getEntityTerm().getName(), EnumType.OPERAND, EnumSubType.ENTITY, argument.getEntityTerm());
							nodeFormulaRoot.addChild(nodeFormulaChild); 
						}
		    		}
		    	}
		    	
		    }
		    
		}
		else{
			if((obj instanceof ResidentNode)){
				ResidentNodePointer residentNodePointer = new ResidentNodePointer((ResidentNode)obj, contextNode); 
				nodeFormulaRoot = new NodeFormulaTree(((ResidentNode)obj).getName(), EnumType.OPERAND, EnumSubType.NODE, residentNodePointer); 
				
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
	 * Extracts the comments from a owl individual
	 * @param individualOne
	 * @param ontology : the ontology being manipulated
	 * @return a comment as a string or a null value if it is not found
	 */
	protected String getDescription(OWLOntology ontology, OWLIndividual individual) {
		// ignore individual if this is a nameless individual (which is not a proper member in PR-OWL ontology).
		if (!individual.isNamed()) {
			return null;
		}
		
		// obtains the descriptor of the comment annotation
		OWLAnnotationProperty commentProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_COMMENT.getIRI());
		
		// extracts comments
		Set<OWLAnnotation> comments = individual.asOWLNamedIndividual().getAnnotations(ontology, commentProperty);
	
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
		Debug.println(this.getClass(), "Comment loaded: " + comment + " for individual " + individual);
		return comment;
	}
	
	/**
	 * @return {@link #getDefaultPrefixManager(null)}
	 * @see #getDefaultPrefixManager(OWLOntology)
	 */
	protected PrefixManager getDefaultPrefixManager() {
		return this.getDefaultPrefixManager(null);
	}
	
	/**
	 * Obtains the default prefix manager, which will be used in order to extract classes by name/ID.
	 * If ontology == null, it uses {@link #getProwlOntologyNamespaceURI()} in order to create the prefix
	 * (thus, if ontology == null, it returns a PR-OWL ontology prefix).
	 * @param ontology : ontology being read.
	 * @return a prefix manager or null if it could not be created
	 */
	protected PrefixManager getDefaultPrefixManager(OWLOntology ontology) {
		
		// use PR-OWL prefix if no ontology was specified
		if (ontology == null) {
			// extract the PR-OWL ontology namespaces with '#'
			String defaultPrefix = this.getProwlOntologyNamespaceURI();
			if (defaultPrefix == null) {
				defaultPrefix = "";		// use empty prefix if none was specified
			}
			if (!defaultPrefix.endsWith("#")) {
				defaultPrefix += "#";
			}
			PrefixManager prefixManager = new DefaultPrefixManager(defaultPrefix);
			return prefixManager;
		} else {
			// read prefix from ontology
			try {
				String defaultPrefix = ontology.getOntologyID().getDefaultDocumentIRI().toString();
				if (!defaultPrefix.endsWith("#")) {
					defaultPrefix += "#";
				}
				PrefixManager prefixManager = new DefaultPrefixManager(defaultPrefix);
				return prefixManager;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		
		
		// could not extract prefixes at all...
		return null;
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
	 * {@link OWLAPICompatiblePROWL2IO} implements delegator design patter without directly extending LoaderPrOwlIO (because of incompatible interfaces).
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
	 * {@link OWLAPICompatiblePROWL2IO} implements delegator design patter without directly extending LoaderPrOwlIO (because of incompatible interfaces).
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
	 * @return the mapArgument
	 */
	protected Map<String, Argument> getMapArgument() {
		return mapArgument;
	}

	/**
	 * A mapping from argument name to argument.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
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
	 */
	protected Map<ContextNode, Object> getMapIsContextInstanceOf() {
		return mapIsContextInstanceOf;
	}

	/**
	 * A mapping from a context node's name to either an instance of {@link ResidentNode} or {@link BuiltInRV}.
	 * This map is useful if data must be reused throughout the protected load methods (e.g. {@link #loadMTheoryAndMFrags(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}
	 * and {@link #loadBuiltInRV(OWLOntologyManager, OWLOntology, MultiEntityBayesianNetwork)}).
	 * @param mapIsContextInstanceOf the mapIsContextInstanceOf to set
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
	 * @return the prowlOntologyNamespaceURI
	 */
	protected String getProwlOntologyNamespaceURI() {
		return prowlOntologyNamespaceURI;
	}

	/**
	 * This string identifies the URI of pr-owl elements (http://www.pr-owl.org/pr-owl.owl).
	 * @param prowlOntologyNamespaceURI the prowlOntologyNamespaceURI to set
	 */
	protected void setProwlOntologyNamespaceURI(String prowlOntologyNamespaceURI) {
		this.prowlOntologyNamespaceURI = prowlOntologyNamespaceURI;
	}
}
