/**
 * 
 */
package unbbayes.prs.mebn;

import java.awt.Color;
import java.util.ResourceBundle;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.Node;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.compiler.IdentityNodeCompiler;
import unbbayes.prs.mebn.entity.ObjectEntity;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;
import unbbayes.prs.mebn.extension.IMEBNPluginNode;

/**
 * @author Shou Matsumoto
 *
 */
public class IdentityNode extends ResidentNode implements IMEBNPluginNode {


	public static final Color IDENTITY_NODE_COLOR = new Color(220, 220, 220);




	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.mebn.resources.Resources.class.getName());  		
	
	
	
	private IMEBNMediator mediator;

	
	public IdentityNode() {
		this("Id", null);
	}

	/**
	 * @param name
	 * @param mFrag
	 */
	public IdentityNode(String name, MFrag mFrag) {
		super(name, mFrag);
		this.setColor(IDENTITY_NODE_COLOR);
		this.setCompiler(IdentityNodeCompiler.getInstance(this));
	}

	/*
	 * (non-Javadoc)
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
	
	/**
	 * @return the mediator
	 */
	public IMEBNMediator getMediator() {
		return this.mediator;
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
			this.setName("Id" + mfrag.getDomainResidentNodeNum());
			mfrag.addResidentNode(this);
			mfrag.getMultiEntityBayesianNetwork().getNamesUsed().add(this.getName());
			if (this.getMediator() != null) {
				this.getMediator().getMebnEditionPane().getMTheoryTree().addNode(mfrag, this);
			}
		} else {
			throw new MFragDoesNotExistException();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.Node#addChild(unbbayes.prs.Node)
	 */
	public void addChild(Node node) throws InvalidParentException  {
		super.addChild(node);
		if (node instanceof ResidentNode) {
			ResidentNode residentNode = (ResidentNode) node;
			this.addResidentNodeChild(residentNode); // this will automatically add parent to getResidentNodeFatherList()
		} else {
			throw new InvalidParentException(this.getResource().getString("InvalidEdgeException"));
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addParent(unbbayes.prs.Node)
	 */
	public void addParent(Node parent) throws InvalidParentException {
		// identity nodes should not have parents
		throw new InvalidParentException(this.getResource().getString("InvalidEdgeException"));
	}
	
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ResidentNode#addArgument(unbbayes.prs.mebn.OrdinaryVariable, boolean)
	 */
	public void addArgument(OrdinaryVariable ov, boolean addArgument) throws ArgumentNodeAlreadySetException, OVariableAlreadyExistsInArgumentList {
		// only allow  1 argument
		if (getOrdinaryVariableList().size() > 0) {
			throw new ArgumentNodeAlreadySetException();
		}
		
		// extract the entity associated with OV
		ObjectEntity entity = null;
		try {
			entity = getMediator().getMultiEntityBayesianNetwork().getObjectEntityContainer().getObjectEntityByType(ov.getValueType());
		} catch (Exception e) {
			throw new IllegalArgumentException(this.getResource().getString("CouldNotExtractTypeEntity"), e);
		}
		
		// force possible value to be the same of argument
		this.removeAllPossibleValues();
		if(!this.hasPossibleValue(entity)){
			this.addPossibleValueLink(entity);
			entity.addNodeToListIsPossibleValueOf(this);	
		} 
		this.setTypeOfStates(IResidentNode.OBJECT_ENTITY);
		
		// finally, add argument
		super.addArgument(ov, addArgument);
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ResidentNode#addInputInstanceFromList(unbbayes.prs.mebn.InputNode)
	 */
	protected void addInputInstanceFromList(InputNode instance) {
		throw new UnsupportedOperationException(getResource().getString("IdentityNodeInputInstanceError"));
	}

	/**
	 * @return the resource
	 */
	public static ResourceBundle getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public static void setResource(ResourceBundle resource) {
		IdentityNode.resource = resource;
	}

}
