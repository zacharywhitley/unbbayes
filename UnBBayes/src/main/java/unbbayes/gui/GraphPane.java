/*
 *  UnbBayes
 *  Copyright (C) 2002 Universidade de Bras�lia
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
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import unbbayes.controller.IconController;
import unbbayes.controller.NetworkController;
import unbbayes.draw.UCanvas;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeContextNode;
import unbbayes.draw.UShapeDecisionNode;
import unbbayes.draw.UShapeInputNode;
import unbbayes.draw.UShapeLine;
import unbbayes.draw.UShapeOOBNNode;
import unbbayes.draw.UShapeOrdinaryVariableNode;
import unbbayes.draw.UShapeProbabilisticNode;
import unbbayes.draw.UShapeResidentNode;
import unbbayes.draw.UShapeState;
import unbbayes.draw.UShapeUtilityNode;
import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNetwork;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.hybridbn.ContinuousNode;
import unbbayes.prs.id.DecisionNode;
import unbbayes.prs.id.UtilityNode;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.exception.CycleFoundException;
import unbbayes.prs.mebn.exception.MEBNConstructionException;
import unbbayes.prs.mebn.exception.MFragDoesNotExistException;
import unbbayes.util.extension.dto.INodeDataTransferObject;

/**
 * Essa classe � respons�vel por desenhar a rede Bayesiana ou a MFrag na tela.
 * Ela extende a classe <code>JPanel</code>. Ela tamb�m implementa as interfaces
 * MouseListener e MouseMotionListener, para poder tratar os eventos de mouse e
 * desenhar a rede Bayesiana.
 * 
 *@author Michael S. Onishi
 *@author Rommel N. Carvalho
 *@created 27 de Junho de 2001
 *@see JPanel Modified by Young, 4.13.2009
 * 
 */
public class GraphPane extends UCanvas implements MouseListener,
		MouseMotionListener {

	/** Serialization runtime version number */
	private static final long serialVersionUID = 0;

	// by young4
	// public NetworkController controller;
	public List<Edge> edgeList;

	// TODO Substituir essa lista de n�s por generics como est� acima com a
	// lista de Edge. Fazer isso em todo lugar que for necess�rio, at� que se
	// possa exluir o NodeList
	public ArrayList<Node> nodeList;

	public List<Node> selectedGroup;

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
	
	
	private INodeDataTransferObject nodeDataTransferObject;

	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.resources.GuiResources.class.getName());

	public GraphPane(JDialog dlg, ProbabilisticNetwork n) {
		super();
		net = n;

		edgeList = net.getEdges();
		nodeList = net.getNodes();

		this.setSize(800, 600);

		graphDimension = new Dimension(1500, 1500);
		visibleDimension = new Dimension(0, 0);
		action = GraphAction.NONE;

		update();
	}

	/**
	 * O construtor � respons�vel por iniciar todas as vari�veis que ser�o
	 * utilizadas por essa classe para que se possa desenhar a rede Bayesiana.
	 * 
	 *@param controlador
	 *            o controlador (<code>TControladorTelaPrincipal</code>)
	 *@param graphViewport
	 *            a tela, (<code>TViewport</code>), onde ser� inserida essa
	 *            classe
	 */
	public GraphPane(NetworkController controller, JViewport graphViewport) {
		super();

		this.controller = controller;
		this.graphViewport = graphViewport;
		this.setSize(800, 600);

		edgeList = controller.getGraph().getEdges();
		nodeList = controller.getGraph().getNodes();

		selectedGroup = new ArrayList<Node>();
		graphDimension = new Dimension(1500, 1500);
		visibleDimension = new Dimension(0, 0);
		action = GraphAction.NONE;

		graphViewport.setViewSize(visibleDimension);

	}

	/**
	 * Seta o atributo graphDimension (tamanho da rede Bayesiana) do objeto da
	 * classe GraphPane
	 * 
	 *@param graphDimension
	 *            O novo valor do tamanho da rede (<code>Dimension</code>)
	 *@see Dimension
	 */
	public void setGraphDimension(Dimension graphDimension) {
		this.graphDimension = graphDimension;
	}

	/**
	 * Retorna o objeto selecionado (<code>Object</code>), que pode ser um
	 * <code>Node</code> ou <code>Edge</code>
	 * 
	 *@return O valor do <code>Object</code> selected
	 *@see Object
	 *@see Node
	 *@see Edge
	 */
	public Object getSelected() {
		return getSelectedShapesNode();
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
	 *@return O valor do <code>List</code> selectedGroup
	 *@see List
	 *@see Node
	 *@see Edge
	 */
	public List<Node> getSelectedGroup() {
		selectedGroup.clear();

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
	 *@return A <code>Dimension</code> graphDimension
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
	 *@return True como valor do focusTransversable (m�todo necess�rio para que
	 *         se possa tratar evento de tecla)
	 */
	public boolean isFocusable() {
		return true;
	}

	/**
	 * Pega o atributo graphViewport (<code>JViewport</code>) do objeto da
	 * classe GraphPane
	 * 
	 *@return O graphViewport (container onde essa classe se encontra inserida)
	 *@see JViewport
	 */
	public JViewport getGraphViewport() {
		return this.graphViewport;
	}

	/**
	 * Pega o maior ponto (<code>Point2D.Double</code>) do objeto da classe
	 * GraphPane
	 * 
	 *@return O maior ponto (x,y) existente nessa rede Bayesiana
	 *@see Point2D.Double
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

		if (controller != null)
			if (controller.getGraph() instanceof SingleEntityNetwork) {
				controller.createTable(newNode);
			}
	}

	/**
	 * M�todo respons�vel por tratar o evento de clique no bot�o do mouse
	 * 
	 *@param e
	 *            O <code>MouseEvent</code>
	 *@see MouseEvent
	 */
	public void mouseClicked(MouseEvent e) {

		if (SwingUtilities.isLeftMouseButton(e)) {
			Node newNode = null;

			switch (getAction()) {

			case CREATE_CONTINUOUS_NODE: {
				newNode = controller.insertContinuousNode(e.getX(), e.getY());
				UShapeProbabilisticNode shape = new UShapeProbabilisticNode(
						this, newNode, (int) newNode.getPosition().x
								- newNode.getWidth() / 2, (int) newNode
								.getPosition().y
								- newNode.getHeight() / 2, newNode.getWidth(),
						newNode.getHeight());
				addShape(shape);
				shape.setState(UShape.STATE_SELECTED, null);
				showCPT(newNode);
			}
				break;
			case CREATE_PROBABILISTIC_NODE: {
				newNode = controller
						.insertProbabilisticNode(e.getX(), e.getY());
				UShapeProbabilisticNode shape = new UShapeProbabilisticNode(
						this, newNode, (int) newNode.getPosition().x
								- newNode.getWidth() / 2, (int) newNode
								.getPosition().y
								- newNode.getHeight() / 2, newNode.getWidth(),
						newNode.getHeight());
				addShape(shape);
				shape.setState(UShape.STATE_SELECTED, null);
				showCPT(newNode);

			}
				break;
			case CREATE_DECISION_NODE: {
				newNode = controller.insertDecisionNode(e.getX(), e.getY());
				UShapeDecisionNode shape = new UShapeDecisionNode(this,
						newNode, (int) newNode.getPosition().x
								- newNode.getWidth() / 2, (int) newNode
								.getPosition().y
								- newNode.getHeight() / 2, newNode.getWidth(),
						newNode.getHeight());
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
					JOptionPane.showMessageDialog(controller.getScreen()
							.getMebnEditionPane(), resource
							.getString("withoutMFrag"), resource
							.getString("operationError"),
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
					JOptionPane.showMessageDialog(controller.getScreen()
							.getMebnEditionPane(), resource
							.getString("withoutMFrag"), resource
							.getString("operationError"),
							JOptionPane.WARNING_MESSAGE);
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
					JOptionPane.showMessageDialog(controller.getScreen()
							.getMebnEditionPane(), resource
							.getString("withoutMFrag"), resource
							.getString("operationError"),
							JOptionPane.WARNING_MESSAGE);
				}
			}
				break;
			case CREATE_ORDINARYVARIABLE_NODE: {
				try {
					newNode = controller.getMebnController()
							.insertOrdinaryVariable(e.getX(), e.getY());
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
					JOptionPane.showMessageDialog(controller.getScreen()
							.getMebnEditionPane(), resource
							.getString("withoutMFrag"), resource
							.getString("operationError"),
							JOptionPane.WARNING_MESSAGE);
				}
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
			System.out.println("Left button released.");
		}

		if (SwingUtilities.isMiddleMouseButton(e)) {
			System.out.println("Middle button released.");
		}

		if (SwingUtilities.isRightMouseButton(e)) {
			System.out.println("Right button released.");

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
		} catch (MEBNConstructionException me) {
			JOptionPane.showMessageDialog(controller.getScreen()
					.getMebnEditionPane(), me.getMessage(), resource
					.getString("error"), JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (CycleFoundException cycle) {
			JOptionPane.showMessageDialog(controller.getScreen()
					.getMebnEditionPane(), cycle.getMessage(), resource
					.getString("error"), JOptionPane.ERROR_MESSAGE);
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

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
		case ADD_PLUGIN_NODE:
		{
			if (this.getNodeDataTransferObject() == null || this.getNodeDataTransferObject().getCursorIcon() == null) {
				customCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);
			} else {
				customCursor = toolkit.createCustomCursor(this.getNodeDataTransferObject().getCursorIcon().getImage(), new Point(0, 0),
					"Cursor");
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

	public void createNode(Node newNode) {
		UShape shape = null;

		if (newNode instanceof ContinuousNode) {
			shape = new UShapeProbabilisticNode(this, newNode, (int) newNode
					.getPosition().x, (int) newNode.getPosition().y, newNode
					.getWidth(), newNode.getHeight());
		} else if (newNode instanceof ProbabilisticNode) {
			shape = new UShapeProbabilisticNode(this, newNode, (int) newNode
					.getPosition().x, (int) newNode.getPosition().y, newNode
					.getWidth(), newNode.getHeight());
		} else if (newNode instanceof DecisionNode) {
			shape = new UShapeDecisionNode(this, newNode, (int) newNode
					.getPosition().x, (int) newNode.getPosition().y, newNode
					.getWidth(), newNode.getHeight());
		} else if (newNode instanceof UtilityNode) {
			shape = new UShapeUtilityNode(this, newNode, (int) newNode
					.getPosition().x, (int) newNode.getPosition().y, newNode
					.getWidth(), newNode.getHeight());
		} else if (newNode instanceof ContextNode) {
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
		} else if (newNode instanceof OOBNNodeGraphicalWrapper) {
			shape = new UShapeOOBNNode(this, newNode, (int) newNode
					.getPosition().x, (int) newNode.getPosition().y, newNode
					.getWidth(), newNode.getHeight());
		}

		addShape(shape);
		shape.setState(UShape.STATE_SELECTED, null);
	}

	public void compiled(boolean reset, Node selectedNode) {
		setPaneMode(PANEMODE_COMPILE);

		this.removeAll();

		Node n;
		Edge e;
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
				shape.shapeTypeChange(UShapeProbabilisticNode.STYPE_BAR);
				shape.setState(UShape.STATE_RESIZED, null);
			}

		}

		// Load all Edges
		for (int i = 0; i < edgeList.size(); i++) {
			e = edgeList.get(i);

			if (getNodeUShape(e.getOriginNode()) != null
					&& getNodeUShape(e.getDestinationNode()) != null) {
				UShapeLine line = new UShapeLine(this, getNodeUShape(e
						.getOriginNode()),
						getNodeUShape(e.getDestinationNode()));
				line.setEdge(e);
				line.setUseSelection(false);
				addShape(line);
			}
		}

		// by young4
		setAction(GraphAction.NONE);
		setShapeStateAll(UShape.STATE_NONE, null);
		fitCanvasSizeToAllUShapes();

		if (selectedNode != null) {
			shape = getNodeUShape(selectedNode);
			shape.setState(UShape.STATE_SELECTED, null);
		}
	}

	static public int iUpdate = 0;

	public void update() {
		setPaneMode(PANEMODE_NONE);

		System.out.println("update  = " + iUpdate++ + " ");

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

			if (n instanceof ContinuousNode || n instanceof ProbabilisticNode) {
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
				UShapeLine line = new UShapeLine(this, getNodeUShape(e
						.getOriginNode()),
						getNodeUShape(e.getDestinationNode()));
				line.setEdge(e);
				line.setUseSelection(false);
				addShape(line);
			}
		}

		// by young4
		setAction(GraphAction.NONE);
		setShapeStateAll(UShape.STATE_NONE, null);
		fitCanvasSizeToAllUShapes();

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
		controller.getScreen().getEvidenceTree().setTextOutputMode(
				TEXTOUTPUTMODEMODE_USE_NAME);

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
		controller.getScreen().getEvidenceTree().setTextOutputMode(
				TEXTOUTPUTMODEMODE_USE_DESC);

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
			controller.getScreen().getEvidenceTree().selectTreeItemByState(n,
					s.getName());
			((UShapeProbabilisticNode) s.getParent()).update(s.getName());
		} else if (s instanceof UShapeLine) {

		} else {
			showCPT(s.getNode());
		}
	}

	public void onShapeDeleted(UShape s) {
		if (controller == null)
			return;

		if (s instanceof UShapeLine)
			controller.deleteSelected(((UShapeLine) s).getEdge());
		else {
			controller.deleteSelected(s.getNode());
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
	 * This object is used by GraphPane in order to temporally
	 * store all informations about a node loaded by plugins, between
	 * the moment that a user clicks the "add" button and the moment
	 * that the node is actually created and inserted into canvas.
	 * CAUTION: set this before calling {@link #setAction(GraphAction)}
	 * @return the nodeDataTransferObject
	 */
	public INodeDataTransferObject getNodeDataTransferObject() {
		return nodeDataTransferObject;
	}

	/**
	 * This object is used by GraphPane in order to temporally
	 * store all informations about a node loaded by plugins, between
	 * the moment that a user clicks the "add" button and the moment
	 * that the node is actually created and inserted into canvas.
	 * CAUTION: set this before calling {@link #setAction(GraphAction)}
	 * @param nodeDataTransferObject the nodeDataTransferObject to set
	 */
	public void setNodeDataTransferObject(
			INodeDataTransferObject nodeDataTransferObject) {
		this.nodeDataTransferObject = nodeDataTransferObject;
	}
}