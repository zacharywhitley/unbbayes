/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.io.mebn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IMultiEntityNode;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
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
import unbbayes.util.Debug;

import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/**
 * Save the MEBN structure in a file pr-owl. 
 * 
 * Version Pr-OWL: 1.05 (octuber, 2007)
 * (http://www.pr-owl.org/pr-owl.owl) 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.6 10/28/2007
 * 
 * 
 * @author Shou Matsumoto
 * @version 07/17/2010 : added prefixes to MFrag and resident names.
 *
 */
public class SaverPrOwlIO extends PROWLModelUser{

	private HashMap<String, OWLIndividual> mapBuiltInOwlIndividual = new HashMap<String, OWLIndividual>();
	private HashMap<String, OWLIndividual> mapMetaEntity = new HashMap<String,OWLIndividual>();
	private HashMap<Entity, OWLIndividual> mapCategoricalStates = new HashMap<Entity, OWLIndividual>();
	private HashMap<ObjectEntity, OWLNamedClass> mapObjectEntityClasses = new HashMap<ObjectEntity, OWLNamedClass>(); 
	private HashMap<BooleanStateEntity, OWLIndividual> mapBooleanStatesEntity = new HashMap<BooleanStateEntity, OWLIndividual>(); 
	
	private HashMap<MFrag, OWLIndividual> mapMFrag = new HashMap<MFrag, OWLIndividual>(); 
	private HashMap<OrdinaryVariable, OWLIndividual> mapOrdinaryVariable = new HashMap<OrdinaryVariable, OWLIndividual>();
	private HashMap<ContextNode, OWLIndividual> mapContext = new HashMap<ContextNode, OWLIndividual>();
	private HashMap<InputNode, OWLIndividual> mapGenerativeInput = new HashMap<InputNode, OWLIndividual>();
	private HashMap<ResidentNode, OWLIndividual> mapDomainResident = new HashMap<ResidentNode, OWLIndividual>(); 
	
	private ArrayList<ContextNode> auxContextNodeList = new ArrayList<ContextNode>();  
	
	private JenaOWLModel owlModel = null;	
	
	/* Constants */
	public static final String SCOPE_SEPARATOR = ".";
	public static final String NUMBER_SEPARATOR = "_"; 
	public static final String META_ENTITY_SUFIX = "_Label"; 
	public static final String DECLARATIVE_DISTRO_SUFIX = "_Table"; 
	public static final String INNER_SUFIX = "_Inner";
	
	/** This is a prefix to be added to MFrag's names to avoid conflicts with other frames. */
	public static final String MFRAG_NAME_PREFIX = DOMAIN_MFRAG + ".";
	/** This is a prefix to be added to Resident node's names to avoid conflicts with other frames. */
	public static final String RESIDENT_NAME_PREFIX = DOMAIN_RESIDENT + ".";
	
	private MultiEntityBayesianNetwork mebn; 
	
	/** Load resource file from this package */
	private ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.io.mebn.resources.IoMebnResources.class.getName());
	

		

	/**
	 * Save the mebn structure in an file pr-owl using the last OWLModel used by this object. If
	 * this is the first time (no previously used OWLModel), then it automatically creates a new OWLModel;
	 * @param file: where ontology will be saved
	 * @param mebn: the mebn structure
	 */
	public void saveMebn(File file, MultiEntityBayesianNetwork _mebn) throws IOException, IOMebnException{
		
		
		
		//Debug.setDebug(true);
	    mebn = _mebn; 	
	    if (this.owlModel == null) {
	    	this.owlModel = ProtegeOWL.createJenaOWLModel();
			//this.setOWLModelToUse(this.owlModel);   // currently redundant, but extended classes may be not
			loadPrOwlModel((JenaOWLModel)owlModel); 
			Debug.println("-> Model pr-owl load sucess ");	
			// update owl model
			// the below code was moved to PrOwlIO
//			mebn.setStorageImplementor(new MEBNStorageImplementorDecorator(owlModel));
	    } 
				
		/* Definitions */
	
		saveMetaEntities();
		saveObjectEntitiesClasses(); 
		saveBooleanRVStates(); 
		saveCategoricalStates(); 
		saveBuiltInRV();  
		
		Debug.println("-> Definitions load sucess ");
		
		/* MTheory */
		
		saveMTheory(); 
		Debug.println("-> MTheory load sucess ");
		saveDomainResidentNodes(); 
		Debug.println("-> Domain Resident Nodes load sucess ");		
		saveContextNode(); 
		Debug.println("-> Context Node load sucess ");		
		saveGenerativeInputNode(); 
		Debug.println("-> Generative Input Node load sucess ");		
		saveEntityIndividuals();
		Debug.println("-> Entity Individuals load sucess ");		
		
		clearAuxiliaryLists(); 
		
		/* saving */
		
		Collection<String> errors = new ArrayList<String>();
		owlModel.save(file.toURI(), FileUtils.langXMLAbbrev, errors);
		
		Debug.println("File saved with " + errors.size() + " errors.");
		for(String error: errors){
			Debug.println(" > " + error); 
		}
		Debug.println("\n"); 
		//Debug.setDebug(false);
	}	
	
	
	/**
	 * Save the mebn structure in an file pr-owl. 
	 * This is equivalent to calling setOWLModelToUse -> saveMebn.
	 * @param nameFile: name of the file pr-owl where the mebn structure will be save
	 * @param mebn: the mebn structure
	 * @param owlmodel: the pr-owl model (owl model w/ pr-owl definitions loaded) 
	 * to be updated and saved. If null, then a new instance will
	 * be created and properly loaded.
	 */
	public void saveMebn(File file, MultiEntityBayesianNetwork _mebn, OWLModel owlmodel) throws IOException, IOMebnException{
		this.setOWLModelToUse(owlmodel);
		this.saveMebn(file, _mebn);
		
	}
	
	
	
	/**
	 * Maps
	 * - mapMetaEntity
	 * 
	 * Instances
	 * - MetaEntity
	 * 
	 * Properties
	 * - hasUID
	 * - hasType (isTypeOf)
	 */
	protected void saveMetaEntities(){

		ArrayList<String> metaEntitiesDefault = new ArrayList<String>(); 
		OWLNamedClass metaEntityClass = owlModel.getOWLNamedClass(META_ENTITY); 
		
		
		/*----- First: MetaEntities of the original Pr-OWL upper-ontology -----*/
				
		Collection instances = metaEntityClass.getInstances(false); 
		
		OWLIndividual rootLabel = null;	// stores "TypeLabel" individual, which is root
		
		for(Object instance: instances){
			
			OWLIndividual metaEntityInstance = (OWLIndividual) instance;  
			mapMetaEntity.put(metaEntityInstance.getBrowserText(), metaEntityInstance); 
			metaEntitiesDefault.add(metaEntityInstance.getBrowserText());
			
			// Extract the extreme super-type label (the root type label)
			if (metaEntityInstance.getName().equals(TYPE_LABEL)) {
				rootLabel = metaEntityInstance;
			}
		}
		
		if (rootLabel == null){
			Debug.println("Error: the meta entity TypeLabel don't exists in the model");
			//TODO find a way to report the user when an error were found in the model file.
		}
		
		/*----- Second: MetaEntities create by the user -----*/
		
		List<String> listMetaEntities = mebn.getTypeContainer().getTypesNames(); 
		
		for(String state: listMetaEntities){
			
			if(mebn.getObjectEntityContainer().getRootObjectEntity().getType().getName().equals(state)) {
				continue;
			}

			if (!metaEntitiesDefault.contains(state)){
				   OWLIndividual stateIndividual = metaEntityClass.createOWLIndividual(state); 
				   
				   // hasType
				   stateIndividual.addPropertyValue(owlModel.getOWLObjectProperty("hasType"),rootLabel);
				   
				   //hasUID
				   stateIndividual.setPropertyValue(owlModel.getOWLDatatypeProperty("hasUID"), "!" + state); 
				   
				   mapMetaEntity.put(state, stateIndividual); 	
			}
		}	
	}
	
	/**
	 * Maps
	 * - mapBooleanStatesEntity
	 */
	protected void saveBooleanRVStates(){
		
		OWLNamedClass booleanRVStates = owlModel.getOWLNamedClass(BOOLEAN_STATE); 
	
		Collection instances = booleanRVStates.getInstances(false); 
		
		for(Object instance : instances){
			OWLIndividual stateOwl = (OWLIndividual)instance; 
			if(stateOwl.getBrowserText().equals("true")){
				mapBooleanStatesEntity.put(mebn.getBooleanStatesEntityContainer().getTrueStateEntity(), stateOwl); 
			}
			else{
				if(stateOwl.getBrowserText().equals("false")){
					mapBooleanStatesEntity.put(mebn.getBooleanStatesEntityContainer().getFalseStateEntity(), stateOwl); 
				}
				else{
					if(stateOwl.getBrowserText().equals("absurd")){
						mapBooleanStatesEntity.put(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity(), stateOwl); 
					}
				}
			}
		}
	}
	
	/**
	 * Maps
	 * - mapCategoricalStates
	 *
	 * Instances
	 * - CATEGORICAL_STATES
	 *
	 * Properties
	 * - hasType
	 * 
	 */
	protected void saveCategoricalStates(){
		OWLNamedClass categoricalStateClass = owlModel.getOWLNamedClass(CATEGORICAL_STATE); 
		
		OWLObjectProperty hasType = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasType"); 	
		OWLIndividual categoryLabel = mapMetaEntity.get(CATEGORY_LABEL); 
		
		for(CategoricalStateEntity entity: mebn.getCategoricalStatesEntityContainer().getListEntity()){
			if(entity.getName().equals("absurd")) continue; 
			// check if individual exists
			OWLIndividual categoricalStateIndividual = owlModel.getOWLIndividual(entity.getName());
			if (categoricalStateIndividual == null) {
				// if this is a new individual,　create it
				categoricalStateIndividual = categoricalStateClass.createOWLIndividual(entity.getName()); 
			}
			categoricalStateIndividual.addPropertyValue(hasType, categoryLabel);
			mapCategoricalStates.put(entity, categoricalStateIndividual); 
		}
	}
	
	/**
	 * Maps
	 * - builtInRVMap
	 * 
	 * Instances
	 * - BuiltInRV
	 * 
	 * Properties
	 * - 
	 */
	protected void saveBuiltInRV(){

		OWLNamedClass builtInPr = owlModel.getOWLNamedClass(BUILTIN_RV); 
		
		Collection instances = builtInPr.getInstances(false); 
		
		for (OWLIndividual individualOne: (Collection<OWLIndividual>)instances){
			mapBuiltInOwlIndividual.put(individualOne.getBrowserText(), individualOne);
		}
	}
	
	/**
	 * Maps
	 * - mapObjectEntityClasses
	 * 
	 * Classes
	 * - ObjectEntity
	 * 		
	 */
	// Using List implementation in ObjectEntityContainer
//	protected void saveObjectEntitiesClasses(){
//		
//		//OWLNamedClass entityClass = owlModel.getOWLNamedClass("ObjectEntity"); 
//		OWLNamedClass entityClass = owlModel.getOWLNamedClass(OBJECT_ENTITY); 
//		
//		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
//			// check if the entity class exists
//			OWLNamedClass newEntityClass = owlModel.getOWLNamedClass(entity.getName());
//			if (newEntityClass == null) {
//				// if it is a new entity class, create it
//				newEntityClass = owlModel.createOWLNamedSubclass(entity.getName(), entityClass); 	
//			}
//			mapObjectEntityClasses.put(entity, newEntityClass); 
//		}
//	}
	
	// Using Tree implementation in ObjectEntityContainer
	private void saveObjectEntityClass(OWLNamedClass entityClass, DefaultMutableTreeNode entityTreeNode){

		DefaultMutableTreeNode childEntityTreeNode;
		ObjectEntity childEntity;

		for (int i = entityTreeNode.getChildCount() ; i > 0; i--) {
			
			childEntityTreeNode = (DefaultMutableTreeNode) entityTreeNode.getChildAt(i - 1);
			childEntity = (ObjectEntity) childEntityTreeNode.getUserObject();
			
			// check if the entity class exists
			OWLNamedClass childEntityClass = owlModel.getOWLNamedClass(childEntity.getName());
			if (childEntityClass == null) {
				// if it is a new entity class, create it
				childEntityClass = owlModel.createOWLNamedSubclass(childEntity.getName(), entityClass); 	
			} else {
				childEntityClass.addSuperclass(entityClass);
			}
		
			mapObjectEntityClasses.put(childEntity, childEntityClass);
			
			saveObjectEntityClass(childEntityClass,childEntityTreeNode);
		}
	}
	
	// Using Tree implementation in ObjectEntityContainer
	protected void saveObjectEntitiesClasses(){
		
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) mebn.getObjectEntityContainer().getEntityTreeModel().getRoot();
		
		// Check if needs any modification in the getOWLNamedClass parameter.
		saveObjectEntityClass(owlModel.getOWLNamedClass(OBJECT_ENTITY), root);
	}

	
	/**
	 * Maps
	 * - mapMFrag
	 * - mapDomainResident
	 * - residentNodeListGeral
	 * - nodeMap
	 * - inputNodeListGeral
	 * - mapGenerativeInput
	 * - contextListGeral
	 * - mapContext
	 * - mapOrdinaryVariable
	 * 
	 * Instances
	 * - MTheory
	 * - Domain_MFrag
	 * - Domain_Res
	 * - Generative_input
	 * - OVariable
	 * 
	 * Properties
	 * - hasMFrag
	 * - hasResidentNode
	 * - hasInputNode
	 * - hasContextNode
	 * - hasNode (indirect)
	 * - hasOVariable
	 * - isSubsBy
	 * x hasExemplar
	 */
    protected void saveMTheory(){

		OWLNamedClass mTheoryClass = owlModel.getOWLNamedClass(MTHEORY); 
		// check if individual exists
		OWLIndividual mTheoryIndividual = owlModel.getOWLIndividual(mebn.getName());
		if (mTheoryIndividual == null) {
			// if this individual is new, create it
			mTheoryIndividual = mTheoryClass.createOWLIndividual(mebn.getName()); 
		}
		Debug.println("MTheory = " + mebn.getName());
		
		if(mebn.getDescription() != null){
			mTheoryIndividual.addComment(mebn.getDescription()); 
		}
		
		/* hasMFrag */
		
		OWLObjectProperty hasMFragProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasMFrag"); 	
		List<MFrag> listDomainMFrag = mebn.getDomainMFragList(); 
		
		for(MFrag domainMFrag: listDomainMFrag){
			OWLNamedClass domainMFragClass = owlModel.getOWLNamedClass(DOMAIN_MFRAG); 
			Debug.println("Domain_MFrag = " + domainMFrag.getName());
			// check if individual exists.
			OWLIndividual domainMFragIndividual = owlModel.getOWLIndividual(this.MFRAG_NAME_PREFIX + domainMFrag.getName());
			if (domainMFragIndividual == null) {
				// if new, create it
				domainMFragIndividual = domainMFragClass.createOWLIndividual(this.MFRAG_NAME_PREFIX + domainMFrag.getName());
			}
			mapMFrag.put(domainMFrag, domainMFragIndividual); 
			mTheoryIndividual.addPropertyValue(hasMFragProperty, domainMFragIndividual); 
			
			if(domainMFrag.getDescription()!=null){
				domainMFragIndividual.addComment(domainMFrag.getDescription()); 
			}
			
			/* hasResidentNode */
			OWLObjectProperty hasResidentNodeProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasResidentNode"); 	
			OWLNamedClass domainResClass = owlModel.getOWLNamedClass(DOMAIN_RESIDENT); 
			for(ResidentNode residentNode: domainMFrag.getResidentNodeList()){
				Debug.println("Domain_Res = " + residentNode.getName());	
				// check if individual exists
				OWLIndividual domainResIndividual = owlModel.getOWLIndividual(this.RESIDENT_NAME_PREFIX + residentNode.getName());
				if (domainResIndividual == null) {
					// if new, create it
					domainResIndividual = domainResClass.createOWLIndividual(this.RESIDENT_NAME_PREFIX + residentNode.getName());
				}
				domainMFragIndividual.addPropertyValue(hasResidentNodeProperty, domainResIndividual); 	
				mapDomainResident.put(residentNode, domainResIndividual); 
				
			}	
			
			/* hasInputNode */
			OWLObjectProperty hasInputNodeProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputNode"); 	
			OWLNamedClass generativeInputClass = owlModel.getOWLNamedClass(GENERATIVE_INPUT); 
			for(InputNode inputNode: domainMFrag.getInputNodeList()){
				Debug.println("Generative_input = " + inputNode.getName());
				// check if individuals exists
				OWLIndividual generativeInputIndividual = owlModel.getOWLIndividual(inputNode.getName());
				if (generativeInputIndividual == null) {
					// if new, create it
					generativeInputIndividual = generativeInputClass.createOWLIndividual(inputNode.getName());
				}
				domainMFragIndividual.addPropertyValue(hasInputNodeProperty, generativeInputIndividual); 		
				mapGenerativeInput.put(inputNode, generativeInputIndividual); 		
			}				
			
			/* hasContextNode */
			OWLObjectProperty hasContextNodeProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasContextNode"); 	
			OWLNamedClass contextClass = owlModel.getOWLNamedClass(CONTEXT_NODE); 
			for(ContextNode contextNode: domainMFrag.getContextNodeList()){
				// check if individuals exist
				OWLIndividual contextIndividual = owlModel.getOWLIndividual(contextNode.getName());
				if (contextIndividual == null) {
					// if new, create it
					contextIndividual = contextClass.createOWLIndividual(contextNode.getName());
				}
				domainMFragIndividual.addPropertyValue(hasContextNodeProperty, contextIndividual); 									
				mapContext.put(contextNode, contextIndividual); 	
			}				
			
			/* hasOVariable */
			OWLObjectProperty hasOVariableProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasOVariable"); 	
		    OWLNamedClass oVariableClass = owlModel.getOWLNamedClass(ORDINARY_VARIABLE); 
			OWLObjectProperty isSubsByProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isSubsBy"); 	
			
			for(OrdinaryVariable oVariable: domainMFrag.getOrdinaryVariableList()){
				// Set variable name as "MFragName.OVName"
				OWLIndividual oVariableIndividual = owlModel.getOWLIndividual(
						  oVariable.getMFrag().getName() + SCOPE_SEPARATOR
							+ oVariable.getName() );
				if (oVariableIndividual == null) {
					oVariableIndividual = oVariableClass.createOWLIndividual(
								  oVariable.getMFrag().getName() + SCOPE_SEPARATOR
								+ oVariable.getName() );
				}
				domainMFragIndividual.addPropertyValue(hasOVariableProperty, oVariableIndividual); 		
				
				if (oVariable.getValueType() != null){
					oVariableIndividual.addPropertyValue(isSubsByProperty, mapMetaEntity.get(oVariable.getValueType().getName())); 
				}
				
				if(oVariable.getDescription() != null){
					oVariableIndividual.addComment(oVariable.getDescription()); 	
				}
				
				mapOrdinaryVariable.put(oVariable, oVariableIndividual); 				
			}				
		}    	
    }
	
	/**
	 * Instances
	 * - DeclarativeDist
	 * 
	 * Properties
	 * - hasParent
	 * - hasInnerTerm
	 * - hasInputInstance
	 * - hasProbDist
	 * - hasDeclaration
	 */
    protected void saveDomainResidentNodes(){
    	
    	for(MFrag mfrag : mebn.getDomainMFragList()){
    		for (ResidentNode residentNode: mfrag.getResidentNodeList()){  
    			OWLIndividual domainResIndividual = mapDomainResident.get(residentNode);	
    			
    			/* has Argument */
    			Debug.println("Verifying arguments");
    			List<OrdinaryVariable> ordVariableList = residentNode.getOrdinaryVariableList(); 
    			int argumentNumber = 1; 
    			for(OrdinaryVariable argument: ordVariableList){
    				saveSimpleArgRelationship(argument, domainResIndividual, residentNode.getName(), argumentNumber);
    				argumentNumber++; 
    			}			
    			
    			/* has Parent */
    			Debug.println("Verifying parents");
    			OWLObjectProperty hasParentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasParent"); 				
    			
    			for(ResidentNode residentNodeFather: residentNode.getResidentNodeFatherList()){
    				OWLIndividual residentNodeFatherIndividual = mapDomainResident.get(residentNodeFather); 
    				domainResIndividual.addPropertyValue(hasParentProperty, residentNodeFatherIndividual);
    			}
    			
    			for(InputNode inputNodeFather: residentNode.getParentInputNodesList()){
    				OWLIndividual inputNodeFatherIndividual = mapGenerativeInput.get(inputNodeFather); 
    				domainResIndividual.addPropertyValue(hasParentProperty, inputNodeFatherIndividual);
    			}		
    			
    			/* has possible values */
    			Debug.println("Verifying possible values");
    			saveResidentPossibleValues(domainResIndividual, residentNode); 	        
    			
    			/* has Input Instance */
    			Debug.println("Verifying input instances");
    			OWLObjectProperty hasInputInstanceProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputInstance"); 	
    			for(InputNode inputInstance: residentNode.getInputInstanceFromList()){
    				OWLIndividual inputInstanceIndividual = mapGenerativeInput.get(inputInstance);
    				if (inputInstanceIndividual != null) {
    					domainResIndividual.addPropertyValue(hasInputInstanceProperty, inputInstanceIndividual);
    				}
    			}	
    			
    			/* hasProbDist */
    			Debug.println("Verifying probability distros");
    			OWLObjectProperty hasProbDist = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasProbDist");
    			OWLNamedClass declarativeDist = owlModel.getOWLNamedClass(DECLARATIVE_PROBABILITY_DISTRIBUTION);
    			// check if individual exists
    			OWLIndividual declarativeDistThisNode = owlModel.getOWLIndividual(residentNode.getName() + DECLARATIVE_DISTRO_SUFIX);
				if (declarativeDistThisNode == null) {
					// if new, create it
					declarativeDistThisNode = declarativeDist.createOWLIndividual(residentNode.getName() + DECLARATIVE_DISTRO_SUFIX); 
				}
    			OWLDatatypeProperty hasDeclaration = owlModel.getOWLDatatypeProperty("hasDeclaration"); 
    			if(residentNode.getTableFunction() != null){
    				declarativeDistThisNode.addPropertyValue(hasDeclaration, residentNode.getTableFunction()); 
    				domainResIndividual.addPropertyValue(hasProbDist, declarativeDistThisNode); 
    			}
    			
    			if(residentNode.getDescription() != null){
    			   domainResIndividual.addComment(residentNode.getDescription()); 
    			}
    		} 	
    	} 
    }

	/**
	 * Properties
	 * - hasPossibleValues
	 * - isGloballyExclusive
	 *
	 * @param residentNodeIndividual Individual that is the node in the PowerLoom structure. 
	 * @param node Resident Node of the MEBN structure
	 */
	protected void saveResidentPossibleValues(OWLIndividual residentNodeIndividual, IResidentNode node){

		/* categoricalRVStates */
		
		OWLObjectProperty hasPossibleValues = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 	
		OWLObjectProperty isGloballyExclusive = (OWLObjectProperty)owlModel.getOWLObjectProperty("isGloballyExclusive"); 	
		
		for(StateLink stateLink: node.getPossibleValueLinkList()){
			
			Entity state = stateLink.getState(); 
			if(state instanceof CategoricalStateEntity){
				OWLIndividual owlState = mapCategoricalStates.get(state); 
				residentNodeIndividual.addPropertyValue(hasPossibleValues, owlState);
				if(stateLink.isGloballyExclusive()){
					owlState.addPropertyValue(isGloballyExclusive, residentNodeIndividual); 
				}
			}
			else{
				if(state instanceof BooleanStateEntity){
					OWLIndividual owlState = mapBooleanStatesEntity.get(state); 
					residentNodeIndividual.addPropertyValue(hasPossibleValues, owlState); 
					if(stateLink.isGloballyExclusive()){
						owlState.addPropertyValue(isGloballyExclusive, residentNodeIndividual); 
					}
				}
				else{
					if(state instanceof ObjectEntity){
						OWLIndividual owlState =  mapMetaEntity.get(((ObjectEntity)state).getType().getName()); 
						residentNodeIndividual.addPropertyValue(hasPossibleValues, owlState);
						if(stateLink.isGloballyExclusive()){
							owlState.addPropertyValue(isGloballyExclusive, residentNodeIndividual); 
						}
					}else{
						Debug.print("Error: Invalid State - " + state.getName()); 
					}
				}/* else */
			}/* else */		
		}
	}
    
    /**
     * Save one simple argument relationship of a node. A simple argument is only
     * fill with a ordinary variable. 
     * 
	 * Instances
	 * - SimpleArgRelationship
	 * 
	 * Properties
	 * - hasArgument
	 * - hasArgNumber
	 * - hasArgTerm
     * 
     * @param argument the ordinary variable that is the argument (null is accept)
     * @param individual The individual where this is a argument
     * @param name The name of the node where this is a argument
     * @param argNumber The number of this argument in the list of arguments of the node.
     */
    protected void saveSimpleArgRelationship(OrdinaryVariable argument, OWLIndividual individual, String name, int argNumber){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass(SIMPLE_ARGUMENT_RELATIONSHIP); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(name + NUMBER_SEPARATOR + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
		if(argument != null){
		   OWLIndividual oVariableIndividual = mapOrdinaryVariable.get(argument); 
		   argumentIndividual.addPropertyValue(hasArgTerm, oVariableIndividual); 
		}
		
		OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
		argumentIndividual.setPropertyValue(hasArgNumber, argNumber);
		
    }
    

    /**
     * Pre-conditions: run after loadDomainResidentNode. 
     */
    protected void saveGenerativeInputNode(){
    	
    	for(MFrag mfrag : mebn.getDomainMFragList()){
    		for (InputNode generativeInputNode: mfrag.getInputNodeList()){  
    			OWLIndividual generativeInputNodeIndividual = mapGenerativeInput.get(generativeInputNode);	
    			
    			/* has Argument */
    			if (generativeInputNode.getInputInstanceOf() != null){
    				if(generativeInputNode.getInputInstanceOf() instanceof IResidentNode){
    					ResidentNodePointer pointer = generativeInputNode.getResidentNodePointer(); 
    					OrdinaryVariable[] ovArray = pointer.getOrdinaryVariableArray(); 
    					for(int i = 0; i < ovArray.length; i++){
    						saveSimpleArgRelationship(
    								ovArray[i], 
    								generativeInputNodeIndividual, 
    								generativeInputNode.getName(), 
    								i + 1); 
    					}
    				}
    				else{
    					//TODO instanceof BuiltInRV
    				}
    			}
    			
    			/* has Possible Values */
    			
    			if (generativeInputNode.getInputInstanceOf() != null){
    				if(generativeInputNode.getInputInstanceOf() instanceof IResidentNode){
    					IResidentNode residentNode = (IResidentNode)generativeInputNode.getInputInstanceOf(); 
    					for(Entity state: residentNode.getPossibleValueList()){
    						saveInputPossibleValues(generativeInputNodeIndividual, residentNode); 
    					}
    				}
    				else{
    					//TODO Built-in don't checked... 
    				}
    			}
    			
    			if(generativeInputNode.getDescription() != null){
    				generativeInputNodeIndividual.addComment(generativeInputNode.getDescription()); 
    			}
    		}
    	}
    	
    }
    
	/**
	 * Instances
	 * - CategoricalRVStates
	 * 
	 * Properties
	 * - hasType
	 * - hasPossibleValues
	 * 
	 * @param nodeIndividual
	 * @param node
	 */
	protected void saveInputPossibleValues(OWLIndividual nodeIndividual, IResidentNode node){

		/* categoricalRVStates */
		//OWLNamedClass categoricalRVStatesClass = owlModel.getOWLNamedClass(CATEGORICAL_STATE); 
		//OWLIndividual categoryLabel = mapMetaEntity.get("CategoryLabel"); 		
		//OWLObjectProperty hasType = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasType"); 	
		
		OWLObjectProperty hasPossibleValues = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 	
		
		for(Entity state: node.getPossibleValueList()){
			if(state instanceof CategoricalStateEntity){
				 /* N�o cria estados categ�ricos... apenas procura o estado j� criado para um n� residente. */
				OWLIndividual stateIndividual = mapCategoricalStates.get(state); 
				nodeIndividual.addPropertyValue(hasPossibleValues, stateIndividual);
			}
			else{
				if(state instanceof BooleanStateEntity){
					nodeIndividual.addPropertyValue(hasPossibleValues, mapBooleanStatesEntity.get(state)); 
				}
				else{
					if(state instanceof ObjectEntity){
						nodeIndividual.addPropertyValue(hasPossibleValues, mapMetaEntity.get(((ObjectEntity)state).getType().getName())); 		
					}else{
						Debug.print("Error: Invalid State - " + state.getName()); 
					}
				}
			}
		}
	}
	
    
    /**
     * Load the context nodes from the MEBN structure for the PR-OWL structure. 
     */
    protected void saveContextNode(){
    	
    	for(MFrag mfrag: mebn.getDomainMFragList()){
    		for (ContextNode contextNode: mfrag.getContextNodeList()){
    			
    			OWLIndividual contextNodeIndividual = mapContext.get(contextNode);	
    			NodeFormulaTree formulaNode = contextNode.getFormulaTree(); 
    			if (formulaNode != null){
    				saveContextNodeFormula(formulaNode, contextNodeIndividual, contextNode); 
    			}		
    			saveContextPossibleValues(contextNodeIndividual, contextNode);
    			
    			if(contextNode.getDescription()!=null){
    				contextNodeIndividual.addComment(contextNode.getDescription()); 
    			}
        		
    		}		
    	}
    }
    
    /**
     * Save a category state how one Arg Relationship of a node. 
     *
	 * Instances
	 * - ArgRelationship
	 * 
	 * Properties
	 * - hasArgument
	 * - hasArgTerm
	 * - hasArgNumber
     *
     * @param argument
     * @param individual
     * @param name Name of the node
     * @param argNumber
     */
    protected void saveCategoricalStateArgRelationship(CategoricalStateEntity argument, OWLIndividual individual, String name, int argNumber){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass(ARGUMENT_RELATIONSHIP); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(name + NUMBER_SEPARATOR + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
		if(argument != null){
		   OWLIndividual categoricalStateIndividual = mapCategoricalStates.get(argument); 
		   argumentIndividual.addPropertyValue(hasArgTerm, categoricalStateIndividual); 
		}
			
		OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
		argumentIndividual.setPropertyValue(hasArgNumber, argNumber);
		
    }
    
    /**
     * Save a boolean how one Arg Relationship of a node. 
     *
	 * Instances
	 * - ArgRelationship
	 * 
	 * Properties
	 * - hasArgument
	 * - hasArgTerm
	 * - hasArgNumber
     *
     * @param argument
     * @param individual
     * @param name Name of the node
     * @param argNumber
     */
    protected void saveBooleanArgRelationship(BooleanStateEntity argument, OWLIndividual individual, String name, int argNumber){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass(ARGUMENT_RELATIONSHIP); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(name + NUMBER_SEPARATOR + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
		if(argument != null){
		   OWLIndividual booleanStateIndividual = mapBooleanStatesEntity.get(argument); 
		   argumentIndividual.addPropertyValue(hasArgTerm, booleanStateIndividual); 
		}
			
		OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
		argumentIndividual.setPropertyValue(hasArgNumber, argNumber);
		
    }    
    
    /**
     * 
     * @param individual
     * @param node
     * @param argNumber
     * @param root
     */
    protected void saveBuiltInArgRelationship(OWLIndividual individual, MultiEntityNode node, int argNumber, NodeFormulaTree root){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass(ARGUMENT_RELATIONSHIP); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(node.getName() + NUMBER_SEPARATOR + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
		argumentIndividual.setPropertyValue(hasArgNumber, argNumber);
		
		OWLNamedClass contextNodeClass = owlModel.getOWLNamedClass(CONTEXT_NODE); 
		String innerContextName = node.getName() + NUMBER_SEPARATOR + argNumber + INNER_SUFIX; 
		OWLIndividual innerContextNode = contextNodeClass.createOWLIndividual(innerContextName); 
		
		ContextNode contextAux = new ContextNode(innerContextName, (MFrag)node.getMFrag()); 
		auxContextNodeList.add(contextAux); 
		
		OWLObjectProperty isInnerTermOf = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInnerTermOf"); 	
		innerContextNode.addPropertyValue(isInnerTermOf, individual); 
		
		OWLObjectProperty isContextNodeIn = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextNodeIn"); 	
		innerContextNode.addPropertyValue(isContextNodeIn, mapMFrag.get(node.getMFrag())); 
		
		saveContextNodeFormula(root, innerContextNode, contextAux); 
		
		OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
		argumentIndividual.addPropertyValue(hasArgTerm, innerContextNode); 
		
    }       
    
    /**
     * Save a SimpleArgRelationship without ordinary variable set. 
     * 
     * @param individual
     * @param name The name of the node that has the argument. 
     * @param argNumber
     */
    protected void saveEmptySimpleArgRelationship(OWLIndividual individual, String name, int argNumber){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass(SIMPLE_ARGUMENT_RELATIONSHIP); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(name + "_" + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
		argumentIndividual.setPropertyValue(hasArgNumber, argNumber);
		
    }

    /**
     * Save a argument where the term is a Resident Node. 
     * The strucuture of Pr-OWL for this save is: 
     * 
     * 01 - Is create a ArgRelationship where the name is NodeName_ArgNumber
     * 02 - The argument is setted how a argument of the individual (original context node)
     * 03 - Is create a context node Inner Term with the name NodeName_ArgNumber_inner
     * 04 - The inner term is setted how inner term of the individual (original context node)
     * 05 - The inner term is setted how context node of the MFrag of the original context node
     * 06 - The possible values of the inner term is setted how the possible values of the resident node
     * 07 - The arguments of the inner term is setted how the arguments of the residentNode (recursively for this method). 
     * 08 - The inner term is setted how the argument element of the ArgRelationship create in the first step.  
     * 
     * @param argument The pointer for the resident node that is the argument
     * @param individual The individual where this is a argument
     * @param node The name of the node where this is a argument
     * @param argNumber The number of this argument in the list of arguments of the node.
     */
    protected void saveResidentNodeArgRelationship(ResidentNodePointer argument, OWLIndividual individual, MultiEntityNode node, int argNumber){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass(ARGUMENT_RELATIONSHIP); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(node.getName() + "_" + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
		argumentIndividual.setPropertyValue(hasArgNumber, argNumber);
		
		OWLNamedClass contextNodeClass = owlModel.getOWLNamedClass(CONTEXT_NODE); 
		OWLIndividual innerContextNode = contextNodeClass.createOWLIndividual(node.getName()  + "_" + argNumber + "_inner"); 
		
		OWLObjectProperty isInnerTermOf = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInnerTermOf"); 	
		innerContextNode.addPropertyValue(isInnerTermOf, individual); 
		
		OWLObjectProperty isContextNodeIn = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextNodeIn"); 	
		innerContextNode.addPropertyValue(isContextNodeIn, mapMFrag.get(node.getMFrag())); 
		
		OWLObjectProperty isContextInstanceOf = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextInstanceOf"); 	
		innerContextNode.addPropertyValue(isContextInstanceOf, mapDomainResident.get(argument.getResidentNode())); 
		
		//Save the possible values
		//saveHasPossibleValueProperty(innerContextNode, argument.getResidentNode()); 
		saveResidentPossibleValues(innerContextNode, (IResidentNode)argument.getResidentNode()); 
		
        //Save the arguments
		OrdinaryVariable[] oVariableArray = argument.getOrdinaryVariableArray(); 
		for(int i = 0; i < oVariableArray.length; i++){
			if(oVariableArray[i] == null){
				this.saveEmptySimpleArgRelationship(innerContextNode, innerContextNode.getName(), i + 1); 
			}
			else{
				this.saveSimpleArgRelationship(oVariableArray[i], innerContextNode, innerContextNode.getName(), i + 1); 
			}
		}
		
		OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
		argumentIndividual.addPropertyValue(hasArgTerm, innerContextNode); 
		
    }   
    
    /**
     * Create the PR-OWL structure for the formula of a context node. 
     * 
     * @param _formulaNode Root of the tree of the formula
     * @param contextNodeIndividual Protege individual for the context node
     * @param contextNode Context node from the MEBN structure
     */
    protected void saveContextNodeFormula(NodeFormulaTree _formulaNode, OWLIndividual contextNodeIndividual, ContextNode contextNode){
    	
    	NodeFormulaTree formulaNode = _formulaNode; 
    	
    	if((formulaNode.getTypeNode() == EnumType.SIMPLE_OPERATOR)||
    			(formulaNode.getTypeNode() == EnumType.QUANTIFIER_OPERATOR)){
    		
    		if(formulaNode.getNodeVariable() instanceof BuiltInRV){
    			
    			//Step 1: Built-In
    			OWLObjectProperty isContextInstanceOf = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextInstanceOf");
    			OWLIndividual builtInIndividual = mapBuiltInOwlIndividual.get(((BuiltInRV)(formulaNode.getNodeVariable())).getName()); 
    			contextNodeIndividual.setPropertyValue(isContextInstanceOf, builtInIndividual); 	
    			
    			//Step 2: Arguments
    			List<NodeFormulaTree> childrenList = formulaNode.getChildren(); 
    			
    			/* 
    			 * Number of the argument in the list of arguments. 
    			 * In the Pr-Owl the first argument have the number 1. 
    			 */
    			int argNumber = 0; 
    			
    			for(NodeFormulaTree child: childrenList){
    				
    				/*
    				 * One argument of a context node can be of the types: 
    				 * - Ordinary Variable
    				 * - Node (Domain Resident Node)
    				 * - Entity
    				 * - Exemplar
    				 */
    				argNumber++; 					
    				if(child.getTypeNode() == EnumType.OPERAND){
    					
    					switch(child.getSubTypeNode()){
    					
    					case NOTHING:
    						saveEmptySimpleArgRelationship(contextNodeIndividual, contextNode.getName(), argNumber );
    						break; 
    						
    					case OVARIABLE: 
    						saveSimpleArgRelationship((OrdinaryVariable)(child.getNodeVariable()), contextNodeIndividual, contextNode.getName(), argNumber ); 
    						break; 
    						
    					case NODE: 
    						if(child.getNodeVariable() instanceof ResidentNodePointer){
    							saveResidentNodeArgRelationship((ResidentNodePointer)child.getNodeVariable(), contextNodeIndividual, contextNode, argNumber );
    						}
    						else{
    							//TODO other cases? 
    						}
    						break; 
    						
    					case ENTITY:
    						if(child.getNodeVariable() instanceof CategoricalStateEntity){
    							saveCategoricalStateArgRelationship((CategoricalStateEntity)child.getNodeVariable(), contextNodeIndividual, contextNode.getName(), argNumber); 
    						}
    						else{
    							if(child.getNodeVariable() instanceof BooleanStateEntity){
        							saveBooleanArgRelationship((BooleanStateEntity)child.getNodeVariable(), contextNodeIndividual, contextNode.getName(), argNumber); 
        						}
    							else{
    								//TODO algum outro caso possivel? 
    							}
    						}
    						break; 
    						
    					default: 
    						saveEmptySimpleArgRelationship(contextNodeIndividual, contextNode.getName(), argNumber );
    					break; 
    					
    					}
    					
    				}
    				else{
    					if(child.getTypeNode() == EnumType.SIMPLE_OPERATOR) {
    						saveBuiltInArgRelationship(contextNodeIndividual, contextNode, argNumber, child );
    						
    					}
    					else{
    						if(child.getTypeNode() == EnumType.QUANTIFIER_OPERATOR){
    							saveBuiltInArgRelationship(contextNodeIndividual, contextNode, argNumber, child );
    						}
    						else{
    							//TODO avaliar outros casos... 
    							saveEmptySimpleArgRelationship(contextNodeIndividual, contextNode.getName(), argNumber );
    						}
    					}
    				}
    				
    			}
    			
    		}
    		else{ //don't is a built-in... 
    			
    		}
    		
    	}
    	else{ //don't is a enumType.OPERANDO
    		if((formulaNode.getTypeNode() == EnumType.OPERAND)&&
	    			(formulaNode.getSubTypeNode() == EnumSubType.NODE)){
				
				OWLObjectProperty isContextInstanceOf = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextInstanceOf"); 	
				ResidentNodePointer pointer = (ResidentNodePointer)formulaNode.getNodeVariable(); 
				contextNodeIndividual.addPropertyValue(isContextInstanceOf, mapDomainResident.get(pointer.getResidentNode())); 
				
				//Save the possible values
				saveResidentPossibleValues(contextNodeIndividual, pointer.getResidentNode()); 
				
		        //Save the arguments
				OrdinaryVariable[] oVariableArray = pointer.getOrdinaryVariableArray(); 
				for(int i = 0; i < oVariableArray.length; i++){
					if(oVariableArray[i] == null){
						this.saveEmptySimpleArgRelationship(contextNodeIndividual, contextNodeIndividual.getName(), i + 1); 
					}
					else{
						this.saveSimpleArgRelationship(oVariableArray[i], contextNodeIndividual, contextNodeIndividual.getName(), i + 1); 
					}
				}
				
			}else{
				
			}
    	}
    }
    
	
	/**
	 * Save the possible values of a node. 
	 * 
	 * @param individual The individual owl for the node
	 * @param node The node that have the possible values
	 */
	protected void saveContextPossibleValues(OWLIndividual individual, IMultiEntityNode node){
		/*has possible values */
		OWLObjectProperty hasPossibleValuesProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 	
		for(Entity possibleValue: node.getPossibleValueList()){
			if(possibleValue instanceof CategoricalStateEntity)
			individual.addPropertyValue(hasPossibleValuesProperty, this.mapCategoricalStates.get(possibleValue)); 
			else{ //boolean states entity
				individual.addPropertyValue(hasPossibleValuesProperty, this.mapBooleanStatesEntity.get(possibleValue)); 
			}
		}
	}
	
	protected void clearAuxiliaryLists(){
		
		for(ContextNode context: auxContextNodeList){
			context.delete(); 
		}
		
	}
	
	/**
	 * Maps:
	 * 	- mapObjectEntityClasses
	 * 
	 * this method expect all other OWL structures from the MFrag was previously build
	 *  - basically, ObjectEntity and MetaEntity are sufficient
	 *  
	 *  Currently, it does some redundant data access (since other methods already
	 *  accesses those data), but correcting it would be a major
	 *  refactor work to solve this problem.
	 */
	protected void saveEntityIndividuals() {
		OWLIndividual individual = null;
		OWLNamedClass currentOWLEntity = null;
		// extracts all entities found inside this MTheory
		for (ObjectEntity entity : this.mebn.getObjectEntityContainer().getListEntity()) {
			currentOWLEntity = this.mapObjectEntityClasses.get(entity);
			 if (currentOWLEntity != null) {
				 // create OWL individuals for each object entity instance found for that entity
				 for ( ObjectEntityInstance entityInstance : entity.getInstanceList()) {
					individual = currentOWLEntity.createOWLIndividual(entityInstance.getName()); 
					// fill required properties (hasType and hasUID)
					individual.addPropertyValue( owlModel.getOWLObjectProperty("hasType"),
												 this.mapMetaEntity.get(entityInstance.getType().getName()));
					individual.addPropertyValue( owlModel.getOWLDatatypeProperty("hasUID"),"!" + entityInstance.getName());
				}
			 }
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.IProtegeOWLModelUser#getLastOWLModel()
	 */
	public OWLModel getLastOWLModel() {
		return this.owlModel;
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.IProtegeOWLModelUser#setOWLModelToUse(edu.stanford.smi.protegex.owl.model.OWLModel)
	 */
	public void setOWLModelToUse(OWLModel model) throws IOMebnException {
		JenaOWLModel jenamodel = null;
		try{
			jenamodel = (JenaOWLModel)model;
		} catch (ClassCastException cce) {
			// this class only works with JenaOWLModel
			return;
		}
		this.owlModel = jenamodel;
	}


	/**
	 * @return the mapBuiltInOwlIndividual
	 */
	protected HashMap<String, OWLIndividual> getMapBuiltInOwlIndividual() {
		return mapBuiltInOwlIndividual;
	}


	/**
	 * @param mapBuiltInOwlIndividual the mapBuiltInOwlIndividual to set
	 */
	protected void setMapBuiltInOwlIndividual(
			HashMap<String, OWLIndividual> mapBuiltInOwlIndividual) {
		this.mapBuiltInOwlIndividual = mapBuiltInOwlIndividual;
	}


	/**
	 * @return the mapMetaEntity
	 */
	protected HashMap<String, OWLIndividual> getMapMetaEntity() {
		return mapMetaEntity;
	}


	/**
	 * @param mapMetaEntity the mapMetaEntity to set
	 */
	protected void setMapMetaEntity(HashMap<String, OWLIndividual> mapMetaEntity) {
		this.mapMetaEntity = mapMetaEntity;
	}


	/**
	 * @return the mapCategoricalStates
	 */
	protected HashMap<Entity, OWLIndividual> getMapCategoricalStates() {
		return mapCategoricalStates;
	}


	/**
	 * @param mapCategoricalStates the mapCategoricalStates to set
	 */
	protected void setMapCategoricalStates(
			HashMap<Entity, OWLIndividual> mapCategoricalStates) {
		this.mapCategoricalStates = mapCategoricalStates;
	}


	/**
	 * @return the mapObjectEntityClasses
	 */
	protected HashMap<ObjectEntity, OWLNamedClass> getMapObjectEntityClasses() {
		return mapObjectEntityClasses;
	}


	/**
	 * @param mapObjectEntityClasses the mapObjectEntityClasses to set
	 */
	protected void setMapObjectEntityClasses(
			HashMap<ObjectEntity, OWLNamedClass> mapObjectEntityClasses) {
		this.mapObjectEntityClasses = mapObjectEntityClasses;
	}


	/**
	 * @return the mapBooleanStatesEntity
	 */
	protected HashMap<BooleanStateEntity, OWLIndividual> getMapBooleanStatesEntity() {
		return mapBooleanStatesEntity;
	}


	/**
	 * @param mapBooleanStatesEntity the mapBooleanStatesEntity to set
	 */
	protected void setMapBooleanStatesEntity(
			HashMap<BooleanStateEntity, OWLIndividual> mapBooleanStatesEntity) {
		this.mapBooleanStatesEntity = mapBooleanStatesEntity;
	}


	/**
	 * @return the mapMFrag
	 */
	protected HashMap<MFrag, OWLIndividual> getMapMFrag() {
		return mapMFrag;
	}


	/**
	 * @param mapMFrag the mapMFrag to set
	 */
	protected void setMapMFrag(HashMap<MFrag, OWLIndividual> mapMFrag) {
		this.mapMFrag = mapMFrag;
	}


	/**
	 * @return the mapOrdinaryVariable
	 */
	protected HashMap<OrdinaryVariable, OWLIndividual> getMapOrdinaryVariable() {
		return mapOrdinaryVariable;
	}


	/**
	 * @param mapOrdinaryVariable the mapOrdinaryVariable to set
	 */
	protected void setMapOrdinaryVariable(
			HashMap<OrdinaryVariable, OWLIndividual> mapOrdinaryVariable) {
		this.mapOrdinaryVariable = mapOrdinaryVariable;
	}


	/**
	 * @return the mapContext
	 */
	protected HashMap<ContextNode, OWLIndividual> getMapContext() {
		return mapContext;
	}


	/**
	 * @param mapContext the mapContext to set
	 */
	protected void setMapContext(HashMap<ContextNode, OWLIndividual> mapContext) {
		this.mapContext = mapContext;
	}


	/**
	 * @return the mapGenerativeInput
	 */
	protected HashMap<InputNode, OWLIndividual> getMapGenerativeInput() {
		return mapGenerativeInput;
	}


	/**
	 * @param mapGenerativeInput the mapGenerativeInput to set
	 */
	protected void setMapGenerativeInput(
			HashMap<InputNode, OWLIndividual> mapGenerativeInput) {
		this.mapGenerativeInput = mapGenerativeInput;
	}


	/**
	 * @return the mapDomainResident
	 */
	protected HashMap<ResidentNode, OWLIndividual> getMapDomainResident() {
		return mapDomainResident;
	}


	/**
	 * @param mapDomainResident the mapDomainResident to set
	 */
	protected void setMapDomainResident(
			HashMap<ResidentNode, OWLIndividual> mapDomainResident) {
		this.mapDomainResident = mapDomainResident;
	}


	/**
	 * @return the auxContextNodeList
	 */
	protected ArrayList<ContextNode> getAuxContextNodeList() {
		return auxContextNodeList;
	}


	/**
	 * @param auxContextNodeList the auxContextNodeList to set
	 */
	protected void setAuxContextNodeList(ArrayList<ContextNode> auxContextNodeList) {
		this.auxContextNodeList = auxContextNodeList;
	}


	/**
	 * @return the owlModel
	 */
	protected JenaOWLModel getOwlModel() {
		return owlModel;
	}


	/**
	 * @param owlModel the owlModel to set
	 */
	protected void setOwlModel(JenaOWLModel owlModel) {
		this.owlModel = owlModel;
	}


	/**
	 * @return the mebn
	 */
	protected MultiEntityBayesianNetwork getMebn() {
		return mebn;
	}


	/**
	 * @param mebn the mebn to set
	 */
	protected void setMebn(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
	}


	/**
	 * @return the resource
	 */
	public ResourceBundle getResource() {
		return resource;
	}


	/**
	 * @param resource the resource to set
	 */
	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}
	
	
	
	
}