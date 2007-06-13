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

import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.context.enumType;
import unbbayes.prs.mebn.entity.BooleanStatesEntity;
import unbbayes.prs.mebn.entity.CategoricalStatesEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.util.Debug;

import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.repository.impl.LocalFileRepository;

/**
 * Save the MEBN structure in a file pr-owl. 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 05/31/2007
 *
 */
public class SaverPrOwlIO {

	private HashMap<MFrag, OWLIndividual> mFragMap = new HashMap<MFrag, OWLIndividual>(); 
	private HashMap<MultiEntityNode, OWLIndividual> nodeMap = new HashMap<MultiEntityNode, OWLIndividual>(); 
	
	private ArrayList<DomainResidentNode> residentNodeListGeral = new ArrayList<DomainResidentNode>(); 
	private HashMap<DomainResidentNode, OWLIndividual> domainResMap = new HashMap<DomainResidentNode, OWLIndividual>(); 
	
	private ArrayList<GenerativeInputNode> inputNodeListGeral = new ArrayList<GenerativeInputNode>(); 
	private HashMap<GenerativeInputNode, OWLIndividual> generativeInputMap = new HashMap<GenerativeInputNode, OWLIndividual>();
	
	private ArrayList<ContextNode> contextListGeral = new ArrayList<ContextNode>(); 
	private HashMap<ContextNode, OWLIndividual> contextMap = new HashMap<ContextNode, OWLIndividual>();
	
	private ArrayList<OrdinaryVariable> oVariableGeral = new ArrayList<OrdinaryVariable>(); 
	private HashMap<OrdinaryVariable, OWLIndividual> oVariableMap = new HashMap<OrdinaryVariable, OWLIndividual>();
	
	private ArrayList<BuiltInRV> builtInRVGeral = new ArrayList<BuiltInRV>(); 
	private ArrayList<OWLIndividual> builtInRVIndividualList = new ArrayList<OWLIndividual>(); 
	
	private HashMap<String, OWLIndividual> mapMetaEntity = new HashMap<String,OWLIndividual>();
	private HashMap<Entity, OWLIndividual> mapCategoricalStates = new HashMap<Entity, OWLIndividual>();
	private HashMap<ObjectEntity, OWLNamedClass> mapObjectEntityClasses = new HashMap<ObjectEntity, OWLNamedClass>(); 
	private HashMap<BooleanStatesEntity, OWLIndividual> mapBooleanStatesEntity = new HashMap<BooleanStatesEntity, OWLIndividual>(); 
	
	private ArrayList<ContextNode> auxContextNodeList = new ArrayList<ContextNode>();  
	
	private JenaOWLModel owlModel;	
	
	private File file; 
	
	private String ordinaryVarScopeSeparator = ".";
	private String possibleValueScopeSeparator = ".";	
	
	MultiEntityBayesianNetwork mebn; 
	
	/** Load resource file from this package */
	final ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.io.mebn.resources.IoMebnResources");
	

	private static final String PROWLMODELFILE = "pr-owl/pr-owl.owl"; 	

	/**
	 * Save the mebn structure in an file pr-owl. 
	 * @param nameFile: name of the file pr-owl where the mebn structure will be save
	 * @param mebn: the mebn structure
	 */
	
	public void saveMebn(File file, MultiEntityBayesianNetwork _mebn) throws IOException, IOMebnException{
		Debug.setDebug(true);
	    mebn = _mebn; 
		owlModel = ProtegeOWL.createJenaOWLModel();
		
		
		
		loadPrOwlModel(owlModel); 
		Debug.println("-> Model pr-owl load sucess ");	

		/* Definitions */
	
		loadMetaEntities();
		
		/* 
		 * Os estados categoricos estao sendo salvos a partir do n� residente
		 * que os cria (isto esta sendo necessario porque o possivel estado esta 
		 * sendo salco com o nome modificado, de forma a ser precedido pelo nome
		 * do noh residente do qual ele eh um possivel estado).  
		 */
		
		//loadCategoricalRVStates(); 			
		
		loadObjectEntitiesClasses(); 
		loadBooleanRVStates(); 
		loadBuiltInRV();  
		
		Debug.println("-> Definitions load sucess ");
		
		/* MTheory */
		
		loadMTheory(); 
		Debug.println("-> MTheory load sucess ");
		loadDomainResidentNode(); 
		Debug.println("-> Domain Resident Nodes load sucess ");		
		loadContextNode(); 
		Debug.println("-> Context Node load sucess ");		
		loadGenerativeInputNode(); 
		Debug.println("-> Generative Input Node load sucess ");		
		
		clearAuxiliaryLists(); 
		
		/* saving */
		
		Collection<String> errors = new ArrayList();
		owlModel.save(file.toURI(), FileUtils.langXMLAbbrev, errors);
		
		Debug.println("File saved with " + errors.size() + " errors.");
		for(String error: errors){
			Debug.println(""); 
		}
		Debug.println("\n"); 
		Debug.setDebug(false);
	}	
	
	private void loadPrOwlModel(JenaOWLModel owlModel)throws IOException, IOMebnException{
		
		File filePrOwl = new File(PROWLMODELFILE);
		owlModel.getRepositoryManager().addProjectRepository(new LocalFileRepository(filePrOwl, false));
	
		FileInputStream inputStreamOwl; 
		
		inputStreamOwl = new FileInputStream(PROWLMODELFILE); 
		
		try{
			owlModel.load(inputStreamOwl, FileUtils.langXMLAbbrev);   
		}
		catch (Exception e){
			throw new IOMebnException(resource.getString("ModelCreationError")); 
		}			
	}
	
	
	/**
	 * Load MetaEntities for the MTheory and build the structure for
	 * save this. 
	 * Fill the mapMetaEntity with the MetaEntities of the 
	 * MTheory and with the MetaEntities default of the pr-owl. 
	 */
	
	private void loadMetaEntities(){

		ArrayList<String> metaEntitiesDefault = new ArrayList<String>(); 
		
		/* primeiro utiliza-se as metaEntities ja existentes no arquivo Pr-OWL */
				
		OWLNamedClass metaEntityClass = owlModel.getOWLNamedClass("MetaEntity"); 
		Collection instances = metaEntityClass.getInstances(false); 
		
		OWLIndividual rootLabel = null;	// stores "TypeLabel" individual, which is root
		
		for(Object instance: instances){
			
			OWLIndividual metaEntityInstance = (OWLIndividual) instance;  
			mapMetaEntity.put(metaEntityInstance.getBrowserText(), metaEntityInstance); 
			metaEntitiesDefault.add(metaEntityInstance.getBrowserText());
			
			// Extract the extreme super-type label (the root type label)
			if (metaEntityInstance.getName().compareTo("TypeLabel") == 0) {
				rootLabel = metaEntityInstance;
			}
			
		}
		
		/* segundo passo: lista de meta entidades criadas pelo usuario */
		
		List<String> listMetaEntities = mebn.getTypeContainer().getTypesNames(); 
		
		boolean present;
		
		for(String state: listMetaEntities){

			/* necessario para evitar dupla inclusao */
			present = false; 
			for(String type: metaEntitiesDefault){
				if(type.compareTo(state) == 0){
					present = true; 
					break; 
				}
			}
			
			if(present == false){
			   OWLIndividual stateIndividual = metaEntityClass.createOWLIndividual(state); 
			   
			   // Set user-defined meta-entity's superclass to TypeLabel
			   if (rootLabel != null) {
				   stateIndividual.addPropertyValue(owlModel.getOWLObjectProperty("hasType"),rootLabel);
			   }
			   
			   // Since isTypeOf is inverse of hasType, it is set automatically
			   
			   mapMetaEntity.put(state, stateIndividual); 					
			}
		}	
	}
	
	/** 
	 * Load the CategoricalRVStates from the MEBN structure (<CategoricalStatesEntity>)
	 * to the OWL structure. 
	 * Fill the <mapCategoricalStates> with the CategoricalRVStates of the MEBN structure. 
	 */
	private void loadCategoricalRVStates(){

		/* categoricalRVStates */
		OWLNamedClass categoricalRVStatesClass = owlModel.getOWLNamedClass("CategoricalRVStates"); 
		List<CategoricalStatesEntity> listStates = mebn.getCategoricalStatesEntityContainer().getListEntity(); 
		OWLIndividual typeOfState = mapMetaEntity.get("CategoryLabel"); 		
		OWLObjectProperty hasType = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasType"); 	
		
		for(CategoricalStatesEntity state: listStates){
			
			OWLIndividual stateIndividual = categoricalRVStatesClass.createOWLIndividual(state.getName()); 
			stateIndividual.addPropertyValue(hasType, typeOfState);
			mapCategoricalStates.put(state, stateIndividual); 
		}

	}

	/**
	 * Load the categorical RV states of one resident node to the saver structure. 
	 * (Load both categorical states and boolean states)
	 * 
	 * @param residentNodeIndividual Individual that is the node in the PowerLoom structure. 
	 * @param node Resident Node of the MEBN structure
	 */
	private void loadPossibleValues(OWLIndividual residentNodeIndividual, ResidentNode node){

		/* categoricalRVStates */
		OWLNamedClass categoricalRVStatesClass = owlModel.getOWLNamedClass("CategoricalRVStates"); 
		List<Entity> listStates = node.getPossibleValueList(); 
		OWLIndividual categoryLabel = mapMetaEntity.get("CategoryLabel"); 		
		OWLObjectProperty hasType = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasType"); 	
		
		OWLObjectProperty hasPossibleValues = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 	
		
		for(Entity state: listStates){
			if(state instanceof CategoricalStatesEntity){
				//Name = nodeName.categoricalEntityName 
				String name = node.getName() + possibleValueScopeSeparator + state.getName(); 
				OWLIndividual stateIndividual = categoricalRVStatesClass.createOWLIndividual(name); 
				stateIndividual.addPropertyValue(hasType, categoryLabel);
				mapCategoricalStates.put(state, stateIndividual); 
				
				residentNodeIndividual.addPropertyValue(hasPossibleValues, stateIndividual);
			}
			else{
				residentNodeIndividual.addPropertyValue(hasPossibleValues, this.mapBooleanStatesEntity.get(state)); 
			}
		}

	}
	
	/**
	 * Map the boolean RV States of the PrOwl with the boolean RV States
	 * of the Mebn structure (<BooleanStatesEntity>). 
	 * Fill the <mapBooleanStatesEntity>  
	 */
	
	private void loadBooleanRVStates(){
		
		OWLNamedClass booleanRVStates = owlModel.getOWLNamedClass("BooleanRVStates"); 
	
		Collection instances = booleanRVStates.getInstances(false); 
		
		/*
		 * � assumido que o arquivo PR-OWL contem os tipos booleanos (todos e apenas estes)
		 * - true
		 * - false
		 * - absurde
		 */
		
		for(Object instance : instances){
			OWLIndividual stateOwl = (OWLIndividual)instance; 
			if(stateOwl.getBrowserText().compareTo("true") == 0){
				this.mapBooleanStatesEntity.put(mebn.getBooleanStatesEntityContainer().getTrueStateEntity(), stateOwl); 
			}
			else{
				if(stateOwl.getBrowserText().compareTo("false") == 0){
					this.mapBooleanStatesEntity.put(mebn.getBooleanStatesEntityContainer().getFalseStateEntity(), stateOwl); 
				}
				else{
					if(stateOwl.getBrowserText().compareTo("absurd") == 0){
						this.mapBooleanStatesEntity.put(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity(), stateOwl); 
					}					
				}
			}
			
		}
		
	}
	
	/**
	 * Load the <ObjectEntity> from the MEBN structure to the OWL structure. 
	 * Fill the <mapObjectEntity> with the Object Entities of the MEBN structure. 
	 *
	 */
	private void loadObjectEntitiesClasses(){
		
		String labelSuffix = "_Label";
		
		OWLNamedClass entityClass = owlModel.getOWLNamedClass("ObjectEntity"); 
		List<ObjectEntity> listEntities = mebn.getObjectEntityContainer().getListEntity(); 
		
		// Prepare to Get the correspondent meta entity and assign proper hasType property
		OWLNamedClass metaEntity = owlModel.getOWLNamedClass("MetaEntity");
		OWLObjectProperty hasType = owlModel.getOWLObjectProperty("hasType");
		
		for(ObjectEntity entity: listEntities){
			
			OWLNamedClass newEntityClass = owlModel.createOWLNamedSubclass(entity.getName(), entityClass); 
			
//			// Grants all individuals of ObjEntity classes has its right types (<Name of entity><TypeSuffix>. Ex: EX1_Label)
//			if ((hasType != null) && (metaEntity != null)) {
//				
//				// use map to search proper meta entity for that object entity, and adds restriction
//				newEntityClass.addSuperclass( owlModel.createOWLHasValue(hasType , mapMetaEntity.get(newEntityClass.getName() + mebn.getTypeContainer().getLabelSuffix())));
//								
//				//	Search proper meta entity and add restriction
//				
//				for (Iterator iter = metaEntity.getInstances(false).iterator(); iter.hasNext();) {
//					OWLIndividual element = (OWLIndividual) iter.next();
//					if (element.getName().equals(newEntityClass.getName() + mebn.getTypeContainer().getLabelSuffix())) {
//						newEntityClass.addSuperclass(owlModel.createOWLHasValue(hasType,element));
//						break;
//					}
//				}
//				
//			}
			
			mapObjectEntityClasses.put(entity, newEntityClass); 
		}
		
	}
	
	private BuiltInRV findBuiltInByName(List<BuiltInRV> builtInRVList, String name){
		for(BuiltInRV builtInRV: builtInRVList){
	        if (builtInRV.getName().compareTo(name) == 0){
	        	return builtInRV; 
	        }
		}
		return null; 
	}
	
	private OWLIndividual findBuiltInIndividualByName(String name){
		
		for(OWLIndividual individual: this.builtInRVIndividualList){
	        if (individual.getBrowserText().compareTo(name) == 0){
	        	return individual; 
	        }
		}
		return null;
		
		//TODO lan?¿œar excess?¿œo... 
		
	}
		
	
    private void loadMTheory(){

		OWLNamedClass mTheoryClass = owlModel.getOWLNamedClass("MTheory"); 
		OWLIndividual mTheoryIndividual = mTheoryClass.createOWLIndividual(mebn.getName()); 
		
		/* hasMFrag */
		
		OWLObjectProperty hasMFragProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasMFrag"); 	
		List<DomainMFrag> listDomainMFrag = mebn.getDomainMFragList(); 
		
		for(DomainMFrag domainMFrag: listDomainMFrag){
			OWLNamedClass domainMFragClass = owlModel.getOWLNamedClass("Domain_MFrag"); 
			OWLIndividual domainMFragIndividual = domainMFragClass.createOWLIndividual(domainMFrag.getName());
			this.mFragMap.put(domainMFrag, domainMFragIndividual); 
			mTheoryIndividual.addPropertyValue(hasMFragProperty, domainMFragIndividual); 
			
			/* Proprierties of the Domain MFrag */
							
			/* hasResidentNode */
			OWLObjectProperty hasResidentNodeProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasResidentNode"); 	
			List<DomainResidentNode> domainResidentNodeList = domainMFrag.getDomainResidentNodeList(); 
			for(DomainResidentNode residentNode: domainResidentNodeList){
				OWLNamedClass domainResClass = owlModel.getOWLNamedClass("Domain_Res"); 
				
				Debug.println("Nome = " + residentNode.getName());	
				
				
				OWLIndividual domainResIndividual = domainResClass.createOWLIndividual(residentNode.getName());
				
				
				domainMFragIndividual.addPropertyValue(hasResidentNodeProperty, domainResIndividual); 	
				
				residentNodeListGeral.add(residentNode);
				domainResMap.put(residentNode, domainResIndividual); 
				nodeMap.put(residentNode, domainResIndividual);
			}	
			
			/* hasInputNode */
			OWLObjectProperty hasInputNodeProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputNode"); 	
			List<GenerativeInputNode> generativeInputNodeList = domainMFrag.getGenerativeInputNodeList(); 
			for(GenerativeInputNode inputNode: generativeInputNodeList){
				Debug.println("input node name = " + inputNode.getName());	
				
				OWLNamedClass generativeInputClass = owlModel.getOWLNamedClass("Generative_input"); 
				OWLIndividual generativeInputIndividual = generativeInputClass.createOWLIndividual(inputNode.getName());
				domainMFragIndividual.addPropertyValue(hasInputNodeProperty, generativeInputIndividual); 		
				
				inputNodeListGeral.add(inputNode);
				generativeInputMap.put(inputNode, generativeInputIndividual); 			
				nodeMap.put(inputNode, generativeInputIndividual);
			}				
			
			/* hasContextNode */
			OWLObjectProperty hasContextNodeProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasContextNode"); 	
			List<ContextNode> contextNodeList = domainMFrag.getContextNodeList(); 
			for(ContextNode contextNode: contextNodeList){
				
				OWLNamedClass contextClass = owlModel.getOWLNamedClass("Context"); 
				OWLIndividual contextIndividual = contextClass.createOWLIndividual(contextNode.getName());
				domainMFragIndividual.addPropertyValue(hasContextNodeProperty, contextIndividual); 									
				
				contextListGeral.add(contextNode);
				contextMap.put(contextNode, contextIndividual); 			
				nodeMap.put(contextNode, contextIndividual);
				
			}				
			
			/* hasOVariable */
			OWLObjectProperty hasOVariableProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasOVariable"); 	
			List<OrdinaryVariable> oVariableList = domainMFrag.getOrdinaryVariableList(); 

			
			OWLNamedClass oVariableClass = owlModel.getOWLNamedClass("OVariable"); 
			OWLObjectProperty isSubsByProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isSubsBy"); 	
			
			for(OrdinaryVariable oVariable: oVariableList){
				// Set variable name as "MFragName.OVName"
				OWLIndividual oVariableIndividual = oVariableClass.createOWLIndividual(
								  oVariable.getMFrag().getName() + this.getOrdinaryVarScopeSeparator() 
								+ oVariable.getName() );
				domainMFragIndividual.addPropertyValue(hasOVariableProperty, oVariableIndividual); 		
				
				for(String type: mebn.getTypeContainer().getTypesNames()){
					if(type.compareTo(oVariable.getValueType().getName()) == 0){
						domainMFragIndividual.addPropertyValue(isSubsByProperty, mapMetaEntity.get(type)); 
						break; 
					}
				}
				
				oVariableGeral.add(oVariable);
				oVariableMap.put(oVariable, oVariableIndividual); 				
			}				
			
			/* hasNode is automatic */
			
			/* hasSkolen don't implemented */
		}    	
    }
	
    private void loadDomainResidentNode(){
    	Debug.println("loadDomainResidentNode");
		for (DomainResidentNode residentNode: residentNodeListGeral){  
			OWLIndividual domainResIndividual = domainResMap.get(residentNode);	
			
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
			
			List<DomainResidentNode> residentNodeFatherList = residentNode.getResidentNodeFatherList(); 	        
			for(DomainResidentNode residentNodeFather: residentNodeFatherList){
				OWLIndividual residentNodeFatherIndividual = domainResMap.get(residentNodeFather); 
				domainResIndividual.addPropertyValue(hasParentProperty, residentNodeFatherIndividual);
			}
			
			List<GenerativeInputNode> inputNodeFatherList = residentNode.getInputNodeFatherList(); 
			for(GenerativeInputNode inputNodeFather: inputNodeFatherList){
				OWLIndividual inputNodeFatherIndividual = generativeInputMap.get(inputNodeFather); 
				domainResIndividual.addPropertyValue(hasParentProperty, inputNodeFatherIndividual);
			}		
			
			/* has possible values */
			Debug.println("Verifying possible values");
			loadPossibleValues(domainResIndividual, residentNode); 
			
			/* has Context Instance */
			
			/* has Inner Term */
			Debug.println("Verifying inner terms");
			OWLObjectProperty hasInnerTermProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInnerTerm"); 	
			List<MultiEntityNode> innerTermList = residentNode.getInnerTermOfList(); 
			for(MultiEntityNode innerTerm: innerTermList){
				OWLIndividual innerTermIndividual = nodeMap.get(innerTerm);
				domainResIndividual.addPropertyValue(hasInnerTermProperty, innerTermIndividual);
			}		        
			
			/* has Input Instance */
			Debug.println("Verifying input instances");
			OWLObjectProperty hasInputInstanceProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputInstance"); 	
			List<GenerativeInputNode> inputInstanceList = residentNode.getInputInstanceFromList(); 
			for(GenerativeInputNode inputInstance: inputInstanceList){
				Debug.println("Read input: " + inputInstance.getName());
				OWLIndividual inputInstanceIndividual = generativeInputMap.get(inputInstance);
				if (inputInstanceIndividual != null) {
					domainResIndividual.addPropertyValue(hasInputInstanceProperty, inputInstanceIndividual);
				}
				
			}	
			
			saveHasPossibleValueProperty(domainResIndividual, residentNode); 
			
			/* hasProbDist */
			Debug.println("Verifying probability distros");
			OWLObjectProperty hasProbDist = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasProbDist");
			OWLNamedClass declarativeDist = owlModel.getOWLNamedClass("DeclarativeDist"); 
			OWLIndividual declarativeDistThisNode = declarativeDist.createOWLIndividual(residentNode.getName() + "_table"); 
			OWLDatatypeProperty hasDeclaration = owlModel.getOWLDatatypeProperty("hasDeclaration"); 
			if(residentNode.getTableFunction() != null){
			   declarativeDistThisNode.addPropertyValue(hasDeclaration, residentNode.getTableFunction()); 
			   domainResIndividual.addPropertyValue(hasProbDist, declarativeDistThisNode); 
			}
		} 	
    	
    }
    
    /**
     * Save one simple argument relationship of a node. A simple argument is only
     * fill with a ordinary variable. 
     * 
     * @param argument the ordinary variable that is the argument (null is accept)
     * @param individual The individual where this is a argument
     * @param name The name of the node where this is a argument
     * @param argNumber The number of this argument in the list of arguments of the node.
     */
    private void saveSimpleArgRelationship(OrdinaryVariable argument, OWLIndividual individual, String name, int argNumber){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass("SimpleArgRelationship"); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(name + "_" + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
		if(argument != null){
		   OWLIndividual oVariableIndividual = oVariableMap.get(argument); 
		   argumentIndividual.addPropertyValue(hasArgTerm, oVariableIndividual); 
		}
		
			
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
     * 04 - The inner term is settend how inner term of the individual (original context node)
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
    private void saveResidentNodeArgRelationship(ResidentNodePointer argument, OWLIndividual individual, MultiEntityNode node, int argNumber){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass("ArgRelationship"); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(node.getName() + "_" + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
		argumentIndividual.setPropertyValue(hasArgNumber, argNumber);
		
		OWLNamedClass contextNodeClass = owlModel.getOWLNamedClass("Context"); 
		OWLIndividual innerContextNode = contextNodeClass.createOWLIndividual(node.getName()  + "_" + argNumber + "_inner"); 
		
		OWLObjectProperty isInnerTermOf = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInnerTermOf"); 	
		innerContextNode.addPropertyValue(isInnerTermOf, individual); 
		
		OWLObjectProperty isContextNodeIn = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextNodeIn"); 	
		innerContextNode.addPropertyValue(isContextNodeIn, mFragMap.get(node.getMFrag())); 
		
		OWLObjectProperty isContextInstanceOf = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextInstanceOf"); 	
		innerContextNode.addPropertyValue(isContextInstanceOf, domainResMap.get(argument.getResidentNode())); 
		
		//Save the possible values
		saveHasPossibleValueProperty(innerContextNode, argument.getResidentNode()); 
		
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
     * Save a categirucak state how one Arg Relationship of a node. 
     *
     * @param argument
     * @param individual
     * @param name
     * @param argNumber
     */
    private void saveCategoricalStateArgRelationship(CategoricalStatesEntity argument, OWLIndividual individual, String name, int argNumber){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass("ArgRelationship"); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(name + "_" + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
		if(argument != null){
		   OWLIndividual categoricalStateIndividual = mapCategoricalStates.get(argument); 
		   argumentIndividual.addPropertyValue(hasArgTerm, categoricalStateIndividual); 
		}
			
		OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
		argumentIndividual.setPropertyValue(hasArgNumber, argNumber);
		
    }
    
    private void saveBuiltInArgRelationship(OWLIndividual individual, MultiEntityNode node, int argNumber, NodeFormulaTree root){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass("ArgRelationship"); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(node.getName() + "_" + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
		argumentIndividual.setPropertyValue(hasArgNumber, argNumber);
		
		OWLNamedClass contextNodeClass = owlModel.getOWLNamedClass("Context"); 
		String innerContextName = node.getName() + "_" + argNumber + "_inner"; 
		OWLIndividual innerContextNode = contextNodeClass.createOWLIndividual(innerContextName); 
		
		ContextNode contextAux = new ContextNode(innerContextName, (DomainMFrag)node.getMFrag()); 
		auxContextNodeList.add(contextAux); 
		
		OWLObjectProperty isInnerTermOf = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInnerTermOf"); 	
		innerContextNode.addPropertyValue(isInnerTermOf, individual); 
		
		OWLObjectProperty isContextNodeIn = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextNodeIn"); 	
		innerContextNode.addPropertyValue(isContextNodeIn, mFragMap.get(node.getMFrag())); 
		
		loadContextNodeFormula(root, innerContextNode, contextAux); 
		
		OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
		argumentIndividual.addPropertyValue(hasArgTerm, innerContextNode); 
		
    }       
    
    /**
     * Save a SimpleArgRelationship without ordinary variable setted. 
     * 
     * @param individual
     * @param name The name of the node that has the argument. 
     * @param argNumber
     */
    private void saveEmptySimpleArgRelationship(OWLIndividual individual, String name, int argNumber){
    	
    	OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
		OWLNamedClass argumentClass = owlModel.getOWLNamedClass("SimpleArgRelationship"); 
		OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(name + "_" + argNumber);
		individual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
			
		OWLDatatypeProperty hasArgNumber = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasArgNumber");
		argumentIndividual.setPropertyValue(hasArgNumber, argNumber);
		
    }
    
    /**
     * Load the context nodes from the MEBN structure for the PR-OWL structure. 
     */
    private void loadContextNode(){
    	
		for (ContextNode contextNode: contextListGeral){
			
			Debug.println("Saving Context: " + contextNode.getName()); 
			
			OWLIndividual contextNodeIndividual = contextMap.get(contextNode);	
			NodeFormulaTree formulaNode = contextNode.getFormulaTree(); 
			if (formulaNode != null){
				loadContextNodeFormula(formulaNode, contextNodeIndividual, contextNode); 
			}		
			saveHasPossibleValueProperty(contextNodeIndividual, contextNode); 
		}		
		
    }
    
    /**
     * Create the PR-OWL structure for the formula of a context node. 
     * 
     * @param _formulaNode Root of the tree of the formula
     * @param contextNodeIndividual Protege individual for the context node
     * @param contextNode Context node from the MEBN structure
     */
    private void loadContextNodeFormula(NodeFormulaTree _formulaNode, OWLIndividual contextNodeIndividual, ContextNode contextNode){
    	
    	NodeFormulaTree formulaNode = _formulaNode; 
    	
    	if((formulaNode.getTypeNode() == enumType.SIMPLE_OPERATOR)||
				(formulaNode.getTypeNode() == enumType.QUANTIFIER_OPERATOR)){
			
			if(formulaNode.getNodeVariable() instanceof BuiltInRV){
				
				//Step 1: Built-In
				OWLObjectProperty isContextInstanceOf = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextInstanceOf");
				OWLIndividual builtInIndividual = findBuiltInIndividualByName(((BuiltInRV)(formulaNode.getNodeVariable())).getName()); 
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
					if(child.getTypeNode() == enumType.OPERANDO){
						
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
								//wait for the complet model. 
							}
							break; 
						
						case ENTITY:
							if(child.getNodeVariable() instanceof CategoricalStatesEntity){
								saveCategoricalStateArgRelationship((CategoricalStatesEntity)child.getNodeVariable(), contextNodeIndividual, contextNode.getName(), argNumber); 
							}
							else{
								//TODO save normal entities instances?
								//wait for the complet model. 
							}
							break; 
							
						default: 
							saveEmptySimpleArgRelationship(contextNodeIndividual, contextNode.getName(), argNumber );
							break; 
						
						}
						
					}
					else{
						if(child.getTypeNode() == enumType.SIMPLE_OPERATOR) {
							this.saveBuiltInArgRelationship(contextNodeIndividual, contextNode, argNumber, child );
							
						}
						else{
							if(child.getTypeNode() == enumType.QUANTIFIER_OPERATOR){
								this.saveBuiltInArgRelationship(contextNodeIndividual, contextNode, argNumber, child );
							}
							else{
								//TODO avaliar outros casos... 
								this.saveEmptySimpleArgRelationship(contextNodeIndividual, contextNode.getName(), argNumber );
							}
						}
					}
					
				}
				
			}
			else{ //don't is a built-in... 
				//TODO levantar excessao
			}
								
		}
		
		else{ //don't is a enumType.OPERANDO
			
		}
	
	
    	
    }
    
    /**
     * 
     * Pre-conditions: the method loadDomainResidentNode shoud run before. 
     */
    private void loadGenerativeInputNode(){
    	
		for (GenerativeInputNode generativeInputNode: inputNodeListGeral){  
			OWLIndividual generativeInputNodeIndividual = generativeInputMap.get(generativeInputNode);	
			
			/* has Argument */
			if (generativeInputNode.getInputInstanceOf() != null){
				if(generativeInputNode.getInputInstanceOf() instanceof DomainResidentNode){
				     ResidentNodePointer pointer = generativeInputNode.getResidentNodePointer(); 
				     OrdinaryVariable[] ovArray = pointer.getOrdinaryVariableArray(); 
				     for(int i = 0; i < ovArray.length; i++){
				    	 saveSimpleArgRelationship(ovArray[i], 
				    			 generativeInputNodeIndividual, generativeInputNode.getName(), i); 
				     }
				}
			}
			
//			OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
//			List<Argument> argumentList = generativeInputNode.getArgumentList(); 
//			for(Argument argument: argumentList){
//				if (!argument.isSimpleArgRelationship()){
//					OWLNamedClass argumentClass = owlModel.getOWLNamedClass("ArgRelationship"); 
//					OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(argument.getName());
//					generativeInputNodeIndividual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
//					
//					OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
//					if(argument.getArgumentTerm() != null){
//						MultiEntityNode node = argument.getArgumentTerm();
//						OWLIndividual nodeIndividual = nodeMap.get(node); 
//						argumentIndividual.addPropertyValue(hasArgTerm, nodeIndividual); }
//				}
//				else{
//					OWLNamedClass argumentClass = owlModel.getOWLNamedClass("SimpleArgRelationship"); 
//					OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(argument.getName());
//					generativeInputNodeIndividual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
//					
//					OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
//					OrdinaryVariable oVariable = argument.getOVariable();
//					OWLIndividual oVariableIndividual = oVariableMap.get(oVariable); 
//					argumentIndividual.addPropertyValue(hasArgTerm, oVariableIndividual); 
//				}
//			}
			
			/* has Inner Term */
			OWLObjectProperty hasInnerTermProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInnerTerm"); 	
			List<MultiEntityNode> innerTermList = generativeInputNode.getInnerTermOfList(); 
			for(MultiEntityNode innerTerm: innerTermList){
				OWLIndividual innerTermIndividual = nodeMap.get(innerTerm);
				generativeInputNodeIndividual.addPropertyValue(hasInnerTermProperty, innerTermIndividual);
			}
			
			/* has Possible Values */
			
			if (generativeInputNode.getInputInstanceOf() != null){
				if(generativeInputNode.getInputInstanceOf() instanceof DomainResidentNode){
					DomainResidentNode residentNode = (DomainResidentNode)generativeInputNode.getInputInstanceOf(); 
					for(Entity state: residentNode.getPossibleValueList()){
						//Pre conditions shoud be true... 
						OWLObjectProperty hasPossibleValues = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 	
						generativeInputNodeIndividual.addPropertyValue(hasPossibleValues, mapCategoricalStates.get(state)); 
					}
				}
				else{
					// Built-in don't checked... 
				}
			}
		}
    	
    }
    
	private void savePositionProperty(OWLIndividual individual, MultiEntityNode node){
		
		/* has PositionX */
		OWLDatatypeProperty hasPositionXProperty = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasPositionX");
		individual.setPropertyValue(hasPositionXProperty, (float)node.getPosition().getX());
		
		/* has PositionY */
		OWLDatatypeProperty hasPositionYProperty = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasPositionY");
		individual.setPropertyValue(hasPositionYProperty, (float)node.getPosition().getY());
	
	}
	
	/**
	 * Save the possible values of a node. 
	 * 
	 * @param individual The individual owl for the node
	 * @param node The node that have the possible values
	 */
	private void saveHasPossibleValueProperty(OWLIndividual individual, MultiEntityNode node){
		/*has possible values */
		OWLObjectProperty hasPossibleValuesProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 	
		for(Entity possibleValue: node.getPossibleValueList()){
			if(possibleValue instanceof CategoricalStatesEntity)
			individual.addPropertyValue(hasPossibleValuesProperty, this.mapCategoricalStates.get(possibleValue)); 
			else{ //boolean states entity
				individual.addPropertyValue(hasPossibleValuesProperty, this.mapBooleanStatesEntity.get(possibleValue)); 
			}
		}
	}
	
//	
//	private void saveHasPossibleValuePropertyContext(OWLIndividual individual, MultiEntityNode node){
//		
//		/*has possible values */
//		OWLObjectProperty hasPossibleValuesProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 	
//		
//		for(Entity possibleValue: node.getPossibleValueList()){
//			
//			if(this.mapCategoricalStates.get(possibleValue) == null){
//			   individual.addPropertyValue(hasPossibleValuesProperty, this.mapBooleanStatesEntity.get(possibleValue)); 	
//			}
//			else{
//			   individual.addPropertyValue(hasPossibleValuesProperty, this.mapCategoricalStates.get(possibleValue)); 
//			}
//		}
//		
//	}	
//	
	private void loadBuiltInRV(){

		/* BuiltInRV */
		
		builtInRVGeral = (ArrayList)mebn.getBuiltInRVList();
		
		OWLNamedClass builtInPr = owlModel.getOWLNamedClass("BuiltInRV"); 
		
		Collection instances = builtInPr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			OWLIndividual individualOne = (OWLIndividual)it.next();
		    builtInRVIndividualList.add(individualOne);
		}
		
		
		/*
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			OWLIndividual individualOne = (OWLIndividual)it.next();
			BuiltInRV builtInRVTest = findBuiltInByName(builtInRVGeral, individualOne.getBrowserText());
		    if(builtInRVTest != null){
			    builtInRVMap.put(builtInRVTest, individualOne);
		        
			    List<GenerativeInputNode> inputInstanceFromList = builtInRVTest.getInputInstanceFromList();
				OWLObjectProperty hasInputInstance = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputInstance"); 
				for(GenerativeInputNode inputInstance: inputInstanceFromList)		{
					OWLIndividual generativeInputNodeIndividual = generativeInputMap.get(inputInstance);	
					individualOne.addPropertyValue(hasInputInstance, generativeInputNodeIndividual); 
				}
			    
				
			    List<ContextNode> contextInstanceFromList = builtInRVTest.getContextFromList(); 
				OWLObjectProperty hasContextInstance = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasContextInstance"); 
				for(ContextNode contextInstance: contextInstanceFromList)		{
					OWLIndividual contextNodeIndividual = generativeInputMap.get(contextInstance);	
					individualOne.addPropertyValue(hasContextInstance, contextNodeIndividual); 
				}
			    
		    }
		}
		*/
		
	}
	
	private void clearAuxiliaryLists(){
		
		for(ContextNode context: this.auxContextNodeList){
			context.delete(); 
		}
		
	}

	/**
	 * @return Returns the ordinaryVarScopeSeparator.
	 */
	public String getOrdinaryVarScopeSeparator() {
		return ordinaryVarScopeSeparator;
	}

	/**
	 * @param ordinaryVarScopeSeparator The ordinaryVarScopeSeparator to set.
	 */
	public void setOrdinaryVarScopeSeparator(String ordinaryVarScopeSeparator) {
		this.ordinaryVarScopeSeparator = ordinaryVarScopeSeparator;
	}
	
}