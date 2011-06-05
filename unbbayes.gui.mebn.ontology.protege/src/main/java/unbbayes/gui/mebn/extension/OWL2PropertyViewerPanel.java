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
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;

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

	private OWLOntologyChangeListener owlOntologyChangeListener;


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
		OWLDataFactory owlModel = ontologyManager.getOWLDataFactory();
		
		// create list data
		this.setPropertyListModel(new DefaultListModel());
		
		// prepare comparator so that we can sort the properties by name
		Comparator<OWLProperty> owlPropertyNameComparator = new Comparator<OWLProperty>() {
			public int compare(OWLProperty o1, OWLProperty o2) {
				try {
					// compare by the last name, if they are named
					if (!o1.isAnonymous() && !o2.isAnonymous()) {
						// both are named properties
						// case-insensitive comparison to the string after '#' (this is called a "fragment" of an IRI)
						return o1.getIRI().getFragment().compareToIgnoreCase(o2.getIRI().getFragment());
					} else if (!o1.isAnonymous()) {
						// o1 is named and o2 is not named. Consider named properties as lower (comes first) in order
						return -1;
					} else if (!o2.isAnonymous()) {
						// o2 is named and o1 is not named. Consider named properties as lower (comes first) in order
						return 1;
					}
				} catch (Exception e) {
					try {
						Debug.println(this.getClass(), "Could not compare " + o1 + " and " + o2 + ": " + e.getMessage(), e);
					} catch (Throwable t) {
						// TODO: handle exception
					}
				}
				// default: string comparison 
				return o1.toString().compareTo(o2.toString());
			}
		};
		
		// fill list data with object properties (including imports)
		List<OWLProperty> properties = new ArrayList<OWLProperty>(owlOntology.getObjectPropertiesInSignature(true));
		Collections.sort(properties, owlPropertyNameComparator);    // sort by name
		// fill list model with sorted data
		for (OWLObject property : properties) {
			this.getPropertyListModel().addElement(property);
		}
		
		
		// fill list data with data properties (including imports)
		properties = new ArrayList<OWLProperty>(owlOntology.getDataPropertiesInSignature(true));
		Collections.sort(properties, owlPropertyNameComparator);	// sort by name
		// fill list model with sorted data
		for (OWLObject property : properties) {
			this.getPropertyListModel().addElement(property);
		}
		
		// create list on GUI
		this.setPropertyList(new JList(this.getPropertyListModel()));
		try {
			// If Protege is loaded, make sure entries are shown up nicely, with icons
			this.getPropertyList().setCellRenderer(new OWLCellRenderer(((IProtegeStorageImplementorDecorator)this.getMebn().getStorageImplementor()).getOWLEditorKit()));	
		} catch (Throwable e) {
			Debug.println(this.getClass(), "Could not set up OWLCellRenderer to show properties nicely.", e);
		}
		this.getPropertyList().setDragEnabled(true);	// enable drag and drop
		this.getPropertyList().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		// wrap list with scroll pane
		this.setPropertyListScrollPane(ComponentFactory.createScrollPane(this.getPropertyList()));
		this.getPropertyListScrollPane().setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		// Wrap the list together with a button bar
//        this.setOwlLabeledComponent(new OWLLabeledComponent(this.getResource().getString("OWLProperties"), this.getPropertyListScrollPane()));

		this.setLayout(new BorderLayout());
		this.setBorder(MebnToolkit.getBorderForTabPanel(this.getResource().getString("DnDOWLProperty")));
		this.add(this.getPropertyListScrollPane(), BorderLayout.CENTER);
	}


	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.ontology.protege.OWLPropertyViewerPanel#initListeners()
	 */
	protected void initListeners() {
		try {
			// create a listener that reloads this panel if an ontology change happens
			this.setOWLOntologyChangeListener(new OWLOntologyChangeListener() {
				public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
					Debug.println(this.getClass(), "Reset OWL2PropertyViewerPanel");
					resetComponents();
				}
			});
			// add to owl model
			((IOWLAPIStorageImplementorDecorator)getMebn().getStorageImplementor()).getAdaptee().getOWLOntologyManager().addOntologyChangeListener(this.getOWLOntologyChangeListener());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		// what happens when user drag and drops OWL property to graph panel
		this.getPropertyList().setTransferHandler(new TransferHandler () {
			protected Transferable createTransferable(JComponent c) {
				try{
					// obtains the currently selected properties
					Object selectedObjects[] = getPropertyList().getSelectedValues();
					Debug.println(this.getClass(), "Creating transferable element for objects: " + selectedObjects);
					
					// converting to collection, so that we can use OWLPropertyDTO
					Collection owlProperties = new HashSet();
					for (Object obj : selectedObjects) {
						owlProperties.add(obj);
					}
					return OWLPropertyDTO.newInstance(owlProperties);
				} catch (Exception e) {
					e.printStackTrace();
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
			e.printStackTrace();
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
