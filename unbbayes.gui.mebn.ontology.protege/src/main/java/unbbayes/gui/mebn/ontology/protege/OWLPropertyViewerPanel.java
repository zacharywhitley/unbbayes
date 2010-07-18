/**
 * 
 */
package unbbayes.gui.mebn.ontology.protege;

import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.io.mebn.MEBNStorageImplementorDecorator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ontology.protege.OWLPropertyDTO;
import unbbayes.util.Debug;
import unbbayes.util.ResourceController;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.OWLObjectProperty;
import edu.stanford.smi.protegex.owl.model.RDFProperty;
import edu.stanford.smi.protegex.owl.model.RDFSNamedClass;
import edu.stanford.smi.protegex.owl.model.event.ModelAdapter;
import edu.stanford.smi.protegex.owl.ui.OWLLabeledComponent;
import edu.stanford.smi.protegex.owl.ui.ProtegeUI;
import edu.stanford.smi.protegex.owl.ui.ResourceRenderer;
import edu.stanford.smi.protegex.owl.ui.icons.OWLIcons;
import edu.stanford.smi.protegex.owl.ui.widget.OWLUI;

/**
 * This panel shows OWL properties
 * @author Shou Matsumoto
 *
 */
public class OWLPropertyViewerPanel extends JPanel {


	private ResourceBundle resource;
	
	private MultiEntityBayesianNetwork mebn;
	private DefaultListModel propertyListModel;
	private JList propertyList;

	private JScrollPane propertyListScrollPane;

	private OWLLabeledComponent owlLabeledComponent;

	private ModelAdapter modelListener;
	
	/**
	 * Default constructor is at least protected to allow inheritance. 
	 */
	protected OWLPropertyViewerPanel() {
		this.resource = ResourceController.newInstance().getBundle(
			unbbayes.gui.mebn.ontology.protege.resources.Resources.class.getName(),
			Locale.getDefault(),
			this.getClass().getClassLoader());
	}
		
	/**
	 * Default constructor method using fields.
	 * @param owlModelHolder : this is a storage implementor. 
	 * Usually, {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork.MultiEntityBayesianNetwork#getStorageImplementor()}
	 * holds a instance of this class.
	 * @return a panel
	 */
	public static OWLPropertyViewerPanel newInstance(MultiEntityBayesianNetwork mebn) {
		OWLPropertyViewerPanel ret = new OWLPropertyViewerPanel ();
		ret.setMebn(mebn);
		ret.initComponents();
		ret.initListeners();
		ret.setVisible(true);
		return ret;
	}

	/**
	 * Initialize components. This is called within {@link #newInstance(MEBNStorageImplementorDecorator)}
	 * to build this panel
	 */
	protected void initComponents() {
		OWLModel owlModel = this.getOwlModelHolder().getAdaptee();
		
		// create list data
		this.setPropertyListModel(new DefaultListModel());
		
		// fill list data with RDF properties (mostly OWL properties)
		for (Object property : owlModel.getRDFProperties()) {
			this.getPropertyListModel().addElement(property);
		}
		
		// create list on GUI
		this.setPropertyList(new JList(this.getPropertyListModel()));
		this.getPropertyList().setCellRenderer(new ResourceRenderer());	// make sure entries are shown up nicely, with icons
		this.getPropertyList().setDragEnabled(true);	// enable drag and drop
		this.getPropertyList().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		// wrap list with scroll pane
		this.setPropertyListScrollPane(new JScrollPane(this.getPropertyList(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
		
		// Wrap the list together with a button bar
        this.setOwlLabeledComponent(new OWLLabeledComponent(this.getResource().getString("OWLProperties"), this.getPropertyListScrollPane()));

		this.setLayout(new BorderLayout());
		this.setBorder(MebnToolkit.getBorderForTabPanel(this.getResource().getString("DnDOWLProperty")));
		this.add(owlLabeledComponent, BorderLayout.CENTER);
	}

	/**
	 * Initialize components' listeners. This is called within {@link #newInstance(MEBNStorageImplementorDecorator)}
	 * to create listeners of components
	 */
	protected void initListeners() {
		
		// what happens when OWLModel changes (someone adds or removes properties)
		if (this.getOwlModelHolder() != null && this.getOwlModelHolder().getAdaptee() != null) {
			this.setModelListener(new ModelAdapter() {
				public void propertyCreated(RDFProperty arg0) {
					super.propertyCreated(arg0);
					getPropertyListModel().addElement(arg0);
		            getPropertyList().setSelectedValue(arg0, true);
				}
				public void propertyDeleted(RDFProperty arg0) {
					super.propertyDeleted(arg0);
					getPropertyListModel().removeElement(arg0);
					getPropertyList().updateUI();
					getPropertyList().repaint();
				}
			});
			this.getOwlModelHolder().getAdaptee().addModelListener(this.getModelListener());
		}
		
		// what happens when someone presses the add property option.
		if (this.getOwlLabeledComponent() != null) {
			this.getOwlLabeledComponent().addHeaderButton(new AbstractAction(this.getResource().getString("AddProperty"), OWLIcons.getAddIcon(OWLIcons.RDF_PROPERTY)) {
				public void actionPerformed(ActionEvent e) {
					try {
						// ask the name of new property
						String name = JOptionPane.showInputDialog(getResource().getString("EnterNameOfNewProperty"));
						if (name != null && (name.trim().length() > 0)) {
							// select the domain
							RDFSNamedClass domain =  ProtegeUI.getSelectionDialogFactory().selectClass(OWLPropertyViewerPanel.this, getOwlModelHolder().getAdaptee(), getResource().getString("ChooseOWLPropertyDomain"));
							// select the range
							RDFSNamedClass range =  ProtegeUI.getSelectionDialogFactory().selectClass(OWLPropertyViewerPanel.this, getOwlModelHolder().getAdaptee(), getResource().getString("ChooseOWLPropertyRange"));
							OWLObjectProperty owlProperty = getOwlModelHolder().getAdaptee().createOWLObjectProperty(name);
							if (range != null) {
								// set the range
								owlProperty.setRange(range);
							}
							if (domain != null) {
								// set the domain
								owlProperty.setDomain(domain);
							}
						} 
					} catch (Exception exc) {
						exc.printStackTrace();
						JOptionPane.showMessageDialog(
								OWLPropertyViewerPanel.this, 
								getResource().getString("SelectedPropertyError"),
								getResource().getString("NoSelectedPropertyTitle"), 
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		
		// what happens when someone presses the remove property option.
		if (this.getOwlLabeledComponent() != null) {
			this.getOwlLabeledComponent().addHeaderButton(new AbstractAction(this.getResource().getString("RemoveProperty"), OWLIcons.getRemoveIcon(OWLIcons.RDF_PROPERTY)) {
				public void actionPerformed(ActionEvent e) {
					// notify user that removal is extremely dangerous for ontology consistency
					int option = JOptionPane.showConfirmDialog(
							OWLPropertyViewerPanel.this, 
							getResource().getString("PropertyRemovalMessage"),
							getResource().getString("PropertyRemovalTitle"), 
							JOptionPane.YES_NO_OPTION);
					if (option != JOptionPane.YES_OPTION) {
						// user did not press "yes"
						return;
					}
					if (getPropertyList().getSelectedIndex() < 0) {
						JOptionPane.showMessageDialog(
								OWLPropertyViewerPanel.this, 
								getResource().getString("NoSelectedProperty"),
								getResource().getString("NoSelectedPropertyTitle"), 
								JOptionPane.WARNING_MESSAGE);
					}
					try {
						getOwlModelHolder().getAdaptee().getRDFProperty(((RDFProperty)getPropertyList().getSelectedValue()).getName()).delete();
					} catch (Exception exc) {
						exc.printStackTrace();
						JOptionPane.showMessageDialog(
								OWLPropertyViewerPanel.this, 
								getResource().getString("SelectedPropertyError"), 
								getResource().getString("NoSelectedPropertyTitle"), 
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		
		// what happens when user drag and drops OWL property to graph panel
		this.getPropertyList().setTransferHandler(new TransferHandler () {
			protected Transferable createTransferable(JComponent c) {
				try{
					// obtains the currently selected properties
					Object selectedObjects[] = getPropertyList().getSelectedValues();
					Debug.println(this.getClass(), "Creating transferable element for objects: " + selectedObjects);
					
					// converting to collection, so that we can use OWLPropertyDTO
					Collection<RDFProperty> rdfProperties = new HashSet<RDFProperty>();
					for (Object obj : selectedObjects) {
						if (obj instanceof RDFProperty) {
							rdfProperties.add((RDFProperty)obj);
						} else {
							Debug.println(this.getClass(), "Something that is not a RDF property was selected: " + obj);
						}
					}
					return OWLPropertyDTO.newInstance(rdfProperties);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(
							OWLPropertyViewerPanel.this, 
							e.getMessage(), 
							"TransferHandler", 
							JOptionPane.ERROR_MESSAGE);
				}
				return null;
			}
			public int getSourceActions(JComponent c) {
				// declares that only copy mode is enabled
				// copy mode means that the source is not deleted
				return TransferHandler.COPY;
			}
		});
		

		// TODO edit property
	}
	
	/**
	 * Removes all components and calls
	 * {@link #initComponents()} and then
	 * {@link #initListeners()}
	 */
	public void resetComponents() {
		// remove old listeners of "global" objects, to avoid memory leak
		if (this.getOwlModelHolder()!= null && this.getOwlModelHolder().getAdaptee() != null && this.getModelListener() != null) {
			this.getOwlModelHolder().getAdaptee().removeModelListener(this.getModelListener());
			this.setModelListener(null);
		}
		this.removeAll();
		this.initComponents();
		this.initListeners();
	}

	/**
	 * This is a wrapper for {@link MultiEntityBayesianNetwork#getStorageImplementor()}
	 * @return the owlModelHolder or null if it cannot be extracted
	 */
	public MEBNStorageImplementorDecorator getOwlModelHolder() {
		try {
			return (MEBNStorageImplementorDecorator)this.getMebn().getStorageImplementor();
		} catch (Exception e) {
			// probably, a nullpointerexception or classcastexception...
			e.printStackTrace();
		}
		return null;
	}


	/**
	 * @return the propertyListModel
	 */
	public DefaultListModel getPropertyListModel() {
		return propertyListModel;
	}

	/**
	 * @param propertyListModel the propertyListModel to set
	 */
	public void setPropertyListModel(DefaultListModel propertyListModel) {
		this.propertyListModel = propertyListModel;
	}

	/**
	 * @return the propertyList
	 */
	public JList getPropertyList() {
		return propertyList;
	}

	/**
	 * @param propertyList the propertyList to set
	 */
	public void setPropertyList(JList propertyList) {
		this.propertyList = propertyList;
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
	 * @return the propertyListScrollPane
	 */
	public JScrollPane getPropertyListScrollPane() {
		return propertyListScrollPane;
	}

	/**
	 * @param propertyListScrollPane the propertyListScrollPane to set
	 */
	public void setPropertyListScrollPane(JScrollPane propertyListScrollPane) {
		this.propertyListScrollPane = propertyListScrollPane;
	}

	/**
	 * @return the owlLabeledComponent
	 */
	public OWLLabeledComponent getOwlLabeledComponent() {
		return owlLabeledComponent;
	}

	/**
	 * @param owlLabeledComponent the owlLabeledComponent to set
	 */
	public void setOwlLabeledComponent(OWLLabeledComponent owlLabeldComponent) {
		this.owlLabeledComponent = owlLabeldComponent;
	}

	/**
	 * @return the modelListener
	 */
	public ModelAdapter getModelListener() {
		return modelListener;
	}

	/**
	 * @param modelListener the modelListener to set
	 */
	public void setModelListener(ModelAdapter modelListener) {
		this.modelListener = modelListener;
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

	

}
