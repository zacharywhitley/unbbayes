package unbbayes.prs.mebn.ssbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import unbbayes.prs.INode;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.util.Debug;

/**
 * A SimpleSSBNNode is a node used in the construction of the grand SSBN. This only 
 * have the information necessary for build the SSBN, not for the construction of
 * the CPT's. 
 * 
 * @author Laecio Lima dos Santos (laecio@gmail.com)
 */
public class SimpleSSBNNode implements INode {

	private static int ssbnNodeCount = 0; 
	private int id; 
	
	private final ResidentNode residentNode;
	
	private MFragInstance mFragInstance;
	
	private boolean finished; //indicate if the node already was processed 
	
	private List<SimpleSSBNNode> parents; 
	private List<INode> children; // we need to store the child nodes to test adjacency
	private List<SimpleContextNodeFatherSSBNNode> contextParents; 
	
	private OrdinaryVariable                    ovArray[];       //ordinary variables for the home mfrag (The order of the resident node)
	private LiteralEntityInstance               entityArray[];   //evaluation of the ov  
	private Map<MFrag, OrdinaryVariable[]>      ovArrayForMFrag; //correspondency between the ov of the home mFrag with the ov of the external MFrags. 

	private Entity state;	//if state != null this node is a finding
	
	private ProbabilisticNode probNode = null; 
	
	private boolean defaultDistribution = false; 
	private boolean evaluatedForHomeMFrag = false; 
	
	
	private SimpleSSBNNode(ResidentNode residentNode){
		
		id = this.ssbnNodeCount; 
		this.ssbnNodeCount++; 
		
		this.residentNode = residentNode; 
		
		this.finished = false;
		
		this.parents = new ArrayList<SimpleSSBNNode>(); 
		this.children = new ArrayList<INode>(); 
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
	 *
	 */
	@Override
	public boolean equals(Object obj) {

//		boolean result = true; 
		
		if(! (obj instanceof SimpleSSBNNode)){
			return false;
		}
		
		SimpleSSBNNode ssbnNode = (SimpleSSBNNode)obj;
		
		if(ssbnNode.getResidentNode().equals(this.getResidentNode())){
			
			for(int i = 0; i < entityArray.length; i++){
				if(!entityArray[i].equals(ssbnNode.entityArray[i])){
					return false; 
				}
			}
			return true;
		} 
		
		return false; 
		
	}

	@Override
	public String toString(){
		try {
			String ret = "";
			
			try {
				ret += residentNode.getName(); 
			} catch (Exception e) {
				Debug.println(getClass(), e.getMessage(), e);
			}
			
			ret+="(";
			try {
				for(int i = 0; i < ovArray.length; i++){
					ret+= "(";
					ret+= ovArray[i].getName();
					ret+= ","; 
					ret+= entityArray[i].getInstanceName(); 
					ret+= ")"; 
				}
			} catch (Exception e) {
				Debug.println(getClass(), e.getMessage(), e);
			}
			ret+=")";
			
			if(state != null){
				try {
					ret+= "=" + state.getName() + " [F] ";
				} catch (Exception e) {
					Debug.println(getClass(), e.getMessage(), e);
				}
			}
			
			ret+="[" + id + "]"; 
			
			return ret;  
		} catch (Exception e) {
			Debug.println(getClass(), e.getMessage(), e);
		}
		return super.toString();
	}
	
	public String getShortName(){
		
		String name = new String(this.residentNode.getName());
		name += "(";
		for(int i = 0; i < entityArray.length; i++){
			if (name.charAt(name.length() - 1) != '(') {
				name += ",";
			}
			name += entityArray[i].getInstanceName();
		}
		name += ")";
		
		return name;  
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

	/**
	 * Add a context node parent if it don't is at the context node parent list
	 * yet. 
	 */
	public void addContextParent(
			SimpleContextNodeFatherSSBNNode contextNodeParent) {
		
		if(!contextParents.contains(contextNodeParent)){
			this.contextParents.add(contextNodeParent);
		}
		
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

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.INode#addChildNode(unbbayes.prs.INode)
	 */
	public void addChildNode(INode child) throws InvalidParentException {
		if (!this.getChildNodes().contains(child)) {
			this.getChildNodes().add(child);
		}
		if (!child.getParentNodes().contains(this)) {
			child.getParentNodes().add(this); 
		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.INode#addParentNode(unbbayes.prs.INode)
	 */
	public void addParentNode(INode parent) throws InvalidParentException {
		this.addParent((SimpleSSBNNode)parent);
	}

	public void addParent(SimpleSSBNNode parent){
		
		if (!this.getParentNodes().contains(parent)) {
			this.getParentNodes().add(parent);
		}
		
		if (!parent.getChildNodes().contains(this)) {
			parent.getChildNodes().add(this);
		}
		
	}
	
	/**
	 * @deprecated do not use it
	 */
	public void appendState(String state) {
		throw new java.lang.UnsupportedOperationException("appendState");
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.INode#getAdjacentNodes()
	 */
	public List<INode> getAdjacentNodes() {
		List<INode> adjacents = new ArrayList<INode>();
		adjacents.addAll(this.getParentNodes());
		adjacents.addAll(this.getChildNodes());
		return adjacents;
	}

	public List<INode> getChildNodes() {
		return this.children;
	}

	public String getDescription() {
		return this.toString();
	}

	public String getName() {
		return this.toString();
	}

	public List<INode> getParentNodes() {
		return (List)this.getParents();
	}

	/**
	 * @deprecated use {@link #getState()}
	 */
	public String getStateAt(int index) {
		return this.getEntityArray()[index].getInstanceName();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.INode#getStatesSize()
	 */
	public int getStatesSize() {
		return this.getEntityArray().length;
	}

	/**
	 * @deprecated
	 * @see #getResidentNode()
	 */
	public int getType() {
		return this.getResidentNode().getType();
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.INode#removeChildNode(unbbayes.prs.INode)
	 */
	public void removeChildNode(INode child) {
		this.children.remove(child);
	}

	/**
	 * @deprecated
	 */
	public void removeLastState() {
		throw new java.lang.UnsupportedOperationException("removeLastState");
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.INode#removeParentNode(unbbayes.prs.INode)
	 */
	public void removeParentNode(INode parent) {
		this.parents.remove(parent);
	}

	/**
	 * @deprecated
	 */
	public void removeStateAt(int index) {
		throw new java.lang.UnsupportedOperationException("removeStateAt");
	}

	public void setChildNodes(List<INode> children) {
		this.children = children;
	}

	/**
	 * @deprecated
	 */
	public void setDescription(String text) {
		throw new java.lang.UnsupportedOperationException("setDescription");
	}

	/**
	 * @deprecated
	 */
	public void setName(String name) {
		throw new java.lang.UnsupportedOperationException("setName");
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.INode#setParentNodes(java.util.List)
	 */
	public void setParentNodes(List<INode> parents) {
		this.parents = (List)parents;
	}

	/**
	 * @deprecated use {@link #setState(Entity)} or {@link #getEntityArray()}
	 * 
	 */
	public void setStateAt(String state, int index) {
		throw new java.lang.UnsupportedOperationException("setStateAt");
	}

	/**
	 * @deprecated use {@link #setState(Entity)} or {@link #getEntityArray()}
	 */
	public void setStates(List<String> states) {
		throw new java.lang.UnsupportedOperationException("setStates");
	}

	public ProbabilisticNode getProbNode() {
		return probNode;
	}

	public void setProbNode(ProbabilisticNode probNode) {
		this.probNode = probNode;
	}
	
	public int getId(){
		return id; 
	}

	
}
