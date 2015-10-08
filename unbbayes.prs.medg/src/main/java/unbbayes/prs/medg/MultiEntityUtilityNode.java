/**
 * 
 */
package unbbayes.prs.medg;

import java.awt.Color;
import java.util.ArrayList;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.id.UtilityNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.entity.CategoricalStateEntity;
import unbbayes.prs.mebn.entity.Entity;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.entity.exception.CategoricalStateDoesNotExistException;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.prs.mebn.extension.IMEBNPluginNode;
import unbbayes.prs.medg.compiler.MultiEntityUtilityFunctionCompiler;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class MultiEntityUtilityNode extends ResidentNode implements IMEBNPluginNode, IMEDGNode {

	private static final long serialVersionUID = 6058582672771453884L;
	
	/** The only possible state for this node is "utility", which is the value of this string. */
	public static final String UTILITY_STATE = "utility";
	
	public static final Color DEFAULT_COLOR = new UtilityNode().getColor();

	private IMEBNMediator mediator;

	private String nodeNamePrefix = "Utility";
	
	/**
	 * If you want this constructor to be a non-public constructor, you must provide a builder/factory class to the 
	 * plugin.xml
	 */
	public MultiEntityUtilityNode() {
		this("Utility", null);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param name
	 * @param mFrag
	 */
	public MultiEntityUtilityNode(String name, MFrag mFrag) {
		super();
		setListPointers(new ArrayList<ResidentNodePointer>()); 
		setOrdinaryVariableList(new ArrayList<OrdinaryVariable>()); 
		
		setInputInstanceFromList(new ArrayList<InputNode>()); 
		setParentInputNodeList(new ArrayList<InputNode>());
		setResidentNodeFatherList(new ArrayList<ResidentNode>());	
		setResidentNodeChildList(new ArrayList<ResidentNode>());	
		setRandomVariableFindingList(new ArrayList<RandomVariableFinding>()); 
		setPossibleValueLinkList(new ArrayList<StateLink>()); 
		setMFrag(mFrag);
		setName(name); 
		updateLabel(); 		 
		setColor(DEFAULT_COLOR); 
		
		this.setTypeOfStates(CATEGORY_RV_STATES);
		
		
		// set default compiler as the utility lpd compiler
		try {
			this.setCompiler(MultiEntityUtilityFunctionCompiler.newInstance(this));
		}catch (Exception e) {
			Debug.println(this.getClass(), e.getMessage(), e);
		}
	}

	

	/* (non-Javadoc)
	 * @see unbbayes.prs.extension.IPluginNode#getNode()
	 */
	public Node getNode() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.extension.IMEBNPluginNode#setMediator(unbbayes.controller.mebn.IMEBNMediator)
	 */
	public void setMediator(IMEBNMediator mediator) {
		this.mediator = mediator;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.mebn.extension.IMEBNPluginNode#onAddToMFrag(unbbayes.prs.mebn.MFrag)
	 */
	public void onAddToMFrag(MFrag mfrag) throws MFragDoesNotExistException {
		
		// now, the resident node stores what kind of compiler the user has chosen to compile CPT
		this.setMFrag(mfrag); 
		
		if (mfrag != null) {
			// actually, we just need to do mfrag.getResidentNodeList().add(this), but I'm removing and adding again just to make sure consistency is OK		
			mfrag.removeNode(this);
			this.setName(getNodeNamePrefix() + mfrag.getDomainResidentNodeNum());
			mfrag.addResidentNode(this);
			mfrag.getMultiEntityBayesianNetwork().getNamesUsed().add(this.getName());
			if (this.getMediator() != null) {
				this.getMediator().getMebnEditionPane().getMTheoryTree().addNode(mfrag, this);
			}
			
			// make sure this node has the "utility" state
			if (mfrag.getMultiEntityBayesianNetwork() != null  
					&& mfrag.getMultiEntityBayesianNetwork().getCategoricalStatesEntityContainer() != null
					&& this.getPossibleValueByName(UTILITY_STATE) == null) {
				// the only possible value of this node should be "utility"
				CategoricalStateEntity utilityState = null;
				try {
					utilityState = mfrag.getMultiEntityBayesianNetwork().getCategoricalStatesEntityContainer().getCategoricalState(UTILITY_STATE);
				} catch (CategoricalStateDoesNotExistException e) {
					Debug.println(getClass(), e.getMessage(),e);
					utilityState = null;
				}
				if (utilityState == null) {
					// create a new utility state if it was not set yet
					utilityState = mfrag.getMultiEntityBayesianNetwork().getCategoricalStatesEntityContainer().createCategoricalEntity(UTILITY_STATE);
				}
				if (utilityState == null) {
					throw new NullPointerException("Unable to create a categorical state entity called " + UTILITY_STATE + " in " + mfrag.getMultiEntityBayesianNetwork());
				}
				this.addPossibleValue(utilityState);
			}
			
		} else {
			throw new MFragDoesNotExistException();
		}
		
	}
	
	

	/**
	 * @return the mediator
	 */
	public IMEBNMediator getMediator() {
		return mediator;
	}



	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addParent(unbbayes.prs.Node)
	 */
	public void addParent(Node parent) throws InvalidParentException {
		if (parent instanceof MultiEntityUtilityNode) {
			throw new InvalidParentException("Utility nodes cannot be parents: " + parent + " -> " + this);
		} 
		super.addParent(parent);
		if (parent instanceof ResidentNode) {
			ResidentNode residentNode = (ResidentNode) parent;
			residentNode.addResidentNodeChild(this); // this will automatically add parent to this.getResidentNodeFatherList()
		} else if (parent instanceof InputNode) {
			InputNode inputNode = (InputNode) parent;
			inputNode.addResidentNodeChild(this);
		} else {
			try {
				Debug.println(getClass(), parent + " is not a " + ResidentNode.class.getName() + " or " + InputNode.class.getName());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ResidentNode#addPossibleValueLink(unbbayes.prs.mebn.entity.Entity)
	 */
	public StateLink addPossibleValueLink(Entity possibleValue) {
		StateLink foundObj = this.getStateLinkByEntity(possibleValue);
		// avoid duplicate
		if (foundObj == null) {
			return super.addPossibleValueLink(possibleValue);
		}
		return foundObj;
	}
	
	/**
	 * This method searches {@link #getPossibleValueLinkList()}
	 * in order to find a {@link StateLink} containing the an entity.
	 * @param entity : if null, it will return null.
	 * @return : if null, it was not found
	 */
	public StateLink getStateLinkByEntity(Entity entity) {
		if (entity == null) {
			return null;
		}
		for(StateLink value : getPossibleValueLinkList()){
			if (entity.equals(value.getState())){
				return value; 
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addChild(unbbayes.prs.Node)
	 */
	public void addChild(Node child) throws InvalidParentException {
		this.addChildNode(child);
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addChildNode(unbbayes.prs.INode)
	 */
	public void addChildNode(INode child) throws InvalidParentException {
		throw new InvalidParentException("Utility nodes cannot be parents: " + this + " -> " + child);
	}

	/**
	 * @return the nodeNamePrefix
	 */
	public String getNodeNamePrefix() {
		return nodeNamePrefix;
	}

	/**
	 * @param nodeNamePrefix the nodeNamePrefix to set
	 */
	public void setNodeNamePrefix(String nodeNamePrefix) {
		this.nodeNamePrefix = nodeNamePrefix;
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.medg.IMEDGNode#asResidentNode()
	 */
	public ResidentNode asResidentNode() {
		return this;
	}


}
