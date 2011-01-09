/**
 * 
 */
package unbbayes.gui.mebn.ontology.protege;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;

import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ui.error.ErrorLog;
import org.protege.editor.core.ui.error.ErrorNotificationLabel;
import org.protege.editor.core.ui.progress.BackgroundTaskLabel;
import org.protege.editor.core.ui.progress.BackgroundTaskManager;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.workspace.CustomWorkspaceTabsManager;
import org.protege.editor.owl.model.OWLEntityDisplayProvider;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.UIReasonerExceptionHandler;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.selection.OWLSelectionModel;
import org.protege.editor.owl.model.selection.OWLSelectionModelImpl;
import org.protege.editor.owl.ui.find.EntityFinderField;
import org.protege.editor.owl.ui.inference.ReasonerProgressUI;
import org.protege.editor.owl.ui.navigation.OWLEntityNavPanel;
import org.protege.editor.owl.ui.ontology.OntologySourcesChangedHandlerUI;
import org.protege.editor.owl.ui.preferences.AnnotationPreferences;
import org.protege.editor.owl.ui.renderer.KeywordColourMap;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.protege.editor.owl.ui.renderer.OWLIconProviderImpl;
import org.protege.editor.owl.ui.renderer.OWLOntologyCellRenderer;
import org.protege.editor.owl.ui.util.OWLComponentFactory;
import org.protege.editor.owl.ui.util.OWLComponentFactoryImpl;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.util.OWLEntityCollectingOntologyChangeListener;

import unbbayes.util.Debug;

/**
 * This class extends protege's OWLWorkspace in order to stop loading plugins.
 * This is because UnBBayes does not use osgi plugins and
 * protege seems to fail when osgi is not used.
 * Some refactories (e.g. using getters/setters, creating template - protected - methods) was made in order to improve
 * extensibility.
 * @author Shou Matsumoto
 *
 */
public class UnBBayesOWLWorkspace extends OWLWorkspace {

    public static final int FINDER_BORDER = 2;
    public static final int FINDER_MIN_WIDTH = 250;
	
	// the following attributes are a copy from superclass. We could not use them as is because of visibility
	private ArrayList<OWLEntityDisplayProvider> entityDisplayProviders;
	private OWLSelectionModel owlSelectionModel;
    private Map<String, Color> keyWordColorMap;
    private JLabel errorNotificationLabel;
    private JLabel backgroundTaskLabel;
	private ErrorLog errorLog;
	private BackgroundTaskManager bgTaskManager;
	private Set<URI> hiddenAnnotationURIs = new HashSet<URI>();
    private OWLComponentFactory owlComponentFactory;
    private OWLModelManagerListener owlModelManagerListener;
    private OWLEntityCollectingOntologyChangeListener owlEntityCollectingOntologyChangeListener;
    private Set<EventType> reselectionEventTypes = new HashSet<EventType>();

	/**
	 * The constructor is not private in order to allow inheritance
	 * @deprecated use {@link #getInstance()} instead.
	 */
	protected UnBBayesOWLWorkspace() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * This is the default constructor method.
	 * @return a new instance of workspace
	 */
	public static OWLWorkspace getInstance() {
		UnBBayesOWLWorkspace ret = new UnBBayesOWLWorkspace();
		return ret;
	}
	
	

	/**
	 * This method had to be reimplemented because the original method was implemented in a way that as-is reusage was practically prohibitive
	 * (e.g. it was not using designs like template methods or extensible getters/setters)
	 * @see org.protege.editor.owl.model.OWLWorkspace#initialise()
	 */
	public void initialise() {
		
		this.setEntityDisplayProviders(new ArrayList<OWLEntityDisplayProvider>());
        this.setOWLIconProvider(new OWLIconProviderImpl(getOWLEditorKit().getModelManager()));
        this.setOWLSelectionModel(new OWLSelectionModelImpl());

        this.setKeyWordColorMap(new KeywordColourMap());

        // the following code looks like insignificant in the original (super)class
//        defaultAnnotationProperties = new ArrayList<URI>();
//        defaultAnnotationProperties.add(OWLRDFVocabulary.RDFS_COMMENT.getURI());

        try {
        	super.initialise();
        } catch (Throwable e) {
			Debug.println(this.getClass(), "Failed to initialize superclass, but we'll initialyze this class anyway.", e);
		}
        
        this.setErrorLog(new ErrorLog());

        this.createActiveOntologyPanel();
        
        this.getReselectionEventTypes().add(EventType.ACTIVE_ONTOLOGY_CHANGED);
        this.getReselectionEventTypes().add(EventType.ONTOLOGY_RELOADED);
        this.getReselectionEventTypes().add(EventType.ENTITY_RENDERER_CHANGED);
        this.getReselectionEventTypes().add(EventType.ONTOLOGY_VISIBILITY_CHANGED);
        this.getReselectionEventTypes().add(EventType.REASONER_CHANGED);

        this.setHiddenAnnotationURIs(new HashSet<URI>());
        this.getHiddenAnnotationURIs().addAll(AnnotationPreferences.getHiddenAnnotationURIs());

        this.setOWLComponentFactory(new OWLComponentFactoryImpl(this.getOWLEditorKit()));

        final OWLModelManager mngr = this.getOWLModelManager();

        this.setOWLModelManagerListener(new OWLModelManagerListener() {
            public void handleChange(OWLModelManagerChangeEvent event) {
                handleModelManagerEvent(event.getType());
            }
        });
        mngr.addListener(this.getOWLModelManagerListener());

        this.setOWLEntityCollectingOntologyChangeListener(new OWLEntityCollectingOntologyChangeListener() {
            public void ontologiesChanged() {
                verifySelection(getEntities());
            }

            public void ontologiesChanged(List<? extends OWLOntologyChange> changes) throws OWLException {
                super.ontologiesChanged(changes);
                handleOntologiesChanged(changes);
            }
        });
        mngr.addOntologyChangeListener(this.getOWLEntityCollectingOntologyChangeListener());

        if (mngr.getOWLReasonerManager() != null) {
        	mngr.getOWLReasonerManager().setReasonerProgressMonitor(new ReasonerProgressUI(getOWLEditorKit()));
        	mngr.getOWLReasonerManager().setReasonerExceptionHandler(new UIReasonerExceptionHandler(this));
        }
        
        // Probably, we do not need to update reasoner status in UnBBayes. So, let's comment the following lines
//        reasonerManagerStarted = true;
//        updateReasonerStatus(false);
        
        // I don't think we should use OWL2 reasoners in UnBBayes' context.
//        displayReasonerResults.setSelected(mngr.getOWLReasonerManager().getReasonerPreferences().isShowInferences());
//        displayReasonerResults.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                ReasonerPreferences prefs = mngr.getOWLReasonerManager().getReasonerPreferences();
//                prefs.setShowInferences(displayReasonerResults.isSelected());
//            }
//        });

//        new OntologySourcesChangedHandlerUI(this);
	}
	
	/**
	 * This method is initialized in {@link #initialise()} and called by {@link #getOWLEntityCollectingOntologyChangeListener()}
	 * @param changes
	 */
    protected void handleOntologiesChanged(List<? extends OWLOntologyChange> changes) {
        for (OWLOntologyChange chg : changes){
            if (chg instanceof SetOntologyID){
                rebuildOntologiesMenu();
//                updateTitleBar();
                break;
            }
//            else if (chg instanceof ImportChange) {
//                updateReasonerStatus(true);
//            }
//            else if (chg instanceof OWLAxiomChange && chg.getAxiom().isLogicalAxiom()) {
//                updateReasonerStatus(true);
//            }
        }

        // the following code is OS-X specifi, so, we do not have to call it
//        updateDirtyFlag();
    }
    
    /**
     * This method is called in {@link #handleOntologiesChanged(List)} in order to update menu.
     */
    protected void rebuildOntologiesMenu() {
    	// we do not want to use menu items
    	return;
    }
    

//    /**
//     * This method is called in {@link #handleModelManagerEvent(EventType)} in order to
//     * reset the list of ontologies
//     */
//    protected void rebuildList() {
//        try {
//            TreeSet<OWLOntology> ts = new TreeSet<OWLOntology>(getOWLModelManager().getOWLObjectComparator());
//            ts.addAll(getOWLModelManager().getOntologies());
//            ontologiesList.setModel(new DefaultComboBoxModel(ts.toArray()));
//            ontologiesList.setSelectedItem(getOWLModelManager().getActiveOntology());
//        }
//        catch (Exception e) {
//            ProtegeApplication.getErrorLog().logError(e);
//        }
//    }
	
	/**
	 * This is called when {@link #getOWLModelManagerListener()} is activated
	 * @param type
	 */
    protected void handleModelManagerEvent(EventType type) {
        if (this.getReselectionEventTypes().contains(type)) {
        	Debug.println(this.getClass(), "Reselection event..... verifying selections.");
            verifySelection();
        }

        switch (type) {
        case ACTIVE_ONTOLOGY_CHANGED:
        	// we do not have the concept of active ontology, because it will be the one loaded by IO class
//            updateTitleBar();
//            updateReasonerStatus(false);
//            rebuildList();
//            rebuildOntologiesMenu();
//            ontologiesList.repaint();
            break;
        case ONTOLOGY_CLASSIFIED:
        	// The reasoner status is not important to us (UnBBayes is not likely to use a DL reasoner unless building SSBN)
//            updateReasonerStatus(false);
            this.verifySelection();
//            updateReasonerStatus(false);
            break;
        case ABOUT_TO_CLASSIFY:
        case REASONER_CHANGED:
//            updateReasonerStatus(false);
            break;
        case ONTOLOGY_LOADED:
        case ONTOLOGY_CREATED:
            if (getTabCount() > 0) {
                setSelectedTab(0);
            }
            break;
        case ENTITY_RENDERER_CHANGED:
        case ONTOLOGY_RELOADED:
            refreshComponents();
            break;
        case ONTOLOGY_SAVED:
        	// this is OS-X specific code
//            updateDirtyFlag();
            break;
        case ENTITY_RENDERING_CHANGED:
        	break;
        case ONTOLOGY_VISIBILITY_CHANGED:
            break;
        default:
            ProtegeApplication.getErrorLog().logError(new RuntimeException("Programmer Error - missed a case"));
        }
    }

	
	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.model.OWLWorkspace#dispose()
	 */
    public void dispose() {
        // Save our workspace!
        try {
        	super.dispose();
        } catch (Throwable e) {
			e.printStackTrace();
		}

        this.getOWLComponentFactory().dispose();

        getOWLModelManager().removeListener(this.getOWLModelManagerListener());
        getOWLModelManager().removeOntologyChangeListener(this.getOWLEntityCollectingOntologyChangeListener());
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.model.OWLWorkspace#setHiddenAnnotationURI(java.net.URI, boolean)
	 */
    public void setHiddenAnnotationURI(URI annotationURI, boolean hidden) {
        boolean changed;
        if (hidden) {
            changed = this.getHiddenAnnotationURIs().add(annotationURI);
        } else {
            changed = this.getHiddenAnnotationURIs().remove(annotationURI);
        }
        if (changed) {
            AnnotationPreferences.setHiddenAnnotationURIs(this.getHiddenAnnotationURIs());
            getOWLEditorKit().getModelManager().fireEvent(EventType.ACTIVE_ONTOLOGY_CHANGED);
        }
    }

	
	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.model.OWLWorkspace#isHiddenAnnotationURI(java.net.URI)
	 */
    public boolean isHiddenAnnotationURI(URI annotationURI) {
        return this.getHiddenAnnotationURIs().contains(annotationURI);
    }
    
    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.OWLWorkspace#createOWLCellRenderer(boolean, boolean)
     */
    public OWLCellRenderer createOWLCellRenderer(boolean renderExpression, boolean renderIcon) {
    	return null;
        // create an OWL cell renderer that does not load OSGI plugins
//    	return UnBBayesOWLCellRenderer.getInstance(this.getOWLEditorKit(), renderExpression, renderIcon);
    }

	/**
	 * Actually fill the panel with ontology values
	 */
    protected void createActiveOntologyPanel() {

        JPanel topBarPanel = new JPanel(new GridBagLayout());
        topBarPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 3, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 4, 0, 4);
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        topBarPanel.add(new OWLEntityNavPanel(getOWLEditorKit()), gbc);

        final OWLModelManager mngr = getOWLModelManager();

// Install the active ontology combo box
        
        // We are not very interested in changing the active ontology (because the active one will always be the one loaded by IO classes)
//        ontologiesList = new JComboBox();
//        ontologiesList.setToolTipText("Active ontology");
//        ontologiesList.setRenderer(new OWLOntologyCellRenderer(getOWLEditorKit()));
//        rebuildList();

        //        topBarPanel.add(ontologiesList, gbc);
        
//        ontologiesList.addActionListener(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                OWLOntology ont = (OWLOntology) ontologiesList.getSelectedItem();
//                if (ont != null) {
//                    mngr.setActiveOntology(ont);
//                }
//            }
//        });

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 100; // Grow along the x axis
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

// Global find field
        JPanel finderHolder = new JPanel();
        finderHolder.setLayout(new BoxLayout(finderHolder, BoxLayout.LINE_AXIS));
        finderHolder.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1,
                                                                                                  1,
                                                                                                  1,
                                                                                                  1,
                                                                                                  Color.LIGHT_GRAY),
                                                                  BorderFactory.createEmptyBorder(FINDER_BORDER, FINDER_BORDER,
                                                                                                  FINDER_BORDER, FINDER_BORDER)));
        final EntityFinderField entityFinderField = new EntityFinderField(this, getOWLEditorKit());
        final JLabel searchLabel = new JLabel(Icons.getIcon("object.search.gif"));
        final int height = entityFinderField.getPreferredSize().height;
        searchLabel.setPreferredSize(new Dimension(height, height));
        finderHolder.setMinimumSize(new Dimension(FINDER_MIN_WIDTH,
                                                  height+((FINDER_BORDER+1)*2)));
        finderHolder.add(searchLabel);
        finderHolder.add(entityFinderField);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        topBarPanel.add(finderHolder, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        topBarPanel.add(this.getBackgroundTaskLabel());
        topBarPanel.add(this.getErrorNotificationLabel());

        add(topBarPanel, BorderLayout.NORTH);

// Find focus accelerator
        KeyStroke findKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                         Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(findKeyStroke, "FOCUS_FIND");
        getActionMap().put("FOCUS_FIND", new AbstractAction() {
            /**
             * 
             */
            private static final long serialVersionUID = -2205711779338124168L;

            public void actionPerformed(ActionEvent e) {
                entityFinderField.requestFocus();
            }
        });
        
        // We do not want to let protege classes to update the top level frame (which is now the UnBBayes' frame)
//        this.updateTitleBar();
    }

	
	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.model.OWLWorkspace#displayOWLEntity(org.semanticweb.owlapi.model.OWLEntity)
	 */
    public void displayOWLEntity(OWLEntity owlEntity) {
        OWLEntityDisplayProvider candidate = null;
        for (OWLEntityDisplayProvider provider : this.getEntityDisplayProviders()) {
            if (provider.canDisplay(owlEntity)) {
                if (candidate == null){
                    candidate = provider;
                }
                if (provider.getDisplayComponent().isShowing()) {
                    candidate = provider;
                    break;
                }
            }
        }
        if (candidate != null) {
            JComponent component = candidate.getDisplayComponent();
            if (component != null) {
                this.bringComponentToFront(component);
            }
        }
    }
    
    /**
     * This recursive method brings a given component to the upper layer.
     * @param component
     */
    protected void bringComponentToFront(Component component) {
    	if (component == null) {
    		return;
    	}
        if (component.isShowing()) {
            return;
        }
        Component parent = component.getParent();
        if (parent == null) {
            return;
        }
        if (parent instanceof JTabbedPane) {
            ((JTabbedPane) parent).setSelectedComponent(component);
        }
        bringComponentToFront(parent);
    }

    
    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.OWLWorkspace#registerOWLEntityDisplayProvider(org.protege.editor.owl.model.OWLEntityDisplayProvider)
     */
    public void registerOWLEntityDisplayProvider(OWLEntityDisplayProvider provider) {
        this.getEntityDisplayProviders().add(provider);
    }

    /*
     * (non-Javadoc)
     * @see org.protege.editor.owl.model.OWLWorkspace#unregisterOWLEntityDisplayProvider(org.protege.editor.owl.model.OWLEntityDisplayProvider)
     */
    public void unregisterOWLEntityDisplayProvider(OWLEntityDisplayProvider provider) {
    	this.getEntityDisplayProviders().remove(provider);
    }


	/**
	 * @return the entityDisplayProviders
	 */
	public ArrayList<OWLEntityDisplayProvider> getEntityDisplayProviders() {
		return entityDisplayProviders;
	}

	/**
	 * @param entityDisplayProviders the entityDisplayProviders to set
	 */
	public void setEntityDisplayProviders(
			ArrayList<OWLEntityDisplayProvider> entityDisplayProviders) {
		this.entityDisplayProviders = entityDisplayProviders;
	}

	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.model.OWLWorkspace#getOWLSelectionModel()
	 */
	public OWLSelectionModel getOWLSelectionModel() {
		return owlSelectionModel;
	}

	/**
	 * @param owlSelectionModel the owlSelectionModel to set
	 * @see #getOWLSelectionModel()
	 */
	public void setOWLSelectionModel(OWLSelectionModel owlSelectionModel) {
		this.owlSelectionModel = owlSelectionModel;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.model.OWLWorkspace#getKeyWordColorMap()
	 */
    public Map<String, Color> getKeyWordColorMap() {
        return keyWordColorMap;
    }
    
    /**
     * @param keyWordColorMap to set
     * @see #getKeyWordColorMap()
     */
    public void setKeyWordColorMap(Map<String, Color> keyWordColorMap) {
        this.keyWordColorMap = keyWordColorMap;
    }

	/**
	 * @return the errorNotificationLabel
	 */
	public JLabel getErrorNotificationLabel() {
		if (this.errorNotificationLabel == null) {
			this.errorNotificationLabel = new ErrorNotificationLabel(this.getErrorLog(), this);
		}
		return errorNotificationLabel;
	}

	/**
	 * @param errorNotificationLabel the errorNotificationLabel to set
	 */
	public void setErrorNotificationLabel(JLabel errorNotificationLabel) {
		this.errorNotificationLabel = errorNotificationLabel;
	}

	/**
	 * @return the errorLog
	 */
	public ErrorLog getErrorLog() {
		return errorLog;
	}

	/**
	 * @param errorLog the errorLog to set
	 */
	public void setErrorLog(ErrorLog errorLog) {
		this.errorLog = errorLog;
	}

	/**
	 * @return the backgroundTaskLabel
	 */
	public JLabel getBackgroundTaskLabel() {
		if (backgroundTaskLabel == null) {
			backgroundTaskLabel = new BackgroundTaskLabel(this.getBgTaskManager());
		}
		return backgroundTaskLabel;
	}

	/**
	 * @param backgroundTaskLabel the backgroundTaskLabel to set
	 */
	public void setBackgroundTaskLabel(JLabel backgroundTaskLabel) {
		this.backgroundTaskLabel = backgroundTaskLabel;
	}

	/**
	 * @return the bgTaskManager
	 */
	public BackgroundTaskManager getBgTaskManager() {
		if (bgTaskManager == null) {
			bgTaskManager = new BackgroundTaskManager();
		}
		return bgTaskManager;
	}

	/**
	 * @param bgTaskManager the bgTaskManager to set
	 */
	public void setBgTaskManager(BackgroundTaskManager bgTaskManager) {
		this.bgTaskManager = bgTaskManager;
	}

	/**
	 * This method was overwritten in order to return the original set.
	 * @return the hiddenAnnotationURIs
	 */
	public Set<URI> getHiddenAnnotationURIs() {
		return hiddenAnnotationURIs;
	}

	/**
	 * @param hiddenAnnotationURIs the hiddenAnnotationURIs to set
	 */
	public void setHiddenAnnotationURIs(Set<URI> hiddenAnnotationURIs) {
		this.hiddenAnnotationURIs = hiddenAnnotationURIs;
		 AnnotationPreferences.setHiddenAnnotationURIs(hiddenAnnotationURIs);
         getOWLEditorKit().getModelManager().fireEvent(EventType.ACTIVE_ONTOLOGY_CHANGED);
	}

	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.model.OWLWorkspace#getOWLComponentFactory()
	 */
	public OWLComponentFactory getOWLComponentFactory() {
		return owlComponentFactory;
	}

	/**
	 * @param owlComponentFactory the owlComponentFactory to set
	 */
	public void setOWLComponentFactory(OWLComponentFactory owlComponentFactory) {
		this.owlComponentFactory = owlComponentFactory;
	}

	/**
	 * @return the owlModelManagerListener
	 */
	public OWLModelManagerListener getOWLModelManagerListener() {
		return owlModelManagerListener;
	}

	/**
	 * @param owlModelManagerListener the owlModelManagerListener to set
	 */
	public void setOWLModelManagerListener(
			OWLModelManagerListener owlModelManagerListener) {
		this.owlModelManagerListener = owlModelManagerListener;
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.model.OWLWorkspace#getCustomTabsManager()
	 */
	@Override
	protected CustomWorkspaceTabsManager getCustomTabsManager() {
		// TODO Auto-generated method stub
		return super.getCustomTabsManager();
	}

	/**
	 * @return the reselectionEventTypes
	 */
	public Set<EventType> getReselectionEventTypes() {
		return reselectionEventTypes;
	}

	/**
	 * @param reselectionEventTypes the reselectionEventTypes to set
	 */
	public void setReselectionEventTypes(Set<EventType> reselectionEventTypes) {
		this.reselectionEventTypes = reselectionEventTypes;
	}

	/**
	 * This listener is added to the {@link #getOWLModelManager()} in {@link #initialise()}
	 * @return the owlEntityCollectingOntologyChangeListener
	 */
	public OWLEntityCollectingOntologyChangeListener getOWLEntityCollectingOntologyChangeListener() {
		return owlEntityCollectingOntologyChangeListener;
	}

	/**
	 * This listener is added to the {@link #getOWLModelManager()} in {@link #initialise()}
	 * @param owlEntityCollectingOntologyChangeListener the owlEntityCollectingOntologyChangeListener to set
	 */
	public void setOWLEntityCollectingOntologyChangeListener(
			OWLEntityCollectingOntologyChangeListener owlEntityCollectingOntologyChangeListener) {
		this.owlEntityCollectingOntologyChangeListener = owlEntityCollectingOntologyChangeListener;
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.model.OWLWorkspace#initialiseExtraMenuItems(javax.swing.JMenuBar)
	 */
	protected void initialiseExtraMenuItems(JMenuBar menuBar) {
		if (menuBar != null) {
			super.initialiseExtraMenuItems(menuBar);
		}
	}

//	/**
//	 * This method was extended in order to return the original set (not a copy)
//	 * and it never returns null now.
//	 * @see org.protege.editor.core.ui.workspace.TabbedWorkspace#getWorkspaceTabs()
//	 */
//	public Set<WorkspaceTab> getWorkspaceTabs() {
//		if (workspaceTabs == null) {
//			this.setWorkspaceTabs(new HashSet<WorkspaceTab>());
//		}
//		return workspaceTabs;
//	}
//
//	/**
//	 * @param workspaceTabs the workspaceTabs to set
//	 */
//	public void setWorkspaceTabs(Set<WorkspaceTab> workspaceTabs) {
//		this.workspaceTabs = workspaceTabs;
//	}


}
