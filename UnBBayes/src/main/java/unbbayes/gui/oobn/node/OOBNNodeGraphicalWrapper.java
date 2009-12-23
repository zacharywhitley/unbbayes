/**
 * 
 */
package unbbayes.gui.oobn.node;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import unbbayes.prs.Node;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.PotentialTable;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.util.Debug;

/**
 * This class represents graphically an OOBN node.
 * It is responsible to actually draw a node in a panel.
 * 
 * @author Shou Matsumoto
 *
 */
public class OOBNNodeGraphicalWrapper extends ProbabilisticNode {
	
	//by young
	private int width = (int)Node.getDefaultSize().getX();
	private int height = (int)Node.getDefaultSize().getY();
	
	
	// this number is used to ajust instance node's size
	public static final float INSTANCE_SIZE_MULTIPLIER = 1.5f;
	
	// this number is used to ajust instance node's size
	private static final int RAWS_NUM = 3;
	
	// the OOBN node that this graphic wraps
	private IOOBNNode wrappedNode = null;
	
	// if this node type is assumed to be immutable
	private boolean isImmutableNode = false;
	
	// set of inner nodes (if this node is an instance node, it contains other nodes)
	private Set<OOBNNodeGraphicalWrapper> innerNodes = null;
	
	// OBS. the output node is also a ordinal description node, so we do not set colors at this class
	
	private Color inputColor = Color.LIGHT_GRAY;
	private Color privateColor = Color.WHITE;
	private Color instanceColor = privateColor;
	private Color instanceInputColor = new Color(200,200,200);
	private Color instanceOutputColor = instanceColor;
	
	
//	// since the probability table of superclass is not visible (and hard to set), we start managing locally
//	private ProbabilisticTable potentialTable = null;

	/**
	 * Visually represents an OOBN node.
	 * It should wrap an OOBNNode
	 * @param wrappedNode: wrapped oobn node
	 */
	protected OOBNNodeGraphicalWrapper(IOOBNNode wrappedNode) {
		// TODO Auto-generated constructor stub
		super();
		this.setWrappedNode(wrappedNode);
		
		
	}

	/**
	 * Creates a new instance of a graphic representation of an OOBN node.
	 * We should do it by wrapping an existing IOOBNNode
	 * 
	 * Note: if the wrapped Node's type is instance type, the inner nodes
	 * (input nodes and output nodes from external oobn class)
	 * will be automatically created, but not inserted to a particular network
	 * 
	 * @param wrappedNode
	 * @return a new instance of graphical node representation
	 * @see IOOBNNode
	 */
	public static OOBNNodeGraphicalWrapper newInstance(IOOBNNode wrappedNode) {
		OOBNNodeGraphicalWrapper ret =  new OOBNNodeGraphicalWrapper(wrappedNode);
		ret.setOutputColor(ret.getDescriptionColor());
		
		ret.setInnerNodes(new HashSet<OOBNNodeGraphicalWrapper>());
		
		ret.initMarginalList();
		
//		ret.potentialTable = new ProbabilisticTable();
		
		
		// if node type is instance node, then we must fill its inner nodes (external input/output nodes)
	 	ret.setUpInnerNodesGraphically();	// this should fill the inner node set as well
		
		return ret;
	}
		
	public void setInstanceNodePositionAndSize(int x, int y)
	{
		if ((this.getWrappedNode().getType() & IOOBNNode.TYPE_INSTANCE) == 0) {
			// we should not set up a node as an instance node if it is not so
			return;
		}
		
		// calculate size, proportional of number of nodes
		int horizontalNodeNumber = Math.max(this.getWrappedNode().getParentClass().getInputNodes().size()
									      , this.getWrappedNode().getParentClass().getOutputNodes().size());
		
		// do not allow the horizontal size to be zero
		if (horizontalNodeNumber <= 0) {
			horizontalNodeNumber = 1;
		}
		
		
		Point2D.Double size = new Point2D.Double((int)Node.getDefaultSize().getX() * horizontalNodeNumber * this.INSTANCE_SIZE_MULTIPLIER
											   , (int)Node.getDefaultSize().getY() * RAWS_NUM * this.INSTANCE_SIZE_MULTIPLIER);
		
		//by young
		this.setPosition(x-(int)size.getX()/2+(int)Node.getDefaultSize().getX()/2, y-(int)size.getY()/2);
 		this.setSize(size.getX(), size.getY());
 		 		
 		System.out.println("setInstanceNodePositionAndSize " + getPosition().x + " " + getPosition().y );
 		
		// if node type is instance node, then we must fill its inner nodes (external input/output nodes)
		this.setUpInnerNodesGraphically();	// this should fill the inner node set as well
		
		try {
			this.setSizeVariable(size.getX(), size.getY());
			this.setWidth((int)size.getX());
			this.setHeight((int)size.getY());
		} catch (Exception e) {
			Debug.println(this.getClass(), "Could not set width and height", e);
		}	
	}
	
	
	/**
	 * Initializes the drawer which draws a instance node
	 */
	protected void setUpInstanceDrawer() {
		
		if ((this.getWrappedNode().getType() & IOOBNNode.TYPE_INSTANCE) == 0) {
			// we should not set up a node as an instance node if it is not so
			return;
		}
		
		// calculate size, proportional of number of nodes
		int horizontalNodeNumber = Math.max(this.getWrappedNode().getParentClass().getInputNodes().size()
									      , this.getWrappedNode().getParentClass().getOutputNodes().size());
		
		// do not allow the horizontal size to be zero
		if (horizontalNodeNumber <= 0) {
			horizontalNodeNumber = 1;
		}
		
		
		Point2D.Double size = new Point2D.Double((int)Node.getDefaultSize().getX() * horizontalNodeNumber * this.INSTANCE_SIZE_MULTIPLIER
											   , (int)Node.getDefaultSize().getY() * RAWS_NUM * this.INSTANCE_SIZE_MULTIPLIER);
		
 
		//by young 
 		this.setSize(size.getX(), size.getY());
 		
		try {
			this.setSizeVariable(size.getX(), size.getY());
			this.setWidth((int)size.getX());
			this.setHeight((int)size.getY());
		} catch (Exception e) {
			Debug.println(this.getClass(), "Could not set width and height", e);
		}
	}
	
	

	/**
	 * @return the wrappedNode
	 */
	public IOOBNNode getWrappedNode() {
		return wrappedNode;
	}

	/**
	 * @param wrappedNode the wrappedNode to set
	 */
	public void setWrappedNode(IOOBNNode wrappedNode) {
		this.wrappedNode = wrappedNode;
	}

	/**
	 * @return the inputColor
	 */
	public Color getInputColor() {
		return inputColor;
	}

	/**
	 * @param inputColor the inputColor to set
	 */
	public void setInputColor(Color inputColor) {
		this.inputColor = inputColor;
	}

	/**
	 * @return the outputColor
	 */
	public Color getOutputColor() {
		return super.getDescriptionColor();
	}

	/**
	 * @param outputColor the outputColor to set
	 */
	public void setOutputColor(Color outputColor) {
		super.setDescriptionColor(outputColor.getRGB());
	}

	/**
	 * @return the privateColor
	 */
	public Color getPrivateColor() {
		return privateColor;
	}

	/**
	 * @param privateColor the privateColor to set
	 */
	public void setPrivateColor(Color privateColor) {
		this.privateColor = privateColor;
	}

	/**
	 * @return the instanceColor
	 */
	public Color getInstanceColor() {
		return instanceColor;
	}

	/**
	 * @param instanceColor the instanceColor to set
	 */
	public void setInstanceColor(Color instanceColor) {
		this.instanceColor = instanceColor;
	}

	/**
	 * if this node's visual should not be changed anymore
	 * Usually, if a node is set as instance, it should not be changed anymore
	 * @return the isImmutableNode
	 */
	public boolean isImmutableNode() {
		return isImmutableNode;
	}

	/**
	 * if this node's visual should not be changed anymore
	 * Usually, if a node is set as instance, it should not be changed anymore
	 * @param isImmutableNode the isImmutableNode to set
	 */
	public void setImmutableNode(boolean isImmutableNode) {
		this.isImmutableNode = isImmutableNode;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#getName()
	 */
	@Override
	public String getName() {
		try {
			return this.getWrappedNode().getName();
		} catch (Exception e) {
			return super.getName();
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setName(java.lang.String)
	 */
	@Override
	public void setName(String name) {
		try {
			this.getWrappedNode().setName(name);
			super.setName(this.getWrappedNode().getName());
			this.setLabel(this.getName());
		} catch (Exception e) {
			super.setName(name);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getName();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.bn.ProbabilisticNode#appendState(java.lang.String)
	 */
	@Override
	public void appendState(String state) {
		super.appendState(state);
		try{
			this.getWrappedNode().getStateNames().add(state);
		} catch (Exception e) {
			Debug.println(this.getClass(), "Failure on append state", e);
		}
	}

//	/* (non-Javadoc)
//	 * @see unbbayes.prs.bn.ProbabilisticNode#removeLastState()
//	 */
//	@Override
//	public void removeLastState() {
//		// TODO Auto-generated method stub
//		super.removeLastState();
//	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#removeStateAt(int)
	 */
	@Override
	public void removeStateAt(int index) {
		super.removeStateAt(index);
		
		try{
			this.getWrappedNode().getStateNames().remove(index);
		} catch (Exception e) {
			Debug.println(this.getClass(), "Failure removeStateAt", e);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setStateAt(java.lang.String, int)
	 */
	@Override
	public void setStateAt(String state, int index) {
		super.setStateAt(state, index);
		
		try{
			this.getWrappedNode().getStateNames().set(index, state);
		} catch (Exception e) {
			Debug.println(this.getClass(), "Failure setStateAt", e);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setStates(java.util.List)
	 */
	@Override
	public void setStates(List<String> states) {
		super.setStates(states);
		
		try{
			this.getWrappedNode().setStateNames(states);
		} catch (Exception e) {
			Debug.println(this.getClass(), "Failure setStates", e);
		}
	}

	/**
	 * @return the instanceInputColor
	 */
	public Color getInstanceInputColor() {
		return instanceInputColor;
	}

	/**
	 * @param instanceInputColor the instanceInputColor to set
	 */
	public void setInstanceInputColor(Color instanceInputColor) {
		this.instanceInputColor = instanceInputColor;
	}

	/**
	 * @return the instanceOutputColor
	 */
	public Color getInstanceOutputColor() {
		return instanceOutputColor;
	}

	/**
	 * @param instanceOutputColor the instanceOutputColor to set
	 */
	public void setInstanceOutputColor(Color instanceOutputColor) {
		this.instanceOutputColor = instanceOutputColor;
	}

	

	/**
	 * @return the width
	 */
	public int getThisWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getThisHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	
	
	
	/**
	 * Treats the inner nodes graphically by rendering each of them as probabilistic nodes.
	 * Only instance nodes should have inner nodes (this method asserts it)
	 * The JComponent should take care of how they are going to be treated by events.
	 * The wrapped IOOBNNode should be already filled with reasonable data.
	 * Of course, the wrapped IOOBNNode must be instance node.
	 * @see IOOBNNode
	 */
	protected void setUpInnerNodesGraphically() {
		
		// assert only instance nodes are being set up by this method
		if ( ( this.getWrappedNode().getType() & this.getWrappedNode().TYPE_INSTANCE ) == 0 ) {
			return;
		}
		
		//by young
		setUpInstanceDrawer();
		
		// assert that this node's description is not the default description (like C1, C2...)
		this.setDescription(this.getName());
		
		// assert that this node (instance node) has a position already defined
		if (this.getPosition() == null) {
			throw new IllegalArgumentException(this.getName() + ": position == null");
		}
		
		// input nodes should be inserted slightly above the instance node's center
		double inputNodeY = (int)Node.getDefaultSize().getY();
		//double inputNodeY = this.getPosition().getY() - (int)Node.getDefaultSize().getY();
		
		// output nodes should be inserted slightly below the instance node's center
		double outputNodeY = 3*((int)Node.getDefaultSize().getY() );
		//double outputNodeY = this.getPosition().getY() + ((int)Node.getDefaultSize().getY() );
		
		// obtain input nodes
		Set<IOOBNNode> inputNodes = null;
		try {
			inputNodes = this.getWrappedNode().getParentClass().getInputNodes();
		} catch (Exception e) {
			Debug.println(this.getClass(), "Could not retrieve input nodes of " + this.getWrappedNode().getParentClass(), e);
		}
		
		// adjust x position of starting node
		// if number of nodes is even, slide a half of node's size to right and slide all to left
		double nodeX = (int)this.getWidth()/2 - (int)Node.getDefaultSize().getX()/2 + ((inputNodes.size() % 2 == 0)?((int)Node.getDefaultSize().getX()* OOBNNodeGraphicalWrapper.INSTANCE_SIZE_MULTIPLIER/2):0) - ((inputNodes.size() / 2) * (int)Node.getDefaultSize().getX() * OOBNNodeGraphicalWrapper.INSTANCE_SIZE_MULTIPLIER);
		//double nodeX = this.getPosition().getX() + ((inputNodes.size() % 2 == 0)?((int)Node.getDefaultSize().getX()* OOBNNodeGraphicalWrapper.INSTANCE_SIZE_MULTIPLIER/2):0) - ((inputNodes.size() / 2) * (int)Node.getDefaultSize().getX() * OOBNNodeGraphicalWrapper.INSTANCE_SIZE_MULTIPLIER);
		
		try {
			// add instance's input inner nodes to set
			for (IOOBNNode input : inputNodes) {
				this.getInnerNodes().add( this.insertInnerNode(
								this.getWrappedNode(), 
								input, 
								input.TYPE_INSTANCE_INPUT, 
								nodeX, 
								inputNodeY )
				);
				
				System.out.println("RESIZED " + input.getName() + " "+ nodeX + " " + inputNodeY);
				
				nodeX += (int)Node.getDefaultSize().getX() * OOBNNodeGraphicalWrapper.INSTANCE_SIZE_MULTIPLIER;				
			}
		} catch (Exception e) {
			Debug.println(this.getClass(), "Error filling Input nodes", e);
		}
		
		// obtain output nodes
		Set<IOOBNNode> outputNodes = null;
		try {
			outputNodes = this.getWrappedNode().getParentClass().getOutputNodes();
		} catch (Exception e) {
			Debug.println(this.getClass(), "Could not retrieve output nodes of " + this.getWrappedNode().getParentClass(), e);
		}
		
		// adjust x position of starting node
		// if number of nodes is even, slide a half of node's size to right and slide all to left
		nodeX = (int)this.getWidth()/2 - (int)Node.getDefaultSize().getX()/2 + ((outputNodes.size() % 2 == 0)?((int)Node.getDefaultSize().getX()* OOBNNodeGraphicalWrapper.INSTANCE_SIZE_MULTIPLIER/2):0) - ((outputNodes.size() / 2) * (int)Node.getDefaultSize().getX() * OOBNNodeGraphicalWrapper.INSTANCE_SIZE_MULTIPLIER);
		//nodeX = this.getPosition().getX() + ((outputNodes.size() % 2 == 0)?((int)Node.getDefaultSize().getX()* OOBNNodeGraphicalWrapper.INSTANCE_SIZE_MULTIPLIER/2):0) - ((outputNodes.size() / 2) * (int)Node.getDefaultSize().getX() * OOBNNodeGraphicalWrapper.INSTANCE_SIZE_MULTIPLIER);
		
		// add instance's output inner nodes to set
		for (IOOBNNode output : outputNodes) {
			this.getInnerNodes().add( this.insertInnerNode(
					this.getWrappedNode(), 
					output, 
					output.TYPE_INSTANCE_OUTPUT, 
					nodeX, 
					outputNodeY)
					
					
			);
			
			System.out.println("RESIZED " + output.getName() + " "+ nodeX + " " + outputNodeY);
			
			nodeX += (int)Node.getDefaultSize().getX()* OOBNNodeGraphicalWrapper.INSTANCE_SIZE_MULTIPLIER;
		}
		
		Debug.println(this.getClass(), "Instance node " + this.getName() + "'s inner nodes are fully populated now.");
		 
	}
	
	
	/**
	 * Insert a new Inner Node
	 * @param upperNode: node containing this new inner node
	 * @param oobnNode: the original node which will be referenced by this new instance's inner node
	 * @param type: set the type of the new node as this type
	 * @param x
	 * @param y
	 */
	private OOBNNodeGraphicalWrapper insertInnerNode(IOOBNNode upperNode, IOOBNNode oobnNode, int type, double x, double y) {
		
		OOBNNodeGraphicalWrapper node = null;
		
		try{
			node = OOBNNodeGraphicalWrapper.newInstance(oobnNode.clone());
		} catch (Exception e) {
			Debug.println(this.getClass(), "Could not obtain new instance of OOBNNodeGraphicalWrapper", e);
		}
		

		node.getWrappedNode().setOriginalClassNode(oobnNode);
		
		node.setPosition(x, y);
		
		node.setStates(oobnNode.getStateNames());
		
		node.setName(oobnNode.getName());
		
		node.setDescription(node.getName());
		
		node.getWrappedNode().setType(type);
		
		node.getWrappedNode().setUpperInstanceNode(upperNode);
		
		node.getWrappedNode().setParentClass(upperNode.getParentClass());
		
		upperNode.addInnerNode(node.getWrappedNode());
		
		PotentialTable auxTabProb = (PotentialTable)((IRandomVariable) node).getProbabilityFunction();
		auxTabProb.addVariable(node);
		
		// initialize values using default linear values
		float linearValue = 1f / node.getStatesSize();
		for (int i = 0; i < auxTabProb.tableSize(); i++) {
			auxTabProb.setValue(i, linearValue);	
		}
		
		
//		this.getNetwork().addNode(node);
		return node;
	}
	

	/**
	 * @return the innerNodes
	 */
	public Set<OOBNNodeGraphicalWrapper> getInnerNodes() {
		return innerNodes;
	}

	/**
	 * @param innerNodes the innerNodes to set
	 */
	public void setInnerNodes(Set<OOBNNodeGraphicalWrapper> innerNodes) {
		this.innerNodes = innerNodes;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#setPosition(double, double)
	 */
	//by young
	/*
	@Override
	public void setPosition(double x, double y) {
		
		
		// overwriting this method in order to move inner nodes when necessary
		
		// calculate delta (diffs) while we can (before update)
		double deltaX = x - this.getPosition().getX();
		double deltaY = y - this.getPosition().getY();
		
		super.setPosition(x, y);
		
		// if this is an instance node, update its inner nodes as well
		if (this.getWrappedNode().getType() == this.getWrappedNode().TYPE_INSTANCE) {
			try {
				for (OOBNNodeGraphicalWrapper inner : this.getInnerNodes()) {
					// update each inner node by using the delta value extracted before
					inner.setPosition(inner.getPosition().getX() + deltaX, inner.getPosition().getY() + deltaY);
				}
			} catch (RuntimeException e) {
				Debug.println(this.getClass(), "It was not possible to move inner node of " + this.getName());
				throw e;
			}
		}
		
	
	}

*/
	// the graphical wrapper has no need to overwrite addChild
//	/* (non-Javadoc)
//	 * @see unbbayes.prs.Node#addChild(unbbayes.prs.Node)
//	 */
//	@Override
//	public void addChild(Node child) throws InvalidParentException {
//		
//		try{
//			this.getWrappedNode().addChild(((OOBNNodeGraphicalWrapper)child).getWrappedNode());
//			super.addChild(child);
//		} catch (Exception e) {
//			Debug.println(this.getClass(), "Could not add a child to wrapped OOBN node", e);
//			throw new InvalidParentException(e.getMessage());
//		}
//	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#addParent(unbbayes.prs.Node)
	 */
	@Override
	public void addParent(Node parent) throws InvalidParentException {
		
		try {
			// this is going to add child to the parent as well
			this.getWrappedNode().addParent(((OOBNNodeGraphicalWrapper)parent).getWrappedNode());
			// update parents/childrens for super class as well
			super.addParent(parent);
			parent.addChild(this);
		} catch (Exception e) {
			Debug.println(this.getClass(), "Could not add a parent to wrapped OOBN node", e);
			throw new InvalidParentException(e.getMessage());
		}
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.Node#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		
		if (obj instanceof IOOBNNode) {
			return this.getWrappedNode().equals(obj);
		}
		
		if (obj instanceof OOBNNodeGraphicalWrapper) {
			return this == obj || this.getName().equals(((OOBNNodeGraphicalWrapper)obj).getName());
		}
		
		return super.equals(obj);
	}
	
	
	
	

}
