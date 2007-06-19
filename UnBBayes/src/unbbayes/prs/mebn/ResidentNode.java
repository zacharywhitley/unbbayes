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

	private List<OrdinaryVariable> ordinaryVariableList; 
	
	private List<ResidentNodePointer> listPointers; 
	
	private int numNextArgument = 0; 
	
	public ResidentNode(){
		
		super(); 
		listPointers = new ArrayList<ResidentNodePointer>(); 
		ordinaryVariableList = new ArrayList<OrdinaryVariable>(); 
		
	}
	
	public void addResidentNodePointer(ResidentNodePointer pointer){
		listPointers.add(pointer); 
	}
	
	public void removeResidentNodePointer(ResidentNodePointer pointer){
		listPointers.remove(pointer); 
	}
	
	/**
	 *@see unbbayes.prs.bn.ITabledVariable#getPotentialTable()
	 */
	public PotentialTable getPotentialTable() {
		return null;
	}
	
	/**
	 * Add a ov in the list of arguments in this resident node
	 * 
	 * @param ov
	 * @throws ArgumentNodeAlreadySetException
	 * @throws OVariableAlreadyExistsInArgumentList
	 */
	public void addArgument(OrdinaryVariable ov) throws ArgumentNodeAlreadySetException, 
	OVariableAlreadyExistsInArgumentList{
		
		if(ordinaryVariableList.contains(ov)){
			throw new OVariableAlreadyExistsInArgumentList(); 
		}
		else{
			ordinaryVariableList.add(ov); 
			ov.addIsOVariableOfList(this); 
		}
	}
	
	/**
	 * Delete the extern references for this node
	 * 
	 * - Ordinary Variables
	 */
	public void delete(){
		while(!ordinaryVariableList.isEmpty()){
			ordinaryVariableList.remove(0).removeIsOVariableOfList(this); 
		}
	}
	
	public void removeArgument(OrdinaryVariable ov){
		
		ordinaryVariableList.remove(ov);
		//ov.removeIsOVariableOfList(this); -> deve ser feito pela classe que chama. 
		
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

