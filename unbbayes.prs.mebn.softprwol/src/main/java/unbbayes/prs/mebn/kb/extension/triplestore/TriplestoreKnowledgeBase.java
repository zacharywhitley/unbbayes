package unbbayes.prs.mebn.kb.extension.triplestore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.io.exception.UBIOException;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DefaultMappingArgumentExtractor;
import unbbayes.prs.mebn.IMappingArgumentExtractor;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.builtInRV.BuiltInRVAnd;
import unbbayes.prs.mebn.builtInRV.BuiltInRVEqualTo;
import unbbayes.prs.mebn.builtInRV.BuiltInRVIff;
import unbbayes.prs.mebn.builtInRV.BuiltInRVImplies;
import unbbayes.prs.mebn.builtInRV.BuiltInRVNot;
import unbbayes.prs.mebn.builtInRV.BuiltInRVOr;
import unbbayes.prs.mebn.context.EnumSubType;
import unbbayes.prs.mebn.context.EnumType;
import unbbayes.prs.mebn.context.NodeFormulaTree;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.entity.ObjectEntityInstance;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.SearchResult;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.triplestore.TriplestoreController;
import unbbayes.triplestore.exception.InvalidQuerySintaxException;
import unbbayes.triplestore.exception.TriplestoreQueryEvaluationException;
import unbbayes.triplestore.exception.TriplestoreException;
import unbbayes.util.Debug;

public class TriplestoreKnowledgeBase implements KnowledgeBase {

	private MultiEntityBayesianNetwork defaultMEBN;

	private IMEBNMediator defaultMediator;

	private TriplestoreController triplestoreController; 
	
	private static int blankNodeNumber = 0; 
	
	private final String AND_OPERATOR = "."; 
	private final String OR_OPERATOR = "UNION"; 
	
	private static final String GENERAL_PREFIX_DECLARATION = 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
	        "PREFIX owl: <http://www.w3.org/2002/07/owl#> "; 
	
	private static final String OWL_SAMEAS = "owl:sameAs"; 
	private static final String RDF_TYPE = "rdf:type"; 
	private static final String SPACE = " "; 
	private static final String POINT = ". "; 

	/**
	 * This is the default instance of {@link #getMappingArgumentExtractor()}.
	 */
	public IMappingArgumentExtractor DEFAULT_MAPPING_ARGUMENT_EXTRACTOR = 
			DefaultMappingArgumentExtractor.newInstance();

	private IMappingArgumentExtractor mappingArgumentExtractor = DEFAULT_MAPPING_ARGUMENT_EXTRACTOR;

	public TriplestoreKnowledgeBase(){
		triplestoreController = new TriplestoreController(); 
	}

	/**
	 * This is just a call to {@link #getInstance(null, MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	public static KnowledgeBase getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		TriplestoreKnowledgeBase ret = new TriplestoreKnowledgeBase();
		ret.setDefaultMediator(mediator);
		ret.setDefaultMEBN(mebn);
		return ret;
	}

	public void setDefaultMEBN(MultiEntityBayesianNetwork defaultMEBN) {
		this.defaultMEBN = defaultMEBN;
	}

	public TriplestoreController getTriplestoreController() {
		return triplestoreController;
	}

	/**
	 * A {@link IMEBNMediator} to be used by this knowledge base if GUI or IO commands needs to be accessed.
	 * @param defaultMediator the defaultMediator to set
	 */
	public void setDefaultMediator(IMEBNMediator defaultMediator) {
		this.defaultMediator = defaultMediator;
	}	

	/* 
	 * The user can't delete the knowledge base using UnBBayes. He should do 
	 * this using the database manager. 
	 */
	public void clearKnowledgeBase() {
		Debug.println(this.getClass(), "Can't clear the knowledge base. Try to do it using the triplestore manager. ");
		return;
	}

	/* 
	 * The user can't delete the knowledge base using UnBBayes. He should do 
	 * this using the database manager. 
	 */
	public void clearFindings() {
		Debug.println(this.getClass(), "Can't clear the findings of knowledge base. Try to do it using the triplestore manager. ");
		return;
	}

	@Override
	//TODO Implement 
	public void createGenerativeKnowledgeBase(
			MultiEntityBayesianNetwork mebn) {

		Debug.println(this.getClass(), "Method don't implemented for triplestore databases. ");
		
//		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
//			createEntityDefinition(entity);
//		}
//
//		for(MFrag mfrag: mebn.getDomainMFragList()){
//			for(ResidentNode resident: mfrag.getResidentNodeList()){
//				createRandomVariableDefinition(resident);
//			}
//		}
	}


	@Override
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file) {
		throw new IllegalStateException("Method don't implemented for triplestore databases"); 
	}

	@Override
	public void saveFindings(MultiEntityBayesianNetwork mebn, File file) {
		throw new IllegalStateException("Method don't implemented for triplestore databases"); 
	}

	@Override
	public void loadModule(File file, boolean findingModule)
			throws UBIOException {
		throw new IllegalStateException("Method don't implemented for triplestore databases"); 
	}

	@Override
	public boolean supportsLocalFile(boolean isLoad) {
		return false;
	}

	@Override
	public String[] getSupportedLocalFileExtension(boolean isLoad) {
		String[] supportedLocalFileExtension = new String[0]; 
		return supportedLocalFileExtension;
	}

	@Override
	public void fillFindings(ResidentNode resident) {
		throw new IllegalStateException("Method don't implemented for triplestore databases"); 
	}

	//--------------------------------------------------------------------------
	// Inserting elements and definitions into base 
	//--------------------------------------------------------------------------

	@Override
	public void createEntityDefinition(ObjectEntity entity) {
		throw new IllegalStateException("Method don't implemented for triplestore databases"); 
	}

	@Override
	public void createRandomVariableDefinition(ResidentNode resident) {
		throw new IllegalStateException("Method don't implemented for triplestore databases"); 
	}

	@Override
	public void insertEntityInstance(ObjectEntityInstance entityInstance) {
		// TODO Auto-generated method stub
	}

	@Override
	public void insertRandomVariableFinding(
			RandomVariableFinding randomVariableFinding) {
		// TODO Auto-generated method stub
	}


	//--------------------------------------------------------------------------
	// Evaluation of FOL formulas 
	//--------------------------------------------------------------------------

	/* 
	 * Valid formats: 
	 * 
	 * <atom> ::= 
	 *     ov1 == ov2 |  
	 *     booleanRV(ov1, [,ov2 ...])  |                
	 *     nonBooleanRV(ov1, [,ov2 ...])  = ov0 |       
	 *     ov0 = nonBooleanRV(ov1, [,ov2 ...]) | 
	 *     nonBooleanRV(ov1, [,ov2 ...])  = CONST |     
	 *     CONST = nonBooleanRV(ov1, [,ov2 ...])
	 *
	 * <negation> ::= NOT <atom >  

	 * <conjunction> ::= <atom> [AND <atom>]+ 
	 * 
	 * <disjunction> ::= <atom> [OR <atom>]+         
	 * 
	 * <formula> ::= <atom> | <negation> | 
	 *               <conjunction> | <disjunction> 
	 * 
	 * 
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	@Override
	
	/*
	 * Context Node with all ordinary variables filled -> Only ask on base 
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public Boolean evaluateContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) {

		NodeFormulaTree formulaTree = (NodeFormulaTree) context
				.getFormulaTree();
		
		//BooleanRV(ov1, [,ov2 ...])  
		//EP2, EP3, EP4
		if ((formulaTree.getTypeNode() == EnumType.OPERAND)
				&& (formulaTree.getSubTypeNode() == EnumSubType.NODE)) {

			ResidentNodePointer pointer = (ResidentNodePointer)formulaTree.getNodeVariable();
			
			if(pointer.getResidentNode().getTypeOfStates() != ResidentNode.BOOLEAN_RV_STATES){
				throw new IllegalArgumentException("Invalid context node formula: " + context + ".");
			}else{
				StateLink result = searchFinding(pointer.getResidentNode(), ovInstances); 
				if (result.getState().getName().equals("true")){
					return new Boolean(Boolean.TRUE); 
				}else{
					return new Boolean(Boolean.FALSE); 
				}
			}
			
		} else {

			String query = ""; 
			
			query = GENERAL_PREFIX_DECLARATION + 
					"ASK WHERE { " ; 
			
			switch (formulaTree.getTypeNode()) {

			case SIMPLE_OPERATOR:

				BuiltInRV builtIn = (BuiltInRV) formulaTree.getNodeVariable();

				if(builtIn instanceof BuiltInRVAnd){
					query+= makeBinaryStatement(formulaTree, ovInstances, null, AND_OPERATOR);
				}else
					if(builtIn instanceof BuiltInRVOr){
						query+= makeBinaryStatement(formulaTree, ovInstances,null , OR_OPERATOR);
					}else
						if(builtIn instanceof BuiltInRVEqualTo){
							query+= makeEqualStatement(formulaTree, ovInstances, null, true); 	
						}else
							if(builtIn instanceof BuiltInRVIff){
								throw new IllegalArgumentException("Context Node formula using ***IFF***. This implementation don't deal with this formula. ");
							}else
								if(builtIn instanceof BuiltInRVImplies){
									throw new IllegalArgumentException("Context Node formula using ***IMPLIES***. This implementation don't deal with this formula. ");
								}else
									//========================= NOT ===========================
									if(builtIn instanceof BuiltInRVNot){

										formulaTree = formulaTree.getChildren().get(0); 
										
										if ((formulaTree.getTypeNode() == EnumType.OPERAND)
												&& (formulaTree.getSubTypeNode() == EnumSubType.NODE)) {

											ResidentNodePointer pointer = (ResidentNodePointer)formulaTree.getNodeVariable();

											if(pointer.getResidentNode().getTypeOfStates() != ResidentNode.BOOLEAN_RV_STATES){
												throw new IllegalArgumentException("Invalid context node formula: " + context + ".");
											}else{
												
												//Invert values from positive case 
												StateLink result = searchFinding(pointer.getResidentNode(), ovInstances); 
												if (result.getState().getName().equals("true")){
													return new Boolean(Boolean.FALSE); 
												}else{
													return new Boolean(Boolean.TRUE); 
												}
											}
										}else{
											if(formulaTree.getTypeNode() == EnumType.SIMPLE_OPERATOR){ 
												
												builtIn = (BuiltInRV) formulaTree.getNodeVariable();
												
												if(builtIn instanceof BuiltInRVEqualTo){

													query+= makeEqualStatement(formulaTree, ovInstances, null, false); 
													
												}else{
													throw new IllegalArgumentException("Negation formula don't allowed by this implementation: " + formulaTree.toString());
												}
												
											}else{
												throw new IllegalArgumentException("Negation formula don't allowed by this implementation: " + formulaTree.toString());
											}
										}
									}	    
				break;

			case QUANTIFIER_OPERATOR:

				throw new IllegalArgumentException("Context Node formula using Quantifier. "
						+ "This implementation don't deal with this formula. ");
				
			default:
				throw new IllegalArgumentException("ERROR! type of operator don't found");
			}
			
			query+="}"; 
			
			if((query != null) && (!(query.equals("")))){
				
				Debug.println("SPARQL Formula: " + query);
				
				boolean result = false;
				
				try {
					result = triplestoreController.executeAskQuery(query);
					
				} catch (InvalidQuerySintaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TriplestoreQueryEvaluationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TriplestoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				Debug.println("Result = " + result);
				return new Boolean(result); 
			}
		}

		return null;
		
	}
	
	private String makeEqualStatement(NodeFormulaTree formulaTree,
			List<OVInstance> ovInstanceList, List<OrdinaryVariable> ovFaultList, boolean positive) {
		
		String query = null; 
		
		NodeFormulaTree lOperand = formulaTree.getChildren().get(0);
		NodeFormulaTree rOperand = formulaTree.getChildren().get(1); 
		
		if((lOperand.getTypeNode() != EnumType.OPERAND) || 
		   (rOperand.getTypeNode() != EnumType.OPERAND)){
			throw new IllegalArgumentException("This implementation don't support this formula.");
		}
		
		//Case 1: ov1 == ov2 
		if ((lOperand.getSubTypeNode() == EnumSubType.OVARIABLE) && 
		    (rOperand.getSubTypeNode() == EnumSubType.OVARIABLE)){
			
			query = makeComparisonOvOvStatement((OrdinaryVariable)lOperand.getNodeVariable(), 
					                            (OrdinaryVariable)rOperand.getNodeVariable(), 
					                            ovInstanceList, ovFaultList, positive); 
		
		}
		
		//Case 2: RandomVariable(a,b,c) == x (and inverse order) 
		if ((lOperand.getSubTypeNode() == EnumSubType.OVARIABLE) && 
				(rOperand.getSubTypeNode() == EnumSubType.NODE)){
			
			//The formula will be evaluate in the next if. Only change the order of arguments. 
			NodeFormulaTree aux = rOperand; 
			rOperand = lOperand;
			lOperand = aux; 
		
		}
		
		if ((lOperand.getSubTypeNode() == EnumSubType.NODE) && 
				(rOperand.getSubTypeNode() == EnumSubType.OVARIABLE)){
			
			if(positive){
				query = makeComparisonNodeOvStatement((ResidentNodePointer)lOperand.getNodeVariable(), 
						(OrdinaryVariable)rOperand.getNodeVariable(), 
						ovInstanceList, ovFaultList, true); 
			}else{
				query = makeComparisonNodeOvStatement((ResidentNodePointer)lOperand.getNodeVariable(), 
						(OrdinaryVariable)rOperand.getNodeVariable(), 
						ovInstanceList, ovFaultList, false); 
			}
			
		}
		
		//Case 3: RandomVariable(a,b,c) == CONST (and inverse order) 
		//CONST here is an entity value. In the UnBBayes implementation, this CONST can't be a single datatype value 
		
		if ((lOperand.getSubTypeNode() == EnumSubType.ENTITY) && 
				(lOperand.getSubTypeNode() == EnumSubType.NODE)){
			
			//The formula will be evaluate in the next if. Only change the order of arguments. 
			
			NodeFormulaTree aux = rOperand; 
			rOperand = lOperand;
			lOperand = aux; 
		}
		
		if ((lOperand.getSubTypeNode() == EnumSubType.NODE) && 
				(lOperand.getSubTypeNode() == EnumSubType.ENTITY)){
			
			Entity entity = (Entity)(rOperand.getNodeVariable()); 
			
			query = makeComparisonNodeEntityStatement((ResidentNodePointer)lOperand.getNodeVariable(), 
					                                  entity, ovInstanceList, ovFaultList, positive); 
		}
		
		return query; 
		
	}
	
	private String makeBooleanNodeStatement(NodeFormulaTree formulaTree,
			List<OVInstance> ovInstanceList, List<OrdinaryVariable> ovFaultList, boolean positive) {
		
		String query = "";
		
		ResidentNodePointer pointer = (ResidentNodePointer)(formulaTree.getNodeVariable()); 
		ResidentNode residentNode = pointer.getResidentNode();
		
		//Type of node have to be ObjectEntity 
		if(residentNode.getTypeOfStates() != ResidentNode.BOOLEAN_RV_STATES){
			throw new IllegalStateException("Fail in evaluate search context node formula for node " + residentNode + ". Invalid format." );
		}
		
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), residentNode);
		if (propertyIRI == null) {
			throw new IllegalStateException("Fail in search finding to node " + residentNode + "." + "\n" + 
		                    this.getDefaultMEBN() + " does not contain references to the OWL property related to resident node " + residentNode);
		}
		
		if(!positive){
			query+="FILTER NOT EXISTS {" + " "; 
		}
		
		if (residentNode.getArgumentList().size() == 1){
			//Single Case: Data property with only one argument. 
						
			query+= "?X0" + " <" + propertyIRI + "> " + "TRUE" + "."; 
			
		}else{
			if (residentNode.getArgumentList().size() == 2){

				// extract the values of the arguments
				
				String subjectName = null; 
				String objectName = null; 
				
				Argument argument1 = residentNode.getArgumentList().get(0); 
				OrdinaryVariable ov1 = argument1.getOVariable(); 
				for(OVInstance ovInstance: ovInstanceList){
					if(ovInstance.getOv().equals(ov1)){
						subjectName = "<" + ovInstance.getEntity().getInstanceName() + ">"; 
						break; 
					}
				}
				
				if(subjectName == null){
						subjectName = "?X" + ovFaultList.indexOf(ov1);
				}
				
				Argument argument2 = residentNode.getArgumentList().get(1); 
				OrdinaryVariable ov2 = argument2.getOVariable(); 
				
				for(OVInstance ovInstance: ovInstanceList){
					if(ovInstance.getOv().equals(ov2)){
						objectName = "<" + ovInstance.getEntity().getInstanceName() + ">"; 
						break; 
					}
				}
				
				if(objectName == null){
					objectName = "?X" + ovFaultList.indexOf(ov2);
				}
				
				//TODO Analyze! This hould be the arguments of context node, that can be different of arguments of original resident node. 
				Map<Argument, Map<OWLProperty, Integer>> argumentMappings = getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
						residentNode, residentNode.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());
				
				if (argumentMappings != null) {
					// we know that at this point the resident node has 2 arguments (because the number of arguments in resident and number of entries in listArguments match)
					Map<OWLProperty, Integer> argMap1 = argumentMappings.get(residentNode.getArgumentList().get(0)); // extract the mapping of the 1st argument
					if (argMap1 != null) {
						// checking consistency
						if (argMap1.size() != 1) {
							throw new IllegalArgumentException("Found " + argMap1.size() + " mappings to 1st argument of node " + residentNode);
						}
						if (!argMap1.keySet().iterator().next().getIRI().equals(propertyIRI)) {
							throw new IllegalArgumentException("Node " + residentNode + " is defining uncertainty of " + propertyIRI 
									+ ", but its 1st argument was mapped to owl property " + argMap1.keySet().iterator().next());
						}
						// at this point, we know there is 1 mapping to this argument
						if (argMap1.values().iterator().next().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
							// 1st argument is object, so swap the subjectName and objectName (because at this point the subject was assumed to be the 1st argument)
							String temp = objectName;
							objectName = subjectName;
							subjectName = temp;
						}	// if argument is either subject or unknown, then use default behavior
					} 
					Map<OWLProperty, Integer> argMap2 = argumentMappings.get(residentNode.getArgumentList().get(1)); // extract the mapping of the 2nd argument
					if (argMap2 != null) {
						// checking consistency
						if (argMap2.size() != 1) {
							throw new IllegalArgumentException("Found " + argMap2.size() + " mappings to 2nd argument of node " + residentNode);
						}
						if (!argMap2.keySet().iterator().next().getIRI().equals(propertyIRI)) {
							throw new IllegalArgumentException("Node " + residentNode + " is defining uncertainty of " + propertyIRI 
									+ ", but its 2nd argument was mapped to owl property " + argMap2.keySet().iterator().next());
						}
						// at this point, we know there is 1 mapping to this argument
						if (argMap2.values().iterator().next().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
							// 2nd argument is subject, so swap the subjectName and objectName (because at this point the subject was assumed to be the 1st argument)
							String temp = objectName;
							objectName = subjectName;
							subjectName = temp;
						}	// if argument is either subject or unknown, then use default behavior
					}
				}
				
				query+= subjectName  + " " +
						"<" + propertyIRI + ">" + " " + 
						objectName; 
				
			}else{
				Map<Argument, Map<OWLProperty, Integer>> argumentMappings = getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(residentNode, residentNode.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());

				// translate the mapping to a map of ordinary variables to properties, because listArguments uses ordinary variables as reference
				Map<OrdinaryVariable, Map<OWLProperty, Integer>> propertiesPerOV = 
						new HashMap<OrdinaryVariable, Map<OWLProperty,Integer>>();

				for (Entry<Argument, Map<OWLProperty, Integer>> entry : argumentMappings.entrySet()) {
					propertiesPerOV.put(entry.getKey().getOVariable(), entry.getValue());
				}
				

				for(OrdinaryVariable ov: residentNode.getOrdinaryVariableList()){
					
					String argumentName = null; 
					
					for(OVInstance ovInstance: ovInstanceList){
						if(ovInstance.getOv().equals(ov)){
							argumentName = "<" + ovInstance.getEntity().getInstanceName() + ">"; 
							break; 
						}
					}
					
					if(argumentName == null){
						argumentName = "?X" + ovFaultList.indexOf(ov);
					}
					
					// if there is no valid mapping, use default (use the one in definesUncertaintyOf, and isSubjectIn)
					OWLProperty property = null; 
					boolean isSubjectIn = true;
					
					// check if there is any argument without mapping. If not, use default behavior (use the property specified in definesUncertaintyOf)
					Map<OWLProperty, Integer> propertyMap = propertiesPerOV.get(ov);
					if (propertyMap != null) {
						// Note: the signature allows multiple mappings per argument, but here we use only 1 (the first one which is not IMappingArgumentExtractor.UNDEFINED_CODE). 
						for (Entry<OWLProperty, Integer> entry : propertyMap.entrySet()) {
							if (entry.getValue().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
								isSubjectIn = false;
								property = entry.getKey();
								break;
							} else if (entry.getValue().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
								isSubjectIn = true;
								property = entry.getKey();
								break;
							} 
							// or else, entry.getValue() == IMappingArgumentExtractor.UNDEFINED_CODE), so find next
						}
					}else{
						throw new IllegalArgumentException("Ordinary Variable " + ov + " of " + residentNode + " has no mapping to OWL property.");
					}
					
					if(isSubjectIn){
						query+= argumentName + 
								"<" + property.getIRI() + ">" + " " + 
								"_:a1" + " " + 
								"." + " "; 
					}else{
						query+= " _:a1" + " " + 
								"<" + property.getIRI() + ">" + " " +   
								argumentName + 
								"." + " "; 
					} 
				}
			}
		}
		
		if(!positive){
			query+="}"; 
		}
		
		return query; 
	}
	
	/*
	 * Operator: "=" or "!=" 
	 */
	private String makeComparisonOvOvStatement(OrdinaryVariable lOperand, OrdinaryVariable rOperand, 
			                              List<OVInstance> ovInstanceList, List<OrdinaryVariable> ovFaultList, boolean positive){
		String query = ""; 
		
		Type type = rOperand.getValueType();
		ObjectEntity oe = this.defaultMEBN.getObjectEntityContainer().getObjectEntityByType(type); 
		IRI objectEntityType = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, oe);

		String rName = null; 
		
		OVInstance rOVInstance = null; 
		for(OVInstance ovInstance: ovInstanceList){
			if(ovInstance.getOv().equals(rOperand)){
				rOVInstance = ovInstance; 
				break; 
			}
		}
		
		if(rOVInstance == null){
			if(ovFaultList != null){
				int index = ovFaultList.indexOf(rOperand); 
				if(index >= 0){
					rName = "?X" + index; 
				}else{
					throw new IllegalArgumentException("Ordinary Variable Fault don't found for first argument of context formula.");
				}
			}else{
				throw new IllegalArgumentException("OVInstance don't found for first argument of context formula.");
			}
		}else{
			rName = "<" + 
					rOVInstance.getEntity().getInstanceName() + 
					">";
		}

		type = lOperand.getValueType(); 
		oe = this.defaultMEBN.getObjectEntityContainer().getObjectEntityByType(type); 
		objectEntityType = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, oe);

		String lName = null; 
		
		OVInstance lOVInstance = null; 
		for(OVInstance ovInstance: ovInstanceList){
			if(ovInstance.getOv().equals(lOperand)){
				lOVInstance = ovInstance; 
				break; 
			}
		}
		
		if(lOVInstance == null){
			if(ovFaultList != null){
				int index = ovFaultList.indexOf(lOperand); 
				if(index >= 0){
					lName = "?X" + index; 
				}else{
					throw new IllegalArgumentException("Ordinary Variable Fault don't found for second argument of context formula.");
				}
			}else{
				throw new IllegalArgumentException("OVInstance don't found for second argument of context formula.");
			}
		}else{
			lName = "<" + 
					lOVInstance.getEntity().getInstanceName() + 
					">";
		}
		
		// This negative form don't will inside brackets in an AND clause formula. 
		// OK, because the BNF grammar proposed only accepts positive terms in AND formula. 
		
		if(!positive){
			query+= "FILTER NOT EXISTS{"; 
			query+= " "; 
		}
		
		query+= lName;
		query+= " " + OWL_SAMEAS + " "; 
		query+= rName;
				
		if(!positive){
			query+= "}"; 
		}
		
		return query; 
		
	}
	
	/*
	 * Operator: "=" or "!=" 
	 */
	//Not used more. It stays here for analysis. (before used for solve context node formula) 
	private String makeComparisonOvOvStatementUsingEqual(OrdinaryVariable lOperand, OrdinaryVariable rOperand, 
			                              List<OVInstance> ovInstanceList, boolean positive){
		String query = ""; 

		OrdinaryVariable ov = rOperand;
		Type type = ov.getValueType();
		ObjectEntity oe = this.defaultMEBN.getObjectEntityContainer().getObjectEntityByType(type); 
		IRI objectEntityType = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, oe);
		
		OVInstance rOVInstance = null; 
		for(OVInstance ovInstance: ovInstanceList){
			if(ovInstance.getOv().equals(ov)){
				rOVInstance = ovInstance; 
				break; 
			}
		}
		
		if(rOVInstance == null){
			throw new IllegalArgumentException("OVInstance don't found for first argument of context formula.");
		}
		
		query+= "?X " + "rdf:type " + "<" + objectEntityType + "> " + ". ";
		
		ov = lOperand; 
		type = ov.getValueType(); 
		oe = this.defaultMEBN.getObjectEntityContainer().getObjectEntityByType(type); 
		objectEntityType = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, oe);
		
		OVInstance lOVInstance = null; 
		for(OVInstance ovInstance: ovInstanceList){
			if(ovInstance.getOv().equals(ov)){
				lOVInstance = ovInstance; 
				break; 
			}
		}
		
		if(lOVInstance == null){
			throw new IllegalArgumentException("OVInstance don't found for first argument of context formula.");
		}
		
		query+= "?Y " + "rdf:type " + "<" + objectEntityType + "> " + ". ";
		
		query+= " FILTER (";
		
		query+= "?X = " + "<" + 
		        rOVInstance.getEntity().getInstanceName() + 
		        "> && ";  
		query+= "?Y = " + "<" + 
		        lOVInstance.getEntity().getInstanceName() + 
		        "> && ";		
		
		if(positive){
			query+= "?X " + "=" + " ?Y"; 
		}else{
			query+= "?X " + "!=" + " ?Y";
		}
		
		query+= ")"; 
		
		return query; 
		
	}	
	
	/*
	 * Operator: "=" or "!=" 
	 */
	private String makeComparisonNodeOvStatement(ResidentNodePointer residentNodePointer, OrdinaryVariable rOperand, 
			                              List<OVInstance> ovInstanceList, List<OrdinaryVariable> ovFaultList, boolean positive){
		String query = null; 
	
		ResidentNode residentNode = residentNodePointer.getResidentNode();
		
		//Type of node have to be ObjectEntity 
		if(residentNode.getTypeOfStates() != ResidentNode.OBJECT_ENTITY){
			throw new IllegalStateException("Fail in evaluate search context node formula for node " + residentNode + ". Invalid format." );
		}
		
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), residentNode);
		if (propertyIRI == null) {
			throw new IllegalStateException("Fail in search finding to node " + residentNode + "." + "\n" + 
		                    this.getDefaultMEBN() + " does not contain references to the OWL property related to resident node " + residentNode);
		}
		
		String mainPropertyString = "<" + propertyIRI + ">"; 
		
		Type type = rOperand.getValueType(); 
		ObjectEntity oe = this.defaultMEBN.getObjectEntityContainer().getObjectEntityByType(type); 
		IRI objectEntityType = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, oe);
		
		OVInstance rOVInstance = null; 
		for(OVInstance ovInstance: ovInstanceList){
			if(ovInstance.getOv().equals(rOperand)){
				rOVInstance = ovInstance; 
				break; 
			}
		}
		
		String entityString = null; 
		
		if(rOVInstance == null){
			int index = ovFaultList.indexOf(rOperand); 
			if(index >= 0){
				entityString = "?X" + index;
			}else{
				throw new IllegalArgumentException("Ordinary Variable Fault don't found for argument of context formula.");
			}
		}else{
			entityString = "<" + rOVInstance.getEntity().getInstanceName() + ">"; 
		}
		
		//Type of node have to be ObjectEntity 
		//This already is validate in tree building. 

		//Simple Case: Resident Node with only one argument 
		if(residentNode.getArgumentList().size() == 1){
						
			//Argument 
			OrdinaryVariable ov = residentNode.getArgumentList().get(0).getOVariable(); 
			
			String argString = null;
			
			OVInstance argOVInstance = null; 
			for(OVInstance ovInstance: ovInstanceList){
				if(ovInstance.getOv().equals(ov)){
					argOVInstance = ovInstance; 
					argString = "<" + argOVInstance.getEntity().getInstanceName() + ">"; 
					break; 
				}
			}
			
			if(argOVInstance == null){
				if(ovFaultList != null){
					int index = ovFaultList.indexOf(ov); 
					if(index >= 0){ 
						argString = "?X" + index;
					}else{
						throw new IllegalArgumentException("Not found value for ordinary variable " + ov + ". ");
					}
				}else{
					throw new IllegalArgumentException("Not found value for ordinary variable " + ov + ". ");
				}
			}
			
			//Default query. Can be changed after search definitions of isObjectIn and isSubjectIn 
			query = ""; 
			if(!positive){
				query+="FILTER NOT EXISTS {" + " "; 
			}
			
			query+= argString + " " + mainPropertyString + " " + 
					entityString + " "  + " ."; 
			
			if(!positive){
				query+="}"; 
			}
			
			Map<Argument, Map<OWLProperty, Integer>> argumentMappings = 
                    getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
                                   residentNode, residentNode.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());
		
			if (argumentMappings != null) {
				// extract the mapping of the only argument
				Map<OWLProperty, Integer> singleArgMap = argumentMappings.get(residentNode.getArgumentList().get(0));
				if (singleArgMap != null) {
					// checking consistency
					if (singleArgMap.size() != 1) {
						throw new IllegalArgumentException("Found " + singleArgMap.size() + " mappings to arguments of node " + residentNode);
					}
					if (!singleArgMap.keySet().iterator().next().getIRI().equals(propertyIRI)) {
						throw new IllegalArgumentException("Node " + residentNode + " is defining uncertainty of " + propertyIRI 
								+ ", but its argument was mapped to owl property " + singleArgMap.keySet().iterator().next());
					}

					// at this pint, we know there are 1 argument and 1 mapping
					if (singleArgMap.values().iterator().next().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
						query = ""; 
						if(!positive){
							query+="FILTER NOT EXISTS {" + " "; 
						}
						query+=  entityString + " " + mainPropertyString + " " + 
								argString + " "  + " ."; 
						
						if(!positive){
							query+="}"; 
						}
					}
				}
			}

		}else{
			
			query = ""; 
			
			if(!positive){
				query+="FILTER NOT EXISTS {" + " "; 
			}
			
			//ResidentNode with more than one argument 
			// get the owl properties related (by subjectIn or objectIn) to the arguments of this node
			Map<Argument, Map<OWLProperty, Integer>> propertiesPerArgument = 
					getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
							residentNode, residentNode.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());
			if (propertiesPerArgument == null || propertiesPerArgument.isEmpty()) {
				// a node with no arguments mapped to OWL properties cannot have findings anyway
				try {
					Debug.println(getClass(), "There is no mapping specified for n-ary relationship of node " + residentNode);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return null;
			}
			if (propertiesPerArgument.size() != residentNode.getArgumentList().size()) {
				try {
					Debug.println(getClass(), "The n-ary relationship of node " + residentNode + " is not fully mapped to OWL properties.");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return null;
			}
			
			// translate the mapping to a map of ordinary variables to properties, because listArguments uses ordinary variables as reference
			Map<OrdinaryVariable, Map<OWLProperty, Integer>> propertiesPerOV = 
					new HashMap<OrdinaryVariable, Map<OWLProperty,Integer>>();

			for (Entry<Argument, Map<OWLProperty, Integer>> entry : propertiesPerArgument.entrySet()) {
				propertiesPerOV.put(entry.getKey().getOVariable(), entry.getValue());
			}

			OWLDataFactory factory = getOWLOntology().getOWLOntologyManager().getOWLDataFactory();

			// Extract the OWL object property pointed by definesUncertaintyOf.
			// At least 1 argument must be using it either in subjectIn or objectIn. 
			// If not, by default the 1st unspecified argument will be considered as the subject of this property

			OWLObjectProperty mainProperty = factory.getOWLObjectProperty(propertyIRI);

			//OrdinaryVariable state need to be the object of property. 
			//(we can't set the isObject/isSubject property for range of a function)
			
			//We use the blank node a1 to represent the central node of a n-ary relation 
			query+= "_:a1" + " " + mainPropertyString + " " + 
					" " + entityString + " "+ 
					"." + " "; 
			
			//Now, for each argument of resident node we write a search using the connector blank node 
			for(OrdinaryVariable ov: residentNode.getOrdinaryVariableList()){
				
				String ovInstanceArgumentURI = null; 
				
				OVInstance argInstance = null; 
				
				for(OVInstance ovInstance: ovInstanceList){
					if(ovInstance.getOv().equals(ov)){
						argInstance = ovInstance; 
						ovInstanceArgumentURI = "<"  + ovInstance.getEntity().getInstanceName() + ">";
						break; 
					}
				}
				
				if(argInstance == null){
					if(ovFaultList != null){
						int index = ovFaultList.indexOf(ov); 
						if(index >= 0){ 
							ovInstanceArgumentURI = "?X" + index;
						}else{
							throw new IllegalArgumentException("Not found value for ordinary variable " + ov + ". ");
						}
					}else{
						throw new IllegalArgumentException("Not found value for ordinary variable " + ov + ". ");
					}
				}
				
				// if there is no valid mapping, use default (use the one in definesUncertaintyOf, and isSubjectIn)
				OWLProperty property = null; 
				boolean isSubjectIn = true;
				
				// check if there is any argument without mapping. If not, use default behavior (use the property specified in definesUncertaintyOf)
				Map<OWLProperty, Integer> propertyMap = propertiesPerOV.get(argInstance.getOv());
				if (propertyMap != null) {
					// Note: the signature allows multiple mappings per argument, but here we use only 1 (the first one which is not IMappingArgumentExtractor.UNDEFINED_CODE). 
					for (Entry<OWLProperty, Integer> entry : propertyMap.entrySet()) {
						if (entry.getValue().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
							isSubjectIn = false;
							property = entry.getKey();
							break;
						} else if (entry.getValue().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
							isSubjectIn = true;
							property = entry.getKey();
							break;
						} 
						// or else, entry.getValue() == IMappingArgumentExtractor.UNDEFINED_CODE), so find next
					}
				}else{
					throw new IllegalArgumentException("Argument " + argInstance + " of " + residentNode + " has no mapping to OWL property.");
				}

				if(isSubjectIn){
					query+= ovInstanceArgumentURI + 
							"<" + property.getIRI() + ">" + " " + 
							"_:a1" + " " + 
							"." + " "; 
				}else{
					query+= " _:a1" + " " + 
							"<" + property.getIRI() + ">" + " " +   
							ovInstanceArgumentURI + 
							"." + " "; 
				}	
			}
			
			if(!positive){
				query+="}"; 
			}
		}
		
		return query; 
		
	}
	
	/*
	 * Operator: "=" or "!=" 
	 */
	private String makeComparisonNodeEntityStatement(ResidentNodePointer residentNodePointer, Entity rOperand, 
			                              List<OVInstance> ovInstanceList, List<OrdinaryVariable> ovFaultList, boolean positive){
		String query = ""; 
		
		if(!positive){
			query+= "FILTER NOT EXISTS { "; 
		}
		
		// initial assertions
		if (residentNodePointer == null)  {
			// it is impossible to search a finding if no variable is provided
			Debug.println(this.getClass(), "Attempted to search finding for null resident node.");
			return null;
		}
		
		ResidentNode residentNode = residentNodePointer.getResidentNode();
		
		if (residentNode == null)  {
			// it is impossible to search a finding if no variable is provided
			Debug.println(this.getClass(), "Attempted to search finding for null resident node.");
			return null;
		}
		
		if (residentNode.getArgumentList() == null) {
			throw new IllegalArgumentException("This knowledge base cannot handle resident nodes with null arguments. Resident node = " + residentNode);
		}	
		
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), residentNode);
		if (propertyIRI == null) {
			throw new IllegalStateException("Fail in search finding to node " + residentNode + "." + "\n" + 
		                    this.getDefaultMEBN() + " does not contain references to the OWL property related to resident node " + residentNode);
		}
		
		String property = "<" + propertyIRI + ">"; 
		
		Entity entity = rOperand; 
		IRI objectEntity = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, entity);
		String constant = "<" + entity + ">"; 

		//Type of node have to be ObjectEntity 
		//This already is validate in tree building. 

		//Simple Case: Resident Node with only one argument 
		if(residentNode.getArgumentList().size() == 1){
			
			//Argument 
			OrdinaryVariable ov = residentNode.getArgumentList().get(0).getOVariable(); 
			
			OVInstance argOVInstance = null; 
			
			for(OVInstance ovInstance: ovInstanceList){
				if(ovInstance.getOv().equals(ov)){
					argOVInstance = ovInstance; 
					break; 
				}
			}
			
			String argument; 
			
			if(argOVInstance == null){
				if(ovFaultList != null){
					int index = ovFaultList.indexOf(ov); 
					if(index >= 0){
						argument = "?X" + index; 
					}else{
						throw new IllegalArgumentException("Ordinary Variable Fault don't found for argument of context formula.");
					}
				}else{
					throw new IllegalArgumentException("Fail in search finding to node " + residentNode + "." + "\n" + 
		                    this.getDefaultMEBN() + " Argument " + ov + " don't filled.");
				}
			}else{
				argument = "<" + argOVInstance.getEntity().getInstanceName() + ">"; 
			}
			
			Map<Argument, Map<OWLProperty, Integer>> argumentMappings = 
                    getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
                                   residentNode, residentNode.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());
			
			if (argumentMappings != null) {
				// extract the mapping of the only argument
				Map<OWLProperty, Integer> singleArgMap = argumentMappings.get(residentNode.getArgumentList().get(0));
				if (singleArgMap != null) {
					// checking consistency
					if (singleArgMap.size() != 1) {
						throw new IllegalArgumentException("Found " + singleArgMap.size() + " mappings to arguments of node " + residentNode);
					}
					if (!singleArgMap.keySet().iterator().next().getIRI().equals(propertyIRI)) {
						throw new IllegalArgumentException("Node " + residentNode + " is defining uncertainty of " + propertyIRI 
								+ ", but its argument was mapped to owl property " + singleArgMap.keySet().iterator().next());
					}
					
					// at this pint, we know there are 1 argument and 1 mapping
					if (singleArgMap.values().iterator().next().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
						query+= constant + " " + property + " " + argument;  
					}else{
						query+= argument + " " + property + " " + constant; 
					}
				}else{ // Argument was not mapped with isSubjectIn or isObjectIn. Assume default
					query+= argument + " " + property + " " + constant; 
				}
			}else{
				query+= argument + " " + property + " " + constant; 
			}
		}else{
			
			// get the owl properties related (by subjectIn or objectIn) to the arguments of this node
			Map<Argument, Map<OWLProperty, Integer>> propertiesPerArgument = 
					getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
							residentNode, residentNode.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());

			if (propertiesPerArgument == null || propertiesPerArgument.isEmpty()) {
				// a node with no arguments mapped to OWL properties cannot have findings anyway
				try {
					Debug.println(getClass(), "There is no mapping specified for n-ary relationship of node " + residentNode);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return null;
			}
			if (propertiesPerArgument.size() != residentNode.getArgumentList().size()) {
				try {
					Debug.println(getClass(), "The n-ary relationship of node " + residentNode + " is not fully mapped to OWL properties.");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return null;
			}

			// translate the mapping to a map of ordinary variables to properties, because listArguments uses ordinary variables as reference
			Map<OrdinaryVariable, Map<OWLProperty, Integer>> propertiesPerOV = 
					new HashMap<OrdinaryVariable, Map<OWLProperty,Integer>>();

			for (Entry<Argument, Map<OWLProperty, Integer>> entry : propertiesPerArgument.entrySet()) {
				propertiesPerOV.put(entry.getKey().getOVariable(), entry.getValue());
			}

			OWLDataFactory factory = getOWLOntology().getOWLOntologyManager().getOWLDataFactory();

			// Extract the OWL object property pointed by definesUncertaintyOf.
			// At least 1 argument must be using it either in subjectIn or objectIn. 
			// If not, by default the 1st unspecified argument will be considered as the subject of this property

			OWLObjectProperty mainProperty = factory.getOWLObjectProperty(propertyIRI);
			
			query+= " :a1 " + "<" + mainProperty.getIRI() + "> " + constant + " . "; 
			
			// Note: we already checked at the beginning of this method that listArgument.size() == resident.getArgumentList().size()
			for(OrdinaryVariable ov: residentNode.getOrdinaryVariableList()){
				
				OVInstance argOVInstance = null; 
				
				for(OVInstance ovInstance: ovInstanceList){
					if(ovInstance.getOv().equals(ov)){
						argOVInstance = ovInstance; 
						break; 
					}
				}
				
				String argument; 
				
				if(argOVInstance == null){
					if(ovFaultList != null){
						int index = ovFaultList.indexOf(ov); 
						if(index >= 0){
							argument = "?X" + index; 
						}else{
							throw new IllegalArgumentException("Ordinary Variable Fault don't found for argument of context formula.");
						}
					}else{
						throw new IllegalArgumentException("Fail in search finding to node " + residentNode + "." + "\n" + 
			                    this.getDefaultMEBN() + " Argument " + ov + " don't filled.");
					}
				}else{
					argument = "<" + argOVInstance.getEntity().getInstanceName() + ">"; 
				}
				
				// if there is no valid mapping, use default (use the one in definesUncertaintyOf, and isSubjectIn)
				OWLProperty owlProperty = null; 
				boolean isSubjectIn = true;
				
				// check if there is any argument without mapping. If not, use default behavior (use the property specified in definesUncertaintyOf)
				Map<OWLProperty, Integer> propertyMap = propertiesPerOV.get(ov);
				if (propertyMap != null) {
					// Note: the signature allows multiple mappings per argument, but here we use only 1 (the first one which is not IMappingArgumentExtractor.UNDEFINED_CODE). 
					for (Entry<OWLProperty, Integer> entry : propertyMap.entrySet()) {
						if (entry.getValue().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
							isSubjectIn = false;
							owlProperty = entry.getKey();
							break;
						} else if (entry.getValue().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
							isSubjectIn = true;
							owlProperty = entry.getKey();
							break;
						} 
						// or else, entry.getValue() == IMappingArgumentExtractor.UNDEFINED_CODE), so find next
					}
				}else{
					throw new IllegalArgumentException("Argument " + ov + " of " + residentNode + " has no mapping to OWL property.");
				}

				if(isSubjectIn){
					query+= argument + SPACE + 
							"<" + owlProperty.getIRI() + ">" + " " + 
							"_:a1" + " " + 
							"." + " "; 
				}else{
					query+= " _:a1" + " " + 
							"<" + owlProperty.getIRI() + ">" + " " +   
							argument + SPACE + 
							"." + " "; 
				}
			}
		}
		
		if(!positive){
			query+= "} "; 
		}
		
		return query; 
		
	}	
	
	/*
	 * Evaluate AND clauses. Possibly this can be a set of AND clauses, and 
	 * possibly with a negation. 
	 * 
	 * Using a tree structure, a sequence of AND clauses will be in the format: 
	 *                     A AND (B AND (C AND (D AND E) 
	 * where each AND is a node of the tree with two paths. Each element A, B... E 
	 * can be a equal formula or a simple node. The evaluation build a List containing 
	 * each A, B... E element and evaluate then like an equivalent AND formula: 
	 *                        A AND B AND C AND D AND E 
	 * 
	 */
	private String makeBinaryStatement(NodeFormulaTree formulaTree,
			List<OVInstance> ovInstanceList, List<OrdinaryVariable> ovFaultList, String operator) {
		
		String query = "{"; 

		List<NodeFormulaTree> componentList = new ArrayList<NodeFormulaTree>(); 
		
		NodeFormulaTree operandToEvaluate = formulaTree.getChildren().get(0);
		componentList.add(operandToEvaluate); 
		
		NodeFormulaTree rOperand = formulaTree.getChildren().get(1);
		
		while(rOperand != null){
			if(rOperand.getTypeNode() == EnumType.SIMPLE_OPERATOR){
				BuiltInRV builtIn = (BuiltInRV) formulaTree.getNodeVariable();
				
				if(operator.equals(AND_OPERATOR) && (builtIn instanceof BuiltInRVAnd)){
					operandToEvaluate = rOperand.getChildren().get(0);
					componentList.add(operandToEvaluate); 
					rOperand = rOperand.getChildren().get(1); 
				}else{
					if(operator.equals(OR_OPERATOR) && (builtIn instanceof BuiltInRVOr)){
						operandToEvaluate = rOperand.getChildren().get(0);
						componentList.add(operandToEvaluate); 
						rOperand = rOperand.getChildren().get(1); 
					}else{
						if (builtIn instanceof BuiltInRVEqualTo){
							operandToEvaluate = rOperand;
							componentList.add(operandToEvaluate); 
							rOperand = null; 
						}else{
							throw new IllegalStateException("This implementation can't evaluate the context node formula: " + formulaTree);
						}
					}
				}
			}else{
				if(rOperand.getTypeNode() == EnumType.OPERAND){
					if(rOperand.getSubTypeNode() == EnumSubType.NODE){
						operandToEvaluate = rOperand;
						componentList.add(operandToEvaluate); 
						rOperand = null; 
					}else{
						throw new IllegalStateException("This implementation can't evaluate the context node formula: " + formulaTree);		
					}
				}else{
					throw new IllegalStateException("This implementation can't evaluate the context node formula: " + formulaTree);
				}
			}
		}
		
		//Evaluate list of clauses of AND: 
		boolean first = true; 
		
		for(NodeFormulaTree component: componentList ){
			
			if(!first){
				query+= " " + operator + " "; 
			}else{
				first = false; 
			}
			
			query+= "{" + makeAtomEvaluationStatement(component, ovInstanceList, ovFaultList) + "}"; 
			
		}
		
		query+="}"; 
		
		return query; 
	}
	
	/* 
	 * NodeFormulaTree: One of formats:
	 * -> booleanRV(ov1, ...)  
	 * -> ov1 == ov2
	 * -> nonBooleanRV(ov1, ...) = ov0 
	 * -> nonBooleanRV(ov1, ...) = CONST 
	 */
	private String makeAtomEvaluationStatement(NodeFormulaTree component,
			List<OVInstance> ovInstanceList, 
			List<OrdinaryVariable> ovFaultList){
		
		String query = ""; 
		
		switch(component.getTypeNode()){
		
		case OPERAND: 
			if(component.getSubTypeNode() == EnumSubType.NODE){
				
				ResidentNode residentNode = ((ResidentNodePointer)(component.getNodeVariable())).getResidentNode(); 	
				query = createBooleanNodeEvaluationQuery(residentNode, ovInstanceList, ovFaultList); 
			
			}else{
				throw new IllegalStateException("Invalid context node formula: " + component);		
			}
			break; 
			
		case SIMPLE_OPERATOR: 
			if(component.getSubTypeNode() == EnumSubType.EQUALTO){

				query = makeEqualStatement(component, ovInstanceList, ovFaultList, true);  
				
			}else{
				throw new IllegalStateException("Invalid context node formula: " + component);	
			}
			break; 
			
		default: 
			throw new IllegalStateException("Invalid context node formula: " + component);		
		}
		
		return query; 
	}	
	
	public List<String> evaluateSingleSearchContextNodeFormula(ContextNode context, List<OVInstance> ovInstanceList)throws OVInstanceFaultException {
		
		List<OrdinaryVariable> ovFaultList = context.getOVFaultForOVInstanceSet(ovInstanceList); 
		
		if(ovFaultList.size() != 1){
			throw new IllegalStateException("Method Evaluated Single Search Context Node Formula called with more than one ordinary variable don't filled.");	
		}
		
		// delegate query to another method
		SearchResult result = this.evaluateSearchContextNodeFormula(context, ovInstanceList);
		
		// prepare list to return
		List<String> ret = new ArrayList<String>();
		
		if (result == null) {
			return ret;
		}else{
			// fill list
			for (String[] values : result.getValuesResultList()) {
				if (values == null) {
					throw new IllegalStateException("Invalid result in evaluation of search context node.");	
				}else{
					if (values.length != 1){
						throw new IllegalStateException("Invalid result in evaluation of search context node.");	
					}else{
						ret.add(values[0]); 
					}
				}
			}

			return ret;
		}
	}

	

	
	
	
	//*************************     SEARCH  ************************************

	/*
	 * Context node with more than one ordinary variable not filled -> Select on base with multiple variables  
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateSearchContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public SearchResult evaluateSearchContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstanceList) {
		
		List<OrdinaryVariable> ovFaultList = context.getOVFaultForOVInstanceSet(ovInstanceList); 
		
		if(ovFaultList.size() == 0){
			throw new IllegalArgumentException("Search Context Node Method called for formula without empty ordinary variables."); 
		}
		
		System.out.println("Ordinary Variable Fault List: ");
		for(OrdinaryVariable ov: ovFaultList){
			System.out.println(ov.toString());
		}
		
		NodeFormulaTree formulaTree = (NodeFormulaTree) context
				.getFormulaTree();

		String query;
		
		query = GENERAL_PREFIX_DECLARATION + 
				"SELECT ";
		
		for(int i = 0; i < ovFaultList.size(); i++){
			query+= "?X" + i; 
			query+= " "; 
		}
				
		query+= "WHERE { ";
		
		//This is a typed implementation. Set the type of ordinary variable expected. 
		for(int i = 0; i < ovFaultList.size(); i++){
			
			Type type = ovFaultList.get(i).getValueType(); 
			ObjectEntity oe = this.defaultMEBN.getObjectEntityContainer().getObjectEntityByType(type); 
			IRI objectEntityType = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, oe);
			
			query+= ("?X" + i) + SPACE + RDF_TYPE + SPACE + "<" + objectEntityType + "> " + POINT + SPACE;
			
		}
		
		if ((formulaTree.getTypeNode() == EnumType.OPERAND)
				&& (formulaTree.getSubTypeNode() == EnumSubType.NODE)) {

			System.out.println("Formula envolving a boolean node. ");
			query+= makeBooleanNodeStatement(formulaTree, ovInstanceList, ovFaultList, true);
			
		} else {

			switch (formulaTree.getTypeNode()) {

			case SIMPLE_OPERATOR:

				BuiltInRV builtIn = (BuiltInRV) formulaTree.getNodeVariable();

				if(builtIn instanceof BuiltInRVAnd){
					query+= makeBinaryStatement(formulaTree, ovInstanceList, ovFaultList, AND_OPERATOR);
				}else
					if(builtIn instanceof BuiltInRVOr){
						query+= makeBinaryStatement(formulaTree, ovInstanceList, ovFaultList, OR_OPERATOR);
					}else
						if(builtIn instanceof BuiltInRVEqualTo){
							query+= makeEqualStatement(formulaTree, ovInstanceList, ovFaultList, true); 	
						}else
							if(builtIn instanceof BuiltInRVIff){
								Debug.println("Context Node formula using Iff. This implementation don't deal with this formula. ");
							}else
								if(builtIn instanceof BuiltInRVImplies){
									Debug.println("Context Node formula using implies. This implementation don't deal with this formula. ");
								}else
									if(builtIn instanceof BuiltInRVNot){
										
										formulaTree = formulaTree.getChildren().get(0); 
										
										if ((formulaTree.getTypeNode() == EnumType.OPERAND)
												&& (formulaTree.getSubTypeNode() == EnumSubType.NODE)) {

											ResidentNodePointer pointer = (ResidentNodePointer)formulaTree.getNodeVariable();

											if(pointer.getResidentNode().getTypeOfStates() != ResidentNode.BOOLEAN_RV_STATES){
												throw new IllegalArgumentException("This knowledge base cannot "
														+ "handle resident nodes with 0 arguments. Resident node = " + pointer.getResidentNode() + ".");
											}else{
												query+= makeBooleanNodeStatement(formulaTree, ovInstanceList, ovFaultList, false);
											}
										}else{
											if(formulaTree.getTypeNode() == EnumType.SIMPLE_OPERATOR){ 
												
												builtIn = (BuiltInRV) formulaTree.getNodeVariable();
												
												if(builtIn instanceof BuiltInRVEqualTo){

													query+= makeEqualStatement(formulaTree, ovInstanceList, ovFaultList, false); 	   
													
												}else{
													throw new IllegalArgumentException("Negation formula don't allowed by this implementation: " + formulaTree.toString());
												}
												
											}else{
												throw new IllegalArgumentException("Negation formula don't allowed by this implementation: " + formulaTree.toString());
											}
										}
									}
				
				break;

			case QUANTIFIER_OPERATOR:

				Debug.println("Context Node formula using Quantifier. This implementation don't deal with this formula. ");
				
			default:
				Debug.println("ERROR! type of operator don't found");

			}
		}
		
		query+= "}";

		System.out.println("Query = " + query);

		//The order of entities in listResult depend of the order of variables! 
		//Here, we generate the search based on position of node in ovFaultList. 
		List<String[]> listResult = null;

		try {
			listResult = triplestoreController.executeSelectQuery(query);
		} catch (InvalidQuerySintaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TriplestoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TriplestoreQueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		if(listResult != null){

			//TODO This only deal with a single fault ordinary variable! 

			OrdinaryVariable[] ovFault = new OrdinaryVariable[]{ovFaultList.get(0)}; 
			SearchResult searchResult = new SearchResult(ovFault);

			for(String[] line: listResult){
				searchResult.addResult(line);
			}

			return searchResult; 

		} else{

			return null; 

		}

	}
	


	/*
	 * Several context nodes with ordinary variables don't filled. Search on base the values of ordinary 
	 * variables that satisfies all context nodes on the same time. 
	 * 
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateMultipleSearchContextNodeFormula(java.util.List, java.util.List)
	 */
	
	//TODO Maybe remove this method. It isn't used in SSBN algorithm and is complicate to implement 
	//(PowerLoom uses a quantifier to implement).  
	
	public Map<OrdinaryVariable, List<String>> evaluateMultipleSearchContextNodeFormula(
			List<ContextNode> contextList, List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean existEntity(String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public StateLink searchFinding(ResidentNode residentNode,
			Collection<OVInstance> listArguments) {

		//We have to analyze the quantity of ordinary variables and verify the
		//property "definesUncertaintyOf"

		// initial assertions
		if (residentNode == null) {
			// it is impossible to search a finding if no variable is provided
			Debug.println(this.getClass(), "Attempted to search finding for null resident node.");
			return null;
		}
		
		if (residentNode.getArgumentList() == null || listArguments == null) {
			throw new IllegalArgumentException("This knowledge base cannot handle resident nodes with null arguments. Resident node = " + residentNode);
		}

		if (residentNode.getArgumentList().size() != listArguments.size()) {
			throw new IllegalArgumentException("Findings of " + residentNode + " should have " + residentNode.getArgumentList().size() + " arguments.");
		}

		if (listArguments.size() == 0) {
			// This version cannot represent resident nodes with no arguments
			throw new IllegalArgumentException("This knowledge base cannot handle resident nodes with 0 arguments. Resident node = " + residentNode);
		}

		if (triplestoreController.isConnected() == false) {
			throw new IllegalStateException("Database disconnected.");
		}

		
		String query = null; 

		// extract IRI of the property pointed by the resident node
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), residentNode);
		
		if (propertyIRI == null) {
			throw new IllegalStateException("Fail in search finding to node " + residentNode + "." + "\n" + 
		                    this.getDefaultMEBN() + " does not contain references to the OWL property related to resident node " + residentNode);
		}
		
		if (listArguments.size() == 1) {

			//01 - Boolean Node: should be a data property to boolean value  
			if (residentNode.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES){

				query = GENERAL_PREFIX_DECLARATION + 
						"SELECT ?X " + 
						"WHERE { " + "<" + listArguments.iterator().next().getEntity().getInstanceName()
						+ ">" + " " + "<" + propertyIRI + ">" + " " + "?X" + " . }"; 

			}else{ 
				//02 - Other Cases: this is a functional format (e.g. F(x) = y), but the 
				//possible state can be the subject (functional) or object (inverse functional) 

				Map<Argument, Map<OWLProperty, Integer>> argumentMappings = 
						getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
								residentNode, residentNode.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());
				
				if (argumentMappings != null) {
					// extract the mapping of the only argument
					Map<OWLProperty, Integer> singleArgMap = argumentMappings.get(residentNode.getArgumentList().get(0));
					if (singleArgMap != null) {
						// checking consistency
						if (singleArgMap.size() != 1) {
							throw new IllegalArgumentException("Found " + singleArgMap.size() + " mappings to arguments of node " + residentNode);
						}
						if (!singleArgMap.keySet().iterator().next().getIRI().equals(propertyIRI)) {
							throw new IllegalArgumentException("Node " + residentNode + " is defining uncertainty of " + propertyIRI 
									+ ", but its argument was mapped to owl property " + singleArgMap.keySet().iterator().next());
						}
						
						// at this pint, we know there are 1 argument and 1 mapping
						if (singleArgMap.values().iterator().next().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
							query = GENERAL_PREFIX_DECLARATION + 
									"SELECT ?X " + 
									"WHERE { " + "?X" + " " + "<" + propertyIRI + ">" + " " + 
									"<" + listArguments.iterator().next().getEntity().getInstanceName() + ">" + " "  + " . }"; 
						}
					} // Argument was not mapped with isSubjectIn or isObjectIn. Assume default
					if(query == null){
						query = GENERAL_PREFIX_DECLARATION + 
								"SELECT ?X " + 
								"WHERE { " + "<" + listArguments.iterator().next().getEntity().getInstanceName()
								+ ">" + " " + "<" + propertyIRI + ">" + " " + "?X" + " . }"; 
					}
				}
			}
		} else {
			//Deal with more than one argument 
			if ((listArguments.size() == 2) && 
					(residentNode.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES)){
				// 2 arguments. This is a simple binary relationship

				// extract the values of the arguments
				Iterator<OVInstance> it = listArguments.iterator();
				
				OVInstance ovi1 = it.next(); 
				OVInstance ovi2 = it.next();
				
				String subjectName = null; 
				String objectName = null; 
				
				if(residentNode.getOrdinaryVariableByIndex(0).equals(ovi1.getOv())){
					subjectName = ovi1.getEntity().getInstanceName();
					objectName = ovi2.getEntity().getInstanceName(); 
				}else{
					subjectName = ovi2.getEntity().getInstanceName();
					objectName = ovi1.getEntity().getInstanceName(); 
				}
				
				Map<Argument, Map<OWLProperty, Integer>> argumentMappings = getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
						residentNode, residentNode.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());
				
				Argument arg1 = residentNode.getArgumentList().get(0); 
				Argument arg2 = residentNode.getArgumentList().get(1); 
				
				if(!residentNode.getOrdinaryVariableByIndex(0).equals(arg1.getOVariable())){
					Argument temp = arg1;
					arg1 = arg2; 
					arg2 = arg1; 
				}
				
				if (argumentMappings != null) {
					// we know that at this point the resident node has 2 arguments (because the number of arguments in resident and number of entries in listArguments match)
					Map<OWLProperty, Integer> argMap1 = argumentMappings.get(arg1); // extract the mapping of the 1st argument
					if (argMap1 != null) {
						// checking consistency
						if (argMap1.size() != 1) {
							throw new IllegalArgumentException("Found " + argMap1.size() + " mappings to 1st argument of node " + residentNode);
						}
						if (!argMap1.keySet().iterator().next().getIRI().equals(propertyIRI)) {
							throw new IllegalArgumentException("Node " + residentNode + " is defining uncertainty of " + propertyIRI 
									+ ", but its 1st argument was mapped to owl property " + argMap1.keySet().iterator().next());
						}
						// at this point, we know there is 1 mapping to this argument
						if (argMap1.values().iterator().next().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
							// 1st argument is object, so swap the subjectName and objectName (because at this point the subject was assumed to be the 1st argument)
							String temp = objectName;
							objectName = subjectName;
							subjectName = temp;
						}	// if argument is either subject or unknown, then use default behavior
					}else{ 
						//Only is necessary see arg 2 if arg 1 is null
						Map<OWLProperty, Integer> argMap2 = argumentMappings.get(arg2); // extract the mapping of the 2nd argument
						if (argMap2 != null) {
							// checking consistency
							if (argMap2.size() != 1) {
								throw new IllegalArgumentException("Found " + argMap2.size() + " mappings to 2nd argument of node " + residentNode);
							}
							if (!argMap2.keySet().iterator().next().getIRI().equals(propertyIRI)) {
								throw new IllegalArgumentException("Node " + residentNode + " is defining uncertainty of " + propertyIRI 
										+ ", but its 2nd argument was mapped to owl property " + argMap2.keySet().iterator().next());
							}
							// at this point, we know there is 1 mapping to this argument
							if (argMap2.values().iterator().next().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
								// 2nd argument is subject, so swap the subjectName and objectName (because at this point the subject was assumed to be the 1st argument)
								String temp = objectName;
								objectName = subjectName;
								subjectName = temp;
							}	// if argument is either subject or unknown, then use default behavior
						}
					}
				}
				
				query = GENERAL_PREFIX_DECLARATION + 
						"ASK WHERE { " + 
						"<" + subjectName + ">" + " " +
						"<" + propertyIRI + ">" + " " + 
						"<" + objectName + ">" + "}"; 
				
			}else{
				
				//Boolean with more than two arguments or others cases with more than one
				
				// get the owl properties related (by subjectIn or objectIn) to the arguments of this node
				Map<Argument, Map<OWLProperty, Integer>> propertiesPerArgument = 
						getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
								residentNode, residentNode.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());

				if (propertiesPerArgument == null || propertiesPerArgument.isEmpty()) {
					// a node with no arguments mapped to OWL properties cannot have findings anyway
					try {
						Debug.println(getClass(), "There is no mapping specified for n-ary relationship of node " + residentNode);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return null;
				}
				if (propertiesPerArgument.size() != residentNode.getArgumentList().size()) {
					try {
						Debug.println(getClass(), "The n-ary relationship of node " + residentNode + " is not fully mapped to OWL properties.");
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return null;
				}

				// translate the mapping to a map of ordinary variables to properties, because listArguments uses ordinary variables as reference
				Map<OrdinaryVariable, Map<OWLProperty, Integer>> propertiesPerOV = 
						new HashMap<OrdinaryVariable, Map<OWLProperty,Integer>>();

				for (Entry<Argument, Map<OWLProperty, Integer>> entry : propertiesPerArgument.entrySet()) {
					propertiesPerOV.put(entry.getKey().getOVariable(), entry.getValue());
				}

				OWLDataFactory factory = getOWLOntology().getOWLOntologyManager().getOWLDataFactory();

				// Extract the OWL object property pointed by definesUncertaintyOf.
				// At least 1 argument must be using it either in subjectIn or objectIn. 
				// If not, by default the 1st unspecified argument will be considered as the subject of this property

				OWLObjectProperty mainProperty = factory.getOWLObjectProperty(propertyIRI);

				
				if (residentNode.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES){
					
					query = GENERAL_PREFIX_DECLARATION + 
							"ASK WHERE { " ; 
					
					// Note: we already checked at the beginning of this method that listArgument.size() == resident.getArgumentList().size()
					for (Iterator<OVInstance> iterator = listArguments.iterator(); iterator.hasNext(); ) {
						// I'm using an explicit iterator, because expressionToParse shall include an "and" at the end of expression
						OVInstance argInstance = iterator.next();

						// if there is no valid mapping, use default (use the one in definesUncertaintyOf, and isSubjectIn)
						OWLProperty property = null; 
						boolean isSubjectIn = true;
						
						// check if there is any argument without mapping. If not, use default behavior (use the property specified in definesUncertaintyOf)
						Map<OWLProperty, Integer> propertyMap = propertiesPerOV.get(argInstance.getOv());
						if (propertyMap != null) {
							// Note: the signature allows multiple mappings per argument, but here we use only 1 (the first one which is not IMappingArgumentExtractor.UNDEFINED_CODE). 
							for (Entry<OWLProperty, Integer> entry : propertyMap.entrySet()) {
								if (entry.getValue().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
									isSubjectIn = false;
									property = entry.getKey();
									break;
								} else if (entry.getValue().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
									isSubjectIn = true;
									property = entry.getKey();
									break;
								} 
								// or else, entry.getValue() == IMappingArgumentExtractor.UNDEFINED_CODE), so find next
							}
						}else{
							throw new IllegalArgumentException("Argument " + argInstance + " of " + residentNode + " has no mapping to OWL property.");
						}

						if(isSubjectIn){
							query+= "<" + argInstance.getEntity().getInstanceName() + "> " + 
									"<" + property.getIRI() + ">" + " " + 
									"_:a1" + " " + 
									"." + " "; 
						}else{
							query+= " _:a1" + " " + 
									"<" + property.getIRI() + ">" + " " +   
									"<" + argInstance.getEntity().getInstanceName() + "> " + 
									"." + " "; 
						}
					}

					query+= "}"; 
					
				}else{
					// this is a non boolean n-ary relationship with n => 2

					// from the mapping, create an expression that returns the subject if the subject has a link to object.
					// example 1: inverse inv_MTI value Slow and inverse inv_MTI_RPT value Rpt2 and inverse inv_MTI_T value T1
					// example 2: MTI value Fast and MTI_RPT value Rpt1 and MTI_T value T1
					
					query = GENERAL_PREFIX_DECLARATION + 
							"SELECT ?X WHERE { _:a1 " + "<" + mainProperty.getIRI() + "> " + " ?X . "; 
					
					// Note: we already checked at the beginning of this method that listArgument.size() == resident.getArgumentList().size()
					for (Iterator<OVInstance> iterator = listArguments.iterator(); iterator.hasNext(); ) {
						// I'm using an explicit iterator, because expressionToParse shall include an "and" at the end of expression
						OVInstance argInstance = iterator.next();

						// if there is no valid mapping, use default (use the one in definesUncertaintyOf, and isSubjectIn)
						OWLProperty property = null; 
						boolean isSubjectIn = true;
						
						// check if there is any argument without mapping. If not, use default behavior (use the property specified in definesUncertaintyOf)
						Map<OWLProperty, Integer> propertyMap = propertiesPerOV.get(argInstance.getOv());
						if (propertyMap != null) {
							// Note: the signature allows multiple mappings per argument, but here we use only 1 (the first one which is not IMappingArgumentExtractor.UNDEFINED_CODE). 
							for (Entry<OWLProperty, Integer> entry : propertyMap.entrySet()) {
								if (entry.getValue().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
									isSubjectIn = false;
									property = entry.getKey();
									break;
								} else if (entry.getValue().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
									isSubjectIn = true;
									property = entry.getKey();
									break;
								} 
								// or else, entry.getValue() == IMappingArgumentExtractor.UNDEFINED_CODE), so find next
							}
						}else{
							throw new IllegalArgumentException("Argument " + argInstance + " of " + residentNode + " has no mapping to OWL property.");
						}

						if(isSubjectIn){
							query+= "<" + argInstance.getEntity().getInstanceName() + "> " + 
									"<" + property.getIRI() + ">" + " " + 
									"_:a1" + " " + 
									"." + " "; 
						}else{
							query+= " _:a1" + " " + 
									"<" + property.getIRI() + ">" + " " +   
									"<" + argInstance.getEntity().getInstanceName() + "> " + 
									"." + " "; 
						}
					}

					query+= "}"; 
				}
			}
		} // if 
		
		Debug.println(this.getClass(), "Expression: " + query);

		if((residentNode.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES) && 
				residentNode.getArgumentList().size() > 1) {

			try {
				boolean result = triplestoreController.executeAskQuery(query);

				StateLink link = residentNode.getPossibleValueByName("" + result); 

				if(link != null){
					System.out.println("Returned: " + link);
					return link; 
				}
			} catch (InvalidQuerySintaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TriplestoreQueryEvaluationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TriplestoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

		}else{
			
			List<String[]> resultList = null;

			try {
				//TODO Booolean Node case (except for the case with only one ordinary variable, 
				//     the others don't will return true or false. 
				resultList = triplestoreController.executeSelectQuery(query);
			} catch (InvalidQuerySintaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TriplestoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TriplestoreQueryEvaluationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} 

			if(resultList == null){
				return null; 
			}

			if(resultList.size() == 0){
				if ((residentNode.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES)&&
						(residentNode.getArgumentList().size() == 1)){
					//Closed World Assumption - If the base haven't a statement, it is false
					return residentNode.getPossibleValueByName("false"); 
				}else{
					return null; 
				}
			}

			if(resultList.size() > 1){
				throw new IllegalStateException("Query returned more than one result!");
			}

			String[] resultLine = resultList.get(0); 
			if(resultLine.length > 1){
				throw new IllegalStateException("Query returned more than one result!");
			}

			System.out.println(resultLine[0]);

			if (residentNode.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES){

				StateLink link = residentNode.getPossibleValueByName(resultLine[0]); 

				if(link != null){
					System.out.println("Returned: " + link);
					return link; 
				}
			} else{
				if(residentNode.getTypeOfStates() == ResidentNode.CATEGORY_RV_STATES){
					String iriStateReturned = resultLine[0];
					System.out.println("State Returned = " + iriStateReturned);

					Entity stateFinding = null; 

					for(Entity entity: residentNode.getPossibleValueList()){
						IRI entityIRIWhenLoaded = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.getDefaultMEBN(), entity);
						System.out.println(entityIRIWhenLoaded);
						if(entityIRIWhenLoaded != null){
							if(entityIRIWhenLoaded.toString().equals(iriStateReturned)){
								stateFinding = entity; 
								System.out.println("Finding state = " + stateFinding);
							}
						}else{
							if(!entity.getName().equals("absurd")){
								Debug.println("Error trying find IRI for possible state entity = " + entity);
							}
						}
					}

					if(stateFinding!=null){
						StateLink link = residentNode.getPossibleValueByName(stateFinding.getName()); 
						System.out.println("Link = " + link);
						return link; 
					}else{
						Debug.println("Finding for a state not possible");
					}

				}else{
					if (residentNode.getTypeOfStates() == ResidentNode.OBJECT_ENTITY){
						Entity e = residentNode.getPossibleValueList().get(0); 
						if (e instanceof ObjectEntity){
							ObjectEntityInstance oei = new ObjectEntityInstance(resultLine[0], (ObjectEntity)e); 
							StateLink link = new StateLink(oei); 
							return link; 
						}
					}
				}
			}
		}
		
		return null; 
	}
	
	private String makeSearchFindingNodeEvaluationExpression(ResidentNode resident,
			Collection<OVInstance> listArguments) {
		
		String query = null;
		
		// extract IRI of the property pointed by the resident node
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), resident);
		
		if (propertyIRI == null) {
			throw new IllegalStateException("Fail in search finding to node " + resident + "." + "\n" + 
		                    this.getDefaultMEBN() + " does not contain references to the OWL property related to resident node " + resident);
		}
		
		if (listArguments.size() == 1) {

			//01 - Boolean Node: should be a data property to boolean value  
			if (resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES){

				query = GENERAL_PREFIX_DECLARATION + 
						"SELECT ?X " + 
						"WHERE { " + "<" + listArguments.iterator().next().getEntity().getInstanceName()
						+ ">" + " " + "<" + propertyIRI + ">" + " " + "?X" + " . }"; 

			}else{ 
				//02 - Other Cases: this is a functional format (e.g. F(x) = y), but the 
				//possible state can be the subject (functionl) or object (inverse functional) 

				Map<Argument, Map<OWLProperty, Integer>> argumentMappings = getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(resident, resident.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());
				
				if (argumentMappings != null) {
					// extract the mapping of the only argument
					Map<OWLProperty, Integer> singleArgMap = argumentMappings.get(resident.getArgumentList().get(0));
					if (singleArgMap != null) {
						// checking consistency
						if (singleArgMap.size() != 1) {
							throw new IllegalArgumentException("Found " + singleArgMap.size() + " mappings to arguments of node " + resident);
						}
						if (!singleArgMap.keySet().iterator().next().getIRI().equals(propertyIRI)) {
							throw new IllegalArgumentException("Node " + resident + " is defining uncertainty of " + propertyIRI 
									+ ", but its argument was mapped to owl property " + singleArgMap.keySet().iterator().next());
						}
						
						// at this pint, we know there are 1 argument and 1 mapping
						if (singleArgMap.values().iterator().next().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
							query = GENERAL_PREFIX_DECLARATION + 
									"SELECT ?X " + 
									"WHERE { " + "?X" + " " + "<" + propertyIRI + ">" + " " + 
									"<" + listArguments.iterator().next().getEntity().getInstanceName() + ">" + " "  + " . }"; 
						}
					} // Argument was not mapped with isSubjectIn or isObjectIn. Assume default
					if(query == null){
						query = GENERAL_PREFIX_DECLARATION + 
								"SELECT ?X " + 
								"WHERE { " + "<" + listArguments.iterator().next().getEntity().getInstanceName()
								+ ">" + " " + "<" + propertyIRI + ">" + " " + "?X" + " . }"; 
					}
				}
			}

		} else {
			//Deal with more than one argument 
			if ((listArguments.size() == 2) && 
					(resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES)){
				// 2 arguments. This is a simple binary relationship

				// extract the values of the arguments
				Iterator<OVInstance> it = listArguments.iterator();
				
				OVInstance ovi1 = it.next(); 
				OVInstance ovi2 = it.next(); 
				
				String subjectName = null; 
				String objectName = null; 
				
				if(resident.getOrdinaryVariableByIndex(0).equals(ovi1.getOv())){
					subjectName = ovi1.getEntity().getInstanceName();
					objectName = ovi2.getEntity().getInstanceName(); 
				}else{
					subjectName = ovi2.getEntity().getInstanceName();
					objectName = ovi1.getEntity().getInstanceName(); 
				}
				
				Map<Argument, Map<OWLProperty, Integer>> argumentMappings = getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
						resident, resident.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());
				
				if (argumentMappings != null) {
					// we know that at this point the resident node has 2 arguments (because the number of arguments in resident and number of entries in listArguments match)
					Map<OWLProperty, Integer> argMap1 = argumentMappings.get(resident.getArgumentList().get(0)); // extract the mapping of the 1st argument
					if (argMap1 != null) {
						// checking consistency
						if (argMap1.size() != 1) {
							throw new IllegalArgumentException("Found " + argMap1.size() + " mappings to 1st argument of node " + resident);
						}
						if (!argMap1.keySet().iterator().next().getIRI().equals(propertyIRI)) {
							throw new IllegalArgumentException("Node " + resident + " is defining uncertainty of " + propertyIRI 
									+ ", but its 1st argument was mapped to owl property " + argMap1.keySet().iterator().next());
						}
						// at this point, we know there is 1 mapping to this argument
						if (argMap1.values().iterator().next().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
							// 1st argument is object, so swap the subjectName and objectName (because at this point the subject was assumed to be the 1st argument)
							String temp = objectName;
							objectName = subjectName;
							subjectName = temp;
						}	// if argument is either subject or unknown, then use default behavior
					} 
					Map<OWLProperty, Integer> argMap2 = argumentMappings.get(resident.getArgumentList().get(1)); // extract the mapping of the 2nd argument
					if (argMap2 != null) {
						// checking consistency
						if (argMap2.size() != 1) {
							throw new IllegalArgumentException("Found " + argMap2.size() + " mappings to 2nd argument of node " + resident);
						}
						if (!argMap2.keySet().iterator().next().getIRI().equals(propertyIRI)) {
							throw new IllegalArgumentException("Node " + resident + " is defining uncertainty of " + propertyIRI 
									+ ", but its 2nd argument was mapped to owl property " + argMap2.keySet().iterator().next());
						}
						// at this point, we know there is 1 mapping to this argument
						if (argMap2.values().iterator().next().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
							// 2nd argument is subject, so swap the subjectName and objectName (because at this point the subject was assumed to be the 1st argument)
							String temp = objectName;
							objectName = subjectName;
							subjectName = temp;
						}	// if argument is either subject or unknown, then use default behavior
					}
				}
				
				query = GENERAL_PREFIX_DECLARATION + 
						"ASK WHERE { " + 
						"<" + subjectName + ">" + " " +
						"<" + propertyIRI + ">" + " " + 
						"<" + objectName + ">" + "}"; 
				
			}else{
				
				// get the owl properties related (by subjectIn or objectIn) to the arguments of this node
				Map<Argument, Map<OWLProperty, Integer>> propertiesPerArgument = 
						getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
								resident, resident.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());

				if (propertiesPerArgument == null || propertiesPerArgument.isEmpty()) {
					// a node with no arguments mapped to OWL properties cannot have findings anyway
					try {
						Debug.println(getClass(), "There is no mapping specified for n-ary relationship of node " + resident);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return null;
				}
				if (propertiesPerArgument.size() != resident.getArgumentList().size()) {
					try {
						Debug.println(getClass(), "The n-ary relationship of node " + resident + " is not fully mapped to OWL properties.");
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return null;
				}

				// translate the mapping to a map of ordinary variables to properties, because listArguments uses ordinary variables as reference
				Map<OrdinaryVariable, Map<OWLProperty, Integer>> propertiesPerOV = 
						new HashMap<OrdinaryVariable, Map<OWLProperty,Integer>>();

				for (Entry<Argument, Map<OWLProperty, Integer>> entry : propertiesPerArgument.entrySet()) {
					propertiesPerOV.put(entry.getKey().getOVariable(), entry.getValue());
				}

				OWLDataFactory factory = getOWLOntology().getOWLOntologyManager().getOWLDataFactory();

				// Extract the OWL object property pointed by definesUncertaintyOf.
				// At least 1 argument must be using it either in subjectIn or objectIn. 
				// If not, by default the 1st unspecified argument will be considered as the subject of this property

				OWLObjectProperty mainProperty = factory.getOWLObjectProperty(propertyIRI);

				
				if (resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES){
					
					query = GENERAL_PREFIX_DECLARATION + 
							"ASK WHERE { " ; 
					
					// Note: we already checked at the beginning of this method that listArgument.size() == resident.getArgumentList().size()
					for (Iterator<OVInstance> iterator = listArguments.iterator(); iterator.hasNext(); ) {
						// I'm using an explicit iterator, because expressionToParse shall include an "and" at the end of expression
						OVInstance argInstance = iterator.next();

						// if there is no valid mapping, use default (use the one in definesUncertaintyOf, and isSubjectIn)
						OWLProperty property = null; 
						boolean isSubjectIn = true;
						
						// check if there is any argument without mapping. If not, use default behavior (use the property specified in definesUncertaintyOf)
						Map<OWLProperty, Integer> propertyMap = propertiesPerOV.get(argInstance.getOv());
						if (propertyMap != null) {
							// Note: the signature allows multiple mappings per argument, but here we use only 1 (the first one which is not IMappingArgumentExtractor.UNDEFINED_CODE). 
							for (Entry<OWLProperty, Integer> entry : propertyMap.entrySet()) {
								if (entry.getValue().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
									isSubjectIn = false;
									property = entry.getKey();
									break;
								} else if (entry.getValue().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
									isSubjectIn = true;
									property = entry.getKey();
									break;
								} 
								// or else, entry.getValue() == IMappingArgumentExtractor.UNDEFINED_CODE), so find next
							}
						}else{
							throw new IllegalArgumentException("Argument " + argInstance + " of " + resident + " has no mapping to OWL property.");
						}

						if(isSubjectIn){
							query+= "<" + argInstance.getEntity().getInstanceName() + "> " + 
									"<" + property.getIRI() + ">" + " " + 
									"_:a1" + " " + 
									"." + " "; 
						}else{
							query+= " _:a1" + " " + 
									"<" + property.getIRI() + ">" + " " +   
									"<" + argInstance.getEntity().getInstanceName() + "> " + 
									"." + " "; 
						}
					}

					query+= "}"; 
					
				}else{
					// this is a boolean n-ary relationship with n => 2

					// from the mapping, create an expression that returns the subject if the subject has a link to object.
					// example 1: inverse inv_MTI value Slow and inverse inv_MTI_RPT value Rpt2 and inverse inv_MTI_T value T1
					// example 2: MTI value Fast and MTI_RPT value Rpt1 and MTI_T value T1
					
					query = GENERAL_PREFIX_DECLARATION + 
							"SELECT ?X WHERE { _:a1 " + "<" + mainProperty.getIRI() + "> " + " ?X . "; 
					
					// Note: we already checked at the beginning of this method that listArgument.size() == resident.getArgumentList().size()
					for (Iterator<OVInstance> iterator = listArguments.iterator(); iterator.hasNext(); ) {
						// I'm using an explicit iterator, because expressionToParse shall include an "and" at the end of expression
						OVInstance argInstance = iterator.next();

						// if there is no valid mapping, use default (use the one in definesUncertaintyOf, and isSubjectIn)
						OWLProperty property = null; 
						boolean isSubjectIn = true;
						
						// check if there is any argument without mapping. If not, use default behavior (use the property specified in definesUncertaintyOf)
						Map<OWLProperty, Integer> propertyMap = propertiesPerOV.get(argInstance.getOv());
						if (propertyMap != null) {
							// Note: the signature allows multiple mappings per argument, but here we use only 1 (the first one which is not IMappingArgumentExtractor.UNDEFINED_CODE). 
							for (Entry<OWLProperty, Integer> entry : propertyMap.entrySet()) {
								if (entry.getValue().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
									isSubjectIn = false;
									property = entry.getKey();
									break;
								} else if (entry.getValue().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
									isSubjectIn = true;
									property = entry.getKey();
									break;
								} 
								// or else, entry.getValue() == IMappingArgumentExtractor.UNDEFINED_CODE), so find next
							}
						}else{
							throw new IllegalArgumentException("Argument " + argInstance + " of " + resident + " has no mapping to OWL property.");
						}

						if(isSubjectIn){
							query+= "<" + argInstance.getEntity().getInstanceName() + "> " + 
									"<" + property.getIRI() + ">" + " " + 
									"_:a1" + " " + 
									"." + " "; 
						}else{
							query+= " _:a1" + " " + 
									"<" + property.getIRI() + ">" + " " +   
									"<" + argInstance.getEntity().getInstanceName() + "> " + 
									"." + " "; 
						}
					}

					query+= "}"; 
				}
			}
		} // if 
		
		Debug.println(this.getClass(), "Expression: " + query);
		
		return query;
	}
	
	/*
	 * Evaluate a resident node evaluation query, possibly with ordinary variable don't filled. 
	 * 
	 * > OVInstanceList + OVFaultList should contain all ordinary variables of resident node. 
	 * > Each ordinary variable in ovFaultList can originate an variable Xi in SPARQL query, 
	 *   where i is the index of ov in ovFaultList. If you are evaluating a multiple atom 
	 *   formula like a AND chain or a OR chain, the ovFaultList should be the same in the 
	 *   evaluation of each atom, because the position of each ov in list will give origin 
	 *   to the name of SPARQL variable. 
	 */
	private String createBooleanNodeEvaluationQuery(ResidentNode resident,
			Collection<OVInstance> ovInstanceList, List<OrdinaryVariable> ovFaultList){
		
		String query = ""; 
		//We have to analyze the quantity of ordinary variables and verify the
		//property "definesUncertaintyOf"

		// initial assertions
		if (resident == null) {
			// it is impossible to search a finding if no variable is provided
			throw new IllegalArgumentException("Attempted to search finding for null resident node.");
		}
		
		if (resident.getArgumentList() == null || ovInstanceList == null) {
			throw new IllegalArgumentException("This knowledge base cannot handle resident nodes with null arguments. Resident node = " + resident);
		}
		
		String[] variableInstanceList = new String[resident.getOrdinaryVariableList().size()]; 
		
		OrdinaryVariable[] residentOVList = 
				resident.getOrdinaryVariableList().toArray(new OrdinaryVariable[resident.getOrdinaryVariableList().size()]);  
				
		//Verify if each resident node ov is in one of the ordinary variables lists. 

		for(int i = 0; i < residentOVList.length; i++){
			
			boolean find = false; 
			
			OrdinaryVariable ov = residentOVList[i]; 
			
			for(OVInstance ovInstance: ovInstanceList){
				if(ovInstance.getOv().equals(ov)){
					find=true; 
					variableInstanceList[i] = "<" + ovInstance.getEntity().getInstanceName() + ">" ; 
					break; 
				}
			}
			
			if(!find){
				int index = ovFaultList.indexOf(ov); 
				
				if(index <= 0){
					throw new IllegalArgumentException("Ordinary Variable don't filled.");
				}
				variableInstanceList[i] = "?X" + index; 
			}
		}

		// extract IRI of the property pointed by the resident node
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), resident);
		if (propertyIRI == null) {
			throw new IllegalStateException("Fail in evaluation of node " + resident + "." + "\n" + 
		                    this.getDefaultMEBN() + " does not contain references to the OWL property related to resident node " + resident);
		}

		if (residentOVList.length == 1) {

			//1 - Boolean Data Property
			
			// SELECT ?X WHERE ENTITY PROPERTY ?X
			query+= "" + variableInstanceList[0]
					+ " " + "<" + propertyIRI + "> " + "true " + " . ";

		} else {
			//Deal with more than one argument 
			if (residentOVList.length == 2){
				// 2 arguments. This is a simple binary relationship
				
				String subjectName = variableInstanceList[0];  
				String objectName = variableInstanceList[1]; 
				
				Map<Argument, Map<OWLProperty, Integer>> argumentMappings = getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
						resident, resident.getMFrag().getMultiEntityBayesianNetwork(), getOWLOntology());
				
				Argument arg1 = resident.getArgumentList().get(0); 
				Argument arg2 = resident.getArgumentList().get(1); 
				
				if(!resident.getOrdinaryVariableByIndex(0).equals(arg1.getOVariable())){
					Argument temp = arg1;
					arg1 = arg2; 
					arg2 = arg1; 
				}
				
				if (argumentMappings != null) {
					// we know that at this point the resident node has 2 arguments (because the number of arguments in resident and number of entries in listArguments match)
					Map<OWLProperty, Integer> argMap1 = argumentMappings.get(arg1); // extract the mapping of the 1st argument
					if (argMap1 != null) {
						// checking consistency
						if (argMap1.size() != 1) {
							throw new IllegalArgumentException("Found " + argMap1.size() + " mappings to 1st argument of node " + resident);
						}
						if (!argMap1.keySet().iterator().next().getIRI().equals(propertyIRI)) {
							throw new IllegalArgumentException("Node " + resident + " is defining uncertainty of " + propertyIRI 
									+ ", but its 1st argument was mapped to owl property " + argMap1.keySet().iterator().next());
						}
						// at this point, we know there is 1 mapping to this argument
						if (argMap1.values().iterator().next().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
							// 1st argument is object, so swap the subjectName and objectName (because at this point the subject was assumed to be the 1st argument)
							String temp = objectName;
							objectName = subjectName;
							subjectName = temp;
						}	// if argument is either subject or unknown, then use default behavior
					}else{
						Map<OWLProperty, Integer> argMap2 = argumentMappings.get(arg2); // extract the mapping of the 2nd argument
						if (argMap2 != null) {
							// checking consistency
							if (argMap2.size() != 1) {
								throw new IllegalArgumentException("Found " + argMap2.size() + " mappings to 2nd argument of node " + resident);
							}
							if (!argMap2.keySet().iterator().next().getIRI().equals(propertyIRI)) {
								throw new IllegalArgumentException("Node " + resident + " is defining uncertainty of " + propertyIRI 
										+ ", but its 2nd argument was mapped to owl property " + argMap2.keySet().iterator().next());
							}
							// at this point, we know there is 1 mapping to this argument
							if (argMap2.values().iterator().next().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
								// 2nd argument is subject, so swap the subjectName and objectName (because at this point the subject was assumed to be the 1st argument)
								String temp = objectName;
								objectName = subjectName;
								subjectName = temp;
							}	// if argument is either subject or unknown, then use default behavior
						}
					}
				}
				
				query+= subjectName + " " +
						"<" + propertyIRI + ">" + " " + 
						objectName + ". "; 

				System.out.println(query);

			}else{
				// this is a boolean n-ary relationship with n > 2

				OWLOntology ontology = getOWLOntology(); 

				// Extract the OWL object property pointed by definesUncertaintyOf.
				// At least 1 argument must be using it either in subjectIn or objectIn. 
				// If not, by default the 1st unspecified argument will be considered as the subject of this property

				// get the owl properties related (by subjectIn or objectIn) to the arguments of this node
				Map<Argument, Map<OWLProperty, Integer>> propertiesPerArgument = 
						getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(
								resident, resident.getMFrag().getMultiEntityBayesianNetwork(), ontology);

				if (propertiesPerArgument == null || propertiesPerArgument.isEmpty()) {
					// a node with no arguments mapped to OWL properties cannot have findings anyway
					try {
						Debug.println(getClass(), "There is no mapping specified for n-ary relationship of node " + resident);
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return null;
				}
				if (propertiesPerArgument.size() != resident.getArgumentList().size()) {
					try {
						Debug.println(getClass(), "The n-ary relationship of node " + resident + " is not fully mapped to OWL properties.");
					} catch (Throwable t) {
						t.printStackTrace();
					}
					return null;
				}

				// translate the mapping to a map of ordinary variables to properties, because listArguments uses ordinary variables as reference
				Map<OrdinaryVariable, Map<OWLProperty, Integer>> propertiesPerOV = 
						new HashMap<OrdinaryVariable, Map<OWLProperty,Integer>>();

				for (Entry<Argument, Map<OWLProperty, Integer>> entry : propertiesPerArgument.entrySet()) {
					propertiesPerOV.put(entry.getKey().getOVariable(), entry.getValue());
				}

				OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();

				OWLObjectProperty mainProperty = factory.getOWLObjectProperty(propertyIRI);

				String blankNode = createBlankNode();
				// from the mapping, create an expression that returns the subject if the subject has a link to object.
				// example 1: inverse inv_MTI value Slow and inverse inv_MTI_RPT value Rpt2 and inverse inv_MTI_T value T1
				// example 2: MTI value Fast and MTI_RPT value Rpt1 and MTI_T value T1
				
				// Note: we already checked at the beginning of this method that listArgument.size() == resident.getArgumentList().size()
//				for (Iterator<OVInstance> iterator = ovInstanceList.iterator(); iterator.hasNext(); ) {
				for (int i = 0; i < residentOVList.length; i++) {
						
					OrdinaryVariable ov = residentOVList[i]; 
					
					// if there is no valid mapping, use default (use the one in definesUncertaintyOf, and isSubjectIn)
					OWLProperty property = mainProperty;
					
					boolean isSubjectIn = true;
					
					// check if there is any argument without mapping. If not, use default behavior (use the property specified in definesUncertaintyOf)
					Map<OWLProperty, Integer> propertyMap = propertiesPerOV.get(ov);
					if (propertyMap != null) {
						// Note: the signature allows multiple mappings per argument, but here we use only 1 (the first one which is not IMappingArgumentExtractor.UNDEFINED_CODE). 
						for (Entry<OWLProperty, Integer> entry : propertyMap.entrySet()) {
							if (entry.getValue().equals(IMappingArgumentExtractor.OBJECT_CODE)) {
								isSubjectIn = false;
								property = entry.getKey();
								break;
							} else if (entry.getValue().equals(IMappingArgumentExtractor.SUBJECT_CODE)) {
								isSubjectIn = true;
								property = entry.getKey();
								break;
							} 
							// or else, entry.getValue() == IMappingArgumentExtractor.UNDEFINED_CODE), so find next
						}
					}

					if(isSubjectIn){
						query+= variableInstanceList[i] + 
								"<" + property.getIRI() + ">" + " " + 
								blankNode + " " + 
								"." + " "; 
					}else{
						query+= " " + blankNode + " " + 
								"<" + property.getIRI() + ">" + " " +   
								variableInstanceList[i] + 
								"." + " "; 
					}
				} 

				Debug.println(this.getClass(), "Expression: " + query);
			}
		}		
		
		return query; 
		
	}

	public List<String> getEntityByType(String type) {

		List<String> resultList = new ArrayList<String>(); 

		Type typeMEBN = this.defaultMEBN.getTypeContainer().getType(type); 
		
		if(typeMEBN == null){
			//TODO Use same Exception scheme
			System.out.println("Type not found: " + type);
			return null; 
		}

		ObjectEntity objectEntity = 
				this.defaultMEBN.getObjectEntityContainer().getObjectEntityByType(typeMEBN); 
		
		if(objectEntity == null){
			System.out.println("Object Entity not found for type " + type);
			return null; 
		}

		IRI entityIRIWhenLoaded = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, objectEntity);

		if(entityIRIWhenLoaded == null){
			System.out.println("IRI for Object Entity not found" + type);
			return null; 
		}else{
			Debug.println("IRI = " + entityIRIWhenLoaded );		
		}

		String query = GENERAL_PREFIX_DECLARATION + 
				"SELECT ?X " + 
				"WHERE { ?X rdf:type " + "<" + entityIRIWhenLoaded + ">" + " . }"; 

		Debug.println("Query = " + query);

		List<String[]> listResults = null;  
		
		try {
			listResults = triplestoreController.executeSelectQuery(query);
		} catch (InvalidQuerySintaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TriplestoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TriplestoreQueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(listResults != null){ 
			
			for(String[] listEntities: listResults){
				for(String entity: listEntities){
					resultList.add(entity); 
				}
			}
		}

		return resultList;
	}

	public MultiEntityBayesianNetwork getDefaultMEBN() {
		return defaultMEBN;
	}


	public OWLOntology getOWLOntology(){

		//In the actual version, is necessary have the OWL reasoner running, because we 
		//use it for get the original OWL ontology. 
		//TODO This have to be changed in future versions... 

		OWLReasoner reasoner = this.getDefaultOWLReasoner(); 

		if (reasoner == null) {
			throw new IllegalStateException("No OWL reasoner provided.");
		}

		return reasoner.getRootOntology(); 
		
	}

	private OWLReasoner getDefaultOWLReasoner() {

		OWLReasoner reasoner = null;
		try {
			if (this.getDefaultMEBN() != null
					&& this.getDefaultMEBN().getStorageImplementor() != null 
					&& this.getDefaultMEBN().getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator) {
				reasoner = ((IOWLAPIStorageImplementorDecorator)this.getDefaultMEBN().getStorageImplementor()).getOWLReasoner();
			}
		} catch (Throwable t) {
			// it is OK, because we can try extracting the reasoner when KB methods are called and MEBN is passed as arguments
			try {
				Debug.println(this.getClass(), "Could not extract reasoner from MEBN " + this.getDefaultMEBN(), t);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		// if reasoner is not available, use the one extracted from MEBN
		Debug.println(this.getClass(), "Extracted reasoner from MEBN: " + reasoner);

		return reasoner;

	}

	/**
	 * This object is used in order to handle the mappings (between OWL and MEBN) of arguments of nodes.
	 * The method {@link IMappingArgumentExtractor#getOWLPropertiesOfArgumentsOfSelectedNode(unbbayes.prs.INode, MultiEntityBayesianNetwork, OWLOntology)}
	 * is used for example in {@link #searchFinding(ResidentNode, Collection)} and {@link #evaluateSearchContextNodeFormula(ContextNode, List)} in order to get what
	 * OWL properties are referenced by objectIn or subjectIn.
	 * @return the mappingArgumentExtractor
	 */
	public IMappingArgumentExtractor getMappingArgumentExtractor() {
		return mappingArgumentExtractor;
	}

	@Override
	public String getSupportedLocalFileDescription(boolean isLoad) {
		// TODO Auto-generated method stub
		return null;
	}

	private String createBlankNode(){
		blankNodeNumber++; 
		return "_:a" + blankNodeNumber; 
	}
	
}
