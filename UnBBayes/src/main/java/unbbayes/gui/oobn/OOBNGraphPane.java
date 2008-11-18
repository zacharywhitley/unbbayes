/**
 * 
 */
package unbbayes.gui.oobn;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;

import unbbayes.controller.NetworkController;
import unbbayes.controller.oobn.OOBNClassController;
import unbbayes.controller.oobn.OOBNController;
import unbbayes.gui.GraphPane;
import unbbayes.gui.oobn.node.OOBNNodeGraphicalWrapper;
import unbbayes.prs.Edge;
import unbbayes.prs.Node;
import unbbayes.prs.bn.ProbabilisticNode;
import unbbayes.prs.bn.SingleEntityNetwork;
import unbbayes.prs.oobn.IOOBNClass;
import unbbayes.prs.oobn.IOOBNNode;
import unbbayes.util.Debug;

/**
 * @author Shou Matsumoto
 *
 */
public class OOBNGraphPane extends GraphPane {
	
	/** Load resource file from this package */
	private static ResourceBundle resource = ResourceBundle.getBundle("unbbayes.gui.oobn.resources.OOBNGuiResource");
	

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
		
		ret.setVisibleDimension(new Dimension());
		
		return ret;
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
					node = (OOBNNodeGraphicalWrapper)getSelected();
					node.getWrappedNode().setType(IOOBNNode.TYPE_INPUT);
					Debug.println(this.getClass(), "I'm setting the node as an input");	
					update();
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
					node = (OOBNNodeGraphicalWrapper)getSelected();
					node.getWrappedNode().setType(IOOBNNode.TYPE_OUTPUT);
					Debug.println(this.getClass(), "I'm setting the node as an output");	
					update();
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
					node = (OOBNNodeGraphicalWrapper)getSelected();
					node.getWrappedNode().setType(IOOBNNode.TYPE_PRIVATE);
					Debug.println(this.getClass(), "I'm setting the node as private");	
					update();
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
	
	/**
	 * Initializes the TransferHundler, which is responsible for
	 * drag n drop / copy n paste operations
	 */
	public void setUpTransferHundler() {
		this.setTransferHandler(new TransferHandler() {

			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
			 */
			@Override
			public boolean canImport(TransferSupport support) {
				// check if this is local java object
				try{
					boolean ret = support.isDataFlavorSupported(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
					return ret;
				} catch (Exception e) {
					try{
						Debug.println(this.getClass(), "Could not support data transfer from " + support.getComponent().getName(), e);
					} catch (Exception newe) {
						Debug.println(this.getClass(), "Could not support data transfer using drag/drop or copy/paste", e);
					}
					return false;
				}
			}

			/* (non-Javadoc)
			 * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
			 */
			@Override
			public boolean importData(TransferSupport support) {
				
				Debug.println(this.getClass(), "Importing data from dragndrop: " + support.toString());
				
				
				// TODO finish this
				if (!this.canImport(support)) {
					return false;
				}
				
				try {
					// obtains the location to insert oobn instance
					DropLocation location = support.getDropLocation();
					
					
					// extract the oobn class
					Transferable transfer = support.getTransferable();
					IOOBNClass oobnClass = (IOOBNClass)transfer.getTransferData(new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType));
					
					if (oobnClass == null) {
						Debug.println(this.getClass(), "Nothing was extracted from drag n drop");
						return false;
					} 
					
					// insert new oobn class' instance
					getController().insertInstanceNode(oobnClass, location.getDropPoint().getX(), location.getDropPoint().getY());
					
					Debug.println(this.getClass(), "It seems that we added the class " + oobnClass.getClassName() 
							+ "at position (" + location.getDropPoint().getX() + "," + location.getDropPoint().getY() + ")");
					
									
					
				} catch (Exception e) {
					JOptionPane.showMessageDialog(getController().getScreen(), e.getMessage(), resource.getString("CannotDragNDrop"), JOptionPane.ERROR_MESSAGE);
					throw new RuntimeException(resource.getString("CannotDragNDrop") , e);
				}
				
				
				// delegate to upper class
				super.importData(support);
				
				// update whole panel (instead of ordinal update, which only updates a small part of screen)
				repaint();	
				
				// if this code is reached, no problem was found
				return true;
			}
			
			
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
		
		switch (this.getAction()) {
		case NONE:
			this.setBMoveNode(true);
			//setCursor(new Cursor(Cursor.MOVE_CURSOR));
			try {
				OOBNNodeGraphicalWrapper node = (OOBNNodeGraphicalWrapper)getNode(e.getX(), e.getY());
				
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
				
		default:
			break;
		}
		
		// do the default behavior as well		
		super.mousePressed(e);
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#mouseReleased(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		// I'm overwriting this method to add special node treatment for OOBN node
		// when a mouse is clicked over such node.
		// I'm not using e.isPoputrigger because it seems not to be working on Linux...
		if ((e.getModifiers() == MouseEvent.BUTTON3_MASK) && (getSelected() != null)) {
			// we should only trigger such event if the selected one is an OOBN Node
			if (this.getSelected() instanceof OOBNNodeGraphicalWrapper) {
				// only allow to popup if selected node is not an instance node
				if ((((OOBNNodeGraphicalWrapper)this.getSelected()).getWrappedNode().getType() & IOOBNNode.TYPE_INSTANCE) == 0) {
					this.getOobnOnNodePopup().setEnabled(true);
					this.getOobnOnNodePopup().show(e.getComponent(), e.getX(), e.getY());
				}
			} else {
				// nothing to do
			}
		} 
		
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
		
	}
	
	
	

	/* (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#mouseClicked(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
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
		super.mouseClicked(e);
	}
	
	
	
	

	/* (non-Javadoc)
	 * @see unbbayes.gui.GraphPane#mouseEntered(java.awt.event.MouseEvent)
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// this case was added just because to make visibleDimension as local variable,
		// in order to easily overwrite getRectangleRepresentation.
		if (!this.isBMoveNode()) {
			try{
				this.setVisibleDimension(new Dimension((int) (controller.getScreen().getJspGraph().getSize().getWidth()), (int) (controller.getScreen().getJspGraph().getSize().getHeight())));
			} catch (Exception exc) {
				Debug.println(this.getClass(), "Not able to set new visible dimension", exc);
			}
		}
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
			
			Debug.println(this.getClass(), "Obtaining rectangle repaint from OOBNGraphPane");
			return new Rectangle((int) (menorX - 6 * width), (int) (menorY - 6 * height), (int) (maiorX - menorX + 12 * width), (int) (maiorY - menorY + 12 * height));
		} else {

			Debug.println(super.getClass(), "Obtaining rectangle repaint from superclass");
			return super.getRectangleRepaint();
			
		}
	}

	/**
	 * @return the bMoveNode
	 */
	protected boolean isBMoveNode() {
		return bMoveNode;
	}

	/**
	 * @param moveNode the bMoveNode to set
	 */
	protected void setBMoveNode(boolean moveNode) {
		bMoveNode = moveNode;
	}

	/**
	 * @return the visibleDimension
	 */
	protected Dimension getVisibleDimension() {
		return visibleDimension;
	}

	/**
	 * @param visibleDimension the visibleDimension to set
	 */
	protected void setVisibleDimension(Dimension visibleDimension) {
		this.visibleDimension = visibleDimension;
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
