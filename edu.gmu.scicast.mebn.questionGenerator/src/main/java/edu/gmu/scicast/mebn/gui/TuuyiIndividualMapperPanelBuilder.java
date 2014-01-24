package edu.gmu.scicast.mebn.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLEntityRemover;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.controller.mebn.IconController;
import unbbayes.gui.mebn.auxiliary.ListCellRenderer;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder;
import unbbayes.io.mebn.owlapi.DefaultPROWL2ModelUser;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.owlapi.IPROWL2ModelUser;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.kb.extension.ontology.protege.OWL2KnowledgeBase;
import unbbayes.util.Debug;
import unbbayes.util.IBridgeImplementor;
import unbbayes.util.ResourceController;

import com.Tuuyi.TuuyiOntologyServer.OntologyClient;
import com.Tuuyi.TuuyiOntologyServer.generatedClasses.TuuyiOntologyServer.Term;

import edu.gmu.scicast.mebn.TuuyiOntologyUser;
import edu.gmu.scicast.mebn.kb.TuuyiKnowledgeBase;

/**
 * Builds a panel that allows user to browse Tuuyi ontology and
 * include/edit individuals in scicast question generator upper ontology.
 * @author Shou Matsumoto
 */
public class TuuyiIndividualMapperPanelBuilder extends JPanel implements IMEBNEditionPanelBuilder, TuuyiOntologyUser {
	
	private boolean isToUseReasoner = false;
	
	private IPROWL2ModelUser prowl2modelUser = DefaultPROWL2ModelUser.getInstance();

	private OntologyClient ontologyClient = new OntologyClient();

	private PrefixManager prefixManager = TuuyiOntologyUser.EXTERNAL_ONTOLOGY_DEFAULT_PREFIX_MANAGER;
	
	private IMEBNMediator mediator;
	private MultiEntityBayesianNetwork mebn;


	private ResourceBundle resource;
	
	private IconController iconController;

	
	private int defaultRandomRangeOnMultiplePropertySelection = 100;

	private JList tuuyiClassList;

	private JScrollPane tuuyiClassListScrollPane;

	private JToolBar tuuyiClassToolBar;

	private JPanel tuuyiClassListPanel;

	private JButton tuuyiAddClassButton;

	private JButton tuuyiRemoveClassButton;

	private JSplitPane editSplitPane;

	private JSplitPane classSplitPane;

	private JButton tuuyiRefreshClassButton;

	private JScrollPane tuuyiSearchScrollPane;

	private JPanel searchPanel;


	private JToolBar tuuyiSearchToolBar;


	private JTextField tuuyiNameSearchTextField;

	private JCheckBox tuuyiNameSearchExactMatchCheckBox;

	private JPanel tuuyiSearchResultPanel;

	private JCheckBox tuuyiSearchIDCheckBox;

	private JButton tuuyiConsiderIndividualIncludeButton;

	private JButton tuuyiConsiderIndividualRemoveButton;

	private JButton tuuyiConsiderIndividualRefreshButton;

	private JToolBar tuuyiConsiderIndividualToolBar;

	private JToolBar tuuyiIgnoreIndividualToolBar;

	private JButton tuuyiIgnoreIndividualIncludeButton;

	private JButton tuuyiIgnoreIndividualRemoveButton;

	private JButton tuuyiIgnoreIndividualRefreshButton;

	private JTree tuuyiSearchResultTree;

	private JPanel tuuyiIgnoreIndividualPanel;

	private JPanel tuuyiConsiderIndividualPanel;

	private JList tuuyiIgnoredIndividualList;

	private JList tuuyiConsideredIndividualList;
	
	/**
	 * Default constructor is kept public, so that plugin framework can instantiate this class.
	 * This constructor simply initializes resource (used in UnBBayes primarily for multi-language support) class and icon manager.
	 */
	public TuuyiIndividualMapperPanelBuilder() {
		super();
		this.resource = ResourceController.newInstance().getBundle(
				unbbayes.gui.mebn.ontology.protege.resources.Resources.class.getName(),
				Locale.getDefault(),
				this.getClass().getClassLoader());
		
		// set up default customization of IconController, which returns another icon for resident nodes
		this.iconController = 
				IconController.getInstance();
//				new IconController()  {
//			public ImageIcon getResidentNodeIcon() {
//				if (residentNodeIcon == null) {
//					try {
//						residentNodeIcon = new ImageIcon(getClass().getClassLoader().getResource("properties.png"));
//					} catch (Throwable t) {
//						Debug.println(this.getClass(), t.getMessage(), t);
//						return super.getResidentNodeIcon();
//					}
//				}
//				return residentNodeIcon;
//			}
//		};
		// icon controller can be extended as anonymous inner class like in the above commented code, so you can use the above as example on how to customize icons
	}

	


	/* (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder#buildPanel(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public JPanel buildPanel(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		
		// we do not need this plugin if mebn is not bound to a project
		if (mebn == null || mebn.getStorageImplementor() == null ) {
			return null;
		}
		
		// igore if we cannot access OWL ontology
		if (this.getOWLOntology(mebn, mediator) == null) {
			return null;
		}
		
		this.mediator = mediator;
		this.mebn = mebn;
		
		
		
		
		// reuse ontology prefix and  if it is using scicast knowledge base
		if (mediator.getKnowledgeBase() instanceof TuuyiKnowledgeBase) {
			this.setPrefixManager(((TuuyiKnowledgeBase) mediator.getKnowledgeBase()).getQuestionGeneratorOntologyPrefixManager());
			this.setOntologyClient(((TuuyiKnowledgeBase) mediator.getKnowledgeBase()).getOntologyClient());
			this.setPROWL2modelUser(((TuuyiKnowledgeBase) mediator.getKnowledgeBase()).getProwlModelUserDelegator());
		}
		
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
	 * @param mebn
	 * @param mediator
	 * @return
	 * Extracts {@link OWLOntology} from {@link MultiEntityBayesianNetwork#getStorageImplementor()}.
	 * Null if not found.
	 */
	public OWLOntology getOWLOntology(MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		IBridgeImplementor storageImplementor = mebn.getStorageImplementor();
		if (storageImplementor instanceof IOWLAPIStorageImplementorDecorator) {
			return ((IOWLAPIStorageImplementorDecorator) storageImplementor).getAdaptee();
		}
		return null;
	}




	/**
	 * Create components. This is called by {@link #buildPanel(MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	protected void initComponents() {
		this.setLayout(new GridLayout(1, 0, 0, 0));
		this.setBackground(Color.WHITE);
		
		// at the center, a panel to search and display tuuyi ontology individuals
		// at the right, panel for individuals (included or excluded)
		this.editSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, this.createTuuyiSearchPanel(), this.createTuuyiIndividualPanel());
		// at the left, a list of classes whose individuals shall be considered to be defined in Tuuyi ontology
		this.classSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, this.createTuuyiClassPanel(), editSplitPane);
		classSplitPane.setDividerLocation(.35);
		editSplitPane.setDividerLocation(.8);
		
		// change the split pane colors
		BasicSplitPaneDivider divider = (BasicSplitPaneDivider) editSplitPane.getComponent(2);
		divider.setBackground(Color.BLUE);
		divider.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
//		divider.setBorder(null);
		divider.setBackground(Color.BLUE);
		divider = (BasicSplitPaneDivider) classSplitPane.getComponent(2);
		divider.setBackground(Color.BLUE);
		divider.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
//		divider.setBorder(null);
		divider.setBackground(Color.BLUE);
		
		this.add(classSplitPane);
	}

	/**
	 * @return : two lists of tuuyi ontology individuals - one for individuals marked to be included
	 * (either the individual itself, or a subtree rooted by the displayed individual, given
	 * hierarchy of selected relation/property) in 
	 * search by {@link TuuyiKnowledgeBase}, and the other for individuals marked to be excluded
	 * in search by {@link TuuyiKnowledgeBase}.
	 */
	public Component createTuuyiIndividualPanel() {
		
		tuuyiConsiderIndividualPanel = new JPanel(new BorderLayout());
		tuuyiConsiderIndividualPanel.setBorder(new TitledBorder("Considered by the question generator"));
		
		tuuyiConsiderIndividualToolBar = new JToolBar("Individuals to include in ontology", JToolBar.VERTICAL);
		tuuyiConsiderIndividualPanel.add(tuuyiConsiderIndividualToolBar, BorderLayout.WEST);
//		tuuyiConsiderIndividualToolBar.setBackground(Color.WHITE);
		
		// to include more individuals
		this.tuuyiConsiderIndividualIncludeButton = new JButton(iconController.getMoreIcon());
		tuuyiConsiderIndividualToolBar.add(tuuyiConsiderIndividualIncludeButton);
		tuuyiConsiderIndividualIncludeButton.setToolTipText("Include current selection (in search results) to entities to be considered in question generator.");
		
		// delete included individuals
		this.tuuyiConsiderIndividualRemoveButton = new JButton(iconController.getLessIcon());
		tuuyiConsiderIndividualToolBar.add(tuuyiConsiderIndividualRemoveButton);
		tuuyiConsiderIndividualRemoveButton.setToolTipText("Remove current selection from list of considered individuals.");
		
		// refresh list
		this.tuuyiConsiderIndividualRefreshButton = new JButton(iconController.getEditUndo());
		tuuyiConsiderIndividualToolBar.add(tuuyiConsiderIndividualRefreshButton);
		tuuyiConsiderIndividualRefreshButton.setToolTipText("Refresh list.");
		
		
		tuuyiIgnoreIndividualPanel = new JPanel(new BorderLayout());
		tuuyiIgnoreIndividualPanel.setBorder(new TitledBorder("Ignored posteriorly when question is generated"));
		

		tuuyiIgnoreIndividualToolBar = new JToolBar("Individuals to include in ontology", JToolBar.VERTICAL);
		tuuyiIgnoreIndividualPanel.add(tuuyiIgnoreIndividualToolBar, BorderLayout.WEST);
//		tuuyiIgnoreIndividualToolBar.setBackground(Color.WHITE);
		
		// to include more individuals
		this.tuuyiIgnoreIndividualIncludeButton = new JButton(iconController.getMoreIcon());
		tuuyiIgnoreIndividualToolBar.add(tuuyiIgnoreIndividualIncludeButton);
		tuuyiIgnoreIndividualIncludeButton.setToolTipText("Include current selection (in search results) to individuals to be ignored by question generator.");
		
		// delete included individuals
		this.tuuyiIgnoreIndividualRemoveButton = new JButton(iconController.getLessIcon());
		tuuyiIgnoreIndividualToolBar.add(tuuyiIgnoreIndividualRemoveButton);
		tuuyiIgnoreIndividualRemoveButton.setToolTipText("Remove current selection from list of ignored individuals.");
		
		// refresh list
		this.tuuyiIgnoreIndividualRefreshButton = new JButton(iconController.getEditUndo());
		tuuyiIgnoreIndividualToolBar.add(tuuyiIgnoreIndividualRefreshButton);
		tuuyiIgnoreIndividualRefreshButton.setToolTipText("Refresh list.");
		
		// create list of individuals considered by question generator
		this.rebuildConsideredIndividualsList();
		
		// create list of individuals ignored by question generator
		this.rebuildIgnoredIndividualsList();
		
		
		JSplitPane splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false, tuuyiConsiderIndividualPanel, tuuyiIgnoreIndividualPanel);
		splitter.setBackground(Color.WHITE);
		BasicSplitPaneDivider divider = (BasicSplitPaneDivider) splitter.getComponent(2);
		divider.setBackground(Color.BLUE);
		divider.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
		divider.setBackground(Color.BLUE);
		return splitter;
	}


	/**
	 * Checks {@link #getTuuyiClassList()} to list for individuals
	 * ignored by question generator, and also checks for {@link #getTuuyiSearchResultTree()}
	 * in order to include more elements in list.
	 * @see #getTuuyiIgnoreIndividualPanel()
	 */
	public void rebuildIgnoredIndividualsList() {
		if (tuuyiIgnoredIndividualList == null) {
			tuuyiIgnoredIndividualList = new JList(new DefaultListModel());
			tuuyiIgnoredIndividualList.setBackground(Color.WHITE);
			getTuuyiIgnoreIndividualPanel().add(new JScrollPane(tuuyiIgnoredIndividualList));

			// make sure the list contains icons and name (instead of IRI)
			tuuyiIgnoredIndividualList.setCellRenderer(new ListCellRenderer(iconController.getEntityInstanceIcon()) {
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					if (value instanceof OWLIndividual) {
						OWLIndividual individual = (OWLIndividual) value;
						
						// extract term ID of this individual
						OWLOntology ontology = getOWLOntology(getMEBN(), getMediator());
						// extract term ID of this individual
						Collection<OWLLiteral> ids = getDataPropertyValues(individual, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HAS_UID_PROPERTY_NAME, getPrefixManager()), ontology);
						if (ids != null && !ids.isEmpty()) {	
							// only check 1st value
							Term term = getOntologyClient().getTermById(ids.iterator().next().parseInteger());
							// use id + name instead of IRI
							super.setText("[" + term.getId() + "] " + getPROWL2modelUser().extractName((OWLObject)value));
							
							// the tool tip is the remote name and IRI of the individual itself
							super.setToolTipText("[" + term.getSimpleName() + "] " + value.toString());
						} else {
							// just use name instead of IRI
							super.setText( getPROWL2modelUser().extractName((OWLObject)value));
							
							// the tool tip is just the IRI of the individual itself
							super.setToolTipText(value.toString());
						}
						
					}
					return this;
				}
				
			});
		}
		
		// the data model to contain data
		DefaultListModel dataModel = null;
		if (tuuyiIgnoredIndividualList.getModel() instanceof DefaultListModel) {
			dataModel = (DefaultListModel) tuuyiIgnoredIndividualList.getModel();
			dataModel.removeAllElements();	// reset its content
		} else {
			dataModel = new DefaultListModel();
			tuuyiIgnoredIndividualList.setModel(dataModel);
		}
		
		if (getTuuyiClassList() != null) {
			// extract currently selected class and iterate on its individuals
			OWLClassExpression selectedClass = (OWLClassExpression) getTuuyiClassList().getSelectedValue();
			
			// fill if there is selected
			if (selectedClass != null) {
				// extract upper ontology, so that we can use it to find OWL assertions
				OWLOntology ontology = getOWLOntology(getMEBN(), getMediator());
				
				// extract the data property that indicates whether the individual shall be ignored
				OWLDataProperty isToExclude = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IS_TO_EXCLUDE_DATA_PROPERTY_NAME, getPrefixManager());
				
				for (OWLIndividual individual : this.getIndividuals(selectedClass, ontology)) {
					Collection<OWLLiteral> values = getDataPropertyValues(individual, isToExclude, ontology);
					// check that data property was declared and is true
					if (values != null && !values.isEmpty() && values.iterator().next().parseBoolean()) {
						dataModel.addElement(individual);
					}
				}
				
				
				tuuyiIgnoredIndividualList.updateUI();
				tuuyiIgnoredIndividualList.repaint();
			}
		}
		
		
		
	}

	/**
	 * 
	 * @param individual
	 * @param isToExclude
	 * @param ontology
	 * @return
	 */
	protected Collection<OWLLiteral> getDataPropertyValues(OWLIndividual individual, OWLDataProperty property, OWLOntology ontology) {
		// check if we can use a reasoner instead
		OWLReasoner reasoner = this.getReasoner();
		if (reasoner != null) {
			return reasoner.getDataPropertyValues(individual.asOWLNamedIndividual(), property);
		}
		return individual.getDataPropertyValues(property, ontology);
	}




	/**
	 * Returns owl individuals that satisfies class expression
	 * @param owlClass
	 * @return
	 */
	protected Collection<OWLIndividual> getIndividuals(OWLClassExpression owlClassExpression, OWLOntology ontology) {
		// check if we can use a reasoner instead
		OWLReasoner reasoner = this.getReasoner();
		if (reasoner != null) {
			// we found a reasoner, so use it
			NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(owlClassExpression, false);
			if (instances != null) {
				return (Collection)instances.getFlattened();
			}
		} else {
			List<OWLIndividual> ret = new ArrayList<OWLIndividual>();
			// no reasoner found. Try finding manually.
			// Try asserted individuals as well
			if (owlClassExpression instanceof OWLClass) {
				// if expression is exactly an OWL class, use this method
				try{
					return ((OWLClass)owlClassExpression).getIndividuals(ontology);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				// expression is a complex expression. use another method
				try{
					return (Set)owlClassExpression.getIndividualsInSignature();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return Collections.emptyList();
	}



	/**
	 * Attempts to get a reasoner either from {@link #getMediator()}, or from {@link #getMEBN()}
	 * @return The reasoner if found. null if {@link #isToUseReasoner()} is false. 
	 */
	protected OWLReasoner getReasoner() {
		if (isToUseReasoner()) {
			if (getMediator().getKnowledgeBase() instanceof OWL2KnowledgeBase) {
				OWL2KnowledgeBase knowledgeBase = (OWL2KnowledgeBase) mediator.getKnowledgeBase();
				OWLReasoner ret = knowledgeBase.getDefaultOWLReasoner();
				if (ret != null) {
					return ret;
				}
			}
			if (getMEBN().getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator) {
				IOWLAPIStorageImplementorDecorator storage = (IOWLAPIStorageImplementorDecorator) getMEBN().getStorageImplementor();
				return storage.getOWLReasoner();
			}
		}
		return null;
	}




	/**
	 * Checks {@link #getTuuyiClassList()} to list for individuals
	 * considered by question generator, and also checks for {@link #getTuuyiSearchResultTree()}
	 * in order to include more elements in list.
	 * @see #getTuuyiConsiderIndividualPanel()
	 * 
	 */
	public void rebuildConsideredIndividualsList() {
		
		if (tuuyiConsideredIndividualList == null) {
			tuuyiConsideredIndividualList = new JList(new DefaultListModel());
			tuuyiConsideredIndividualList.setBackground(Color.WHITE);
			getTuuyiConsiderIndividualPanel().add(new JScrollPane(tuuyiConsideredIndividualList));

			// make sure the list contains icons and name (instead of IRI)
			tuuyiConsideredIndividualList.setCellRenderer(new DefaultListCellRenderer() {
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					
					if(isSelected){
					   super.setBorder(BorderFactory.createEtchedBorder()); 
					}
					
					if (value instanceof OWLIndividual) {
						OWLIndividual individual = (OWLIndividual) value;
						
						// render differently depending on data property values
						OWLOntology ontology = getOWLOntology(getMEBN(), getMediator());	// extract ontology
						
						// extract name of the property
						String propertyName = null;
						Collection<OWLLiteral> propertyId = getDataPropertyValues(individual, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HIERARCHY_PROPERTY_ID, getPrefixManager()), ontology);
						if (propertyId != null && !propertyId.isEmpty()) {
							// use 1st element
							Term propertyTerm = getOntologyClient().getTermById(propertyId.iterator().next().parseInteger());
							propertyName = propertyTerm.getSimpleName();
						}
						
						// extract value of how many levels in hierarchy to consider
						int depth = (propertyName == null)?0:1;	// if there is a property name, then by default depth is declared to at least 1
						Collection<OWLLiteral> depths = getDataPropertyValues(individual, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HAS_MAX_DEPTH_PROPERTY_NAME, getPrefixManager()), ontology);
						if (depths != null && !depths.isEmpty()) {	
							// only check 1st value
							depth = depths.iterator().next().parseInteger();
						}
						
						// extract term ID of this individual
						Term term = null;
						Collection<OWLLiteral> ids = getDataPropertyValues(individual, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HAS_UID_PROPERTY_NAME, getPrefixManager()), ontology);
						if (ids != null && !ids.isEmpty()) {	
							// only check 1st value
							term = getOntologyClient().getTermById(ids.iterator().next().parseInteger());
						}
						
						
						// the tool tip is the remote name plus IRI of the individual itself
						if (term != null) {
							super.setToolTipText("[" + term.getSimpleName() + "] " + value.toString());	
						} else {
							super.setToolTipText(value.toString());	
						}
						
						// customize what to display
						String textToDisplay = null;
						if (depth == 0) {
							
							// only this individual must be included. There is no hierarchy
							super.setIcon(iconController.getEntityInstanceIcon()); 
							// use name instead of IRI
							textToDisplay = getPROWL2modelUser().extractName((OWLObject)value);
							
						} else {
							
							// this individual is a root of a hierarchy
							super.setIcon(iconController.getResidentNodeIcon()); 
							
							
							// extract whether it's using inverse property
							boolean isInverse = false;
							Collection<OWLLiteral> isInverseValues = getDataPropertyValues(individual, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IS_INVERSE_HIERARCHY_PROPERTY_ID, getPrefixManager()), ontology);
							if (isInverseValues != null && !isInverseValues.isEmpty()) {
								// use 1st element
								isInverse = isInverseValues.iterator().next().parseBoolean();
							}
							
							// just customize name to display
							textToDisplay = getPROWL2modelUser().extractName((OWLObject)value) + " (" ;
							if (depth > 1) {
								textToDisplay += depth + " levels";
							} else if (depth == 1) {
								textToDisplay += "1 level";
							} else {
								// depth is negative, so it's the whole subtree
								textToDisplay += "entire hierarchy";
							}
							if (propertyName != null && propertyName.length() > 0) {
								textToDisplay += " of ";
								if (isInverse) {
									textToDisplay += "inverse ";
								}
								textToDisplay += propertyName;
							}
							textToDisplay += ")";
						}
						
						if (term != null) {
							textToDisplay = "[" + term.getId() + "] " + textToDisplay;
						}
						
						// name to display
						super.setText(textToDisplay);
					}
					return this;
				}
				
			});
		}
		
		// the data model to contain data
		DefaultListModel dataModel = null;
		if (tuuyiConsideredIndividualList.getModel() instanceof DefaultListModel) {
			dataModel = (DefaultListModel) tuuyiConsideredIndividualList.getModel();
			dataModel.removeAllElements();	// reset its content
		} else {
			dataModel = new DefaultListModel();
			tuuyiConsideredIndividualList.setModel(dataModel);
		}
		
		if (getTuuyiClassList() != null) {
			// extract currently selected class and iterate on its individuals
			OWLClassExpression selectedClass = (OWLClassExpression) getTuuyiClassList().getSelectedValue();
			
			// fill if there is selected
			if (selectedClass != null) {
				// extract upper ontology, so that we can use it to find OWL assertions
				OWLOntology ontology = getOWLOntology(getMEBN(), getMediator());
				
				// extract the data property that indicates whether the individual shall be ignored
				OWLDataProperty isToExclude = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IS_TO_EXCLUDE_DATA_PROPERTY_NAME, getPrefixManager());
				OWLDataProperty hasMaxDepth = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HAS_MAX_DEPTH_PROPERTY_NAME, getPrefixManager());
				OWLDataProperty hierarchyProperty = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HIERARCHY_PROPERTY_ID, getPrefixManager());
				
				// include in list if isToExclude != true, or hasMaxDepth != 0, or hierarcyProperty is specified
				for (OWLIndividual individual : this.getIndividuals(selectedClass, ontology)) {
					Collection<OWLLiteral> values = getDataPropertyValues(individual, isToExclude, ontology);
					// check that data property was declared and is true
					if (values == null || values.isEmpty() || !values.iterator().next().parseBoolean()) {
						// immediately include
						dataModel.addElement(individual);
					} else { // if it is specifying a hierarchy, include
						// check if max depth was specified
						values = getDataPropertyValues(individual, hasMaxDepth, ontology);
						if (values != null && !values.isEmpty() && values.iterator().next().parseInteger() != 0) {
							// depth was non-zero, so a hierarchy is specified
							dataModel.addElement(individual);
						} else {
							// check if hierarcyProperty was specified
							values = getDataPropertyValues(individual, hierarchyProperty, ontology);
							if (values != null && !values.isEmpty()) {
								// hierarchy was specified
								dataModel.addElement(individual);
							}
						}
					}
				}
				
				
				tuuyiConsideredIndividualList.updateUI();
				tuuyiConsideredIndividualList.repaint();
			}
		}
		
		
		
	}




	/**
	 * @return : a new panel to search and display details (e.g. ID, name, related individuals) about a given tuuyi ontology individual.
	 */
	public Component createTuuyiSearchPanel() {
		searchPanel = new JPanel(new BorderLayout());
		searchPanel.setBackground(Color.WHITE);
		searchPanel.setBorder(new TitledBorder("Search entities in Tuuyi ontology server"));
		
		JPanel toolBarPanel = new JPanel(new GridLayout(0, 1, 0, 0));
//		toolBarPanel.setBackground(Color.WHITE);
		searchPanel.add(toolBarPanel, BorderLayout.NORTH);
		
		// tool bar with text field to search by name
		this.tuuyiSearchToolBar = new JToolBar("Search by name", JToolBar.HORIZONTAL);
		tuuyiSearchToolBar.setBackground(Color.WHITE);
		toolBarPanel.add(tuuyiSearchToolBar);
		
//		tuuyiSearchToolBar.add(new JLabel("Search by name"));
		// text field for search
		tuuyiNameSearchTextField = new JTextField(30);
		tuuyiNameSearchTextField.setBorder(new TitledBorder("Search"));
		tuuyiSearchToolBar.add(tuuyiNameSearchTextField);
		tuuyiNameSearchTextField.setToolTipText("Fill name/ID and press enter.");
		
		// check box to indicate that shall search for exact match
		tuuyiNameSearchExactMatchCheckBox = new JCheckBox("Exact match");
		tuuyiNameSearchExactMatchCheckBox.setBackground(Color.WHITE);
		tuuyiSearchToolBar.add(tuuyiNameSearchExactMatchCheckBox);
		tuuyiNameSearchExactMatchCheckBox.setToolTipText("Check this to search for exact match");
		
		// check box to indicate that shall search for ID
		tuuyiSearchIDCheckBox = new JCheckBox("Search by ID");
		tuuyiSearchIDCheckBox.setBackground(Color.WHITE);
		tuuyiSearchToolBar.add(tuuyiSearchIDCheckBox);
		tuuyiSearchIDCheckBox.setToolTipText("Check this to search for exact match");
		
		// the following code was migrated to rebuildSearchTree
//		this.tuuyiSearchResultPanel = new JPanel(new FlowLayout());
//		tuuyiSearchResultPanel.setBackground(Color.LIGHT_GRAY);
//		tuuyiSearchResultPanel.setBorder(new TitledBorder("Results"));
//		searchPanel.add(new JScrollPane(tuuyiSearchResultPanel));
		
		// create the result of search and display it as a tree. This shall also instantiate the result panel if necessary
//		this.rebuildSearchTree(getOntologyClient().matchTermByName("Category:Science"));
		this.rebuildSearchTree(null);
		
		searchPanel.setPreferredSize(new Dimension(500,380));
		
		// include the main panel into a scroll pane, then return the scroll pane
//		return tuuyiSearchScrollPane = new JScrollPane(searchPanel);
		return searchPanel;
	}


	/**
	 * Object used to fill result of search at {@link TuuyiIndividualMapperPanelBuilder#rebuildSearchTree(Term)}
	 * @author Shou Matsumoto
	 */
	public class TermWrapper {
		/** type used at {@link TermWrapper#TermWrapper(Term, int)}. This one indicates that this is an ordinal term */
		public static final int TERM = 1;
		/** type used at {@link TermWrapper#TermWrapper(Term, int)}. This one indicates that this is a property */
		public static final int PROPERTY = 2;
		/** type used at {@link TermWrapper#TermWrapper(Term, int)}. This one indicates that this is an inverse property */
		public static final int INVERSE_PROPERTY = 3;
		/** type used at {@link TermWrapper#TermWrapper(Term, int)}. This one indicates that this is unknown, or invalid */
		public static final int OTHER = 0;
		private Term term;
		private int type;
		private String label;
		public TermWrapper(Term term, int type) {
			this.setTerm(term);
			this.setType(type);
		}
		public TermWrapper(String label, int type) {
			this.setLabel(label);
			this.setType(type);
		}
		public void setType(int type) { this.type = type; } 
		public Term getTerm() { return term; }
		public void setTerm(Term term) { this.term = term; }
		public int getType() { return type; }
		public String getLabel() { return label; }
		public void setLabel(String label) { this.label = label; }
		public String toString() { 
			if (term != null) {
				return "["+term.getId() + "] " + term.getSimpleName();
			} 
			if (label != null) {
				return label;
			} 
			return "No match";
		}
	}

	/**
	 * This method will add a {@link JTree} in {@link #getTuuyiSearchResultPanel()},
	 * in order to display the results of a search.
	 * @param term : term returned by a search
	 * @see #getOntologyClient()
	 * @see OntologyClient#matchTermByName(String)
	 * @see OntologyClient#getTermById(int)
	 * @see OntologyClient#getTermBySimpleName(String)
	 */
	public void rebuildSearchTree(Term term) {
		try {
			// change cursor
			try { getMediator().getScreen().setCursor(new Cursor(Cursor.WAIT_CURSOR)); } catch (Throwable t) {}
			

			// panel to be updated
			JPanel panel = getTuuyiSearchResultPanel();
			if (panel == null) {
				// instantiate panel if it was not instantiated yet
				panel = new JPanel(new GridLayout(0, 1, 0, 0));
				panel.setBackground(Color.WHITE);
				panel.setBorder(new TitledBorder("Results"));
				setTuuyiSearchResultPanel(panel);
//				getSearchPanel().add(new JScrollPane(panel), BorderLayout.CENTER);
				getSearchPanel().add((panel), BorderLayout.CENTER);
			}
			
			// object to be used in order to search for more elements (e.g. related individuals, names), if necessary
			OntologyClient client = getOntologyClient();
			
			// clear panel
			panel.removeAll();
			
			// build tree with details about the term
			DefaultMutableTreeNode root = new DefaultMutableTreeNode(new TermWrapper(term, TermWrapper.TERM));
			DefaultTreeModel model = new DefaultTreeModel(root);
			
			if (term != null) {
				// build the first nodes in tree: separates properties and inverse properties
				DefaultMutableTreeNode property = new DefaultMutableTreeNode(new TermWrapper("Related: ", TermWrapper.OTHER));
				root.add(property);
				DefaultMutableTreeNode inverseProperty = new DefaultMutableTreeNode(new TermWrapper("Inverse related: ", TermWrapper.OTHER));
				root.add(inverseProperty);
				
				
				// map from property id to related terms' ids
				Map<Integer, List<Integer>> termRelationships = client.getTermRelationships(term.getId());
				if (termRelationships != null) {
					// fill properties.
					for (Entry<Integer, List<Integer>> entry : termRelationships.entrySet()) {
						if (entry.getKey() == null || entry.getValue() == null || entry.getValue().isEmpty()) {
							// ignore this entry
							continue;
						}
						// key of entry is property ID, values of entry are related individuals
						DefaultMutableTreeNode newProp = new DefaultMutableTreeNode(new TermWrapper(client.getTermById(entry.getKey()), TermWrapper.PROPERTY));
						property.add(newProp);
						
						// fill related individuals
						for (Integer relatedID : entry.getValue()) {
							if (relatedID == null) {
								continue;	// ignore
							}
							newProp.add(new DefaultMutableTreeNode(new TermWrapper(client.getTermById(relatedID), TermWrapper.TERM)));
						}
					}
				}
				
				// do same thing to inverse properties.
				termRelationships = client.getTermInverseRelationships(term.getId());
				if (termRelationships != null) {
					for (Entry<Integer, List<Integer>> entry : termRelationships.entrySet()) {
						if (entry.getKey() == null || entry.getValue() == null || entry.getValue().isEmpty()) {
							// ignore this entry
							continue;
						}
						// key of entry is property ID, values of entry are related individuals
						DefaultMutableTreeNode newProp = new DefaultMutableTreeNode(new TermWrapper(client.getTermById(entry.getKey()), TermWrapper.INVERSE_PROPERTY));
						inverseProperty.add(newProp);
						
						// fill related individuals
						for (Integer relatedID : entry.getValue()) {
							if (relatedID == null) {
								continue;	// ignore
							}
							newProp.add(new DefaultMutableTreeNode(new TermWrapper(client.getTermById(relatedID), TermWrapper.TERM)));
						}
					}
				}
				
			}
			
			
			tuuyiSearchResultTree = new JTree(model);
			// do not allow content of tree to be changed by user (unless making another search)
			tuuyiSearchResultTree.setEditable(false);
			// indicate that we can select multiple elements, if we want
			tuuyiSearchResultTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
			tuuyiSearchResultTree.setToolTipText("Double-click on node will also trigger search. Select press include/exclude button to configure question generator.");
			
			// change the icons
			tuuyiSearchResultTree.setCellRenderer(new DefaultTreeCellRenderer() {
				public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
					super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
					if (value instanceof DefaultMutableTreeNode && ((DefaultMutableTreeNode) value).getUserObject() instanceof TermWrapper) {
						TermWrapper wrapper = (TermWrapper) ((DefaultMutableTreeNode) value).getUserObject();
						switch (wrapper.getType()) {
						case TermWrapper.PROPERTY:
						case TermWrapper.INVERSE_PROPERTY:
							super.setIcon(iconController.getResidentNodeIcon());
							break;
						case TermWrapper.TERM:
							super.setIcon(iconController.getEntityInstanceIcon());
							break;
						default:
							super.setIcon(null);
							break;
						}
					}
					return this;
				}
				
			});
			
			
			// if we double-click a node, we shall start search for that node
			tuuyiSearchResultTree.addMouseListener(new MouseAdapter() {
			    public void mousePressed(MouseEvent e) {
			        int selRow = getTuuyiSearchResultTree().getRowForLocation(e.getX(), e.getY());
			        TreePath selPath = getTuuyiSearchResultTree().getPathForLocation(e.getX(), e.getY());
			        if(selRow != -1) {
			           if(e.getClickCount() == 2) {
			        	   // this is a double-click event
			        	   try {
			        		   // update search text field
			        		   getTuuyiNameSearchTextField().setText(((TermWrapper)((DefaultMutableTreeNode)selPath.getLastPathComponent()).getUserObject()).getTerm().getId()+"");
			        		   getTuuyiNameSearchTextField().updateUI();
			        		   getTuuyiNameSearchTextField().repaint();
			        		   getTuuyiSearchIDCheckBox().setSelected(true);
			        		   getTuuyiSearchIDCheckBox().updateUI();
			        		   getTuuyiSearchIDCheckBox().repaint();
			        		   
			        		   // do search as if we did press "Enter" during search
			        		   triggerSearchFromCurrentSearchTextField();
			        	   } catch (Throwable t) {
			        		   Debug.println(getClass(), t.getMessage(), t);
			        	   }
			            }
			        }
			    }
			});
			
			// add the tree
			panel.add(new JScrollPane(tuuyiSearchResultTree));
			
			panel.updateUI();
			panel.repaint();
			
		} finally {
			// return cursor
			try { getMediator().getScreen().setCursor(new Cursor(Cursor.DEFAULT_CURSOR)); } catch (Throwable t) {}
		}
		
	}




	/**
	 * @return lists classes that will be shown in MEBN project as object entities, and
	 * instances will be searched from tuuyi ontology.
	 */
	public Component createTuuyiClassPanel() {
		
		// the main panel
		tuuyiClassListPanel = new JPanel(new BorderLayout());
		tuuyiClassListPanel.setBackground(Color.WHITE);
//		tuuyiClassListPanel.setPreferredSize(new Dimension(300, 600));
		tuuyiClassListPanel.setBorder(new TitledBorder("Custom category/class"));
		tuuyiClassListPanel.setToolTipText("Edit category/class of remote individuals that will be used in question generator.");
		
		tuuyiClassToolBar = new JToolBar("Add/remove classes",JToolBar.HORIZONTAL);
//		tuuyiClassToolBar.setBackground(Color.WHITE);
		tuuyiClassListPanel.add(tuuyiClassToolBar, BorderLayout.NORTH);
		
		// add buttons to include/exclude classes to list.
		this.tuuyiAddClassButton = new JButton(iconController.getMoreIcon());
		tuuyiClassToolBar.add(tuuyiAddClassButton);
		tuuyiAddClassButton.setToolTipText("Add new custom category/class");
		
		this.tuuyiRemoveClassButton = new JButton(iconController.getLessIcon());
		tuuyiClassToolBar.add(tuuyiRemoveClassButton);
		tuuyiRemoveClassButton.setToolTipText("Remove selected custom category/class");
		
		this.tuuyiRefreshClassButton = new JButton(iconController.getEditUndo());
		tuuyiClassToolBar.add(tuuyiRefreshClassButton);
		tuuyiRefreshClassButton.setToolTipText("Refresh list of custom category/class");
		
		
		// the list of classes
		tuuyiClassList = new JList(new DefaultListModel());
		tuuyiClassListPanel.add(tuuyiClassList, BorderLayout.CENTER);
		
		// fill list of classes
		this.rebuildClassList();
		
		// configure list of classes
		tuuyiClassList.setBackground(Color.WHITE);
		// individuals can be included to only 1 class at time. 
		tuuyiClassList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// multi-column list that will be likely to scroll vertically
		tuuyiClassList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		
		// make sure the list contains icons and name (instead of IRI)
		tuuyiClassList.setCellRenderer(new ListCellRenderer(iconController.getObjectEntityIcon()) {
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
				super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (value instanceof OWLObject) {
					// use name instead of IRI
					super.setText(getPROWL2modelUser().extractName((OWLObject)value));
					super.setToolTipText(value.toString());
				}
				return this;
			}
			
		});
		
		
		// include the main panel into a scroll pane, then return the scroll pane
		return tuuyiClassListScrollPane = new JScrollPane(tuuyiClassListPanel);
	}



	/**
	 * Fills {@link #getTuuyiClassList()} with subclasses of {@link TuuyiOntologyUser#REMOTE_CLASS_NAME}
	 * @see #getOWLOntology(MultiEntityBayesianNetwork, IMEBNMediator)
	 */
	public void rebuildClassList() {
		// extract list
		JList classList = this.getTuuyiClassList();
		
		// the data model to contain data
		DefaultListModel dataModel = null;
		if (classList.getModel() instanceof DefaultListModel) {
			dataModel = (DefaultListModel) classList.getModel();
			dataModel.removeAllElements();	// reset its content
		} else {
			dataModel = new DefaultListModel();
			classList.setModel(dataModel);
		}
		
		// extract classes that are used to group individuals from tuuyi ontology
		OWLOntology ontology = getOWLOntology(getMEBN(), getMediator());
		OWLClass tuuyiClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(REMOTE_CLASS_NAME, getPrefixManager());
		for (OWLClassExpression subClass : tuuyiClass.getSubClasses(ontology)) {
			if (subClass.isAnonymous()) {
				// only consider named classes
				continue;
			}
			dataModel.addElement(subClass);
		}
		
		classList.updateUI();
		classList.repaint();
	}




	/**
	 * Reset this component. 
	 */
	public void resetComponents() {
		this.removeAll();
		this.initComponents();
		this.initListeners();
	}

	/**
	 * Fill listeners of components created by {@link #initComponents()}.
	 * This is called by {@link #buildPanel(MultiEntityBayesianNetwork, IMEBNMediator)}
	 */
	protected void initListeners() {
		
		// what happens if I select an element in the class list
		getTuuyiClassList().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
			    if (e.getValueIsAdjusting() == false) {
			    	// finished selection
			        if (getTuuyiClassList().getSelectedIndex() == -1) {
			        	//No selection. Ignore
			        	Debug.println(getClass(), "Nothing is selected");
			        } else {
			        	// selected
			        	Debug.println(getClass(), "Selected: " + getTuuyiClassList().getSelectedValue());
			        	rebuildConsideredIndividualsList();
			        	rebuildIgnoredIndividualsList();
			        }
			    }
			}
		});
		
		// what happens if I press the add class button in class list
		getTuuyiAddClassButton().addActionListener(new ActionListener() {
			/** This is used to check if name is valid */
			public Pattern p = Pattern.compile("[a-z_][a-z0-9_]*", Pattern.CASE_INSENSITIVE);
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog(null, "Name of new custom class/category", "New custom class/category", JOptionPane.QUESTION_MESSAGE);
				if (input != null ) {
					// check empty
					if (input.trim().length() <= 0 ) {
						JOptionPane.showMessageDialog(null, "Anonymous class/category is not allowed.", "Invalid name", JOptionPane.ERROR_MESSAGE);
						return;
					}
					// check invalid character
					if (!p.matcher(input).find()) {
						JOptionPane.showMessageDialog(null, "Name contains invalid character.", "Invalid name", JOptionPane.ERROR_MESSAGE);
						return;
					} 
					
					// create the class assertion
					OWLOntology ontology = getOWLOntology(mebn, mediator);
					OWLClass parent = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(REMOTE_CLASS_NAME, prefixManager);
					OWLClass newClass = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(IRI.create(ontology.getOntologyID().getOntologyIRI() + "#" + input));
			        
					// Now add new class as subclass of remote class
			        ontology.getOWLOntologyManager().addAxiom(ontology, ontology.getOWLOntologyManager().getOWLDataFactory().getOWLSubClassOfAxiom(newClass, parent));
			        
			        // refresh list of class
			        // TODO simply add, instead of rebuild
					rebuildClassList();
					
				} // else cancelled
			}
		});
		
		
		// what happens if I press the delete class button
		getTuuyiRemoveClassButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    // finished selection
			    if (getTuuyiClassList().getSelectedIndex() == -1) {
			    	// No selection
			    	JOptionPane.showMessageDialog(null, "Select the custom class/category to delete.", "No selection", JOptionPane.ERROR_MESSAGE);
					return;
			    } else {
			    	try {
			    		// the ontology being edited
			    		OWLOntology ont = getOWLOntology(mebn, mediator);
			    		
			    		// class responsible for removing OWL entities
			    		OWLEntityRemover remover = new OWLEntityRemover(ont.getOWLOntologyManager(), ont.getImportsClosure());
			    		
			    		// extract selected value
			    		OWLEntity selectedEntity = (OWLEntity) getTuuyiClassList().getSelectedValue();
						
			    		// mark as deleted
			    		selectedEntity.accept(remover);
			    		
			    		// commit changes
			    		ont.getOWLOntologyManager().applyChanges(remover.getChanges());
						
						// refresh list of class
				        // TODO simply remove 1 element, instead of rebuilding whole list
						rebuildClassList();
						
					} catch (Throwable t) {
						t.printStackTrace();
						JOptionPane.showMessageDialog(null, "Type of error (see console for more details): " + t.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
						return;
					}
			    }
			}
		});
		
		
		// what to do if we press refresh button
		getTuuyiRefreshClassButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// simply rebuild class list
				rebuildClassList();
			}
		});
		
		// what happens if we press enter on search text field
		getTuuyiNameSearchTextField().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				triggerSearchFromCurrentSearchTextField();
			}
		});
		
		// refresh buttons in individual panel
		getTuuyiConsiderIndividualRefreshButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rebuildConsideredIndividualsList();
			}
		});
		getTuuyiIgnoreIndividualRefreshButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rebuildIgnoredIndividualsList();;
			}
		});
		
		// remove buttons in individual panel
		getTuuyiConsiderIndividualRemoveButton().addActionListener(new ActionListener() {
			// if we remove the individual from here, we are removing completely
			public void actionPerformed(ActionEvent e) {
				try {
					// the ontology being edited
					OWLOntology ont = getOWLOntology(mebn, mediator);
					
					// class responsible for removing OWL entities
					OWLEntityRemover remover = new OWLEntityRemover(ont.getOWLOntologyManager(), ont.getImportsClosure());
					
					// delete in batch
					for (Object selected : getTuuyiConsideredIndividualList().getSelectedValues()) {
						if (selected instanceof OWLEntity) {
							OWLEntity selectedEntity = (OWLEntity) selected;

							// mark as deleted
					    	try {
					    		selectedEntity.accept(remover);
							} catch (Throwable t) {
								t.printStackTrace();
							}
					    
						}
					}
					
					// commit changes
					ont.getOWLOntologyManager().applyChanges(remover.getChanges());
					
					// refresh lists
					// TODO simply remove 1 element, instead of rebuilding whole list
					rebuildIgnoredIndividualsList();
					rebuildConsideredIndividualsList();
				} catch (Throwable t) {
					t.printStackTrace();
					JOptionPane.showMessageDialog(null, "Type of error (see console for more details): " + t.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
			}
		});
		getTuuyiIgnoreIndividualRemoveButton().addActionListener(new ActionListener() {
			// if we remove the individual from here, we are removing completely
			public void actionPerformed(ActionEvent e) {
				try {
					// the ontology being edited
					OWLOntology ont = getOWLOntology(mebn, mediator);
					
					// class responsible for removing OWL entities
					OWLEntityRemover remover = new OWLEntityRemover(ont.getOWLOntologyManager(), ont.getImportsClosure());
					
					// delete in batch
					for (Object selected : getTuuyiIgnoredIndividualList().getSelectedValues()) {
						if (selected instanceof OWLEntity) {
							OWLEntity selectedEntity = (OWLEntity) selected;
							
							// mark as deleted
							try {
								selectedEntity.accept(remover);
							} catch (Throwable t) {
								t.printStackTrace();
							}
							
						}
					}
					
					// commit changes
					ont.getOWLOntologyManager().applyChanges(remover.getChanges());
					
					// refresh lists
					// TODO simply remove 1 element, instead of rebuilding whole list
					rebuildIgnoredIndividualsList();
					rebuildConsideredIndividualsList();
				} catch (Throwable t) {
					t.printStackTrace();
					JOptionPane.showMessageDialog(null, "Type of error (see console for more details): " + t.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
			}
		});
		
		
		getTuuyiConsiderIndividualIncludeButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// extract class where individual will be created
				OWLClass selectedClass = (OWLClass)getTuuyiClassList().getSelectedValue();
				if (selectedClass == null) {
					JOptionPane.showMessageDialog(null, "Please, select a custom class (at the left panel)",  "ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String selectedClassName = getPROWL2modelUser().extractName(selectedClass);
				int id = -1;
				
				OWLOntology ontology = getOWLOntology(getMEBN(), getMediator());
				
				PrefixManager prefixManager = new DefaultPrefixManager(ontology.getOntologyID().getOntologyIRI() + "#");
						
				TermWrapper root = ((TermWrapper)((DefaultMutableTreeNode)getTuuyiSearchResultTree().getModel().getRoot()).getUserObject());
				
				OWLDataProperty isInverseHierarchy = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IS_INVERSE_HIERARCHY_PROPERTY_ID, getPrefixManager());
				OWLDataProperty hierarchyPropertyID = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HIERARCHY_PROPERTY_ID, getPrefixManager());
				OWLDataProperty hasUID = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HAS_UID_PROPERTY_NAME, getPrefixManager());
				
				for (TreePath path : getTuuyiSearchResultTree().getSelectionPaths()) {
					TermWrapper selected = ((TermWrapper)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject());
					
					OWLNamedIndividual individual = null;
					switch (selected.getType()) {
					case TermWrapper.PROPERTY:
						id = root.getTerm().getId();	// if included a property, then use id of the root term
						// name of individual is class_root_property
						individual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(
								selectedClassName + "_" + root.getTerm().getSimpleName() + "_" + selected.getTerm().getSimpleName(), 
								prefixManager
						);
						// indicate property
						ontology.getOWLOntologyManager().applyChanges(
								ontology.getOWLOntologyManager().addAxiom(
										ontology, 
										ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(hierarchyPropertyID, individual, selected.getTerm().getId())
								)
						);
						break;
					case TermWrapper.INVERSE_PROPERTY:
						id = root.getTerm().getId();		// if included a property, then use id of the root term
						// name of individual is class_root_INV_property
						individual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(
								selectedClassName + "_" + root.getTerm().getSimpleName() + "_INV_" + selected.getTerm().getSimpleName(), 
								prefixManager
						);
						// indicate property
						ontology.getOWLOntologyManager().applyChanges(
								ontology.getOWLOntologyManager().addAxiom(
										ontology, 
										ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(hierarchyPropertyID, individual, selected.getTerm().getId())
								)
						);
						// mark as inverse
						ontology.getOWLOntologyManager().applyChanges(
								ontology.getOWLOntologyManager().addAxiom(
										ontology, 
										ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(isInverseHierarchy, individual, true)
								)
						);
						break;
					case TermWrapper.TERM:
						id = selected.getTerm().getId();	// if selected a term, use its id
						// name of the individual is class_selected
						individual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(
								selectedClassName + "_" + selected.getTerm().getSimpleName(), 
								prefixManager
						);
						break;

					default:
						// do not create individual
						continue;
					}
					boolean isOfSelectedClassAlready = false;
					for (OWLClassExpression type : individual.getTypes(ontology.getImportsClosure())) {
						if (selectedClass.equals(type)) {
							isOfSelectedClassAlready = true;
							break;
						}
					};
					if (!isOfSelectedClassAlready) {
						// specify class
						List<OWLOntologyChange> changes = ontology.getOWLOntologyManager().addAxiom(
							ontology, 
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(selectedClass, individual)
						);
						// specify ID
						
						changes.addAll(
							ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(hasUID, individual, id)
							)	
						);
						ontology.getOWLOntologyManager().applyChanges(changes);
					}
				}
				rebuildConsideredIndividualsList();
			}
		});
		
		getTuuyiIgnoreIndividualIncludeButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// extract class where individual will be created
				OWLClass selectedClass = (OWLClass)getTuuyiClassList().getSelectedValue();
				if (selectedClass == null) {
					JOptionPane.showMessageDialog(null, "Please, select a custom class (at the left panel)",  "ERROR", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				String selectedClassName = getPROWL2modelUser().extractName(selectedClass);
				
				OWLOntology ontology = getOWLOntology(getMEBN(), getMediator());
				
				PrefixManager prefixManager = new DefaultPrefixManager(ontology.getOntologyID().getOntologyIRI() + "#");
				
				OWLDataProperty isToExclude = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(IS_TO_EXCLUDE_DATA_PROPERTY_NAME, getPrefixManager());
				OWLDataProperty hasUID = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(HAS_UID_PROPERTY_NAME, getPrefixManager());
				
				for (TreePath path : getTuuyiSearchResultTree().getSelectionPaths()) {
					TermWrapper selected = ((TermWrapper)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject());
					
					OWLNamedIndividual individual = null;
					switch (selected.getType()) {
					case TermWrapper.TERM:
						// name of the individual is class_selected
						individual = ontology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(
								selectedClassName + "_" + selected.getTerm().getSimpleName(), 
								prefixManager
								);
						break;
					default:
						// do not create individual
						continue;
					}
					
					ontology.getOWLOntologyManager().applyChanges(
						ontology.getOWLOntologyManager().addAxiom(
							ontology, 
							ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(isToExclude, individual, true)
						)	
					);
					
					boolean isOfSelectedClassAlready = false;
					for (OWLClassExpression type : individual.getTypes(ontology.getImportsClosure())) {
						if (selectedClass.equals(type)) {
							isOfSelectedClassAlready = true;
							break;
						}
					};
					
					if (!isOfSelectedClassAlready) {
						// specify class
						List<OWLOntologyChange> changes = ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClassAssertionAxiom(selectedClass, individual)
						);
						
						// specify ID
						changes.addAll(
							ontology.getOWLOntologyManager().addAxiom(
								ontology, 
								ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataPropertyAssertionAxiom(hasUID, individual, selected.getTerm().getId())
							)	
						);
						// indicate that it is to ignore when questions are generating
						
						ontology.getOWLOntologyManager().applyChanges(changes);
					}
				}
				rebuildIgnoredIndividualsList();
				rebuildConsideredIndividualsList();
			}
		});
		
		
		
	}

	/**
	 * Uses the state of {@link #getTuuyiNameSearchTextField()},
	 * {@link #getTuuyiNameSearchExactMatchCheckBox()}, and {@link #getTuuyiSearchIDCheckBox()}
	 * in order to perform a new search
	 */
	public void triggerSearchFromCurrentSearchTextField() {
		Term term = null;
		try {
			if (getTuuyiSearchIDCheckBox().isSelected()) {
				// search by term id
				term = getOntologyClient().getTermById(Integer.parseInt(getTuuyiNameSearchTextField().getText()));
			} else if (getTuuyiNameSearchExactMatchCheckBox().isSelected()) {
				// exact search by simple name
				term = getOntologyClient().getTermBySimpleName(getTuuyiNameSearchTextField().getText());
			} else {
				// Slowest: normal search by name
				term = getOntologyClient().matchTermByName(getTuuyiNameSearchTextField().getText());
			}
			// update the results
			this.rebuildSearchTree(term);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Message: " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
		}
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
	public MultiEntityBayesianNetwork getMEBN() {
		return mebn;
	}




	/**
	 * @param mebn the mebn to set
	 */
	public void setMEBN(MultiEntityBayesianNetwork mebn) {
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




	/**
	 * This prefix manager stores information about ontology prefixes
	 * that identifies the scicast upper ontology definition.
	 * In other words, OWL objects with this prefix in its IRI
	 * are to be considered part of scicast upper ontology definition.
	 * @return the prefixManager
	 */
	public PrefixManager getPrefixManager() {
		return prefixManager;
	}




	/**
	 * This prefix manager stores information about ontology prefixes
	 * that identifies the scicast upper ontology definition.
	 * In other words, OWL objects with this prefix in its IRI
	 * are to be considered part of scicast upper ontology definition.
	 * @param prefixManager the prefixManager to set
	 */
	public void setPrefixManager(
			PrefixManager questionGeneratorOntologyPrefixManager) {
		this.prefixManager = questionGeneratorOntologyPrefixManager;
	}




	/**
	 * @return the tuuyiClassListScrollPane
	 */
	public JScrollPane getTuuyiClassListScrollPane() {
		return tuuyiClassListScrollPane;
	}




	/**
	 * @param tuuyiClassListScrollPane the tuuyiClassListScrollPane to set
	 */
	public void setTuuyiClassListScrollPane(JScrollPane tuuyiClassListScrollPane) {
		this.tuuyiClassListScrollPane = tuuyiClassListScrollPane;
	}




	/**
	 * @return the tuuyiClassList
	 */
	public JList getTuuyiClassList() {
		return tuuyiClassList;
	}




	/**
	 * @param tuuyiClassList the tuuyiClassList to set
	 */
	public void setTuuyiClassList(JList tuuyiClassList) {
		this.tuuyiClassList = tuuyiClassList;
	}




	/**
	 * @return the tuuyiClassToolBar
	 */
	public JToolBar getTuuyiClassToolBar() {
		return tuuyiClassToolBar;
	}




	/**
	 * @param tuuyiClassToolBar the tuuyiClassToolBar to set
	 */
	public void setTuuyiClassToolBar(JToolBar tuuyiClassToolBar) {
		this.tuuyiClassToolBar = tuuyiClassToolBar;
	}




	/**
	 * @return the tuuyiClassListPanel
	 */
	public JPanel getTuuyiClassListPanel() {
		return tuuyiClassListPanel;
	}




	/**
	 * @param tuuyiClassListPanel the tuuyiClassListPanel to set
	 */
	public void setTuuyiClassListPanel(JPanel tuuyiClassListPanel) {
		this.tuuyiClassListPanel = tuuyiClassListPanel;
	}




	/**
	 * @return the tuuyiAddClassButton
	 */
	public JButton getTuuyiAddClassButton() {
		return tuuyiAddClassButton;
	}




	/**
	 * @param tuuyiAddClassButton the tuuyiAddClassButton to set
	 */
	public void setTuuyiAddClassButton(JButton tuuyiAddClassButton) {
		this.tuuyiAddClassButton = tuuyiAddClassButton;
	}




	/**
	 * @return the tuuyiRemoveClassButton
	 */
	public JButton getTuuyiRemoveClassButton() {
		return tuuyiRemoveClassButton;
	}




	/**
	 * @param tuuyiRemoveClassButton the tuuyiRemoveClassButton to set
	 */
	public void setTuuyiRemoveClassButton(JButton tuuyiRemoveClassButton) {
		this.tuuyiRemoveClassButton = tuuyiRemoveClassButton;
	}




	/**
	 * @return the ontologyClient
	 */
	public OntologyClient getOntologyClient() {
		return ontologyClient;
	}




	/**
	 * @param ontologyClient the ontologyClient to set
	 */
	public void setOntologyClient(OntologyClient ontologyClient) {
		this.ontologyClient = ontologyClient;
	}




	/**
	 * @return the prowl2modelUser
	 */
	public IPROWL2ModelUser getPROWL2modelUser() {
		return prowl2modelUser;
	}




	/**
	 * @param prowl2modelUser the prowl2modelUser to set
	 */
	public void setPROWL2modelUser(IPROWL2ModelUser prowl2modelUser) {
		this.prowl2modelUser = prowl2modelUser;
	}




	/**
	 * @return the editSplitPane
	 */
	public JSplitPane getEditSplitPane() {
		return editSplitPane;
	}




	/**
	 * @param editSplitPane the editSplitPane to set
	 */
	public void setEditSplitPane(JSplitPane editSplitPane) {
		this.editSplitPane = editSplitPane;
	}




	/**
	 * @return the classSplitPane
	 */
	public JSplitPane getClassSplitPane() {
		return classSplitPane;
	}




	/**
	 * @param classSplitPane the classSplitPane to set
	 */
	public void setClassSplitPane(JSplitPane classSplitPane) {
		this.classSplitPane = classSplitPane;
	}




	/**
	 * @return the tuuyiRefreshClassButton
	 */
	public JButton getTuuyiRefreshClassButton() {
		return tuuyiRefreshClassButton;
	}




	/**
	 * @param tuuyiRefreshClassButton the tuuyiRefreshClassButton to set
	 */
	public void setTuuyiRefreshClassButton(JButton tuuyiRefreshClassButton) {
		this.tuuyiRefreshClassButton = tuuyiRefreshClassButton;
	}




	/**
	 * @return the searchPanel
	 */
	public JPanel getSearchPanel() {
		return searchPanel;
	}




	/**
	 * @param searchPanel the searchPanel to set
	 */
	public void setSearchPanel(JPanel searchPanel) {
		this.searchPanel = searchPanel;
	}




	/**
	 * @return the tuuyiSearchScrollPane
	 */
	public JScrollPane getTuuyiSearchScrollPane() {
		return tuuyiSearchScrollPane;
	}




	/**
	 * @param tuuyiSearchScrollPane the tuuyiSearchScrollPane to set
	 */
	public void setTuuyiSearchScrollPane(JScrollPane tuuyiSearchScrollPane) {
		this.tuuyiSearchScrollPane = tuuyiSearchScrollPane;
	}




	/**
	 * @return the tuuyiSearchToolBar
	 */
	public JToolBar getTuuyiNameSearchToolBar() {
		return tuuyiSearchToolBar;
	}




	/**
	 * @param tuuyiSearchToolBar the tuuyiSearchToolBar to set
	 */
	public void setTuuyiNameSearchToolBar(JToolBar tuuyiNameSearchToolBar) {
		this.tuuyiSearchToolBar = tuuyiNameSearchToolBar;
	}







	/**
	 * @return the tuuyiNameSearchTextField
	 */
	public JTextField getTuuyiNameSearchTextField() {
		return tuuyiNameSearchTextField;
	}




	/**
	 * @param tuuyiNameSearchTextField the tuuyiNameSearchTextField to set
	 */
	public void setTuuyiNameSearchTextField(JTextField tuuyiNameSearchTextField) {
		this.tuuyiNameSearchTextField = tuuyiNameSearchTextField;
	}




	/**
	 * @return the tuuyiNameSearchExactMatchCheckBox
	 */
	public JCheckBox getTuuyiNameSearchExactMatchCheckBox() {
		return tuuyiNameSearchExactMatchCheckBox;
	}




	/**
	 * @param tuuyiNameSearchExactMatchCheckBox the tuuyiNameSearchExactMatchCheckBox to set
	 */
	public void setTuuyiNameSearchExactMatchCheckBox(
			JCheckBox tuuyiNameSearchExactMatchCheckBox) {
		this.tuuyiNameSearchExactMatchCheckBox = tuuyiNameSearchExactMatchCheckBox;
	}




	/**
	 * @return the tuuyiSearchResultPanel
	 */
	public JPanel getTuuyiSearchResultPanel() {
		return tuuyiSearchResultPanel;
	}




	/**
	 * @param tuuyiSearchResultPanel the tuuyiSearchResultPanel to set
	 */
	public void setTuuyiSearchResultPanel(JPanel tuuyiSearchResultPanel) {
		this.tuuyiSearchResultPanel = tuuyiSearchResultPanel;
	}




	/**
	 * @return the tuuyiSearchIDCheckBox
	 */
	public JCheckBox getTuuyiSearchIDCheckBox() {
		return tuuyiSearchIDCheckBox;
	}




	/**
	 * @param tuuyiSearchIDCheckBox the tuuyiSearchIDCheckBox to set
	 */
	public void setTuuyiSearchIDCheckBox(JCheckBox tuuyiSearchIDCheckBox) {
		this.tuuyiSearchIDCheckBox = tuuyiSearchIDCheckBox;
	}




	/**
	 * @return the tuuyiConsiderIndividualIncludeButton
	 */
	public JButton getTuuyiConsiderIndividualIncludeButton() {
		return tuuyiConsiderIndividualIncludeButton;
	}




	/**
	 * @param tuuyiConsiderIndividualIncludeButton the tuuyiConsiderIndividualIncludeButton to set
	 */
	public void setTuuyiConsiderIndividualIncludeButton(
			JButton tuuyiConsiderIndividualIncludeButton) {
		this.tuuyiConsiderIndividualIncludeButton = tuuyiConsiderIndividualIncludeButton;
	}




	/**
	 * @return the tuuyiConsiderIndividualRemoveButton
	 */
	public JButton getTuuyiConsiderIndividualRemoveButton() {
		return tuuyiConsiderIndividualRemoveButton;
	}




	/**
	 * @param tuuyiConsiderIndividualRemoveButton the tuuyiConsiderIndividualRemoveButton to set
	 */
	public void setTuuyiConsiderIndividualRemoveButton(
			JButton tuuyiConsiderIndividualRemoveButton) {
		this.tuuyiConsiderIndividualRemoveButton = tuuyiConsiderIndividualRemoveButton;
	}




	/**
	 * @return the tuuyiConsiderIndividualToolBar
	 */
	public JToolBar getTuuyiConsiderIndividualToolBar() {
		return tuuyiConsiderIndividualToolBar;
	}




	/**
	 * @param tuuyiConsiderIndividualToolBar the tuuyiConsiderIndividualToolBar to set
	 */
	public void setTuuyiConsiderIndividualToolBar(
			JToolBar tuuyiConsiderIndividualToolBar) {
		this.tuuyiConsiderIndividualToolBar = tuuyiConsiderIndividualToolBar;
	}




	/**
	 * @return the tuuyiIgnoreIndividualToolBar
	 */
	public JToolBar getTuuyiIgnoreIndividualToolBar() {
		return tuuyiIgnoreIndividualToolBar;
	}




	/**
	 * @param tuuyiIgnoreIndividualToolBar the tuuyiIgnoreIndividualToolBar to set
	 */
	public void setTuuyiIgnoreIndividualToolBar(
			JToolBar tuuyiIgnoreIndividualToolBar) {
		this.tuuyiIgnoreIndividualToolBar = tuuyiIgnoreIndividualToolBar;
	}




	/**
	 * @return the tuuyiIgnoreIndividualIncludeButton
	 */
	public JButton getTuuyiIgnoreIndividualIncludeButton() {
		return tuuyiIgnoreIndividualIncludeButton;
	}




	/**
	 * @param tuuyiIgnoreIndividualIncludeButton the tuuyiIgnoreIndividualIncludeButton to set
	 */
	public void setTuuyiIgnoreIndividualIncludeButton(
			JButton tuuyiConsiderIgnoreIndividualIncludeButton) {
		this.tuuyiIgnoreIndividualIncludeButton = tuuyiConsiderIgnoreIndividualIncludeButton;
	}




	/**
	 * @return the tuuyiIgnoreIndividualRemoveButton
	 */
	public JButton getTuuyiIgnoreIndividualRemoveButton() {
		return tuuyiIgnoreIndividualRemoveButton;
	}




	/**
	 * @param tuuyiIgnoreIndividualRemoveButton the tuuyiIgnoreIndividualRemoveButton to set
	 */
	public void setTuuyiIgnoreIndividualRemoveButton(
			JButton tuuyiIgnoreIndividualRemoveButton) {
		this.tuuyiIgnoreIndividualRemoveButton = tuuyiIgnoreIndividualRemoveButton;
	}




	/**
	 * @return the tuuyiIgnoreIndividualRefreshButton
	 */
	public JButton getTuuyiIgnoreIndividualRefreshButton() {
		return tuuyiIgnoreIndividualRefreshButton;
	}




	/**
	 * @param tuuyiIgnoreIndividualRefreshButton the tuuyiIgnoreIndividualRefreshButton to set
	 */
	public void setTuuyiIgnoreIndividualRefreshButton(
			JButton tuuyiIgnoreIndividualRefreshButton) {
		this.tuuyiIgnoreIndividualRefreshButton = tuuyiIgnoreIndividualRefreshButton;
	}




	/**
	 * @return the tuuyiSearchResultTree
	 */
	public JTree getTuuyiSearchResultTree() {
		return tuuyiSearchResultTree;
	}




	/**
	 * @param tuuyiSearchResultTree the tuuyiSearchResultTree to set
	 */
	public void setTuuyiSearchResultTree(JTree tuuyiSearchResultTree) {
		this.tuuyiSearchResultTree = tuuyiSearchResultTree;
	}




	/**
	 * @return the tuuyiIgnoreIndividualPanel
	 */
	public JPanel getTuuyiIgnoreIndividualPanel() {
		return tuuyiIgnoreIndividualPanel;
	}




	/**
	 * @param tuuyiIgnoreIndividualPanel the tuuyiIgnoreIndividualPanel to set
	 */
	public void setTuuyiIgnoreIndividualPanel(JPanel tuuyiIgnoreIndividualPanel) {
		this.tuuyiIgnoreIndividualPanel = tuuyiIgnoreIndividualPanel;
	}




	/**
	 * @return the tuuyiConsiderIndividualRefreshButton
	 */
	public JButton getTuuyiConsiderIndividualRefreshButton() {
		return tuuyiConsiderIndividualRefreshButton;
	}




	/**
	 * @param tuuyiConsiderIndividualRefreshButton the tuuyiConsiderIndividualRefreshButton to set
	 */
	public void setTuuyiConsiderIndividualRefreshButton(
			JButton tuuyiConsiderIndividualRefreshButton) {
		this.tuuyiConsiderIndividualRefreshButton = tuuyiConsiderIndividualRefreshButton;
	}




	/**
	 * @return the tuuyiConsiderIndividualPanel
	 */
	public JPanel getTuuyiConsiderIndividualPanel() {
		return tuuyiConsiderIndividualPanel;
	}




	/**
	 * @param tuuyiConsiderIndividualPanel the tuuyiConsiderIndividualPanel to set
	 */
	public void setTuuyiConsiderIndividualPanel(JPanel considerIndividualPanel) {
		this.tuuyiConsiderIndividualPanel = considerIndividualPanel;
	}








	/**
	 * @return the tuuyiIgnoredIndividualList
	 */
	public JList getTuuyiIgnoredIndividualList() {
		return tuuyiIgnoredIndividualList;
	}




	/**
	 * @param tuuyiIgnoredIndividualList the tuuyiIgnoredIndividualList to set
	 */
	public void setTuuyiIgnoredIndividualList(JList tuuyiIgnoredIndividualList) {
		this.tuuyiIgnoredIndividualList = tuuyiIgnoredIndividualList;
	}




	/**
	 * @return the isToUseReasoner : if true, {@link #getReasoner()} will attempt to access reasoner from {@link #getMEBN()} or {@link #getMediator()}
	 */
	public boolean isToUseReasoner() {
		return isToUseReasoner;
	}




	/**
	 * @param isToUseReasoner the isToUseReasoner to set: if true, {@link #getReasoner()} will attempt to access reasoner from {@link #getMEBN()} or {@link #getMediator()}
	 */
	public void setToUseReasoner(boolean isToUseReasoner) {
		this.isToUseReasoner = isToUseReasoner;
	}




	/**
	 * @return the tuuyiConsideredIndividualList
	 */
	public JList getTuuyiConsideredIndividualList() {
		return tuuyiConsideredIndividualList;
	}




	/**
	 * @param tuuyiConsideredIndividualList the tuuyiConsideredIndividualList to set
	 */
	public void setTuuyiConsideredIndividualList(
			JList tuuyiConsideredIndividualList) {
		this.tuuyiConsideredIndividualList = tuuyiConsideredIndividualList;
	}





}
