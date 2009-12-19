package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.OrdinaryVariable;

public class SimpleContextNodeFatherSSBNNode {

	/**
	 * This class represents a ContextNode that is father in the probabilistic 
	 * network (it is a search context node and don't have values definited into
	 * knowledge base, then turn to father of the probabilistic node that
	 * make its evaluation)
	 * 
	 * @author Laecio Lima dos Santos (laecio@gmail.com)
	 */

	private ContextNode contextNode;	    //what resident node this instance represents
	
	private Collection<String> possibleValues; //this is the possible values of this node at that moment (might be one, if there is an evidence)

	private OrdinaryVariable ovProblematic; 

	private ResourceBundle resource = 
		ResourceBundle.getBundle(unbbayes.prs.mebn.ssbn.resources.Resources.class.getName());	

	/**
	 * 
	 * @param pnet
	 * @param contextNode
	 * @param probNode
	 */
	public SimpleContextNodeFatherSSBNNode(ContextNode contextNode, OrdinaryVariable ovFault) {
		this.contextNode = contextNode;
		this.ovProblematic = ovFault; 
		possibleValues = new ArrayList<String>();
	}

	public OrdinaryVariable getOvProblematic() {
		return ovProblematic;
	}

	public Collection<String> getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(Collection<String> _possibleValues) {
		this.possibleValues = _possibleValues;
	}


	public ContextNode getContextNode() {
		return contextNode;
	}

	public String toString(){
		return "SSBNNode:" + contextNode.getLabel() + " " + ovProblematic + "[" + possibleValues + "]";
	}

	@Override
	public boolean equals(Object o){
		
		SimpleContextNodeFatherSSBNNode context = (SimpleContextNodeFatherSSBNNode)o;
		
		boolean result = false; 
		
		if(context.contextNode.equals(this.contextNode)){
			if(context.ovProblematic.equals(this.ovProblematic)){
				result = true; 
			}
		}
		
		return result; 
		
	}
	
}
