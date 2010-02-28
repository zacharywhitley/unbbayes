/**
 * 
 */
package unbbayes.gui.oobn;
//by young
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;

import unbbayes.controller.oobn.OOBNClassController;
import unbbayes.draw.UShape;
import unbbayes.draw.UShapeFrame;
import unbbayes.draw.UShapeLine;
import unbbayes.draw.oobn.UShapeOOBNNode;
import unbbayes.gui.GraphPane;
import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNGraphPane extends GraphPane {
	
	/** Load resource file from this package */
	private static ResourceBundle resource = unbbayes.util.ResourceController.newInstance().getBundle(
			unbbayes.gui.oobn.resources.OOBNGuiResource.class.getName());
	

	private OOBNClassController controller = null;
	
	
	private JPopupMenu oobnOnNodePopup = null;
	
	
	private boolean bMoveNode = false;
	
	
	private Dimension visibleDimension = null;
	
	/**
	 * @param controller
	 * @param graphViewport
	 */
	protected OOBNGraphPane(OOBNClassController controller, JViewport graphViewport) {
		super(controller, graphViewport);
		// TODO Auto-generated constructor stub
		this.setController(controller);
	}
	
	/**
	 * @param controller
	 * @param graphViewport
	 */
	public static OOBNGraphPane newInstance(OOBNClassController controller, JViewport graphViewport) {
		OOBNGraphPane ret = new OOBNGraphPane( controller,  graphViewport);
				 	 
		ret.setOobnOnNodePopup(new JPopupMenu(resource.getString("OOBNPopupMenuMessage")));		
		ret.setUpPopupMenu();
		 		
		ret.setUpTransferHundler();
		
		//
		//by young: this function is removed
		//
		/*
		ret.setVisibleDimension(new Dimension());
		*/
		
		ret.setToolTipText(resource.getString("leftClickToChangeNodeType"));
		
		return ret;
	}
	
	//by young
	public void changeAllSelectedNodeType(int type) 
	{
		int n = this.getComponentCount();
		
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			((OOBNNodeGraphicalWrapper)shape.getNode()).getWrappedNode().setType(type);
    			((UShapeOOBNNode)shape).shapeTypeChange(type);

    		}
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
					changeAllSelectedNodeType(IOOBNNode.TYPE_INPUT);
					Debug.println(this.getClass(), "I'm setting the node as an input");
					/*
					 * by young
					node = (OOBNNodeGraphicalWrapper)getSelected();
					node.getWrappedNode().setType(IOOBNNode.TYPE_INPUT);
					Debug.println(this.getClass(), "I'm setting the node as an input");	
					update();*/
				} catch (IllegalArgumentException iae) {
					JOptionPane.showMessageDialog(getController().getScreen(), iae.getMessage(), resource.getString("changeNodeToInput"), JOptionPane.ERROR_MESSAGE);
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
					changeAllSelectedNodeType(IOOBNNode.TYPE_OUTPUT);
					Debug.println(this.getClass(), "I'm setting the node as an output");
					/*
					 * by young
					node = (OOBNNodeGraphicalWrapper)getSelected();
					node.getWrappedNode().setType(IOOBNNode.TYPE_OUTPUT);
					Debug.println(this.getClass(), "I'm setting the node as an output");	
					update();
					*/
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
					changeAllSelectedNodeType(IOOBNNode.TYPE_PRIVATE);
					Debug.println(this.getClass(), "I'm setting the node as private");
					/*
					 *by young
					 *
					node = (OOBNNodeGraphicalWrapper)getSelected();
					node.getWrappedNode().setType(IOOBNNode.TYPE_PRIVATE);
					Debug.println(this.getClass(), "I'm setting the node as private");	
					update();
					*/
				} catch (Exception e) {
					Debug.println(this.getClass(), "The selected node does not look like a valid wrapped OOBN node", e);
					throw new IllegalArgumentException(e);
				}
							
			}
		});
		
		
		// add them all to the popup menu
		this.getOobnOnNodePopup().add(itemChangeNodeToInput);
		this.getOobnOnNodePopup().add(itemChangeNodeToOutput);
		this.getOobnOnNodePopup().add(itemChangeNodeToPrivate);
		
		
		this.getOobnOnNodePopup().setLabel(resource.getString("OOBNPopupMenuMessage"));
		this.getOobnOnNodePopup().setToolTipText(resource.getString("OOBNPopupMenuTooltipMessage"));
	} 
 
    public void onShapeChanged( UShape s )
	{ 
    	if(s.getNode() instanceof OOBNNodeGraphicalWrapper) 
    	{
    		if( ((OOBNNodeGraphicalWrapper)s.getNode()).getWrappedNode().getType() == IOOBNNode.TYPE_INSTANCE ||
    			((OOBNNodeGraphicalWrapper)s.getNode()).getWrappedNode().getType() == IOOBNNode.TYPE_INSTANCE_INPUT ||
    			((OOBNNodeGraphicalWrapper)s.getNode()).getWrappedNode().getType() == IOOBNNode.TYPE_INSTANCE_OUTPUT )
    		{
    			
    		}
    		else
    			super.onShapeChanged(s);
    	}    	
 	}
    
	public UShape getUShape(IOOBNNode oobnNode) 
	{
	 	int size = this.getComponentCount();
    	for( int i = 0; i < size; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		if( shape.getNode() != null )
    		{
    			IOOBNNode wrapper = ((OOBNNodeGraphicalWrapper)shape.getNode()).getWrappedNode();
    			
    			if( wrapper  == oobnNode)
    				return shape;
    		}
    	}
    	
    	return null;
	}
    
	public void createNode( Node newNode )    {
		 UShape shape = null;
		 
		if(newNode instanceof OOBNNodeGraphicalWrapper) {
			IOOBNNode wrapper = ((OOBNNodeGraphicalWrapper)newNode).getWrappedNode();
			
			if( wrapper.getType() == IOOBNNode.TYPE_INSTANCE )	{	
				
				shape = new UShapeFrame(this, newNode, (int)newNode.getPosition().x, (int)newNode.getPosition().y, (int)newNode.getWidth(), (int)newNode.getHeight());
				addShape( shape );	
				shape.setState(UShape.STATE_NONE, null);
				
			} else {		 
				
				IOOBNNode upperInstanceNode = wrapper.getUpperInstanceNode();
				UShape shapeFrame = null;
		 
				shapeFrame = getUShape( upperInstanceNode );
					
				shape = new UShapeOOBNNode(this, newNode, (int)newNode.getPosition().x, (int)newNode.getPosition().y, newNode.getWidth(), newNode.getHeight());
				 	
				if ( shapeFrame != null )	{
					//Point locFrame = shapeFrame.getLocation();
					//Point loc = shape.getLocation();
					
					//shape.move(loc.x - locFrame.x, loc.y - locFrame.y);					
					shapeFrame.add(shape);
					shapeFrame.setState(UShape.STATE_NONE, null);
					shape.setState(UShape.STATE_NONE, null);
				} else {
					addShape( shape );
					shape.setState(UShape.STATE_NONE, null);
				}
			}
 		}
    	
    } 
	
	public void update()
	{
		this.removeAll();
		
		Node n; 
		Edge e;
		UShape shape = null;
		Point2D defaultStartPos = new Point2D.Double(0,0);
		 
		// Load all nodes.
		for (int i = 0; i < nodeList.size(); i++) 
		{
			n = nodeList.get(i);
			n.updateLabel();
  	
			createNode( n );
		 
		}	
		
		// Load all Edges
		for (int i = 0; i < edgeList.size(); i++) 
		{
			e = edgeList.get(i);
			
			if(getNodeUShape(e.getOriginNode()) != null && getNodeUShape(e.getDestinationNode()) != null )
			{
				UShapeLine line = new UShapeLine(this, getNodeUShape(e.getOriginNode()), getNodeUShape(e.getDestinationNode()) );
				line.setEdge(e);
				line.setUseSelection(false);
				addShape( line );
			}
		}	
		
		//by young4
		setShapeStateAll(UShape.STATE_NONE, null);
		fitCanvasSizeToAllUShapes();
		 
	} 

	/**
	 * Initializes the TransferHundler, which is responsible for
	 * drag n drop / copy n paste operations
	 */
	public void setUpTransferHundler() {
		this.setTransferHandler(new TransferHandler() {

//			/* (non-Javadoc)
//			 * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
//			 */
//			@Override
//			public boolean canImport(TransferSupport support) {
//				// check if this is local java object
//				try{
//					boolean ret = support.isDataFlavorSupported(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
//					return ret;
//				} catch (Exception e) {
//					try{
//						Debug.println(this.getClass(), "Could not support data transfer from " + support.getComponent().getName(), e);
//					} catch (Exception newe) {
//						Debug.println(this.getClass(), "Could not support data transfer using drag/drop or copy/paste", e);
//					}
//					return false;
//				}
//			}
			
			

			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
			 */
			@Override
			public boolean canImport(JComponent comp,
					DataFlavor[] transferFlavors) {				
				// check if this is at least a local java object
				try{
					for (DataFlavor dataFlavor : transferFlavors) {
						if (java.awt.datatransfer.DataFlavor.javaJVMLocalObjectMimeType.contains(dataFlavor.getSubType())) {
							return true;
						}							
					}
					return false;
				} catch (Exception e) {
					try{
						Debug.println(this.getClass(), "Could not support data transfer from " + comp.getName(), e);
					} catch (Exception newe) {
						Debug.println(this.getClass(), "Could not support data transfer using drag/drop or copy/paste", e);
					}
					return false;
				}
//				return true;
			}


			
			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
			 */
			@Override
			public boolean importData(JComponent comp, Transferable t) {
				Debug.println(this.getClass(), "Importing data from dragndrop: " + t.toString() + ", from component " + comp.getName());
				
			 
				//by young4
				//all selected nodes be unselected
				setShapeStateAll(UShape.STATE_NONE, null);
				
				// TODO finish this
				if (!this.canImport(comp,t.getTransferDataFlavors())) {
					return false;
				}
				 
				try {
					// obtains the location to insert oobn instance
//					DropLocation location = support.getDropLocation();
					Point location = comp.getMousePosition();
					
					// extract the oobn class
					Transferable transfer = t;
					IOOBNClass oobnClass = (IOOBNClass)transfer.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
					
					if (oobnClass == null) {
						Debug.println(this.getClass(), "Nothing was extracted from drag n drop");
						return false;
					} 
					 
					// insert new oobn class' instance
					getController().insertInstanceNode(oobnClass, location.getX(), location.getY());
					
					Debug.println(this.getClass(), "It seems that we added the class " + oobnClass.getClassName() 
							+ "at position (" + location.getX() + "," + location.getY() + ")");
					 
					
				} catch (Exception e) {
					JOptionPane.showMessageDialog(getController().getScreen(), e.getMessage(), resource.getString("CannotDragNDrop"), JOptionPane.ERROR_MESSAGE);
					throw new RuntimeException(resource.getString("CannotDragNDrop") , e);
				}
				
				
				// delegate to upper class
				super.importData(comp, t);
				
				// update whole panel (instead of ordinal update, which only updates a small part of screen)
				update();	
				 
				// if this code is reached, no problem was found
				return true;
			
			}



//			/* (non-Javadoc)
//			 * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
//			 */
//			@Override
//			public boolean importData(TransferSupport support) {
//				
//				Debug.println(this.getClass(), "Importing data from dragndrop: " + support.toString());
//				
//				
//				// TODO finish this
//				if (!this.canImport(support)) {
//					return false;
//				}
//				
//				try {
//					// obtains the location to insert oobn instance
//					DropLocation location = support.getDropLocation();
//					
//					
//					// extract the oobn class
//					Transferable transfer = support.getTransferable();
//					IOOBNClass oobnClass = (IOOBNClass)transfer.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
//					
//					if (oobnClass == null) {
//						Debug.println(this.getClass(), "Nothing was extracted from drag n drop");
//						return false;
//					} 
//					
//					// insert new oobn class' instance
//					getController().insertInstanceNode(oobnClass, location.getDropPoint().getX(), location.getDropPoint().getY());
//					
//					Debug.println(this.getClass(), "It seems that we added the class " + oobnClass.getClassName() 
//							+ "at position (" + location.getDropPoint().getX() + "," + location.getDropPoint().getY() + ")");
//					
//									
//					
//				} catch (Exception e) {
//					JOptionPane.showMessageDialog(getController().getScreen(), e.getMessage(), resource.getString("CannotDragNDrop"), JOptionPane.ERROR_MESSAGE);
//					throw new RuntimeException(resource.getString("CannotDragNDrop") , e);
//				}
//				
//				
//				// delegate to upper class
//				super.importData(support);
//				
//				// update whole panel (instead of ordinal update, which only updates a small part of screen)
//				repaint();	
//				
//				// if this code is reached, no problem was found
//				return true;
//			}
			
			
		});
		
	}
	

	/* (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#paint(java.awt.Graphics)
	 */
	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		try{
			super.paint(g);
		} catch (Exception e) {
			Debug.println(this.getClass(), "Failure on paint", e);
		}
	}

	

	

	/* (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#mousePressed(java.awt.event.MouseEvent)
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		// I had to overwrite this method just because I had to overwrite getRectangleRepresentation,
		// but the visibility of bMoveNode did not permit me to do so
		// So, I had to manage bMoveNode locally
		
		// also, this method is used to change details of mouse event behavior, like selection of nodes
		
		//
		// by young : this function moved to UShapeOOBNNode, because node's action should work in UShapeOOBNNode
		// 
		/*
		switch (this.getAction()) {
		case NONE:
			this.setBMoveNode(true);
			//setCursor(new Cursor(Cursor.MOVE_CURSOR));
			try {
				OOBNNodeGraphicalWrapper node = (OOBNNodeGraphicalWrapper)getNode(e.getX(), e.getY());
				
				this.describeOOBNNode(node);
				
				if (node != null) {
					if ((node.getWrappedNode().getType() | node.getWrappedNode().TYPE_INSTANCE ) != 0){
						// the selected node is not a instance node.
						// since it is a node w/ probabilities, we can show name/description/table
						this.controller.getScreen().getNetWindowEdition().getJspTable().setVisible(true);	
						this.controller.getScreen().getNetWindowEdition().getJtbState().setVisible(true);	
						this.repaint();
						if ( node.getWrappedNode().getType() == node.getWrappedNode().TYPE_INSTANCE_INPUT
								  || node.getWrappedNode().getType() == node.getWrappedNode().TYPE_INSTANCE_OUTPUT ){
							 // I do not want to make inner nodes selectable either.
							 // so, return without changing status
							 return;
						} 
					} else {
						// the selected node is not a instance node.
						// since it is a node w/ probabilities, we can show name/description/table
						this.controller.getScreen().getNetWindowEdition().getJspTable().setVisible(true);	
						this.controller.getScreen().getNetWindowEdition().getJtbState().setVisible(true);	
						this.repaint();
					}
					
				} else {
					// if no node is selected, I dont want the name/description edition pane to be visible either.
					this.controller.getScreen().getNetWindowEdition().getJspTable().setVisible(false);
					this.controller.getScreen().getNetWindowEdition().getJtbState().setVisible(false);	
					this.repaint();
				}
			} catch (Exception t) {
				Debug.println(this.getClass(), "You clicked at a non-OOBN node", t);
			}
				
		default:
			break;
		}*/
		
		// do the default behavior as well		
		super.mousePressed(e);
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
		// I'm overwriting this method to add special node treatment for OOBN node
		// when a mouse is clicked over such node.
		
		
		//
		// by young : this function moved to UShapeOOBNNode, because node's action should work in UShapeOOBNNode
		// 
		/*
		switch (this.getAction()) {
		case NONE:
			// this case was added just because to make bMoveNode as local variable,
			// in order to easily overwrite getRectangleRepresentation.
			this.setBMoveNode(false);
			setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			
			break;
		default:
			break;
		}
		
		
		// if the event is not what we intended to overwrite, pass to upper class
		super.mouseReleased(e);
		
		// I'm not using e.isPoputrigger because it seems not to be working on Linux...
		if ((e.getModifiers() == MouseEvent.BUTTON3_MASK) && (getSelected() != null)) {
			// we should only trigger such event if the selected one is an OOBN Node
			if (this.getSelected() instanceof OOBNNodeGraphicalWrapper) {
				// only allow to popup if selected node is not an instance node
				this.showNodeTypeChangePopup(e.getComponent(), e.getX(), e.getY());
			} else {
				// nothing to do
			}
		} 
		*/
		
	}
	
	
	

	/* (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) 
	{
		//
		//by young: this function is removed
		//
		if (SwingUtilities.isLeftMouseButton(e)) 
		{
			Node newNode = null;
			
			switch (getAction()) 
			{ 
				case CREATE_PROBABILISTIC_NODE:
				{
					newNode = controller.insertProbabilisticNode(e.getX(), e.getY());
					UShapeOOBNNode shape = new UShapeOOBNNode(this, newNode, (int)newNode.getPosition().x-newNode.getWidth()/2, (int)newNode.getPosition().y-newNode.getHeight()/2, newNode.getWidth(), newNode.getHeight());
					addShape( shape );	
					shape.setState(UShape.STATE_SELECTED, null);
					showCPT(newNode);				 
				}
				break;
				case NONE:
				{
					if( controller != null )
						controller.unselectAll(); 
				}
				break;
			}
			
		}
				
		/*
		Node node = getNode(e.getX(), e.getY());
		try {
			if (node != null) {
				if ( ((((OOBNNodeGraphicalWrapper)node).getWrappedNode().getType() &  IOOBNNode.TYPE_INSTANCE)) != 0 ) {
					// i'd not like to edit an instance node, so, do nothing if this is an instance
					return;
				}
			}
		} catch (Exception ex) {
			Debug.println(this.getClass(), "The node was not in an expected format", ex);
		}
		
		
	 	super.mouseClicked(e);*/
		
	}
	
	
	
	

	/* (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// this case was added just because to make visibleDimension as local variable,
		// in order to easily overwrite getRectangleRepresentation.
		//
		//by young: this function is removed
		//
		/*
		if (!this.isBMoveNode()) {
			try{
				this.setVisibleDimension(new Dimension((int) (controller.getScreen().getJspGraph().getSize().getWidth()), (int) (controller.getScreen().getJspGraph().getSize().getHeight())));
			} catch (Exception exc) {
				Debug.println(this.getClass(), "Not able to set new visible dimension", exc);
			}
		}*/
		super.mouseEntered(e);
	}

	/**
	 * @return the controller
	 */
	public OOBNClassController getController() {
		return controller;
	}

	
	
	/**
	 * @param controller the controller to set
	 */
	public void setController(OOBNClassController controller) {
		this.controller = controller;
	}

	/**
	 * This popupmenu is used by this pane to edit an
	 * oobn node
	 * @return the oobnOnNodePopup
	 */
	public JPopupMenu getOobnOnNodePopup() {
		return oobnOnNodePopup;
	}

	/**
	 * This popupmenu is used by this pane to edit an
	 * oobn node
	 * @param oobnOnNodePopup the oobnOnNodePopup to set
	 */	 
	public void setOobnOnNodePopup(JPopupMenu oobnOnNodePopup) {
		this.oobnOnNodePopup = oobnOnNodePopup;
	} 

	/* (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#getRectangleRepaint()
	 */
	//
	//by young: this function is removed
	//
	/*
	@Override
	public Rectangle getRectangleRepaint() {
		double maiorX;
		double maiorY;
		double menorX;
		double menorY;
		
		if (this.isBMoveNode() && (this.getSelected() instanceof OOBNNodeGraphicalWrapper)){
			OOBNNodeGraphicalWrapper noAux = (OOBNNodeGraphicalWrapper) this.getSelected();
			maiorX = noAux.getPosition().getX();
			menorX = noAux.getPosition().getX();
			maiorY = noAux.getPosition().getY();
			menorY = noAux.getPosition().getY();
			
			Node noAux2;
			for (int i = 0; i < noAux.getParents().size(); i++) {
				noAux2 = (Node) noAux.getParents().get(i);
				
				if (maiorX < noAux2.getPosition().getX()) {
					maiorX = noAux2.getPosition().getX();
				}
				else {
					if (menorX > noAux2.getPosition().getX()) {
						menorX = noAux2.getPosition().getX();
					}
				}
				
				if (maiorY < noAux2.getPosition().getY()) {
					maiorY = noAux2.getPosition().getY();
				}
				else {
					if (menorY > noAux2.getPosition().getY()) {
						menorY = noAux2.getPosition().getY();
					}
				}
			}
			
			for (int i = 0; i < noAux.getChildren().size(); i++) {
				noAux2 = (Node) noAux.getChildren().get(i);
				
				if (maiorX < noAux2.getPosition().getX()) {
					maiorX = noAux2.getPosition().getX();
				}
				else {
					if (menorX > noAux2.getPosition().getX()) {
						menorX = noAux2.getPosition().getX();
					}
				}
				
				if (maiorY < noAux2.getPosition().getY()) {
					maiorY = noAux2.getPosition().getY();
				}
				else {
					if (menorY > noAux2.getPosition().getY()) {
						menorY = noAux2.getPosition().getY();
					}
				}
			}
			
			long width = noAux.getThisWidth()/2;
			long height = noAux.getThisWidth()/2;
			
			//Debug.println(this.getClass(), "Obtaining rectangle repaint from OOBNGraphPane");
			return new Rectangle((int) (menorX - 6 * width), (int) (menorY - 6 * height), (int) (maiorX - menorX + 12 * width), (int) (maiorY - menorY + 12 * height));
		} else {

			//Debug.println(super.getClass(), "Obtaining rectangle repaint from superclass");
			return super.getRectangleRepaint();
			
		}
	}
*/
	/**
	 * @return the bMoveNode
	 */
	//
	//by young: this function is removed
	//
	/*
	protected boolean isBMoveNode() {
		return bMoveNode;
	}
*/
	/**
	 * @param moveNode the bMoveNode to set
	 */
	//
	//by young: this function is removed
	//
	/*
	protected void setBMoveNode(boolean moveNode) {
		bMoveNode = moveNode;
	}
	*/

	/**
	 * @return the visibleDimension
	 */
	//
	//by young: this function is removed
	//
	/*
	protected Dimension getVisibleDimension() {
		return visibleDimension;
	}*/

	/**
	 * @param visibleDimension the visibleDimension to set
	 */
	//
	//by young: this function is removed
	//
	/*
	protected void setVisibleDimension(Dimension visibleDimension) {
		this.visibleDimension = visibleDimension;
	}*/

	
	/**
	 * If debug mode is on, this method writes to Debug a description of OOBN node
	 * @param node
	 */
	//
	// by young : this function moved to UShapeOOBNNode, because node's action should work in UShapeOOBNNode
	// 
	/*protected void describeOOBNNode(OOBNNodeGraphicalWrapper node) {
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
	}*/
		 
	public void showNodeTypeChangePopup(Component invoker, int x, int y) 
	{
		int n = this.getComponentCount();
    	for( int i = 0; i < n; i++ )
    	{
    		UShape shape = (UShape)this.getComponent(i);
    		
    		if( shape.getState() == UShape.STATE_SELECTED )
    		{
    			if ((((OOBNNodeGraphicalWrapper)shape.getNode()).getWrappedNode().getType() & IOOBNNode.TYPE_INSTANCE) == 0) {
    				this.getOobnOnNodePopup().setEnabled(true);
    				this.getOobnOnNodePopup().show(invoker, x, y);
    				
    				return;
    			}	
    		}
    	}    	
	} 	
}
