package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.InvalidOperationException;

public class ContextFatherSSBNNode {

	private ContextNode contextNode;	// what resident node this instance represents
	private ProbabilisticNode  probNode;	// stores the UnBBayes BN ordinal node which represents this SSBNNode
	private ProbabilisticNetwork pnet;
	
	private Collection<LiteralEntityInstance> possibleValues; // this is the possible values of this node at that moment (might be one, if there is an evidence)
	
	private OrdinaryVariable ovProblematic; 
	
	private boolean cptGenerated = false; 
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");	
	

	public ContextFatherSSBNNode (ProbabilisticNetwork pnet, ContextNode contextNode , ProbabilisticNode probNode) {
		this.pnet = pnet;
		this.contextNode = contextNode;
		this.probNode = probNode; 
		probNode.setName(contextNode.getLabel());
		pnet.addNode(probNode);
		
		possibleValues = new ArrayList<LiteralEntityInstance>();
	}
	
	public ContextFatherSSBNNode (ProbabilisticNetwork pnet, ContextNode contextNode) {
		this(pnet, contextNode, new ProbabilisticNode());
	}
	
	public OrdinaryVariable getOvProblematic() {
		return ovProblematic;
	}


	public void setOvProblematic(OrdinaryVariable ovProblematic) {
		this.ovProblematic = ovProblematic;
	}

	public void addPossibleValue(LiteralEntityInstance e){
		if(possibleValues != null){
			possibleValues.add(e);
			probNode.appendState(e.getInstanceName());
		}
	}
	
	public Collection<LiteralEntityInstance> getPossibleValues() {
		return possibleValues;
	}


	public void setPossibleValues(Collection<LiteralEntityInstance> possibleValues) {
		this.possibleValues = possibleValues;
	}
	
	/*
	 * Generate the cpt of this node (using the possible values)
	 */
	public void generateCPT() throws InvalidOperationException{
		
		if(!isCptGenerated()){
			/*
			 * A CPT é uma distribuição linear das probabilidades entre os estados
			 * possiveis, tendo cada um deles a mesma probabilidade de ocorrer. 
			 */
			
			for(LiteralEntityInstance literalEntityInstance: possibleValues){
//				probNode.appendState(literalEntityInstance.getInstanceName());
			}
			
			probNode.getPotentialTable().addVariable(probNode);
			
		}else{
			throw new InvalidOperationException();
		}
	}
	
	public String toString(){
		return "SSBNNode:" + contextNode.getLabel() + " " + ovProblematic + "[" + possibleValues + "]";
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

	public void setCptGenerated(boolean cptGenerated) {
		this.cptGenerated = cptGenerated;
	}
}
