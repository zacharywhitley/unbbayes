package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.util.Debug;

/*
 * only tests... 
 */
public class ContextNodeAvaliatorTest{

    public static void main(String[] args) throws Exception {
    	MultiEntityBayesianNetwork mebn = null; 
    	
		KnowledgeBase kb = PowerLoomKB.getNewInstanceKB(); 
		
		kb.loadModule(new File("testeGenerativeStarship.plm")); 
		kb.loadModule(new File("testeFindingsStarship.plm")); 
		
		PrOwlIO io = new PrOwlIO(); 
		try {
			mebn = io.loadMebn(new File("examples/mebn/StarTrek37.owl"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOMebnException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		
		LiteralEntityInstance literalEntityInstance; 
		OVInstance ovInstance; 
		OrdinaryVariable ov; 
		
		Type type = null;
		
		List<OVInstance> ovInstanceList = new ArrayList<OVInstance>(); 
		List<OrdinaryVariable> ordVariableList = new ArrayList<OrdinaryVariable>(); 
		
		type = mebn.getTypeContainer().getType("Starship_label"); 
		literalEntityInstance = LiteralEntityInstance.getInstance("ST0", type); 
		ov = new OrdinaryVariable("st", type, mFrag); 
		ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
		ovInstanceList.add(ovInstance); 
		
		type = mebn.getTypeContainer().getType("TimeStep_label"); 
		literalEntityInstance = LiteralEntityInstance.getInstance("T0", type); 
		ov = new OrdinaryVariable("t", type, mFrag); 
		ovInstance = OVInstance.getInstance(ov, literalEntityInstance); 
		ovInstanceList.add(ovInstance); 
		
		type = mebn.getTypeContainer().getType("Zone_label"); 
		ov = new OrdinaryVariable("z", type, mFrag); 
		ordVariableList.add(ov); 
		
		System.out.println("MFrag: " + mFrag.getName());
		for(OVInstance ovInstanc: ovInstanceList){
			System.out.println("OVInstance: " + ovInstanc);
		}
		for(OrdinaryVariable ovInstanc: ordVariableList){
			System.out.println("OV: " + ovInstanc);
		}
		
		List<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		for(OVInstance ovInstanc: ovInstanceList){
			ovList.add(ovInstanc.getOv()); 
		}
		ovList.addAll(ordVariableList); 
		
		ContextNodeAvaliator avaliator = new ContextNodeAvaliator(kb); 
		
		evaluateContextNodes(mFrag, ovInstanceList, ordVariableList, avaliator, kb); 
    
    }	
    

	/**
	 * True - All nodes ok
	 * False - Use default distribution
	 * 
	 * List<entities> resultado. -> normal, busca geral. 
	 * 
	 * @param mFrag
	 * @param ovInstanceList
	 * @param ordVariableList
	 */
	public static void evaluateContextNodes(MFrag mFrag, List<OVInstance> ovInstanceList, List<OrdinaryVariable> ordVariableList, 
			ContextNodeAvaliator avaliator, KnowledgeBase kb){
		
		Debug.setDebug(true); 
		
		Collection<ContextNode> contextNodeList; 
		
		List<OrdinaryVariable> ovList = new ArrayList<OrdinaryVariable>(); 
		for(OVInstance ovInstance: ovInstanceList){
			ovList.add(ovInstance.getOv()); 
		}
		ovList.addAll(ordVariableList); 
		
		contextNodeList = mFrag.getContextByOVCombination(ovList); 
		
		Debug.println(""); 
		Debug.println("Evaluating... "); 
		Debug.println(""); 
		
		for(ContextNode context: contextNodeList){
			Debug.println("Context Node: " + context.getFormula()); 
			try{
				if(!avaliator.evaluateContextNode(context, ovInstanceList)){
					Debug.println("Result = FALSE. Use default distribution "); 
//					return false;  //use the default distribution. 
				}
			}
			catch(OVInstanceFaultException e){
				try {
					Debug.println("OVInstance Fault. Try evaluate a search. "); 
					List<String> result = null;
					
					try {
						result = avaliator.evalutateSearchContextNode(context, ovInstanceList);
					} catch (OVInstanceFaultException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return; 
					}
					
					if(result.isEmpty()){
						
						OrdinaryVariable rigthTerm = context.getFreeVariable(); 
						result = kb.getEntityByType(rigthTerm.getValueType().getName());
						
						Debug.println("No information in Knowlege Base"); 
						Debug.print("Result = "); 
						for(String entity: result){
							Debug.print(entity + " "); 
						}
						Debug.println(""); 
						
//						return false; 
					}else{
						Debug.print("Result = "); 
						for(String entity: result){
							Debug.print(entity + " "); 
						}
						Debug.println(""); 
//						return true; 
					}
				} catch (InvalidContextNodeFormulaException ie) {
					Debug.println("Invalid Context Node: the formula don't is accept."); 
					// TODO Auto-generated catch block
					ie.printStackTrace();
				} 
			}
			Debug.println(""); 
		}
		
//		return true; 
		
	}
	
}
