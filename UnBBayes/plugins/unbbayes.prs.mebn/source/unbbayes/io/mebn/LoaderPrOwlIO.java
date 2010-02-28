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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import unbbayes.gui.InternalErrorDialog;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Edge;
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
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.EntityInstanceAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
import unbbayes.util.Debug;
import unbbayes.util.longtask.LongTaskProgressChangedEvent;
import unbbayes.util.longtask.ILongTaskProgressObservable;
import unbbayes.util.longtask.ILongTaskProgressObserver;

import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;

/**
 * Make the loader from a file pr-owl for the mebn structure. 
 * 
 * Version Pr-OWL: 1.05 (octuber, 2007)
 * (http://www.pr-owl.org/pr-owl.owl) 
 * 
 * @author Laecio Lima dos Santos
 * @author Shou Matsumoto
 * @version 1.0 
 */

public class LoaderPrOwlIO extends PROWLModelUser implements ILongTaskProgressObservable{

	/* MEBN Structure */ 
	
	private MultiEntityBayesianNetwork mebn = null;
	
	private Collection instances; 
	private Iterator itAux; 
	
	private HashMap<String, MFrag> mapDomainMFrag = new HashMap<String, MFrag>(); 
	private HashMap<String, OrdinaryVariable> mapOVariable = new HashMap<String, OrdinaryVariable>();
	
	/* 
	 * the first contains the context nodes of the MTheory while the second contains 
	 * the context nodes inner terms (exists only in the pr-owl, not in the mebn
	 * structure)
	 */
	private HashMap<String, ContextNode> mapContextNode = new HashMap<String, ContextNode>();
	private HashMap<String, ContextNode> mapContextNodeInner = new HashMap<String, ContextNode>();
	
	private List<ContextNode> listContextNode = new ArrayList<ContextNode>(); 
	
	private HashMap<ContextNode, Object> mapIsContextInstanceOf = new HashMap<ContextNode, Object>(); 

	private HashMap<String, ResidentNode> mapDomainResidentNode = new HashMap<String, ResidentNode>();
	private HashMap<String, InputNode> mapGenerativeInputNode = new HashMap<String, InputNode>();
	private HashMap<String, Argument> mapArgument = new HashMap<String, Argument>();
	private HashMap<String, MultiEntityNode> mapMultiEntityNode = new HashMap<String, MultiEntityNode>(); 
	private HashMap<String, BuiltInRV> mapBuiltInRV = new HashMap<String, BuiltInRV>(); 
	
	private HashMap<String, ObjectEntity> mapObjectEntity = new HashMap<String, ObjectEntity>(); 	
	private HashMap<String, CategoricalStateEntity> mapCategoricalStates = new HashMap<String, CategoricalStateEntity>(); 
	private HashMap<String, BooleanStateEntity> mapBooleanStates = new HashMap<String, BooleanStateEntity>(); 
	
	private HashMap<String, List<String>> mapObjectEntityGloballyObjects = new HashMap<String, List<String>>(); 
	private HashMap<String, List<String>> mapCategoricalStateGloballyObjects = new HashMap<String, List<String>>(); 
	private HashMap<String, List<String>> mapBooleanStateGloballyObjects = new HashMap<String, List<String>>(); 
	
	private HashMap<String, ObjectEntity> mapTypes = new HashMap<String, ObjectEntity>(); 	
	
	/* Protege API Structure */
	
	private JenaOWLModel owlModel;  
	
	/** Load resource file from this package */
	final ResourceBundle resource = 
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.io.mebn.resources.IoMebnResources.class.getName());	
	
	//private static final String PROWLMODELFILE = "pr-owl/pr-owl.owl";
	
	private final String ORDINARY_VAR_SCOPE_SEPARATOR = ".";
	private final String POSSIBLE_VALUE_SCOPE_SEPARATOR = ".";	
	
	//names of the classes in PR_OWL FIle
	//private static final String CATEGORICAL_STATE = "CategoricalRVState"; 
	//private static final String BOOLEAN_STATE = "BooleanRVState";
	//private static final String OBJECT_ENTITY = "ObjectEntity"; 
	//private static final String META_ENTITY = "MetaEntity"; 
	
	public void cancel(){
		System.out.println("Stop");
	}
	
	/**
	 * Make the load from file to MEBN structure.
	 * 
	 * @param file The pr-owl file. 
	 * @return the <MultiEntityBayesianNetwork> build from the pr-owl file
	 * @throws IOException
	 * @throws IOMebnException
	 */
	public MultiEntityBayesianNetwork loadMebn(File file) throws 
													IOException, IOMebnException{

		Debug.setDebug(false); 
		
		List<String> listWarnings = new ArrayList<String>(); 
		
		updateProgress(0, ""); 
		owlModel = ProtegeOWL.createJenaOWLModel();
		
		Debug.println("[DEBUG]" + this.getClass() + " -> Load begin"); 
		
//		File filePrOwl = new File(PROWLMODELFILE);
//		FileInputStream inputStreamOwl = new FileInputStream(filePrOwl); 
//		
//		owlModel.getRepositoryManager().addProjectRepository(
//				new LocalFileRepository(filePrOwl, true));
//		
//		try{
//			owlModel.load(inputStreamOwl, FileUtils.langXMLAbbrev);
//			Debug.println("-> Load of model file PR-OWL successful"); 
//		}	
//		catch(Exception e){
//			throw new IOMebnException(resource.getString("ErrorReadingFile") + 
//					                  ": " + PROWLMODELFILE); 
//		}
			
		FileInputStream inputStream = new FileInputStream(file); 
		
		try{
			owlModel.load(inputStream, FileUtils.langXMLAbbrev);   
			updateProgress(5, ""); 
		}
		catch (Exception e){
			throw new IOMebnException(resource.getString("ErrorReadingFile") + 
					                    ": " + file.getAbsolutePath()); 
		}
		
		/*------------------- MTheory -------------------*/
		loadMTheoryClass(); 
		updateProgress(6, ""); 
		
		/*------------------- Entities -------------------*/

		loadObjectEntity(); 
		updateProgress(7, "");
		
		loadMetaEntitiesClasses();
		updateProgress(9, "");
		
		loadCategoricalStateEntity(); 
		updateProgress(11, "");
		
		loadBooleanStateEntity(); 
		updateProgress(13, "");
		
		/*-------------------MTheory elements------------*/
		loadDomainMFrag(); 
		updateProgress(20, "");
		
		loadBuiltInRV(); 
		updateProgress(25, "");
		
		loadContextNode(); 	
		updateProgress(35, "");
		
		loadDomainResidentNode();
		updateProgress(45, "");
		
		loadGenerativeInputNode(); 	
		updateProgress(55, "");
		
		/*---------------------Arguments-------------------*/
		loadOrdinaryVariable();
		updateProgress(65, "");
		
		loadArgRelationship();
		updateProgress(75, "");
		 		
		loadSimpleArgRelationship(); 
		updateProgress(85, "");
		
		ajustArgumentOfNodes(); 
		updateProgress(90, "");
		
		setFormulasOfContextNodes();
		updateProgress(95, "");
		
		// Load object entity individuals (ObjectEntityInstances)
		try {
			loadObjectEntityIndividuals();
			updateProgress(100, "");
		} catch (TypeException te) {
			te.printStackTrace();
			throw new IOMebnException(te.getMessage());
		}
		
		return mebn; 		
	}
	
	/**
	 * Load the MTheory and the MFrags objects
	 * 
	 * Pre-requisites:
	 * - Only one MTheory per file 
	 * - The MFrags have different names
	 */
	private void loadMTheoryClass() throws IOMebnException {
        
		MFrag domainMFrag; 		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 
		OWLNamedClass owlNamedClass; 	
		OWLObjectProperty objectProperty; 
		
		owlNamedClass = owlModel.getOWLNamedClass(MTHEORY); 
		
		instances = owlNamedClass.getInstances(false); 
		itAux = instances.iterator(); 
		
		if(!itAux.hasNext()){
			throw new IOMebnException(resource.getString("MTheoryNotExist")); 
		}
		
		individualOne = (OWLIndividual) itAux.next();
		mebn = new MultiEntityBayesianNetwork(individualOne.getBrowserText()); 
		mebn.getNamesUsed().add(individualOne.getBrowserText()); 
		
		Debug.println("MTheory loaded: " + individualOne.getBrowserText()); 
		
		//Properties
		String comment = null; 
		
		comment = getDescription(individualOne);
		
		mebn.setDescription(comment); 
		
		/* hasMFrag */
		objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasMFrag"); 	
		instances = individualOne.getPropertyValues(objectProperty); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualTwo = (OWLIndividual) it.next();
			Debug.println("hasDomainMFrag: " + individualTwo.getBrowserText()); 
			domainMFrag = new MFrag(individualTwo.getBrowserText(), mebn); 
			mebn.addDomainMFrag(domainMFrag); 
			mapDomainMFrag.put(individualTwo.getBrowserText(), domainMFrag); 
			
			mebn.getNamesUsed().add(individualTwo.getBrowserText()); 
		}	
 
	}

	private String getDescription(OWLIndividual individualOne) {
		String comment = null; 
		Collection comments = individualOne.getComments(); 
		if(comments.size() > 0){
			if(comments.toArray()[0] instanceof String){
				comment =  (String)comments.toArray()[0]; 
			}
		}
		return comment;
	}
	
	/**
	 * Load the MetaEntities for types of the mebn structure. 
	 */
	private void loadMetaEntitiesClasses(){
		
		OWLNamedClass metaEntityClass; 
		Collection instances; 
		OWLIndividual individualOne;
		
		metaEntityClass = owlModel.getOWLNamedClass(META_ENTITY);
		
		instances = metaEntityClass.getInstances(false); 
		
		for (Object owlIndividual : instances){
			individualOne = (OWLIndividual) owlIndividual; 
			
			try{
			    Type type = mebn.getTypeContainer().createType(individualOne.getBrowserText()); 

				mebn.getNamesUsed().add(individualOne.getBrowserText()); 
			}
			catch (TypeAlreadyExistsException exception){
				//OK... lembre-se que os tipos basicos já existem... 
			}
			
			Debug.println("Meta Entity Loaded: " + individualOne.getBrowserText()); 
						
		}		
	}
	
	/**
	 * Load the Object Entities of the file. 
	 * 
	 * Note: the type of the object entity don't is read of the file pr-owl... 
	 * it is create automaticaly when the object entity is created (default name for
	 * a type of a object entity). 
	 */
	private void loadObjectEntity(){
    
        OWLNamedClass objectEntityClass; 
		Collection subClasses; 
		OWLNamedClass subClass; 
		OWLObjectProperty objectProperty; 		
		
		OWLObjectProperty isGloballyExclusive = (OWLObjectProperty)owlModel.getOWLObjectProperty("isGloballyExclusive"); 	

		objectEntityClass = owlModel.getOWLNamedClass(OBJECT_ENTITY);
		
		subClasses = objectEntityClass.getSubclasses(true); 
		
		for (Object owlClass : subClasses){
			
			subClass = (OWLNamedClass)owlClass; 
			
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasType");

			try{
				ObjectEntity objectEntityMebn = mebn.getObjectEntityContainer().createObjectEntity(subClass.getBrowserText()); 	
			    mapObjectEntity.put(subClass.getBrowserText(), objectEntityMebn); 
			    mapTypes.put(objectEntityMebn.getType().getName(), objectEntityMebn); 

				mebn.getNamesUsed().add(subClass.getBrowserText()); 
			}
			catch(TypeException typeException){
				typeException.printStackTrace(); 
			}
		}	
	}
	
	private void loadCategoricalStateEntity(){
		
		OWLNamedClass categoricalStateClass; 
		Collection instances; 
		Collection globallyExclusiveObjects; 
		OWLIndividual individualOne;
		OWLObjectProperty isGloballyExclusive = (OWLObjectProperty)owlModel.getOWLObjectProperty("isGloballyExclusive"); 	

		categoricalStateClass = owlModel.getOWLNamedClass(CATEGORICAL_STATE);
		
		instances = categoricalStateClass.getInstances(false); 
		
		for (Object owlIndividual : instances){
			individualOne = (OWLIndividual) owlIndividual; 
			
			CategoricalStateEntity state = mebn.getCategoricalStatesEntityContainer().createCategoricalEntity(individualOne.getBrowserText()); 

			mebn.getNamesUsed().add(individualOne.getBrowserText()); 
			
			globallyExclusiveObjects = individualOne.getPropertyValues(isGloballyExclusive); 
			ArrayList<String> listObjects = new ArrayList<String>(); 
			for (Object object : globallyExclusiveObjects){
				OWLIndividual nodeIndividual = (OWLIndividual) object;
				listObjects.add(nodeIndividual.getBrowserText()); 

			}
			
			mapCategoricalStateGloballyObjects.put(state.getName(), listObjects); 
			
			Debug.println("Categorical State Entity Loaded: " + individualOne.getBrowserText()); 
						
		}	
	}
	
	private void loadBooleanStateEntity(){
		
		OWLNamedClass booleanStateClass; 
		Collection instances; 
		Collection globallyExclusiveObjects; 
		OWLIndividual individualOne;
		OWLObjectProperty isGloballyExclusive = (OWLObjectProperty)owlModel.getOWLObjectProperty("isGloballyExclusive"); 	

		booleanStateClass = owlModel.getOWLNamedClass(BOOLEAN_STATE);
		
		instances = booleanStateClass.getInstances(false); 
		
		for (Object owlIndividual : instances){
			individualOne = (OWLIndividual) owlIndividual; 
			
			BooleanStateEntity state = null; 
			
			if(individualOne.getBrowserText().equals("true")){
				state = mebn.getBooleanStatesEntityContainer().getTrueStateEntity(); 
				mebn.getNamesUsed().add(individualOne.getBrowserText()); 
				
			}else{
				if(individualOne.getBrowserText().equals("false")){
					state = mebn.getBooleanStatesEntityContainer().getFalseStateEntity(); 
					mebn.getNamesUsed().add(individualOne.getBrowserText()); 
					
				}else{
					if(individualOne.getBrowserText().equals("absurd")){
						state = mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity(); 
						mebn.getNamesUsed().add(individualOne.getBrowserText()); 
						
					}else{
						// 
					}
				}
			}
			
			if(state!=null){
				globallyExclusiveObjects = individualOne.getPropertyValues(isGloballyExclusive); 
				ArrayList<String> listObjects = new ArrayList<String>(); 
				for (Object object : globallyExclusiveObjects){
					OWLIndividual nodeIndividual = (OWLIndividual) object;
					listObjects.add(nodeIndividual.getBrowserText()); 
				}
				
				mapBooleanStateGloballyObjects.put(state.getName(), listObjects); 
			}
			
			Debug.println("Boolean State Entity Loaded: " + individualOne.getBrowserText()); 
		}	
		
	}
	
	private void loadDomainMFrag() throws IOMebnException{

		MFrag domainMFrag; 
		OrdinaryVariable oVariable; 
		ContextNode contextNode; 
		ResidentNode domainResidentNode; 
		InputNode generativeInputNode; 
		BuiltInRV builtInRV;		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 
		OWLNamedClass owlNamedClass; 	
		OWLObjectProperty objectProperty; 
		
		owlNamedClass = owlModel.getOWLNamedClass(DOMAIN_MFRAG); 
		instances = owlNamedClass.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();
			domainMFrag = mapDomainMFrag.get(individualOne.getBrowserText()); 
			if (domainMFrag == null){
				throw new IOMebnException(resource.getString("DomainMFragNotExistsInMTheory"), individualOne.getBrowserText()); 
			}
			
			Debug.println("DomainMFrag loaded: " + individualOne.getBrowserText()); 
			
			domainMFrag.setDescription(getDescription(individualOne)); 
			
			/* -> hasResidentNode */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasResidentNode"); 
			instances = individualOne.getPropertyValues(objectProperty); 
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				domainResidentNode = new ResidentNode(individualTwo.getBrowserText(), domainMFrag); 
				mebn.getNamesUsed().add(individualTwo.getBrowserText()); 
				
				domainMFrag.addResidentNode(domainResidentNode); 
				mapDomainResidentNode.put(individualTwo.getBrowserText(), domainResidentNode); 
				mapMultiEntityNode.put(individualTwo.getBrowserText(), domainResidentNode); 
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}	
			
			/* -> hasInputNode */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputNode"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				generativeInputNode = new InputNode(individualTwo.getBrowserText(), domainMFrag); 
				mebn.getNamesUsed().add(individualTwo.getBrowserText()); 
				domainMFrag.addInputNode(generativeInputNode); 
				mapGenerativeInputNode.put(individualTwo.getBrowserText(), generativeInputNode); 
				mapMultiEntityNode.put(individualTwo.getBrowserText(), generativeInputNode); 				
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}	
			
			/* -> hasContextNode */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasContextNode"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				contextNode = new ContextNode(individualTwo.getBrowserText(), domainMFrag); 
				mebn.getNamesUsed().add(individualTwo.getBrowserText()); 
				domainMFrag.addContextNode(contextNode); 
				mapContextNode.put(individualTwo.getBrowserText(), contextNode); 
				mapMultiEntityNode.put(individualTwo.getBrowserText(), contextNode); 				
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}	
			
			/* -> hasOVariable */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasOVariable"); 
			instances = individualOne.getPropertyValues(objectProperty); 
			String ovName = null;
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				ovName = individualTwo.getBrowserText();	// Name of the OV individual
				// Remove MFrag name from ovName. MFrag name is a scope identifier
				try {
					ovName = ovName.split(domainMFrag.getName() + this.getOrdinaryVarScopeSeparator())[1];
				} catch (java.lang.ArrayIndexOutOfBoundsException e) {
					//Use the original name... 
					ovName = ovName;	// If its impossible to split, then no Scope id was found
				}
				//Debug.println("> Internal OV name is : " + ovName);				
				// Create instance of OV w/o scope identifier
				oVariable = new OrdinaryVariable(ovName, mebn.getTypeContainer().getDefaultType(), domainMFrag); 
				domainMFrag.addOrdinaryVariable(oVariable); 
				// let's map objects w/ scope identifier included
				mapOVariable.put(individualTwo.getBrowserText(), oVariable); 
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}
		}						
	}
	
	private void loadContextNode() throws IOMebnException{

		MFrag domainMFrag; 
		ContextNode contextNode; 
		Argument argument;
		MultiEntityNode multiEntityNode; 	
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 
		
		OWLNamedClass contextNodePr = owlModel.getOWLNamedClass(CONTEXT_NODE); 
		instances = contextNodePr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			
			individualOne = (OWLIndividual)it.next();
			contextNode = mapContextNode.get(individualOne.getBrowserText()); 
			if (contextNode == null){
				throw new IOMebnException(resource.getString("ContextNodeNotExistsInMTheory"), individualOne.getBrowserText()); 
			}
			
			Debug.println("Context Node loaded: " + individualOne.getBrowserText()); 				
			
			contextNode.setDescription(getDescription(individualOne)); 
			
			/* -> isContextNodeIn  */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextNodeIn"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			individualTwo = (OWLIndividual) itAux.next();
			domainMFrag = mapDomainMFrag.get(individualTwo.getBrowserText()); 
			if(domainMFrag.containsContextNode(contextNode) == false){
				throw new IOMebnException(resource.getString("ContextNodeNotExistsInMFrag"), contextNode.getName() + ", " + domainMFrag.getName()); 
			}
			
			Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			
			/* -> isNodeFrom */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isNodeFrom"); 			
			instances = individualOne.getPropertyValues(objectProperty);		
			for(Iterator itIn = instances.iterator(); itIn.hasNext();  ){
				individualTwo = (OWLIndividual) itAux.next();
				domainMFrag = mapDomainMFrag.get(individualTwo.getBrowserText()); 
				if(domainMFrag.containsNode(contextNode) == false){
					throw new IOMebnException(resource.getString("NodeNotExistsInMFrag"), contextNode.getName() + ", " + domainMFrag.getName()); 
				}
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());				
			}
			
			/* -> hasArgument */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();			
				argument = new Argument(individualTwo.getBrowserText(), contextNode); 
				contextNode.addArgument(argument); 
				mapArgument.put(individualTwo.getBrowserText(), argument); 
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}
			
			/* -> isContextInstanceOf */
			
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextInstanceOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			
			if(itAux.hasNext() != false){
				individualTwo = (OWLIndividual) itAux.next();
				
				if(mapBuiltInRV.get(individualTwo.getBrowserText()) != null){
					mapIsContextInstanceOf.put(contextNode, mapBuiltInRV.get(individualTwo.getBrowserText())); 
				}
				else{
					if(mapDomainResidentNode.get(individualTwo.getBrowserText()) != null){
						mapIsContextInstanceOf.put(contextNode, mapDomainResidentNode.get(individualTwo.getBrowserText())); 
					}
				}
				
			}			
			
			/*
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextInstanceOf"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				contextNode = mapContextNode.get(individualTwo.getBrowserText());
				if(contextNode == null){
					throw new IOMebnException(resource.getString("ContextNodeNotExistsInMTheory"), individualTwo.getBrowserText()); 
				}
				builtInRV.addContextInstance(contextNode); 
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}
			*/
			
			
			/* -> isInnerTermOf */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInnerTermOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			
			// the context is only inner term of other... 
			if(itAux.hasNext()){
				
				domainMFrag.removeContextNode(contextNode); 
			
				for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
					individualTwo = (OWLIndividual) itIn.next();
					multiEntityNode = mapMultiEntityNode.get(individualTwo.getBrowserText()); 
					contextNode.addInnerTermFromList(multiEntityNode); 
					multiEntityNode.addInnerTermOfList(contextNode); 
					Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
				}				
			}
			else{
				listContextNode.add(contextNode); 
			}
		}
		
		/* the property isContextIntanceOf is fill in the mathod <loadBuiltInRV>  */ 
	
	}
	
	/**
	 * 
	 * 
	 * Note: shoud be executed after loadContextNode.  
	 * @throws IOMebnException
	 */
	
	private void loadBuiltInRV() throws IOMebnException{

		InputNode generativeInputNode; 
		BuiltInRV builtInRV = null;		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 
		
		OWLNamedClass builtInPr = owlModel.getOWLNamedClass(BUILTIN_RV); 
		instances = builtInPr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();
			
			String nameBuiltIn = individualOne.getBrowserText(); 
			
			if(nameBuiltIn.equals("and")){
				builtInRV = new BuiltInRVAnd(); 
			}else
				if(nameBuiltIn.equals("or")){
					builtInRV = new BuiltInRVOr(); 
				}else
					if(nameBuiltIn.equals("equalto")){
						builtInRV = new BuiltInRVEqualTo(); 
					}else
						if(nameBuiltIn.equals("exists")){
							builtInRV = new BuiltInRVExists(); 
						}else
							if(nameBuiltIn.equals("forall")){
								builtInRV = new BuiltInRVForAll(); 
							}else
								if(nameBuiltIn.equals("not")){
									builtInRV = new BuiltInRVNot(); 
								}else
									if(nameBuiltIn.equals("iff")){
										builtInRV = new BuiltInRVIff(); 
									}else								
										if(nameBuiltIn.equals("implies")){
											builtInRV = new BuiltInRVImplies(); 
										}else{
											//TODO exception? 											
										}	
			
			if(builtInRV != null){
				
				mebn.addBuiltInRVList(builtInRV); 
				mapBuiltInRV.put(individualOne.getBrowserText(), builtInRV); 
				Debug.println("BuiltInRV loaded: " + individualOne.getBrowserText()); 				
				
				/* -> hasInputInstance */
				objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputInstance"); 
				instances = individualOne.getPropertyValues(objectProperty); 	
				for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
					individualTwo = (OWLIndividual) itIn.next();
					generativeInputNode = mapGenerativeInputNode.get(individualTwo.getBrowserText());
					if(generativeInputNode == null){
						throw new IOMebnException(resource.getString("GenerativeInputNodeNotExistsInMTheory"), individualTwo.getBrowserText()); 
					}
					builtInRV.addInputInstance(generativeInputNode); 
					Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
				}
			}
			
		}						
	}
	
	private void loadDomainResidentNode() throws IOMebnException{

		MFrag domainMFrag; 
		ResidentNode domainResidentNode; 
		InputNode generativeInputNode; 
		Argument argument;
		MultiEntityNode multiEntityNode; 	
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 	
		
		OWLNamedClass domainResidentNodePr = owlModel.getOWLNamedClass(DOMAIN_RESIDENT); 
		instances = domainResidentNodePr.getInstances(false); 
		MFrag mFragOfNode = null; 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			
			individualOne = (OWLIndividual)it.next();
			domainResidentNode = mapDomainResidentNode.get(individualOne.getBrowserText()); 
			if (domainResidentNode == null){
				throw new IOMebnException(resource.getString("DomainResidentNotExistsInMTheory"), individualOne.getBrowserText() ); 
			}
			
			Debug.println("Domain Resident loaded: " + individualOne.getBrowserText()); 			
			
			domainResidentNode.setDescription(getDescription(individualOne)); 
			
			/* -> isResidentNodeIn  */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isResidentNodeIn"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			individualTwo = (OWLIndividual) itAux.next();
			domainMFrag = mapDomainMFrag.get(individualTwo.getBrowserText()); 
			if(domainMFrag.containsDomainResidentNode(domainResidentNode) == false){
				throw new IOMebnException(resource.getString("DomainResidentNotExistsInDomainMFrag") ); 
			}
			mFragOfNode = domainMFrag; 
			Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			
			/* -> hasArgument */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				argument = new Argument(individualTwo.getBrowserText(), domainResidentNode); 
				domainResidentNode.addArgument(argument); 
				mapArgument.put(individualTwo.getBrowserText(), argument); 
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}		
			
			/* -> hasParent */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasParent"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				if (mapDomainResidentNode.containsKey(individualTwo.getBrowserText())){
					ResidentNode aux = mapDomainResidentNode.get(individualTwo.getBrowserText()); 
					
					Edge auxEdge = new Edge(aux, domainResidentNode);
					try{
					    mFragOfNode.addEdge(auxEdge); 
					}
					catch(Exception e){
						Debug.println("Erro: arco invalido!!!"); 
					}
				}
				else{
					if (mapGenerativeInputNode.containsKey(individualTwo.getBrowserText())){
						InputNode aux = mapGenerativeInputNode.get(individualTwo.getBrowserText()); 
						
						Edge auxEdge = new Edge(aux, domainResidentNode);
						try{
						mFragOfNode.addEdge(auxEdge); 
						}
						catch(Exception e){
							Debug.println("Erro: arco invalido!!!"); 
						}
					
					}
					else{
						throw new IOMebnException(resource.getString("NodeNotFound"), individualTwo.getBrowserText() ); 
					}
				}
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}	
			
			/* -> hasInputInstance  */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputInstance"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				generativeInputNode = mapGenerativeInputNode.get(individualTwo.getBrowserText()); 
				try{
				   generativeInputNode.setInputInstanceOf(domainResidentNode); 
				}
				catch(Exception e){
					e.printStackTrace(); 
			    }
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			}
			
			/* -> isInnerTermOf */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInnerTermOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();			
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				multiEntityNode = mapMultiEntityNode.get(individualTwo.getBrowserText()); 
				domainResidentNode.addInnerTermFromList(multiEntityNode); 
				multiEntityNode.addInnerTermOfList(domainResidentNode); 
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			}				

			/* -> hasPossibleValues */
			{
				CategoricalStateEntity state = null; 
				objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 			
				instances = individualOne.getPropertyValues(objectProperty); 	
				itAux = instances.iterator();
				for (Object instance: instances){
					individualTwo = (OWLIndividual)instance;
					String stateName = individualTwo.getBrowserText(); 
					/* case 1: booleans states */
					if(stateName.equals("true")){
						StateLink link = domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());   
						List<String> globallyObjects = mapBooleanStateGloballyObjects.get("true"); 
						if(globallyObjects.contains(domainResidentNode.getName())){
							link.setGloballyExclusive(true); 
						}else{
							link.setGloballyExclusive(false); 
						}
						domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
					}
					else{
						if(stateName.equals("false")){
							StateLink link = domainResidentNode.addPossibleValueLink(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());   
							List<String> globallyObjects = mapBooleanStateGloballyObjects.get("false"); 
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
								List<String> globallyObjects = mapBooleanStateGloballyObjects.get("absurd"); 
								if(globallyObjects.contains(domainResidentNode.getName())){
									link.setGloballyExclusive(true); 
								}else{
									link.setGloballyExclusive(false); 
								}
								domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES);
							}
							else{
								if(mapTypes.get(stateName) != null){
									
									/* case 2:object entities */
									
									StateLink link = domainResidentNode.addPossibleValueLink(mapTypes.get(stateName)); 
//									List<String> globallyObjects = mapObjectEntityGloballyObjects.get(stateName); 
//									if(globallyObjects.contains(domainResidentNode.getName())){
//										link.setGloballyExclusive(true); 
//									}else{
//										link.setGloballyExclusive(false); 
//									}
									domainResidentNode.setTypeOfStates(ResidentNode.OBJECT_ENTITY);
								
								}
								else{
									/* case 3: categorical states */
									try {
										state = mebn.getCategoricalStatesEntityContainer().getCategoricalState(individualTwo.getBrowserText()) ;
										StateLink link = domainResidentNode.addPossibleValueLink(state); 
										
										List<String> globallyObjects = mapCategoricalStateGloballyObjects.get(state.getName()); 
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
			
			OWLObjectProperty hasProbDist = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasProbDist");
			OWLDatatypeProperty hasDeclaration = owlModel.getOWLDatatypeProperty("hasDeclaration"); 
			String cpt = null;
			for (Iterator iter = individualOne.getPropertyValues(hasProbDist).iterator(); iter.hasNext();) {
				OWLIndividual element = (OWLIndividual) iter.next();
				try {
					cpt = (String)element.getPropertyValue(hasDeclaration);
				} catch (Exception e) {
					cpt = "";
				}
				domainResidentNode.setTableFunction(cpt);
			}
			
			/* isArgTermIn don't checked */
			
		}
				
	}
	
	private void loadGenerativeInputNode() throws IOMebnException{
	    
		ResidentNode domainResidentNode; 
		InputNode generativeInputNode; 
		Argument argument;
		MultiEntityNode multiEntityNode; 
		BuiltInRV builtInRV;		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 		
		
		OWLNamedClass inputNodePr = owlModel.getOWLNamedClass(GENERATIVE_INPUT); 
		instances = inputNodePr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			
			individualOne = (OWLIndividual)it.next();
			Debug.println("  - Input Node loaded: " + individualOne.getBrowserText()); 			
			generativeInputNode = mapGenerativeInputNode.get(individualOne.getBrowserText()); 
			if (generativeInputNode == null){
				throw new IOMebnException(resource.getString("GenerativeInputNodeNotExistsInMTheory"), individualOne.getBrowserText() ); 				
			}
			
			generativeInputNode.setDescription(getDescription(individualOne)); 
			
			//loadHasPositionProperty(individualOne, generativeInputNode); 
			
			/* -> isInputInstanceOf  */
			
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInputInstanceOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			
			if(itAux.hasNext() != false){
				individualTwo = (OWLIndividual) itAux.next();
				
				if (mapDomainResidentNode.containsKey(individualTwo.getBrowserText())){
					domainResidentNode = mapDomainResidentNode.get(individualTwo.getBrowserText()); 
					try{
						generativeInputNode.setInputInstanceOf(domainResidentNode); 
					}
					catch(Exception e){
						e.printStackTrace(); 
					}
					Debug.println("   - isInputInstanceOf " + domainResidentNode.getName()); 
				}
				else{
					if (mapBuiltInRV.containsKey(individualTwo.getBrowserText())){
						builtInRV = mapBuiltInRV.get(individualTwo.getBrowserText()); 
						generativeInputNode.setInputInstanceOf(builtInRV); 
						Debug.println("   - isInputInstanceOf " + builtInRV.getName()); 
					}				
				}
			}
			
			/* -> hasArgument */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				argument = new Argument(individualTwo.getBrowserText(), generativeInputNode); 
				generativeInputNode.addArgument(argument); 
				mapArgument.put(individualTwo.getBrowserText(), argument); 
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}		
			
			/* isParentOf */
			//already checked in DomainResident load process. 
//			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isParentOf"); 
//			instances = individualOne.getPropertyValues(objectProperty); 	
//			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
//				individualTwo = (OWLIndividual) itIn.next();
//				domainResidentNode = mapDomainResidentNode.get(individualTwo.getBrowserText());
//				if(domainResidentNode == null){
//					throw new IOMebnException(resource.getString("DomainMFragNotExistsInMTheory"),  individualTwo.getBrowserText()); 
//				}
//				generativeInputNode.addResidentNodeChild(domainResidentNode); 
//				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
//			}			
			
			/* -> isInnerTermOf */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInnerTermOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();			
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				multiEntityNode = mapMultiEntityNode.get(individualTwo.getBrowserText()); 
				generativeInputNode.addInnerTermFromList(multiEntityNode); 
				multiEntityNode.addInnerTermOfList(generativeInputNode); 
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			}	
			
			/* hasProbDist don't checked */
			
		}		
	}
	
	private void loadOrdinaryVariable() throws IOMebnException{
		
		MFrag domainMFrag; 
		OrdinaryVariable oVariable; 		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 
		OWLObjectProperty objectProperty; 
		
		OWLNamedClass ordinaryVariablePr = owlModel.getOWLNamedClass(ORDINARY_VARIABLE); 
		instances = ordinaryVariablePr.getInstances(false); 
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();		
			oVariable = mapOVariable.get(individualOne.getBrowserText()); 
			if (oVariable == null){
				throw new IOMebnException(resource.getString("OVariableNotExistsInMTheory"),  individualOne.getBrowserText()); 
			}
			Debug.println("Ordinary Variable loaded: " + individualOne.getBrowserText()); 				
			
			oVariable.setDescription(getDescription(individualOne)); 
			
			/* -> isOVariableIn  */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isOVariableIn"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			individualTwo = (OWLIndividual) itAux.next();
			domainMFrag = mapDomainMFrag.get(individualTwo.getBrowserText()); 
			if(domainMFrag != oVariable.getMFrag()){
				throw new IOMebnException(resource.getString("isOVariableInError"),  individualOne.getBrowserText()); 
			}
			Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			
			/* -> isSubsBy */
			
			 objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isSubsBy"); 			
			 instances = individualOne.getPropertyValues(objectProperty); 	
			 itAux = instances.iterator();
			 if(itAux.hasNext()){
			     individualTwo = (OWLIndividual) itAux.next();
			     Type type = mebn.getTypeContainer().getType(individualTwo.getBrowserText()); 
			     if (type != null){
			    	 oVariable.setValueType(type); 
			    	 oVariable.updateLabel(); 
			     }
			     else{
			    	 //TODO Erro no arquivo Pr-OWL... 
			    	 
			     }
			 }
			
			/* isRepBySkolen don't checked */ 
			
		}		
	}
	
	/**
	 * 
	 * @throws IOMebnException
	 */
	private void loadArgRelationship() throws IOMebnException{
		
		OrdinaryVariable oVariable; 
		Argument argument;
		MultiEntityNode multiEntityNode; 	
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 
		
		OWLNamedClass argRelationshipPr = owlModel.getOWLNamedClass(ARGUMENT_RELATIONSHIP); 
		instances = argRelationshipPr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){	
			individualOne = (OWLIndividual)it.next();
			argument = mapArgument.get(individualOne.getBrowserText()); 
			
			if (argument == null){
				throw new IOMebnException(resource.getString("ArgumentNotFound"),  individualOne.getBrowserText()); 
			}
			
			Debug.println("-> ArgRelationship loaded: " + individualOne.getBrowserText()); 
			
			/* -> hasArgTerm  */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 			
			
			individualTwo = (OWLIndividual)individualOne.getPropertyValue(objectProperty); 	
			
			if(individualTwo != null){
				//TODO apenas por enquanto, pois n�o podera ser igual a null no futuro!!!
				
				/* check: 
				 * - node
				 * - entity //don't checked in this version
				 * - oVariable
				 * - skolen // don't checked in this version
				 */
				
				if ((multiEntityNode = mapMultiEntityNode.get(individualTwo.getBrowserText())) != null){
					try{
						if (multiEntityNode instanceof ResidentNode){
							argument.setArgumentTerm(multiEntityNode);
					        argument.setType(Argument.RESIDENT_NODE);  
						}else{
							if(multiEntityNode instanceof ContextNode){
								argument.setArgumentTerm(multiEntityNode); 
								argument.setType(Argument.CONTEXT_NODE); 
							}
						}
					}
					catch(Exception e){
						throw new IOMebnException(resource.getString("ArgumentTermError"),  individualTwo.getBrowserText()); 				   
					}
				}
				else{
					if( (oVariable = mapOVariable.get(individualTwo.getBrowserText())) != null) {
						try{
							argument.setOVariable(oVariable);
							argument.setType(Argument.ORDINARY_VARIABLE); 					
						}
						catch(Exception e){
							throw new IOMebnException(resource.getString("ArgumentTermError"),  individualTwo.getBrowserText()); 				   
						}
					}
					else{
						CategoricalStateEntity state; 
						if((state = mapCategoricalStates.get(individualTwo.getBrowserText())) != null){
					        argument.setEntityTerm(state); 	
					        argument.setType(Argument.ORDINARY_VARIABLE); 
						}
						else{
							if(individualTwo.getBrowserText().equals("true")){
								argument.setEntityTerm(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());
							    argument.setType(Argument.BOOLEAN_STATE); 
							}else{
								if(individualTwo.getBrowserText().equals("false")){
									argument.setEntityTerm(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());
								    argument.setType(Argument.BOOLEAN_STATE); 
								}else{
									if(individualTwo.getBrowserText().equals("absurd")){
										argument.setEntityTerm(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());
									    argument.setType(Argument.BOOLEAN_STATE); 
									}
								}
							}
								
						}
					}
				}
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
				
			}
			
			/* has Arg Number */
			OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
			Integer argNumber = (Integer)individualOne.getPropertyValue(hasArgNumber);
			if(argNumber != null){
			   argument.setArgNumber(argNumber);
			}
			
			/* -> isArgumentOf  */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isArgumentOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			individualTwo = (OWLIndividual) itAux.next();
			multiEntityNode = mapMultiEntityNode.get(individualTwo.getBrowserText()); 
			if (argument.getMultiEntityNode() != multiEntityNode){
				throw new IOMebnException(resource.getString("isArgumentOfError"),  individualTwo.getBrowserText()); 				   
			}
			Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());					
		}
				
	}
	
	private void loadSimpleArgRelationship() throws IOMebnException{
		
		OrdinaryVariable oVariable = null; 
		Argument argument;
		MultiEntityNode multiEntityNode; 	
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 		
		
		OWLNamedClass argRelationshipPr = owlModel.getOWLNamedClass(SIMPLE_ARGUMENT_RELATIONSHIP); 
		instances = argRelationshipPr.getInstances(false); 
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();
			argument = mapArgument.get(individualOne.getBrowserText()); 
			if (argument == null){
				throw new IOMebnException(resource.getString("ArgumentNotFound"),  individualOne.getBrowserText()); 
			}
			Debug.println("SimpleArgRelationship loaded: " + individualOne.getBrowserText()); 
			
			/* -> hasArgTerm  */
			
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			if(itAux.hasNext()){
				individualTwo = (OWLIndividual) itAux.next();
				
				oVariable = mapOVariable.get(individualTwo.getBrowserText()); 
				if (oVariable == null){
					throw new IOMebnException(resource.getString("ArgumentTermError"),  individualTwo.getBrowserText()); 	
				}
				
				try{
					argument.setOVariable(oVariable); 
				}
				catch(Exception e){
					throw new IOMebnException(resource.getString("ArgumentTermError"),  individualTwo.getBrowserText()); 	
				}
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			}
			
			/* -> hasArgNumber */
			
			OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty)owlModel.getOWLDatatypeProperty("hasArgNumber");
	        
			if (individualOne.getPropertyValue(hasArgNumber) != null){
			   argument.setArgNumber((Integer)individualOne.getPropertyValue(hasArgNumber));
			}
			
			/* -> isArgumentOf  */
			
//			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isArgumentOf"); 			
//			instances = individualOne.getPropertyValues(objectProperty); 	
//			itAux = instances.iterator();
//			individualTwo = (OWLIndividual) itAux.next();
//			multiEntityNode = mapMultiEntityNode.get(individualTwo.getBrowserText()); 
//			
//			if(multiEntityNode instanceof ResidentNode){
//				try{
//			       ((ResidentNode) multiEntityNode).addArgument(oVariable); 
//				}
//				catch(Exception e){
//					new InternalErrorDialog(); 
//					e.printStackTrace(); 
//				}
//			}
//			else{
//				if(multiEntityNode instanceof InputNode){					
//					
//				}
//			}
//			
//			if (argument.getMultiEntityNode() != multiEntityNode){
//				throw new IOMebnException(resource.getString("isArgumentOfError"),  individualTwo.getBrowserText()); 				   
//			}
//			
//			Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());					
		}		
	}
	
	/*
	 * Este mecanismo complexo eh necessario para que os argumentos sejam 
	 * inseridos no noh residente na mesma ordem em que foram salvos, permitindo
	 * manter a liga��o com os respectivos argumentos dos nos inputs instancias 
	 * destes... Eh ineficiente... merece uma atencao para otimiza��o posterior.
	 * (ps.: Funciona!) 
	 */
	private void ajustArgumentOfNodes(){
		
		for(ResidentNode resident: mapDomainResidentNode.values()){
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
		
		for(InputNode input: mapGenerativeInputNode.values()){
			
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
						Debug.println("Error: Arguemt " + argument.getName() 
								+ " do input " + input.getName() + " don't setted..."); 
						//TODO... problens when the arguments of the resident node aren't set... 
					}
					
				}
			}
			
		}
	}

	private void setFormulasOfContextNodes() {
		for(ContextNode context: this.listContextNode){
			context.setFormulaTree(buildFormulaTree(context)); 
		}
	}	
	
	/** 
	 * 
	 * @param contextNode
	 */
	
	private NodeFormulaTree buildFormulaTree(ContextNode contextNode){
		
		
		NodeFormulaTree nodeFormulaRoot; 
		NodeFormulaTree nodeFormulaChild; 
		
		nodeFormulaRoot = new NodeFormulaTree("formula", EnumType.FORMULA, 	EnumSubType.NOTHING, null);  
    	
		Debug.println("Entrou no build " +  contextNode.getName()); 
		
		/* 
		 * a raiz sera setada como o builtIn do qual o contextnode � instancia. 
		 * */
		
		Object obj = mapIsContextInstanceOf.get(contextNode); 
		
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
	 * Test that print the mtheory structure. 
	 */
	
	private void checkMTheory(){
		
		Debug.println("\n\n\n\n-------   Test Begin --------"); 
		
		Debug.println("-> MTheory: " + mebn.getName());
		
		List<MFrag> mFragList = mebn.getDomainMFragList(); 
		
		int desvio = 0; 
		
		for(MFrag mFrag: mFragList){
			
			desvio++; 
			
			Debug.println(printSpace(desvio,3) + "->MFrag: " + mFrag.getName());
			
			List<ContextNode> contextNodeList = mFrag.getContextNodeList(); 
			
			for(ContextNode contextNode: contextNodeList){
				
				desvio++; 
				Debug.println(printSpace(desvio,3) + "-> ContextNode: " + contextNode.getName());
				
				for(Argument argument: contextNode.getArgumentList()){
					desvio++; 
					Debug.println(printSpace(desvio,3) + "-> Argument: " + argument.getName());
					{
					   desvio++; 
						Debug.println(printSpace(desvio,3) + "- IsSimpleArg: " + argument.isSimpleArgRelationship());
					    if(!argument.isSimpleArgRelationship()){
					    	Debug.println(printSpace(desvio,3) + "- ArgTerm: " + argument.getArgumentTerm().getName() );
					    }
					   desvio--; 
					}
					desvio--; 
				}
				
				desvio--; 
			}
			
			desvio--; 
		}
		
		Debug.println("-------   Test End --------\n\n\n\n"); 			
	}
	
	private String printSpace(int numSpaces, int size){
		
		String stringSize = ""; 
		String stringReturn = "";
		
		for(int i = 0; i< size; i++){
			stringSize+= " "; 
		}
		
		for(int i = 0; i< numSpaces; i++){
			stringReturn+= stringSize; 
		}
		
		return stringReturn; 
	}

	/**
	 * @return Returns the ordinaryVarScopeSeparator.
	 */
	public String getOrdinaryVarScopeSeparator() {
		return ORDINARY_VAR_SCOPE_SEPARATOR;
	}
	
	
	/**
	 * Reads the OWL's ObjectEntity's individuals and fills the MEBN's OrdinaryVariableInstance
	 */
	private void loadObjectEntityIndividuals() throws TypeException {
		OWLNamedClass objectEntityClass = owlModel.getOWLNamedClass(OBJECT_ENTITY);	
		ObjectEntity mebnEntity = null;
		// TODO the for below has a dangerous unchecked class cast - solve it using more charming way
		for (OWLNamedClass subClass : (Collection<OWLNamedClass>)objectEntityClass.getSubclasses(true)) {
			 mebnEntity = this.mebn.getObjectEntityContainer().getObjectEntityByName(subClass.getName());
			// TODO the for below has a dangerous unchecked class cast - solve it using more charming way
			for (OWLIndividual individual : (Collection<OWLIndividual>)subClass.getInstances(false)) {
				// creates a object entity instance and adds it into the mebn entity container
				try {
					this.mebn.getObjectEntityContainer().addEntityInstance(mebnEntity.addInstance(individual.getName()));
				} catch (EntityInstanceAlreadyExistsException eiaee) {
					// Duplicated instance/individuals are not a major problem for now
					Debug.println("Duplicated instance/individual declaration found at OWL Loader");
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
	public void setOWLModelToUse(OWLModel model) {
		// this class should work only with internally loaded OWL models
		return;
	}
	
	/* LONG TASK BEGIN */

	private List<ILongTaskProgressObserver> observers = new ArrayList<ILongTaskProgressObserver>();
	
	public void registerObserver(ILongTaskProgressObserver observer) {
		observers.add(observer); 
	}

	public void removeObserver(ILongTaskProgressObserver observer) {
		observers.remove(observer); 
	}

	public void notityObservers(LongTaskProgressChangedEvent event) {
		for(ILongTaskProgressObserver observer: observers){
			observer.update(event); 
		}
	}
	
	protected int maxProgress = 100;
	
	public int getMaxProgress() {
		return maxProgress;
	}
	
	protected int currentProgress = 0;
	
	public int getCurrentProgress() {
		return currentProgress;
	}
	
	public int getPercentageDone() {
		return Math.round((float)currentProgress / maxProgress * 10000);
	}
	
	protected String currentProgressStatus = "";
	
	public String getCurrentProgressStatus() {
		return currentProgressStatus;
	}
	
	protected void updateProgress(int progress, String progressStatus){
		currentProgress = progress;
		currentProgressStatus = progressStatus;
		LongTaskProgressChangedEvent event = new LongTaskProgressChangedEvent(getCurrentProgressStatus(), getPercentageDone()); 
		notityObservers(event); 
	}
	
	protected void updateProgress(int progress){
		updateProgress(progress, ""); 
	}
	
	/* LONG TASK END */
}