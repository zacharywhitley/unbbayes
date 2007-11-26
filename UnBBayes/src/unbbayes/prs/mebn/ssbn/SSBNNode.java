/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.Edge;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.GenerativeInputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.*;
import unbbayes.prs.mebn.compiler.ICompiler;
import unbbayes.prs.mebn.compiler.Compiler;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.MEBNException;
import unbbayes.util.NodeList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;


/**
 *
 * @author shou matsumoto
 *
 */
public class SSBNNode {
	
	/*
	 * Inner class which represents argument instances (which are instances of entities). It links
	 * instances of entities and the ordinal variable it replaces.
	 * @author shou matsumoto
	 *
	 */
	
	
	// Private Attributes
	
	private DomainResidentNode resident = null;	// what resident node this instance represents
	private ProbabilisticNode  probNode = null;	// stores the UnBBayes BN ordinal node which represents this SSBNNode
	
	private List<OVInstance> arguments = null;
	private Collection<SSBNNode> parents = null;
	
	private Collection<Entity> actualValues = null; // this is the possible values of this node at that moment (might be one, if there is an evidence)
													// this is useful when this node must provide some values different than the resident nodes' ones
	
	//private boolean isUsingDefaultCPT = false;	// checks if this node should use defaultCPT
	
	private String strongOVSeparator = ".";	// When creating names for sets of strong OVs, this string/char separates the compound names. Ex. When separator is ".", ovs = {st,z} -> name= "st.z"
	
	
	
	private ICompiler compiler = null;
	
	private boolean isFinding = false;
	private boolean isContext = false; 
	
	private ProbabilisticNetwork probabilisticNetwork = null;
	
	private ResourceBundle resource = 
		ResourceBundle.getBundle("unbbayes.prs.mebn.ssbn.resources.Resources");	
	
	// Constructors
	
	
	
	private SSBNNode (ProbabilisticNetwork pnet, DomainResidentNode resident , ProbabilisticNode probNode, boolean isFinding) {
		
		this.arguments = new ArrayList<OVInstance>();
		this.parents = new ArrayList<SSBNNode>();
		this.resident = resident;
		
		if (pnet == null) {
			this.probabilisticNetwork = new ProbabilisticNetwork(this.resource.getString("DefaultNetworkName"));
		} else {
			this.probabilisticNetwork = pnet;
		}
		
		if (isFinding) {
			this.setProbNode(null);
		} else {
			if (probNode == null) {
				this.probNode = new ProbabilisticNode();
			}else{
				this.setProbNode(probNode);
			}
		}
		
		this.appendProbNodeState();	// if OK, probNode's states become the same of the resident's one
		
		if (this.getProbNode() != null) {
			this.probabilisticNetwork.addNode(this.getProbNode());
		}
		
		this.actualValues = new ArrayList<Entity>();
		if (this.getProbNode() != null) {
			for (StateLink state : resident.getPossibleValueLinkList()) {
				this.actualValues.add(state.getState());
			}
		} else {
			this.actualValues.add(resident.getPossibleValueLinkList().get(0).getState());
		}
		
		
		this.setUsingDefaultCPT(false);
		
		this.setCompiler(new Compiler(resident, this));
		
		this.isFinding = isFinding;
		
		
		
	}
	
	/**
	 *  This class is a temporary representation of a resident random variable instance at ssbn creation step.
	 * Basically, works as a bridge (not a design pattern) between MEBN solid resident node representation (DomainResidentNode)
	 * and the actual ProbabilisticNode on UnBBayes.
	 * @param resident: the resident node this SSBNNode represents
	 * @param probNode: this is useful when we already know which ProbabilisticNode (UnBBayes representation of a node) shall
	 * represent this node once SSBN is generated.
	 * If declared as a finding, probNode will be set to null (the value would be the 1st possible value declared
	 * by its resident node).
	 * @param isFinding: declares this node as a finding node, making it impossible to set multiple values and add a parent
	 * @param probabilisticNetwork: the network which probNode should work on. If null, a new one will be created.
	 * @return a SSBNNode instance.
	 */
	public static SSBNNode getInstance (ProbabilisticNetwork probabilisticNetwork,DomainResidentNode resident , ProbabilisticNode probNode, boolean isFinding)  {
		return new SSBNNode(probabilisticNetwork, resident,probNode, isFinding);
	}
	
	/*
	 *  This class is a temporary representation of a resident random variable instance at ssbn creation step.
	 * Basically, works as a bridge (not a design pattern) between MEBN solid resident node representation (DomainResidentNode)
	 * and the actual ProbabilisticNode on UnBBayes.
	 * @param resident: the resident node this SSBNNode represents
	 * @param probNode: this is useful when we already know which ProbabilisticNode (UnBBayes representation of a node) shall
	 * represent this node once SSBN is generated.
	 * If declared as a finding, probNode will be set to null (the value would be the 1st possible value declared
	 * by its resident node).
	 * @param isFinding: declares this node as a finding node, making it impossible to set multiple values and add a parent
	 * @return a SSBNNode instance.
	 */
	//public static SSBNNode getInstance (DomainResidentNode resident , ProbabilisticNode probNode, boolean isFinding)  {
	//	return new SSBNNode(null,resident,probNode, isFinding);
	//}
	
	/*
	 *  This class is a temporary representation of a resident random variable instance at ssbn creation step.
	 * Basically, works as a bridge (not a design pattern) between MEBN solid resident node representation (DomainResidentNode)
	 * and the actual ProbabilisticNode on UnBBayes. This is, for now, identical to getInstance(null, resident,null, false)
	 * NOTE THAT THIS IS GOING TO CREATE A NEW PROBABILISTICNETWORK
	 * @param resident: the resident node this SSBNNode represents
	 * @return a SSBNNode instance.
	 * 
	 */
	
	//public static SSBNNode getInstance (DomainResidentNode resident)  {
	//	return new SSBNNode(null,resident,null, false);
	//}
	
	/**
	 *  This class is a temporary representation of a resident random variable instance at ssbn creation step.
	 * Basically, works as a bridge (not a design pattern) between MEBN solid resident node representation (DomainResidentNode)
	 * and the actual ProbabilisticNode on UnBBayes. This is, for now, identical to getInstance(null, resident,null, false)
	 * @param resident: the resident node this SSBNNode represents
	 * @param net: a probabilistic network where the probabilistic node associated with this node will be inserted into.
	 * If null, a new one will be created.
	 * @return a SSBNNode instance.
	 * 
	 */
	public static SSBNNode getInstance (ProbabilisticNetwork net ,DomainResidentNode resident)  {
		return new SSBNNode(net,resident,null, false);
	}
	
	
	// private methods
	
	
	
	private void appendProbNodeState() {
		if (this.getProbNode() == null) {
			return;
		}
		if (this.getResident() != null) {
			for (Entity entity : this.resident.getPossibleValueList()) {
				this.getProbNode().appendState(entity.getName());
			}
		}
		if (this.getProbNode().getPotentialTable() != null) {
			this.getProbNode().getPotentialTable().addVariable(this.getProbNode());
		}
	}
	
	
	private String getNameByDots(String...names) {
		String dotName = new String(names[0]);
		for (int i = 1; i < names.length; i++) {
			dotName.concat("." + names[i]);
		}
		return dotName;
	}
	
	private String getNameByDots(Collection<OrdinaryVariable> ovs) {
		OrdinaryVariable[] ovArray = new OrdinaryVariable[ovs.size()];
		int i = 0;
		for (OrdinaryVariable variable : ovs) {
			ovArray[i] = variable;
			i++;
		}
			
		String dotName = new String(ovArray[0].getName());
		for (i = 1; i < ovArray.length; i++) {
			dotName+= ("." + ovArray[i].getName());
		}
		return dotName;
	}
	
	private boolean isAllValuesBelow(int value, int[] vector) {
		for (int i = 0; i < vector.length; i++) {
			if(vector[i] >= value) {
				return false;
			}
		}
		return true;
	}
	
	private int factorial(int n) {
		int ret = 1;
		for (int i = 1; i <= n; i++) {
			ret *= i;
		}
		return ret;
	}
	
	private int combination(int n , int by) {
		if (n < by) {
			return 1;
		}
		return (factorial(n) / (factorial(by)*factorial(n-by)));
	}
	
	private Collection<Collection<OrdinaryVariable>> getOVCombination(int byHowMany, OrdinaryVariable... ovs ){
		
		Collection<Collection<OrdinaryVariable>> ret = new ArrayList<Collection<OrdinaryVariable>>();
		if (byHowMany <= 0) {
			return ret;
		}
		if (byHowMany > ovs.length) {
			return ret;
		}
		
		OrdinaryVariable[] ovArray = (OrdinaryVariable[])ovs;
		int[] indexes = new int[byHowMany];
		
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}
		
		Collection<OrdinaryVariable> tempOVs = null;
		//while (indexes[0] <= ovArray.length - indexes.length)
		int combination = this.combination(ovArray.length, byHowMany);
		for (int i = 0; i < combination; i++) {
			
			tempOVs = new ArrayList<OrdinaryVariable>();
			for (int j = 0; j < indexes.length; j++) {
				tempOVs.add(ovArray[indexes[j]]);
			}
			ret.add(tempOVs);
			
			indexes[indexes.length - 1]++;
			for (int j = indexes.length - 2; j >= 0; j--) {
				if (indexes[j + 1] > ovArray.length - (indexes.length - (j+1))) {
					if (indexes[j] + 1 < indexes[j + 1]) {
						indexes[j]++;
						for (int k = j + 1; k < indexes.length; k++) {
							indexes[k] = indexes[k-1] + 1;
						}
						
					}
				}
			}
		}
		return ret;
	}
	
	private Collection<Collection<OrdinaryVariable>> getOVCombination(int byHowMany, Collection<OrdinaryVariable> ovs ) {
		OrdinaryVariable[] array = new OrdinaryVariable[ovs.size()];
		int i = 0;
		for (OrdinaryVariable ov : ovs) {
			array[i] = ov;
			i++;
		}
		return this.getOVCombination(byHowMany, array);
	}
	
	
	private Collection<OrdinaryVariable> getAllParentsOV() {
		Set<OrdinaryVariable> ovs = new HashSet<OrdinaryVariable>();
		for (SSBNNode parent : this.getParents()) {
			ovs.addAll(parent.getOVs());
		}
		return ovs;
	}
	
	
	// exported methods
	
	
	/**
	 * 
	 * @return a set of OVs that exists as this node's arguments.
	 */
	public Collection<OrdinaryVariable> getOVs () {
		Set<OrdinaryVariable> ovs = new HashSet<OrdinaryVariable>();
		for (OVInstance element : this.arguments) {
			ovs.add(element.getOv());
		}
		return ovs;
	}
	
	
	/**
	 * 
	 * @param ov
	 * @return true if ov is an argument of this node. False otherwise.
	 */
	public boolean hasOV(OrdinaryVariable ov) {
		for (OVInstance element : this.getArguments()) {
			if (element.getOv().equals(ov)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 
	 * @param name: the name to search for.
	 * @param isOVName: true if the search is by the name of OV. False if the search is by
	 * the name of entity instance.
	 * @return true if the node contains that OV (or entity instance) as its argument. False otherwise.
	 */
	public boolean hasOV( String name , boolean isOVName) {
		if (isOVName) {
			for (OVInstance element : this.getArguments()) {
				if (element.getOv().getName().equalsIgnoreCase(name)) {
					return true;
				}
			}
		} else {
			for (OVInstance element : this.getArguments()) {
				if (element.getEntity().getInstanceName().equalsIgnoreCase(name)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	
	/**
	 * 
	 * @param ordinaryVariables: collection of ordinaryVariables to be found as argument
	 * @return true if the node's argument contains all ordinary variables passed by its arguments.
	 * It will also return false when ovs is invalid (null or having 0 elements)
	 */
	public boolean hasAllOVs(Collection<OrdinaryVariable> ovs) {
		if (ovs == null) {
			return false;
		}
		if (ovs.size() <= 0) {
			return false;
		}
		for (OrdinaryVariable variable : ovs) {
			if (!this.hasOV(variable)) {
				return false;
			}
		}
		return true;
	}
	
	
	/**
	 * 
	 * @param ordinaryVariables: collection of ordinaryVariables to be found as argument
	 * @return true if the node's argument contains all ordinary variables passed by its arguments.
	 * It will also return false when ovs is invalid (null or having 0 elements)
	 */
	public boolean hasAllOVs(OrdinaryVariable... ovs) {
		if (ovs == null) {
			return false;
		}
		if (ovs.length <= 0) {
			return false;
		}
		for (OrdinaryVariable variable : ovs) {
			if (!this.hasOV(variable)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 
	 * @param ovs: array of ordinaryVariables' names to be found as argument
	 * @param isOVName: true to search by OVName. False to search by Entity Instance Name
	 * @return true if the node's argument contains all names passed by its arguments.
	 */
	public boolean hasAllOVs(boolean isOVName, String...ovs) {
		if (ovs == null) {
			return false;
		}
		for (int i = 0; i < ovs.length; i++) {
			if (!this.hasOV(ovs[i],isOVName)) {
				return false;
			}
		}
		return true;
	}
	
	
	
	
	
	/**
	 * Adds an argument. If input is null, NullPointerException should be returned.
	 * It does nothing when entityInstanceName is not null but empty!!
	 * @param ov: ordinal variable associated to this argument
	 * @param entityInstanceName: name of a OV instance (ex. !ST4, !Z0, !T1 ...)
	 */
	public void addArgument(OrdinaryVariable ov, String entityInstanceName) throws SSBNNodeGeneralException {
		if (entityInstanceName.length() <= 0) {
			return;
		}
		this.arguments.add(OVInstance.getInstance( ov, entityInstanceName , ov.getValueType() ));
	}
	
	/**
	 * Adds an argument. 
	 * @param ovInstance
	 */
	public void addArgument(OVInstance ovInstance){
		this.arguments.add(ovInstance); 
	}
	
	/**
	 * Adds an argument at a particular position	 * 
	 * It does nothing when entityInstanceName is not null but empty!!
	 * @param ov: ordinal variable associated to this argument
	 * @param entityInstanceName: name of a OV instance (ex. !ST4, !Z0, !T1 ...)
	 * @param pos: position (when argument order is important) to add this argument
	 */
	public void addArgument(OrdinaryVariable ov, String entityInstanceName,  int pos) throws SSBNNodeGeneralException {
		if (entityInstanceName.length() <= 0) {
			return;
		}
		this.arguments.add(pos, OVInstance.getInstance( ov, entityInstanceName , ov.getValueType() ));
	}
	
	
	
	
	
	
	
	
	/**
	 * This is the same as setting node's actual value as a unique value
	 * and setting ProbNode to null.
	 * @param uniqueValue: the unique value this node represents
	 */
	public void setNodeAsFinding(Entity uniqueValue) {
		Collection actualValue = new ArrayList<Entity>();
		actualValue.add(uniqueValue);
		this.setActualValues(actualValue);
		this.setFinding(true);
		this.setProbNode(null);
	}
	
	
	
	public void fillProbabilisticTable() throws MEBNException {
		this.compiler.generateCPT(this);
	}
	
	
	
	// Parent controller
	
	/**
	 * This will add a parent to this node. It may check if the resident node
	 * remains consistent. If argument is null, it throws NullPointerException
	 * PLEASE NOTE IT DOES NOT ADD AN EDGE YET! ADD IT AT AN OUTSIDE METHOD
	 * @param parent the node to be added as parent. Its ProbNode will be added as
	 * this ProbNode's parent and, if said so, its resident node will be checked if it is the
	 * expected parent node by this node's resident node.
	 * @param isCheckingParentResident true to check if parent's resident node was expected
	 * by child's resident node; false to disable check
	 * @throws SSBNNodeGeneralException when parent has no resident node or ProbNode or 
	 * there were inconsistency when isCheckingParentResident was set to true.
	 */
	public void addParent(SSBNNode parent, boolean isCheckingParentResident) throws SSBNNodeGeneralException{
		
		// initial check. Note that if node is finding (probNode==null), then it should not have a parent
		if ((parent.getResident() == null )) {
			throw new SSBNNodeGeneralException();
		}
		/*
		if (isCheckingParentResident && ( parent.getProbNode() == null ) ) {
			throw new SSBNNodeGeneralException();
		}
		*/
		if (this.isFinding()) {
			throw new SSBNNodeGeneralException();
		}
		
		// perform consistency check
		if (isCheckingParentResident) {
			NodeList expectedParents = this.getResident().getParents();
			boolean isConsistent = false;
			GenerativeInputNode input = null;
			for (int i = 0; i < expectedParents.size(); i++) {
				if (parent.getResident() == expectedParents.get(i)) {
					isConsistent = true;
					break;
				}
				if (expectedParents.get(i) instanceof GenerativeInputNode) {
					input = (GenerativeInputNode)expectedParents.get(i);
					if (input.getResidentNodePointer().getResidentNode() == parent.getResident()) {
						isConsistent = true;
						break;
					}
				}
			}
			if (!isConsistent) {
				throw new SSBNNodeGeneralException();
			}
			// check if both probNodes are in a same network
			if (this.getProbNode() != null) {
				if (parent.getProbNode() != null) {
					if (this.getProbabilisticNetwork() != parent.getProbabilisticNetwork()) {
						throw new SSBNNodeGeneralException(this.resource.getString("IncompatibleNetworks"));
					}
				}
			}
		}
		
		this.getParents().add(parent);		
		if (this.getProbNode() != null) {
			this.getProbNode().addParent(parent.getProbNode());
			if (parent.getProbNode() != null){
				Edge edge = new Edge(parent.getProbNode(), this.getProbNode());
				//if (this.getProbabilisticNetwork() != null) {
					//this.getProbabilisticNetwork().addEdge(edge);
				//}
			}
		}
	}
	
	
	protected void removeParent(SSBNNode parent) {
		this.getParents().remove(parent);
		if (this.getProbNode() != null) {
			this.getProbNode().getParents().remove(this.getProbNode());
		}
		// TODO solve dangling references
	}
	
	
	protected void removeParentByName(String name) {
		if (name == null) {
			return;
		}
		if (this.isFinding) {
			return;
		}
		Collection<SSBNNode> parents = this.getParents();
		Collection<SSBNNode> removingNodes = new ArrayList<SSBNNode>();
		for (SSBNNode node : parents) {
			if (node.getName().compareToIgnoreCase(name) == 0) {
				// we do not remove directly because of concurrent modification exception
				removingNodes.add(node);
			}
		}
		parents.removeAll(removingNodes);
	}
	
	
	/**
	 * 
	 * @param ovNames: names of the strong ovs
	 * @param isExactMatch: true if "only" the ovs passed by arguments should be considered (nothing more than). False
	 * if "at least" those passed by argument should be considered.
	 * @return a collection of SSBNNode which contains ONLY the ovs passed by the arguments.
	 * If invalid argument was passed, then it will return an empty collection (size = 0)
	 */
	public Collection<SSBNNode> getParentSetByStrongOV(boolean isExactMatch, String...ovNames ) {
		Collection<SSBNNode> parents = new HashSet();
		if (ovNames == null) {
			return parents;
		}
		if (ovNames.length <= 0) {
			return parents;
		}
		if (this.isFinding) {
			return parents;
		}
		for (SSBNNode parent : this.parents) {
			if (parent.hasAllOVs(true, ovNames)) {
				if (isExactMatch) {
					if (parent.getOVs().size() == ovNames.length) {
						parents.add(parent);
					}
				} else {
					parents.add(parent);
				}
			}
		}
		return parents;
	}
	
	
	/**
	 * 
	 * @param setOfOV: set of strong ovs
	 * @param isExactMatch: true if "only" the ovs passed by arguments should be considered (nothing more than). False
	 * if "at least" those passed by argument should be considered.
	 * @return a collection of SSBNNode which contains ONLY the ovs passed by the arguments
	 * If invalid argument was passed, then it will return an empty collection (size = 0)
	 */
	public Collection<SSBNNode> getParentSetByStrongOV(boolean isExactMatch, Collection<OrdinaryVariable> setOfOV) {
		Collection<SSBNNode> parents = new HashSet();
		if (setOfOV == null) {
			return parents;
		}
		if (this.isFinding) {
			return parents;
		}
		for (SSBNNode parent : this.parents) {
			if (parent.hasAllOVs(setOfOV)) {
				if (isExactMatch) {
					if (parent.getOVs().size() == setOfOV.size()) {
						parents.add(parent);
					}	
				} else {
					parents.add(parent);
				}
							
			}
		}
		return parents;
	}
	
	/**
	 * 
	 * @param setOfOV: set of strong ovs
	 * @param isExactMatch: true if "only" the ovs passed by arguments should be considered (nothing more than). False
	 * if "at least" those passed by argument should be considered.
	 * @return a collection of SSBNNode which contains ONLY the ovs passed by the arguments
	 * If invalid argument was passed, then it will return an empty collection (size = 0)
	 */
	public Collection<SSBNNode> getParentSetByStrongOV(boolean isExactMatch,OrdinaryVariable... setOfOV) {
		Collection<SSBNNode> parents = new HashSet();
		if (setOfOV == null) {
			return parents;
		}
		if (setOfOV.length <= 0) {
			return parents;
		}
		if (this.isFinding) {
			return parents;
		}
		for (SSBNNode parent : this.parents) {
			if (parent.hasAllOVs(setOfOV)) {
				if (isExactMatch) {
					if (parent.getOVs().size() == setOfOV.length) {
						parents.add(parent);
					}	
				} else {
					parents.add(parent);
				}
				
			}
		}
		return parents;
	}
	
	
	/**
	 * "Automagically" sets up a map containing SSBNs by strong ov's, considering anything but argument as strong variables.
	 * @param weakOV anything but these ovs will be considered as "strong"
	 * @return a map containing a set name (which would be strong ov names separated by dots) and parent SSBNNodes containing such ovs as arguments.
	 */
	public Map<String, Collection<SSBNNode>> getParentMapByWeakOV(OrdinaryVariable...weakOVs) {
		
		Map<String, Collection<SSBNNode>> ret = new HashMap<String, Collection<SSBNNode>>();
		if (this.isFinding) {
			return ret;
		}
		List<SSBNNode> knownNodes = new ArrayList<SSBNNode>();
		
		// Disconsider weak ovs
		Collection<OrdinaryVariable> strongOVs = new ArrayList<OrdinaryVariable>(this.getAllParentsOV());
		for (int i = 0; i < weakOVs.length; i++) {
			strongOVs.remove(weakOVs[i]);
		}
		
		// start collecting parents, first the ones w/ more arguments
		Collection<Collection<OrdinaryVariable>>  ovCombo = null;
		Collection<SSBNNode> tempParentSet = null;
		Collection<SSBNNode> ignoringParentSet = null;
		for (int i = strongOVs.size(); i >  0 ; i--) {
			ovCombo =  this.getOVCombination(i , strongOVs);
			for (Collection<OrdinaryVariable> ovs : ovCombo) {
				tempParentSet = this.getParentSetByStrongOV(false,ovs);
				ignoringParentSet = new ArrayList<SSBNNode>();
				for (SSBNNode parentNode : tempParentSet) {
					if (knownNodes.contains(parentNode)) {
						ignoringParentSet.add(parentNode);
					} else {
						knownNodes.add(parentNode);
					}
				}
				tempParentSet.removeAll(ignoringParentSet);
				if (tempParentSet.size() > 0) {
					ret.put(this.getNameByDots(ovs), tempParentSet);
				}								
			}
		}
		// Start analizing those nodes w/o strong variables
		for (int i = weakOVs.length; i >  0 ; i--) {
			ovCombo =  this.getOVCombination(i, weakOVs);
			for (Collection<OrdinaryVariable> ovs : ovCombo) {
				tempParentSet = this.getParentSetByStrongOV(true,ovs);
				/*for (SSBNNode parentNode : tempParentSet) {
					if (knownNodes.contains(parentNode)) {
						tempParentSet.remove(parentNode);
					} else {
						knownNodes.add(parentNode);
					}
				}*/
				if (tempParentSet.size() > 0) {
					ret.put(this.getNameByDots(ovs), tempParentSet);
				}								
			}
		}
		return ret;
	}
	
	
	/**
	 * "Automagically" sets up a map containing SSBNs by strong ov's, considering anything but argument as strong variables.
	 * @param weakOV anything but these ovs will be considered as "strong"
	 * @return a map containing a set name (which would be strong ov names separated by dots) and parent SSBNNodes containing such ovs as arguments.
	 */
	/*public Map<String, SSBNNode> getParentMapByWeakOV(String...weakOVNames) {
		// TODO implement this if necessary
	}*/
	
	// Ordinal getters and setters
	
	
	/**
	 * Returns the name of the node -> which is the same of resident's and
	 * its current arguments (entity instances) between parentheses, with no
	 * spaces. E.g. HarmPotential(ST4,T0)
	 * @return
	 */
	public String getName() {
		String name = new String(this.resident.getName());
		name += "(";
		for (OVInstance ovi : this.getArguments()) {
			if (name.charAt(name.length() - 1) != '(') {
				name += ",";
			}
			name += ovi.getEntity().getInstanceName();
		}
		name += ")";
		if (this.getProbNode() != null) {
			this.getProbNode().setName(name);
		}
		return name;
	}

	/**
	 * If node is a finding, it will return a single value (a collection w/ only 1 value)
	 * @return the actualValues: node's possible values known at that moment. It might be
	 * different than resident node's ones. It might be a single value, when a finding is present.
	 */
	public Collection<Entity> getActualValues() {
		if (this.getProbNode() == null || this.isFinding) {
			ArrayList<Entity> ret = new ArrayList<Entity>();
			ret.add(this.actualValues.iterator().next());
			return ret;
		}
		return actualValues;
	}

	/**
	 * @param actualValues the actualValues to set
	 */
	public void setActualValues(Collection<Entity> actualValues) {
		this.actualValues = actualValues;
	}

	/**
	 * @return the arguments
	 */
	public Collection<OVInstance> getArguments() {
		return arguments;
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(List<OVInstance> arguments) {
		this.arguments = arguments;
	}

	/**
	 * @return the isUsingDefaultCPT
	 */
	public boolean isUsingDefaultCPT() {
		return resident.getMFrag().isUsingDefaultCPT();
	}

	/**
	 * Adds a note whitch determines this mode must (or not) use its default CPT distribution. It also adds a note for
	 * nodes from its MFrag.
	 * @param isUsingDefaultCPT the isUsingDefaultCPT to set
	 */
	public void setUsingDefaultCPT(boolean isUsingDefaultCPT) {
		resident.getMFrag().setAsUsingDefaultCPT(isUsingDefaultCPT);
	}

	/**
	 * @return the parents
	 */
	public Collection<SSBNNode> getParents() {
		if (this.isFinding) {
			return new ArrayList<SSBNNode>();
		}
		return parents;
	}

	/**
	 * This method sets a parent without checking structure consistency. Be careful when using this.
	 * @param parents the parents to set
	 */
	public void setParents(Collection<SSBNNode> parents) {
		this.parents = parents;
	}

	/**
	 * @return the probNode. Null if it should be a finding
	 */
	public ProbabilisticNode getProbNode() {
		if (this.probNode != null) {
			// currently, this process is redundant (because getName already sets probNode's name)...
			//this.probNode.setName(this.getName());
		}
		if (this.isFinding) {
			return null;
		}
		return probNode;
	}

	/**
	 * Setting a probNode to null is the same of declaring it as a finding (the value should be the 1st possible value declared
	 * by its resident node). The new probNode should not have parents.
	 * @param probNode the ProbabilisticNode (UnBBayes node representation) to set
	 */
	public void setProbNode(ProbabilisticNode probNode) {
		// TODO treat parents and dangling references
		if (this.probNode != null) {
			this.probabilisticNetwork.removeNode(this.probNode);
		}
		this.probNode = probNode;
		this.appendProbNodeState();
		if (this.probNode != null) {
			this.getProbabilisticNetwork().addNode(this.probNode);
		}
	}

	/**
	 * @return the DomainResidentNode this node represents
	 */
	public DomainResidentNode getResident() {
		return resident;
	}

	/**
	 * @param resident the resident to set
	 */
	protected void setResident(DomainResidentNode resident) {
		this.resident = resident;
		this.setProbNode(new ProbabilisticNode());
	}

	/**
	 * When creating names for sets of strong OVs, this string/char separates the compound names.
	 * Ex. When separator is ".", ovs = {st,z} -> name= "st.z"
	 * @return the strongOVSeparator
	 */
	public String getStrongOVSeparator() {
		return strongOVSeparator;
	}

	/**
	 * When creating names for sets of strong OVs, this string/char separates the compound names.
	 * Ex. When separator is ".", ovs = {st,z} -> name= "st.z"
	 * @param strongOVSeparator the strongOVSeparator to set
	 */
	public void setStrongOVSeparator(String strongOVSeparator) {
		this.strongOVSeparator = strongOVSeparator;
	}

	/**
	 * @return the current compiler
	 */
	public ICompiler getCompiler() {
		return compiler;
	}

	/**
	 * Sets which compiler class we should use to parse pseudocode and generate CPT
	 * @param compiler: the compiler to set
	 */
	public void setCompiler(ICompiler compiler) {
		this.compiler = compiler;
	}

	/**
	 * @return true if this node is set as a finding. False otherwise.
	 */
	public boolean isFinding() {
		return isFinding;
	}

	/**
	 * @param isFinding: true if this node is set as a finding. False otherwise. Setting this
	 * value to true will make it impossible to set multiple possible values and/or add a parent
	 */
	private void setFinding(boolean isFinding) {
		this.isFinding = isFinding;
	}

	/**
	 * @return the probabilisticNetwork
	 */
	public ProbabilisticNetwork getProbabilisticNetwork() {
		return probabilisticNetwork;
	}

	/**
	 * @param probabilisticNetwork the probabilisticNetwork to set
	 */
	public void setProbabilisticNetwork(ProbabilisticNetwork probabilisticNetwork) throws SSBNNodeGeneralException {
		if (probabilisticNetwork == null) {
			throw new SSBNNodeGeneralException(this.resource.getString("NoNetworkDefined"));
		}
		this.probabilisticNetwork = probabilisticNetwork;
		
		if (this.getProbNode() != null) {
			this.probabilisticNetwork.addNode(this.getProbNode());
		}
	}
	
	public String toString(){
		String ret = resident.getName(); 
		
		ret+="(";
		for(OVInstance instance: arguments){
			ret+= instance.toString();
		}
		ret+=")";
		
		if(isFinding){
			ret+= " [F] ";
		}
		
		return ret;  
	}

	public boolean isContext() {
		return isContext;
	}

	/**
	 * Set this node how a Context Node father. (cases where a context node 
	 * dont avaliate became a father of a SSBNNode). 
	 */
	public void setIsContext() {
		this.setProbNode(null);
		this.isContext = true;
	}
	
	
}
