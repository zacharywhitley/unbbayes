/**
 * 
 */
package unbbayes.gui.mebn.extension;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.mebn.MEBNEditionPane;
import unbbayes.gui.mebn.MEBNGraphPane;
import unbbayes.gui.mebn.MEBNNetworkWindow;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder;
import unbbayes.gui.mebn.ontology.protege.OWLPropertyViewerPanel;
import unbbayes.io.mebn.MEBNStorageImplementorDecorator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ontology.protege.OWLPropertyDTO;
import unbbayes.util.Debug;
import unbbayes.util.ResourceController;
import edu.stanford.smi.protegex.owl.model.RDFProperty;

/**
 * @author Shou Matsumoto
 *
 */
public class OWLPropertyImportPanelBuilder extends JPanel implements IMEBNEditionPanelBuilder {

	private String owlPropertyCardLayoutID = "OWLProperties";
	
	private MEBNEditionPane newWindow;
	private IMEBNMediator mediator;
	private MultiEntityBayesianNetwork mebn;


	private ResourceBundle resource;
	
	private IconController iconController;
	private MEBNGraphPane graphPane;
	private JViewport graphViewport;
	private JScrollPane jspGraph;
	private JToggleButton btnTabOptionOWLProperties;

	
	private int defaultRandomRangeOnMultiplePropertySelection = 100;
	
	/**
	 * 
	 */
	public OWLPropertyImportPanelBuilder() {
		super();
		this.resource = ResourceController.newInstance().getBundle(
				unbbayes.gui.mebn.ontology.protege.resources.Resources.class.getName(),
				Locale.getDefault(),
				this.getClass().getClassLoader());
		
		// set up default customization of IconController, which returns another icon for resident nodes
		this.iconController = new IconController() {
			public ImageIcon getResidentNodeIcon() {
				if (residentNodeIcon == null) {
					try {
						residentNodeIcon = new ImageIcon(getClass().getClassLoader().getResource("properties.png"));
					} catch (Throwable t) {
						Debug.println(this.getClass(), t.getMessage(), t);
						return super.getResidentNodeIcon();
					}
				}
				return residentNodeIcon;
			}
		};
	}

	


	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder#buildPanel(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public JPanel buildPanel(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		
		// we do not need this plugin if mebn is not bound to a owl project
		if (mebn.getStorageImplementor() == null 
				|| !(mebn.getStorageImplementor() instanceof MEBNStorageImplementorDecorator)
				|| ((MEBNStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee() == null) {
			return null;
		}
		
		this.mediator = mediator;
		this.mebn = mebn;
		
		this.initComponents();
		this.initListeners();
		
		/*
		 * Delegate property changes.
		 * I'm adding this listener here, because I want it to be added only once. I cannot move this code to initListeners because
		 * initListeners can be called more than once, and Swing does not actually remove PropertyChangeListener when we call
		 * removePropertyChangeListener, so it would cause memory leak if I add it more than once.
		 */
		this.addPropertyChangeListener(IMEBNEditionPanelBuilder.MEBN_EDITION_PANEL_CHANGE_PROPERTY, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				for (Component comp : getComponents()) {
					comp.firePropertyChange(IMEBNEditionPanelBuilder.MEBN_EDITION_PANEL_CHANGE_PROPERTY, 
							Integer.parseInt(evt.getOldValue().toString()), 
							Integer.parseInt(evt.getNewValue().toString()));
				}
			}
		});
		
		return this;
	}



	/**
	 * Create components. This is called by {@link #buildPanel(MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	protected void initComponents() {
		this.setLayout(new BorderLayout());
		
		// reuse components of MEBNEditionPane
		newWindow = new  MEBNEditionPane((MEBNNetworkWindow)mediator.getScreen(), (MEBNController)mediator);		
		
		// hide/remove unnecessary components
		newWindow.getJtbEdition().setVisible(false);
		newWindow.getTabsPanel().remove(newWindow.getDescriptionPane());
		newWindow.getJtbTabSelection().remove(newWindow.getBtnTabOptionOVariable());
		newWindow.getJtbTabSelection().remove(newWindow.getBtnTabOptionEntity());
		newWindow.getJtbTabSelection().remove(newWindow.getBtnTabOptionEntityFinding());
		newWindow.getJtbTabSelection().remove(newWindow.getBtnTabOptionNodeFinding());
		newWindow.getTopPanel().setVisible(false);
		
		if (mediator.getCurrentMFrag() != null) {
			newWindow.showTitleGraph(mediator.getCurrentMFrag().getName());
			newWindow.getNodeSelectedToolBar().setVisible(false);
		}
		
		this.add(newWindow);
		
		// could not reuse the graph pane... We are creating new one...
		
		this.setGraphViewport(new JViewport());
		this.setGraphPane(new MEBNGraphPane((MEBNController)mediator, this.getGraphViewport()));

		this.setJspGraph(new JScrollPane(this.getGraphViewport()));
		this.getJspGraph().setVisible(true);
		
		newWindow.getGraphPanel().setBottomComponent(this.getJspGraph());
		
		// add new tab button to the left editor (where MTheory tree resides), in order to show OWL properties
		this.setBtnTabOptionOWLProperties(new JToggleButton(iconController.getResidentNodeIcon()));
		this.getBtnTabOptionOWLProperties().setBackground(MebnToolkit.getColorTabPanelButton());
		this.getBtnTabOptionOWLProperties().setToolTipText(resource.getString("OWLPropertiesToolTip"));
		
		// add button to the window
		newWindow.getGroupButtonsTabs().add(this.getBtnTabOptionOWLProperties());
		newWindow.getJtbTabSelection().add(this.getBtnTabOptionOWLProperties(), 1);
		
		// add a panel to be displayed when the above button is toggled
		if (this.getMebn() != null && this.getMebn().getStorageImplementor() != null && (this.getMebn().getStorageImplementor() instanceof MEBNStorageImplementorDecorator)) {
			// show OWL properties hold by MEBN as its storage implementor (usually, MEBN holds who is implementing his storage)
			newWindow.getJpTabSelected().add(this.getOwlPropertyCardLayoutID(), OWLPropertyViewerPanel.newInstance(this.getMebn()));
		} else {
			// this MEBN is not holding an OWL model (this is a new model or it is not an PR-OWL project)
			newWindow.getJpTabSelected().add(this.getOwlPropertyCardLayoutID(), new JScrollPane(new JLabel(this.getResource().getString("NoOWLModelFound"), SwingConstants.LEFT)));
		}
		
	}

	/**
	 * Reset this component. 
	 */
	public void resetComponents() {
		this.removeAll();
		this.initComponents();
		this.initListeners();
		System.gc();
	}

	/**
	 * Fill listeners of components created by {@link #initComponents()}.
	 * This is called by {@link #buildPanel(MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	protected void initListeners() {
		
		// update panels on tab change using the new property listener
		newWindow.addPropertyChangeListener(IMEBNEditionPanelBuilder.MEBN_EDITION_PANEL_CHANGE_PROPERTY, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				resetComponents();
				getGraphPane().resetGraph();
			}
		});
		
		// change graph when tree is selected
		newWindow.getMTheoryTree().addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {
				if (mediator.getCurrentMFrag() != null) {
					newWindow.showTitleGraph(mediator.getCurrentMFrag().getName());
					newWindow.getNodeSelectedToolBar().setVisible(false);
				}
				getGraphPane().resetGraph();
			}
			public void mouseReleased(MouseEvent e) {}
		});
		
		
		// listener to show OWL properties when a tab changes (toggle button event) is triggered
		this.getBtnTabOptionOWLProperties().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getNewWindow().getCardLayout().show(getNewWindow().getJpTabSelected(), getOwlPropertyCardLayoutID());
			}
		});
		
		// what happens on drop action from the OWLPropertyViewerPanel
		getGraphPane().setTransferHandler(new TransferHandler() {
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
			}
			public boolean importData(JComponent comp, Transferable t) {
				Debug.println(this.getClass(), "Importing data from dragndrop: " + t.toString() + ", from component " + comp.getName());
				if (!this.canImport(comp,t.getTransferDataFlavors())) {
					Debug.println(this.getClass(),"Cannot import this data.");
					return false;
				}
				try {
					// obtains the location to insert node (the new node representing a OWL property)
					Point location = comp.getMousePosition();
					
					// extract the property expecting it as a OWLPropertyDTO. OWLPropertyViewerPanel must provide OWLPropertyDTO in drag operation
					Collection collectionOfProperties = (Collection)t.getTransferData(OWLPropertyDTO.DEFAULT_DATA_FLAVORS[0]);
					
					if (collectionOfProperties == null) {
						JOptionPane.showMessageDialog(
								getNewWindow(), 
								getResource().getString("CannotDragNDrop"), 
								getResource().getString("DnDOWLProperty"), 
								JOptionPane.WARNING_MESSAGE);
						return false;
					} 
					 
					// when multiple nodes must be created, we shall "randomize" the position within a given range
					int randomRange = getDefaultRandomRangeOnMultiplePropertySelection();
					if (collectionOfProperties.size() == 1) {
						// there is no multiple selection, so, we do not have to put nodes in a "range"
						randomRange = 0;	// position is bound to a range of 0 = no random range
					}
					
					// insert property as a resident node
					for (Object object : collectionOfProperties) {
						
						RDFProperty property = (RDFProperty) object;	// extracting property
						
						// ..it seems that we cannot add a node using an existing name (even for OWL frames' names)...
//						String newNodeName = property.getName().replace(":", "_");
						String newNodeName = property.getLocalName();
						if (getMebn().getNamesUsed().contains(newNodeName)) {
							// TODO also check OWLModel frame's names...
							JOptionPane.showMessageDialog(
									getNewWindow(), 
									resource.getString("nameAlreadyExists") + ": " + newNodeName, 
									resource.getString("CannotDragNDrop"), 
									JOptionPane.ERROR_MESSAGE);
							continue;
						}
						
						// randomize range of node's positions: <random> or -<random>, bound to maximum of <randomRange>
						double xPos = (((Math.random()<.5)?-1:1) * (randomRange * Math.random())) + location.getX(); 
						double yPos = (((Math.random()<.5)?-1:1) * (randomRange * Math.random())) + location.getY();
						
						// create node
						ResidentNode newNode = getMediator().insertDomainResidentNode(xPos, yPos);
						newNode.setName(newNodeName);
						getMebn().getNamesUsed().add(newNodeName);
						
						Debug.println(this.getClass(), "It seems that we added the property " + property.getName() );
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							getNewWindow(), 
							e.getMessage(), 
							resource.getString("CannotDragNDrop"), 
							JOptionPane.ERROR_MESSAGE);
				}
				
				
				// delegate to upper class
				super.importData(comp, t);
				
				// update whole graph panel
				getGraphPane().resetGraph();
				 
				// if this code is reached, no problem was found
				return true;
			}
		});
		
	}




	/**
	 * @return the newWindow
	 */
	public MEBNEditionPane getNewWindow() {
		return newWindow;
	}




	/**
	 * @param newWindow the newWindow to set
	 */
	public void setNewWindow(MEBNEditionPane newWindow) {
		this.newWindow = newWindow;
	}




	/**
	 * @return the mediator
	 */
	public IMEBNMediator getMediator() {
		return mediator;
	}




	/**
	 * @param mediator the mediator to set
	 */
	public void setMediator(IMEBNMediator mediator) {
		this.mediator = mediator;
	}




	/**
	 * @return the mebn
	 */
	public MultiEntityBayesianNetwork getMebn() {
		return mebn;
	}




	/**
	 * @param mebn the mebn to set
	 */
	public void setMebn(MultiEntityBayesianNetwork mebn) {
		this.mebn = mebn;
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




	/**
	 * @return the iconController
	 */
	public IconController getIconController() {
		return iconController;
	}




	/**
	 * @param iconController the iconController to set
	 */
	public void setIconController(IconController iconController) {
		this.iconController = iconController;
	}




	/**
	 * @return the graphPane
	 */
	public MEBNGraphPane getGraphPane() {
		return graphPane;
	}




	/**
	 * @param graphPane the graphPane to set
	 */
	public void setGraphPane(MEBNGraphPane graphPane) {
		this.graphPane = graphPane;
	}




	/**
	 * @return the graphViewport
	 */
	public JViewport getGraphViewport() {
		return graphViewport;
	}




	/**
	 * @param graphViewport the graphViewport to set
	 */
	public void setGraphViewport(JViewport graphViewport) {
		this.graphViewport = graphViewport;
	}




	/**
	 * @return the jspGraph
	 */
	public JScrollPane getJspGraph() {
		return jspGraph;
	}




	/**
	 * @param jspGraph the jspGraph to set
	 */
	public void setJspGraph(JScrollPane jspGraph) {
		this.jspGraph = jspGraph;
	}




	/**
	 * @return the btnTabOptionOWLProperties
	 */
	public JToggleButton getBtnTabOptionOWLProperties() {
		return btnTabOptionOWLProperties;
	}




	/**
	 * @param btnTabOptionOWLProperties the btnTabOptionOWLProperties to set
	 */
	public void setBtnTabOptionOWLProperties(JToggleButton btnTabOptionOWLProperties) {
		this.btnTabOptionOWLProperties = btnTabOptionOWLProperties;
	}


	/**
	 * Default customization of {@link IconController} for 
	 * this panel.
	 * It just replaces some icons
	 * @author Shou Matsumoto
	 *
	 */
	public class OWLPropertyImportPanelIconController extends IconController {
		public OWLPropertyImportPanelIconController () {
			residentNodeIcon = new ImageIcon(getClass().getResource("properties.png"));
		}
	}


	/**
	 * @return the owlPropertyCardLayoutID
	 */
	public String getOwlPropertyCardLayoutID() {
		return owlPropertyCardLayoutID;
	}




	/**
	 * @param owlPropertyCardLayoutID the owlPropertyCardLayoutID to set
	 */
	public void setOwlPropertyCardLayoutID(String owlPropertyCardLayoutID) {
		this.owlPropertyCardLayoutID = owlPropertyCardLayoutID;
	}




	/**
	 * This is the "noise" to be considered to the positions of nodes
	 * when multiple property selection is provided.
	 * @return the defaultRandomRangeOnMultiplePropertySelection
	 */
	public int getDefaultRandomRangeOnMultiplePropertySelection() {
		return defaultRandomRangeOnMultiplePropertySelection;
	}




	/**
	 * This is the "noise" to be considered to the positions of nodes
	 * when multiple property selection is provided.
	 * @param defaultRandomRangeOnMultiplePropertySelection the defaultRandomRangeOnMultiplePropertySelection to set
	 */
	public void setDefaultRandomRangeOnMultiplePropertySelection(
			int defaultRandomRangeOnMultiplePropertySelection) {
		this.defaultRandomRangeOnMultiplePropertySelection = defaultRandomRangeOnMultiplePropertySelection;
	}

}
