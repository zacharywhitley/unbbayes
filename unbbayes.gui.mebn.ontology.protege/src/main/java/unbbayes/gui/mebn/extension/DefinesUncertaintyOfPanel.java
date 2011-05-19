/**
 * 
 */
package unbbayes.gui.mebn.extension;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
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
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.Argument;
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
	private AbstractButton mapArgumentsButton;
	private JPanel argumentMappingPanel;
	
	
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
		
		this.setContentPanel(new JPanel(new GridLayout(0, 1, 10, 10)));
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
						owlProperty.getIRI().getFragment(), 
						DATA_PROPERTY_ICON, 
						SwingConstants.CENTER
				));
			} else {
				// it was an object property (this is the default)
				this.setCurrentlySelectedOWLPropertyLabel(new JLabel(
						owlProperty.getIRI().getFragment(), 
						OBJECT_PROPERTY_ICON, 
						SwingConstants.CENTER
				));
			}
			this.getCurrentlySelectedOWLPropertyLabel().setToolTipText(owlProperty.toStringID());
		} else {
			// no owl property found
			this.setCurrentlySelectedOWLPropertyLabel(new JLabel(
					this.getResource().getString("NoSelectedPropertyTitle"), 
					OBJECT_PROPERTY_ICON, 
					SwingConstants.CENTER
			));
			this.getCurrentlySelectedOWLPropertyLabel().setToolTipText(this.getResource().getString("NoSelectedPropertyTitle"));
		}
		this.getCurrentlySelectedOWLPropertyLabel().setBorder(MebnToolkit.getBorderForTabPanel(this.getResource().getString("OWLProperties")));
		this.getContentPanel().add(this.getCurrentlySelectedOWLPropertyLabel());
		
				
		// set up tool bar to pop up OWL property selector
//		this.setButtonPanel(new JToolBar(this.getResource().getString("DefineUncertaintyOfOWLProperty"), JToolBar.HORIZONTAL));
		this.setButtonPanel(new JPanel(new FlowLayout(FlowLayout.CENTER)));
		this.getButtonPanel().setToolTipText(this.getResource().getString("DefineUncertaintyOfToolBarToolTip"));
		this.getButtonPanel().setBackground(Color.WHITE);
		
		// add button to change the mapping of arguments
		this.setMapArgumentsButton(new JButton(IconController.getInstance().getArgumentsIcon()));
		this.getMapArgumentsButton().setToolTipText(this.getResource().getString("EditArgumentMappingToolTip"));
		this.getButtonPanel().add(this.getMapArgumentsButton());
		
		// add button to change the linked owl property
		this.setChangePropertyButton(new JButton(IconController.getInstance().getMoreIcon()));
		this.getChangePropertyButton().setToolTipText(this.getResource().getString("SelectOWLPropertyToolTip"));
		this.getButtonPanel().add(this.getChangePropertyButton());
		
		// add button to clear the linked owl property
		this.setRemovePropertyButton(new JButton(IconController.getInstance().getLessIcon()));
		this.getRemovePropertyButton().setToolTipText(this.getResource().getString("ClearSelectionToolTip"));
		this.getButtonPanel().add(this.getRemovePropertyButton());
		
		// add the toolbar to the panel
		this.getContentPanel().add(this.getButtonPanel());
		
		// prepare panel to show argument mappings
		
		this.updateArgumentMappingPanel();
		
		System.gc();
	}
	
	/**
	 * This method refills {@link #getArgumentMappingPanel()} and adds it to
	 * {@link #getContentPanel()}
	 */
	public void updateArgumentMappingPanel() {
		// remove old panel
		if (this.getArgumentMappingPanel() != null) {
			this.getContentPanel().remove(this.getArgumentMappingPanel());
		}
		
		// add new panel
		this.setArgumentMappingPanel(new JPanel(new GridLayout(0, 1)));
		this.getArgumentMappingPanel().setBorder(MebnToolkit.getBorderForTabPanel(this.getResource().getString("ArgumentMapping")));
		this.getArgumentMappingPanel().setBackground(Color.WHITE);
		this.getContentPanel().add(this.getArgumentMappingPanel());
		
		// set up the label showing the OWL property of arguments
		Map<Argument, Map<OWLProperty, Boolean>> argumentToPropertyToDomainOrRangeMap = this.getOWLPropertiesOfArgumentsOfSelectedNode();
		if (argumentToPropertyToDomainOrRangeMap != null && !argumentToPropertyToDomainOrRangeMap.isEmpty() ) {
			// iterate on all mapped arguments
			for (Argument argument : argumentToPropertyToDomainOrRangeMap.keySet()) {
				if (argument == null) {
					continue;	//ignore
				}
				// extract mapped properties and whether or not it is a subject.
				Map<OWLProperty, Boolean> mappedPropertyMap = argumentToPropertyToDomainOrRangeMap.get(argument);
				if (mappedPropertyMap == null || mappedPropertyMap.isEmpty()) {
					continue;	//ignore
				}
				// iterate on extracted properties
				for (OWLProperty mappedProperty : mappedPropertyMap.keySet()) {
					
					if (mappedPropertyMap.get(mappedProperty) == null) {
						try {
							Debug.println(this.getClass(), argument + " -> " + mappedProperty + " is an unspecified mapping (we cannot infer if argument is a subject or object).");
						} catch (Throwable t) {
							t.printStackTrace();
						}
						continue;	// ignore mappings that do not specify if it is a subject or an object
					}
					
					// fill argument mapping panel with the following information: <argumentOV> <subject of | object of> <property>
					String labelContent = argument.getOVariable().getName()
						+ (mappedPropertyMap.get(mappedProperty)?this.getResource().getString("isSubjectOf"):this.getResource().getString("isObjectIn"))
						+  mappedProperty.getIRI().getFragment(); 
					
					// change label's icon depending on the property type
					JLabel labelToAdd = null;
					if (mappedProperty instanceof OWLDataProperty) {
						// it was a data property
						labelToAdd = new JLabel(labelContent,DATA_PROPERTY_ICON, SwingConstants.CENTER);
					} else {
						// it was an object property (this is the default)
						labelToAdd = new JLabel(labelContent, OBJECT_PROPERTY_ICON, SwingConstants.CENTER);
					}
					
					labelToAdd.setToolTipText(mappedProperty.toStringID());
					this.getArgumentMappingPanel().add(labelToAdd);
				}
			}
		} else {
			// no owl property found
			this.getArgumentMappingPanel().add(new JLabel(
					this.getResource().getString("NoMappings"), 
					IconController.getInstance().getWarningIcon(), 
					SwingConstants.CENTER
			));
		}
		try {
			System.gc();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		this.getContentPanel().updateUI();
		this.getContentPanel().repaint();
	}

	/**
	 * This method obtains all mapped arguments from the selected node.
	 * @return a mapping from an argument to its OWL property. The last boolean element indicates if the argument
	 * is a subject of the owl property (true) or object (false).
	 */
	protected Map<Argument, Map<OWLProperty, Boolean>> getOWLPropertiesOfArgumentsOfSelectedNode() {
		Map<Argument, Map<OWLProperty, Boolean>> ret = new HashMap<Argument, Map<OWLProperty,Boolean>>();
		
		// assertion
		if (this.getMebn() == null || this.getMediator() == null) {
			return ret;
		}
		
		// get selected node
		INode selectedNode = this.getMediator().getSelectedNode();
		if (selectedNode == null) {
			return ret;
		}
		
		// Ignore nodes that are not resident nodes
		if (!(selectedNode instanceof ResidentNode)) {
			try {
				Debug.println(this.getClass(), selectedNode + " is not a resident node, thus its argument mappings are going to be ignored.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return ret;
		}
		
		// convert to resident node
		ResidentNode resident = (ResidentNode)selectedNode;
		
		// extract owl ontology
		OWLOntology ontology = null;
		if (this.getMebn().getStorageImplementor() != null &&  this.getMebn().getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator) {
			ontology = ((IOWLAPIStorageImplementorDecorator)this.getMebn().getStorageImplementor()).getAdaptee();
		}

		// ignore mebn that do not have an associated ontology
		if (ontology == null) {
			try {
				Debug.println(this.getClass(), mebn + " does not contain an OWL2 ontology.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			return ret;
		}
		
		// extract owl factory
		OWLDataFactory factory = ontology.getOWLOntologyManager().getOWLDataFactory();
		
		// iterate on arguments
		List<Argument> argumentList = resident.getArgumentList();
		if (argumentList != null) {
			for (Argument argument : argumentList) {
				// extract IRIs of "subjectFrom" mapping
				Collection<IRI> iris = IRIAwareMultiEntityBayesianNetwork.getIsSubjectFromMEBN(this.getMebn(), argument);
				if (iris != null) {
					// extract properties from IRI
					for (IRI iri : iris) {
						// add mapping to ret. If mapping exists, just add another entry
						Map<OWLProperty, Boolean> mapping = ret.get(argument);
						if (mapping == null) {
							mapping = new HashMap<OWLProperty, Boolean>();
						}
						// check if iri is an object property or data property
						if (ontology.containsDataPropertyInSignature(iri, true)) {
							// this IRI is a data property
							mapping.put(factory.getOWLDataProperty(iri), true); // true means "add as subject of owl property"
							try {
								Debug.println(this.getClass(), argument + " is subject of data property " + iri);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						} else {
							// default: this IRI is an object property
							mapping.put(factory.getOWLObjectProperty(iri), true); // true means "add as subject of owl property"
							try {
								Debug.println(this.getClass(), argument + " is subject of object property " + iri);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
						ret.put(argument, mapping);
					}
				}
				// extract IRIs of "objectOf" mapping
				iris = IRIAwareMultiEntityBayesianNetwork.getIsObjectFromMEBN(this.getMebn(), argument);
				if (iris != null) {
					// extract properties from IRI
					for (IRI iri : iris) {
						// add mapping to ret. If mapping exists, just add another entry
						Map<OWLProperty, Boolean> mapping = ret.get(argument);
						if (mapping == null) {
							mapping = new HashMap<OWLProperty, Boolean>();
						}
						// check if iri is an object property or data property
						if (ontology.containsDataPropertyInSignature(iri, true)) {
							// this IRI is a data property. 
							mapping.put(factory.getOWLDataProperty(iri), false);	// false means "add as object of owl property"
							try {
								Debug.println(this.getClass(), argument + " is object of data property " + iri + ", but we are not checking type consistency yet.");
							} catch (Throwable t) {
								t.printStackTrace();
							}
						} else {
							// default: this IRI is an object property
							mapping.put(factory.getOWLObjectProperty(iri), false);  // false means "add as object of owl property"
							try {
								Debug.println(this.getClass(), argument + " is object of of object property " + iri);
							} catch (Throwable t) {
								t.printStackTrace();
							}
						}
						ret.put(argument, mapping);
					}
				}
			}
		}
		
		return ret;
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
				&& (this.getMebn().getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator)) {
			
			OWLOntology ontology = ((IOWLAPIStorageImplementorDecorator)this.getMebn().getStorageImplementor()).getAdaptee();
			
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
					ResidentNode selectedNode = (ResidentNode)getSelectedNode();
					IRIAwareMultiEntityBayesianNetwork.addDefineUncertaintyToMEBN(getMebn(), selectedNode, null);
					// clear argument mappings
					for (Argument argument : selectedNode.getArgumentList()) {
						IRIAwareMultiEntityBayesianNetwork.clearObjectMappingOfMEBN(mebn, argument);
						IRIAwareMultiEntityBayesianNetwork.clearSubjectMappingOfMEBN(mebn, argument);
					}
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
		
		// listener for button to map arguments
		this.getMapArgumentsButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showArgumentMappingDialog();
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
	 * Show a popup and force user to map arguments
	 * @return
	 */
	protected void showArgumentMappingDialog() {
		JDialog dialog = new JDialog(this.getMediator().getScreen().getUnbbayesFrame(), this.getResource().getString("EditArgumentMapping"), true);
		dialog.add(ArgumentMappingPanel.newInstance(this.getMebn(), this.getMediator(), this.getMediator().getSelectedNode()));
		dialog.pack();
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
	 * Update the values of {@link #getCurrentlySelectedOWLPropertyLabel()} and {@link #getCurrentlySelectedResidentNodeLabel()}.
	 * It also calls {@link DefinesUncertaintyOfPanel#updateArgumentMappingPanel()}
	 * @see DefinesUncertaintyOfPanel#updateArgumentMappingPanel()
	 */
	public void updateLabels() {
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
				getCurrentlySelectedOWLPropertyLabel().setText(owlProperty.getIRI().getFragment());
				getCurrentlySelectedOWLPropertyLabel().setIcon(DATA_PROPERTY_ICON);
			} else {
				// it was an object property (this is the default)
				getCurrentlySelectedOWLPropertyLabel().setText(owlProperty.getIRI().getFragment());
				getCurrentlySelectedOWLPropertyLabel().setIcon(OBJECT_PROPERTY_ICON);
			}
			getCurrentlySelectedOWLPropertyLabel().setToolTipText(owlProperty.toStringID());
		} else {
			// no owl property found
			getCurrentlySelectedOWLPropertyLabel().setText(getResource().getString("NoSelectedPropertyTitle"));
			getCurrentlySelectedOWLPropertyLabel().setIcon(OBJECT_PROPERTY_ICON);
			getCurrentlySelectedOWLPropertyLabel().setToolTipText(getResource().getString("NoSelectedPropertyTitle"));
		}
		
		// repaint the selected node label
		getCurrentlySelectedResidentNodeLabel().updateUI();
		getCurrentlySelectedResidentNodeLabel().repaint();
		
		// repaint the selected property label
		getCurrentlySelectedOWLPropertyLabel().updateUI();
		getCurrentlySelectedOWLPropertyLabel().repaint();
		
		// update labels of argument mapping panel
		this.updateArgumentMappingPanel();
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

	/**
	 * @return the mapArgumentsButton
	 */
	public AbstractButton getMapArgumentsButton() {
		return mapArgumentsButton;
	}

	/**
	 * @param mapArgumentsButton the mapArgumentsButton to set
	 */
	public void setMapArgumentsButton(AbstractButton mapArgumentsButton) {
		this.mapArgumentsButton = mapArgumentsButton;
	}

	/**
	 * @return the argumentMappingPanel
	 */
	public JPanel getArgumentMappingPanel() {
		return argumentMappingPanel;
	}

	/**
	 * @param argumentMappingPanel the argumentMappingPanel to set
	 */
	public void setArgumentMappingPanel(JPanel argumentMappingPanel) {
		this.argumentMappingPanel = argumentMappingPanel;
	}
	
}
