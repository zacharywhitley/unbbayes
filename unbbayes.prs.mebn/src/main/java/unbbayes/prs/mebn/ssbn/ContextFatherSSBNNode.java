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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.InvalidOperationException;

/**
 * This class represents a ContextNode that is father in the probabilistic 
 * network (it is a search context node and don't have values definited into
 * knowledge base, then turn to father of the probabilistic node that
 * make its evaluation)
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 * 
 * @author Shou Matsumoto
 * @version 160131 - made changes so that the name of the probabilistic node can be something more suggestive
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
	
//	private boolean isModified = true;
//	
	private static boolean isToGenerateSuggestiveProbabilisticNodeName = true;
	
	private Collection<OVInstance> knownOVs = Collections.EMPTY_LIST;	

	/**
	 * This node represents a multiplexor node generated from an unsolved (uncertain) context node
	 * in a format like y=Function(x).
	 * @param pnet : the BN where probNode will be included into
	 * @param contextNode : the context node representing this node
	 * @param probNode : the equivalent probNode to be included to BN
	 * @param unknownOV : which OV in contextNode is unknown. For instance, if context node is y=Function(x),
	 * then either x or y (usually the y) must be this.
	 * @param knownOVs : what OVs of this MFrag are instantiated already.
	 * We recommend you to reuse instances of {@link Collection} (instead of duplicating instances), in order to save memory space.
	 * If context node is y=Function(x), then this must be filled with at least the instantiation of the OV
	 * other than the unknownOV.
	 * @see #setKnownOVs(Collection)
	 * @see #setOvProblematic(OrdinaryVariable)
	 */
	public ContextFatherSSBNNode (ProbabilisticNetwork pnet, ContextNode contextNode, 
			ProbabilisticNode probNode, OrdinaryVariable unknownOV, Collection<OVInstance> knownOVs) {
		
		this.pnet = pnet;
		this.contextNode = contextNode;
		this.probNode = probNode;
		this.setKnownOVs(knownOVs); 
		
		String name = contextNode.getName();
		
		probNode.setName(name);
		probNode.setDescription(contextNode.getName());
		
		// avoid duplicate name, because name is the primary identifier of a node in UnBBayes
		if (pnet.getNode(name) != null) {	
			// generate a new name, by adding a new number after the name
			int i = 1;
			while (pnet.getNode(name + "_" + i) != null) {i++;}	// find a number which was not used yet
			probNode.setName(name + "_" + i); // finally, use the new name
		}
		
		
		possibleValues = new ArrayList<ILiteralEntityInstance>();
//		if (possibleStates != null) {
//			for (ILiteralEntityInstance knownValue : possibleStates) {
//				this.addPossibleValue(knownValue);
//			}
//			// make sure we are using the same object instance
////			possibleValues = knownValues;
//		}
		
		this.setOvProblematic(unknownOV);
		
		
		if (isToGenerateSuggestiveProbabilisticNodeName()) {
			probNode.setName(this.getSuggestiveName());
		} 
		
		pnet.addNode(probNode);
	}
	
	
	/**
	 * @param pnet
	 * @param contextNode
	 * @param probNode
	 * @see #setKnownOVs(Collection)
	 * @see #setOvProblematic(OrdinaryVariable)
	 * @deprecated use {@link #ContextFatherSSBNNode(ProbabilisticNetwork, ContextNode, ProbabilisticNode, OrdinaryVariable, Collection)} instead
	 */
	@Deprecated
	public ContextFatherSSBNNode (ProbabilisticNetwork pnet, ContextNode contextNode, ProbabilisticNode probNode) {
		this(pnet, contextNode, probNode, null, null);
	}
	

	
	/**
	 * @param pnet
	 * @param contextNode
	 * @see #setKnownOVs(Collection)
	 * @see #setOvProblematic(OrdinaryVariable)
	 * @deprecated use {@link #ContextFatherSSBNNode(ProbabilisticNetwork, ContextNode, ProbabilisticNode, OrdinaryVariable, Collection)} instead.
	 */
	@Deprecated
	public ContextFatherSSBNNode (ProbabilisticNetwork pnet, ContextNode contextNode) {
		this(pnet, contextNode, new ProbabilisticNode(), null, null);
	}
	
	

	/**
	 * This method will convert a given name to some valid name,
	 * by removing invalid characters.
	 * <br/> <br/>
	 * Basically, the name will be trimmed (i.e. {@link String#trim()}), 
	 * punctuation characters and whitespaces will be substituted with underscores (i.e. '_'),
	 * 2 or more consecutive underscores will be substituted to 1 underscore,
	 * and underscores at the beginning and end of the name will be removed.
	 * <br/> <br/>
	 * Classes extending this method may personalize this behavior.
	 * <br/> <br/>
	 * The following substitution will be also applied to FOL operation symbols:
	 * <pre>
	 * "=" -> "_equals_"
	 * "¬" -> "_not_"
	 * "∀" -> "_all_"
	 * "∃" -> "_exists_"
	 * "∧" -> "_and_"
	 * "∨" -> "_or_"
	 * "→" -> "_implies_"
	 * "↔" -> "_iff_"
	 * </pre>
	 * Please, notice that {@link #getSuggestiveName()} may require this method to return a name in some
	 * format, like OVs separated by underscores.
	 * @param nameToClean : the name to consider.
	 * @return the converted name.
	 * @see #getSuggestiveName()
	 */
	public String getCleanName(String nameToClean) {
		
		// return "null" if its null or empty
		if (nameToClean == null) {
			return "null";
		}
		nameToClean = nameToClean.trim();
		if (nameToClean.isEmpty()) {
			return "null";
		}
		
		// replace some characters with special meaning
		nameToClean = nameToClean.replace("=", "_equals_");
		nameToClean = nameToClean.replace("¬", "_not_");
		nameToClean = nameToClean.replace("∀", "_all_");
		nameToClean = nameToClean.replace("∃", "_exists_");
		nameToClean = nameToClean.replace("∧", "_and_");
		nameToClean = nameToClean.replace("∨", "_or_");
		nameToClean = nameToClean.replace("→", "_implies_");
		nameToClean = nameToClean.replace("↔", "_iff_");
		
		
		// replace white space and punctuation with underscores. 
		nameToClean = nameToClean.replaceAll("[\\p{Punct}\\p{Space}]", "_"); // "\p{Punct}" and \p{Space} are POSIX regex classes.
		
		// remove redundant underscores
		nameToClean = nameToClean.replaceAll("_+", "_");
		

		// remove underscores in 1st or last character
		int beginIndex = 0;
		if (nameToClean.charAt(0) == '_') {
			beginIndex++;
		}
		int endIndex = nameToClean.length();
		if (nameToClean.charAt(endIndex-1) == '_') {
			endIndex--;
		}
		nameToClean = nameToClean.substring(beginIndex, endIndex);
		
		return nameToClean;
	}
	

	/**
	 * @return this will use {@link #getContextNode()}, {@link #getOvProblematic()}, and {@link #getKnownOVs()}
	 * in order to generate a suggestive name (a name with some meaning, not something like CX1, or CX2).
	 * <br/> <br/>
	 * If {@link #getOvProblematic()} or {@link #getKnownOVs()} are null (or empty), then
	 * this will simply return {@link #getCleanName(String)} of {@link ContextNode#toString()}.
	 * <br/> <br/>
	 * This method expects that {@link #getCleanName(String)} will have OVs separated by underscores.
	 * @see ContextNode#getFormulaTree()
	 * @see ContextNode#toString()
	 * @see #getCleanName(String)
	 */
	public String getSuggestiveName() {
		// basic check
		if (contextNode == null || contextNode.getFormulaTree() == null) {
			throw new IllegalStateException("contextNode == null.");
		} 
		
		// first candidate is the context node's formulae.
		// This is expected to have underscores separating OVs with the rest.
		String name = this.getCleanName(contextNode.toString());
		
		// substitute known ordinary variables with its values
		if (ovProblematic != null && knownOVs != null && !knownOVs.isEmpty()) {
			// use a mapping, so that we don't need to do a quadratic search on known ovs
			Map<String, ILiteralEntityInstance> ovNameToLiteralMap = new HashMap<String, ILiteralEntityInstance>();
			for (OVInstance ovInstance : knownOVs) {
				ovNameToLiteralMap.put(ovInstance.getOv().getName(), ovInstance.getEntity());
			}
			
			// only consider OVs in context node formulae
			// I'm using "_"+name+"_" so that it is guaranteed that ovs are separated by underscores in both sides.
			name = ("_"+name+"_");
			for (OrdinaryVariable ov : contextNode.getVariableList()) {
				if (ov == null || ov.getName() == null) {
					continue;	// ignore invalid entries
				}
				if (!ov.getName().equals(ovProblematic.getName())
						&& ovNameToLiteralMap.containsKey(ov.getName())) {
					// substitute occurrences of ov with the value. Because I added underscores in both sides, OVs are guaranteed to have underscores in both sides.
					name = name.replace("_"+ov.getName()+"_", "_"+ovNameToLiteralMap.get(ov.getName()).getInstanceName()+"_");
				}
			}
			// remove the underscores inserted before considering the OVs
			name = name.substring(1, name.length()-1);
		}
		
		// avoid duplicate name, because name is the primary identifier of a node in UnBBayes
		if (pnet.getNode(name) != null) {	
			// generate a new name, by adding a new number after the name
			int i = 1;
			while (pnet.getNode(name + "_" + i) != null) {i++;}	// find a number which was not used yet
			name = name + "_" + i;
		}
		
		return name;
	}
	
	/**
	 * Generate the cpt of this node (using the possible values). 
	 * The CPT of a context father node is one linear distribution of probabilities
	 * between the possible states. Each state have the same probability.  
	 * <pre>
	 * Ex.: 
	 * EntityLinkedTo(!R) = E1, E2, E3
	 * #States = 3 -> P(State(i)) = 1.0 / 3 = 0.33  
	 *             Prob(E1) = 0.33
	 *             Prob(E2) = 0.33
	 *             Prob(E3) = 0.33
	 * </pre>
	 * 
	 * TODO the distribution should be accordingly to the distribution of random variables included in this context node's formulae.
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
//		setModified(true);
	}

	public void addPossibleValue(ILiteralEntityInstance e){
//		Debug.println(this.contextNode.getFormula() + " --> Acrescentado estado ao ssbn context node = "  + e.toString());
		if(possibleValues != null){
			possibleValues.add(e);
			probNode.appendState(e.getInstanceName());
		}
//		setModified(true);
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

//	/**
//	 * @return the isModified : if true, then this means that this node has been modified, thus {@link #getProbNode()}
//	 * will rename the node accordingly to the configuration in {@link #isToGenerateSuggestiveProbabilisticNodeName()}
//	 */
//	public boolean isModified() {
//		return isModified;
//	}
//
//	/**
//	 * @param isModified the isModified to set : if true, then this means that this node has been modified, thus {@link #getProbNode()}
//	 * will rename the node accordingly to the configuration in {@link #isToGenerateSuggestiveProbabilisticNodeName()}
//	 */
//	public void setModified(boolean isModified) {
//		this.isModified = isModified;
//	}
//
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


	/**
	 * @return the knownOVs
	 */
	public Collection<OVInstance> getKnownOVs() {
		return knownOVs;
	}


	/**
	 * @param knownOVs the knownOVs to set
	 */
	public void setKnownOVs(Collection<OVInstance> knownOVs) {
		this.knownOVs = knownOVs;
	}

}
