package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;

/**
 * A SimpleSSBNNode it a node used in the building of the grand SSBN. This only 
 * have the information necessary for build the SSBN, not for build the CPT. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class SimpleSSBNNode {

	private final ResidentNode residentNode; 
	
	private boolean finished; //indicate if the node already was processed 
	
	private List<SimpleSSBNNode> parents; 
	
	private MFragInstance mFragInstance;
	
	//Contain the arguments for the home MFrag of the resident node
	private List<OVInstance> listArguments; 
	
	//Contain the arguments for the input's MFrag's of the resident node
	private Map<MFrag, List<OVInstance>> argumentsForMFrag; 
	
	//if state != null this node is a finding
	private Entity state;
	
	private boolean defaultDistribution = false; 
	
	private SimpleSSBNNode(ResidentNode residentNode){
		
		this.residentNode = residentNode; 
		
		this.finished = false;
		
		this.parents = new ArrayList<SimpleSSBNNode>(); 
		this.listArguments = new ArrayList<OVInstance>(); 
		this.argumentsForMFrag = new HashMap<MFrag, List<OVInstance>>(); 
		
		this.state = null; 
		
	}
	
	public static SimpleSSBNNode getInstance(ResidentNode residentNode){
		return new SimpleSSBNNode(residentNode); 
	}
	
	//Evaluate if two SimpleSSBNNodes are equals. 
	public boolean equals(Object obj) {

		return true; 
		
	}

	@Override
	public String toString(){
		String ret = residentNode.getName(); 

		ret+="(";
		for(OVInstance instance: listArguments){
			ret+= instance.toString();
		}
		ret+=")";

		if(state != null){
			ret+= "=" + state.getName() + " [F] ";
		}

		return ret;  
	}
	
	// GET AND SET METHODS
	
	public ResidentNode getResidentNode() {
		return residentNode;
	}

	public boolean isFinished() {
		return finished;
	}
	
	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public List<SimpleSSBNNode> getParents() {
		return parents;
	}

	
	public void addParent(SimpleSSBNNode parent){
		parents.add(parent); 
	}

	public MFragInstance getMFragInstance() {
		return mFragInstance;
	}

	public void setMFragInstance(MFragInstance mFragInstance) {
		this.mFragInstance = mFragInstance;
	}

	public List<OVInstance> getArgumentList() {
		return listArguments;
	}
	
	public void addArgument(OVInstance argument){
		this.listArguments.add(argument); 
	}

	public List<OVInstance> getArgumentsForExternalMFrag(MFrag externalMFrag) {
		return argumentsForMFrag.get(externalMFrag);
	}

	public void addArgumentsForExternalMFrag(MFrag externalMFrag, List<OVInstance> argumentList){
		this.argumentsForMFrag.put(externalMFrag, argumentList); 
	}

	public Entity getState() {
		return state;
	}

	public void setState(Entity state) {
		this.state = state;
	}
	
	public boolean isFinding(){
		return (state != null); 
	}

	public boolean isDefaultDistribution() {
		return defaultDistribution;
	}

	public void setDefaultDistribution(boolean defaultDistribution) {
		this.defaultDistribution = defaultDistribution;
	}
	

	
}
