/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.*;
import unbbayes.prs.mebn.compiler.AbstractCompiler;
import unbbayes.prs.mebn.compiler.Compiler;
import unbbayes.prs.mebn.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
	
	
	
	private AbstractCompiler compiler = null;
	
	
	
	// Constructors
	
	
	
	private SSBNNode (DomainResidentNode resident , ProbabilisticNode probNode) {
		
		this.arguments = new ArrayList<OVInstance>();
		this.parents = new ArrayList<SSBNNode>();
		this.resident = resident;
		this.probNode = probNode;
		
		this.actualValues = new ArrayList<Entity>(resident.getPossibleValueList());
		this.setUsingDefaultCPT(false);
		
		this.setCompiler(new Compiler(resident, probNode.getPotentialTable()));
		
	}
	
	/**
	 *  This class is a temporary representation of a resident random variable instance at ssbn creation step.
	 * Basically, works as a bridge (not a design pattern) between MEBN solid resident node representation (DomainResidentNode)
	 * and the actual ProbabilisticNode on UnBBayes.
	 * @param resident: the resident node this SSBNNode represents
	 * @param probNode: this is useful when we already know which ProbabilisticNode (UnBBayes representation of a node) shall
	 * represent this node once SSBN is generated.
	 * @return a SSBNNode instance.
	 */
	public static SSBNNode getInstance (DomainResidentNode resident , ProbabilisticNode probNode)  {
		return new SSBNNode(resident,probNode);
	}
	
	/**
	 *  This class is a temporary representation of a resident random variable instance at ssbn creation step.
	 * Basically, works as a bridge (not a design pattern) between MEBN solid resident node representation (DomainResidentNode)
	 * and the actual ProbabilisticNode on UnBBayes. This is, for now, identical to getInstance(resident,null)
	 * @param resident: the resident node this SSBNNode represents
	 * @return a SSBNNode instance.
	 * 
	 */
	public static SSBNNode getInstance (DomainResidentNode resident)  {
		return new SSBNNode(resident,null);
	}
	
	
	
	// private methods
	
	private String getNameByDots(String...names) {
		String dotName = new String(names[0]);
		for (int i = 1; i < names.length; i++) {
			dotName.concat("." + names[i]);
		}
		return dotName;
	}
	
	private String getNameByDots(Collection<OrdinaryVariable> ovs) {
		OrdinaryVariable[] ovArray = (OrdinaryVariable[])ovs.toArray();
		String dotName = new String(ovArray[0].getName());
		for (int i = 1; i < ovArray.length; i++) {
			dotName.concat("." + ovArray[i].getName());
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
	
	private Collection<Collection<OrdinaryVariable>> getOVCombination(int byHowMany, OrdinaryVariable... ovs ){
		
				
		Collection<Collection<OrdinaryVariable>> ret = new ArrayList<Collection<OrdinaryVariable>>();
		if (byHowMany <= 0) {
			return ret;
		}
		
		OrdinaryVariable[] ovArray = (OrdinaryVariable[])ovs;
		int[] indexes = new int[byHowMany];
		
		for (int i = 0; i < indexes.length; i++) {
			indexes[i] = i;
		}
		
		Collection<OrdinaryVariable> tempOVs = null;
		while (indexes[0] <= ovArray.length - indexes.length)
		for (int i = 0; i < ovArray.length - byHowMany; i++) {
			
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
	 */
	public boolean hasAllOVs(Collection<OrdinaryVariable> ovs) {
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
	 */
	public boolean hasAllOVs(OrdinaryVariable... ovs) {
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
		for (int i = 0; i < ovs.length; i++) {
			if (!this.hasOV(ovs[i],isOVName)) {
				return false;
			}
		}
		return true;
	}
	
	
	
	
	
	/**
	 * Adds an argument
	 * @param ov: ordinal variable associated to this argument
	 * @param entityInstanceName: name of a OV instance (ex. !ST4, !Z0, !T1 ...)
	 */
	public void addArgument(OrdinaryVariable ov, String entityInstanceName) throws SSBNNodeGeneralException {
		this.arguments.add(OVInstance.getInstance( ov, entityInstanceName , ov.getValueType() ));
	}
	
	
	/**
	 * Adds an argument at a particular position
	 * @param ov: ordinal variable associated to this argument
	 * @param entityInstanceName: name of a OV instance (ex. !ST4, !Z0, !T1 ...)
	 * @param pos: position (when argument order is important) to add this argument
	 */
	public void addArgument(OrdinaryVariable ov, String entityInstanceName,  int pos) throws SSBNNodeGeneralException {
		this.arguments.add(pos, OVInstance.getInstance( ov, entityInstanceName , ov.getValueType() ));
		
	}
	
	
	
	
	
	
	
	
	/**
	 * This is the same as setting node's actual value as a unique value.
	 * @param uniqueValue: the unique value this node represents
	 */
	public void setNodeAsFinding(Entity uniqueValue) {
		Collection actualValue = new ArrayList<Entity>();
		actualValue.add(uniqueValue);
		this.setActualValues(actualValue);
	}
	
	
	
	public void fillProbabilisticTable() {
		this.compiler.generateCPT();
	}
	
	
	
	// Parent controller
	
	public void addParent(SSBNNode parent) {
		this.getParents().add(parent);		
	}
	
	
	public void removeParent(SSBNNode parent) {
		this.getParents().remove(parent);
	}
	
	
	public void removeParentByName(String name) {
		Collection<SSBNNode> parents = this.getParents();
		for (SSBNNode node : parents) {
			if (node.getName().compareToIgnoreCase(name) == 0) {
				parents.remove(node);
			}
		}
	}
	
	
	/**
	 * 
	 * @param ovNames: names of the strong ovs
	 * @return a collection of SSBNNode which contains ONLY the ovs passed by the arguments
	 */
	public Collection<SSBNNode> getParentSetByStrongOV(String...ovNames ) {
		Collection<SSBNNode> parents = new HashSet();
		for (SSBNNode parent : this.parents) {
			if (parent.hasAllOVs(true, ovNames)) {
				parents.add(parent);
			}
		}
		return parents;
	}
	
	
	/**
	 * 
	 * @param setOfOV: set of strong ovs
	 * @return a collection of SSBNNode which contains ONLY the ovs passed by the arguments
	 */
	public Collection<SSBNNode> getParentSetByStrongOV(Collection<OrdinaryVariable> setOfOV) {
		Collection<SSBNNode> parents = new HashSet();
		for (SSBNNode parent : this.parents) {
			if (parent.hasAllOVs(setOfOV)) {
				parents.add(parent);
			}
		}
		return parents;
	}
	
	/**
	 * 
	 * @param setOfOV: set of strong ovs
	 * @return a collection of SSBNNode which contains ONLY the ovs passed by the arguments
	 */
	public Collection<SSBNNode> getParentSetByStrongOV(OrdinaryVariable... setOfOV) {
		Collection<SSBNNode> parents = new HashSet();
		for (SSBNNode parent : this.parents) {
			if (parent.hasAllOVs(setOfOV)) {
				parents.add(parent);
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
		
		List<SSBNNode> knownNodes = new ArrayList<SSBNNode>();
		
		// Disconsider weak ovs
		Collection<OrdinaryVariable> strongOVs = new ArrayList<OrdinaryVariable>(this.getAllParentsOV());
		for (int i = 0; i < weakOVs.length; i++) {
			strongOVs.remove(weakOVs[i]);
		}
		
		// start collecting parents, first the ones w/ more arguments
		Collection<Collection<OrdinaryVariable>>  ovCombo = null;
		Collection<SSBNNode> tempParentSet = null;
		for (int i = strongOVs.size() - 1; i >=  0 ; i--) {
			ovCombo =  this.getOVCombination(i , (OrdinaryVariable[])strongOVs.toArray());
			for (Collection<OrdinaryVariable> ovs : ovCombo) {
				tempParentSet = this.getParentSetByStrongOV(ovs);
				for (SSBNNode parentNode : tempParentSet) {
					if (knownNodes.contains(parentNode)) {
						tempParentSet.remove(parentNode);
					} else {
						knownNodes.add(parentNode);
					}
				}
				if (tempParentSet.size() > 0) {
					ret.put(this.getNameByDots(ovs), tempParentSet);
				}								
			}
		}
		// Start analizing those nodes w/o strong variables
		for (int i = weakOVs.length - 1; i >=  0 ; i--) {
			ovCombo =  this.getOVCombination(i, weakOVs);
			for (Collection<OrdinaryVariable> ovs : ovCombo) {
				tempParentSet = this.getParentSetByStrongOV(ovs);
				for (SSBNNode parentNode : tempParentSet) {
					if (knownNodes.contains(parentNode)) {
						tempParentSet.remove(parentNode);
					} else {
						knownNodes.add(parentNode);
					}
				}
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
	 * Returns the name of the node -> which is the same of resident's.
	 * @return
	 */
	public String getName() {
		return this.resident.getName();
	}

	/**
	 * @return the actualValues: node's possible values known at that moment. It might be
	 * different than resident node's ones. It might be a single value, when a finding is present.
	 */
	public Collection<Entity> getActualValues() {
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
		return parents;
	}

	/**
	 * @param parents the parents to set
	 */
	public void setParents(Collection<SSBNNode> parents) {
		this.parents = parents;
	}

	/**
	 * @return the probNode
	 */
	public ProbabilisticNode getProbNode() {
		return probNode;
	}

	/**
	 * @param probNode the ProbabilisticNode (UnBBayes node representation) to set
	 */
	public void setProbNode(ProbabilisticNode probNode) {
		this.probNode = probNode;
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
	public void setResident(DomainResidentNode resident) {
		this.resident = resident;
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
	public AbstractCompiler getCompiler() {
		return compiler;
	}

	/**
	 * Sets which compiler class we should use to parse pseudocode and generate CPT
	 * @param compiler: the compiler to set
	 */
	public void setCompiler(AbstractCompiler compiler) {
		this.compiler = compiler;
	}
	
	
	
	
	
	
	
	
	
}
