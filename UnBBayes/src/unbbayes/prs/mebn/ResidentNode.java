package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticTable;

public class ResidentNode extends MultiEntityNode implements ITabledVariable {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 8497908054569004909L;
	 
	private List<InputNode> inputNodeFatherList;
	 
	private List<InputNode> inputInstanceFromList;
	 
	private ProbabilisticTable probabilisticTable;
	 
	private List<ResidentNode> residentNodeFatherList;
	 
	private List<ResidentNode> residentNodeChildList;
	
	private List<OrdinaryVariable> ordinaryVariableList; 
	 
	public ResidentNode(){
		
		super(); 
		
		ordinaryVariableList = new ArrayList<OrdinaryVariable>(); 
	}
	
	/**
	 *@see unbbayes.prs.bn.ITabledVariable#getPotentialTable()
	 */
	public PotentialTable getPotentialTable() {
		return null;
	}
	
	
	public void addOrdinaryVariable(OrdinaryVariable ov){
		ordinaryVariableList.add(ov); 
	}
	
	public void removeOrdinaryVariable(OrdinaryVariable ov){
	    ordinaryVariableList.remove(ov); 	
	}
	
	public boolean containsOrdinaryVariable(OrdinaryVariable ov){
	  	return ordinaryVariableList.contains(ov); 
	}
	
	public List<OrdinaryVariable> getOrdinaryVariableList(){
		return ordinaryVariableList; 
	}
	 
}
 
