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
package unbbayes.prs.mebn.ssbn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import unbbayes.io.mebn.PrOwlIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.DomainMFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.kb.KnowledgeBase;
import unbbayes.prs.mebn.kb.powerloom.PowerLoomKB;
import unbbayes.prs.mebn.ssbn.ContextNodeAvaliator;
import unbbayes.prs.mebn.ssbn.LiteralEntityInstance;
import unbbayes.prs.mebn.ssbn.OVInstance;
import unbbayes.prs.mebn.ssbn.exception.InvalidContextNodeFormulaException;
import unbbayes.prs.mebn.ssbn.exception.OVInstanceFaultException;
import unbbayes.util.Debug;

/*
 * only tests... 
 */
public class ContextNodeAvaliatorTest{

    public static void main(String[] args) throws Exception {
    	MultiEntityBayesianNetwork mebn = null; 
    	
		KnowledgeBase kb = PowerLoomKB.getInstanceKB(); 
		
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
		
		DomainMFrag mFrag = mebn.getMFragByName("Starship_MFrag"); 
		
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
	public static void evaluateContextNodes(DomainMFrag mFrag, List<OVInstance> ovInstanceList, List<OrdinaryVariable> ordVariableList, 
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
