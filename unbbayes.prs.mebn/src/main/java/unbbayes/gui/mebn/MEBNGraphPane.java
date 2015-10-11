/**
 * 
 */
package unbbayes.gui.mebn;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import unbbayes.controller.NetworkController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeContextNode;
import unbbayes.draw.UShapeInputNode;
import unbbayes.draw.UShapeLine;
import unbbayes.draw.UShapeOrdinaryVariableNode;
import unbbayes.draw.UShapeResidentNode;
import unbbayes.gui.GraphAction;
import unbbayes.gui.GraphPane;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.IRandomVariable;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.cpt.IProbabilityFunctionAdapter;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.hybridbn.ContinuousNode;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.BuiltInRV;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IMultiEntityNode;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MultiEntityNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ResidentNodePointer;
import unbbayes.prs.mebn.entity.Type;
import unbbayes.prs.mebn.exception.ArgumentNodeAlreadySetException;
import unbbayes.prs.mebn.exception.CycleFoundException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.prs.mebn.exception.OVDontIsOfTypeExpected;
import unbbayes.prs.mebn.exception.OVariableAlreadyExistsInArgumentList;
import unbbayes.prs.mebn.extension.IMEBNPluginNode;
import unbbayes.util.Debug;
import unbbayes.util.ResourceController;
import unbbayes.util.clipboard.InternalClipboard;
import unbbayes.util.mebn.extension.manager.MEBNPluginNodeManager;

/**
 * This is the graph pane for MEBN module.
 * MEBN specific codes were moved from {@link GraphPane} to here.
 * @author Shou Matsumoto
 *
 */
public class MEBNGraphPane extends GraphPane {

	/** The resource is not static, so that hotplug would become easier */
	private ResourceBundle resource;
	
	
	/**
	 * @param dlg
	 * @param n
	 */
	public MEBNGraphPane(JDialog dlg, ProbabilisticNetwork n) {
		super(dlg, n);
		this.resource = ResourceController.newInstance().getBundle(
				unbbayes.gui.mebn.resources.Resources.class.getName());
	}

	/**
	 * @param controller
	 * @param graphViewport
	 */
	public MEBNGraphPane(NetworkController controller, JViewport graphViewport) {
		super(controller, graphViewport);
		this.resource = ResourceController.newInstance().getBundle(
				unbbayes.gui.mebn.resources.Resources.class.getName());
		
		// the following code was migrated from MEBNNetworkWindow to here during refactory
		
		//by young
		long width = (long)Node.getDefaultSize().getX();
		long height = (long)Node.getDefaultSize().getY();
		
		this.getGraphViewport().reshape(0, 0,
				(int) (this.getBiggestPoint().getX() + width),
				(int) (this.getBiggestPoint().getY() + height));
		
		this.getGraphViewport().setViewSize(
				new Dimension(
						(int) (this.getBiggestPoint().getX() + width),
						(int) (this.getBiggestPoint().getY() + height)));

		// set the content and size of graphViewport
		this.getGraphViewport().setView(this);
		this.getGraphViewport().setSize(800, 600);
	}
	
	/**
	 * This method is responsible to treat mouse button events
	 * 
	 *@param e
	 *            <code>MouseEvent</code>
	 *@see MouseEvent
	 */
	public void mouseClicked(MouseEvent e) {
		GraphAction action = this.getAction();
		// do not delegate to superclass if this is a request for plugin node
		if (!action.equals(GraphAction.ADD_PLUGIN_NODE)) {
			super.mouseClicked(e);
		}
		
		// following, MEBN specific codes.
		
		// first, lets test if we can apply mebn specific codes to controller
		if (!(this.getController() instanceof MEBNController)) {
			// do nothing if we cannot apply specific code
			return;
		}
		
		// do actions for MEBN
		MEBNController controller = (MEBNController)this.getController();
		if (SwingUtilities.isLeftMouseButton(e)) {
			Node newNode = null;
			switch (this.getAction()) {
				case CREATE_DOMAIN_MFRAG: {
					controller.insertDomainMFrag();
				}
					break;
				case CREATE_CONTEXT_NODE: {
					try {
						newNode = controller.insertContextNode(e.getX(), e.getY());
						UShapeContextNode shape = new UShapeContextNode(this,
								newNode, (int) newNode.getPosition().x
										- newNode.getWidth() / 2, (int) newNode
										.getPosition().y
										- newNode.getHeight() / 2, newNode
										.getWidth(), newNode.getHeight());
						addShape(shape);
						shape.setState(UShape.STATE_SELECTED, null);
						controller.selectNode(newNode);
					} catch (MEBNConstructionException exception) {
						JOptionPane.showMessageDialog(((MEBNNetworkWindow)controller.getScreen())
								.getMebnEditionPane(), resource.getString("withoutMFrag"), 
								resource.getString("operationError"),
								JOptionPane.WARNING_MESSAGE);
					}
				}
					break;
				case CREATE_RESIDENT_NODE: {
					try {
						newNode = controller.insertResidentNode(e.getX(), e.getY());
						UShapeResidentNode shape = new UShapeResidentNode(this,
								newNode, (int) newNode.getPosition().x
										- newNode.getWidth() / 2, (int) newNode
										.getPosition().y
										- newNode.getHeight() / 2, newNode
										.getWidth(), newNode.getHeight());
						addShape(shape);
						shape.setState(UShape.STATE_SELECTED, null);
						controller.selectNode(newNode);
					} catch (MEBNConstructionException exception) {
						JOptionPane.showMessageDialog(
									((MEBNNetworkWindow)controller.getScreen()).getMebnEditionPane(), 
									resource.getString("withoutMFrag"), 
									resource.getString("operationError"),
									JOptionPane.WARNING_MESSAGE
								);
					}
				}
					break;
				case CREATE_INPUT_NODE: {
					try {
						newNode = controller.insertInputNode(e.getX(), e.getY());
						UShapeInputNode shape = new UShapeInputNode(this, newNode,
								(int) newNode.getPosition().x - newNode.getWidth()
										/ 2, (int) newNode.getPosition().y
										- newNode.getHeight() / 2, newNode
										.getWidth(), newNode.getHeight());
						addShape(shape);
						shape.setState(UShape.STATE_SELECTED, null);
						controller.selectNode(newNode);
					} catch (MFragDoesNotExistException exception) {
						JOptionPane.showMessageDialog(
								((MEBNNetworkWindow)controller.getScreen()), resource
								.getString("withoutMFrag"), resource
								.getString("operationError"),
								JOptionPane.WARNING_MESSAGE);
					}
				}
					break;
				case CREATE_ORDINARYVARIABLE_NODE: {
					try {
						newNode = controller.insertOrdinaryVariable(e.getX(), e.getY());
						UShapeOrdinaryVariableNode shape = new UShapeOrdinaryVariableNode(
								this, newNode, (int) newNode.getPosition().x
										- newNode.getWidth() / 2, (int) newNode
										.getPosition().y
										- newNode.getHeight() / 2, newNode
										.getWidth(), newNode.getHeight());
						addShape(shape);
						shape.setState(UShape.STATE_SELECTED, null);
						controller.selectNode(newNode);
	
					} catch (MEBNConstructionException exception) {
						JOptionPane.showMessageDialog(
								((MEBNNetworkWindow)controller.getScreen()), 
								resource.getString("withoutMFrag"), 
								resource.getString("operationError"),
								JOptionPane.WARNING_MESSAGE);
					}
				}
					break;
				case ADD_PLUGIN_NODE: {
					// the following code is still a stub, since plugin nodes for MEBN are not implemented yet
					// build new node
					newNode = this.getNodeDataTransferObject().getNodeBuilder().buildNode();
					newNode.setPosition(e.getX(), e.getY());
					
					if (this.getController() instanceof MEBNController) {
						if (((MEBNController)this.getController()).getCurrentMFrag() != null) {
							// update mediator if it is a MEBN plugin node
							if (newNode instanceof IMEBNPluginNode) {
								IMEBNPluginNode pluginNode = (IMEBNPluginNode) newNode;
								try {
									pluginNode.setMediator(((MEBNController)this.getController()));
								}catch (Exception exc) {
									Debug.println(this.getClass(), pluginNode + " is not a Plugin Node, but we'll keep running the program.", exc);
								}
							}
							// add new node into MFrag
							((MEBNController)this.getController()).getCurrentMFrag().addNode(newNode);
						} else {
							JOptionPane.showMessageDialog(
									((MEBNNetworkWindow)controller.getScreen()), 
									resource.getString("withoutMFrag"), 
									resource.getString("operationError"),
									JOptionPane.WARNING_MESSAGE);
							return;
						}
						
						// notify node that it was added to a mfrag
						if (newNode instanceof IMEBNPluginNode) {
							IMEBNPluginNode pluginNode = (IMEBNPluginNode) newNode;
							try {
								pluginNode.onAddToMFrag(((MEBNController)this.getController()).getCurrentMFrag());
							} catch (MFragDoesNotExistException e1) {
								e1.printStackTrace();
								JOptionPane.showMessageDialog(
										((MEBNNetworkWindow)controller.getScreen()), 
										resource.getString("withoutMFrag"), 
										resource.getString("operationError"),
										JOptionPane.WARNING_MESSAGE);
							}
						}
						
						// build a new shape for new node
						UShape shape = null;
						try {
							shape = this.getNodeDataTransferObject().getShapeBuilder().build().getUShape(newNode, this);
						} catch (IllegalAccessException e1) {
							throw new RuntimeException(e1);
						} catch (InstantiationException e1) {
							throw new RuntimeException(e1);
						}
						shape.setNode(newNode);
						shape.setCanvas(this);
						shape.setLocation((int)(newNode.getPosition().getX()), (int)(newNode.getPosition().getY()));
						
						// add shape into this pane (canvas)
						addShape(shape);
						
						// set this node/shape as selected
						shape.setState(UShape.STATE_SELECTED, null);
						
						shape.update();
						
						// notify the probability function panel's builder that a new node is currently "selected" as owner			
						this.getNodeDataTransferObject().getProbabilityFunctionPanelBuilder().setProbabilityFunctionOwner(newNode);
						
						// The following method expects that 
						// controller.getPluginNodeManager().getPluginNodeInformation(this.getNodeDataTransferObject().getProbabilityFunctionPanelBuilder().getProbabilityFunctionOwner().getClass())
						// which is the same as controller.getPluginNodeManager().getPluginNodeInformation(newNode.getClass())
						// can retrieve the same values of this.getNodeDataTransferObject()
						
						this.getController().selectNode(newNode);
					}
					
				}
					break;
				default: {
					if (controller != null)
						controller.unselectAll();
				}
			}
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#insertEdge(unbbayes.prs.Edge)
	 */
	public boolean insertEdge(Edge edge) {
		// overwritting the method in order to do MEBN-specific jobs or catch special exceptions
		try {
			return this.controller.insertEdge(edge);
		} catch (MEBNConstructionException me) {
			JOptionPane.showMessageDialog(
					((MEBNNetworkWindow)controller.getScreen()).getMebnEditionPane(), 
					me.getMessage(), 
					resource.getString("error"), 
					JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (CycleFoundException cycle) {
			JOptionPane.showMessageDialog(
					((MEBNNetworkWindow)controller.getScreen()).getMebnEditionPane(), 
					cycle.getMessage(), 
					resource.getString("error"), 
					JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#createNode(unbbayes.prs.Node)
	 */
	public void createNode(Node newNode) {

		// create nodes from superclass (ordinal nodes). 
		// It may look like the following code is useless, but it is going to execute something after mebn is compiled
		// (because SSBN contains only normal bn nodes, and they must be rendered by a Shape class here).
		if (!(newNode instanceof IMultiEntityNode)) {
			// this code must only be executed when we are sure it is not a MEBN node, because nodes both compatible with BN & MEBN (e.g. plugin nodes) 
			// causes duplicate shapes in canvas.
			super.createNode(newNode);
		}
		
		
		// create MEBN-specific nodes 
		// TODO stop using if-instanceof structure and start using object binding to a UShape builder.
		UShape shape = null;
		if (newNode instanceof IMEBNPluginNode) {
			try {
				shape = this.getPluginNodeManager().getPluginNodeInformation(
						newNode.getClass()).getShapeBuilder().build()
						.getUShape(newNode, this);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			}
			if (shape != null) {
				shape.setBounds(
						(int)newNode.getPosition().getX(), 
						(int)newNode.getPosition().getY(), 
						newNode.getWidth(), 
						newNode.getHeight()
					);
			}
		} else if (newNode instanceof ContextNode) {
			shape = new UShapeContextNode(this, newNode, (int) newNode
					.getPosition().x, (int) newNode.getPosition().y, newNode
					.getWidth(), newNode.getHeight());
		} else if (newNode instanceof IResidentNode) {
			shape = new UShapeResidentNode(this, newNode, (int) newNode
					.getPosition().x, (int) newNode.getPosition().y, newNode
					.getWidth(), newNode.getHeight());
		} else if (newNode instanceof InputNode) {
			shape = new UShapeInputNode(this, newNode, (int) newNode
					.getPosition().x, (int) newNode.getPosition().y, newNode
					.getWidth(), newNode.getHeight());
		} else if (newNode instanceof OrdinaryVariable) {
			shape = new UShapeOrdinaryVariableNode(this, newNode, (int) newNode
					.getPosition().x, (int) newNode.getPosition().y, newNode
					.getWidth(), newNode.getHeight());
		} 

		if (shape != null) {
			try {
				addShape(shape);
				shape.setState(UShape.STATE_SELECTED, null);
			} catch (NullPointerException e) {
				throw new RuntimeException("Could not find or set a shape for node: " + newNode.getName(),e);
			}
		}
	}
	
	private Boolean nameExists(String name){
		List<Node> nodes = ((MEBNController)controller).getCurrentMFrag().getNodes();
		for (Node node : nodes) {
			if (name.equals(node.getName())) {
				return true;
			}
		}
		return false;
	}
	
	//returns -1 if there is no element with such a name in the list
	private int getNodeIndex(String name, List<Node> nodes){
		for (Node node : nodes) {
			if (name.equals(node.getName())) {
				return nodes.indexOf(node);
			}
		}
		return -1;
	}
	//this function checks if the ovar in the list is already in context
	//if not it will create it
	private List<OrdinaryVariable> getOVarList(Node originalNode, Node clonedNode, double positionX, double positionY){
		//check if the ovar list is on context
		List<OrdinaryVariable> originalOVarList = new ArrayList<OrdinaryVariable>(((ResidentNode)originalNode).getOrdinaryVariableList());
		List<OrdinaryVariable> newOVarList = new ArrayList<OrdinaryVariable>();
		List<Node> allNodes = controller.getGraph().getNodes();
		for(OrdinaryVariable oVar: originalOVarList){
			int nodeIndex = getNodeIndex(oVar.getName(), allNodes);
			//if it is on context, bind my node to it & vice-versa
			if(nodeIndex != -1 && allNodes.get(nodeIndex) instanceof OrdinaryVariable){
				OrdinaryVariable ov = (OrdinaryVariable)allNodes.get(nodeIndex);
				newOVarList.add(ov);
				ov.addIsOVariableOfList(clonedNode);
			}
			//otherwise create a new oVar then bind
			else{
				OrdinaryVariable newOVar;
				try {
					newOVar = ((MEBNController)controller).insertOrdinaryVariable(positionX, positionY-50);
					newOVar.setName(oVar.getName());
					newOVar.setValueType(oVar.getValueType());
					newOVarList.add(newOVar);
					newOVar.addIsOVariableOfList(clonedNode);
					newOVar.updateLabel();
				} catch (MFragDoesNotExistException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//getNodeUShape(newOVar).setState(UShape.STATE_SELECTED, null);
			}
		}
		return newOVarList;
		
	}
	@Override
	protected Node addClonedNode(final Node originalNode,
			int xPixelsToCopy, int yPixelsToCopy) {
		Debug.println("Cloning " + originalNode);

		// A new cloned node to create.
		Node clonedNode = new DecisionNode();

		double positionX = originalNode.getPosition().getX() + xPixelsToCopy;
		double positionY = originalNode.getPosition().getY() + yPixelsToCopy;
		
		try {
			if(originalNode instanceof InputNode){
				clonedNode = (InputNode)((MEBNController)controller).insertInputNode(positionX, positionY);
			
				
				Object inputInstance = ((InputNode)originalNode).getInputInstanceOf();
				if(inputInstance instanceof ResidentNode)
					((MEBNController)controller).setInputInstanceOf((InputNode) clonedNode, (ResidentNode)inputInstance);
					
			}else if(originalNode instanceof OrdinaryVariable){
				Type type = ((OrdinaryVariable)originalNode).getValueType();
				clonedNode = (OrdinaryVariable)((MEBNController)controller).insertOrdinaryVariable(positionX, positionY);
				((OrdinaryVariable)clonedNode).setValueType(type);
				
			}else if(originalNode instanceof ContextNode){
				clonedNode = (ContextNode)((MEBNController)controller).insertContextNode(positionX, positionY);
				((ContextNode)clonedNode).setFormulaTree(((ContextNode)originalNode).getFormulaTree());
			}else if(originalNode instanceof ResidentNode){
				clonedNode = (ResidentNode)((MEBNController)controller).insertResidentNode(positionX, positionY);
				for(OrdinaryVariable oVar: getOVarList(originalNode, clonedNode, positionX, positionY))
					((ResidentNode)clonedNode).addArgument(oVar, true);
//				((ResidentNode)clonedNode).setOrdinaryVariableList(getOVarList(originalNode, clonedNode, positionX, positionY));
//				((ResidentNode)clonedNode).(getOVarList(originalNode, clonedNode, positionX, positionY));
				
			}
			else{
				return super.addClonedNode(originalNode, xPixelsToCopy, yPixelsToCopy);
			}
		} catch (MFragDoesNotExistException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CycleFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OVDontIsOfTypeExpected e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentNodeAlreadySetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OVariableAlreadyExistsInArgumentList e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String newName;
		if (!nameExists(originalNode.getName())){
			newName = originalNode.getName();
		}
		else{
			newName = getUniqueName(originalNode.getName() + "_1");
		}
		clonedNode.setName(newName);
		clonedNode.updateLabel();
		createNode(clonedNode);
//		getNodeUShape(clonedNode).setState(UShape.STATE_SELECTED, null);
//		controller.selectNode(clonedNode);
		((MEBNController)controller).updateArgumentsOfObject(clonedNode);
		selectNode(clonedNode);
		// Clear the default states
		clonedNode.removeStates();
		
		// Copy states
		int numStates = originalNode.getStatesSize();
		for (int i = 0; i < numStates; i++) {
			clonedNode.appendState(originalNode.getStateAt(i));
		}
		
		return clonedNode;

	}

	/**
	 * 
	 * delegates to {@link #getController()}
	 * @see {@link #getController()}
	 * @see {@link IMEBNMediator#getPluginNodeManager()}
	 */
	public MEBNPluginNodeManager getPluginNodeManager() {
		try {
			return ((IMEBNMediator)this.getController()).getPluginNodeManager();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	/**
	 * delegates to {@link #getController()}
	 * @deprecated use {@link #getController()} and then {@link IMEBNMediator#setPluginNodeManager(MEBNPluginNodeManager)}
	 */
	public void setPluginNodeManager(MEBNPluginNodeManager pluginNodeManager) {
		try {
			((IMEBNMediator)this.getController()).setPluginNodeManager(pluginNodeManager);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	/**
	 * @return the resource
	 */
	public ResourceBundle getResource() {
		return resource;
	}

	/**
	 * @param resource the resource to set
	 */
	public void setResource(ResourceBundle resource) {
		this.resource = resource;
	}
	
}