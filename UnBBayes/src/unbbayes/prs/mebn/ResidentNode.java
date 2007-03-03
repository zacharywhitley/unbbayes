package unbbayes.prs.mebn;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.bn.ITabledVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticTable;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;

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
	
	private List<OrdinaryVariable> ordinaryVariableList; 
	
	private int numNextArgument = 0; 
	
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
	
	public void addArgument(OrdinaryVariable ov) throws ArgumentNodeAlreadySetException, 
	OVariableAlreadyExistsInArgumentList{
		
		Argument argument = new Argument(this.getName() + "_" + numNextArgument, this);
		
		this.addArgument(argument); 
		
		if(ordinaryVariableList.contains(ov)){
			throw new OVariableAlreadyExistsInArgumentList(); 
		}
		else{
			ordinaryVariableList.add(ov); 
			ov.addIsOVariableOfList(this); 
			argument.setOVariable(ov); 
		}
		
	}
	
	public void removeArgument(OrdinaryVariable ov){
		
		ordinaryVariableList.remove(ov);
		ov.removeIsOVariableOfList(this);
		
		for(Argument argument: super.getArgumentList()){
			if(argument.getOVariable() == ov){
				super.removeArgument(argument); 
				return; 
			}
		}
	}
	
	
	/**
	 * 
	 * @param ov
	 * @return
	 */
	public boolean containsArgument(OrdinaryVariable ov){
		return ordinaryVariableList.contains(ov); 
	}
	
	public List<OrdinaryVariable> getOrdinaryVariableList(){
		return ordinaryVariableList; 
	}
	
	
}

