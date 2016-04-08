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
import unbbayes.prs.mebn.MFrag;
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
import unbbayes.util.Debug;

public class TriplestoreKnowledgeBase implements KnowledgeBase {

	private MultiEntityBayesianNetwork defaultMEBN;

	private IMEBNMediator defaultMediator;

	private TriplestoreController triplestoreController; 

	/**
	 * This is the default instance of {@link #getMappingArgumentExtractor()}.
	 */
	public IMappingArgumentExtractor DEFAULT_MAPPING_ARGUMENT_EXTRACTOR = DefaultMappingArgumentExtractor.newInstance();

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

		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
			createEntityDefinition(entity);
		}

		for(MFrag mfrag: mebn.getDomainMFragList()){
			for(ResidentNode resident: mfrag.getResidentNodeList()){
				createRandomVariableDefinition(resident);
			}
		}
	}


	@Override
	public void saveGenerativeMTheory(MultiEntityBayesianNetwork mebn, File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveFindings(MultiEntityBayesianNetwork mebn, File file) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadModule(File file, boolean findingModule)
			throws UBIOException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean supportsLocalFile(boolean isLoad) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String[] getSupportedLocalFileExtension(boolean isLoad) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	public void createGenerativeKnowledgeBase(
//			MultiEntityBayesianNetwork mebn) {
//		
//		for(ObjectEntity entity: mebn.getObjectEntityContainer().getListEntity()){
//			createEntityDefinition(entity);
//		}
//
//		for(MFrag mfrag: mebn.getDomainMFragList()){
//			for(ResidentNode resident: mfrag.getResidentNodeList()){
//				createRandomVariableDefinition(resident);
//			}
//		}
//		return null;
//	}

	@Override
	public void fillFindings(ResidentNode resident) {
		// TODO Auto-generated method stub

	}

	//--------------------------------------------------------------------------
	// Inserting elements and definitions into base 
	//--------------------------------------------------------------------------

	@Override
	public void createEntityDefinition(ObjectEntity entity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createRandomVariableDefinition(ResidentNode resident) {
		// TODO Auto-generated method stub

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

		if ((formulaTree.getTypeNode() == EnumType.OPERAND)
				&& (formulaTree.getSubTypeNode() == EnumSubType.NODE)) {

			ResidentNodePointer pointer = (ResidentNodePointer)formulaTree.getNodeVariable(); 
			return evaluateBooleanRandomVariable(pointer.getResidentNode(), ovInstances); 

		} else {
			
			String query = ""; 

			switch (formulaTree.getTypeNode()) {

			case SIMPLE_OPERATOR:

				BuiltInRV builtIn = (BuiltInRV) formulaTree.getNodeVariable();

				if(builtIn instanceof BuiltInRVAnd){
					query = makeAndStatement(formulaTree, ovInstances);
				}else
					if(builtIn instanceof BuiltInRVOr){
						query = makeOrStatement(formulaTree, ovInstances);	
					}else
						if(builtIn instanceof BuiltInRVEqualTo){
							query = makeEqualStatement(formulaTree, ovInstances, true); 	
						}else
							if(builtIn instanceof BuiltInRVIff){
								Debug.println("Context Node formula using Iff. This implementation don't deal with this formula. ");
							}else
								if(builtIn instanceof BuiltInRVImplies){
									Debug.println("Context Node formula using implies. This implementation don't deal with this formula. ");
								}else
									if(builtIn instanceof BuiltInRVNot){
										query = makeNegationStatement(formulaTree, ovInstances) ; 
									}	    
				break;

			case QUANTIFIER_OPERATOR:

				Debug.println("Context Node formula using Quantifier. This implementation don't deal with this formula. ");
				
			default:
				Debug.println("ERROR! type of operator don't found");

			}
			
			if(query != null && !query.equals("")){
				System.out.println("SQL: " + query);
				boolean result = triplestoreController.executeAskQuery(query); 
				System.out.println("Result = " + result);
				return new Boolean(result); 
			}
		}

		return null;
	}
	
	private String makeNegationStatement(NodeFormulaTree formulaTree,
			List<OVInstance> ovInstances) {

		String query = ""; 
		
		NodeFormulaTree positiveFormula = formulaTree.getChildren().get(0); 
		
		switch (positiveFormula.getTypeNode()) {

		case SIMPLE_OPERATOR:

			BuiltInRV builtIn = (BuiltInRV) positiveFormula.getNodeVariable();

			if(builtIn instanceof BuiltInRVAnd){
//				query = makeAndStatement(positiveFormula, ovInstances);
				//TODO Deal with negation of AND 
			}else
				if(builtIn instanceof BuiltInRVOr){
//					query = makeOrStatement(positiveFormula, ovInstances);	
					//TODO Deal with negation of OR 
				}else
					if(builtIn instanceof BuiltInRVEqualTo){
						query = makeEqualStatement(positiveFormula, ovInstances, false); 	
					}else
						if(builtIn instanceof BuiltInRVIff){
							Debug.println("Context Node formula using Iff. This implementation don't deal with this formula. ");
						}else
							if(builtIn instanceof BuiltInRVImplies){
								Debug.println("Context Node formula using implies. This implementation don't deal with this formula. ");
							}else
								if(builtIn instanceof BuiltInRVNot){
//									query = makeNegationStatement(formulaTree, ovInstances) ; 
									//TODO Hehe... Deal with negation of negation 
								}	    
			break;

		case QUANTIFIER_OPERATOR:

			Debug.println("Context Node formula using Quantifier. This implementation don't deal with this formula. ");
			
		default:
			Debug.println("ERROR! type of operator don't found");

		}
		return query; 
	}

	//FormulaTree.T
	private String makeEqualStatement(NodeFormulaTree formulaTree,
			List<OVInstance> ovInstanceList, boolean positive) {
		
		String query = null; 
		
		NodeFormulaTree rOperand = formulaTree.getChildren().get(0);
		NodeFormulaTree lOperand = formulaTree.getChildren().get(1); 
		
		if((rOperand.getTypeNode() != EnumType.OPERAND) || 
		   (lOperand.getTypeNode() != EnumType.OPERAND)){
			Debug.println("This implementation don't support this formula.");
			return null; 
		}
		
		//Case 1: ov1 == ov2 
		if ((rOperand.getSubTypeNode() == EnumSubType.OVARIABLE) && 
		    (lOperand.getSubTypeNode() == EnumSubType.OVARIABLE)){
	
			if(positive){
				return makeEqualOvOvStatement((OrdinaryVariable)rOperand.getNodeVariable(), 
						(OrdinaryVariable)lOperand.getNodeVariable(), 
						ovInstanceList); 
			}else{
				return makeDifferentOvOvStatement((OrdinaryVariable)rOperand.getNodeVariable(), 
						(OrdinaryVariable)lOperand.getNodeVariable(), 
						ovInstanceList); 
			}
			
		}
		
		//Case 2: RandomVariable(a,b,c) == x (and inverse order) 
		if ((lOperand.getSubTypeNode() == EnumSubType.OVARIABLE) && 
				(lOperand.getSubTypeNode() == EnumSubType.NODE)){
			
			NodeFormulaTree aux = rOperand; 
			rOperand = lOperand;
			lOperand = aux; 
			
			//will be evaluate in next if. 
		}
		
		if ((lOperand.getSubTypeNode() == EnumSubType.NODE) && 
				(lOperand.getSubTypeNode() == EnumSubType.OVARIABLE)){
			
			if(positive){
				makeEqualNodeOvStatement((ResidentNodePointer)rOperand.getNodeVariable(), 
						(OrdinaryVariable)lOperand.getNodeVariable(), 
						ovInstanceList); 
			}else{
				makeDifferentNodeOvStatement((ResidentNodePointer)rOperand.getNodeVariable(), 
						(OrdinaryVariable)lOperand.getNodeVariable(), 
						ovInstanceList); 
			}
			
		}
		
		//Case 3: RandomVariable(a,b,c) == CONST (and inverse order) 
		//CONST here is an entity value. In the UnBBayes implementation, this CONST can't be a single datatype value 
		
		if ((lOperand.getSubTypeNode() == EnumSubType.ENTITY) && 
				(lOperand.getSubTypeNode() == EnumSubType.NODE)){
			
			NodeFormulaTree aux = rOperand; 
			rOperand = lOperand;
			lOperand = aux; 
		}
		
		if ((lOperand.getSubTypeNode() == EnumSubType.NODE) && 
				(lOperand.getSubTypeNode() == EnumSubType.ENTITY)){
			
		}
		
		return query; 
		
	}

	private String makeEqualOvOvStatement (OrdinaryVariable rOperand, OrdinaryVariable lOperand, 
            List<OVInstance> ovInstanceList){
		
		return makeComparisonOvOvStatement(rOperand, lOperand, ovInstanceList, "="); 
	}
	
	private String makeDifferentOvOvStatement (OrdinaryVariable rOperand, OrdinaryVariable lOperand, 
            List<OVInstance> ovInstanceList){
		
		return makeComparisonOvOvStatement(rOperand, lOperand, ovInstanceList, "!="); 
	}
	
	/*
	 * Operator: "=" or "!=" 
	 */
	private String makeComparisonOvOvStatement(OrdinaryVariable rOperand, OrdinaryVariable lOperand, 
			                              List<OVInstance> ovInstanceList, String operator){
		String query = ""; 
		
		query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
				"ASK " + 
				"WHERE { ";

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
		//TODO Exception 
		
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
		//TODO Exception 
		
		query+= "?Y " + "rdf:type " + "<" + objectEntityType + "> " + ". ";
		
		query+= " FILTER (";
		
		query+= "?X = " + "<" + 
		        rOVInstance.getEntity().getInstanceName() + 
		        "> && ";  
		query+= "?Y = " + "<" + 
		        lOVInstance.getEntity().getInstanceName() + 
		        "> && ";			
		query+= "?X " + operator + " ?Y"; 
		
		query+= ")"; 
		
		query+= "}"; 	
		
		return query; 
		
	}
	
	private String makeEqualNodeOvStatement (ResidentNodePointer residentNodePointer, OrdinaryVariable lOperand, 
            List<OVInstance> ovInstanceList){
		
		return makeComparisonNodeOvStatement(residentNodePointer, lOperand, ovInstanceList, "="); 
	}
	
	private String makeDifferentNodeOvStatement (ResidentNodePointer residentNodePointer, OrdinaryVariable lOperand, 
            List<OVInstance> ovInstanceList){
		
		return makeComparisonNodeOvStatement(residentNodePointer, lOperand, ovInstanceList, "!="); 
	}
	
	/*
	 * Operator: "=" or "!=" 
	 */
	private String makeComparisonNodeOvStatement(ResidentNodePointer residentNodePointer, OrdinaryVariable lOperand, 
			                              List<OVInstance> ovInstanceList, String operator){
		String query = ""; 
		
		query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
				"ASK " + 
				"WHERE { ";

		ResidentNode residentNode = residentNodePointer.getResidentNode();
		
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), residentNode);
		if (propertyIRI == null) {
			throw new IllegalStateException("Fail in search finding to node " + residentNode + "." + "\n" + 
		                    this.getDefaultMEBN() + " does not contain references to the OWL property related to resident node " + residentNode);
		}
		
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
			
			query+= "<" + argOVInstance.getEntity().getInstanceName() + "> "; 
			
			//Relation 
			query+= "<" + propertyIRI + "> "; 

		}else{
			
			//TODO Treat residentNode with more than one argument 
			
		}

		OrdinaryVariable ov = lOperand; 
		Type type = ov.getValueType(); 
		ObjectEntity oe = this.defaultMEBN.getObjectEntityContainer().getObjectEntityByType(type); 
		IRI objectEntityType = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, oe);
		
		OVInstance lOVInstance = null; 
		for(OVInstance ovInstance: ovInstanceList){
			if(ovInstance.getOv().equals(ov)){
				lOVInstance = ovInstance; 
				break; 
			}
		}
		//TODO Exception 
		
		query+= "<" + 
		        lOVInstance.getEntity().getInstanceName() + 
		        "> ";			
		
		query+= "}"; 	
		
		return query; 
		
	}
	
	private String makeOrStatement(NodeFormulaTree formulaTree,
			List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null; 
	}

	private String makeAndStatement(NodeFormulaTree formulaTree,
			List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null; 
		
	}

	public Boolean evaluateBooleanRandomVariable(ResidentNode residentNode, List<OVInstance> ovInstances){
		
		if(residentNode.getTypeOfStates() != ResidentNode.BOOLEAN_RV_STATES){
			throw new IllegalArgumentException("This knowledge base cannot "
					+ "handle resident nodes with 0 arguments. Resident node = " + residentNode);
		}else{
			StateLink result = searchFinding(residentNode, ovInstances); 
			if (result.getState().getName().equals("true")){
				return new Boolean(Boolean.TRUE); 
			}else{
				return new Boolean(Boolean.FALSE); 
			}
		}
	}

	/*
	 * Context Node with only one ordinary variable not filled -> Only select on base 
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateSingleSearchContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public List<String> evaluateSingleSearchContextNodeFormula(
			ContextNode context, List<OVInstance> ovInstances)
					throws OVInstanceFaultException {
		// TODO Auto-generated method stub
		
		NodeFormulaTree formulaTree = (NodeFormulaTree) context
				.getFormulaTree();

		if ((formulaTree.getTypeNode() == EnumType.OPERAND)
				&& (formulaTree.getSubTypeNode() == EnumSubType.NODE)) {

			//TODO Simple Node 

		} else {
			
			String query = ""; 

			switch (formulaTree.getTypeNode()) {

			case SIMPLE_OPERATOR:

				BuiltInRV builtIn = (BuiltInRV) formulaTree.getNodeVariable();

				if(builtIn instanceof BuiltInRVAnd){
//					query = makeAndStatement(formulaTree, ovInstances); 
					//TODO
				}else
					if(builtIn instanceof BuiltInRVOr){
//						query = makeOrStatement(formulaTree, ovInstances);	
						//TODO 
					}else
						if(builtIn instanceof BuiltInRVEqualTo){
							query = makeSearchEqualStatement(formulaTree, ovInstances, true); 	
							//TODO 
						}else
							if(builtIn instanceof BuiltInRVIff){
								Debug.println("Context Node formula using Iff. This implementation don't deal with this formula. ");
							}else
								if(builtIn instanceof BuiltInRVImplies){
									Debug.println("Context Node formula using implies. This implementation don't deal with this formula. ");
								}else
									if(builtIn instanceof BuiltInRVNot){
//										query = makeNegationStatement(formulaTree, ovInstances) ; 
										//TODO 
									}	    
				break;

			case QUANTIFIER_OPERATOR:

				Debug.println("Context Node formula using Quantifier. This implementation don't deal with this formula. ");
				
			default:
				Debug.println("ERROR! type of operator don't found");

			}
			
			if(query != null && !query.equals("")){
				
				List<String[]> listResult = triplestoreController.executeSelectQuery(query); 
				
				if(listResult != null){
					if(listResult.size() == 1){
						List<String> resultList = new ArrayList<String>(); 
						for(String result: listResult.get(0)){
							resultList.add(result); 
						}; 
						return resultList; 
					}
				}
				
				return null; 
			}
		}		
		
		return null;
	}

	//FormulaTree.T
	private String makeSearchEqualStatement(NodeFormulaTree formulaTree,
			List<OVInstance> ovInstanceList, boolean positive) {
		
		String query = null; 
		
		NodeFormulaTree rOperand = formulaTree.getChildren().get(0);
		NodeFormulaTree lOperand = formulaTree.getChildren().get(1); 
		
		if((rOperand.getTypeNode() != EnumType.OPERAND) || 
		   (lOperand.getTypeNode() != EnumType.OPERAND)){
			Debug.println("This implementation don't support this formula.");
			return null; 
		}
		
		//Case 1: ov1 == ov2 
		if ((rOperand.getSubTypeNode() == EnumSubType.OVARIABLE) && 
		    (lOperand.getSubTypeNode() == EnumSubType.OVARIABLE)){
	
			//TODO Develop. 
//			if(positive){
//				return makeEqualOvOvStatement((OrdinaryVariable)rOperand.getNodeVariable(), 
//						(OrdinaryVariable)lOperand.getNodeVariable(), 
//						ovInstanceList); 
//			}else{
//				return makeDifferentOvOvStatement((OrdinaryVariable)rOperand.getNodeVariable(), 
//						(OrdinaryVariable)lOperand.getNodeVariable(), 
//						ovInstanceList); 
//			}
			
		}
		
		//Case 2: RandomVariable(a,b,c) == x (and inverse order) 
		if ((lOperand.getSubTypeNode() == EnumSubType.OVARIABLE) && 
				(lOperand.getSubTypeNode() == EnumSubType.NODE)){
			
			NodeFormulaTree aux = rOperand; 
			rOperand = lOperand;
			lOperand = aux; 
			
			//will be evaluate in next if. 
		}
		
		if ((lOperand.getSubTypeNode() == EnumSubType.NODE) && 
				(lOperand.getSubTypeNode() == EnumSubType.OVARIABLE)){
			
			if(positive){
				makeEqualNodeOvStatement((ResidentNodePointer)rOperand.getNodeVariable(), 
						(OrdinaryVariable)lOperand.getNodeVariable(), 
						ovInstanceList); 
			}else{
//				makeDifferentNodeOvStatement((ResidentNodePointer)rOperand.getNodeVariable(), 
//						(OrdinaryVariable)lOperand.getNodeVariable(), 
//						ovInstanceList); 
				//TODO Develop this method
			}
			
		}
		
		//Case 3: RandomVariable(a,b,c) == CONST (and inverse order) 
		//CONST here is an entity value. In the UnBBayes implementation, this CONST can't be a single datatype value 
		
		if ((lOperand.getSubTypeNode() == EnumSubType.ENTITY) && 
				(lOperand.getSubTypeNode() == EnumSubType.NODE)){
			
			NodeFormulaTree aux = rOperand; 
			rOperand = lOperand;
			lOperand = aux; 
		}
		
		if ((lOperand.getSubTypeNode() == EnumSubType.NODE) && 
				(lOperand.getSubTypeNode() == EnumSubType.ENTITY)){
			
		}
		
		return query; 
		
	}
	
	/*
	 * Operator: "=" or "!=" 
	 */
	private String makeSearchComparisonNodeOvStatement(ResidentNodePointer residentNodePointer, OrdinaryVariable oVariable, 
			                              List<OVInstance> ovInstanceList, String operator){
		String query = ""; 
		
		query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
				"SELECT ?X " + 
				"WHERE { ";

		ResidentNode residentNode = residentNodePointer.getResidentNode();
		
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), residentNode);
		if (propertyIRI == null) {
			throw new IllegalStateException("Fail in search finding to node " + residentNode + "." + "\n" + 
		                    this.getDefaultMEBN() + " does not contain references to the OWL property related to resident node " + residentNode);
		}
		
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
			
			if(argOVInstance == null){
				query+= " ?X "; 
			}else{
				query+= "<" + argOVInstance.getEntity().getInstanceName() + "> "; 
			}
			
			query+= "<" + propertyIRI + "> "; 
			
			if(argOVInstance != null){
				query+= " ?X ";
			} else{
				for(OVInstance ovInstance: ovInstanceList){
					if(ovInstance.getOv().equals(oVariable)){
						argOVInstance = ovInstance; 
						break; 
					}
					if(argOVInstance!= null){
						query+= "<" + argOVInstance.getEntity().getInstanceName() + "> "; 
					}
					else{
						//TODO Exception
						System.out.println("Algorithm Fail!");
					}
				}
			}

		}else{
			
			//TODO Treat residentNode with more than one argument 
			
		}
		
		query+= "}"; 

		OrdinaryVariable ov = oVariable; 
		Type type = ov.getValueType(); 
		ObjectEntity oe = this.defaultMEBN.getObjectEntityContainer().getObjectEntityByType(type); 
		IRI objectEntityType = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, oe);
		
		OVInstance lOVInstance = null; 
		for(OVInstance ovInstance: ovInstanceList){
			if(ovInstance.getOv().equals(ov)){
				lOVInstance = ovInstance; 
				break; 
			}
		}
		//TODO Exception 
		
		query+= "<" + 
		        lOVInstance.getEntity().getInstanceName() + 
		        "> ";			
		
		query+= "}"; 	
		
		return query; 
		
	}
	
	/*
	 * Context node with more than one ordinary variable not filled -> Select on base with multiple variables  
	 * @see unbbayes.prs.mebn.kb.KnowledgeBase#evaluateSearchContextNodeFormula(unbbayes.prs.mebn.ContextNode, java.util.List)
	 */
	public SearchResult evaluateSearchContextNodeFormula(ContextNode context,
			List<OVInstance> ovInstances) {
		// TODO Auto-generated method stub
		return null;
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
	public StateLink searchFinding(ResidentNode resident,
			Collection<OVInstance> listArguments) {

		//TODO We have to validate the arguments. 

		//We have to analyze the quantity of ordinary variables and verify the
		//property "definesUncertaintyOf"

		// initial assertions
		if (resident == null) {
			// it is impossible to search a finding if no variable is provided
			Debug.println(this.getClass(), "Attempted to search finding for null resident node.");
			return null;
		}
		if (resident.getArgumentList() == null || listArguments == null) {
			throw new IllegalArgumentException("This knowledge base cannot handle resident nodes with null arguments. Resident node = " + resident);
		}

		//TODO Cache

		if (resident.getArgumentList().size() != listArguments.size()) {
			throw new IllegalArgumentException("Findings of " + resident + " should have " + resident.getArgumentList().size() + " arguments.");
		}

		if (listArguments.size() == 0) {
			// This version cannot represent resident nodes with no arguments
			throw new IllegalArgumentException("This knowledge base cannot handle resident nodes with 0 arguments. Resident node = " + resident);
		}

		if (triplestoreController.isConnected() == false) {
			throw new IllegalStateException("Database disconnected.");
		}

		// extract IRI of the property pointed by the resident node
		IRI propertyIRI = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getDefaultMEBN(), resident);
		if (propertyIRI == null) {
			throw new IllegalStateException("Fail in search finding to node " + resident + "." + "\n" + 
		                    this.getDefaultMEBN() + " does not contain references to the OWL property related to resident node " + resident);
		}

		if (listArguments.size() == 1) {
			// this is either a functional format (e.g. F(x) = y) or a boolean data property

			//1 - Boolean Data Property 
			//TODO Here we have a problem! The user will use an IRI or the name of a entity? 
			//				ObjectEntityInstance entityInstance = this.getDefaultMEBN().getObjectEntityContainer().getEntityInstanceByName(listArguments.iterator().next().getEntity().getInstanceName());
			//				IRI subjectIRI = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.getDefaultMEBN(), entityInstance);
			//				if (subjectIRI == null) {
			//					throw new IllegalStateException("Could not extract the subject of property " + propertyIRI + " from MEBN " + this.getDefaultMEBN());
			//				}

			// SELECT ?X WHERE ENTITY PROPERTY ?X
			String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
					"SELECT ?X " + 
					"WHERE { " + "<" + listArguments.iterator().next().getEntity().getInstanceName()
					+ ">" + " " + "<" + propertyIRI + ">" + " " + "?X" + " . }"; 

			System.out.println(query);

			List<String[]> resultList = triplestoreController.executeSelectQuery(query); 

			//TODO Remove this after
			if(resultList.size() == 0){
				return null; 
			}

			if(resultList.size() > 1){
				throw new IllegalStateException("Query returned more than one result!");
			}

			String[] resultLine = resultList.get(0); 
			if(resultLine.length > 1){
				throw new IllegalStateException("Query returned more than one result!");
			}

			System.out.println(resultLine[0]);

			//TODO Other solution for store the possible values 

			if ((resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES) || 
					(resident.getTypeOfStates() == ResidentNode.CATEGORY_RV_STATES)){
				StateLink link = resident.getPossibleValueByName(resultLine[0]); 

				if(link != null){
					System.out.println("Returned: " + link);
					return link; 
				}
			} else{
				if (resident.getTypeOfStates() == ResidentNode.OBJECT_ENTITY){
					Entity e = resident.getPossibleValueList().get(0); 
					if (e instanceof ObjectEntity){
						ObjectEntityInstance oei = new ObjectEntityInstance(resultLine[0], (ObjectEntity)e); 
						StateLink link = new StateLink(oei); 
						return link; 
					}
				}
			}
		} else {
			//Deal with more than one argument 
			if ((listArguments.size() == 2) && 
					(resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES)){
				// 2 arguments. This is a simple binary relationship

				// SELECT ?X WHERE ENTITY PROPERTY ?X
				//TODO IRI dos argumentos. 
				String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
						"ASK WHERE { " + 
						"<" + listArguments.iterator().next().getEntity().getInstanceName() + ">" + " " +
						"<" + propertyIRI + ">" + " " + 
						"<" + listArguments.iterator().next().getEntity().getInstanceName() + ">" + "}"; 

				System.out.println(query);

				boolean result = triplestoreController.executeAskQuery(query); 

				StateLink link = resident.getPossibleValueByName("" + result); 

				if(link != null){
					System.out.println("Returned: " + link);
					return link; 
				}

				//TODO Deal with isSubjectIn and isObjectIn 
				//Duvida: pode-se utilizar o isSubjectIn e isObjectIn nestes casos, 
				//onde já está bem claro quais são os elementos da propriedade? Na 
				//implementação do Shou ele não utiliza estas prop quando elas não são necessárias. 
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

				// from the mapping, create an expression that returns the subject if the subject has a link to object.
				// example 1: inverse inv_MTI value Slow and inverse inv_MTI_RPT value Rpt2 and inverse inv_MTI_T value T1
				// example 2: MTI value Fast and MTI_RPT value Rpt1 and MTI_T value T1
				String expressionToParse = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
						"SELECT ?X WHERE { _:a1 " + "<" + mainProperty.getIRI() + "> " + " ?X . "; 
				// Note: we already checked at the beginning of this method that listArgument.size() == resident.getArgumentList().size()
				for (Iterator<OVInstance> iterator = listArguments.iterator(); iterator.hasNext(); ) {
					// I'm using an explicit iterator, because expressionToParse shall include an "and" at the end of expression
					OVInstance argInstance = iterator.next();

					// if there is no valid mapping, use default (use the one in definesUncertaintyOf, and isSubjectIn)
					OWLProperty property = mainProperty;
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
					}

					//TODO Use IRI instead of getInstanceName
					//TODO Neste momento o nome da entidade estará com a IRI? 
					//Lembrando que as entidades do modelo ou foram informadas pelo usuário ou 
					//foram recuperadas da base de conhecimento... e portanto, temos que ter os 
					//nomes completos. 
					if(isSubjectIn){
						expressionToParse+= "<" + argInstance.getEntity().getInstanceName() + "> " + 
								"<" + property.getIRI() + ">" + " " + 
								"_:a1" + " " + 
								"." + " "; 
					}else{
						expressionToParse+= " _:a1" + " " + 
								"<" + property.getIRI() + ">" + " " +   
								"<" + argInstance.getEntity().getInstanceName() + "> " + 
								"." + " "; 
					}
				}

				expressionToParse+= "}"; 

				System.out.println("Expression:" + expressionToParse);
				Debug.println(this.getClass(), "Expression: " + expressionToParse);

				// because we are in open-world assumption, we must check if individuals "are" related, "never" related or "unknown"

				//				boolean result = triplestoreController.executeAskQuery(expressionToParse); 

				List<String[]> resultList = triplestoreController.executeSelectQuery(expressionToParse); 

				//TODO Remove this after
				if(resultList.size() == 0){
					return null; 
				}

				if(resultList.size() > 1){
					throw new IllegalStateException("Query returned more than one result!");
				}

				String[] resultLine = resultList.get(0); 
				if(resultLine.length > 1){
					throw new IllegalStateException("Query returned more than one result!");
				}

				System.out.println(resultLine[0]);

				if ((resident.getTypeOfStates() == ResidentNode.BOOLEAN_RV_STATES) || 
						(resident.getTypeOfStates() == ResidentNode.CATEGORY_RV_STATES)){
					StateLink link = resident.getPossibleValueByName(resultLine[0]); 

					if(link != null){
						System.out.println("Returned: " + link);
						return link; 
					}
				} else{
					if (resident.getTypeOfStates() == ResidentNode.OBJECT_ENTITY){
						Entity e = resident.getPossibleValueList().get(0); 
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

	public List<String> getEntityByType(String type) {

		List<String[]> listResults;  
		List<String> resultList = new ArrayList<String>(); 

		Type typeMEBN = this.defaultMEBN.getTypeContainer().getType(type); 
		if(typeMEBN == null){
			System.out.println("Type not found: " + type);
			return null; 
		}

		ObjectEntity objectEntity = this.defaultMEBN.getObjectEntityContainer().getObjectEntityByType(typeMEBN); 
		if(objectEntity == null){
			System.out.println("Object Entity not found" + type);
			return null; 
		}

		IRI entityIRIWhenLoaded = IRIAwareMultiEntityBayesianNetwork.getIRIFromMEBN(this.defaultMEBN, objectEntity);

		if(entityIRIWhenLoaded == null){
			System.out.println("IRI for Object Entity not found" + type);
			return null; 
		}else{
			System.out.println("IRI = " + entityIRIWhenLoaded );			
		}

		String query = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " + 
				"SELECT ?X " + 
				"WHERE { ?X rdf:type " + "<" + entityIRIWhenLoaded + ">" + " . }"; 

		System.out.println("Query = " + query);

		listResults = triplestoreController.executeSelectQuery(query);

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

}
