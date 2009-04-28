package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
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
	
	private MFragInstance mFragInstance;
	
	private boolean finished; //indicate if the node already was processed 
	
	private List<SimpleSSBNNode> parents; 
	private List<SimpleContextNodeFatherSSBNNode> contextParents; 
	
	private OrdinaryVariable                    ovArray[];       //ordinary variables for the home mfrag (The order of the resident node)
	private LiteralEntityInstance               entityArray[];   //evaluation of the ov  
	private Map<MFrag, OrdinaryVariable[]>      ovArrayForMFrag; //correspondency between the ov of the home mFrag with the ov of the external MFrags. 

	private Entity state;	//if state != null this node is a finding
	
	private boolean defaultDistribution = false; 
	private boolean evaluatedForHomeMFrag = false; 
	
	
	private SimpleSSBNNode(ResidentNode residentNode){
		
		this.residentNode = residentNode; 
		
		this.finished = false;
		
		this.parents = new ArrayList<SimpleSSBNNode>(); 
		this.contextParents = new ArrayList<SimpleContextNodeFatherSSBNNode>(); 
		
		this.state = null; 
		
		ovArray = new OrdinaryVariable[residentNode.getOrdinaryVariableList().size()]; 
		int index = 0; 
		for(OrdinaryVariable ordinaryVariable: residentNode.getOrdinaryVariableList()){
			ovArray[index] = ordinaryVariable; 
			index++; 
		}
		
		entityArray = new LiteralEntityInstance[residentNode.getOrdinaryVariableList().size()]; 
		ovArrayForMFrag = new HashMap<MFrag, OrdinaryVariable[]>(); 
		
	}
	
	public static SimpleSSBNNode getInstance(ResidentNode residentNode){
		return new SimpleSSBNNode(residentNode); 
	}
	
	/**
	 * Two SimpleSSBNNode are equals if: 
	 * 
	 * 1. It referes to the same Resident Node. 
	 * 2. The instanciated entity for each ordinary variable are the same. 
	 */
	@Override
	public boolean equals(Object obj) {

		boolean result = true; 
		
		SimpleSSBNNode ssbnNode = (SimpleSSBNNode)obj;
		
		if(ssbnNode.getResidentNode().equals(this.getResidentNode())){
			
			for(int i = 0; i < entityArray.length; i++){
				if(!entityArray[i].equals(ssbnNode.entityArray[i])){
					result = false; 
					break; 
				}
			}
			
		}else{
			result = false; 
		}
		
		return result; 
		
	}

	@Override
	public String toString(){
		String ret = residentNode.getName(); 

		ret+="(";
		for(int i = 0; i < ovArray.length; i++){
			ret+= "(";
			ret+= ovArray[i].getName();
			ret+= ","; 
			ret+= entityArray[i].getInstanceName(); 
			ret+= ")"; 
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

	public boolean isEvaluatedForHomeMFrag() {
		return evaluatedForHomeMFrag;
	}

	public void setEvaluatedForHomeMFrag(boolean evaluatedForHomeMFrag) {
		this.evaluatedForHomeMFrag = evaluatedForHomeMFrag;
	}

	public List<SimpleContextNodeFatherSSBNNode> getContextParents() {
		return contextParents;
	}

	public void addContextParent(
			SimpleContextNodeFatherSSBNNode contextParents) {
		this.contextParents.add(contextParents);
	}

	
	
	
	// OV AND ENTITIES METHODS
	
	/**
	 * Set the value of ov how the entity if this node have the ov how argument. 
	 * If it don't have, don't do anything. 
	 */
	public void setEntityForOv(OrdinaryVariable ov, LiteralEntityInstance lei){
		
		for(int i = 0; i < ovArray.length; i++){
			if(ovArray[i].equals(ov)){
				entityArray[i] = lei; 
				break; 
			}
		}
	}
	
	public OrdinaryVariable[] getOvArray() {
		return ovArray;
	}

	public LiteralEntityInstance[] getEntityArray() {
		return entityArray;
	}

	public LiteralEntityInstance getEntityForOv(OrdinaryVariable ov){
		
		LiteralEntityInstance entity = null;
		
		for(int i = 0; i < ovArray.length; i++){
			if(ovArray[i].equals(ov)){
				entity = entityArray[i]; 
				break; 
			}
		}
		
		return entity; 
	}
	
	public Map<MFrag, OrdinaryVariable[]> getOvArrayForMFrag() {
		return ovArrayForMFrag;
	}

	
}
