/**
 * 
 */
package unbbayes.io.mebn.owlapi;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.PROWLModelUser;
import unbbayes.io.mebn.SaverPrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.ArgumentOVariableAlreadySetException;
import unbbayes.util.Debug;

/**
 * It implements PR-OWL2 support.
 * @author Shou Matsumoto
 *
 */
public class OWLAPICompatiblePROWL2IO extends OWLAPICompatiblePROWLIO {

	private Map<String, Argument> recursivelyAddedArgumentsOfMExpression;

	/**
	 * @deprecated
	 */
	public OWLAPICompatiblePROWL2IO() {
		super();
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
	}

	/**
	 * This is the constructor method to be used in order to create new instances of {@link OWLAPICompatiblePROWL2IO}
	 * @return
	 */
	public static MebnIO newInstance() {
		return new OWLAPICompatiblePROWL2IO();
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
		
		
		// Reset the non-PR-OWL classes extractor (this)
		this.resetNonPROWLClassExtractor();
		
		// update last owl reasoner
		if (reasoner != null) {
			this.setLastOWLReasoner(reasoner);
		}
		
		// this is a instance of MEBN to be filled. The name will be updated after loadMTheoryAndMFrags
		IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, mebn, ontology.getOntologyID().getOntologyIRI());
		
		// Start loading ontology. This is template method design pattern.

		// load MTheory. The nameToMFragMap maps a name to a MFrag.
		try {
			this.setMapNameToMFrag(this.loadMTheoryAndMFrags(this.getLastOWLOntology(), mebn));
		} catch (IOMebnException e) {
			// the ontology does not contain PR-OWL specific elements. Stop loading PR-OWL and return an empty mebn.
			e.printStackTrace();
			return;
		}
		
		try {
			// load object entities and fill the mapping of object entities
			this.setMapLabelToObjectEntity(this.loadObjectEntity(this.getLastOWLOntology(), mebn));
	
			// Meta Entities, categorical entities and boolean states are not loaded as PR-OWL specific classes anymore 
			// (because they became either non-PROWL2 classes or owl:datatypes), so we are commenting them out.
			
//			this.setMapNameToType(this.loadMetaEntitiesClasses(this.getLastOWLOntology(), mebn));
//			this.setMapCategoricalStates(this.loadCategoricalStateEntity(this.getLastOWLOntology(), mebn));
//			this.setMapBooleanStates(this.loadBooleanStateEntity(this.getLastOWLOntology(), mebn));

			// load content of MFrag (nodes, ordinary variables, etc...)
			this.setMapLoadedNodes(this.loadDomainMFragContents(this.getLastOWLOntology(), mebn));
			
			// load built in random variables. Reuse mapLoadedNodes.
			this.setMapBuiltInRV(this.loadBuiltInRV(this.getLastOWLOntology(), mebn));
			
			// load content of resident nodes
			this.setMapFilledResidentNodes(this.loadDomainResidentNode(this.getLastOWLOntology(), mebn));

			// load content of input nodes
//			this.setMapFilledInputNodes(this.loadGenerativeInputNode(this.getLastOWLOntology(), mebn));

			// load content of context nodes. The mapIsContextInstanceOf maps Context nodes to either BuiltInRV or ResidentNode. The mapArgument mapps a name to an argument
			this.setMapTopLevelContextNodes(this.loadContextNode( this.getLastOWLOntology(), mebn));
			
			
			// load content of ordinary variables
			this.setMapFilledOrdinaryVariables(this.loadOrdinaryVariable(this.getLastOWLOntology(), mebn));
			
			// load generic arguments relationships
			this.setMapFilledArguments(this.loadArgRelationship(this.getLastOWLOntology(), mebn));
			
			// load simple arguments
			this.setMapFilledSimpleArguments(this.loadSimpleArgRelationship(this.getLastOWLOntology(), mebn));
			
			// adjust the order of arguments (the appearance order of arguments may not be the correct order). 
			// TODO this seems to be a magic method and should be avoided. This is only used now because of how ancestor classes were implemented
			this.ajustArgumentOfNodes(mebn);
			
			// load the content of the formulas inside context nodes
			this.buildFormulaTrees(this.getMapTopLevelContextNodes(), mebn);
			
			// Load individuals of object entities (ObjectEntityInstances)
			this.setMapLoadedObjectEntityIndividuals(loadObjectEntityIndividuals(this.getLastOWLOntology(), mebn));
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to load ontology " + ontology, e);
		}
		
		// fill the storage implementor of MEBN (a reference to an object that loaded/saved the mebn last time)
		mebn.setStorageImplementor(OWLAPIStorageImplementorDecorator.newInstance(this.getLastOWLOntology()));
		
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
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
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
					ret.addAll(this.getLastOWLReasoner().getInstances(this.parseExpression(IPROWL2ModelUser.DOMAIN_MFRAG + " and inverse " + this.getHasMFragObjectProperty() + " value " + this.extractName(mTheoryObject)), false).getFlattened());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} 
		} else {
			throw new IllegalStateException("Reasoner is not initialized");
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
		OWLClass owlClassDomainMFrag = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAIN_MFRAG, prefixManager); 
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
				OWLObjectProperty objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasResidentNode", prefixManager); 

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
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasInputNode", prefixManager); 
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
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasContextNode", prefixManager); 
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
				objectProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasOrdinaryVariable", prefixManager); 
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
		
		// extract built in random variables (we assume they are only boolean random variables)
		OWLClass builtInOWLClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.BOOLEAN_RANDOM_VARIABLE, prefixManager); 
		if (!ontology.containsClassInSignature(builtInOWLClass.getIRI(),true)) {
			// use the old PR-OWL definition
			builtInOWLClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(BUILTIN_RV, prefixManager); 
		}
		
		// iterate on individuals
		for (OWLIndividual builtInRVIndividual : this.getOWLIndividuals(builtInOWLClass, ontology)){
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
				OWLObjectProperty isTypeOfMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isTypeOfMExpression", prefixManager); 
				for (OWLIndividual mExpressionIndividual : this.getObjectPropertyValues(builtInRVIndividual,isTypeOfMExpression, ontology)){
					// extract the node related to MExpression (-> isMExpressionOf)
					OWLObjectProperty isMExpressionOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("isMExpressionOf", prefixManager);
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


	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadDomainResidentNode(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, ResidentNode> loadDomainResidentNode(OWLOntology ontology, MultiEntityBayesianNetwork mebn)  throws IOMebnException{

		Map<String, ResidentNode> ret = new HashMap<String, ResidentNode>();
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		OWLClass owlClassDomainResidentNode = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.DOMAIN_RESIDENT, prefixManager); 
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
					throw new IOMebnException(this.getResource().getString("DomainResidentNotExistsInMTheory"), "Resident = " + residentNodeIndividual); 
				}
				ResidentNode domainResidentNode = (ResidentNode)mappedNode;
				
				Debug.println(this.getClass(), "Domain Resident loaded: " + residentNodeIndividual); 			
				
				domainResidentNode.setDescription(getDescription(ontology, residentNodeIndividual)); 
				
				// create the link from domain resident node to owl property (definesUncertaintyOf)
				this.loadLinkFromResidentNodeToOWLProperty(domainResidentNode, residentNodeIndividual, ontology);
				
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
				OWLObjectProperty hasMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasMExpression", prefixManager); 
				
				// there should be only 1 MExpression per node. Extract it
				Collection<OWLIndividual> mExpressions =  this.getObjectPropertyValues(residentNodeIndividual,hasMExpression, ontology);
				if (mExpressions.size() > 0 ) {
					OWLIndividual mExpressionIndividual = mExpressions.iterator().next();
					// Node -> hasMExpression exactly 1 MExpression
					// extract arguments from mExpression using hasArgument property
					OWLObjectProperty hasArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasArgument", prefixManager);
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
						
						// force all nodes to have Absurd
						domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());
						
						
						// extract BOOLEAN_RANDOM_VARIABLE so that we can test if this is boolean
						// This code was added because reasoners like Hermit could not retrieve OWL literals from individuals by solving class axioms
						// (it requires that an individual to be explicitly linked to such OWL literal, instead of solving data property axioms in its types)
						OWLClass booleanRandomVariable = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.BOOLEAN_RANDOM_VARIABLE, prefixManager);
						
						// special case: if BOOLEAN_RANDOM_VARIABLE, then force boolean values for node
						if (this.getLastOWLReasoner().getTypes(randomVariableIndividual.asOWLNamedIndividual(), false).containsEntity(booleanRandomVariable)) {
							try {
								Debug.println(this.getClass(), randomVariableIndividual + " is " + IPROWL2ModelUser.BOOLEAN_RANDOM_VARIABLE + " and will be forced as boolean RV.");
							} catch (Throwable t) {
								t.printStackTrace();
							}
							// try boolean datatype
							domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());  
							domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());   
							// the absurd was added previously
							// TODO stop using type of states because this is mutable
							domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
							// do not solve the possible values of this random variable anymore, because we forced it to be boolean
							continue;
						} 
						
						// extract hasPossibleValues data property
						OWLDataProperty hasPossibleValues = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("hasPossibleValues", prefixManager); 			
						
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
										domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());  
										domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());   
										// the absurd was added previously
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
				
				OWLObjectProperty hasProbDist = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasProbabilityDistribution", prefixManager);
				OWLDataProperty hasDeclaration = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("hasDeclaration", prefixManager); 
				for (OWLIndividual element : this.getObjectPropertyValues(residentNodeIndividual,hasProbDist, ontology)) {
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
					
					// Probably we do not need to trace where the CPT is declared in the ontology...
//					try {
//						IRIAwareMultiEntityBayesianNetwork.addIRIToMEBN(mebn, cpt, element.asOWLNamedIndividual().getIRI());
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
				}
				
				
				// fill return (extract name again, because the extracted name may be different from the node's name)
				ret.put(this.extractName(ontology, residentNodeIndividual.asOWLNamedIndividual()), domainResidentNode);
				
			}
		}
		return ret;
	}
	

	/*
	 * (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadLinkFromResidentNodeToOWLProperty(unbbayes.prs.mebn.ResidentNode, org.semanticweb.owlapi.model.OWLObject, org.semanticweb.owlapi.model.OWLOntology)
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
		
		// this method only works if a reasoner is available
		if (this.getLastOWLReasoner() == null) {
			throw new NullPointerException("Reasoner == null");
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
		
		// extract default prefix (PR-OWL2)
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		// extract MExpression property
		OWLObjectProperty hasMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasMExpression", prefixManager);
		
		// extract MExpression
		for (OWLNamedIndividual mExpression : this.getLastOWLReasoner().getObjectPropertyValues(((OWLIndividual)owlObject).asOWLNamedIndividual(), hasMExpression).getFlattened()) {
			// extract the object property for type of MExpression 
			OWLObjectProperty typeOfMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("typeOfMExpression", prefixManager);
			
			// extract the type of the MExpression (this should be a random variable)
			for (OWLNamedIndividual randomVariable : this.getLastOWLReasoner().getObjectPropertyValues(mExpression, typeOfMExpression).getFlattened()) {
				// TODO maybe we should check if this is really a randomVariable...
				
				// extract definesUncertaintyOfIRI property
				OWLDataProperty definesUncertaintyOf = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("definesUncertaintyOf", prefixManager);
				
				// extract value from inferred values
				Collection<OWLLiteral> values = this.getLastOWLReasoner().getDataPropertyValues(randomVariable, definesUncertaintyOf);
				OWLLiteral value = null;
				if (values != null && !values.isEmpty()) {
					// use only the first value
					value = values.iterator().next();
				}
				
				// add value to mebn
				if (value != null) {
					// extract IRI
					String iriString = value.getLiteral();
					if (iriString != null && (iriString.length() > 0)) {
						IRIAwareMultiEntityBayesianNetwork.addDefineUncertaintyToMEBN(mebn, domainResidentNode, IRI.create(iriString));
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
		
		OWLClass owlClassOrdinaryVariable = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.ORDINARY_VARIABLE, prefixManager); 
		if (!ontology.containsClassInSignature(owlClassOrdinaryVariable.getIRI(), true)) {
			// use the old definition
			owlClassOrdinaryVariable = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(PROWLModelUser.ORDINARY_VARIABLE, prefixManager); 
		}
		
		if (owlClassOrdinaryVariable != null) {
			for (OWLIndividual ordinaryVariableIndividual : owlClassOrdinaryVariable.getIndividuals(ontology) ){
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
				
				OWLDataProperty isSubstitutedBy = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("isSubstitutedBy", prefixManager); 			
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
	 * @see IPROWL2ModelUser#ORDINARYVARIABLE_ARGUMENT 
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#loadSimpleArgRelationship(org.semanticweb.owlapi.model.OWLOntology, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
	 */
	protected Map<String, Argument> loadSimpleArgRelationship(OWLOntology ontology, MultiEntityBayesianNetwork mebn) throws IOMebnException{
		
		PrefixManager prefixManager = this.getDefaultPrefixManager();
		
		Map<String, Argument> ret = new HashMap<String, Argument>();
		
		OWLClass owlClassOrdinaryVariableArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.ORDINARYVARIABLE_ARGUMENT, prefixManager); 
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
			
			// mapArgument is filled in methods that load nodes
			Argument argument = this.getMapArgument().get(ordinaryVariableArgumentName); 
			if (argument == null){
				// there may be unknown arguments (e.g. arguments of finding nodes or arguments of random variables that has no correspondent Resident Node)
				System.err.println(this.getResource().getString("ArgumentNotFound") +  ". Argument = " + ordinaryVariableArgumentIndividual); 
				continue;
			}
			Debug.println(this.getClass(), "SimpleArgRelationship loaded: " + ordinaryVariableArgumentIndividual); 
			
			/* -> typeOfArgument  */
			
			OWLObjectProperty typeOfArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("typeOfArgument", prefixManager); 			
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
						throw new IOMebnException(this.getResource().getString("ArgumentTermError"), "Ordinary Variable = " + ordinaryVariableIndividual); 	
					}
					
					try{
						argument.setOVariable(oVariable); 
						argument.setType(Argument.ORDINARY_VARIABLE); 
					} catch(Exception e){
						throw new IOMebnException(this.getResource().getString("ArgumentTermError"),  "Ordinary Variable = " + ordinaryVariableIndividual); 	
					}
					Debug.println(this.getClass(), "-> " + ordinaryVariableArgumentIndividual + ": " + typeOfArgument + " = " + ordinaryVariableIndividual);			
				}
			}
			
			/* -> hasArgumentNumber */
			
			OWLDataProperty hasArgNumber = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("hasArgumentNumber", prefixManager);
	        Set<OWLLiteral> owlLiterals = ordinaryVariableArgumentIndividual.getDataPropertyValues(hasArgNumber, ontology);
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
		
		OWLClass owlClassContextNode = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.CONTEXT_NODE, prefixManager); 
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
				OWLObjectProperty hasMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasMExpression", prefixManager);
				
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
				OWLObjectProperty hasArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasArgument", prefixManager); 
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
					if(this.getMapBuiltInRV().containsKey(this.extractName(ontology, randomVariableIndividual.asOWLNamedIndividual()))){
						this.getMapIsContextInstanceOf().put(contextNode, this.getMapBuiltInRV().get(this.extractName(ontology, randomVariableIndividual.asOWLNamedIndividual()))); 
					} else {
						INode auxNode = this.getMapLoadedNodes().get(this.extractName(ontology, randomVariableIndividual.asOWLNamedIndividual()));
						if(auxNode != null && (auxNode instanceof ResidentNode)){
							this.getMapIsContextInstanceOf().put(contextNode, auxNode); 
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
		
		OWLClass owlClassMExpressionArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.MEXPRESSION_ARGUMENT, prefixManager); 
		
		for (OWLIndividual mExpressionArgumentIndividual : owlClassMExpressionArgument.getIndividuals(ontology) ){	
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
			OWLObjectProperty typeOfArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("typeOfArgument", prefixManager); 			
			
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
						throw new IllegalArgumentException("typeOfArgument = " + typeOf1stLevelArgument,e);
					} catch (ArgumentNodeAlreadySetException e) {
						// Perform Exception translation in order to match signature
						throw new IllegalArgumentException("typeOfArgument = " + typeOf1stLevelArgument,e);
					}
				}
			}
			
			/* has Arg Number */
			OWLDataProperty hasArgNumber = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty("hasArgumentNumber", prefixManager);
			Set<OWLLiteral> literals = mExpressionArgumentIndividual.getDataPropertyValues(hasArgNumber, ontology);
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
		} else if (this.getLastOWLReasoner().getTypes(typeOfArgument.asOWLNamedIndividual(), false).containsEntity(
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IPROWL2ModelUser.MEXPRESSION, prefixManager))) {
			// this is an MExpression. Create arguments recursively
			// assure this is an argument for a context node
			if (argument.getMultiEntityNode() != null 
					&& (argument.getMultiEntityNode() instanceof ContextNode)) {
				
				// extract typeOfMExpression so that we can use it to fill inner node/argument (i.e. the argumentTerm of argument)
				OWLObjectProperty typeOfMExpression = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("typeOfMExpression", prefixManager);
				
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
						
						// extract the resident node from a random variable 
						// RandomVariable -[isTypeOfMExpression]-> MExpression -[isMExpressionOf]-> ResidentNode
						// the query is DomainResidentNode that hasMExpression some (typeOfMExpression value <typeOfMExpressionIndividual>)
						Collection<OWLIndividual> residentNodeIndividuals = this.getOWLIndividuals(this.parseExpression("DomainResidentNode that hasMExpression some (typeOfMExpression value " + nameOfType + ")"), ontology);
						if (!residentNodeIndividuals.isEmpty()) {
							// use the first resident node
							INode auxNode = this.getMapLoadedNodes().get(this.extractName(ontology, residentNodeIndividuals.iterator().next().asOWLNamedIndividual()));
							if(auxNode != null && (auxNode instanceof ResidentNode)){
								this.getMapIsContextInstanceOf().put(innerNode, auxNode); 
								argument.setArgumentTerm((ResidentNode)auxNode); 
								argument.setType(Argument.RESIDENT_NODE);  
							}
						}
						
					}
					
					// solve arguments recursively
					/* mExpression -> hasArgument */
					OWLObjectProperty hasArgument = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("hasArgument", prefixManager); 
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
						OWLObjectProperty typeOfArgumentProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty("typeOfArgument", prefixManager);
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
		
		
		return ret;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#getDefaultPrefixManager()
	 */
	@Override
	protected PrefixManager getDefaultPrefixManager() {
		if (this.getProwlModelUserDelegator() != null) {
			// this is expected to return PR-OWL 2 prefix
			return this.getProwlModelUserDelegator().getOntologyPrefixManager(null);
		}
		return super.getDefaultPrefixManager();
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
	
	
}
