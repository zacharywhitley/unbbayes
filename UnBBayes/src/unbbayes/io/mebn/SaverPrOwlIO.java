package unbbayes.io.mebn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.BooleanStatesEntity;
import unbbayes.prs.mebn.entity.CategoricalStatesEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.Type;

import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.repository.impl.LocalFileRepository;

public class SaverPrOwlIO {

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
	private HashMap<BuiltInRV, OWLIndividual> builtInRVMap = new HashMap<BuiltInRV, OWLIndividual>(); 
	
	private HashMap<String, OWLIndividual> mapMetaEntity = new HashMap<String,OWLIndividual>();
	private HashMap<Entity, OWLIndividual> mapCategoricalStates = new HashMap<Entity, OWLIndividual>();
	private HashMap<ObjectEntity, OWLNamedClass> mapObjectEntityClasses = new HashMap<ObjectEntity, OWLNamedClass>(); 
	private HashMap<BooleanStatesEntity, OWLIndividual> mapBooleanStatesEntity = new HashMap<BooleanStatesEntity, OWLIndividual>(); 
	
	private JenaOWLModel owlModel;	
	
	MultiEntityBayesianNetwork mebn; 
	
	/** Load resource file from this package */
	final ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.io.mebn.resources.IoMebnResources");
	
	
	/**
	 * Save the mebn structure in an file pr-owl. 
	 * @param nameFile: name of the file pr-owl where the mebn structure will be save
	 * @param mebn: the mebn structure
	 */

	public static final String PROWLMODELFILE = "pr-owl/pr-owl.owl"; 	
	
	public void saveMebn(File file, MultiEntityBayesianNetwork _mebn) throws IOException, IOMebnException{
		
	    mebn = _mebn; 
		owlModel = ProtegeOWL.createJenaOWLModel();
		
		loadPrOwlModel(owlModel); 

		/* Definitions */

		loadMetaEntities(); 
		loadCategoricalRVStates(); 			
		loadObjectEntitiesClasses(); 
		loadBooleanRVStates(); 
		loadBuiltInRV(); 
		
		/* MTheory */
		
		loadMTheory(); 
		loadDomainResidentNode(); 
		loadContextNode(); 
		loadGenerativeInputNode(); 
		
		/* saving */
		
		Collection errors = new ArrayList();
		owlModel.save(file.toURI(), FileUtils.langXMLAbbrev, errors);
		System.out.println("File saved with " + errors.size() + " errors.");
		
	}	
	
	private void loadPrOwlModel(JenaOWLModel owlModel)throws IOException, IOMebnException{
		File filePrOwl = new File(PROWLMODELFILE);
		owlModel.getRepositoryManager().addProjectRepository(new LocalFileRepository(filePrOwl, true));
	
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

		/* primeiro utiliza-se as metaEntities ja existentes no arquivo Pr-OWL */
				
		OWLNamedClass metaEntityClass = owlModel.getOWLNamedClass("MetaEntity"); 
		Collection instances = metaEntityClass.getInstances(false); 
		
		for(Object instance: instances){
			
			OWLIndividual metaEntityInstance = (OWLIndividual) instance;  
			mapMetaEntity.put(metaEntityInstance.getBrowserText(), metaEntityInstance); 
			
		}
		
		/* segundo passo: lista de meta entidades criadas pelo usuario */
		
		Set<String> listMetaEntities = Type.getListOfTypes(); 
		
		for(String state: listMetaEntities){

			if(!(mapMetaEntity.containsKey(state))){
				
				OWLIndividual stateIndividual = metaEntityClass.createOWLIndividual(state); 
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
		List<CategoricalStatesEntity> listStates = CategoricalStatesEntity.getListEntity(); 
		OWLIndividual typeOfState = mapMetaEntity.get("CategoryLabel"); 		
		
		for(CategoricalStatesEntity state: listStates){

			OWLIndividual stateIndividual = categoricalRVStatesClass.createOWLIndividual(state.getName()); 
			OWLObjectProperty hasType = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasType"); 	
			
			stateIndividual.addPropertyValue(hasType, typeOfState);
			this.mapCategoricalStates.put(state, stateIndividual); 
			
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
		
		for(Object instance : instances){
			OWLIndividual stateOwl = (OWLIndividual)instance; 
			if(stateOwl.getBrowserText().compareTo("true") == 0){
				this.mapBooleanStatesEntity.put(BooleanStatesEntity.getTrueStateEntity(), stateOwl); 
			}
			else{
				if(stateOwl.getBrowserText().compareTo("false") == 0){
					this.mapBooleanStatesEntity.put(BooleanStatesEntity.getFalseStateEntity(), stateOwl); 
				}
				else{
					if(stateOwl.getBrowserText().compareTo("absurd") == 0){
						this.mapBooleanStatesEntity.put(BooleanStatesEntity.getAbsurdStateEntity(), stateOwl); 
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
		
		OWLNamedClass entityClass = owlModel.getOWLNamedClass("ObjectEntity"); 
		List<ObjectEntity> listEntities = ObjectEntity.getListEntity(); 
		
		for(ObjectEntity entity: listEntities){
			
			OWLNamedClass newEntityClass = owlModel.createOWLNamedSubclass(entity.getName(), entityClass); 
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
	
    private void loadMTheory(){

		OWLNamedClass mTheoryClass = owlModel.getOWLNamedClass("MTheory"); 
		OWLIndividual mTheoryIndividual = mTheoryClass.createOWLIndividual(mebn.getName()); 
		
		/* hasMFrag */
		
		OWLObjectProperty hasMFragProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasMFrag"); 	
		List<DomainMFrag> listDomainMFrag = mebn.getDomainMFragList(); 
		
		for(DomainMFrag domainMFrag: listDomainMFrag){
			OWLNamedClass domainMFragClass = owlModel.getOWLNamedClass("Domain_MFrag"); 
			OWLIndividual domainMFragIndividual = domainMFragClass.createOWLIndividual(domainMFrag.getName());
			mTheoryIndividual.addPropertyValue(hasMFragProperty, domainMFragIndividual); 
			
			/* Proprierties of the Domain MFrag */
		
			System.out.println("Pt1!" + domainMFrag.getName()); 
							
			/* hasResidentNode */
			OWLObjectProperty hasResidentNodeProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasResidentNode"); 	
			List<DomainResidentNode> domainResidentNodeList = domainMFrag.getDomainResidentNodeList(); 
			for(DomainResidentNode residentNode: domainResidentNodeList){
				OWLNamedClass domainResClass = owlModel.getOWLNamedClass("Domain_Res"); 
				
				System.out.println("Nome = " + residentNode.getName());	
				
				
				OWLIndividual domainResIndividual = domainResClass.createOWLIndividual(residentNode.getName());
				
				
				domainMFragIndividual.addPropertyValue(hasResidentNodeProperty, domainResIndividual); 	
				
				System.out.println("Pt1.2!");		
				
				residentNodeListGeral.add(residentNode);
				domainResMap.put(residentNode, domainResIndividual); 
				nodeMap.put(residentNode, domainResIndividual);
			}	
			
			  System.out.println("Pt2!");
			/* hasInputNode */
			OWLObjectProperty hasInputNodeProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputNode"); 	
			List<GenerativeInputNode> generativeInputNodeList = domainMFrag.getGenerativeInputNodeList(); 
			for(GenerativeInputNode inputNode: generativeInputNodeList){
				OWLNamedClass generativeInputClass = owlModel.getOWLNamedClass("Generative_input"); 
				OWLIndividual generativeInputIndividual = generativeInputClass.createOWLIndividual(inputNode.getName());
				domainMFragIndividual.addPropertyValue(hasInputNodeProperty, generativeInputIndividual); 		
				
				inputNodeListGeral.add(inputNode);
				generativeInputMap.put(inputNode, generativeInputIndividual); 			
				nodeMap.put(inputNode, generativeInputIndividual);
			}				
			
			  System.out.println("Pt3!");
			
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
			
			  System.out.println("Pt4!");
			
			/* hasOVariable */
			OWLObjectProperty hasOVariableProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasOVariable"); 	
			List<OrdinaryVariable> oVariableList = domainMFrag.getOrdinaryVariableList(); 
			for(OrdinaryVariable oVariable: oVariableList){
				OWLNamedClass oVariableClass = owlModel.getOWLNamedClass("OVariable"); 
				OWLIndividual oVariableIndividual = oVariableClass.createOWLIndividual(oVariable.getName());
				domainMFragIndividual.addPropertyValue(hasOVariableProperty, oVariableIndividual); 		
				
				oVariableGeral.add(oVariable);
				oVariableMap.put(oVariable, oVariableIndividual); 				
			}				
			
			/* hasNode is automatic */
			
			/* hasSkolen don't implemented */
		}    	
    }
	
    private void loadDomainResidentNode(){

		System.out.println("Chegou ponto 1"); 
		
		for (DomainResidentNode residentNode: residentNodeListGeral){  
			OWLIndividual domainResIndividual = domainResMap.get(residentNode);	
			
			savePositionProperty(domainResIndividual, residentNode); 
			
			/* has Argument */
			OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
			List<Argument> argumentList = residentNode.getArgumentList(); 
			for(Argument argument: argumentList){
				if (!argument.isSimpleArgRelationship()){
					OWLNamedClass argumentClass = owlModel.getOWLNamedClass("ArgRelationship"); 
					OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(argument.getName());
					domainResIndividual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
					
					OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
					if(argument.getArgumentTerm() != null){
						MultiEntityNode node = argument.getArgumentTerm();
						OWLIndividual nodeIndividual = nodeMap.get(node); 
						argumentIndividual.addPropertyValue(hasArgTerm, nodeIndividual); 
					}		
				}
				else{
					OWLNamedClass argumentClass = owlModel.getOWLNamedClass("SimpleArgRelationship"); 
					OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(argument.getName());
					domainResIndividual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
					
					OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
					OrdinaryVariable oVariable = argument.getOVariable();
					OWLIndividual oVariableIndividual = oVariableMap.get(oVariable); 
					argumentIndividual.addPropertyValue(hasArgTerm, oVariableIndividual); 
					
				}
			}	
			
			/* has Parent */
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
			
			/* has Context Instance */
			
			/* has Inner Term */
			OWLObjectProperty hasInnerTermProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInnerTerm"); 	
			List<MultiEntityNode> innerTermList = residentNode.getInnerTermOfList(); 
			for(MultiEntityNode innerTerm: innerTermList){
				OWLIndividual innerTermIndividual = nodeMap.get(innerTerm);
				domainResIndividual.addPropertyValue(hasInnerTermProperty, innerTermIndividual);
			}		        
			
			/* has Input Instance */
			OWLObjectProperty hasInputInstanceProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputInstance"); 	
			List<GenerativeInputNode> inputInstanceList = residentNode.getInputInstanceFromList(); 
			for(GenerativeInputNode inputInstance: inputInstanceList){
				OWLIndividual inputInstanceIndividual = generativeInputMap.get(inputInstance);
				domainResIndividual.addPropertyValue(hasInputInstanceProperty, inputInstanceIndividual);
			}	
			
			saveHasPossibleValueProperty(domainResIndividual, residentNode); 
			
		}
		
		System.out.println("Chegou ponto 2");     	
    	
    }
    
    private void loadContextNode(){
    	
		for (ContextNode contextNode: contextListGeral){  
			OWLIndividual contextNodeIndividual = contextMap.get(contextNode);	
			
			savePositionProperty(contextNodeIndividual, contextNode);
			
			/* has Argument */
			OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
			List<Argument> argumentList = contextNode.getArgumentList(); 
			for(Argument argument: argumentList){
				if (!argument.isSimpleArgRelationship()){
					OWLNamedClass argumentClass = owlModel.getOWLNamedClass("ArgRelationship"); 
					OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(argument.getName());
					contextNodeIndividual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
					
					OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
					if(argument.getArgumentTerm() != null){
						MultiEntityNode node = argument.getArgumentTerm();
						OWLIndividual nodeIndividual = nodeMap.get(node); 
						argumentIndividual.addPropertyValue(hasArgTerm, nodeIndividual); 
					}
				}
				else{
					OWLNamedClass argumentClass = owlModel.getOWLNamedClass("SimpleArgRelationship"); 
					OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(argument.getName());
					contextNodeIndividual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
					
					OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
					OrdinaryVariable oVariable = argument.getOVariable();
					OWLIndividual oVariableIndividual = oVariableMap.get(oVariable); 
					argumentIndividual.addPropertyValue(hasArgTerm, oVariableIndividual); 
					
				}
			}	
			
			/* has Inner Term */
			OWLObjectProperty hasInnerTermProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInnerTerm"); 	
			List<MultiEntityNode> innerTermList = contextNode.getInnerTermOfList(); 
			for(MultiEntityNode innerTerm: innerTermList){
				OWLIndividual innerTermIndividual = nodeMap.get(innerTerm);
				contextNodeIndividual.addPropertyValue(hasInnerTermProperty, innerTermIndividual);
			}		        	        
			
			saveHasPossibleValuePropertyContext(contextNodeIndividual, contextNode); 
			
		}		
		
    }
    
    private void loadGenerativeInputNode(){
    	

		for (GenerativeInputNode generativeInputNode: inputNodeListGeral){  
			OWLIndividual generativeInputNodeIndividual = generativeInputMap.get(generativeInputNode);	
			
			savePositionProperty(generativeInputNodeIndividual, generativeInputNode);
			
			/* has Argument */
			
			OWLObjectProperty hasArgumentProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 	
			List<Argument> argumentList = generativeInputNode.getArgumentList(); 
			
			for(Argument argument: argumentList){
				if (!argument.isSimpleArgRelationship()){
					OWLNamedClass argumentClass = owlModel.getOWLNamedClass("ArgRelationship"); 
					OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(argument.getName());
					generativeInputNodeIndividual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
					
					OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
					if(argument.getArgumentTerm() != null){
						MultiEntityNode node = argument.getArgumentTerm();
						OWLIndividual nodeIndividual = nodeMap.get(node); 
						argumentIndividual.addPropertyValue(hasArgTerm, nodeIndividual); }
				}
				else{
					OWLNamedClass argumentClass = owlModel.getOWLNamedClass("SimpleArgRelationship"); 
					OWLIndividual argumentIndividual = argumentClass.createOWLIndividual(argument.getName());
					generativeInputNodeIndividual.addPropertyValue(hasArgumentProperty, argumentIndividual); 		
					
					OWLObjectProperty hasArgTerm = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 
					OrdinaryVariable oVariable = argument.getOVariable();
					OWLIndividual oVariableIndividual = oVariableMap.get(oVariable); 
					argumentIndividual.addPropertyValue(hasArgTerm, oVariableIndividual); 
				}
			}
			
			/* has Inner Term */
			OWLObjectProperty hasInnerTermProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInnerTerm"); 	
			List<MultiEntityNode> innerTermList = generativeInputNode.getInnerTermOfList(); 
			for(MultiEntityNode innerTerm: innerTermList){
				OWLIndividual innerTermIndividual = nodeMap.get(innerTerm);
				generativeInputNodeIndividual.addPropertyValue(hasInnerTermProperty, innerTermIndividual);
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
	
	
	
	private void saveHasPossibleValueProperty(OWLIndividual individual, MultiEntityNode node){
		/*has possible values */
		OWLObjectProperty hasPossibleValuesProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 	
		for(Entity possibleValue: node.getPossibleValueList()){
			individual.addPropertyValue(hasPossibleValuesProperty, this.mapCategoricalStates.get(possibleValue)); 
		}
	}
	
	private void saveHasPossibleValuePropertyContext(OWLIndividual individual, MultiEntityNode node){
		
		/*has possible values */
		OWLObjectProperty hasPossibleValuesProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 	
		
		for(Entity possibleValue: node.getPossibleValueList()){
			
			if(this.mapCategoricalStates.get(possibleValue) == null){
			   individual.addPropertyValue(hasPossibleValuesProperty, this.mapBooleanStatesEntity.get(possibleValue)); 	
			}
			else{
			   individual.addPropertyValue(hasPossibleValuesProperty, this.mapCategoricalStates.get(possibleValue)); 
			}
		}
		
	}	
	
	private void loadBuiltInRV(){

		/* BuiltInRV */
		/*
		builtInRVGeral = (ArrayList)mebn.getBuiltInRVList(); 
		OWLNamedClass builtInPr = owlModel.getOWLNamedClass("BuiltInRV"); 
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
		}*/
		
	}
	
}