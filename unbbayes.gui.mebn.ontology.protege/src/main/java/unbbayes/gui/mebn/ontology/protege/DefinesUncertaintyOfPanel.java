/**
 * 
 */
package unbbayes.gui.mebn.ontology.protege;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.protege.editor.owl.ui.OWLIcons;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.io.mebn.owlapi.OWLAPIStorageImplementorDecorator;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.Debug;
import unbbayes.util.ResourceController;


/**
 * This panel is used in {@link unbbayes.gui.mebn.extension.OWL2PropertyImportPanelBuilder}
 * in order for the builder to show a panel that selects or changes what OWL property
 * the resident node is defining an uncertainty.
 * @author Shou Matsumoto
 *
 */
public class DefinesUncertaintyOfPanel extends JPanel {

	/** Protege-like icon for object properties */
	public static final Icon OBJECT_PROPERTY_ICON = OWLIcons.getIcon("property.object.png");
	/** Protege-like icon for data properties */
    public static final Icon DATA_PROPERTY_ICON = OWLIcons.getIcon("property.data.png");
	
	private ResourceBundle resource;
	
	private MultiEntityBayesianNetwork mebn;
	private IMEBNMediator mediator;

	private JLabel currentlySelectedResidentNodeLabel;

//	private MouseListener labelUpdateListener;

	private JPanel contentPanel;

	private JComponent buttonPanel;
	private JLabel currentlySelectedOWLPropertyLabel;
	private JButton changePropertyButton;
	private JButton removePropertyButton;
	
	
	/**
	 * The default constructor is not private so that inheritance is allowed.
	 * @deprecated use {@link #getInstance(MultiEntityBayesianNetwork)} instead
	 */
	protected DefinesUncertaintyOfPanel() {
		super();
		try {
			this.resource = ResourceController.newInstance().getBundle(
					unbbayes.gui.mebn.ontology.protege.resources.Resources.class.getName(),
					Locale.getDefault(),
					this.getClass().getClassLoader());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	/**
	 * Default constructor method using fields.
	 * @param mebn
	 * @return
	 */
	public static JPanel getInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		DefinesUncertaintyOfPanel ret = new DefinesUncertaintyOfPanel();
		ret.setMebn(mebn);
		ret.setMediator(mediator);
		ret.initComponents();
		ret.initListeners();
		return ret;
	}

//	/**
//	 * Resets this component
//	 */
//	protected void resetComponent() {
//		this.removeAll();
//		this.get
//	}
	
	/**
	 * Initializes the components of this panel
	 */
	protected void initComponents() {
		
		this.setLayout(new BorderLayout());

		this.setBorder(MebnToolkit.getBorderForTabPanel(this.getResource().getString("DefineUncertaintyOfOWLProperty")));
		this.setToolTipText(this.getResource().getString("DefineUncertaintyOfOWLPropertyToolTip"));
		
		this.setContentPanel(new JPanel(new BorderLayout()));
		this.getContentPanel().setBackground(Color.WHITE);
		
		this.add(new JScrollPane(this.getContentPanel()), BorderLayout.CENTER);
		
		// set up the label showing the currently selected node
		this.setCurrentlySelectedResidentNodeLabel(new JLabel(
				((this.getSelectedNode() != null)?this.getSelectedNode().getName():this.getResource().getString("NoSelectedPropertyTitle")), 
				IconController.getInstance().getYellowNodeIcon(), 
				SwingConstants.CENTER));
		this.getCurrentlySelectedResidentNodeLabel().setBorder(MebnToolkit.getBorderForTabPanel(this.getResource().getString("ResidentTabTitle")));
		this.getContentPanel().add(this.getCurrentlySelectedResidentNodeLabel(), BorderLayout.NORTH);
		
		
		
		// set up the label showing the OWL property referenced by the currently selectedÅ@node
		OWLProperty owlProperty = this.getOWLPropertyOfSelectedNode();
		if (owlProperty != null) {
			// there was a related owl property
			if (owlProperty instanceof OWLDataProperty) {
				// it was a data property
				this.setCurrentlySelectedOWLPropertyLabel(new JLabel(
						owlProperty.toStringID(), 
						DATA_PROPERTY_ICON, 
						SwingConstants.CENTER
				));
			} else {
				// it was an object property (this is the default)
				this.setCurrentlySelectedOWLPropertyLabel(new JLabel(
						owlProperty.toStringID(), 
						OBJECT_PROPERTY_ICON, 
						SwingConstants.CENTER
				));
			}
		} else {
			// no owl property found
			this.setCurrentlySelectedOWLPropertyLabel(new JLabel(
					this.getResource().getString("NoSelectedPropertyTitle"), 
					OBJECT_PROPERTY_ICON, 
					SwingConstants.CENTER
			));
		}
		this.getCurrentlySelectedOWLPropertyLabel().setBorder(MebnToolkit.getBorderForTabPanel(this.getResource().getString("OWLProperties")));
		this.getContentPanel().add(this.getCurrentlySelectedOWLPropertyLabel(), BorderLayout.CENTER);
		
		
		// set up tool bar to pop up OWL property selector
//		this.setButtonPanel(new JToolBar(this.getResource().getString("DefineUncertaintyOfOWLProperty"), JToolBar.HORIZONTAL));
		this.setButtonPanel(new JPanel(new FlowLayout(FlowLayout.CENTER)));
		this.getButtonPanel().setToolTipText(this.getResource().getString("DefineUncertaintyOfToolBarToolTip"));
		
		// add button to change the linked owl property
		this.setChangePropertyButton(new JButton(IconController.getInstance().getMoreIcon()));
		this.getChangePropertyButton().setToolTipText(this.getResource().getString("SelectOWLPropertyToolTip"));
		this.getButtonPanel().add(this.getChangePropertyButton());
		
		// add button to clear the linked owl property
		this.setRemovePropertyButton(new JButton(IconController.getInstance().getLessIcon()));
		this.getRemovePropertyButton().setToolTipText(this.getResource().getString("ClearSelectionToolTip"));
		this.getButtonPanel().add(this.getRemovePropertyButton());
		
		// add the toolbar to the panel
		this.getContentPanel().add(this.getButtonPanel(), BorderLayout.SOUTH);
		
		
		System.gc();
	}
	
	/**
	 * Obtains the currently selected (resident) node in the MEBN editor
	 * @return a node or null if not found
	 */
	protected INode getSelectedNode() {
		if (this.getMediator() != null) {
			return this.getMediator().getResidentNodeActive();
		}
		return null;
	}
	
	/**
	 * Obtains the owl property related to the currently selected node {@link #getSelectedNode()}
	 * @return
	 */
	protected OWLProperty getOWLPropertyOfSelectedNode() {
		// extract node
		INode node = this.getSelectedNode();
		
		// extract OWL ontology if it is possible
		if (node != null
				&& (node instanceof ResidentNode)
				&& this.getMebn() != null
				&& this.getMebn().getStorageImplementor() != null
				&& (this.getMebn().getStorageImplementor() instanceof OWLAPIStorageImplementorDecorator)) {
			
			OWLOntology ontology = ((OWLAPIStorageImplementorDecorator)this.getMebn().getStorageImplementor()).getAdaptee();
			
			// extract IRI
			IRI iri = IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getMebn(), node);
			if (iri == null) {
				// there was no related IRI
				return null;
			}
			
			// Find data property
			OWLProperty ret = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(iri);
			
			// verify if data property exists
			if (ontology.containsDataPropertyInSignature(ret.getIRI(), true)) {
				return ret;
			} else {
				// it was not a data property. By default, use object property
				return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(IRIAwareMultiEntityBayesianNetwork.getDefineUncertaintyFromMEBN(this.getMebn(), node));
			}
		}
		
		return null;
	}
	
	/**
	 * Initializes the listeners of components of this panel
	 * @see #initComponents().
	 */
	protected void initListeners() {

		// create mouse listener to update label when the MEBN graph is clicked
		// We could not find a functionality (e.g. listener) that notifies node selection changes, so we are updating nodes only when mouse enters to this panel
		// TODO find out another way to update label
		MouseListener labelUpdateListener = new MouseListener() {
			public void mouseEntered(MouseEvent e) {
				updateLabels();
			}
			public void mouseExited(MouseEvent e) {this.mouseEntered(e);} 
			public void mousePressed(MouseEvent e) {}
			public void mouseReleased(MouseEvent e) {} 
			public void mouseClicked(MouseEvent e) {}
		};
		
		this.addMouseListener(labelUpdateListener);
		this.getContentPanel().addMouseListener(labelUpdateListener);
		
		// listener for the button to clear property selection
		this.getRemovePropertyButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					IRIAwareMultiEntityBayesianNetwork.addDefineUncertaintyToMEBN(getMebn(), (ResidentNode)getSelectedNode(), null);
					updateLabels();
				} catch (Throwable t) {
					Debug.println(this.getClass(), "Failed to reset", t);
				}
			}
		});
		
		// listener for the button to select property 
		this.getChangePropertyButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					showPropertySelectionDialog();
				} catch (Throwable t) {
					Debug.println(this.getClass(), "Failed to reset", t);
				}
			}
		});
		
		System.gc();
	}
	
	/**
	 * Show a popup and force user to select a property
	 * @return
	 */
	protected void showPropertySelectionDialog() {
		JDialog dialog = new PropertySelectionDialog();
		dialog.setVisible(true);
	}
	
	/**
	 * This is a dialog to ask a user to select an OWL property.
	 * It is reusing {@link OWL2PropertyViewerPanel}
	 * @author Shou Matsumoto
	 *
	 */
	public class PropertySelectionDialog extends JDialog {
		private JButton okButton;
		public PropertySelectionDialog () {
			super(getMediator().getScreen().getUnbbayesFrame(), getResource().getString("SelectOWLPropertyToolTip"), true);
			
			this.getContentPane().setLayout(new BorderLayout());
			
			// reuse property list
			final OWL2PropertyViewerPanel propertyListPanel = (OWL2PropertyViewerPanel)OWL2PropertyViewerPanel.newInstance(getMebn());
			// disable multiple selection of properties
			propertyListPanel.getPropertyList().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// disable drag and drop
			propertyListPanel.getPropertyList().setDragEnabled(false);
			// change text border
			propertyListPanel.setBorder(MebnToolkit.getBorderForTabPanel(getResource().getString("SelectOWLPropertyToolTip")));
			// add property list
			this.getContentPane().add(propertyListPanel, BorderLayout.CENTER);
			
			// create the OK button
			this.setOkButton(new JButton(IconController.getInstance().getMoreIcon()));
			this.getOkButton().setToolTipText(getResource().getString("DefineUncertaintyOfOWLProperty"));
			this.getContentPane().add(this.getOkButton(), BorderLayout.SOUTH);
			
			// add the action of the button
			this.getOkButton().addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Object selected = propertyListPanel.getPropertyList().getSelectedValue();
					if (selected instanceof OWLProperty) {
						INode selectedNode = getSelectedNode();
						if (selectedNode != null
								&& (selectedNode instanceof ResidentNode)) {
							IRIAwareMultiEntityBayesianNetwork.addDefineUncertaintyToMEBN(getMebn(), (ResidentNode)selectedNode, ((OWLProperty)selected).getIRI());
							updateLabels();
						} else {
							try {
								Debug.println(this.getClass(), "The selected node is not a resident node: " + selectedNode);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
					} else {
						try {
							Debug.println(this.getClass(), "The selected property is not an OWL property: " + selected);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
					PropertySelectionDialog.this.setVisible(false);
					PropertySelectionDialog.this.dispose();
				}
			});
			
			Dimension preferredSize = new Dimension(480, 600);
			this.setPreferredSize(preferredSize);
			this.setSize(preferredSize);
			this.pack();
		}
		public JButton getOkButton() {return okButton;}
		public void setOkButton(JButton okButton) {this.okButton = okButton;}
	}
	
	/**
	 * Update the values of {@link #getCurrentlySelectedOWLPropertyLabel()} and {@link #getCurrentlySelectedResidentNodeLabel()}
	 */
	protected void updateLabels() {
		// update the selected node
		getCurrentlySelectedResidentNodeLabel().setText(
				((getMediator() != null && getMediator().getResidentNodeActive() != null)?getMediator().getResidentNodeActive().getName():getResource().getString("NoSelectedPropertyTitle"))
		);
		
		// update the selected owl property
		OWLProperty owlProperty = getOWLPropertyOfSelectedNode();
		if (owlProperty != null) {
			// there was a related owl property
			if (owlProperty instanceof OWLDataProperty) {
				// it was a data property
				getCurrentlySelectedOWLPropertyLabel().setText(owlProperty.toStringID());
				getCurrentlySelectedOWLPropertyLabel().setIcon(DATA_PROPERTY_ICON);
			} else {
				// it was an object property (this is the default)
				getCurrentlySelectedOWLPropertyLabel().setText(owlProperty.toStringID());
				getCurrentlySelectedOWLPropertyLabel().setIcon(OBJECT_PROPERTY_ICON);
			}
		} else {
			// no owl property found
			getCurrentlySelectedOWLPropertyLabel().setText(getResource().getString("NoSelectedPropertyTitle"));
			getCurrentlySelectedOWLPropertyLabel().setIcon(OBJECT_PROPERTY_ICON);
		}
		
		// repaint the selected node label
		getCurrentlySelectedResidentNodeLabel().updateUI();
		getCurrentlySelectedResidentNodeLabel().repaint();
		
		// repaint the selected property label
		getCurrentlySelectedOWLPropertyLabel().updateUI();
		getCurrentlySelectedOWLPropertyLabel().repaint();
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
	 * @return the currentlySelectedResidentNodeLabel
	 */
	public JLabel getCurrentlySelectedResidentNodeLabel() {
		return currentlySelectedResidentNodeLabel;
	}

	/**
	 * @param currentlySelectedResidentNodeLabel the currentlySelectedResidentNodeLabel to set
	 */
	public void setCurrentlySelectedResidentNodeLabel(
			JLabel currentlySelectedResidentNodeLabel) {
		this.currentlySelectedResidentNodeLabel = currentlySelectedResidentNodeLabel;
	}

//	/**
//	 * @return the labelUpdateListener
//	 */
//	public MouseListener getLabelUpdateMouseListener() {
//		return labelUpdateListener;
//	}
//
//	/**
//	 * @param labelUpdateListener the labelUpdateListener to set
//	 */
//	public void setLabelUpdateMouseListener(MouseListener labelUpdateListener) {
//		this.labelUpdateListener = labelUpdateListener;
//	}

	/**
	 * @return the contentPanel
	 */
	public JPanel getContentPanel() {
		return contentPanel;
	}

	/**
	 * @param contentPanel the contentPanel to set
	 */
	public void setContentPanel(JPanel contentPanel) {
		this.contentPanel = contentPanel;
	}

	/**
	 * @return the buttonPanel
	 */
	public JComponent getButtonPanel() {
		return buttonPanel;
	}

	/**
	 * @param buttonPanel the buttonPanel to set
	 */
	public void setButtonPanel(JComponent buttonPanel) {
		this.buttonPanel = buttonPanel;
	}

	/**
	 * @return the currentlySelectedOWLPropertyLabel
	 */
	public JLabel getCurrentlySelectedOWLPropertyLabel() {
		return currentlySelectedOWLPropertyLabel;
	}

	/**
	 * @param currentlySelectedOWLPropertyLabel the currentlySelectedOWLPropertyLabel to set
	 */
	public void setCurrentlySelectedOWLPropertyLabel(
			JLabel currentlySelectedOWLPropertyLabel) {
		this.currentlySelectedOWLPropertyLabel = currentlySelectedOWLPropertyLabel;
	}

	/**
	 * @return the changePropertyButton
	 */
	public JButton getChangePropertyButton() {
		return changePropertyButton;
	}

	/**
	 * @param changePropertyButton the changePropertyButton to set
	 */
	public void setChangePropertyButton(JButton changePropertyButton) {
		this.changePropertyButton = changePropertyButton;
	}

	/**
	 * @return the removePropertyButton
	 */
	public JButton getRemovePropertyButton() {
		return removePropertyButton;
	}

	/**
	 * @param removePropertyButton the removePropertyButton to set
	 */
	public void setRemovePropertyButton(JButton removePropertyButton) {
		this.removePropertyButton = removePropertyButton;
	}
	
}
