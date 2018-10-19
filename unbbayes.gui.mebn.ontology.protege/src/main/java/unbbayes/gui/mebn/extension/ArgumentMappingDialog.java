/**
 * 
 */
package unbbayes.gui.mebn.extension;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.protege.editor.core.ProtegeApplication;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import unbbayes.controller.IconController;
import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.gui.mebn.ontology.protege.IOWLIconsHolder;
import unbbayes.gui.mebn.ontology.protege.PropertySelectionDialog;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.prs.INode;
import unbbayes.prs.mebn.Argument;
import unbbayes.prs.mebn.DefaultMappingArgumentExtractor;
import unbbayes.prs.mebn.IMappingArgumentExtractor;
import unbbayes.prs.mebn.IRIAwareMultiEntityBayesianNetwork;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ResidentNode;
import unbbayes.util.Debug;
import unbbayes.util.ResourceController;

/**
 * This is a panel for mapping arguments of RVs to domains and ranges of
 * owl properties.
 * The data properties used in this class can handle multiple mapping to
 * an argument (so, it can be extended in order to display multiple mappings for 1 argument), 
 * but the GUI itself will display only 0 or 1 mapping per argument.
 * @author Shou Matsumoto
 *
 */
public class ArgumentMappingDialog extends JDialog implements IOWLIconsHolder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8827516891340579422L;

	private Object[] contentOfComboBox;
	
	private MultiEntityBayesianNetwork mebn;
	private IMEBNMediator mediator;
	private INode selectedNode;
	private OWLOntology ontology;
	private JComponent contentPanel;
	
	private ResourceBundle resource;
	private IMappingArgumentExtractor mappingArgumentExtractor;
	private Map<Argument,AbstractButton> selectAnotherOWLPropertyButtonMap;
	private Map<Argument, JLabel> owlPropertyLabelMap;
	private Map<Argument, Map<OWLProperty<?, ?>, Integer>> argumentMapping = null;
	private Map<Argument, Map<OWLProperty<?, ?>,JComboBox<Object>>> subjectOrObjectComboboxMap;
	private JPanel buttonPanel;
	private JButton okButton;
	private JButton cancelButton;
	
	/**
	 * Default constructor is not private in order to allow inheritance.
	 * @deprecated use {@link #newInstance(MultiEntityBayesianNetwork, IMEBNMediator, ResidentNode, OWLOntology)} instead
	 */
	protected ArgumentMappingDialog() {
		super();
		this.setModal(true);
		this.setVisible(false);
		try {
			this.resource = ResourceController.newInstance().getBundle(
					unbbayes.gui.mebn.ontology.protege.resources.Resources.class.getName(),
					Locale.getDefault(),
					this.getClass().getClassLoader());
		} catch (Throwable t) {
			Debug.println(getClass(), "Error getting resource bundle", t);
		}
		try {
			this.mappingArgumentExtractor = DefaultMappingArgumentExtractor.newInstance();
		}  catch (Throwable t) {
			Debug.println(getClass(), "Error when instantiating DefaultMappingArgumentExtractor", t);
		}
		
		// initialize content of combo box
		try {
			if (this.getResource() != null) {
				this.contentOfComboBox = new String[3];
				this.contentOfComboBox[IMappingArgumentExtractor.UNDEFINED_CODE] = this.getResource().getString("NoMappings");
				this.contentOfComboBox[IMappingArgumentExtractor.SUBJECT_CODE] = this.getResource().getString("isSubjectOf");
				this.contentOfComboBox[IMappingArgumentExtractor.OBJECT_CODE] = this.getResource().getString("isObjectIn");
			} 
		} catch (Exception e) {
			// ignore, because we can still go on
			Debug.println(getClass(), "Error getting resources", e);
		}
		// backup plan...
		if (this.contentOfComboBox == null) {
			// this is erroneous...
			this.contentOfComboBox = new String[3];
			this.contentOfComboBox[IMappingArgumentExtractor.UNDEFINED_CODE] = "NONE";
			this.contentOfComboBox[IMappingArgumentExtractor.SUBJECT_CODE] = "SUBJECT";
			this.contentOfComboBox[IMappingArgumentExtractor.OBJECT_CODE] = "OBJECT";
		}
		
	}


//	protected ArgumentMappingDialog(boolean isDoubleBuffered) {
//		super(isDoubleBuffered);
//		// TODO Auto-generated constructor stub
//	}
//
//
//	protected ArgumentMappingDialog(LayoutManager layout, boolean isDoubleBuffered) {
//		super(layout, isDoubleBuffered);
//		// TODO Auto-generated constructor stub
//	}
//
//
//	protected ArgumentMappingDialog(LayoutManager layout) {
//		super(layout);
//		// TODO Auto-generated constructor stub
//	}


	/**
	 * Default constructor method
	 * @param mebn
	 * @param mediator
	 * @param selectedNode
	 * @param ontology
	 * @return
	 */
	public static ArgumentMappingDialog newInstance(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator, INode selectedNode) {
		ArgumentMappingDialog ret =  new ArgumentMappingDialog();
		ret.setMebn(mebn);
		ret.setMediator(mediator);
		ret.setSelectedNode(selectedNode);
		//  extract ontology
		try {
			if (mebn.getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator) {
				ret.setOntology(((IOWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee());
			}
		} catch (Exception e) {
			Debug.println(ArgumentMappingDialog.class, "Error setting Ontology", e);
			JOptionPane.showMessageDialog(
					null, 
					e.getMessage(), 
					ret.getResource().getString("error"), 
					JOptionPane.ERROR_MESSAGE
			);
		}
		ret.initComponents();
		ret.initListeners();
		return ret;
	}

	/**
	 * Removes and re-generates {@link #getContentPanel()} and listeners.
	 */
	public void resetComponents() {
		this.setVisible(false);
		
		// rebuild only the contents
		this.initializeContentPanel();
		this.initializeContentPanelListener();
		
		// force content to update again
		this.getContentPanel().updateUI();
		this.getContentPanel().repaint();
		
		// force dialog to update 
		this.pack();
		this.repaint();
		
//		System.gc();
		
		this.setVisible(true);
	}

	


	/**
	 * Initialize content of this panel.
	 * 
	 */
	protected void initComponents() {
		/*
		 * Basic format of panel:
		 * _________________________________________________________
		 * 		___________________						________
		 * 	st	|is subject in ||V||	isStarshipIn	|select|
		 * 		-------------------						--------
		 * 		___________________						________
		 * 	z	|is object in  ||V||	isStarshipIn	|select|
		 * 		-------------------						--------
		 * _________________________________________________________
		 *  ____	________
		 *  |OK|	|Cancel|
		 *  ----	--------
		 */
		
		this.setLayout(new BorderLayout());		// center: contentPanel; south: buttonPanel
		this.setBackground(Color.WHITE);
//		this.setBorder(MebnToolkit.getBorderForTabPanel(this.getResource().getString("EditArgumentMapping")));
		
		this.setTitle(this.getResource().getString("EditArgumentMapping"));
		
		// instantiate content panel and add to this dialog
		this.initializeContentPanel();
		this.add(new JScrollPane(this.getContentPanel()), BorderLayout.CENTER);
		
		
		// set up panel to hold "OK" and "Cancel" buttons.
		this.setButtonPanel(new JPanel(new FlowLayout(FlowLayout.CENTER)));
//		this.getButtonPanel().setBackground(Color.WHITE);
		this.add(this.getButtonPanel(), BorderLayout.SOUTH);
		
		// set up OK button
		this.setOkButton(new JButton(this.getResource().getString("confirmLabel")));
		this.getOkButton().setToolTipText(this.getResource().getString("confirmToolTip"));
		this.getButtonPanel().add(this.getOkButton());
		
		// set up cancel button
		this.setCancelButton(new JButton(this.getResource().getString("cancelLabel")));
		this.getCancelButton().setToolTipText(this.getResource().getString("cancelToolTip"));
		this.getButtonPanel().add(this.getCancelButton());
		
	}

	/**
	 * This method is called by {@link #initComponents()} in order
	 * to initialize the content of {@link #getContentPanel()}.
	 * This is called by {@link #resetComponents()} in order to 
	 * reset only this portion
	 */
	protected void initializeContentPanel() {
		
		// instantiate content panel if it is null
		if (this.getContentPanel() == null) {
			// col1: argument; col2: subject in | object in; col3: owl property; col4: button to change property
			this.setContentPanel(new JPanel(new GridLayout(0, 4, 2, 10))); 
		} else {
			this.getContentPanel().removeAll();
		}
		this.getContentPanel().setBackground(Color.WHITE);
		this.getContentPanel().setBorder(MebnToolkit.getBorderForTabPanel(this.getResource().getString("ArgumentMapping")));
		
		// initialize the Map to store the buttons for choosing another OWL property for an argument
		if (this.getSelectAnotherOWLPropertyButtonMap() == null) {
			this.setSelectAnotherOWLPropertyButtonMap(new HashMap<Argument, AbstractButton>());
		} else {
			this.getSelectAnotherOWLPropertyButtonMap().clear();	// reuse instance, because callers may have set a map with special behaviors
		}
		
		// initialize map to store combo boxes that allow user to choose whether argument is subject or object of an owl property
		if (this.getSubjectOrObjectComboboxMap() == null) {
			this.setSubjectOrObjectComboboxMap(new HashMap<Argument, Map<OWLProperty<?, ?>, JComboBox<Object>>>());
		} else {
			this.getSubjectOrObjectComboboxMap().clear();	// reuse instance, because callers may have set a map with special behaviors
		}
		
		// initialize map to store JLabel containing the name of owl property
		if (this.getOwlPropertyLabelMap() == null) {
			this.setOwlPropertyLabelMap(new HashMap<Argument, JLabel>());
		} else {
			this.getOwlPropertyLabelMap().clear();	// reuse instance, because callers may have set a map with special behaviors
		}
		
		// initialize main data structure (this.getMappingArgumentExtractor())
		if (this.getSelectedNode() != null 		// we can only extract arguments if node is not null
				&& (this.getSelectedNode() instanceof ResidentNode)) {	// we are only interested in arguments of resident nodes
			
			// fill map or reuse the previous one
			if ( this.getArgumentMapping() == null ) { 
				// set up argument mapping
				this.setArgumentMapping(this.getMappingArgumentExtractor().getOWLPropertiesOfArgumentsOfSelectedNode(this.getSelectedNode(), this.getMebn(), this.getOntology()));
			}
			
			// by reusing the core data (this.getArgumentMapping()), we can reset this component and still use previous data
			
			// convert the selected node to Resident node, so that we can extract all arguments (not only the mapped ones)
			ResidentNode selectedResidentNode = (ResidentNode) this.getSelectedNode();
			
			// create components for mappings (i.e. argumentLabel, dropdownlist, propertyLabel, button)
			for (Argument arg : selectedResidentNode.getArgumentList()) {
				if (arg == null) {
					continue;	// ignore
				}
				// extract mapping
				Map<OWLProperty<?, ?>, Integer> mappedValue = this.getArgumentMapping().get(arg);
				if (mappedValue == null || mappedValue.isEmpty()) {
					// this argument has no mapping. Create a placeholder in order to allow specifying new mapping
					
					// argument label (the name of ordinary variable)
					this.getContentPanel().add(new JLabel(arg.getOVariable().getName(), IconController.getInstance().getOVariableNodeIcon(), JLabel.LEADING));
					
					// combo box (dropdown list) for choosing whether "subject" or "object" of an owl property
					JComboBox<Object> comboBox = new JComboBox<Object>(this.getContentOfComboBox());
					comboBox.setToolTipText(this.getResource().getString("chooseTypeOfMapping"));
					
					// no mapping. Set disabled and pointing to 1st element
					comboBox.setSelectedIndex(IMappingArgumentExtractor.UNDEFINED_CODE);
					comboBox.setEnabled(false);
					
					// add combo box to panel and map
					this.getContentPanel().add(comboBox);
//					this.getSubjectOrObjectComboboxMap().put(arg, comboBox);
					
					// Empty label of owl property will change depending on the property type
					JLabel owlPropertyLabel = new JLabel(this.getResource().getString("NoMappings"), IconController.getInstance().getWarningIcon(), JLabel.CENTER);
					this.getContentPanel().add(owlPropertyLabel);
					this.getOwlPropertyLabelMap().put(arg, owlPropertyLabel);
					
					// button to change owl property
					JButton selectAnotherPropertyButton = new JButton(PROPERTY_USAGE_ICON);
					selectAnotherPropertyButton.setToolTipText(this.getResource().getString("SelectOWLPropertyToolTip"));
					
					// add button to content panel and map
					this.getContentPanel().add(selectAnotherPropertyButton);
					this.getSelectAnotherOWLPropertyButtonMap().put(arg, selectAnotherPropertyButton);
				
				} else {
					
					// this argument already has a mapping. Show it and allow user to change config
					for (OWLProperty<?, ?> property : mappedValue.keySet()) {	// extract property
						
						// argument label (the name of ordinary variable)
						this.getContentPanel().add(new JLabel(arg.getOVariable().getName(), IconController.getInstance().getOVariableNodeIcon(), JLabel.LEADING));
						
						// combo box (dropdown list) for choosing whether "subject" or "object" of an owl property
						JComboBox<Object> comboBox = new JComboBox<Object>(this.getContentOfComboBox());
						comboBox.setToolTipText(this.getResource().getString("chooseTypeOfMapping"));
						
						// choose initial values of combo box
						if (property == null || mappedValue.get(property) == null) {
							// no mapping. Set disabled and pointing to 1st element
							comboBox.setSelectedIndex(IMappingArgumentExtractor.UNDEFINED_CODE);
							comboBox.setEnabled(false);
						} else {
							// there is a mapping already. Set it
							comboBox.setSelectedIndex(mappedValue.get(property));
						}
						
						// add combo box to panel 
						this.getContentPanel().add(comboBox);
						
						// add combo box to map
						Map<OWLProperty<?, ?>, JComboBox<Object>> comboBoxMap = getSubjectOrObjectComboboxMap().get(arg);
						if (comboBoxMap == null) {
							comboBoxMap = new HashMap<OWLProperty<?, ?>, JComboBox<Object>>();
						}
						comboBoxMap.put(property, comboBox);
						this.getSubjectOrObjectComboboxMap().put(arg, comboBoxMap);
						
						// label of owl property will change depending on the property type
						JLabel owlPropertyLabel = null;
						if (this.getOntology().containsDataPropertyInSignature(property.getIRI(), true)) {
							// this is a datatype property
							owlPropertyLabel = new JLabel(property.getIRI().getFragment(), DATA_PROPERTY_ICON, JLabel.CENTER);
						} else {
							// default: object property
							owlPropertyLabel = new JLabel(property.getIRI().getFragment(), OBJECT_PROPERTY_ICON, JLabel.CENTER);
						}
						owlPropertyLabel.setToolTipText(property.getIRI().toString());	// tool tip is the complete IRI of property, not just the fragment
						
						// add label to panel and to map
						this.getContentPanel().add(owlPropertyLabel);
						this.getOwlPropertyLabelMap().put(arg, owlPropertyLabel);
						
						// button to change owl property
						JButton selectAnotherPropertyButton = new JButton(PROPERTY_USAGE_ICON);
						selectAnotherPropertyButton.setToolTipText(this.getResource().getString("SelectOWLPropertyToolTip"));
						
						// add button to content panel and map
						this.getContentPanel().add(selectAnotherPropertyButton);
						this.getSelectAnotherOWLPropertyButtonMap().put(arg, selectAnotherPropertyButton);
					}
				}
			}
		}
	}


	/**
	 * This is called by {@link #initListeners()} in order to initialize
	 * only the listeners of components created in {@link #initializeContentPanel()}.
	 * This is also called by {@link #resetComponents()} in order to refill
	 * only listeners of {@link #getContentPanel()}.
	 */
	protected void initializeContentPanelListener() {
		// set up buttons for changing OWL property associated to argument
		for (Argument key : this.getSelectAnotherOWLPropertyButtonMap().keySet()) {
			// parameter for inner class (listener)
			final Argument argOfButton = key;
			// get the button to add listener
			AbstractButton button = this.getSelectAnotherOWLPropertyButtonMap().get(argOfButton);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					// show dialog to select property
					final PropertySelectionDialog dialog = PropertySelectionDialog.newInstance(getMebn());
					dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dialog.setModal(true);	// force user to choose a property
					dialog.setVisible(true);
					
					try {
						// get selected property
						OWLProperty<?, ?> selectedProperty = dialog.getSelectedValue();
						Debug.println(this.getClass(), "Selected property is " + selectedProperty);
						if (selectedProperty == null) {
							// no selection means it was canceled. Do not change
							return;
						}
						
						// create a mapping only in the main data object (getArgumentMapping()) without changing MEBN (we should only change MEBN on commit)
						Map<OWLProperty<?, ?>, Integer> mapping = getArgumentMapping().get(argOfButton);
						
						// remove all other mappings (because this version of UnBBayes allow only 1 mapping for an argument)
						if (mapping != null) {
							mapping.clear();
						} else {
							mapping = new HashMap<OWLProperty<?, ?>, Integer>();
						}
						
						// add property, but indicate that this is neither subject nor object yet (i.e. undefined)
						mapping.put(selectedProperty, IMappingArgumentExtractor.UNDEFINED_CODE);
						
						// put to main data object (getArgumentMapping()) again just to make sure 
						getArgumentMapping().put(argOfButton, mapping);
						
//						// if button is changed, we should enable combo box
//						getSubjectOrObjectComboboxMap().get(argOfButton).setEnabled(true);
						
						// reset ArgumentMappingPane, so that components are rebuilt using new values in main data object (getArgumentMapping()) 
						resetComponents();
					} catch (Exception exc) {
						Debug.println(getClass(), "Error when during OWL Property ToolTip selection", exc);
						JOptionPane.showMessageDialog(
								dialog, 
								getResource().getString("SelectOWLPropertyToolTip"), 
								exc.getClass().getName(), 
								JOptionPane.ERROR_MESSAGE
						);
					}
				}
			});
		}
		
	}

	/**
	 * Initialize listeners of components created in {@link #initComponents()}
	 */
	protected void initListeners() {
		// listeners of getContentPanel
		this.initializeContentPanelListener();
		
		// set up OK button (commit)
		this.getOkButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// obtain all arguments from selected node
				INode selectedNode = getSelectedNode();
				if (selectedNode == null || !(selectedNode instanceof ResidentNode)) {
					JOptionPane.showMessageDialog(
							ArgumentMappingDialog.this, 
							getResource().getString("invalidSelectedNode"), 
							getResource().getString("NoSelectedPropertyTitle"), 
							JOptionPane.ERROR_MESSAGE
					);
					return;
				}
				// convert selected node to resident node
				ResidentNode resident = (ResidentNode)selectedNode;
				
				// for each argument, update mapping in MEBN
				for (Argument arg : resident.getArgumentList()) {
					if (arg == null) {
						continue;	// ignore
					}
					try {
						// extract properties from argument
						Map<OWLProperty<?, ?>, Integer> mapping = getArgumentMapping().get(arg);
						
						// assume this is a "complete" mapping, containing type (none/subject/object) and property. 
						// If not, it will throw NullPointerException and will be handled by "catch"
						for (OWLProperty<?, ?> property : mapping.keySet()) {
							// the type of mapping (subject/object) may not be synchronized (because I didn't add a listener for combobox)... 
							try {
								// ...so extract values directly from combo box
								JComboBox<Object> comboBox = getSubjectOrObjectComboboxMap().get(arg).get(property);
								
								// get type of mapping (subject or object). The index is synchronized to type code
								if (comboBox.getSelectedIndex() == IMappingArgumentExtractor.SUBJECT_CODE) {
									// this is a subject
									IRIAwareMultiEntityBayesianNetwork.addSubjectToMEBN(getMebn(), arg, property.getIRI());
									// thus, this is not an object
									try {
										// Clear the whole mapping. Change this code to allow multiple mapping
										IRIAwareMultiEntityBayesianNetwork.clearObjectMappingOfMEBN(getMebn(), arg);
									} catch (Exception ee) {
										Debug.println(getClass(), ee.getMessage(), ee);
									}
								} else if (comboBox.getSelectedIndex() == IMappingArgumentExtractor.OBJECT_CODE) {
									// this is an object
									IRIAwareMultiEntityBayesianNetwork.addObjectToMEBN(getMebn(), arg, property.getIRI());
									// thus, this is not a subject
									try {
										// Clear the whole mapping. Change this code to allow multiple mapping
										IRIAwareMultiEntityBayesianNetwork.clearSubjectMappingOfMEBN(getMebn(), arg);
									} catch (Exception ee) {
										Debug.println(getClass(), ee.getMessage(), ee);
									}
								} else {
									// default: no mapping. This is explicitly set to an invalid mapping type
									try {
										// Clear the whole mapping. Change this code to allow multiple mapping
										IRIAwareMultiEntityBayesianNetwork.clearObjectMappingOfMEBN(getMebn(), arg);
									} catch (Exception ee) {
										Debug.println(getClass(), ee.getMessage(), ee);
									}
									try {
										// Clear the whole mapping. Change this code to allow multiple mapping
										IRIAwareMultiEntityBayesianNetwork.clearSubjectMappingOfMEBN(getMebn(), arg);
									} catch (Exception ee) {
										Debug.println(getClass(), ee.getMessage(), ee);
									}
								}
							} catch (Exception e2) {
								// there is something wrong with this mapping...
								Debug.println(getClass(), "Error when during OWL Property selection", e2);
								JOptionPane.showMessageDialog(
										ArgumentMappingDialog.this, 
										getResource().getString("invalidSelectedNode"), 
										e2.getMessage(), 
										JOptionPane.ERROR_MESSAGE
								);
								continue;	//...but keep trying other properties
							}
						}
					} catch (Exception exc) {
						// there is no mapping at all
						Debug.println(this.getClass(), exc.getMessage(), exc);
						// clear map from mebn
						IRIAwareMultiEntityBayesianNetwork.clearObjectMappingOfMEBN(getMebn(), arg);
						IRIAwareMultiEntityBayesianNetwork.clearSubjectMappingOfMEBN(getMebn(), arg);
						continue;
					}
				}
				
				// hide and dispose dialog
				ArgumentMappingDialog.this.setVisible(false);
				if (ArgumentMappingDialog.this.getDefaultCloseOperation() == ArgumentMappingDialog.DISPOSE_ON_CLOSE) {
					ArgumentMappingDialog.this.dispose();
				}
			}
		});
		
		// set up cancel button
		this.getCancelButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArgumentMappingDialog.this.setVisible(false);
				if (ArgumentMappingDialog.this.getDefaultCloseOperation() == ArgumentMappingDialog.DISPOSE_ON_CLOSE) {
					ArgumentMappingDialog.this.dispose();
				}
			}
		});
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
	 * @return the selectedNode
	 */
	public INode getSelectedNode() {
		return selectedNode;
	}

	/**
	 * @param selectedNode the selectedNode to set
	 */
	public void setSelectedNode(INode selectedNode) {
		this.selectedNode = selectedNode;
	}

	/**
	 * @return the ontology
	 */
	public OWLOntology getOntology() {
		return ontology;
	}

	/**
	 * @param ontology the ontology to set
	 */
	public void setOntology(OWLOntology ontology) {
		this.ontology = ontology;
	}


	/**
	 * @return the contentPanel
	 */
	public JComponent getContentPanel() {
		return contentPanel;
	}


	/**
	 * @param contentPanel the contentPanel to set
	 */
	public void setContentPanel(JComponent contentPanel) {
		this.contentPanel = contentPanel;
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
	 * This object will extract the relationships between arguments of nodes and OWL properties.
	 * @return the mappingArgumentExtractor
	 */
	public IMappingArgumentExtractor getMappingArgumentExtractor() {
		return mappingArgumentExtractor;
	}

	/**
	 * This object will extract the relationships between arguments of nodes and OWL properties.
	 * @param mappingArgumentExtractor the mappingArgumentExtractor to set
	 */
	public void setMappingArgumentExtractor(
			IMappingArgumentExtractor mappingArgumentExtractor) {
		this.mappingArgumentExtractor = mappingArgumentExtractor;
	}

	/**
	 * This map is the main data structure that stores arguments and
	 * its mappings to OWL properties. It can be initialized by
	 * calling {@link IMappingArgumentExtractor#getOWLPropertiesOfArgumentsOfSelectedNode(INode, MultiEntityBayesianNetwork, OWLOntology)}
	 * @return the argumentMapping
	 * @see #getMappingArgumentExtractor()
	 */
	public Map<Argument, Map<OWLProperty<?, ?>, Integer>> getArgumentMapping() {
		return argumentMapping;
	}


	/**
	 * This map is the main data structure that stores arguments and
	 * its mappings to OWL properties. It can be initialized by
	 * calling {@link IMappingArgumentExtractor#getOWLPropertiesOfArgumentsOfSelectedNode(INode, MultiEntityBayesianNetwork, OWLOntology)}.
	 * Setting this value to null will force {@link #initComponents()} and {@link #resetComponents()} to
	 * reload this map from {@link #getMebn()}.
	 * @param argumentMapping the argumentMapping to set
	 */
	public void setArgumentMapping(
			Map<Argument, Map<OWLProperty<?, ?>, Integer>> argumentMapping) {
		this.argumentMapping = argumentMapping;
	}


	/**
	 * This map can be used togather with {@link #getArgumentMapping()} in order
	 * to set up listeners for buttons related to arguments.
	 * @return the selectAnotherOWLPropertyButtonMap
	 * @see #initListeners()
	 */
	public Map<Argument, AbstractButton> getSelectAnotherOWLPropertyButtonMap() {
		return selectAnotherOWLPropertyButtonMap;
	}


	/**
	 * This map can be used together with {@link #getArgumentMapping()} in order
	 * to set up listeners for buttons related to arguments.
	 * @param selectAnotherOWLPropertyButtonMap the selectAnotherOWLPropertyButtonMap to set
	 * @see #initListeners()
	 */
	public void setSelectAnotherOWLPropertyButtonMap(
			Map<Argument, AbstractButton> selectAnotherOWLPropertyButtonMap) {
		this.selectAnotherOWLPropertyButtonMap = selectAnotherOWLPropertyButtonMap;
	}


	/**
	 * This map stores what JComboBox is associated to an argument.
	 * This combo box is used in this panel in order to select if
	 * the argument is a subject or an object of an OWL property.
	 * @return the subjectOrObjectComboboxMap
	 * @see #getArgumentMapping()
	 */
	public Map<Argument, Map<OWLProperty<?, ?>, JComboBox<Object>>> getSubjectOrObjectComboboxMap() {
		return subjectOrObjectComboboxMap;
	}


	/**
	 * This map stores what JComboBox is associated to an argument.
	 * This combo box is used in this panel in order to select if
	 * the argument is a subject or an object of an OWL property.
	 * @param subjectOrObjectComboboxMap the subjectOrObjectComboboxMap to set
	 * @see #getArgumentMapping()
	 */
	public void setSubjectOrObjectComboboxMap(
			Map<Argument, Map<OWLProperty<?, ?>, JComboBox<Object>>> subjectOrObjectComboboxMap) {
		this.subjectOrObjectComboboxMap = subjectOrObjectComboboxMap;
	}


	/**
	 * @return the buttonPanel
	 */
	public JPanel getButtonPanel() {
		return buttonPanel;
	}


	/**
	 * @param buttonPanel the buttonPanel to set
	 */
	public void setButtonPanel(JPanel buttonPanel) {
		this.buttonPanel = buttonPanel;
	}


	/**
	 * @return the okButton
	 */
	public JButton getOkButton() {
		return okButton;
	}


	/**
	 * @param okButton the okButton to set
	 */
	public void setOkButton(JButton okButton) {
		this.okButton = okButton;
	}


	/**
	 * @return the cancelButton
	 */
	public JButton getCancelButton() {
		return cancelButton;
	}


	/**
	 * @param cancelButton the cancelButton to set
	 */
	public void setCancelButton(JButton cancelButton) {
		this.cancelButton = cancelButton;
	}


	/**
	 * This is the default content of combo boxes in this panel.
	 * The index of contents and their meanings must be set
	 * according to the Integer values in {@link #getSubjectOrObjectComboboxMap()}
	 * @return a contentOfComboBox
	 */
	protected Object[] getContentOfComboBox() {
		return contentOfComboBox;
	}


	/**
	 * This is the default content of combo boxes in this panel.
	 * You should call {@link #resetComponents()} after setting
	 * this value in order for changes to make effects.
	 * The index of contents and their meanings must be set
	 * according to the Integer values in {@link #getSubjectOrObjectComboboxMap()}
	 * @param contentOfComboBox the contentOfComboBox to set
	 */
	protected void setContentOfComboBox(Object[] contentOfComboBox) {
		this.contentOfComboBox = contentOfComboBox;
	}


	/**
	 * This label can be used to obtain what JLabel is related
	 * to an argument.
	 * @return the owlPropertyLabelMap
	 * @see #getArgumentMapping()
	 */
	public Map<Argument, JLabel> getOwlPropertyLabelMap() {
		return owlPropertyLabelMap;
	}


	/**
	 * This label can be used to obtain what JLabel is related
	 * to an argument.
	 * @param owlPropertyLabelMap the owlPropertyLabelMap to set
	 * @see #getArgumentMapping()
	 */
	public void setOwlPropertyLabelMap(Map<Argument, JLabel> owlPropertyLabelMap) {
		this.owlPropertyLabelMap = owlPropertyLabelMap;
	}
	

}
