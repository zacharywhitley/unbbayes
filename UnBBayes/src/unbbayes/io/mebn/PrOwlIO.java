package unbbayes.io.mebn;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JProgressBar;

import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;

import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.repository.impl.LocalFileRepository;

/**
 * Make load/save in pr-owl.
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * @version 1.0 2006/10/25
 */

public class PrOwlIO implements MebnIO {
	
	public static final String PROWLMODELFILE = "/pr-owl/pr-owl.owl"; 
	
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException, IOMebnException{
		
		/* MEBN Structure */ 
		
		MultiEntityBayesianNetwork mebn = null;
		
		Collection instances; 
		Iterator itAux; 
		
	/*
		JProgressBar progress; 
		progress = new JProgressBar(0, 100); 
		progress.setValue(0); 
		progress.setStringPainted(true); 
	*/	
		
		DomainMFrag domainMFrag; 
		OrdinaryVariable oVariable; 
		ContextNode contextNode; 
		DomainResidentNode domainResidentNode; 
		GenerativeInputNode generativeInputNode; 
		Argument argument;
		MultiEntityNode multiEntityNode; 
		BuiltInRV builtInRV; 
		
		HashMap<String, DomainMFrag> mapDomainMFrag = new HashMap<String, DomainMFrag>(); 
		HashMap<String, OrdinaryVariable> mapOVariable = new HashMap<String, OrdinaryVariable>();
		HashMap<String, ContextNode> mapContextNode = new HashMap<String, ContextNode>();
		HashMap<String, DomainResidentNode> mapDomainResidentNode = new HashMap<String, DomainResidentNode>();
		HashMap<String, GenerativeInputNode> mapGenerativeInputNode = new HashMap<String, GenerativeInputNode>();
		HashMap<String, Argument> mapArgument = new HashMap<String, Argument>();
		HashMap<String, MultiEntityNode> mapMultiEntityNode = new HashMap<String, MultiEntityNode>(); 
		HashMap<String, BuiltInRV> mapBuiltInRV = new HashMap<String, BuiltInRV>(); 
		
		/* Protege API Structure */
		
		JenaOWLModel owlModel;     	
		OWLIndividual individualOne;
		OWLIndividual individualTwo; 
		
		OWLNamedClass owlNamedClass; 
		
		OWLObjectProperty objectProperty; 
		
		/** Load resource file from this package */
		final ResourceBundle resource = 
			ResourceBundle.getBundle("unbbayes.io.mebn.resources.IoMebnResources");
		
		owlModel = ProtegeOWL.createJenaOWLModel();
		
		URI uri = URIUtilities.createURI("file:///pr-owl.owl");
		
		owlModel.getRepositoryManager().addProjectRepository(new LocalFileRepository(file, true));
		
		try{
			owlModel.load(uri, FileUtils.langXMLAbbrev);
		}	
		catch(Exception e){
			e.printStackTrace(); 
		}
		
		/* Build the owl model */
		
		FileInputStream inputStream; 
		
		inputStream = new FileInputStream(file); 
		
		try{
			owlModel.load(inputStream, FileUtils.langXMLAbbrev);   
		}
		catch (Exception e){
			throw new IOMebnException(resource.getString("ModelCreationError")); 
		}
		
		/*------------------- MTheory -------------------*/
		
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
		objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasMFrag"); 	
		instances = individualOne.getPropertyValues(objectProperty); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualTwo = (OWLIndividual) it.next();
			domainMFrag = new DomainMFrag(individualTwo.getBrowserText(), mebn); 
			mebn.addDomainMFrag(domainMFrag); 
			mapDomainMFrag.put(individualTwo.getBrowserText(), domainMFrag); 
		}	
		
		/*------------------- DomainMFrag -------------------*/
		
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
				oVariable = new OrdinaryVariable(individualTwo.getBrowserText(), domainMFrag); 
				domainMFrag.addOrdinaryVariable(oVariable); 
				mapOVariable.put(individualTwo.getBrowserText(), oVariable); 
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText()); 
			}
			
			/* -> hasSkolen don't checket! */
			
		}		
		
		/*------------------- Context Node -------------------*/
		
		OWLNamedClass contextNodePr = owlModel.getOWLNamedClass("Context"); 
		instances = contextNodePr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();
			contextNode = mapContextNode.get(individualOne.getBrowserText()); 
			if (contextNode == null){
				throw new IOMebnException(resource.getString("ContextNodeNotExistsInMTheory"), individualOne.getBrowserText()); 
			}
			System.out.println("Context Node loaded: " + individualOne.getBrowserText()); 				
			
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
			
			/* -> isInnerTermOf */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInnerTermOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();			
			for (Iterator itIn = instances.iterator(); itIn.hasNext(); ){
				individualTwo = (OWLIndividual) itIn.next();
				multiEntityNode = mapMultiEntityNode.get(individualTwo.getBrowserText()); 
				contextNode.addInnerTermFromList(multiEntityNode); 
				multiEntityNode.addInnerTermOfList(contextNode); 
				System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			}	
			
		}
		
		/*------------------- BuiltIn Node -------------------*/
		
		OWLNamedClass builtInPr = owlModel.getOWLNamedClass("BuiltInRV"); 
		instances = builtInPr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();
			builtInRV = new BuiltInRV(individualOne.getBrowserText()); 			
			mebn.addBuiltInRVList(builtInRV); 
			mapBuiltInRV.put(individualOne.getBrowserText(), builtInRV); 
			System.out.println("BuiltInRV loaded: " + individualOne.getBrowserText()); 
			
			/* -> hasContextInstance */
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasContextInstance"); 
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
		
		/*------------------- Domain Resident -------------------*/
		
		OWLNamedClass domainResidentNodePr = owlModel.getOWLNamedClass("Domain_Res"); 
		instances = domainResidentNodePr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			individualOne = (OWLIndividual)it.next();
			domainResidentNode = mapDomainResidentNode.get(individualOne.getBrowserText()); 
			if (domainResidentNode == null){
				throw new IOMebnException(resource.getString("DomainResidentNotExistsInMTheory"), individualOne.getBrowserText() ); 
			}
			
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
				}
				else{
					if (mapGenerativeInputNode.containsKey(individualTwo.getBrowserText())){
						GenerativeInputNode aux = mapGenerativeInputNode.get(individualTwo.getBrowserText()); 
						domainResidentNode.addInputNodeFather(aux); 
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
				domainResidentNode.addInputInstanceFromList(generativeInputNode); 
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
			
			/* hasProbDist don't checked */
			
			/* hasPossibleValues don't checked */
			
			/* hasContextInstance don't checked */
			
			/* isArgTermIn don't checked */
		}
		
		/*------------------- Generative Input Node -------------------*/
		
		OWLNamedClass inputNodePr = owlModel.getOWLNamedClass("Generative_input"); 
		instances = inputNodePr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){
			
			individualOne = (OWLIndividual)it.next();
			System.out.println("Input Node loaded: " + individualOne.getBrowserText()); 			
			generativeInputNode = mapGenerativeInputNode.get(individualOne.getBrowserText()); 
			if (generativeInputNode == null){
				throw new IOMebnException(resource.getString("GenerativeInputNodeNotExistsInMTheory"), individualOne.getBrowserText() ); 				
			}
			
			/* -> isInputInstanceOf  */
			
			objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isInputInstanceOf"); 			
			instances = individualOne.getPropertyValues(objectProperty); 	
			itAux = instances.iterator();
			individualTwo = (OWLIndividual) itAux.next();
			if (mapDomainResidentNode.containsKey(individualTwo.getBrowserText())){
				domainResidentNode = mapDomainResidentNode.get(individualTwo.getBrowserText()); 
				generativeInputNode.setInputInstanceOfNode(domainResidentNode); 
			}
			else{
				if (mapBuiltInRV.containsKey(individualTwo.getBrowserText())){
					builtInRV = mapBuiltInRV.get(individualTwo.getBrowserText()); 
					generativeInputNode.setInputInstanceOfRV(builtInRV); 
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
		
		/*------------------- Ordinary Variable -------------------*/
		
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
			/*
			 objectProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("isSubsBy"); 			
			 instances = individualOne.getPropertyValues(objectProperty); 	
			 itAux = instances.iterator();
			 individualTwo = (OWLIndividual) itAux.next();
			 argument = mapArgument.get(individualTwo.getBrowserText()); 
			 if (argument.isSimpleArgRelationship()){
			 //TODO: criar algum atributo em OV para identificar onde ele é utilizado? 
			  }
			  else{
			  
			  }
			  System.out.println("-> " + individualOne.getBrowserText() + ": " + objectProperty.getBrowserText() + " = " + individualTwo.getBrowserText());			
			  */
			
			/* isRepBySkolen don't checked */ 
			
		}
		
		/*------------------- Arg Relationship -------------------*/
		
		OWLNamedClass argRelationshipPr = owlModel.getOWLNamedClass("ArgRelationship"); 
		instances = argRelationshipPr.getInstances(false); 
		
		for (Iterator it = instances.iterator(); it.hasNext(); ){	
			individualOne = (OWLIndividual)it.next();
			argument = mapArgument.get(individualOne.getBrowserText()); 
			if (argument == null){
				throw new IOMebnException(resource.getString("ArgumentNotFound"),  individualOne.getBrowserText()); 
			}
			System.out.println("ArgRelationship loaded: " + individualOne.getBrowserText()); 
			
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
		
		/*------------------- Simple Arg Relationship -------------------*/
		
		argRelationshipPr = owlModel.getOWLNamedClass("SimpleArgRelationship"); 
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
		
		System.out.println("Load concluido com sucesso!"); 
		
		return mebn; 		
	}
	
	/**
	 * Save the mebn structure in an file pr-owl. 
	 * @param nameFile: name of the file pr-owl where the mebn structure will be save
	 * @param mebn: the mebn structure
	 */
	
	public void saveMebn(File file, MultiEntityBayesianNetwork mebn) throws IOException, IOMebnException{
		
		HashMap<MultiEntityNode, OWLIndividual> nodeMap = new HashMap<MultiEntityNode, OWLIndividual>(); 
		
		ArrayList<DomainResidentNode> residentNodeListGeral = new ArrayList<DomainResidentNode>(); 
		HashMap<DomainResidentNode, OWLIndividual> domainResMap = new HashMap<DomainResidentNode, OWLIndividual>(); 
		
		ArrayList<GenerativeInputNode> inputNodeListGeral = new ArrayList<GenerativeInputNode>(); 
		HashMap<GenerativeInputNode, OWLIndividual> generativeInputMap = new HashMap<GenerativeInputNode, OWLIndividual>();
		
		ArrayList<ContextNode> contextListGeral = new ArrayList<ContextNode>(); 
		HashMap<ContextNode, OWLIndividual> contextMap = new HashMap<ContextNode, OWLIndividual>();
		
		ArrayList<OrdinaryVariable> oVariableGeral = new ArrayList<OrdinaryVariable>(); 
		HashMap<OrdinaryVariable, OWLIndividual> oVariableMap = new HashMap<OrdinaryVariable, OWLIndividual>();
		
		ArrayList<BuiltInRV> builtInRVGeral = new ArrayList<BuiltInRV>(); 
		HashMap<BuiltInRV, OWLIndividual> builtInRVMap = new HashMap<BuiltInRV, OWLIndividual>(); 
		
		/* Protege API Structure */
		
		JenaOWLModel owlModel;
		
		/** Load resource file from this package */
		final ResourceBundle resource = 
			ResourceBundle.getBundle("unbbayes.io.mebn.resources.IoMebnResources");
		
		/* load the pr-owl model */
		
		owlModel = ProtegeOWL.createJenaOWLModel();
		
		URI uri = URIUtilities.createURI("file:///pr-owl.owl");
		
		File filePrOwl = new File(PROWLMODELFILE);
		
		owlModel.getRepositoryManager().addProjectRepository(new LocalFileRepository(filePrOwl, true));
		
		try{
			owlModel.load(uri, FileUtils.langXMLAbbrev);
		}	
		catch(Exception e){
			throw new IOMebnException(resource.getString("PrOwlNotLoad")); 
		}
		

		
		/* MTheory */
		
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
			
			/* hasResidentNode */
			OWLObjectProperty hasResidentNodeProperty = (OWLObjectProperty)owlModel.getOWLObjectProperty("hasResidentNode"); 	
			List<DomainResidentNode> domainResidentNodeList = domainMFrag.getDomainResidentNodeList(); 
			for(DomainResidentNode residentNode: domainResidentNodeList){
				OWLNamedClass domainResClass = owlModel.getOWLNamedClass("Domain_Res"); 
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
		
		
		/* DomainResidentNode */
		
		for (DomainResidentNode residentNode: residentNodeListGeral){  
			OWLIndividual domainResIndividual = domainResMap.get(residentNode);	
			
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
		}
		
		/* ContextNode */
		
		for (ContextNode contextNode: contextListGeral){  
			OWLIndividual contextNodeIndividual = contextMap.get(contextNode);	
			
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
			
			
		}		
		
		/* InputNode */
		
		for (GenerativeInputNode generativeInputNode: inputNodeListGeral){  
			OWLIndividual generativeInputNodeIndividual = generativeInputMap.get(generativeInputNode);	
			
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

		/* BuiltInRV */
		
		builtInRVGeral = (ArrayList)mebn.getBuiltInRVList(); 
		OWLNamedClass builtInPr = owlModel.getOWLNamedClass("BuiltInRV"); 
		Collection instances = builtInPr.getInstances(false); 
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
		
		/* saving */
		
		Collection errors = new ArrayList();
		owlModel.save(file.toURI(), FileUtils.langXMLAbbrev, errors);
		System.out.println("File saved with " + errors.size() + " errors.");
		
	}	
	
	private BuiltInRV findBuiltInByName(List<BuiltInRV> builtInRVList, String name){
		for(BuiltInRV builtInRV: builtInRVList){
	        if (builtInRV.getName().compareTo(name) == 0){
	        	return builtInRV; 
	        }
		}
		return null; 
	}
}