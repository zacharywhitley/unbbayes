/*
 *  UnBBayes
 *  Copyright (C) 2002, 2009 Universidade de Brasilia - http://www.unb.br
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
package unbbayes.draw;
 
import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import unbbayes.gui.oobn.OOBNGraphPane;
import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.prs.Node;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.util.Debug;

public class UShapeOOBNNode extends UShape  
{       
	/**
	 * 
	 */
	private static final long serialVersionUID = -3394350011502329353L;

	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.oobn.resources.OOBNGuiResource");
	
	protected Ellipse2D ellipse;  
	protected Rectangle2D rect; 
	protected UShapeState stateShape; 
	protected int stateHeight = 18;
	protected int defaultWidth = (int)Node.DEFAULT_SIZE.getX();
	protected int defaultHeight = (int)Node.DEFAULT_SIZE.getY();
	
	private Color inputColor = Color.LIGHT_GRAY;
	private Color privateColor = Color.WHITE;
	private Color instanceColor = privateColor;
	private Color instanceInputColor = new Color(200,200,200);
	private Color instanceOutputColor = instanceColor;
	
	public static final String STYPE_INPUT 		= "Input";
	public static final String STYPE_OUTPUT		= "Output";
	public static final String STYPE_PRIVATE	= "Private";
	public static final String STYPE_INSTANCE	= "INSTANCE";
	public static final String STYPE_INSTANCE_OUTPUT = "INSTANCE OUTPUT";
	public static final String STYPE_INSTANCE_INPUT	 = "INSTANCE INPUT";
 
	public UShapeOOBNNode(UCanvas c, Node pNode, int x, int y, int w, int h)
	{
		super(c, pNode, x, y, w, h);  
		InitShape();	
		shapeTypeChange(((OOBNNodeGraphicalWrapper)getNode()).getWrappedNode().getType());
    }    
	 
	public void InitShape() 
	{
		ellipse = new Ellipse2D.Double(GAP,GAP,getWidth()-GAP*2-1,getHeight()-GAP*2-1); 
	} 
	
	public void shapeTypeChange(int t) 
	{  		
		if( t == IOOBNNode.TYPE_INPUT )
		{
			shapeTypeChange(STYPE_INPUT);
			this.setBackColor(inputColor);
		}
		else
		if( t == IOOBNNode.TYPE_OUTPUT )
		{
			shapeTypeChange(STYPE_OUTPUT);
			this.setBackColor(Color.YELLOW);
		}
		else
		if( t == IOOBNNode.TYPE_PRIVATE )
		{
			shapeTypeChange(STYPE_PRIVATE);
			this.setBackColor(privateColor);
		}
		else
		if( t == IOOBNNode.TYPE_INSTANCE )
		{
		 	shapeTypeChange(STYPE_INSTANCE);
		 	this.setBackColor(privateColor);
		}
		else
		if( t == IOOBNNode.TYPE_INSTANCE_INPUT )
		{
		 	shapeTypeChange(STYPE_INSTANCE_INPUT);
			this.setBackColor(instanceInputColor);
		}
		else
		if( t == IOOBNNode.TYPE_INSTANCE_OUTPUT )
		{
		 	shapeTypeChange(STYPE_INSTANCE_OUTPUT);
			this.setBackColor(instanceOutputColor);
		} 
	}
	
	public void shapeTypeChange(String s) 
	{  
		super.setShapeType(s);
			
		update();
	}	 
	
	public void update() 
	{ 
		//by young3
		updateNodeInformation();	
	 
		InitShape();
		createResizeBtn();
		removeTextBox();
		repaint();
		
		getCanvas().onShapeChanged(this);
	}
	   
	public void paintComponent(Graphics g) 
	{
		super.paintComponent(g); 
		
		InitShape();
		
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setPaint( new GradientPaint( getWidth()/2, getHeight(),  getBackColor(), 
				getWidth()/2, 0,Color.white, false));

		g2.fill(ellipse);
		g2.setColor(getDrawColor());
  	    g2.draw (ellipse);
  	    drawText(g); 
	}	    
	
	public boolean contain(double x, double y) 
	{ 
		if( ellipse != null )
			return ellipse.contains((double)(x-getGlobalX()), (double)(y-getGlobalY()));	
		
		return false;
	}
	
	public void mouseClicked(MouseEvent arg0) 
	{  
		if (SwingUtilities.isLeftMouseButton(arg0)) 
	    {
			if (arg0.getClickCount() == 1 && !arg0.isConsumed()) 
	        {	        
		//		switch (this.getAction()) {
		//		case NONE:
		//			this.setBMoveNode(true);
					//setCursor(new Cursor(Cursor.MOVE_CURSOR));
					try {
						OOBNNodeGraphicalWrapper node = (OOBNNodeGraphicalWrapper)getNode();
						
						this.describeOOBNNode(node);
						
						if (node != null) {
							if ( node.getWrappedNode().getType() == node.getWrappedNode().TYPE_INSTANCE_INPUT
							  || node.getWrappedNode().getType() == node.getWrappedNode().TYPE_INSTANCE_OUTPUT ){
								 // I do not want to make inner nodes selectable.
								 // so, return without changing status
								 return;
							}						
						}
					} catch (Exception t) {
						Debug.println(this.getClass(), "You clicked at a non-OOBN node", t);
					}
						
		//		default:
		//			break;
		//		}
	        }
			
	        if (arg0.getClickCount() == 2 && !arg0.isConsumed()) 
	        {
	        	System.out.println("handle double click.");
	        	
	        	arg0.consume();
	        	
	         	setState( STATE_WAIT_EDIT );
	        	
	        }
	    }
	        
	    if (SwingUtilities.isMiddleMouseButton(arg0)) 
	    {
	          System.out.println("Middle button released.");
	    }
	        
	    if (SwingUtilities.isRightMouseButton(arg0)) 
	    {
	       	System.out.println("Right button released.");
	       	
	    	// I'm not using e.isPoputrigger because it seems not to be working on Linux... 
			this.showNodeTypeChangePopup(arg0.getComponent(), arg0.getX(), arg0.getY());
				 
	    }
	} 
 
	/**
	 * Initializes the OOBN node edition popup menu
	 * (the menu which pops up when left-clicking an OOBN node)
	 */
	protected void setUpPopupMenu() {
		
		// menu item to set node as input node
		JMenuItem itemChangeNodeToInput = new JMenuItem(resource.getString("changeNodeToInput"));
		
		itemChangeNodeToInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				OOBNNodeGraphicalWrapper node = null;
				try {
					((OOBNGraphPane)getCanvas()).changeAllSelectedNodeType(IOOBNNode.TYPE_INPUT);
					Debug.println(this.getClass(), "I'm setting the node as an input");	
				 
				} catch (IllegalArgumentException iae) {
					JOptionPane.showMessageDialog(getCanvas(), iae.getMessage(), resource.getString("changeNodeToInput"), JOptionPane.ERROR_MESSAGE);
				} catch (Exception e) {
					Debug.println(this.getClass(), "The selected node does not look like a valid wrapped OOBN node", e);
					throw new IllegalArgumentException(e);
				}
							
			}
		});
		
		// menu item to set node as output node (which should be the default)
		JMenuItem itemChangeNodeToOutput = new JMenuItem(resource.getString("changeNodeToOutput"));
		itemChangeNodeToOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				OOBNNodeGraphicalWrapper node = null;
				try {
					
					((OOBNGraphPane)getCanvas()).changeAllSelectedNodeType(IOOBNNode.TYPE_OUTPUT);
					Debug.println(this.getClass(), "I'm setting the node as an output");	
				 
				} catch (Exception e) {
					Debug.println(this.getClass(), "The selected node does not look like a valid wrapped OOBN node", e);
					throw new IllegalArgumentException(e);
				}
							
			}
		});
		
		// menu item to set node as private node 
		JMenuItem itemChangeNodeToPrivate = new JMenuItem(resource.getString("changeNodeToPrivate"));
		itemChangeNodeToPrivate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				OOBNNodeGraphicalWrapper node = null;
				try {
					((OOBNGraphPane)getCanvas()).changeAllSelectedNodeType(IOOBNNode.TYPE_PRIVATE);
					Debug.println(this.getClass(), "I'm setting the node as private");	
			 
				} catch (Exception e) {
					Debug.println(this.getClass(), "The selected node does not look like a valid wrapped OOBN node", e);
					throw new IllegalArgumentException(e);
				}
							
			}
		});
		
		
		// add them all to the popup menu
		popup.add(itemChangeNodeToInput);
		popup.add(itemChangeNodeToOutput);
		popup.add(itemChangeNodeToPrivate);
				
		popup.setLabel(resource.getString("OOBNPopupMenuMessage"));
		popup.setToolTipText(resource.getString("OOBNPopupMenuTooltipMessage"));
	}
	
	public void showNodeTypeChangePopup(Component invoker, int x, int y) 
	{
		if ((((OOBNNodeGraphicalWrapper)getNode()).getType() & IOOBNNode.TYPE_INSTANCE) == 0) 
		{
			createPopupMenu();
			setUpPopupMenu();
			popup.setEnabled(true);
			popup.show(invoker, x, y);
		}	
	}
	
	/**
	 * If debug mode is on, this method writes to Debug a description of OOBN node
	 * @param node
	 */
	protected void describeOOBNNode(OOBNNodeGraphicalWrapper node) {
		try {
			Debug.println(this.getClass(), "Node " + node.getName() + " pressed.");
			Debug.println(this.getClass(), "Wrapped node name is " + node.getWrappedNode().getName());
			String[] types = {"0", "output", "private", "3", "input", "5", "6", "7" 
							 ,"instance", "Instance Output", "10", "11", "Instance Input"};
			Debug.println(this.getClass(), "Wrapped node type is " + types[node.getWrappedNode().getType()]);
			Debug.println(this.getClass(), "States are " + node.getWrappedNode().getStateNames());
			Debug.println(this.getClass(), "Original class node is " + ((node.getWrappedNode().getOriginalClassNode()!=null)?node.getWrappedNode().getOriginalClassNode().getName():"null"));
			Debug.println(this.getClass(), "Upper instance node is " + ((node.getWrappedNode().getUpperInstanceNode()!=null)?node.getWrappedNode().getUpperInstanceNode().getName():"null"));
			Debug.println(this.getClass(), "Parent class is " + ((node.getWrappedNode().getParentClass()!=null)?node.getWrappedNode().getParentClass().getClassName():"null"));
			
			if (node.getWrappedNode().getInnerNodes() != null) {
				for (IOOBNNode inner : node.getWrappedNode().getInnerNodes()) {
					Debug.println(this.getClass(), "Inner node is " + inner.getName() + " of type " + types[inner.getType()]);
				}
			}
			
			if (node.getInnerNodes() != null) {
				for (OOBNNodeGraphicalWrapper inner : node.getInnerNodes()) {
					Debug.println(this.getClass(), "Graphical Inner node is " + inner.getName() + " of type " + types[inner.getType()]);
				}
			}
			
			Debug.println(this.getClass(), "Parents are:");
			for (IOOBNNode parent : node.getWrappedNode().getOOBNParents()) {
				Debug.println(this.getClass(), "\t" + parent.getName());				
			}
			Debug.println(this.getClass(), "Children are:");
			for (IOOBNNode child : node.getWrappedNode().getOOBNChildren()) {
				Debug.println(this.getClass(), "\t" + child.getName());				
			}
			
		} catch (Exception t) {
			// do nothing
		}
	}
	
}
