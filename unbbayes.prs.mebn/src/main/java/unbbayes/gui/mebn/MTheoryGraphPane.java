/*
 *  UnBBayes
 *  Copyright (C) 2002, 2008, 2011 Universidade de Brasilia - http://www.unb.br
 *
 *  This file is part of UnBBayes.
 *
 *  UnBBayes is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  UnBBayes is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with UnBBayes.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package unbbayes.gui.mebn;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JViewport;

import unbbayes.controller.NetworkController;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeContextNode;
import unbbayes.draw.UShapeInputNode;
import unbbayes.draw.UShapeLine;
import unbbayes.draw.UShapeMFrag;
import unbbayes.draw.UShapeOrdinaryVariableNode;
import unbbayes.draw.UShapeResidentNode;
import unbbayes.gui.GraphPane;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.mebn.ContextNode;
import unbbayes.prs.mebn.IResidentNode;
import unbbayes.prs.mebn.InputNode;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MFragNode;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.OrdinaryVariable;
import unbbayes.util.ResourceController;

/**
 * This pane is used to show the MTheory view. The nodes shown here a MFrags 
 * with their content.
 * 
 * @author Rommel Carvalho (rommel.carvalho@gmail.com)
 * @version 1.0 06/18/2011 - Created in order to have MTheory view
 *
 */
public class MTheoryGraphPane extends GraphPane {
	
	// TODO create scale capability allowing the selection and movements of nodes to keep working
	double scale = 1;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8581454085625952079L;
	/** The resource is not static, so that hotplug would become easier */
	private ResourceBundle resource;
	
	
	/**
	 * @param controller
	 * @param graphViewport
	 */
	public MTheoryGraphPane(NetworkController controller, JViewport graphViewport) {
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
		
		nodeList = new ArrayList<Node>();
		edgeList = new ArrayList<Edge>();
		
		init();
	}
	
	int x = 10;
	int y = 10;
	int size = 500;
	int nMfrags = 0;
	
	public void createMFrags() {
		
		MultiEntityBayesianNetwork mebn = (MultiEntityBayesianNetwork)controller.getNetwork();
		
		for (MFrag mfrag : mebn.getMFragList()) {
			createMFrag(mfrag);
		}
		
	}
	
	double largestHeight = 0;
	
	private void createMFrag(MFrag mfrag) {
		
		MFragNode node = new MFragNode(mfrag);
		node.setPosition(x, y);
		
		createMFrag(node);
		
	}
	
	private void createMFrag(MFragNode node) {
		
		MFrag mfrag = node.getMfrag();
		
		UShapeMFrag mfragShape = new UShapeMFrag(this, node, 
				(int) node.getPosition().x, 
				(int) node.getPosition().y, 
				node.getWidth(), node.getHeight());
		
		int titleHeight = mfragShape.heightTitle;
		
		double width = 0;
		double height = 0;
		
		UShape shape = null;
		for (Node newNode : mfrag.getNodes()) {
			
			if (newNode.getPosition().x + newNode.getWidth() > width) {
				width = newNode.getPosition().x + newNode.getWidth();
			}
			
			if (newNode.getPosition().y + newNode.getHeight() > height) {
				height = newNode.getPosition().y + newNode.getHeight();
			}
			
			int y = (int)newNode.getPosition().y;
			if (y < titleHeight) {
				y = 2 * titleHeight;
			} 
			
			if (newNode instanceof ContextNode) {
				shape = new UShapeContextNode(this, newNode, (int) newNode
						.getPosition().x, y, newNode
						.getWidth(), newNode.getHeight());
			} else if (newNode instanceof IResidentNode) {
				shape = new UShapeResidentNode(this, newNode, (int) newNode
						.getPosition().x, y, newNode
						.getWidth(), newNode.getHeight());
			} else if (newNode instanceof InputNode) {
				shape = new UShapeInputNode(this, newNode, (int) newNode
						.getPosition().x, y, newNode
						.getWidth(), newNode.getHeight());
			} else if (newNode instanceof OrdinaryVariable) {
				shape = new UShapeOrdinaryVariableNode(this, newNode, (int) newNode
						.getPosition().x, y, newNode
						.getWidth(), newNode.getHeight());
			} 

			if (shape != null) {
				try {
					mfragShape.add(shape);
				} catch (NullPointerException e) {
					throw new RuntimeException("Could not find or set a shape for node: " + newNode.getName(),e);
				}
			}
		}
		
		nodeList.add(node);
		addShape(mfragShape);
		
		for (Edge edge : mfrag.getEdges()) {
			// createLine
			if (getNodeUShape(edge.getOriginNode()) != null
					&& getNodeUShape(edge.getDestinationNode()) != null) {
				UShapeLine line = new UShapeLine(this, getNodeUShape(edge
						.getOriginNode()),
						getNodeUShape(edge.getDestinationNode()));
				line.setEdge(edge);
				line.setLearningLineSelection(this.isToUseSelectionForLines());
				mfragShape.add(line);
			}
		}
		
		node.setSize(width + titleHeight, height + titleHeight);
		
		mfragShape.setSize((int)(width + titleHeight), (int)(height + titleHeight));
		
		if (height + 2 * titleHeight > largestHeight) {
			largestHeight = height + 3 * titleHeight;
		}
		
		nMfrags++;
		
		if (nMfrags % 3 == 0) {
			y += largestHeight;
			x = titleHeight;
		} else {
			x += width + 2 * titleHeight;
		}
		
	}
	
	public void update() {
		List<Node> newList = new ArrayList<Node>();
		
		MultiEntityBayesianNetwork mebn = (MultiEntityBayesianNetwork)controller.getNetwork();
		
		for (MFrag mfrag : mebn.getMFragList()) {
			Node currentNode = null;
			for (Node node : nodeList) {
				if (node instanceof MFragNode && ((MFragNode)node).getMfrag() == mfrag) {
					currentNode = node;
					break;
				}
			}
			MFragNode node = new MFragNode(mfrag);
			if (currentNode != null) {
				node.setPosition(currentNode.getPosition().x, currentNode.getPosition().y);
			} else {
				node.setPosition(x, y);
			}
			
			newList.add(node);
		}
		
		nodeList.clear();
		this.removeAll();
		
		for (Node newNode : newList) {
			createNode(newNode);
		}
	}
	
	/**
	 * Overwrite just to scale, then call super class.
	 */
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
//		RenderingHints rh = new RenderingHints(
//			RenderingHints.KEY_TEXT_ANTIALIASING,
//			RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2.setRenderingHints(rh);
//		rh = new RenderingHints(
//			RenderingHints.KEY_ALPHA_INTERPOLATION,
//			RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
//		g2.setRenderingHints(rh);
//		rh = new RenderingHints(
//				RenderingHints.KEY_COLOR_RENDERING,
//				RenderingHints.VALUE_COLOR_RENDER_QUALITY);
//		g2.setRenderingHints(rh);
//		rh = new RenderingHints(
//				RenderingHints.KEY_RENDERING,
//				RenderingHints.VALUE_RENDER_QUALITY);
//		g2.setRenderingHints(rh);
//		rh = new RenderingHints(
//				RenderingHints.KEY_TEXT_ANTIALIASING,
//				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		g2.setRenderingHints(rh);
		g2.scale(scale, scale);
		
		super.paintComponent(g2);
	}
	
	/**
	 * This method is responsible to treat mouse button events
	 * 
	 *@param e
	 *            <code>MouseEvent</code>
	 *@see MouseEvent
	 */
	public void mouseClicked(MouseEvent e) {
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#insertEdge(unbbayes.prs.Edge)
	 */
	public boolean insertEdge(Edge edge) {
		// we are not going to insert any edge here
		return true;
	}
	
	boolean firstTime = true;
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#createNode(unbbayes.prs.Node)
	 */
	public void createNode(Node newNode) {
		if (newNode instanceof MFragNode) {
			createMFrag(((MFragNode)newNode));
		}
	}
	
	private void init() {
		if (firstTime) {
			createMFrags();
			firstTime = false;
		}
	}
	
	public List<UShapeMFrag> getShapes() {
		List<UShapeMFrag> shapes = new ArrayList<UShapeMFrag>();
		for (int i = 0; i < getComponents().length; i++) {
			if (getComponents()[i] instanceof UShapeMFrag) {
				UShapeMFrag shape = (UShapeMFrag)getComponents()[i];
				shapes.add(shape);
			}
		}
		return shapes;
	}

}
