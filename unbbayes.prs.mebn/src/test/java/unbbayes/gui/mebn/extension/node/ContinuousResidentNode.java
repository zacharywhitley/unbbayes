/**
 * 
 */
package unbbayes.gui.mebn.extension.node;

import java.awt.Color;
import java.util.ArrayList;
import java.util.ResourceBundle;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.prs.Node;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.RandomVariableFinding;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.compiler.Compiler;
import unbbayes.prs.mebn.entity.StateLink;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.prs.mebn.extension.IMEBNPluginNode;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class ContinuousResidentNode extends ResidentNode implements
		IMEBNPluginNode {

	private IMEBNMediator mediator;
	
	/** Load resource file from this package */
  	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
  			unbbayes.prs.mebn.resources.Resources.class.getName());  		
	
	/**
	 * If you want this constructor to be a non-public constructor, you must provide
	 */
	public ContinuousResidentNode() {
		super(); 
		setListPointers(new ArrayList<ResidentNodePointer>()); 
		setOrdinaryVariableList(new ArrayList<OrdinaryVariable>()); 
		
		setInputInstanceFromList(new ArrayList<InputNode>()); 
		setParentInputNodeList(new ArrayList<InputNode>());
		setResidentNodeFatherList(new ArrayList<ResidentNode>());	
		setResidentNodeChildList(new ArrayList<ResidentNode>());	
		setRandomVariableFindingList(new ArrayList<RandomVariableFinding>()); 
		setPossibleValueLinkList(new ArrayList<StateLink>()); 
		
		setName("Default node"); 
		updateLabel(); 		
		//by young
		setColor(new Color(254, 250, 158));
		
		
	}

	/**
	 * @param name
	 * @param mFrag
	 */
	public ContinuousResidentNode(String name, MFrag mFrag) {
		super(name, mFrag);
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
		try {
			this.setCompiler(Compiler.getInstance(this));
		}catch (Exception e) {
			Debug.println(this.getClass(), "Failed to set default compiler. Please, set another one as default compiler.", e);
		}
		if (mfrag != null) {
			// actually, we just need to do mfrag.getResidentNodeList().add(this), but I'm removing and adding again just to make sure consistency is OK		
			mfrag.removeNode(this);
			this.setName("Test node " + mfrag.getDomainResidentNodeNum());
			mfrag.addResidentNode(this);
			mfrag.getMultiEntityBayesianNetwork().getNamesUsed().add(this.getName());
			if (this.getMediator() != null) {
				this.getMediator().getMebnEditionPane().getMTheoryTree().addNode(mfrag, this);
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

	/*
	 * (non-Javadoc)
	 * @see unbbayes.prs.Node#addChild(unbbayes.prs.Node)
	 */
	public void addChild(Node node) throws InvalidParentException  {
		if (this.getClass().isAssignableFrom(node.getClass())) {
			super.addChild(node);
		} else {
			throw new InvalidParentException(this.getResource().getString("InvalidEdgeException"));
		}
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
		ContinuousResidentNode.resource = resource;
	}

}
