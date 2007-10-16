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
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
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
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.context.enumSubType;
import unbbayes.prs.mebn.context.enumType;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.entity.exception.TypeAlreadyExistsException;
import unbbayes.prs.mebn.entity.exception.TypeException;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
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
 * Make de loader from a file pr-owl for the mebn structure. 
 * 
 * Version Pr-OWL: 1.02
 * (http://www.pr-owl.org/pr-owl.owl) 
 * 
 * @author Laecio Lima dos Santos
 * @version 1.0 
 */

public class LoaderPrOwlIO {

	/* MEBN Structure */ 
	
	private MultiEntityBayesianNetwork mebn = null;
	
	private Collection instances; 
	private Iterator itAux; 
	
	private HashMap<String, DomainMFrag> mapDomainMFrag = new HashMap<String, DomainMFrag>(); 
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

	private HashMap<String, DomainResidentNode> mapDomainResidentNode = new HashMap<String, DomainResidentNode>();
	private HashMap<String, GenerativeInputNode> mapGenerativeInputNode = new HashMap<String, GenerativeInputNode>();
	private HashMap<String, Argument> mapArgument = new HashMap<String, Argument>();
	private HashMap<String, MultiEntityNode> mapMultiEntityNode = new HashMap<String, MultiEntityNode>(); 
	private HashMap<String, BuiltInRV> mapBuiltInRV = new HashMap<String, BuiltInRV>(); 
	private HashMap<String, ObjectEntity> mapObjectEntity = new HashMap<String, ObjectEntity>(); 	
	private HashMap<String, CategoricalStateEntity> mapCategoricalStates = new HashMap<String, CategoricalStateEntity>(); 
	private HashMap<String, ObjectEntity> mapTypes = new HashMap<String, ObjectEntity>(); 	
	
	/* Protege API Structure */
	
	private JenaOWLModel owlModel;  
	
	/** Load resource file from this package */
	final ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.io.mebn.resources.IoMebnResources");	
	
	private static final String PROWLMODELFILE = "pr-owl/pr-owl.owl";
	
	private final String ORDINARY_VAR_SCOPE_SEPARATOR = ".";
	private final String POSSIBLE_VALUE_SCOPE_SEPARATOR = ".";	
	
	
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
		
		owlModel = ProtegeOWL.createJenaOWLModel();
		
		Debug.println("[DEBUG]" + this.getClass() + " -> Load begin"); 
		
		File filePrOwl = new File(PROWLMODELFILE);
		FileInputStream inputStreamOwl = new FileInputStream(filePrOwl); 
		
		owlModel.getRepositoryManager().addProjectRepository(
				new LocalFileRepository(filePrOwl, true));
		
		try{
			owlModel.load(inputStreamOwl, FileUtils.langXMLAbbrev);
			Debug.println("-> Load of model file PR-OWL successful"); 
		}	
		catch(Exception e){
			throw new IOMebnException(resource.getString("ErrorReadingFile") + 
					                  ": " + PROWLMODELFILE); 
		}
			
		FileInputStream inputStream = new FileInputStream(file); 
		
		try{
			owlModel.load(inputStream, FileUtils.langXMLAbbrev);   
		}
		catch (Exception e){
			throw new IOMebnException(resource.getString("ErrorReadingFile") + 
					                    ": " + file.getAbsolutePath()); 
		}
		
		/*------------------- MTheory -------------------*/
		loadMTheoryClass(); 
		
		/*------------------- Entities -------------------*/

		loadObjectEntity(); 
		loadMetaEntitiesClasses(); 
		
		/*-------------------MTheory elements------------*/
		loadDomainMFrag(); 
		loadBuiltInRV(); 
		loadContextNode(); 	
		loadDomainResidentNode(); 	
		loadGenerativeInputNode(); 	
		
		/*---------------------Arguments-------------------*/
		loadOrdinaryVariable(); 	
		loadArgRelationship(); 		
		loadSimpleArgRelationship(); 
		ajustArgumentOfNodes(); 
		
		setFormulasOfContextNodes(); 
		
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
		
		Debug.println("MTheory loaded: " + individualOne.getBrowserText()); 
		
		//Properties 
		
		/* hasMFrag */
		objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasMFrag"); 	
		instances = individualOne.getPropertyValues(objectProperty); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualTwo = (OWLIndividual) it.next();
			Debug.println("hasDomainMFrag: " + individualTwo.getBrowserText()); 
			domainMFrag = new DomainMFrag(individualTwo.getBrowserText(), mebn); 
			mebn.addDomainMFrag(domainMFrag); 
			mapDomainMFrag.put(individualTwo.getBrowserText(), domainMFrag); 
		}	
 
	}
	
	/**
	 * Load the MetaEntities for types of the mebn structure. 
	 */
	private void loadMetaEntitiesClasses(){
		
		OWLNamedClass metaEntityClass; 
		Collection instances; 
		OWLIndividual individualOne;
		
		metaEntityClass = owlModel.getOWLNamedClass("MetaEntity");
		
		instances = metaEntityClass.getInstances(false); 
		
		for (Object owlIndividual : instances){
			individualOne = (OWLIndividual) owlIndividual; 
			
			try{
			    Type type = mebn.getTypeContainer().createType(individualOne.getBrowserText()); 
			}
			catch (TypeAlreadyExistsException exception){
				//OK... lembre-se que os tipos basicos j� existem... 
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
		
		objectEntityClass = owlModel.getOWLNamedClass("ObjectEntity");
		
		subClasses = objectEntityClass.getSubclasses(true); 
		
		for (Object owlClass : subClasses){
			
			subClass = (OWLNamedClass)owlClass; 
			
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasType");

			try{
				ObjectEntity objectEntityMebn = mebn.getObjectEntityContainer().createObjectEntity(subClass.getBrowserText()); 	
			    mapObjectEntity.put(subClass.getBrowserText(), objectEntityMebn); 
			    mapTypes.put(objectEntityMebn.getType().getName(), objectEntityMebn); 
			    
			    //TODO verificar se o tipo eh o desejado... 
			}
			catch(TypeException typeException){
				typeException.printStackTrace(); 
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
			
			Debug.println("DomainMFrag loaded: " + individualOne.getBrowserText()); 
			
			/* -> hasResidentNode */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasResidentNode"); 
			instances = individualOne.getPropertyValues(objectProperty); 
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				domainResidentNode = new DomainResidentNode(individualTwo.getBrowserText(), domainMFrag); 
				domainMFrag.addDomainResidentNode(domainResidentNode); 
				mapDomainResidentNode.put(individualTwo.getBrowserText(), domainResidentNode); 
				mapMultiEntityNode.put(individualTwo.getBrowserText(), domainResidentNode); 
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
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
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
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
			
			Debug.println("Context Node loaded: " + individualOne.getBrowserText()); 				
			
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

		GenerativeInputNode generativeInputNode; 
		BuiltInRV builtInRV = null;		
		
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 	
		OWLObjectProperty objectProperty; 
		
		OWLNamedClass builtInPr = owlModel.getOWLNamedClass("BuiltInRV"); 
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
											//TODO lan?��ar excess?��o... 											
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
			
			Debug.println("Domain Resident loaded: " + individualOne.getBrowserText()); 			
			
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
					DomainResidentNode aux = mapDomainResidentNode.get(individualTwo.getBrowserText()); 
					
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
						GenerativeInputNode aux = mapGenerativeInputNode.get(individualTwo.getBrowserText()); 
						aux.addResidentNodeChild(domainResidentNode); 
			
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
				CategoricalStateEntity state; 
				objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasPossibleValues"); 			
				instances = individualOne.getPropertyValues(objectProperty); 	
				itAux = instances.iterator();
				for (Object instance: instances){
					individualTwo = (OWLIndividual)instance;
					String stateName = individualTwo.getBrowserText(); 
					/* case 1: booleans states */
					if(stateName.compareTo("true")==0){
						domainResidentNode.addPossibleValue(mebn.getBooleanStatesEntityContainer().getTrueStateEntity());   
						domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
					}
					else{
						if(stateName.compareTo("false") == 0){
							domainResidentNode.addPossibleValue(mebn.getBooleanStatesEntityContainer().getFalseStateEntity());  
							domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
						}
						else{
							if(stateName.compareTo("absurd") == 0){
								domainResidentNode.addPossibleValue(mebn.getBooleanStatesEntityContainer().getAbsurdStateEntity());   
								domainResidentNode.setTypeOfStates(ResidentNode.BOOLEAN_RV_STATES); 
							}
							else{
								if(mapTypes.get(stateName) != null){
									
									/* case 2:object entities */
									
									domainResidentNode.addPossibleValue(mapTypes.get(stateName)); 
									
								}
								else{
									/* case 3: categorical states */
									String name = individualTwo.getBrowserText(); 
									
									try{
										name = name.split(domainResidentNode.getName() + this.getOrdinaryVarScopeSeparator())[1]; 
									}
									catch(java.lang.ArrayIndexOutOfBoundsException e){
										//The name don't is in the valid format <ResidentNodeName>.<Name> 
										//use the real name of the state...
										name = individualTwo.getBrowserText(); 
										//TODO warning
									}
									
									state = mebn.getCategoricalStatesEntityContainer().createCategoricalEntity(name) ; 
									mapCategoricalStates.put(individualTwo.getBrowserText(), state); 
									domainResidentNode.addPossibleValue(state);  
									domainResidentNode.setTypeOfStates(ResidentNode.CATEGORY_RV_STATES);
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
			Debug.println("  - Input Node loaded: " + individualOne.getBrowserText()); 			
			generativeInputNode = mapGenerativeInputNode.get(individualOne.getBrowserText()); 
			if (generativeInputNode == null){
				throw new IOMebnException(resource.getString("GenerativeInputNodeNotExistsInMTheory"), individualOne.getBrowserText() ); 				
			}
			
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
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isParentOf"); 
			instances = individualOne.getPropertyValues(objectProperty); 	
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				domainResidentNode = mapDomainResidentNode.get(individualTwo.getBrowserText());
				if(domainResidentNode == null){
					throw new IOMebnException(resource.getString("DomainMFragNotExistsInMTheory"),  individualTwo.getBrowserText()); 
				}
				generativeInputNode.addResidentNodeChild(domainResidentNode); 
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
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
				Debug.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
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
			Debug.println("Ordinary Variable loaded: " + individualOne.getBrowserText()); 				
			
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
		
		OWLNamedClass argRelationshipPr = owlModel.getOWLNamedClass("ArgRelationship"); 
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
		
		OWLNamedClass argRelationshipPr = owlModel.getOWLNamedClass("SimpleArgRelationship"); 
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
		
		for(DomainResidentNode resident: mapDomainResidentNode.values()){
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
					   resident.addArgument(argumentOfPosition.getOVariable());
					}
					catch(Exception e){
						new InternalErrorDialog(); 
						e.printStackTrace(); 
					}
				}
				argNumberActual++; 
			}
		}
		
		for(GenerativeInputNode input: mapGenerativeInputNode.values()){
			
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
		
		nodeFormulaRoot = new NodeFormulaTree("formula", enumType.FORMULA, 	enumSubType.NOTHING, null);  
    	
		Debug.println("Entrou no build " +  contextNode.getName()); 
		
		/* 
		 * a raiz sera setada como o builtIn do qual o contextnode � instancia. 
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
		    nodeFormulaRoot.setMnemonic(builtIn.getMnemonic()); 
			
			List<Argument> argumentList = putArgumentListInOrder(contextNode.getArgumentList()); 
		  		    
		    for(Argument argument: argumentList){
		    	if(argument.getOVariable()!= null){
		    		OrdinaryVariable ov = argument.getOVariable(); 
		    		nodeFormulaChild = new NodeFormulaTree(ov.getName(), enumType.OPERANDO, enumSubType.OVARIABLE, ov); 
		    		nodeFormulaRoot.addChild(nodeFormulaChild); 
		    	}
		    	else{
		    		if(argument.getArgumentTerm() != null){
		    			
		    			MultiEntityNode multiEntityNode = argument.getArgumentTerm(); 
		    			
		    			if(multiEntityNode instanceof ResidentNode){
		    				ResidentNodePointer residentNodePointer = new ResidentNodePointer((ResidentNode)multiEntityNode, contextNode); 
		    				nodeFormulaChild = new NodeFormulaTree(multiEntityNode.getName(), enumType.OPERANDO, enumSubType.NODE, residentNodePointer); 
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
							nodeFormulaChild = new NodeFormulaTree(argument.getEntityTerm().getName(), enumType.OPERANDO, enumSubType.ENTITY, argument.getEntityTerm());
							nodeFormulaRoot.addChild(nodeFormulaChild); 
						}
		    		}
		    	}
		    	
		    }
		    
		}
		else{
			if((obj instanceof ResidentNode)){
				ResidentNodePointer residentNodePointer = new ResidentNodePointer((ResidentNode)obj, contextNode); 
				nodeFormulaRoot = new NodeFormulaTree(((ResidentNode)obj).getName(), enumType.OPERANDO, enumSubType.NODE, residentNodePointer); 
				
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
		
		List<DomainMFrag> mFragList = mebn.getDomainMFragList(); 
		
		int desvio = 0; 
		
		for(DomainMFrag mFrag: mFragList){
			
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
	
}
