/**
 * 
 */
package unbbayes.gui.mebn;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

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
import unbbayes.draw.UShapeOrdinaryVariableNode;
import unbbayes.draw.UShapeResidentNode;
import unbbayes.gui.GraphAction;
import unbbayes.gui.GraphPane;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.CycleFoundException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.util.ResourceController;
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
					
					// add new node into MFrag
					controller.getCurrentMFrag().addNode(newNode);
					
					// build a new shape for new node
					UShape shape = null;
					try {
						shape = this.getNodeDataTransferObject().getShapeBuilder().build().getUShape(newNode, this);
					} catch (IllegalAccessException e1) {
						throw new RuntimeException(e1);
					} catch (InstantiationException e1) {
						throw new RuntimeException(e1);
					}
					
					// add shape into this pane (canvas)
					addShape(shape);
					
					// set this node/shape as selected
					shape.setState(UShape.STATE_SELECTED, null);
					
					shape.update();
					
					// notify the probability function panel's builder that a new node is currently "selected" as owner			
					this.getNodeDataTransferObject().getProbabilityFunctionPanelBuilder().setProbabilityFunctionOwner(newNode);
					
					// we are not using controller.selectNode as the other cases because we are following
					// the same scheme adopted by BN plugin node, which calls showProbabilityDistributionPanel.
					// MEBNNetworkWindow overwrites the behavior.
					// Display the probability function panel for new node
					this.controller.getScreen().showProbabilityDistributionPanel(
								this.getNodeDataTransferObject().getProbabilityFunctionPanelBuilder()
							);
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
		// Actually, I don't think it's necessary, but let's just be loyal to the original code (from GraphPane)...
		super.createNode(newNode);
		
		// create MEBN-specific nodes 
		// TODO stop using if-instanceof structure and start using object binding to a UShape builder.
		UShape shape = null;
		if (newNode instanceof ContextNode) {
			shape = new UShapeContextNode(this, newNode, (int) newNode
					.getPosition().x, (int) newNode.getPosition().y, newNode
					.getWidth(), newNode.getHeight());
		} else if (newNode instanceof ResidentNode) {
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

	/**
	 * 
	 * delegates to {@link #getController()}
	 * @deprecated use {@link #getController()} and then {@link IMEBNMediator#getPluginNodeManager()}
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

}
