package unbbayes.prs.mebn.kb;

import java.util.ArrayList;
import java.util.List;

import unbbayes.prs.mebn.OrdinaryVariable;

/**
 * Encapsule the result of a evaluation of a context node that returns the 
 * values for the ordinary variables that satisfies the formula. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class SearchResult{
	
	private OrdinaryVariable[] ordinaryVariableSequence;
	private List<String[]> valuesResultList; 
	
	public SearchResult(OrdinaryVariable[] _ordinaryVariableSequence){
		this.ordinaryVariableSequence = _ordinaryVariableSequence;
		valuesResultList = new ArrayList<String[]>(); 
	}

	public void addResult(String[] result){
		valuesResultList.add(result); 
	}
	
	public OrdinaryVariable[] getOrdinaryVariableSequence() {
		return ordinaryVariableSequence;
	}

	public List<String[]> getValuesResultList() {
		return valuesResultList;
	}
	
}
