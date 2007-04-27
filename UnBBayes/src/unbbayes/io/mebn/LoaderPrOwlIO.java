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

import javax.swing.tree.DefaultMutableTreeNode;

import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVExists;
import unbbayes.prs.mebn.builtInRV.BuiltInRVForAll;
import unbbayes.prs.mebn.builtInRV.BuiltInRVIff;
import unbbayes.prs.mebn.builtInRV.BuiltInRVImplies;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.builtInRV.BuiltInRVOr;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.context.enumSubType;
import unbbayes.prs.mebn.context.enumType;
import unbbayes.prs.mebn.entity.BooleanStatesEntity;
import unbbayes.prs.mebn.entity.CategoricalStatesEntity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;

import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.repository.impl.LocalFileRepository;

public class LoaderPrOwlIO {

	/* MEBN Structure */ 
	
	private MultiEntityBayesianNetwork mebn = null;
	
	private Collection instances; 
	private Iterator itAux; 
	
	private HashMap<String, DomainMFrag> mapDomainMFrag = new HashMap<String, DomainMFrag>(); 
	private HashMap<String, OrdinaryVariable> mapOVariable = new HashMap<String, OrdinaryVariable>();
	
	/* 
	 * the first contains the context nodes of the MTheory while the second contains 
	 * the context nodes inner terms (exists only in the pr-owl format)
	 */
	private HashMap<String, ContextNode> mapContextNode = new HashMap<String, ContextNode>();
	private HashMap<String, ContextNode> mapContextNodeInner = new HashMap<String, ContextNode>();
	private List<ContextNode> listContextNode = new ArrayList<ContextNode>(); 
	
	/*
	 * 
	 */
	private HashMap<ContextNode, Object> mapIsContextInstanceOf = new HashMap<ContextNode, Object>(); 

	
	private HashMap<String, DomainResidentNode> mapDomainResidentNode = new HashMap<String, DomainResidentNode>();
	private HashMap<String, GenerativeInputNode> mapGenerativeInputNode = new HashMap<String, GenerativeInputNode>();
	private HashMap<String, Argument> mapArgument = new HashMap<String, Argument>();
	private HashMap<String, MultiEntityNode> mapMultiEntityNode = new HashMap<String, MultiEntityNode>(); 
	private HashMap<String, BuiltInRV> mapBuiltInRV = new HashMap<String, BuiltInRV>(); 
	private HashMap<String, ObjectEntity> mapObjectEntityEntity = new HashMap<String, ObjectEntity>(); 	
	
	/* Protege API Structure */
	
	private JenaOWLModel owlModel;  
	
	/** Load resource file from this package */
	final ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.io.mebn.resources.IoMebnResources");	
	
	private static final String PROWLMODELFILE = "pr-owl/pr-owl.owl";
	private static final String URIPROWLMODELFILE = "file:///UnBBayes_/UnBBayes/pr-owl/pr-owl.owl"; 
	
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException, IOMebnException{


		owlModel = ProtegeOWL.createJenaOWLModel();
		
		//URI uri = URIUtilities.createURI(URIPROWLMODELFILE);
		
		File filePrOwl = new File(PROWLMODELFILE);
		FileInputStream inputStreamOwl = new FileInputStream(filePrOwl); 
		
		owlModel.getRepositoryManager().addProjectRepository(new LocalFileRepository(filePrOwl, true));
		
		
		try{
			owlModel.load(inputStreamOwl, FileUtils.langXMLAbbrev);
			System.out.println("load do arquivo modelo com sucesso!"); 
		}	
		catch(Exception e){
			e.printStackTrace(); 
		}
		
			
		FileInputStream inputStream = new FileInputStream(file); 
		
		try{
			owlModel.load(inputStream, FileUtils.langXMLAbbrev);   
		}
		catch (Exception e){
			throw new IOMebnException("Erro ao tentar ler o arquivo!!! " + file.getAbsolutePath()); 
		}
		
		/* Build the owl model */
		/*
		FileInputStream inputStream; 
		
		inputStream = new FileInputStream(file); 
		
		
		try{
			System.out.println("File: " + file.getAbsolutePath()); 
			owlModel.load(inputStream, FileUtils.langXMLAbbrev);   
		}
		catch (Exception e){
			throw new IOMebnException("Problemas ao tentar ler o arquivo" + file.getName()); 
		}
		*/
		
		/*------------------- MTheory -------------------*/
		mebn = this.loadMTheoryClass(); 
		
		/*------------------- Entities -------------------*/
		
		//loadMetaEntitiesClasses(); 
		//loadBooleanRVStates(); 
		loadCategoricalRVStates(); 
		loadObjectEntity(); 
		
		/*-------------------MTheory elements------------*/
		loadDomainMFrag(); 
		loadBuiltInRV(); 
		loadContextNode(); 	
		loadDomainResidentNode(); 	
		loadGenerativeInputNode(); 	
		
		/*---------------------Arguments-------------------*/
		loadOrdinaryVariable(); 	
		loadArgRelationship(); 		
		loadSimpleRelationship(); 
		
		for(ContextNode context: this.listContextNode){
			context.setFormulaTree(buildFormulaTree(context)); 
		}
		
		checkMTheory(); 
		
		System.out.println("Load concluido com sucesso!"); 
		
		return mebn; 		
	}	
	
	private void loadPrOwlModel(JenaOWLModel owlModel)throws IOException, IOMebnException{
		
		
		/*
		File filePrOwl = new File(PROWLMODELFILE);
		
		LocalFileRepository repository = new LocalFileRepository(filePrOwl, true); 
		repository.setForceReadOnly(true); 
		
		owlModel.getRepositoryManager().addProjectRepository(repository);
		
		FileInputStream inputStreamOwl; 
		
		inputStreamOwl = new FileInputStream(filePrOwl); 
		
		try{
			
			owlModel.load(inputStreamOwl, FileUtils.langXMLAbbrev);   
		}
		catch (Exception e){
			throw new IOMebnException(resource.getString("ModelCreationError")); 
		}	
		*/
	}
	
	private MultiEntityBayesianNetwork loadMTheoryClass() throws IOMebnException {
        
		MultiEntityBayesianNetwork mebn; 
		DomainMFrag domainMFrag; 		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 
		OWLNamedClass owlNamedClass; 	
		OWLObjectProperty objectProperty; 
		
		owlNamedClass = owlModel.getOWLNamedClass("MTheory"); 
		
		instances = owlNamedClass.getInstances(false); 
		itAux = instances.iterator(); 
		
		if(!itAux.hasNext()){
			throw new IOMebnException(resource.getString("MTheoryNotExist")); 
		}
		
		individualOne = (OWLIndividual) itAux.next();
		mebn = new MultiEntityBayesianNetwork(individualOne.getBrowserText()); 
		
		System.out.println("MTheory loaded: " + individualOne.getBrowserText()); 
		
		//Properties 
		
		/* hasMFrag */
		/* cria todas as MFrags existentes na MTheory e armazena estas no mapDomainMFrag */
		objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasMFrag"); 	
		instances = individualOne.getPropertyValues(objectProperty); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualTwo = (OWLIndividual) it.next();
			System.out.println("hasDomainMFrag: " + individualTwo.getBrowserText()); 
			domainMFrag = new DomainMFrag(individualTwo.getBrowserText(), mebn); 
			mebn.addDomainMFrag(domainMFrag); 
			mapDomainMFrag.put(individualTwo.getBrowserText(), domainMFrag); 
		}	

		return mebn; 
	}
	
	private void loadMetaEntitiesClasses(){
		
		OWLNamedClass metaEntityClass; 
		
		Collection instances; 
		
		OWLIndividual individualOne;
		
		metaEntityClass = owlModel.getOWLNamedClass("MetaEntity");
		
		instances = metaEntityClass.getInstances(false); 
		
		for (Object owlIndividual : instances){
			individualOne = (OWLIndividual) owlIndividual; 
			
			try{
			   Type.addType(individualOne.getBrowserText()); 
			}
			catch (TypeAlreadyExistsException exception){
				//OK... lembre-se que os tipos basicos já existem... 
			}
			
			System.out.println("Meta Entity Loaded: " + individualOne.getBrowserText()); 
						
		}		
	}
	
	/**
	 * Load the categorical RV states of the file, create the 
	 * CategoricalStatesEntity objects and add to the static list
	 * inside the CategoricalStatesEntity class. 
	 */
	private void loadCategoricalRVStates(){
		
		OWLNamedClass metaEntityClass; 
		
		Collection instances; 
		
		OWLIndividual individualOne;
		
		metaEntityClass = owlModel.getOWLNamedClass("CategoricalRVStates");
		
		instances = metaEntityClass.getInstances(false); 
		
		for (Object owlIndividual : instances){
			
			individualOne = (OWLIndividual) owlIndividual; 
			
			CategoricalStatesEntity categoricalStatesEntity = CategoricalStatesEntity.createCategoricalEntity(individualOne.getBrowserText()); 
			
			System.out.println("Categorical State Loaded: " + individualOne.getBrowserText()); 
						
		}			
	}
	
	/**
	 * load the Object Entity of the file
	 *
	 */
	private void loadObjectEntity(){
    
        OWLNamedClass objectEntityClass; 
		Collection subClasses; 
		OWLNamedClass subClass; 
		OWLObjectProperty objectProperty; 		
		
		objectEntityClass = owlModel.getOWLNamedClass("ObjectEntity");
		
		subClasses = objectEntityClass.getSubclasses(true); 
		
		for (Object owlClass : subClasses){
			
			subClass = (OWLNamedClass)owlClass; 
			
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasType");

			//TODO melhorar isto!!!
			try{
				Type.addType(subClass.getBrowserText() + "_Type"); 
				ObjectEntity objectEntityMebn = new ObjectEntity(subClass.getBrowserText(), subClass.getBrowserText() + "_Type"); 	
			}
			catch(TypeException typeException){
				
			}
		}		

	}
	
	
	private void loadDomainMFrag() throws IOMebnException{

		DomainMFrag domainMFrag; 
		OrdinaryVariable oVariable; 
		ContextNode contextNode; 
		DomainResidentNode domainResidentNode; 
		GenerativeInputNode generativeInputNode; 
		BuiltInRV builtInRV;		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 
		OWLNamedClass owlNamedClass; 	
		OWLObjectProperty objectProperty; 
		
		owlNamedClass = owlModel.getOWLNamedClass("Domain_MFrag"); 
		instances = owlNamedClass.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();
			domainMFrag = mapDomainMFrag.get(individualOne.getBrowserText()); 
			if (domainMFrag == null){
				throw new IOMebnException(resource.getString("DomainMFragNotExistsInMTheory"), individualOne.getBrowserText()); 
			}
			
			System.out.println("DomainMFrag loaded: " + individualOne.getBrowserText()); 
			
			/* -> hasResidentNode */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasResidentNode"); 
			instances = individualOne.getPropertyValues(objectProperty); 
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				domainResidentNode = new DomainResidentNode(individualTwo.getBrowserText(), domainMFrag); 
				domainMFrag.addDomainResidentNode(domainResidentNode); 
				mapDomainResidentNode.put(individualTwo.getBrowserText(), domainResidentNode); 
				mapMultiEntityNode.put(individualTwo.getBrowserText(), domainResidentNode); 
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}	
			
			/* -> hasInputNode */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputNode"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				generativeInputNode = new GenerativeInputNode(individualTwo.getBrowserText(), domainMFrag); 
				domainMFrag.addGenerativeInputNode(generativeInputNode); 
				mapGenerativeInputNode.put(individualTwo.getBrowserText(), generativeInputNode); 
				mapMultiEntityNode.put(individualTwo.getBrowserText(), generativeInputNode); 				
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}	
			
			/* -> hasContextNode */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasContextNode"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				contextNode = new ContextNode(individualTwo.getBrowserText(), domainMFrag); 
				domainMFrag.addContextNode(contextNode); 
				mapContextNode.put(individualTwo.getBrowserText(), contextNode); 
				mapMultiEntityNode.put(individualTwo.getBrowserText(), contextNode); 				
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}	
			
			/* -> hasOVariable */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasOVariable"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				oVariable = new OrdinaryVariable(individualTwo.getBrowserText(), Type.getDefaultType(), domainMFrag); 
				domainMFrag.addOrdinaryVariable(oVariable); 
				mapOVariable.put(individualTwo.getBrowserText(), oVariable); 
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}
			
			/* -> hasSkolen don't checked! */
			
		}						
	}
	
	private void loadContextNode() throws IOMebnException{

		DomainMFrag domainMFrag; 
		ContextNode contextNode; 
		Argument argument;
		MultiEntityNode multiEntityNode; 	
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 
		
		OWLNamedClass contextNodePr = owlModel.getOWLNamedClass("Context"); 
		instances = contextNodePr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();
			contextNode = mapContextNode.get(individualOne.getBrowserText()); 
			if (contextNode == null){
				throw new IOMebnException(resource.getString("ContextNodeNotExistsInMTheory"), individualOne.getBrowserText()); 
			}
			
			System.out.println("Context Node loaded: " + individualOne.getBrowserText()); 				
			
			loadHasPositionProperty(individualOne, contextNode); 
			
			System.out.println("Domain Resident loaded: " + individualOne.getBrowserText()); 			
			
			/* -> isContextNodeIn  */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isContextNodeIn"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			individualTwo = (OWLIndividual) itAux.next();
			domainMFrag = mapDomainMFrag.get(individualTwo.getBrowserText()); 
			if(domainMFrag.containsContextNode(contextNode) == false){
				throw new IOMebnException(resource.getString("ContextNodeNotExistsInMFrag"), contextNode.getName() + ", " + domainMFrag.getName()); 
			}
			System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			
			/* -> isNodeFrom */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isNodeFrom"); 			
			instances = individualOne.getPropertyValues(objectProperty);		
			for(Iterator itIn = instances.iterator(); itIn.hasNext();  ){
				individualTwo = (OWLIndividual) itAux.next();
				domainMFrag = mapDomainMFrag.get(individualTwo.getBrowserText()); 
				if(domainMFrag.containsNode(contextNode) == false){
					throw new IOMebnException(resource.getString("NodeNotExistsInMFrag"), contextNode.getName() + ", " + domainMFrag.getName()); 
				}
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());				
			}
			
			/* -> hasArgument */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();			
				argument = new Argument(individualTwo.getBrowserText(), contextNode); 
				contextNode.addArgument(argument); 
				mapArgument.put(individualTwo.getBrowserText(), argument); 
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
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
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
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
					System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
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

		GenerativeInputNode generativeInputNode; 
		BuiltInRV builtInRV;		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 
		
		OWLNamedClass builtInPr = owlModel.getOWLNamedClass("BuiltInRV"); 
		instances = builtInPr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();
			
			String nameBuiltIn = individualOne.getBrowserText(); 
			
			if(nameBuiltIn.compareTo("and") == 0){
				builtInRV = new BuiltInRVAnd(); 
			}else
				if(nameBuiltIn.compareTo("or") == 0){
					builtInRV = new BuiltInRVOr(); 
				}else
					if(nameBuiltIn.compareTo("equalto") == 0){
						builtInRV = new BuiltInRVEqualTo(); 
					}else
						if(nameBuiltIn.compareTo("exists") == 0){
							builtInRV = new BuiltInRVExists(); 
						}else
							if(nameBuiltIn.compareTo("forall") == 0){
								builtInRV = new BuiltInRVForAll(); 
							}else
								if(nameBuiltIn.compareTo("not") == 0){
									builtInRV = new BuiltInRVNot(); 
								}else
									if(nameBuiltIn.compareTo("iff") == 0){
										builtInRV = new BuiltInRVIff(); 
									}else								
										if(nameBuiltIn.compareTo("implies") == 0){
											builtInRV = new BuiltInRVImplies(); 
										}else{
											//TODO lançar excessão... 
											builtInRV = new BuiltInRV(individualOne.getBrowserText(), " "); 											
										}	
			
			mebn.addBuiltInRVList(builtInRV); 
			mapBuiltInRV.put(individualOne.getBrowserText(), builtInRV); 
			System.out.println("BuiltInRV loaded: " + individualOne.getBrowserText()); 				
			
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
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}				
			
		}						
	}
	
	private void loadDomainResidentNode() throws IOMebnException{

		DomainMFrag domainMFrag; 
		DomainResidentNode domainResidentNode; 
		GenerativeInputNode generativeInputNode; 
		Argument argument;
		MultiEntityNode multiEntityNode; 	
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 		
		
		OWLNamedClass domainResidentNodePr = owlModel.getOWLNamedClass("Domain_Res"); 
		instances = domainResidentNodePr.getInstances(false); 
		DomainMFrag mFragOfNode = null; 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			
			individualOne = (OWLIndividual)it.next();
			domainResidentNode = mapDomainResidentNode.get(individualOne.getBrowserText()); 
			if (domainResidentNode == null){
				throw new IOMebnException(resource.getString("DomainResidentNotExistsInMTheory"), individualOne.getBrowserText() ); 
			}
			
			loadHasPositionProperty(individualOne, domainResidentNode); 
			
			System.out.println("Domain Resident loaded: " + individualOne.getBrowserText()); 			
			
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
			System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			
			/* -> hasArgument */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgument"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				argument = new Argument(individualTwo.getBrowserText(), domainResidentNode); 
				domainResidentNode.addArgument(argument); 
				mapArgument.put(individualTwo.getBrowserText(), argument); 
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}		
			
			/* -> hasParent */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasParent"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				if (mapDomainResidentNode.containsKey(individualTwo.getBrowserText())){
					DomainResidentNode aux = mapDomainResidentNode.get(individualTwo.getBrowserText()); 
					aux.addResidentNodeChild(domainResidentNode); 
					
					Edge auxEdge = new Edge(aux, domainResidentNode);
					try{
					mFragOfNode.addEdge(auxEdge); 
					}
					catch(Exception e){
						System.out.println("Erro: arco invalidop!!!"); 
					}
				}
				else{
					if (mapGenerativeInputNode.containsKey(individualTwo.getBrowserText())){
						GenerativeInputNode aux = mapGenerativeInputNode.get(individualTwo.getBrowserText()); 
						aux.addResidentNodeChild(domainResidentNode); 
			
						Edge auxEdge = new Edge(aux, domainResidentNode);
						try{
						mFragOfNode.addEdge(auxEdge); 
						}
						catch(Exception e){
							System.out.println("Erro: arco invalidop!!!"); 
						}
					
					}
					else{
						throw new IOMebnException(resource.getString("NodeNotFound"), individualTwo.getBrowserText() ); 
					}
				}
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}	
			
			/* -> hasInputInstance  */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasInputInstance"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				generativeInputNode = mapGenerativeInputNode.get(individualTwo.getBrowserText()); 
				generativeInputNode.setInputInstanceOf(domainResidentNode); 
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
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
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			}				

			/* -> hasPossibleValues */
			{
				CategoricalStatesEntity state; 
				objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 			
				instances = individualOne.getPropertyValues(objectProperty); 	
				itAux = instances.iterator();
				for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
					individualTwo = (OWLIndividual) itIn.next();
					try{
					   String stateName = individualTwo.getBrowserText(); 
					   /* case 1: booleans states */
					   if(stateName.compareTo("true")==0){
						   domainResidentNode.addPossibleValue(BooleanStatesEntity.getTrueStateEntity());   
					   }
					   else{
						   if(stateName.compareTo("false") == 0){
							   domainResidentNode.addPossibleValue(BooleanStatesEntity.getFalseStateEntity());   						   
						   }
						   else{
							   if(stateName.compareTo("absurd") == 0){
								   domainResidentNode.addPossibleValue(BooleanStatesEntity.getAbsurdStateEntity());   							   
							   }
							   else{
								   /* case 2: categorical states */
								      state = CategoricalStatesEntity.getCategoricalState(individualTwo.getBrowserText()) ; 
								      domainResidentNode.addPossibleValue(state);    
							   }
						   }
					   }
				
					}
					catch(CategoricalStateDoesNotExistException e){
						throw new IOMebnException(resource.getString("CategoricalStateNotFoundException"), individualTwo.getBrowserText() ); 						
					}
				}
			}
			
			/* hasProbDist don't checked */
			
			OWLObjectProperty hasProbDist = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasProbDist");
			OWLNamedClass declarativeDist = owlModel.getOWLNamedClass("DeclarativeDist"); 
			Collection declarativeDistList = declarativeDist.getInstances(true); 
			
			for(Object instance : declarativeDistList){

				OWLIndividual declarativeDistThisNode = (OWLIndividual)instance; 
				OWLDatatypeProperty hasDeclaration = owlModel.getOWLDatatypeProperty("hasDeclaration"); 
				String table = (String)declarativeDistThisNode.getPropertyValue(hasDeclaration); 
				domainResidentNode.setTableFunction(table); 
				
			}
			
			/* -> hasPossibleValues */
			
			/* hasContextInstance don't checked */
			
			/* isArgTermIn don't checked */
		}
				
	}
	
	private void loadGenerativeInputNode() throws IOMebnException{
	    
		DomainResidentNode domainResidentNode; 
		GenerativeInputNode generativeInputNode; 
		Argument argument;
		MultiEntityNode multiEntityNode; 
		BuiltInRV builtInRV;		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 		
		
		OWLNamedClass inputNodePr = owlModel.getOWLNamedClass("Generative_input"); 
		instances = inputNodePr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			
			individualOne = (OWLIndividual)it.next();
			System.out.println("  - Input Node loaded: " + individualOne.getBrowserText()); 			
			generativeInputNode = mapGenerativeInputNode.get(individualOne.getBrowserText()); 
			if (generativeInputNode == null){
				throw new IOMebnException(resource.getString("GenerativeInputNodeNotExistsInMTheory"), individualOne.getBrowserText() ); 				
			}
			
			loadHasPositionProperty(individualOne, generativeInputNode); 
			
			/* -> isInputInstanceOf  */
			
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInputInstanceOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			
			if(itAux.hasNext() != false){
				individualTwo = (OWLIndividual) itAux.next();
				
				if (mapDomainResidentNode.containsKey(individualTwo.getBrowserText())){
					domainResidentNode = mapDomainResidentNode.get(individualTwo.getBrowserText()); 
					generativeInputNode.setInputInstanceOf(domainResidentNode); 
					System.out.println("   - isInputInstanceOf " + domainResidentNode.getName()); 
				}
				else{
					if (mapBuiltInRV.containsKey(individualTwo.getBrowserText())){
						builtInRV = mapBuiltInRV.get(individualTwo.getBrowserText()); 
						generativeInputNode.setInputInstanceOf(builtInRV); 
						System.out.println("   - isInputInstanceOf " + builtInRV.getName()); 
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
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}		
			
			/* isParentOf */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isParentOf"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				domainResidentNode = mapDomainResidentNode.get(individualTwo.getBrowserText());
				if(domainResidentNode == null){
					throw new IOMebnException(resource.getString("DomainMFragNotExistsInMTheory"),  individualTwo.getBrowserText()); 
				}
				generativeInputNode.addResidentNodeChild(domainResidentNode); 
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}			
			
			/* -> isInnerTermOf */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInnerTermOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();			
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				multiEntityNode = mapMultiEntityNode.get(individualTwo.getBrowserText()); 
				generativeInputNode.addInnerTermFromList(multiEntityNode); 
				multiEntityNode.addInnerTermOfList(generativeInputNode); 
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			}	
			
			/* hasProbDist don't checked */
			
		}		
	}
	
	private void loadOrdinaryVariable() throws IOMebnException{
		
		DomainMFrag domainMFrag; 
		OrdinaryVariable oVariable; 		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 
		OWLObjectProperty objectProperty; 
		
		OWLNamedClass ordinaryVariablePr = owlModel.getOWLNamedClass("OVariable"); 
		instances = ordinaryVariablePr.getInstances(false); 
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();		
			oVariable = mapOVariable.get(individualOne.getBrowserText()); 
			if (oVariable == null){
				throw new IOMebnException(resource.getString("OVariableNotExistsInMTheory"),  individualOne.getBrowserText()); 
			}
			System.out.println("Ordinary Variable loaded: " + individualOne.getBrowserText()); 				
			
			/* -> isOVariableIn  */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isOVariableIn"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			individualTwo = (OWLIndividual) itAux.next();
			domainMFrag = mapDomainMFrag.get(individualTwo.getBrowserText()); 
			if(domainMFrag != oVariable.getMFrag()){
				throw new IOMebnException(resource.getString("isOVariableInError"),  individualOne.getBrowserText()); 
			}
			System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			
			/* -> isSubsBy */
			
			 objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isSubsBy"); 			
			 instances = individualOne.getPropertyValues(objectProperty); 	
			 itAux = instances.iterator();
			 if(itAux.hasNext()){
			     individualTwo = (OWLIndividual) itAux.next();
			     if (Type.hasType(individualTwo.getBrowserText())){
			    	 oVariable.setValueType(individualTwo.getBrowserText()); 
			     }
			 }
			
			/* isRepBySkolen don't checked */ 
			
		}		
	}
	
	private void loadArgRelationship() throws IOMebnException{
		
		OrdinaryVariable oVariable; 
		Argument argument;
		MultiEntityNode multiEntityNode; 	
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 
		
		OWLNamedClass argRelationshipPr = owlModel.getOWLNamedClass("ArgRelationship"); 
		instances = argRelationshipPr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){	
			individualOne = (OWLIndividual)it.next();
			argument = mapArgument.get(individualOne.getBrowserText()); 
			
			if (argument == null){
				throw new IOMebnException(resource.getString("ArgumentNotFound"),  individualOne.getBrowserText()); 
			}
			
			System.out.println("-> ArgRelationship loaded: " + individualOne.getBrowserText()); 
			
			/* -> hasArgTerm  */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasArgTerm"); 			
			
			individualTwo = (OWLIndividual)individualOne.getPropertyValue(objectProperty); 	
			
			if(individualTwo != null){
				//TODO apenas por enquanto, pois não podera ser igual a null no futuro!!!
				
				/* check: 
				 * - node
				 * - entity //don't checked in this version
				 * - oVariable
				 * - skolen // don't checked in this version
				 */
				
				if ((multiEntityNode = mapMultiEntityNode.get(individualTwo.getBrowserText())) != null){
					try{
						argument.setArgumentTerm(multiEntityNode);
					}
					catch(Exception e){
						throw new IOMebnException(resource.getString("ArgumentTermError"),  individualTwo.getBrowserText()); 				   
					}
				}
				else{
					if( (oVariable = mapOVariable.get(individualTwo.getBrowserText())) != null) {
						try{
							argument.setOVariable(oVariable);
							
							//ResidentNodes: 
							Node node = argument.getMultiEntityNode(); 
							if (node instanceof ResidentNode){
								((ResidentNode)node).addArgument(oVariable); 
							}
							
						}
						catch(Exception e){
							throw new IOMebnException(resource.getString("ArgumentTermError"),  individualTwo.getBrowserText()); 				   
						}
					}
					else{
						/* TODO Tratamento para Entity */
					}
				}
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
				
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
			System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());					
		}
				
	}
	
	private void loadSimpleRelationship() throws IOMebnException{
		
		OrdinaryVariable oVariable = null; 
		Argument argument;
		MultiEntityNode multiEntityNode; 	
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 		
		
		OWLNamedClass argRelationshipPr = owlModel.getOWLNamedClass("SimpleArgRelationship"); 
		instances = argRelationshipPr.getInstances(false); 
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();
			argument = mapArgument.get(individualOne.getBrowserText()); 
			if (argument == null){
				throw new IOMebnException(resource.getString("ArgumentNotFound"),  individualOne.getBrowserText()); 
			}
			System.out.println("SimpleArgRelationship loaded: " + individualOne.getBrowserText()); 
			
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
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			}
			
			/* -> isArgumentOf  */
			
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isArgumentOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			individualTwo = (OWLIndividual) itAux.next();
			multiEntityNode = mapMultiEntityNode.get(individualTwo.getBrowserText()); 
			
			if(multiEntityNode instanceof ResidentNode){
				try{
			       ((ResidentNode) multiEntityNode).addArgument(oVariable); 
				}
				catch(Exception e){
					e.printStackTrace(); 
				}
			}
			
			if (argument.getMultiEntityNode() != multiEntityNode){
				throw new IOMebnException(resource.getString("isArgumentOfError"),  individualTwo.getBrowserText()); 				   
			}
			
			System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());					
		}		
	}
	
	private void loadHasPositionProperty(OWLIndividual individualOne, Node node){
		
		
		float positionX = 15; 
		float positionY = 15; 
		
		OWLDatatypeProperty hasPositionXProperty = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasPositionX");
        if(hasPositionXProperty != null){
		   if (individualOne.getPropertyValue(hasPositionXProperty) != null){
			   positionX = (Float)individualOne.getPropertyValue(hasPositionXProperty);
		   }
        }
		
		OWLDatatypeProperty hasPositionYProperty = (OWLDatatypeProperty )owlModel.getOWLDatatypeProperty("hasPositionY");
		if(hasPositionYProperty != null){
			   if (individualOne.getPropertyValue(hasPositionYProperty) != null){
				   positionY = (Float)individualOne.getPropertyValue(hasPositionYProperty);
			   }
		}
		
		node.setPosition(positionX, positionY); 
		
	}
	
	
	/** 
	 * 
	 * @param contextNode
	 */
	
	private DefaultMutableTreeNode buildFormulaTree(ContextNode contextNode){
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(); 
		
		DefaultMutableTreeNode node; 
		NodeFormulaTree nodeFormulaRoot; 
		NodeFormulaTree nodeFormulaChild; 
		
		nodeFormulaRoot = new NodeFormulaTree("formula", enumType.FORMULA, 	enumSubType.NOTHING, null);  
    	
	    root = new DefaultMutableTreeNode(nodeFormulaRoot); 
		
		System.out.println("Entrou no build " +  contextNode.getName()); 
		
		/* 
		 * a raiz sera setada como o builtIn do qual o contextnode é instancia. 
		 * */
		
		Object obj = mapIsContextInstanceOf.get(contextNode); 
		if((obj instanceof BuiltInRV)){
			BuiltInRV builtIn = (BuiltInRV) obj; 
			
			enumType type = enumType.EMPTY;
			enumSubType subType = enumSubType.NOTHING; 
			
			if(builtIn instanceof BuiltInRVForAll){
				type = enumType.QUANTIFIER_OPERATOR;
				subType = enumSubType.FORALL; 
			}
			else			
			if(builtIn instanceof BuiltInRVExists){
				type = enumType.QUANTIFIER_OPERATOR;
				subType = enumSubType.EXISTS; 
			}
			else			
				if(builtIn instanceof BuiltInRVAnd){
					type = enumType.SIMPLE_OPERATOR;
					subType = enumSubType.AND; 
				}
				else			
					if(builtIn instanceof BuiltInRVOr){
						type = enumType.SIMPLE_OPERATOR;
						subType = enumSubType.OR; 
					}
					else			
						if(builtIn instanceof BuiltInRVNot){
							type = enumType.SIMPLE_OPERATOR;
							subType = enumSubType.NOT; 
						}
						else			
							if(builtIn instanceof BuiltInRVEqualTo){
								type = enumType.SIMPLE_OPERATOR;
								subType = enumSubType.EQUALTO; 
							}
							else			
								if(builtIn instanceof BuiltInRVIff){
									type = enumType.SIMPLE_OPERATOR;
									subType = enumSubType.IFF; 
								}
								else			
									if(builtIn instanceof BuiltInRVImplies){
										type = enumType.SIMPLE_OPERATOR;
										subType = enumSubType.IMPLIES; 
									}; 
			
			
			nodeFormulaRoot = new NodeFormulaTree(builtIn.getName(), type, subType, builtIn); 
		    root.setUserObject(nodeFormulaRoot); 
		    
			/* 
			 * procura pelos argumentos do builtIn, podendo estes serem contextnodes internos, o que 
			 * acarreta uma busca dos argumentos internos a este até se chegar ao final. 
			 */
		    
		    for(Argument argument: contextNode.getArgumentList()){
		    	
		    	if(argument.getOVariable()!= null){
		    		OrdinaryVariable ov = argument.getOVariable(); 
		    		nodeFormulaChild = new NodeFormulaTree(ov.getName(), enumType.OPERANDO, enumSubType.OVARIABLE, ov); 
		    		node = new DefaultMutableTreeNode(nodeFormulaChild); 
		    		root.add(node); 
		    		nodeFormulaRoot.addChild(nodeFormulaChild); 
		    	}
		    	else{
		    		if(argument.getArgumentTerm() != null){
		    			
		    			MultiEntityNode multiEntityNode = argument.getArgumentTerm(); 
		    			
		    			if(multiEntityNode instanceof ResidentNode){
		    				nodeFormulaChild = new NodeFormulaTree(multiEntityNode.getName(), enumType.OPERANDO, enumSubType.NODE, multiEntityNode); 
		    				node = new DefaultMutableTreeNode(nodeFormulaChild); 
		    				root.add(node); 
		    				nodeFormulaRoot.addChild(nodeFormulaChild); 
		    			}
		    			else{
		    				if(multiEntityNode instanceof ContextNode){
		    					DefaultMutableTreeNode child = buildFormulaTree((ContextNode)multiEntityNode);
		    					root.add(child); 
		    					nodeFormulaRoot.addChild((NodeFormulaTree)child.getUserObject()); 
		    				}
		    			}
		    		}
		    		else{
		    			//TODO lançar exceção... 
		    		}
		    	}
		    	
		    }
		    
		}
		else{
			if((obj instanceof ResidentNode)){
				nodeFormulaRoot = new NodeFormulaTree(((ResidentNode)obj).getName(), enumType.OPERANDO, enumSubType.NODE, (ResidentNode)obj); 
				root.setUserObject(nodeFormulaRoot); 				
			}
		}
		
		return root; 
	}
	
	/**
	 * Test method that print the mtheory structure. 
	 */
	
	private void checkMTheory(){
		
		System.out.println("\n\n\n\n-------   Test End --------"); 
		
		System.out.println("-> MTheory: " + mebn.getName());
		
		List<DomainMFrag> mFragList = mebn.getDomainMFragList(); 
		
		int desvio = 0; 
		
		for(DomainMFrag mFrag: mFragList){
			
			desvio++; 
			
			System.out.println(printSpace(desvio,3) + "->MFrag: " + mFrag.getName());
			
			List<ContextNode> contextNodeList = mFrag.getContextNodeList(); 
			
			for(ContextNode contextNode: contextNodeList){
				
				desvio++; 
				System.out.println(printSpace(desvio,3) + "-> ContextNode: " + contextNode.getName());
				
				for(Argument argument: contextNode.getArgumentList()){
					desvio++; 
					System.out.println(printSpace(desvio,3) + "-> Argument: " + argument.getName());
					{
					   desvio++; 
						System.out.println(printSpace(desvio,3) + "- IsSimpleArg: " + argument.isSimpleArgRelationship());
					    if(!argument.isSimpleArgRelationship()){
					    	System.out.println(printSpace(desvio,3) + "- ArgTerm: " + argument.getArgumentTerm().getName() );
					    }
					   desvio--; 
					}
					desvio--; 
				}
				
				desvio--; 
			}
			
			desvio--; 
		}
		
		System.out.println("-------   Test End --------\n\n\n\n"); 
				
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
	
}
