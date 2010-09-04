package unbbayes.gui.mebn.ontology.protege;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.JLabel;
import javax.swing.JPanel;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder;
import unbbayes.io.mebn.MEBNStorageImplementorDecorator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.ResourceController;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.stanford.smi.protege.widget.TabWidget;
import edu.stanford.smi.protege.widget.WidgetUtilities;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.cls.OWLClassesTab;

public class ProtegeOWLTabPanel extends JPanel {

	protected ResourceBundle resource;
	private MultiEntityBayesianNetwork mebn;
	private TabWidget widget;
	
	private String protegeTabClassName = OWLClassesTab.class.getName();
	
	private IMEBNMediator mediator;

	/**
	 * Default constructor is protected to allow inheritance
	 */
	protected ProtegeOWLTabPanel() {
		super();
		this.resource = ResourceController.newInstance().getBundle(
			unbbayes.gui.mebn.ontology.protege.resources.Resources.class.getName(),
			Locale.getDefault(),
			this.getClass().getClassLoader());
	}
	
	/**
	 * Default constructor method using fields.
	 * @param owlModelHolder : this is a storage implementor. 
	 * @param mediator this is the mediator. This is usually accessed to call the upper GUI container.
	 * Usually, {@link unbbayes.prs.mebn.MultiEntityBayesianNetwork.MultiEntityBayesianNetwork#getStorageImplementor()}
	 * holds a instance of this class.
	 * @param protegeTabClassName a name of a class extending {@link AbstractTabWidget}
	 * @return a panel
	 */
	public static ProtegeOWLTabPanel newInstance(String protegeTabClassName, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		ProtegeOWLTabPanel ret = new ProtegeOWLTabPanel();
		ret.setProtegeTabClassName(protegeTabClassName);
		ret.setMebn(mebn);
		ret.setMediator(mediator);
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
		this.setLayout(new BorderLayout());
		
		// extract the owl model
		OWLModel owlModel = null;
		if (this.getOwlModelHolder() != null) {
			// extract the ontology
			owlModel = this.getOwlModelHolder().getAdaptee();
		}
		if (owlModel == null) {
			// this project is not bound to a protege project or owl model
			this.add(new JLabel(this.getResource().getString("NoOWLModelFound")));
			return;
		}
		
		Project proj = owlModel.getProject();
//		Project proj = new Project(owlModel.getProject().getProjectFilePath(), new ArrayList());
		
		// prepare the protege tab to edit classes
		WidgetDescriptor widgetDescriptor = proj.getTabWidgetDescriptor(this.getProtegeTabClassName());
		if (widgetDescriptor == null) {
	        // if no protege widget is accessible, show an error message
			this.add(new JLabel(this.getResource().getString("CouldNotLoadProtegeOWLWidget")));
			return;
	    }
		
		// generate and add the protege tab to edit classes
	    this.setWidget(WidgetUtilities.createTabWidget(widgetDescriptor, proj));
	    
	    this.add((Component)widget, BorderLayout.CENTER);		
	
	}

	/**
	 * Initialize components' listeners. This is called within {@link #newInstance(MEBNStorageImplementorDecorator)}
	 * to create listeners of components
	 */
	protected void initListeners() {
		// notify components
		this.addPropertyChangeListener(IMEBNEditionPanelBuilder.MEBN_EDITION_PANEL_CHANGE_PROPERTY, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				try {
					if (getOwlModelHolder() != null && getOwlModelHolder().getAdaptee() != null) {
						getOwlModelHolder().getAdaptee().notifyAll();
					}
					if (getWidget() != null) {
						getWidget().notifyAll();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Removes all components and calls
	 * {@link #initComponents()} and then
	 * {@link #initListeners()}
	 */
	public void resetComponents() {
		this.removeAll();
		this.getWidget().dispose();
		this.setWidget(null);
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
	 * @return the widget
	 */
	public TabWidget getWidget() {
		return widget;
	}

	/**
	 * @param widget the widget to set
	 */
	public void setWidget(TabWidget widget) {
		this.widget = widget;
	}

	/**
	 * @return the protegeTabClassName
	 */
	public String getProtegeTabClassName() {
		return protegeTabClassName;
	}

	/**
	 * @param protegeTabClassName the protegeTabClassName to set
	 */
	public void setProtegeTabClassName(String protegeTabClassName) {
		this.protegeTabClassName = protegeTabClassName;
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

}