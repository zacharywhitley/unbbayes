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

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.InvalidOperationException;
import unbbayes.util.Debug;

/**
 * This class represents a ContextNode that is father in the probabilistic 
 * network (it is a search context node and don't have values definited into
 * knowledge base, then turn to father of the probabilistic node that
 * make its evaluation)
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class ContextFatherSSBNNode {

	private ContextNode contextNode;	    // what resident node this instance represents
	private ProbabilisticNode  probNode;	// stores the UnBBayes BN ordinal node which represents this SSBNNode
	private ProbabilisticNetwork pnet;
	
	private Collection<ILiteralEntityInstance> possibleValues; // this is the possible values of this node at that moment (might be one, if there is an evidence)
	
	private OrdinaryVariable ovProblematic; 
	
	private boolean cptGenerated = false; 
	
	private ResourceBundle resource = 
		unbbayes.util.ResourceController.newInstance().getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName());
	
	
	private static boolean isToGenerateSuggestiveProbabilisticNodeName = true;	

	/**
	 * 
	 * @param pnet
	 * @param contextNode
	 * @param probNode
	 */
	public ContextFatherSSBNNode (ProbabilisticNetwork pnet, ContextNode contextNode, 
			ProbabilisticNode probNode) {
		
		this.pnet = pnet;
		this.contextNode = contextNode;
		this.probNode = probNode; 
		
		String name = contextNode.getName();
		if (isToGenerateSuggestiveProbabilisticNodeName()) {
			// generate a suggestive name for the probabilistic node representing this context node (instead of using CX1, CX2...)
			name = contextNode.getCleanName(contextNode.toString());
		} 
		probNode.setName(name);
		probNode.setDescription(contextNode.getName());
		
		// avoid duplicate name, because name is the primary identifier of a node in UnBBayes
		if (pnet.getNode(name) != null) {	
			// generate a new name, by adding a new number after the name
			int i = 1;
			while (pnet.getNode(name + "_" + i) != null) {i++;}	// find a number which was not used yet
			probNode.setName(name + "_" + i); // finally, use the new name
		}
		pnet.addNode(probNode);
		
		possibleValues = new ArrayList<ILiteralEntityInstance>();
	}
	
	/**
	 * 
	 * @param pnet
	 * @param contextNode
	 */
	public ContextFatherSSBNNode (ProbabilisticNetwork pnet, ContextNode contextNode) {
		this(pnet, contextNode, new ProbabilisticNode());
	}
	
	/**
	 * Generate the cpt of this node (using the possible values). 
	 * The CPT of a context father node is one linear distribution of probabilities
	 * between the possible states. Each state have the same probability.  
	 * 
	 * Ex.: 
	 * EntityLinkedTo(!R) = E1, E2, E3
	 * #States = 3 -> P(State(i)) = 1.0 / 3 = 0.33  
	 *             Prob(E1) = 0.33
	 *             Prob(E2) = 0.33
	 *             Prob(E3) = 0.33
	 *            
	 */
	public void generateCPT() throws InvalidOperationException{
		
		if(!isCptGenerated()){
			
			PotentialTable cpt = probNode.getProbabilityFunction();
			cpt.addVariable(probNode);
			
			float probabilityOfEachState = 1.0f / possibleValues.size(); 
			
			for(int i = 0; i < possibleValues.size(); i++){
				cpt.setValue(i, probabilityOfEachState); 	
			}

			cptGenerated = true; 
			
		}else{
			throw new InvalidOperationException();
		}
		
	}
	
	public OrdinaryVariable getOvProblematic() {
		return ovProblematic;
	}

	public void setOvProblematic(OrdinaryVariable ovProblematic) {
		this.ovProblematic = ovProblematic;
	}

	public void addPossibleValue(ILiteralEntityInstance e){
		Debug.println(this.contextNode.getFormula() + " --> Acrescentado estado ao ssbn context node = "  + e.toString());
		if(possibleValues != null){
			possibleValues.add(e);
			probNode.appendState(e.getInstanceName());
		}
	}
	
	public Collection<ILiteralEntityInstance> getPossibleValues() {
		return possibleValues;
	}
	
	public ContextNode getContextNode() {
		return contextNode;
	}

	public ProbabilisticNetwork getPnet() {
		return pnet;
	}

	public ProbabilisticNode getProbNode() {
		return probNode;
	}

	public boolean isCptGenerated() {
		return cptGenerated;
	}

	public String toString(){
		return "SSBNNode:" + contextNode.getLabel() + " " + ovProblematic + "[" + possibleValues + "]";
	}
	
	@Override
	public boolean equals(Object obj) {

		if(! (obj instanceof ContextFatherSSBNNode)){
			return false;
		}
		
		ContextFatherSSBNNode ssbnNode = (ContextFatherSSBNNode)obj;
		
		if(ssbnNode.getContextNode().equals(this.getContextNode())){
			
			return true; 
			
		}else{
			return false;  
		}
		
	}

	/**
	 * If true, then suggestive names will be used for {@link ProbabilisticNode} representing this context node.
	 * In other words, names like "x_eq_Function_y_" will be used instead of "CX1", "CX2"...
	 * @return the isToGenerateSuggestiveProbabilisticNodeName :
	 * if true, then {@link #ContextFatherSSBNNode(ProbabilisticNetwork, ContextNode, ProbabilisticNode)}
	 * will use {@link ContextNode#getCleanName(String)} in order to generate names of {@link ProbabilisticNode}.
	 * If false, then {@link ContextNode#getName()} will be used instead.
	 */
	public static boolean isToGenerateSuggestiveProbabilisticNodeName() {
		return isToGenerateSuggestiveProbabilisticNodeName;
	}

	/**
	 * If true, then suggestive names will be used for {@link ProbabilisticNode} representing this context node.
	 * In other words, names like "x_eq_Function_y_" will be used instead of "CX1", "CX2"...
	 * @param isSuggestiveName the isToGenerateSuggestiveProbabilisticNodeName to set :
	 * if true, then {@link #ContextFatherSSBNNode(ProbabilisticNetwork, ContextNode, ProbabilisticNode)}
	 * will use {@link ContextNode#getCleanName(String)} in order to generate names of {@link ProbabilisticNode}.
	 * If false, then {@link ContextNode#getName()} will be used instead.
	 */
	public static void setToGenerateSuggestiveProbabilisticNodeName(
			boolean isSuggestiveName) {
		isToGenerateSuggestiveProbabilisticNodeName = isSuggestiveName;
	}

}
