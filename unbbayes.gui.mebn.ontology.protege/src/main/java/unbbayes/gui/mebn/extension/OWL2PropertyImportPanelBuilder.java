/**
 * 
 */
package unbbayes.gui.mebn.extension;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLProperty;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.MEBNController;
import unbbayes.gui.mebn.DescriptionPane;
import unbbayes.gui.mebn.MEBNEditionPane;
import unbbayes.gui.mebn.MEBNGraphPane;
import unbbayes.gui.mebn.MEBNNetworkWindow;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder;
import unbbayes.io.mebn.owlapi.DefaultPROWL2ModelUser;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.IMEBNElementFactory;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MFrag;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.PROWL2MEBNFactory;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.prs.mebn.ontology.protege.OWLPropertyDTO;
import unbbayes.util.Debug;

/**
 * Creates a panel to import OWL2 properties as resident nodes.
 * @author Shou Matsumoto
 *
 */
public class OWL2PropertyImportPanelBuilder extends OWLPropertyImportPanelBuilder {
	
	private IMEBNElementFactory mebnFactory;
	private JToggleButton btnTabOptionDefinesUncertaintyOf;
	private String definesUncertaintyOfCardLayoutID = "DefinesUncertaintyOf";
	
	private IPROWL2ModelUser prowlModelUserDelegator = DefaultPROWL2ModelUser.getInstance();

	/**
	 * Default constructor with no arguments must be visible for plug-in compatibility.
	 */
	public OWL2PropertyImportPanelBuilder() {
		super();
		this.setMebnFactory(PROWL2MEBNFactory.getInstance());
	}
	
	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.OWLPropertyImportPanelBuilder#buildPanel(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public JPanel buildPanel(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		// we do not need this plugin if mebn is not bound to a project
		if (mebn == null || mebn.getStorageImplementor() == null ) {
			return null;
		}
		if (!(mebn.getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator)
				|| ((IOWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee() == null) {
			return null;
		}
		
		this.setMediator(mediator);
		this.setMebn(mebn);
		
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

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.OWLPropertyImportPanelBuilder#initComponents()
	 */
	protected void initComponents() {
		this.setLayout(new BorderLayout());
		
		// reuse components of MEBNEditionPane
		this.setNewWindow(new  MEBNEditionPane((MEBNNetworkWindow)this.getMediator().getScreen(), (MEBNController)this.getMediator()));		
		
		// hide/remove unnecessary components
		this.getNewWindow().getJtbEdition().setVisible(false);
		this.getNewWindow().getTabsPanel().remove(this.getNewWindow().getDescriptionPane());
		this.getNewWindow().getJtbTabSelection().remove(this.getNewWindow().getBtnTabOptionOVariable());
		this.getNewWindow().getJtbTabSelection().remove(this.getNewWindow().getBtnTabOptionEntity());
		this.getNewWindow().getJtbTabSelection().remove(this.getNewWindow().getBtnTabOptionEntityFinding());
		this.getNewWindow().getJtbTabSelection().remove(this.getNewWindow().getBtnTabOptionNodeFinding());
		this.getNewWindow().getTopPanel().setVisible(false);
		
		if (this.getMediator().getCurrentMFrag() != null) {
			this.getNewWindow().showTitleGraph(this.getMediator().getCurrentMFrag().getName());
			this.getNewWindow().getNodeSelectedToolBar().setVisible(false);
		}
		
		this.add(this.getNewWindow());
		
		// it was impossible to reuse the graph pane... We are creating new one...
		
		this.setGraphViewport(new JViewport());
		this.setGraphPane(new MEBNGraphPane((MEBNController)this.getMediator(), this.getGraphViewport()));

		this.setJspGraph(new JScrollPane(this.getGraphViewport()));
		this.getJspGraph().setVisible(true);
		
		this.getNewWindow().getGraphPanel().setBottomComponent(this.getJspGraph());
		
		// add new tab button to the left editor (where MTheory tree resides), in order to show OWL properties
		this.setBtnTabOptionOWLProperties(new JToggleButton(this.getIconController().getResidentNodeIcon()));
		this.getBtnTabOptionOWLProperties().setBackground(MebnToolkit.getColorTabPanelButton());
		this.getBtnTabOptionOWLProperties().setToolTipText(this.getResource().getString("OWLPropertiesToolTip"));
		
		// add button to the window
		this.getNewWindow().getGroupButtonsTabs().add(this.getBtnTabOptionOWLProperties());
		this.getNewWindow().getJtbTabSelection().add(this.getBtnTabOptionOWLProperties(), 1);
		
		// add a panel to be displayed when the above button is toggled
		if (this.getMebn() != null && this.getMebn().getStorageImplementor() != null && (this.getMebn().getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator)) {
			// show OWL properties hold by MEBN as its storage implementor (usually, MEBN holds who is implementing his storage)
			this.getNewWindow().getJpTabSelected().add(this.getOwlPropertyCardLayoutID(), OWL2PropertyViewerPanel.newInstance(this.getMebn()));
		} else {
			// this MEBN is not holding an OWL model (this is a new model or it is not an PR-OWL project)
			this.getNewWindow().getJpTabSelected().add(this.getOwlPropertyCardLayoutID(), new JScrollPane(new JLabel(this.getResource().getString("NoOWLModelFound"), SwingConstants.LEFT)));
		}
		
		// add another tab button to the left editor (where MTheory tree resides), in order to show what property the currently selected resident node is linked to
		this.setBtnTabOptionDefinesUncertaintyOf(new JToggleButton(this.getIconController().getYellowNodeIcon()));
		this.getBtnTabOptionDefinesUncertaintyOf().setBackground(MebnToolkit.getColorTabPanelButton());
		this.getBtnTabOptionDefinesUncertaintyOf().setToolTipText(this.getResource().getString("DefinesUncertaintyOfToolTip"));
		
		// add button to the window
		this.getNewWindow().getGroupButtonsTabs().add(this.getBtnTabOptionDefinesUncertaintyOf());
		this.getNewWindow().getJtbTabSelection().add(this.getBtnTabOptionDefinesUncertaintyOf(), 2);
		
		// add a panel to be displayed when the above button is toggled
		if (this.getMebn() != null && this.getMebn().getStorageImplementor() != null && (this.getMebn().getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator)) {
			// show OWL properties hold by MEBN as its storage implementor (usually, MEBN holds who is implementing his storage)
			this.getNewWindow().getJpTabSelected().add(this.getDefinesUncertaintyOfCardLayoutID(), DefinesUncertaintyOfPanel.getInstance(this.getMebn(), this.getMediator()));
		} else {
			// this MEBN is not holding an OWL model (this is a new model or it is not an PR-OWL project)
			this.getNewWindow().getJpTabSelected().add(this.getDefinesUncertaintyOfCardLayoutID(), new JScrollPane(new JLabel(this.getResource().getString("NoOWLModelFound"), SwingConstants.LEFT)));
		}
		
		
	}

	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.OWLPropertyImportPanelBuilder#initListeners()
	 */
	protected void initListeners() {
		
		try {
			super.initListeners();
		} catch (Throwable t) {
			Debug.println(this.getClass(), "There was a problem initializing the upper listeners, but we can still go on.", t);
		}
		
		// listener to show the panel to edit the "definesUncertaintyOf" property when a tab changes (toggle button event) is triggered
		this.getBtnTabOptionDefinesUncertaintyOf().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getNewWindow().getCardLayout().show(getNewWindow().getJpTabSelected(), getDefinesUncertaintyOfCardLayoutID());
			}
		});
		
		// Let's just change the behavior of what happens on drop action from the OWLPropertyViewerPanel
		this.getGraphPane().setTransferHandler(new TransferHandler() {
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
					Collection collectionOfProperties = null;
					try {
						collectionOfProperties = (Collection)t.getTransferData(OWLPropertyDTO.DEFAULT_DATA_FLAVORS[0]);
					} catch (UnsupportedFlavorException e) {
						e.printStackTrace();
						collectionOfProperties = new HashSet<String>();
						collectionOfProperties.add(t.getTransferData(DataFlavor.stringFlavor));
					}
					
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
						
						// randomize range of node's positions: <random> or -<random>, bound to maximum of <randomRange>
						double xPos = (((Math.random()<.5)?-1:1) * (randomRange * Math.random())) + location.getX(); 
						double yPos = (((Math.random()<.5)?-1:1) * (randomRange * Math.random())) + location.getY();
						
						// insert a resident node
						INode generatedNode = null;
						try {
							generatedNode = insertPropertyAsNode(xPos, yPos, object, getMebn(), getMediator());
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (generatedNode == null) {
								JOptionPane.showMessageDialog(
										getNewWindow(), 
										object.toString(), 
										getResource().getString("CannotDragNDrop"), 
										JOptionPane.ERROR_MESSAGE);
								continue;
							}
						}
						
						Debug.println(this.getClass(), "It seems that we added the property " + object);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							getNewWindow(), 
							e.getMessage(), 
							getResource().getString("CannotDragNDrop"), 
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
	 * This method inserts a new (resident) node to a mebn
	 * @param x : x position of the new node to create.
	 * @param y : y position of the new node to create.
	 * @param property : the OWL property related to the new resident node to be inserted
	 * @param mebn : the mebn where the node will be inserted.
	 * @param mediator : contains references to GUI elements or utilities in order to obtain state-sensitive informations
	 * about the currently edited mebn (including what is the currently selected mFrag).
	 * @return the generated node
	 */
	protected INode insertPropertyAsNode(double x, double y, Object property, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
//		OWLProperty owlProperty = (OWLProperty) property;	// extracting property
		
		// extract the name of property and instantiates an ResidentNode with such name
		String newNodeName = getNameOfProperty(property);
		
		if (getMebn().getNamesUsed().contains(newNodeName)) {
			JOptionPane.showMessageDialog(
					getNewWindow(), 
					getResource().getString("nameAlreadyExists") + ": " + newNodeName, 
					getResource().getString("CannotDragNDrop"), 
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		// reimplement what getMediator().insertDomainResidentNode(x, y) would do. 
		// It had to be reimplemented because the mediator was so coupled to ResidentNode (in this way, we cannot add reference to original property)
		
		// obtain the currently selected mfrag
		MFrag domainMFrag = mebn.getCurrentMFrag();
		if (domainMFrag == null) {
			JOptionPane.showMessageDialog(
					getNewWindow(), 
					getResource().getString("withoutMFrag"), 
					getResource().getString("CannotDragNDrop"), 
					JOptionPane.ERROR_MESSAGE);
			return null;
		}
		
		// instantiate node
		ResidentNode node = this.getMebnFactory().createResidentNode(newNodeName, domainMFrag);
		mebn.getNamesUsed().add(newNodeName); 
		
		node.setPosition(x, y);
		node.setDescription(node.getName());
		domainMFrag.addResidentNode(node);
		
		// map the node to what property it is defining uncertainty
		try {
			IRI iri = this.extractIRIFromProperty(property);
			IRIAwareMultiEntityBayesianNetwork.addDefineUncertaintyToMEBN(mebn, node, iri);
			Debug.println(this.getClass(), "IRI of node " + node + " set to " + iri);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		//Updating panels
		mediator.getMebnEditionPane().setEditArgumentsTabActive(node);
		mediator.getMebnEditionPane().setResidentNodeTabActive(node);
		mediator.getMebnEditionPane().setArgumentTabActive();
		mediator.getMebnEditionPane().setResidentBarActive();
		mediator.getMebnEditionPane().setTxtNameResident(((ResidentNode)node).getName());
		mediator.getMebnEditionPane().setDescriptionText(node.getDescription(), DescriptionPane.DESCRIPTION_PANE_RESIDENT); 
		mediator.getMebnEditionPane().getMTheoryTree().addNode(domainMFrag, node); 
		
		// set the new node as selected
		mediator.selectNode(node);
		
		return node;
		
	}

	/**
	 * It extracts the IRI from a property.
	 * @param property : property to extract IRI. If its type is unknown, a new IRI will be created from property.toString().
	 * @return the extracted IRI. If IRI could not be extracted, it will return null.
	 */
	protected IRI extractIRIFromProperty(Object property) {
		// if property is unknown
		if (property instanceof OWLProperty) {
			return ((OWLProperty)property).getIRI();
		}
		// this is unknown
		try {
			return IRI.create(property.toString());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Extract a name from a property (which is in object format).
	 * This method is used by the drag'n'drop action (i.e. TransferHandler) in order to give a name
	 * to the newly created resident node.
	 * @param property
	 * @return a non-null name. If property == null, it will return "null". If the type of property is unknown, it will return property.toString()
	 */
	protected String getNameOfProperty(Object property) {
		if (property == null) {
			return "null";
		}
		if (property instanceof OWLObject) {
//			String name = ((OWLObject)property).toStringID();
//			// use only what resides after '#'
//			try {
//				name = name.substring(name.lastIndexOf('#')+1);
//			} catch (Exception e) {
//				e.printStackTrace();
//				// It is OK. Use the available name though
//			}
			return this.getProwlModelUserDelegator().extractName((OWLObject) property);
		}
		return property.toString();
	}

	/**
	 * This factory will be used in order to instantiate ResidentNode when a drag'n'drop action is performed.
	 * @return the mebnFactory
	 */
	public IMEBNElementFactory getMebnFactory() {
		return mebnFactory;
	}


	/**
	 * This factory will be used in order to instantiate ResidentNode when a drag'n'drop action is performed
	 * @param mebnFactory the mebnFactory to set
	 */
	public void setMebnFactory(IMEBNElementFactory mebnFactory) {
		this.mebnFactory = mebnFactory;
	}

	/**
	 * @return the btnTabOptionDefinesUncertaintyOf
	 */
	public JToggleButton getBtnTabOptionDefinesUncertaintyOf() {
		return btnTabOptionDefinesUncertaintyOf;
	}

	/**
	 * @param btnTabOptionDefinesUncertaintyOf the btnTabOptionDefinesUncertaintyOf to set
	 */
	public void setBtnTabOptionDefinesUncertaintyOf(
			JToggleButton btnTabOptionDefinesUncertaintyOf) {
		this.btnTabOptionDefinesUncertaintyOf = btnTabOptionDefinesUncertaintyOf;
	}
	
	/**
	 * @return the definesUncertaintyOfCardLayoutID
	 */
	public String getDefinesUncertaintyOfCardLayoutID() {
		return definesUncertaintyOfCardLayoutID;
	}




	/**
	 * @param definesUncertaintyOfCardLayoutID the definesUncertaintyOfCardLayoutID to set
	 */
	public void setDefinesUncertaintyOfCardLayoutID (String definesUncertaintyOfCardLayoutID) {
		this.definesUncertaintyOfCardLayoutID = definesUncertaintyOfCardLayoutID;
	}
	
	
	/**
	 * Calls to {@link IPROWL2ModelUser} will be delegated to this object.
	 * @return the prowlModelUserDelegator
	 */
	public IPROWL2ModelUser getProwlModelUserDelegator() {
		return prowlModelUserDelegator;
	}

	/**
	 * Calls to {@link IPROWL2ModelUser} will be delegated to this object.
	 * @param prowlModelUserDelegator the prowlModelUserDelegator to set
	 */
	public void setProwlModelUserDelegator(
			IPROWL2ModelUser prowlModelUserDelegator) {
		this.prowlModelUserDelegator = prowlModelUserDelegator;
	}
	
}
