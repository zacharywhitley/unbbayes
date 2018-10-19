/**
 * 
 */
package unbbayes.gui.mebn.extension;

import java.awt.BorderLayout;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;

import org.protege.editor.core.ui.util.ComponentFactory;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.RemoveAxiom;

import unbbayes.gui.mebn.auxiliary.MebnToolkit;
import unbbayes.gui.mebn.ontology.protege.OWLPropertyViewerPanel;
import unbbayes.io.mebn.MEBNStorageImplementorDecorator;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.protege.IProtegeStorageImplementorDecorator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ontology.protege.OWLPropertyDTO;
import unbbayes.util.Debug;


/**
 * This class extends {@link OWLPropertyViewerPanel} in order to render
 * OWL2 properties.
 * @author Shou Matsumoto
 * @see unbbayes.gui.mebn.extension.OWL2PropertyImportPanelBuilder
 */
public class OWL2PropertyViewerPanel extends OWLPropertyViewerPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 14053783300089793L;
	private OWLOntologyChangeListener owlOntologyChangeListener;
	private int propertySizeToAllowSorting = 1000;


	/**
	 * The default constructor is only visible to subclasses in order to allow
	 * inheritance.
	 * @deprecated use {@link #newInstance(MultiEntityBayesianNetwork)} instead
	 */
	protected OWL2PropertyViewerPanel() {
		// TODO Auto-generated constructor stub
	}

	
	/**
	 * Default constructor method using fields.
	 * @param owlModelHolder : this is a storage implementor. 
	 * Usually, {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork.MultiEntityBayesianNetwork#getStorageImplementor()}
	 * holds a instance of this class.
	 * @return a panel
	 */
	public static OWLPropertyViewerPanel newInstance(MultiEntityBayesianNetwork mebn) {
		final OWL2PropertyViewerPanel ret = new OWL2PropertyViewerPanel ();
		ret.setMebn(mebn);
		ret.initComponents();
		ret.initListeners();
		ret.setVisible(true);
		
		return ret;
	}


	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.ontology.protege.OWLPropertyViewerPanel#initComponents()
	 */
	protected void initComponents() {
		OWLOntology owlOntology = ((IOWLAPIStorageImplementorDecorator)this.getMebn().getStorageImplementor()).getAdaptee();
		OWLOntologyManager ontologyManager = owlOntology.getOWLOntologyManager();
		
		// the list of object properties (including imports)
		List<OWLProperty<?, ?>> objProperties = new ArrayList<OWLProperty<?, ?>>(owlOntology.getObjectPropertiesInSignature(true));
		// extract data properties (including imports)
		List<OWLProperty<?, ?>> dataProperties = new ArrayList<OWLProperty<?, ?>>(owlOntology.getDataPropertiesInSignature(true));
		
		// refill list if there was any change in size
		if (getPropertyList() == null || getPropertyList().getModel().getSize() != objProperties.size() + dataProperties.size()) {
			// fill value of this.getPropertyList()
			this.setUpPropertyList(objProperties, dataProperties);
		}
		
		
		// wrap list with scroll pane
		this.setPropertyListScrollPane(ComponentFactory.createScrollPane(this.getPropertyList()));
		this.getPropertyListScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		// Wrap the list together with a button bar
//        this.setOwlLabeledComponent(new OWLLabeledComponent(this.getResource().getString("OWLProperties"), this.getPropertyListScrollPane()));

		this.setLayout(new BorderLayout());
		this.setBorder(MebnToolkit.getBorderForTabPanel(this.getResource().getString("DnDOWLProperty")));
		this.add(this.getPropertyListScrollPane(), BorderLayout.CENTER);
	}


	/**
	 * Fill {@link #getPropertyList()} with object properties and data properties provided in the argument.
	 * The content of {@link #getPropertyListModel()} will be changed.
	 * The content of {@link #getPropertyList()} will be also changed, because {@link #setPropertyList(JList)} is called
	 * internally.
	 * @param objProperties : the owl object properties to be inserted to the resulting JList.
	 * @param dataProperties  : the owl data properties to be inserted to the resulting JList.
	 * @return a JList to be inserted to {@link #setPropertyListScrollPane(JScrollPane)}
	 * @see #initComponents()
	 */
	protected void setUpPropertyList(List<OWLProperty<?, ?>> objProperties, List<OWLProperty<?, ?>> dataProperties) {
		
		// create/initialize list data
		this.setPropertyListModel(new DefaultListModel());
		
		// prepare comparator so that we can sort the properties by name
		Comparator<OWLProperty<?, ?>> owlPropertyNameComparator = new Comparator<OWLProperty<?, ?>>() {
			public int compare(OWLProperty<?, ?> o1, OWLProperty<?, ?> o2) {
//				try {
//					// compare by the last name, if they are named
//					if (!o1.isAnonymous() && !o2.isAnonymous()) {
//						// both are named properties
//						// case-insensitive comparison to the string after '#' (this is called a "fragment" of an IRI)
//						return o1.getIRI().toString().compareToIgnoreCase(o2.getIRI().toString());
//					} else if (!o1.isAnonymous()) {
//						// o1 is named and o2 is not named. Consider named properties as lower (comes first) in order
//						return -1;
//					} else if (!o2.isAnonymous()) {
//						// o2 is named and o1 is not named. Consider named properties as lower (comes first) in order
//						return 1;
//					}
//				} catch (Exception e) {
//					try {
//						Debug.println(this.getClass(), "Could not compare " + o1 + " and " + o2 + ": " + e.getMessage(), e);
//					} catch (Throwable t) {
//						// TODO: handle exception
//					}
//				}
				// default: string comparison 
				return o1.toString().compareTo(o2.toString());
			}
		};
		
		if (objProperties.size() < getPropertySizeToAllowSorting()) {
			Collections.sort(objProperties, owlPropertyNameComparator);    // sort by name
		}
		
		// fill list model with sorted data
		for (OWLObject property : objProperties) {
			this.getPropertyListModel().addElement(property);
		}
		
		
		// sort data properties if list is smaller than certain limit
		if (dataProperties.size() < getPropertySizeToAllowSorting()) {
			Collections.sort(dataProperties, owlPropertyNameComparator);	// sort by name
		}
		// fill list model with sorted data
		for (OWLObject property : dataProperties) {
			this.getPropertyListModel().addElement(property);
		}
		
		if (this.getPropertyList() == null) {
			// create list on GUI
			this.setPropertyList(new JList(this.getPropertyListModel()));
		} else {
			// reset the property list being filled
			this.getPropertyList().removeAll();
			this.getPropertyList().setModel(this.getPropertyListModel());
			this.getPropertyList().updateUI();
		}
		
		try {
			// If Protege is loaded, make sure entries are shown up nicely, with icons
			this.getPropertyList().setCellRenderer(new OWLCellRenderer(((IProtegeStorageImplementorDecorator)this.getMebn().getStorageImplementor()).getOWLEditorKit()));	
		} catch (Throwable e) {
			Debug.println(this.getClass(), "Could not set up OWLCellRenderer to show properties nicely.", e);
		}
		this.getPropertyList().setDragEnabled(true);	// enable drag and drop
		this.getPropertyList().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}


	/**
	 * If the numbers of properies in the ontolgy is smaller than this value, then the properties will be sorted by name.
	 * @return
	 */
	public int getPropertySizeToAllowSorting() {
		return propertySizeToAllowSorting;
	}
	
	/**
	 * If the numbers of properies in the ontolgy is smaller than this value, then the properties will be sorted by name.
	 * @param propertySizeToAllowSorting
	 */
	public void setPropertySizeToAllowSorting(int propertySizeToAllowSorting) {
		this.propertySizeToAllowSorting = propertySizeToAllowSorting;
	}


	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.ontology.protege.OWLPropertyViewerPanel#initListeners()
	 */
	protected void initListeners() {
		try {
			// create a listener that reloads this panel if an ontology change happens
			this.setOWLOntologyChangeListener(new OWLOntologyChangeListener() {
				public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
					// look for changes on owl properties
					for (OWLOntologyChange change : changes) {
						// reset component only when something was added or removed from ontology (renaming is not supported)
						if ((change instanceof AddAxiom)
								|| change instanceof RemoveAxiom) {
							// reset component only if data properties or object properties were changed
							if ( ( change.getAxiom().getDataPropertiesInSignature() == null 
									|| change.getAxiom().getDataPropertiesInSignature().isEmpty() )
								&& ( change.getAxiom().getObjectPropertiesInSignature() == null 
									|| change.getAxiom().getObjectPropertiesInSignature().isEmpty()) ) {
								continue;
							} else {
								Debug.println(this.getClass(), "Reset OWL2PropertyViewerPanel");
								resetComponents();
								break;
							}
						}
					}
				}
			});
			// add to owl model
			((IOWLAPIStorageImplementorDecorator)getMebn().getStorageImplementor()).getAdaptee().getOWLOntologyManager().addOntologyChangeListener(this.getOWLOntologyChangeListener());
		} catch (Throwable e) {
			Debug.println(getClass(), "Error during initialization of listeners", e);
		}
		
		// what happens when user drag and drops OWL property to graph panel
		this.getPropertyList().setTransferHandler(new TransferHandler () {
			/**
			 * 
			 */
			private static final long serialVersionUID = -902210409564756797L;
			protected Transferable createTransferable(JComponent c) {
				try{
					// obtains the currently selected properties
					Object selectedObjects[] = getPropertyList().getSelectedValues();
					Debug.println(this.getClass(), "Creating transferable element for objects: " + selectedObjects);
					
					// converting to collection, so that we can use OWLPropertyDTO
					Collection<Object> owlProperties = new HashSet<Object>();
					for (Object obj : selectedObjects) {
						owlProperties.add(obj);
					}
					return OWLPropertyDTO.newInstance(owlProperties);
				} catch (Exception e) {
					Debug.println(getClass(), "Error creating transferable", e);
					JOptionPane.showMessageDialog(
							OWL2PropertyViewerPanel.this, 
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
	}


	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.ontology.protege.OWLPropertyViewerPanel#resetComponents()
	 */
	@Override
	public void resetComponents() {
		// remove the old OWLOntologyChangeListener
		try {
			((IOWLAPIStorageImplementorDecorator)getMebn().getStorageImplementor()).getAdaptee().getOWLOntologyManager().removeOntologyChangeListener(this.getOWLOntologyChangeListener());
		} catch (Exception e) {
			// OK, we may have a little memory leak, but the application should work with no much problem
			Debug.println(getClass(), "Error resetting components", e);
		}
		
		super.resetComponents();
	}


	/**
	 * A listener specifying what happens to this panel when the ontology is changed.
	 * This attribute holds the last listener created in {@link #initListeners()}, so {@link #resetComponents()}
	 * must remove this listener from {@link MultiEntityBayesianNetwork#getStorageImplementor()} to avoid
	 * memory leak.
	 * @return the owlOntologyChangeListener
	 */
	public OWLOntologyChangeListener getOWLOntologyChangeListener() {
		return owlOntologyChangeListener;
	}


	/**
	 * A listener specifying what happens to this panel when the ontology is changed.
	 * This attribute holds the last listener created in {@link #initListeners()}, so {@link #resetComponents()}
	 * must remove this listener from {@link MultiEntityBayesianNetwork#getStorageImplementor()} to avoid
	 * memory leak.
	 * @param owlOntologyChangeListener the owlOntologyChangeListener to set
	 */
	public void setOWLOntologyChangeListener(
			OWLOntologyChangeListener owlOntologyChangeListener) {
		this.owlOntologyChangeListener = owlOntologyChangeListener;
	}
	
	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.ontology.protege.OWLPropertyViewerPanel#getOwlModelHolder()
	 */
	public MEBNStorageImplementorDecorator getOwlModelHolder() {
		// this method will not be used anymore.
		return null;
	}
	
}
