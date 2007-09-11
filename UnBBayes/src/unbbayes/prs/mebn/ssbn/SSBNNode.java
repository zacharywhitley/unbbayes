/**
 * 
 */
package unbbayes.prs.mebn.ssbn;

import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.mebn.DomainResidentNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ssbn.exception.*;
import unbbayes.prs.mebn.entity.Entity;

import java.util.ArrayList;
import java.util.List;


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
	private class EntityInstanceContainer {
		
		private String entityInstanceName = null;
		private OrdinaryVariable ov = null;
				
		private EntityInstanceContainer (String entityInstanceName , OrdinaryVariable ov) {
			this.entityInstanceName = entityInstanceName;
			this.ov = ov;
		}
	}
	
	// Private Attributes
	
	private DomainResidentNode resident = null;	// what resident node this instance represents
	private ProbabilisticNode  probNode = null;	// stores the UnBBayes BN ordinal node which represents this SSBNNode
	
	private List<EntityInstanceContainer> arguments = null;
	private List<SSBNNode> parents = null;
	
	private boolean isUsingDefaultCPT = false;	// checks if this node should use defaultCPT
	
	private List<Entity> possibleValues = null;	// this is useful when this node must provide some values different than the resident nodes' ones
	
	private String name = null;					// TODO treat this
	
	
	// Constructors
	
	
	
	private SSBNNode (DomainResidentNode resident , ProbabilisticNode probNode) {
		
		this.arguments = new ArrayList<EntityInstanceContainer>();
		this.parents = new ArrayList<SSBNNode>();
		this.resident = resident;
		this.probNode = probNode;
		
		this.possibleValues = new ArrayList<Entity>(resident.getPossibleValueList());
		
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
	
	
	// exported methods
	
	
	
	public void addArgument(String entityInstanceName, OrdinaryVariable ov) throws SSBNNodeGeneralException {
		this.arguments.add(new EntityInstanceContainer(entityInstanceName,ov));
	}
	
	
	
	public void addArgument(String entityInstanceName, OrdinaryVariable ov , int index) throws SSBNNodeGeneralException {
		this.arguments.add(index,new EntityInstanceContainer(entityInstanceName,ov));
		
	}
	
	
	
	// TODO variants of addArgument, removeArgument and getArgument, including add/remove by collection
	
	
	
	public void setNodeAsFinding(Entity uniqueValue) {
		
	}
	
	
	// TODO ordinal getters and setters
	
	
	
	
	
	
}
