package unbbayes.prs.mebn.ssbn;

import java.util.Collection;
import java.util.List;

import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.TreeVariable;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.ssbn.exception.SSBNNodeGeneralException;

/**
 * Encapsule the SSBN generated by the ISSBNGenerator. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 *
 */
public class SituationSpecificBayesianNetwork {

	private final ProbabilisticNetwork probabilisticNetwork; 
	private final List<SSBNNode> findingList; 
	private final List<Query> queryList; 
	
	private enum State{
		INITIAL, 
		COMPILED, 
		WITH_FINDINGS, 
		FINDINGS_PROPAGATED, 
		USER_ACTION
	}
	
	private State state = State.INITIAL; 
	
	/**
	 *
	 * @param pn Probabilistic network get on the algorith 
	 * @param findingList List of SSBNNode's where for each element the property isFinding = true
	 * @param queryList List of queries
	 */
	public SituationSpecificBayesianNetwork(
			ProbabilisticNetwork pn, 
			List<SSBNNode> findingList, 
			List<Query> queryList){
	
		this.probabilisticNetwork = pn; 
		this.findingList = findingList; 
		this.queryList = queryList;
		
	}
	
	/**
	 * Initialize the ssbn: 
	 * 1) Compile the network
	 * 2) Add the findings
	 * 3) Propagate the findings
	 * 
	 * After this, the network is ready to show to the user
	 * @throws Exception 
	 */
	public void initializeSSBN() throws Exception{
		compileNetwork(); 
		addFindings();
		propagateFindings(); 
	}
	
	private void compileNetwork() throws Exception{
		probabilisticNetwork.compile(); 
		state = State.COMPILED; 
	}
	
	
	/**
	 * Propagate the findings 
	 * 
	 * Pre-Requisite: 
	 * - All the nodes of the list of findings have only one actual value
	 */
	private void addFindings() throws SSBNNodeGeneralException{
		
		for(SSBNNode findingNode: findingList){
			TreeVariable node = findingNode.getProbNode();
			
			Collection<Entity> actualValues = findingNode.getActualValues(); 			

			String nameState = actualValues.toArray(new Entity[1])[0].getName(); 
			
			boolean ok = false; 
			for(int i = 0; i < node.getStatesSize(); i++){
				if(node.getStateAt(i).equals(nameState)){
					node.addFinding(i);
					ok = true; 
					break; 
				}
			}
			
			if(!ok){
				throw new SSBNNodeGeneralException(); 
			}
			
		}
		
		state = State.WITH_FINDINGS; 
		
	}
	
	private void propagateFindings() throws Exception{
		probabilisticNetwork.updateEvidences();
		state = State.FINDINGS_PROPAGATED; 
	}

	public ProbabilisticNetwork getPn() {
		return probabilisticNetwork;
	}

	public List<SSBNNode> getFindingList() {
		return findingList;
	}

	public List<Query> getQueryList() {
		return queryList;
	}
	
	
}
