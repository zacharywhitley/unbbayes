package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticTable;

/**
 *
 */

public class ResidentNode extends MultiEntityNode implements ITabledVariable {
 
	private static final long serialVersionUID = 8497908054569004909L;
	
	private List<InputNode> inputNodeFatherList;
	 
	private List<InputNode> inputInstanceFromList;
	 
	private ProbabilisticTable probabilisticTable;
	 
	private List<ResidentNode> residentNodeFatherList;
	 
	private List<ResidentNode> residentNodeChildList;
	
	/**
	 * List of ordinary variables of this node. Don't have duplicates
	 * elements. 
	 */
	
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
	
	/**
	 * add the ordinary variable in the list of ordinary variables 
	 * of the node (if it alredy is present, don't do nothing)
	 * @param ov
	 */
	
	public void addOrdinaryVariable(OrdinaryVariable ov){
		if(!ordinaryVariableList.contains(ov)){
		   ordinaryVariableList.add(ov); 
		   ov.addIsOVariableOfList(this); 
		}
	}
	
	/**
	 * remove the ordinary variable of the list of ordinary variables
	 * of the node. 
	 * @param ov
	 */
	public void removeOrdinaryVariable(OrdinaryVariable ov){
	    ordinaryVariableList.remove(ov);
	    ov.removeIsOVariableOfList(this); 
	}
	
	/**
	 * 
	 * @param ov
	 * @return
	 */
	public boolean containsOrdinaryVariable(OrdinaryVariable ov){
	  	return ordinaryVariableList.contains(ov); 
	}
	
	/**
	 * 
	 * @return
	 */
	public List<OrdinaryVariable> getOrdinaryVariableList(){
		return ordinaryVariableList; 
	}
	
	
}
 
