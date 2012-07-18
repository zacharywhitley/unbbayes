/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Brasilia
 *
 *  This file is part of UnbBayes.
 *
 *  UnbBayes is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  UnbBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnbBayes; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package unbbayes.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.draw.IEdgeHolderShape;
import unbbayes.draw.INodeHolderShape;
import unbbayes.draw.UCanvas;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeDecisionNode;
import unbbayes.draw.UShapeLine;
import unbbayes.draw.UShapeProbabilisticNode;
import unbbayes.draw.UShapeState;
import unbbayes.draw.UShapeUtilityNode;
import unbbayes.draw.extension.IPluginUShape;
import unbbayes.gui.table.extension.IProbabilityFunctionPanelBuilder;
import unbbayes.prs.Edge;
import unbbayes.prs.INode;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.exception.InvalidParentException;
import unbbayes.prs.extension.IPluginNode;
import unbbayes.prs.hybridbn.ContinuousNode;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;
import unbbayes.util.Debug;
import unbbayes.util.clipboard.InternalClipboard;
import unbbayes.util.extension.dto.INodeClassDataTransferObject;
import unbbayes.util.extension.manager.CorePluginNodeManager;

/**
 * This class has to draw a bayesian network. This is a <code>JPanel</code> and
 * also implements the interfaces MouseListener and MouseMotionListener, in
 * order to capture mouse events and design a bayesian network.
 * 
 * @author Michael S. Onishi
 * @author Rommel N. Carvalho
 * @created 27 de Junho de 2001
 * @see JPanel Modified by Young, 4.13.2009
 * 
 */
public class GraphPane extends UCanvas implements KeyListener {

	private static final int DEFAULT_DIMENSION = 1500;

	private static final int DEFAULT_WIDTH = 600;

	private static final int DEFAULT_HEIGH = 800;

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	// by young4
	// public NetworkController controller;
	public List<Edge> edgeList;

	// TODO Substituir essa lista de n�s por generics como est� acima com a
	// lista de Edge. Fazer isso em todo lugar que for necess�rio, at� que se
	// possa exluir o NodeList
	public List<Node> nodeList;

	public GraphAction action;

	public static Color backgroundColor;

	public JViewport graphViewport;

	public Dimension visibleDimension;

	public Dimension graphDimension;

	/* Icon Controller */
	public final IconController iconController = IconController.getInstance();

	public ProbabilisticNetwork net;

	private JPopupMenu popup = new JPopupMenu();

	public String PANEMODE_NONE = "None";

	public String PANEMODE_COMPILE = "Compile";

	public String strPaneMode = PANEMODE_NONE;

	// a dto that temporally holds a plugin node's informations.
	private INodeClassDataTransferObject nodeClassDataTransferObject;

	// This object manages plugin-loaded nodes.
	private CorePluginNodeManager pluginNodeManager = CorePluginNodeManager
			.newInstance();

	/**
	 * {@link #update()} will set {@link UShapeLine#getUseSelection()} to same
	 * value;
	 * 
	 * @see #update()
	 * @see {@link UShapeLine#getUseSelection()}
	 */
	private boolean toUseSelectionForLines = false;

	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController
			.newInstance().getBundle(
					unbbayes.gui.resources.GuiResources.class.getName());

	public GraphPane(JDialog dlg, ProbabilisticNetwork n) {
		super();
		net = n;

		edgeList = net.getEdges();
		nodeList = net.getNodes();

		this.setSize(DEFAULT_HEIGH, DEFAULT_WIDTH);

		graphDimension = new Dimension(DEFAULT_DIMENSION, DEFAULT_DIMENSION);
		visibleDimension = new Dimension(0, 0);
		action = GraphAction.NONE;

		update();
	}

	/**
	 * O construtor � respons�vel por iniciar todas as vari�veis que ser�o
	 * utilizadas por essa classe para que se possa desenhar a rede Bayesiana.
	 * 
	 * @param controlador
	 *            o controlador (<code>TControladorTelaPrincipal</code>)
	 * @param graphViewport
	 *            a tela, (<code>TViewport</code>), onde ser� inserida essa
	 *            classe
	 */
	public GraphPane(NetworkController controller, JViewport graphViewport) {
		super();

		this.controller = controller;
		this.graphViewport = graphViewport;
		this.setSize(DEFAULT_HEIGH, DEFAULT_WIDTH);

		edgeList = controller.getGraph().getEdges();
		nodeList = controller.getGraph().getNodes();

		graphDimension = new Dimension(DEFAULT_DIMENSION, DEFAULT_DIMENSION);
		visibleDimension = new Dimension(0, 0);
		action = GraphAction.NONE;

		graphViewport.setViewSize(visibleDimension);
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		// Capture Ctr+C = Copy
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_C) {
			Debug.println("Copy");

			// Get selected network
			List<Node> group = getSelectedGroup();

			// Add network to clipboard
			InternalClipboard.getInstance().putToClipboard(group);
		}
		// Capture Ctr+V = Paste
		else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_V) {
			Debug.println("Paste");

			// Load network from clipboard
			Object fromClipboard = InternalClipboard.getInstance()
					.getFromClipboard();

			try {
				List<Node> group = (List<Node>) fromClipboard;

				pasteAndDrawNodes(group);
				// for (Node node : group) {
				// Debug.println("Pegar " + node.toString());
				// }

			} catch (Exception ex) {
				Debug.println(GraphPane.class, "Error pasting", ex);
			}

			// Clone elements

			// Add elements to the network
			// Capture position of the mouse

			// Draw the loaded network in mouse position
		}
	}

	private void pasteAndDrawNodes(List<Node> group) {

		// Nodes to copy
		List<Node> mainNodesToCopy = new ArrayList<Node>();

		// Find root nodes (no parents) and nodes without selected parents
		for (Node node : group) {
			List<Node> parents = node.getParents();

			boolean hasParents = false;
			for (Node parent : parents) {

				if (getNodeUShape(parent).getState().equals(
						UShape.STATE_SELECTED)) {
					hasParents = true;
					break;
				}
			}

			if (hasParents) {
				continue;
			}

			mainNodesToCopy.add(node);
		}

		HashMap<Node, Node> sharedNodes = new HashMap<Node, Node>();
		// copy nodes
		// FIXME this for could be inside the last for
		for (Node rootNode : mainNodesToCopy) {

			// No root identified.
			if (rootNode == null) {
				Debug.println("Root node not identified");
				return;
			}

			// Des-select initial node.
			getNodeUShape(rootNode).setState(UShape.STATE_NONE, null);

		

			// Clone the root and their children recursively
			if (rootNode instanceof ProbabilisticNode) {				
				ProbabilisticNode probNode = (ProbabilisticNode) rootNode;

				addClonedNode(probNode, sharedNodes);

			}
			// TODO support for other kind root nodes such as utility.
		}

	}

	private Node addClonedNode(final Node probNode,
			HashMap<Node, Node> sharedNodes) {
		Debug.println("Cloning "+probNode);
		ProbabilisticNode newNode;
		// Node newNode = controller.insertProbabilisticNode(mouseX, mouseY);
		if (sharedNodes.get(probNode) == null) {
			newNode = (ProbabilisticNode) controller.insertProbabilisticNode(
					probNode.getPosition().getX() + 100, probNode.getPosition()
							.getY() + 100);
			sharedNodes.put(probNode, newNode);
		} else {
			newNode = (ProbabilisticNode) sharedNodes.get(probNode);
			return newNode;
		}

		// set attributes
		String newName = probNode.getName() + "_1";
		newName = getUniqueName(newName);
		newNode.setName(newName);
		newNode.setDescription(probNode.getDescription());

		UShapeProbabilisticNode shape = new UShapeProbabilisticNode(this,
				newNode,
				(int) newNode.getPosition().x - newNode.getWidth() / 2,
				(int) newNode.getPosition().y - newNode.getHeight() / 2,
				newNode.getWidth(), newNode.getHeight());
		addShape(shape);
		shape.setState(UShape.STATE_SELECTED, null);

		// /// Copy CPT Table ///
		// Clear the default states
		newNode.removeStates();

		// Copy states
		int numStates = probNode.getStatesSize();
		for (int i = 0; i < numStates; i++) {
			newNode.appendState(probNode.getStateAt(i));
		}

		// add children
		List<Node> children = probNode.getChildren();
		// Validate that every children is selected
		for (Node childNode : children) {

			// If child node is not selected then
			if (!getNodeUShape(childNode).getState().equals(
					UShape.STATE_SELECTED)) {
				continue;
			}

			// Create child recursively.
			Node clonedChild = addClonedNode(childNode, sharedNodes);

			try {
				newNode.addChildNode(childNode);

				Debug.println("Line between " + newName + " and " + clonedChild);
				// Add a line between parent and new child.
				UShapeLine line = new UShapeLine(this, getNodeUShape(newNode),
						getNodeUShape(clonedChild));

				Edge edge = new Edge(newNode, clonedChild);
				line.setEdge(edge);

				line.setLearningLineSelection(false);
				addShape(line);
				insertEdge(edge);
				onDrawConnectLineReleased(line, 0, 0);

			} catch (InvalidParentException e) {
				Debug.println(GraphPane.class, "Node did not add as a child", e);
			}
		}

		// showCPT(newNode);
		return newNode;

	}

	/**
	 * Obtain an unique name comparing a newName with all existing node names.
	 * This algorithm is recursive in order to verify that the new name is
	 * unique.
	 * 
	 * @param newName
	 *            first new proposed unique node name.
	 * @return a unique node name.
	 */
	private String getUniqueName(String newName) {
		List<Node> nodes = controller.getGraph().getNodes();
		for (Node node : nodes) {
			if (newName.equals(node.getName())) {
				int tempNameIndex = newName.lastIndexOf("_") + 1;
				String copyCounter = newName.substring(tempNameIndex);
				int c = Integer.valueOf(copyCounter) + 1;

				newName = newName.substring(0, tempNameIndex) + c;

				return getUniqueName(newName);
			}
		}

		return newName;
	}

	public void keyPressed(KeyEvent e) {
	}

	/**
	 * Seta o atributo graphDimension (tamanho da rede Bayesiana) do objeto da
	 * classe GraphPane
	 * 
	 * @param graphDimension
	 *            O novo valor do tamanho da rede (<code>Dimension</code>)
	 * @see Dimension
	 */
	public void setGraphDimension(Dimension graphDimension) {
		this.graphDimension = graphDimension;
	}

	/**
	 * Retorna o objeto selecionado (<code>Object</code>), que pode ser um
	 * <code>Node</code> ou <code>Edge</code>
	 * 
	 * @return O valor do <code>Object</code> selected
	 * @see Object
	 * @see Node
	 * @see Edge
	 */
	public Object getSelected() {
		UShape selectedShape = getSelectedShape();
		if (selectedShape instanceof IEdgeHolderShape) {
			return ((IEdgeHolderShape) selectedShape).getEdge();
		}
		if (selectedShape instanceof INodeHolderShape) {
			return ((INodeHolderShape) selectedShape).getNode();
		}
		return selectedShape;
	}

	public void updateSelectedNode() {
		getSelectedShape().update();
	}

	public void setPaneMode(String str) {
		strPaneMode = str;
	}

	public String getPaneMode() {
		return strPaneMode;
	}

	/**
	 * Retorna uma lista de selecionados (<code>List</code>), que podem ser um
	 * <code>Node</code> e/ou <code>Edge</code>
	 * 
	 * @return O valor do <code>List</code> selectedGroup
	 * @see List
	 * @see Node
	 * @see Edge
	 */
	public List<Node> getSelectedGroup() {
		List<Node> selectedGroup = new ArrayList<Node>();

		int n = this.getComponentCount();
		for (int i = 0; i < n; i++) {
			UShape shape = (UShape) this.getComponent(i);

			if (shape.getState() == UShape.STATE_SELECTED
					&& shape.getNode() != null) {
				controller.selectNode(shape.getNode());

				selectedGroup.add(shape.getNode());
			}
		}

		return selectedGroup;
	}

	/**
	 * Pega a <code>Dimension</code> tamanhoRede do objeto da classe GraphPane
	 * 
	 * @return A <code>Dimension</code> graphDimension
	 */
	public Dimension getGraphDimension() {
		return this.graphDimension;
	}

	public void setState(String s) {
		super.setState(s);
	}

	/**
	 * Pega o atributo focusTransversable do objeto da classe GraphPane
	 * 
	 * @return True como valor do focusTransversable (m�todo necess�rio para que
	 *         se possa tratar evento de tecla)
	 */
	public boolean isFocusable() {
		return true;
	}

	/**
	 * Pega o atributo graphViewport (<code>JViewport</code>) do objeto da
	 * classe GraphPane
	 * 
	 * @return O graphViewport (container onde essa classe se encontra inserida)
	 * @see JViewport
	 */
	public JViewport getGraphViewport() {
		return this.graphViewport;
	}

	/**
	 * Pega o maior ponto (<code>Point2D.Double</code>) do objeto da classe
	 * GraphPane
	 * 
	 * @return O maior ponto (x,y) existente nessa rede Bayesiana
	 * @see Point2D.Double
	 */
	public Point2D.Double getBiggestPoint() {
		double maiorX = 0;
		double maiorY = 0;

		for (int i = 0; i < nodeList.size(); i++) {
			Node noAux = (Node) nodeList.get(i);
			if (maiorX < noAux.getPosition().getX() + noAux.getWidth()) {
				maiorX = noAux.getPosition().getX() + noAux.getWidth();
			}

			if (maiorY < noAux.getPosition().getY() + noAux.getHeight()) {
				maiorY = noAux.getPosition().getY() + noAux.getHeight();
			}
		}
		if (maiorX < visibleDimension.getWidth()) {
			maiorX = graphViewport.getViewSize().getWidth();
		}
		if (maiorY < visibleDimension.getHeight()) {
			maiorY = graphViewport.getViewSize().getHeight();
		}

		return new Point2D.Double(maiorX, maiorY);
	}

	public void showCPT(Node newNode) {
		// set new information of node into tree and table viewer
		if (controller != null) {
			// the if below fixes the problem that selecting ContinuousNode was
			// not updating name and description text field
			if (this.controller.getScreen() != null) {
				if (this.controller.getScreen().getTxtName() != null) {
					this.controller.getScreen().getTxtName()
							.setText(newNode.getName());
				}
				if (this.controller.getScreen().getTxtDescription() != null) {
					this.controller.getScreen().getTxtDescription()
							.setText(newNode.getDescription());
				}
			}
			if (controller.getGraph() instanceof SingleEntityNetwork) {
				controller.createTable(newNode);
			}
		}
	}

	/**
	 * This method is responsible to treat mouse button events
	 * 
	 * @param e
	 *            <code>MouseEvent</code>
	 * @see MouseEvent
	 */
	public void mouseClicked(MouseEvent e) {
		// TODO stop using direct access to controller as a field, and start
		// using get/set methods
		if (SwingUtilities.isLeftMouseButton(e)) {
			Node newNode = null;

			switch (getAction()) {

			case CREATE_CONTINUOUS_NODE: {
				newNode = controller.insertContinuousNode(e.getX(), e.getY());
				UShapeProbabilisticNode shape = new UShapeProbabilisticNode(
						this,
						newNode,
						(int) newNode.getPosition().x - newNode.getWidth() / 2,
						(int) newNode.getPosition().y - newNode.getHeight() / 2,
						newNode.getWidth(), newNode.getHeight());
				addShape(shape);
				shape.setState(UShape.STATE_SELECTED, null);
				showCPT(newNode);
			}
				break;
			case CREATE_PROBABILISTIC_NODE: {
				newNode = controller
						.insertProbabilisticNode(e.getX(), e.getY());
				UShapeProbabilisticNode shape = new UShapeProbabilisticNode(
						this,
						newNode,
						(int) newNode.getPosition().x - newNode.getWidth() / 2,
						(int) newNode.getPosition().y - newNode.getHeight() / 2,
						newNode.getWidth(), newNode.getHeight());
				addShape(shape);
				shape.setState(UShape.STATE_SELECTED, null);
				showCPT(newNode);

			}
				break;
			case CREATE_DECISION_NODE: {
				newNode = controller.insertDecisionNode(e.getX(), e.getY());
				UShapeDecisionNode shape = new UShapeDecisionNode(
						this,
						newNode,
						(int) newNode.getPosition().x - newNode.getWidth() / 2,
						(int) newNode.getPosition().y - newNode.getHeight() / 2,
						newNode.getWidth(), newNode.getHeight());
				addShape(shape);
				shape.setState(UShape.STATE_SELECTED, null);
				showCPT(newNode);
			}
				break;
			case CREATE_UTILITY_NODE: {
				newNode = controller.insertUtilityNode(e.getX(), e.getY());
				UShapeUtilityNode shape = new UShapeUtilityNode(
						this,
						newNode,
						(int) newNode.getPosition().x - newNode.getWidth() / 2,
						(int) newNode.getPosition().y - newNode.getHeight() / 2,
						newNode.getWidth(), newNode.getHeight());
				addShape(shape);
				shape.setState(UShape.STATE_SELECTED, null);
				showCPT(newNode);
			}
				break;
			case ADD_PLUGIN_NODE: {
				// build new node
				newNode = this.getNodeDataTransferObject().getNodeBuilder()
						.buildNode();
				newNode.setPosition(e.getX(), e.getY());

				// add new node into network
				this.controller.getNetwork().addNode(newNode);

				// build a new shape for new node
				UShape shape = null;
				try {
					shape = this.getNodeDataTransferObject().getShapeBuilder()
							.build().getUShape(newNode, this);
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

				// notify the probability function panel's builder that a new
				// node is currently "selected" as owner
				this.getNodeDataTransferObject()
						.getProbabilityFunctionPanelBuilder()
						.setProbabilityFunctionOwner(newNode);

				// display the probability function panel for new node
				this.controller.getScreen().showProbabilityDistributionPanel(
						this.getNodeDataTransferObject()
								.getProbabilityFunctionPanelBuilder());
			}
				break;
			case NONE: {
				if (controller != null)
					controller.unselectAll();
			}
				break;
			}
		}

	}

	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);

		if (SwingUtilities.isLeftMouseButton(e)) {
			// Debug.println(this.getClass(), "Left button released.");
		}

		if (SwingUtilities.isMiddleMouseButton(e)) {
			// Debug.println(this.getClass(), "Middle button released.");
		}

		if (SwingUtilities.isRightMouseButton(e)) {
			// Debug.println(this.getClass(), "Right button released.");

			if (getPaneMode() == PANEMODE_COMPILE) {
				resetPopup();
				popup.setEnabled(true);
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public void selectNode(Node n) {
		UShape shape = null;
		shape = getNodeUShape(n);
		shape.setState(UShape.STATE_SELECTED, null);

	}

	public boolean insertEdge(Edge edge) {

		// Ask the controller to insert the following edge
		try {
			return controller.insertEdge(edge);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	/**
	 * This is equivalent to
	 * {@link #setNodeDataTransferObject(INodeClassDataTransferObject)} followed
	 * by {@link #setAction(GraphAction)}
	 * 
	 * @see #setAction(GraphAction)
	 * @see #setNodeDataTransferObject(INodeClassDataTransferObject)
	 * @param action
	 *            : The action to be taken.
	 * @param dto
	 *            : the data transfer object to set
	 */
	public void setAction(GraphAction action, INodeClassDataTransferObject dto) {
		this.setNodeDataTransferObject(dto);
		this.setAction(action);
	}

	/**
	 * Set the action to be taken.
	 * 
	 * @param action
	 *            The action to be taken.
	 */
	public void setAction(GraphAction action) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Cursor customCursor;

		switch (action) {
		case CREATE_CONTINUOUS_NODE: {
			customCursor = toolkit.createCustomCursor(iconController
					.getContinueNodeCursor().getImage(), new Point(0, 0),
					"Cursor");
			setCursor(customCursor);
			setState(STATE_NONE);
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, customCursor);
		}
			break;
		case CREATE_PROBABILISTIC_NODE: {
			customCursor = toolkit.createCustomCursor(iconController
					.getEllipsisNodeCursor().getImage(), new Point(0, 0),
					"Cursor");
			setCursor(customCursor);
			setState(STATE_NONE);
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, customCursor);
		}
			break;
		case CREATE_DECISION_NODE: {
			customCursor = toolkit.createCustomCursor(iconController
					.getDecisionNodeCursor().getImage(), new Point(0, 0),
					"Cursor");
			setCursor(customCursor);
			setState(STATE_NONE);
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, customCursor);
		}
			break;
		case CREATE_UTILITY_NODE: {
			customCursor = toolkit.createCustomCursor(iconController
					.getUtilityNodeCursor().getImage(), new Point(0, 0),
					"Cursor");
			setCursor(customCursor);
			setState(STATE_NONE);
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, customCursor);
		}
			break;
		case CREATE_CONTEXT_NODE: {
			customCursor = toolkit.createCustomCursor(iconController
					.getContextNodeCursor().getImage(), new Point(0, 0),
					"Cursor");
			setCursor(customCursor);
			setState(STATE_NONE);
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, customCursor);
		}
			break;
		case CREATE_INPUT_NODE: {
			customCursor = toolkit
					.createCustomCursor(iconController.getInputNodeCursor()
							.getImage(), new Point(0, 0), "Cursor");
			setCursor(customCursor);
			setState(STATE_NONE);
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, customCursor);
		}
			break;
		case CREATE_RESIDENT_NODE: {
			customCursor = toolkit.createCustomCursor(iconController
					.getResidentNodeCursor().getImage(), new Point(0, 0),
					"Cursor");
			setCursor(customCursor);
			setState(STATE_NONE);
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, customCursor);
		}
			break;
		case CREATE_ORDINARYVARIABLE_NODE: {
			customCursor = toolkit.createCustomCursor(iconController
					.getOvariableNodeCursor().getImage(), new Point(0, 0),
					"Cursor");
			setCursor(customCursor);
			setState(STATE_NONE);
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, customCursor);
		}
			break;
		case CREATE_EDGE: {
			customCursor = toolkit.createCustomCursor(iconController
					.getLineCursor().getImage(), new Point(0, 0), "Cursor");
			setCursor(customCursor);
			setState(STATE_CONNECT_COMP);
			// by young4
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, customCursor);
		}
			break;
		case NONE:// by young2
		{
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			setState(STATE_NONE);

			// by young4
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, new Cursor(
					Cursor.MOVE_CURSOR));
			this.setShapeStateAll(STATE_NONE, null);

		}
			break;
		case ADD_PLUGIN_NODE: {
			if (this.getNodeDataTransferObject() == null
					|| this.getNodeDataTransferObject().getCursorIcon() == null) {
				customCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
			} else {
				customCursor = toolkit.createCustomCursor(
						this.getNodeDataTransferObject().getCursorIcon()
								.getImage(), new Point(0, 0), "Cursor");
			}
			setCursor(customCursor);
			setState(STATE_NONE);
			this.setShapeStateAll(UShape.STATE_CHANGECURSOR, customCursor);
		}
			break;
		default:
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			break;
		}

		this.action = action;
	}

	/**
	 * Get the action to be taken.
	 * 
	 * @return The action to be taken.
	 */
	public GraphAction getAction() {
		return action;
	}

	/**
	 * This method literally re-generates the UShape instance for a given
	 * newNode parameter. This is useful when we temporally destroy a
	 * GraphPane's content and want to re-generate it again (e.g. compiling a
	 * network and showing the compilation pane, and then turning back), or
	 * loading a network from file.
	 * 
	 * @param newNode
	 */
	public void createNode(Node newNode) {

		UShape shape = null;

		if (newNode instanceof IPluginNode) {
			try {
				shape = this.getPluginNodeManager()
						.getPluginNodeInformation(newNode.getClass())
						.getShapeBuilder().build().getUShape(newNode, this);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			}
		}

		// TODO stop using if-instanceof structure and start using object
		// binding to a UShape builder.
		if (shape == null) {
			// if we could not find a plugin node, start testing ordinal nodes
			if (newNode instanceof ContinuousNode) {
				shape = new UShapeProbabilisticNode(this, newNode,
						(int) newNode.getPosition().x,
						(int) newNode.getPosition().y, newNode.getWidth(),
						newNode.getHeight());
			} else if (newNode instanceof ProbabilisticNode) {
				shape = new UShapeProbabilisticNode(this, newNode,
						(int) newNode.getPosition().x,
						(int) newNode.getPosition().y, newNode.getWidth(),
						newNode.getHeight());
			} else if (newNode instanceof DecisionNode) {
				shape = new UShapeDecisionNode(this, newNode,
						(int) newNode.getPosition().x,
						(int) newNode.getPosition().y, newNode.getWidth(),
						newNode.getHeight());
			} else if (newNode instanceof UtilityNode) {
				shape = new UShapeUtilityNode(this, newNode,
						(int) newNode.getPosition().x,
						(int) newNode.getPosition().y, newNode.getWidth(),
						newNode.getHeight());
			}
		}

		if (shape != null) {
			try {
				addShape(shape);
				shape.setState(UShape.STATE_SELECTED, null);
			} catch (NullPointerException e) {
				throw new RuntimeException(
						"Could not find or set a shape for node: "
								+ newNode.getName(), e);
			}
		}
	}

	public void resizeAllToFitText() {
		int n = this.getComponentCount();

		for (int i = 0; i < n; i++) {
			UShape shape = (UShape) this.getComponent(i);
			shape.resizeToFitText();
			shape.repaint();
		}

		updateLines();

		setShapeStateAll(UShape.STATE_NONE, null);
	}

	public void compiled(boolean reset, Node selectedNode) {
		setPaneMode(PANEMODE_COMPILE);

		this.removeAll();

		Node n;

		UShape shape = null;

		// Load all nodes.
		for (int i = 0; i < nodeList.size(); i++) {
			n = nodeList.get(i);

			createNode(n);

			if (reset == true && n instanceof ProbabilisticNode) {
				shape = getNodeUShape(n);
			}

			if (n instanceof ContinuousNode || n instanceof ProbabilisticNode) {

				shape = getNodeUShape(n);
				if (shape != null) {
					shape.shapeTypeChange(UShapeProbabilisticNode.STYPE_BAR);
					shape.setState(UShape.STATE_RESIZED, null);
				} else {
					Debug.println(getClass(),
							"Warning. Could not extract shape from getNodeUShape("
									+ n + ")");
				}
			}

		}

		// Load all Edges
		for (int i = 0; i < edgeList.size(); i++) {
			Edge e = edgeList.get(i);

			if (getNodeUShape(e.getOriginNode()) != null
					&& getNodeUShape(e.getDestinationNode()) != null) {
				UShapeLine line = new UShapeLine(this,
						getNodeUShape(e.getOriginNode()),
						getNodeUShape(e.getDestinationNode()));
				line.setEdge(e);
				// by young 1/23/2010
				line.setLearningLineSelection(false);
				addShape(line);
			}
		}

		// by young4
		setAction(GraphAction.NONE);
		setShapeStateAll(UShape.STATE_NONE, null);
		fitCanvasSizeToAllUShapes();

		if (selectedNode != null) {
			shape = getNodeUShape(selectedNode);
			if (shape != null) {
				shape.setState(UShape.STATE_SELECTED, null);
			}
		}
	}

	static public int iUpdate = 0;

	public void update() {
		setPaneMode(PANEMODE_NONE);

		this.removeAll();

		Node n;
		Edge e;
		UShape shape = null;

		// Load all nodes.
		for (int i = 0; i < nodeList.size(); i++) {
			n = nodeList.get(i);
			n.updateLabel();

			// create node
			createNode(n);

			if (n instanceof ContinuousNode || n instanceof ProbabilisticNode
					|| n instanceof IPluginNode) {

				shape = getNodeUShape(n);

				if (shape != null) {
					shape.shapeTypeChange(UShapeProbabilisticNode.STYPE_NONE);
					shape.setState(UShape.STATE_RESIZED, null);
				}
			}
		}

		// Load all Edges
		for (int i = 0; i < edgeList.size(); i++) {
			e = edgeList.get(i);

			// createLine
			if (getNodeUShape(e.getOriginNode()) != null
					&& getNodeUShape(e.getDestinationNode()) != null) {
				UShapeLine line = new UShapeLine(this,
						getNodeUShape(e.getOriginNode()),
						getNodeUShape(e.getDestinationNode()));
				line.setEdge(e);
				// line.setUseSelection();
				// by young 1/23/2010
				line.setLearningLineSelection(this.isToUseSelectionForLines());
				addShape(line);
			}
		}

		// by young4
		setAction(GraphAction.NONE);
		setShapeStateAll(UShape.STATE_NONE, null);
		fitCanvasSizeToAllUShapes();

		if (controller != null) {
			Node selectedNode = controller.getSelectedNode();
			if (selectedNode != null) {
				UShape selectedUshape = this.getNodeUShape(selectedNode);
				if (selectedUshape != null) {
					selectedUshape.setState(UShape.STATE_SELECTED, null);
				}
			}
		}
	}

	public void resetGraph() {

		if (controller == null)
			return;

		edgeList = controller.getGraph().getEdges();
		nodeList = controller.getGraph().getNodes();

		action = GraphAction.NONE;

		JMenuItem item = new JMenuItem(resource.getString("properties"));
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				controller
						.showExplanationProperties((ProbabilisticNode) getSelected());
			}
		});

		popup.add(item);

		update();
		repaint();
	}

	public void updateAllNodesName() {
		if (getTextOutputMode() == TEXTOUTPUTMODEMODE_USE_NAME)
			useNameAllShape();
		else if (getTextOutputMode() == TEXTOUTPUTMODEMODE_USE_DESC)
			useDescAllShape();
	}

	public void useNameAllShape() {
		setTextOutputMode(TEXTOUTPUTMODEMODE_USE_NAME);
		controller.getScreen().getEvidenceTree()
				.setTextOutputMode(TEXTOUTPUTMODEMODE_USE_NAME);

		int n = this.getComponentCount();
		for (int i = 0; i < n; i++) {
			UShape shape = (UShape) this.getComponent(i);

			Node node = shape.getNode();
			if (node != null)
				shape.setLabel(node.getName());
		}

		repaint();
	}

	public void useDescAllShape() {
		setTextOutputMode(TEXTOUTPUTMODEMODE_USE_DESC);
		controller.getScreen().getEvidenceTree()
				.setTextOutputMode(TEXTOUTPUTMODEMODE_USE_DESC);

		int n = this.getComponentCount();
		for (int i = 0; i < n; i++) {
			UShape shape = (UShape) this.getComponent(i);

			Node node = shape.getNode();
			if (node != null)
				shape.setLabel(node.getDescription());
		}

		repaint();
	}

	public void resetPopup() {
		popup.removeAll();

		JMenuItem item1 = new JMenuItem("View Name");
		item1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				useNameAllShape();
			}
		});

		JMenuItem item2 = new JMenuItem("View Description");
		item2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				useDescAllShape();
			}
		});

		popup.add(item1);
		popup.add(item2);
	}

	public UShapeLine onDrawConnectLineReleased(UShape shapeParent, int x, int y) {
		UShapeLine line = super.onDrawConnectLineReleased(shapeParent, x, y);

		if (line != null) {
			Edge e = new Edge(line.getSource().getNode(), line.getTarget()
					.getNode());
			if (e != null) {
				if (insertEdge(e) == true) {
					line.setEdge(e);
					line.setLearningLineSelection(this
							.isToUseSelectionForLines());
				} else {
					delShape(line);
					repaint();
				}
			}
		}

		return line;
	}

	public void onShapeChanged(UShape s) {
		if (s instanceof UShapeState) {
			Node n = ((UShape) s.getParent()).getNode();
			controller.getScreen().getEvidenceTree()
					.selectTreeItemByState(n, s.getName());
			((UShapeProbabilisticNode) s.getParent()).update(s.getName());
		} else if (s instanceof UShapeLine) {

		} else if ((s instanceof IPluginUShape)
				|| (s.getNode() instanceof IPluginNode)) {
			// this is a node inserted by plugin infrastructure

			// Obtains the correct panel builder for currently selected node
			// TODO find a better way to couple the node and its builder without
			// messing up the draw/Node/GUI relationship
			IProbabilityFunctionPanelBuilder builder = null;
			try {
				builder = this.getPluginNodeManager()
						.getPluginNodeInformation(s.getNode().getClass())
						.getProbabilityFunctionPanelBuilder();
			} catch (Exception e) {
				Debug.println(this.getClass(),
						"Could not restore the node panel builder for "
								+ s.getNode().getName(), e);
			}
			if (builder != null) {
				// notify the probability function panel that the current owner
				// (node) is different
				builder.setProbabilityFunctionOwner(s.getNode());
				this.controller.getScreen().showProbabilityDistributionPanel(
						builder);
			}
		} else {
			showCPT(s.getNode());
		}
	}

	public void onShapeDeleted(UShape s) {
		if (controller != null) {
			if (s instanceof UShapeLine)
				controller.deleteSelected(((UShapeLine) s).getEdge());
			else {
				controller.deleteSelected(s.getNode());
			}
		} else if (net != null) {
			if (s instanceof UShapeLine) {
				net.removeEdge(((UShapeLine) s).getEdge());
			} else {
				net.removeNode(s.getNode());
			}
		}
	}

	public void onSelectionChanged() {
		if (controller == null)
			return;

		controller.unselectAll();

		int n = this.getComponentCount();
		for (int i = 0; i < n; i++) {
			UShape shape = (UShape) this.getComponent(i);

			if (shape.getState() == UShape.STATE_SELECTED
					&& shape.getNode() != null) {
				controller.selectNode(shape.getNode());

				if (controller.getScreen().getEvidenceTree() != null)
					controller.getScreen().getEvidenceTree()
							.selectTreeItemByNode(shape.getNode());
			}
		}
	}

	/**
	 * This object is used by GraphPane in order to temporally store all
	 * informations about a node loaded by plugins, between the moment that a
	 * user clicks the "add" button and the moment that the node is actually
	 * created and inserted into canvas. CAUTION: set this before calling
	 * {@link #setAction(GraphAction)}
	 * 
	 * @return the nodeClassDataTransferObject
	 */
	public INodeClassDataTransferObject getNodeDataTransferObject() {
		return nodeClassDataTransferObject;
	}

	/**
	 * This object is used by GraphPane in order to temporally store all
	 * informations about a node loaded by plugins, between the moment that a
	 * user clicks the "add" button and the moment that the node is actually
	 * created and inserted into canvas. CAUTION: set this before calling
	 * {@link #setAction(GraphAction)}
	 * 
	 * @param nodeClassDataTransferObject
	 *            the nodeClassDataTransferObject to set
	 */
	protected void setNodeDataTransferObject(
			INodeClassDataTransferObject nodeClassDataTransferObject) {
		this.nodeClassDataTransferObject = nodeClassDataTransferObject;
	}

	/**
	 * This object manages plugin-loaded nodes.
	 * 
	 * @return the pluginNodeManager
	 */
	protected CorePluginNodeManager getPluginNodeManager() {
		return pluginNodeManager;
	}

	/**
	 * This object manages plugin-loaded nodes.
	 * 
	 * @param pluginNodeManager
	 *            the pluginNodeManager to set
	 */
	protected void setPluginNodeManager(CorePluginNodeManager pluginNodeManager) {
		this.pluginNodeManager = pluginNodeManager;
	}

	/**
	 * {@link #update()} will set {@link UShapeLine#getUseSelection()} to same
	 * value;
	 * 
	 * @see #update()
	 * @see {@link UShapeLine#getUseSelection()}
	 * @return the toUseSelectionForLines
	 */
	public boolean isToUseSelectionForLines() {
		return toUseSelectionForLines;
	}

	/**
	 * {@link #update()} will set {@link UShapeLine#getUseSelection()} to same
	 * value;
	 * 
	 * @see #update()
	 * @see {@link UShapeLine#getUseSelection()}
	 * @param toUseSelectionForLines
	 *            the toUseSelectionForLines to set
	 */
	public void setToUseSelectionForLines(boolean toUseSelectionForLines) {
		this.toUseSelectionForLines = toUseSelectionForLines;
	}

}