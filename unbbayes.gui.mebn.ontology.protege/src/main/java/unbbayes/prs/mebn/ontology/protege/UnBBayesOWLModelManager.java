///**
// * 
// */
//package unbbayes.prs.mebn.ontology.protege;
//
//
//
//import java.io.File;
//import java.io.IOException;
//import java.net.ProtocolException;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.coode.xml.XMLWriterPreferences;
//import org.protege.editor.core.AbstractModelManager;
//import org.protege.editor.core.ProtegeApplication;
//import org.protege.editor.core.ui.error.ErrorLogPanel;
//import org.protege.editor.owl.model.MissingImportHandler;
//import org.protege.editor.owl.model.MissingImportHandlerImpl;
//import org.protege.editor.owl.model.OWLModelManager;
//import org.protege.editor.owl.model.SaveErrorHandler;
//import org.protege.editor.owl.model.XMLWriterPrefs;
//import org.protege.editor.owl.model.cache.OWLEntityRenderingCache;
//import org.protege.editor.owl.model.cache.OWLEntityRenderingCacheImpl;
//import org.protege.editor.owl.model.cache.OWLObjectRenderingCache;
//import org.protege.editor.owl.model.classexpression.anonymouscls.AnonymousDefinedClassManager;
//import org.protege.editor.owl.model.entity.CustomOWLEntityFactory;
//import org.protege.editor.owl.model.entity.OWLEntityFactory;
//import org.protege.editor.owl.model.event.EventType;
//import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
//import org.protege.editor.owl.model.event.OWLModelManagerListener;
//import org.protege.editor.owl.model.find.OWLEntityFinder;
//import org.protege.editor.owl.model.hierarchy.OWLHierarchyManager;
//import org.protege.editor.owl.model.hierarchy.OWLHierarchyManagerImpl;
//import org.protege.editor.owl.model.history.HistoryManager;
//import org.protege.editor.owl.model.history.HistoryManagerImpl;
//import org.protege.editor.owl.model.inference.OWLReasonerManager;
//import org.protege.editor.owl.model.inference.OWLReasonerManagerImpl;
//import org.protege.editor.owl.model.inference.ReasonerPreferences;
//import org.protege.editor.owl.model.io.AutoMappedRepositoryIRIMapper;
//import org.protege.editor.owl.model.io.IOListener;
//import org.protege.editor.owl.model.io.IOListenerEvent;
//import org.protege.editor.owl.model.io.OntologySourcesManager;
//import org.protege.editor.owl.model.io.UserResolvedIRIMapper;
//import org.protege.editor.owl.model.io.WebConnectionIRIMapper;
//import org.protege.editor.owl.model.library.OntologyCatalogManager;
//import org.protege.editor.owl.model.selection.ontologies.ActiveOntologySelectionStrategy;
//import org.protege.editor.owl.model.selection.ontologies.AllLoadedOntologiesSelectionStrategy;
//import org.protege.editor.owl.model.selection.ontologies.ImportsClosureOntologySelectionStrategy;
//import org.protege.editor.owl.model.selection.ontologies.OntologySelectionStrategy;
//import org.protege.editor.owl.model.util.ListenerManager;
//import org.protege.editor.owl.ui.OWLObjectComparator;
//import org.protege.editor.owl.ui.OWLObjectRenderingComparator;
//import org.protege.editor.owl.ui.clsdescriptioneditor.ManchesterOWLExpressionCheckerFactory;
//import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionCheckerFactory;
//import org.protege.editor.owl.ui.error.OntologyLoadErrorHandler;
//import org.protege.editor.owl.ui.explanation.ExplanationManager;
//import org.protege.editor.owl.ui.renderer.OWLEntityRenderer;
//import org.protege.editor.owl.ui.renderer.OWLEntityRendererImpl;
//import org.protege.editor.owl.ui.renderer.OWLEntityRendererListener;
//import org.protege.editor.owl.ui.renderer.OWLModelManagerEntityRenderer;
//import org.protege.editor.owl.ui.renderer.OWLObjectRenderer;
//import org.protege.editor.owl.ui.renderer.OWLObjectRendererImpl;
//import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;
//import org.protege.owlapi.apibinding.ProtegeOWLManager;
//import org.protege.owlapi.model.ProtegeOWLOntologyManager;
//import org.protege.xmlcatalog.XMLCatalog;
//import org.semanticweb.owlapi.model.IRI;
//import org.semanticweb.owlapi.model.OWLClass;
//import org.semanticweb.owlapi.model.OWLDataFactory;
//import org.semanticweb.owlapi.model.OWLEntity;
//import org.semanticweb.owlapi.model.OWLObject;
//import org.semanticweb.owlapi.model.OWLOntology;
//import org.semanticweb.owlapi.model.OWLOntologyChange;
//import org.semanticweb.owlapi.model.OWLOntologyChangeException;
//import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
//import org.semanticweb.owlapi.model.OWLOntologyCreationException;
//import org.semanticweb.owlapi.model.OWLOntologyID;
//import org.semanticweb.owlapi.model.OWLOntologyLoaderListener;
//import org.semanticweb.owlapi.model.OWLOntologyManager;
//import org.semanticweb.owlapi.model.OWLOntologyStorageException;
//import org.semanticweb.owlapi.model.OWLRuntimeException;
//import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.util.SimpleIRIMapper;
//
//import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;
//import unbbayes.util.Debug;
//
///**
// * This class rewrites OWLModelManagerImpl in order to avoid some bugs happening
// * when loading protege classes without adhering to OSGI framework.
// * We could not just use protege classes "as is" because they were written 
// * in a way that using them as a library (without using OSGI) was impractical...
// * It also does some refactories in order for subclasses to extend functionalities
// * with ease.
// * @author Shou Matsumoto
// */
//public class UnBBayesOWLModelManager extends AbstractModelManager implements OWLModelManager, OWLEntityRendererListener, OWLOntologyChangeListener, OWLOntologyLoaderListener {
//
//	private HistoryManager historyManager;
//
//    private OWLModelManagerEntityRenderer entityRenderer;
//
//    private OWLObjectRenderer objectRenderer;
//
//    private OWLOntology activeOntology;
//
//    private OWLEntityRenderingCache owlEntityRenderingCache;
//
//    /**
//     * P4 repeatedly asks for the same rendering multiple times in a row
//     * because of the components listening to mouse events etc so cache a
//     * small number of objects we have just rendered
//     */
//    private OWLObjectRenderingCache owlObjectRenderingCache;
//
//    private OWLEntityFinder entityFinder;
//
//    private OWLReasonerManager owlReasonerManager;
//
//    /**
//     * Dirty ontologies are ontologies that have been edited
//     * and not saved.
//     */
//    private Set<OWLOntology> dirtyOntologies;
//
//    /**
//     * The <code>OWLConnection</code> that we use to manage
//     * ontologies.
//     */
//    private OWLOntologyManager manager;
//
//    private OntologyCatalogManager ontologyLibraryManager;
//    
//    private ExplanationManager explanationManager;
//
//    private OWLEntityFactory entityFactory;
//
//    /**
//     * A cache for the imports closure.  Originally, we just requested this
//     * each time from the OWLOntologyManager, but this proved to be expensive
//     * in terms of time.
//     */
//    private Set<OWLOntology> activeOntologies;
//
//    private Set<OntologySelectionStrategy> ontSelectionStrategies;
//
//    private OntologySelectionStrategy activeOntologiesStrategy;
//
//    private OWLExpressionCheckerFactory owlExpressionCheckerFactory;
//
//
//    // error handlers
//
//    private SaveErrorHandler saveErrorHandler;
//
//    private OntologyLoadErrorHandler loadErrorHandler;
//
//    private AutoMappedRepositoryIRIMapper autoMappedRepositoryIRIMapper;
//
//    private UserResolvedIRIMapper userResolvedIRIMapper;
//
//
//    // listeners
//
//    private List<OWLModelManagerListener> modelManagerChangeListeners;
//
//    private ListenerManager<OWLModelManagerListener> modelManagerListenerManager;
//
//    private ListenerManager<OWLOntologyChangeListener> changeListenerManager;
//
//    private List<IOListener> ioListeners;
//
//    /**
//     * The default constructor is not private in order to allow inheritance.
//     * @deprecated use {@link #newInstance()} instead.
//     */
//    protected UnBBayesOWLModelManager() {
//    	super();
//    }
//    
//    /**
//     * Construction method initializing field using default values (values inherited from protege4.1 API).
//     * @return a new instance.
//     * @deprecated use {@link #newInstance(OWLOntologyManager)} instead, because the Protege4.1 API may not work in non-OSGI environment.
//     */
//    public static OWLModelManager newInstance() {
//    	return UnBBayesOWLModelManager.newInstance(ProtegeOWLManager.createOWLOntologyManager());
//    }
//    
//    /**
//     * This is the default construction method using fields.
//     * @param ontologyManager : the {@link OWLOntologyManager} (from OWL API) related to this {@link UnBBayesOWLModelManager}.
//     * This class will use this ontologyManager in order to actually manipulate ontologies using OWL API.
//     * @return a new instance of the model manager
//     */
//    public static OWLModelManager newInstance(OWLOntologyManager ontologyManager) {
//    	UnBBayesOWLModelManager ret = new UnBBayesOWLModelManager();
//
//    	ret.setModelManagerListenerManager(new ListenerManager<OWLModelManagerListener>());
//    	ret.setChangeListenerManager(new ListenerManager<OWLOntologyChangeListener>());
//    	ret.setManager(ontologyManager);
//        if (ret.getManager() instanceof ProtegeOWLOntologyManager) {
//        	((ProtegeOWLOntologyManager)ret.getManager()).setUseWriteSafety(true);
//        	((ProtegeOWLOntologyManager)ret.getManager()).setUseSwingThread(true);
//        }
//        ret.getManager().setSilentMissingImportsHandling(true);
//        ret.getManager().addOntologyChangeListener(ret);
//        ret.getManager().addOntologyLoaderListener(ret);
//
//
//        // URI mappers for loading - added in reverse order
//        ret.setAutoMappedRepositoryIRIMapper(new AutoMappedRepositoryIRIMapper(ret));
//        ret.setUserResolvedIRIMapper(new UserResolvedIRIMapper(new MissingImportHandlerImpl()));
//        ret.getManager().clearIRIMappers();
//        ret.getManager().addIRIMapper(ret.getUserResolvedIRIMapper());
//        ret.getManager().addIRIMapper(new WebConnectionIRIMapper());
//        ret.getManager().addIRIMapper(ret.getAutoMappedRepositoryIRIMapper());
//
//
//        ret.setDirtyOntologies(new HashSet<OWLOntology>());
//        ret.setOntSelectionStrategies(new HashSet<OntologySelectionStrategy>());
//
//
//        ret.setModelManagerChangeListeners(new ArrayList<OWLModelManagerListener>());
//        ret.setIOListeners(new ArrayList<IOListener>());
//
//
//        ret.setObjectRenderer(new OWLObjectRendererImpl(ret));
//        ret.setOwlEntityRenderingCache(new OWLEntityRenderingCacheImpl());
//        ret.getOwlEntityRenderingCache().setOWLModelManager(ret);
//        ret.setOwlObjectRenderingCache(new OWLObjectRenderingCache(ret));
//
//        ret.setOwlExpressionCheckerFactory(new ManchesterOWLExpressionCheckerFactory(ret));
//
//        ret.setActiveOntologies(new HashSet<OWLOntology>());
//
//        // force the renderer to be created
//        // to prevent double cache rebuild once ontologies loaded
//        ret.getOWLEntityRenderer();
//
//        ret.registerOntologySelectionStrategy(new ActiveOntologySelectionStrategy(ret));
//        ret.registerOntologySelectionStrategy(new AllLoadedOntologiesSelectionStrategy(ret));
//        
//        ret.setActiveOntologiesStrategy(new ImportsClosureOntologySelectionStrategy(ret));
//        ret.registerOntologySelectionStrategy(ret.getActiveOntologiesStrategy());
//
//        XMLWriterPreferences.getInstance().setUseNamespaceEntities(XMLWriterPrefs.getInstance().isUseEntities());
//
////        put(AnonymousDefinedClassManager.ID, new AnonymousDefinedClassManager(this));
//
//        ret.put(OntologySourcesManager.ID, new OntologySourcesManager(ret));
//        
//        return ret;
//    }
//
//
//    public void dispose() {
//        super.dispose();
//
//        OntologySourcesManager sourcesMngr = get(OntologySourcesManager.ID);
//        removeIOListener(sourcesMngr);
//
//        try {
//            // Empty caches
//            owlEntityRenderingCache.dispose();
//            owlObjectRenderingCache.dispose();
//
//            if (entityRenderer != null){
//                entityRenderer.dispose();
//            }
//
//            owlReasonerManager.dispose();
//        }
//        catch (Exception e) {
//        	Debug.println(this.getClass(), "Failed to dispose caches and managers." , e);
//        }
//
//        // Name and shame plugins that do not (or can't be bothered to) clean up
//        // their listeners!
//        Debug.println(this.getClass(), "(Listeners should be removed in the plugin dispose method!)");
//    }
//
//
//    public boolean isDirty() {
//        return !dirtyOntologies.isEmpty();
//    }
//
//
//    public OWLOntologyManager getOWLOntologyManager() {
//        return manager;
//    }
//
//
//    /**
//     * This method is 
//     */
//    public OntologyCatalogManager getOntologyCatalogManager() {
//        if (ontologyLibraryManager == null) {
//        	// new OntologyCatalogManager(List) does not initialize plug-ins
//            ontologyLibraryManager = new OntologyCatalogManager(new ArrayList());
//        }
//        return ontologyLibraryManager;
//    }
//
//
//    public OWLHierarchyManager getOWLHierarchyManager() {
//        OWLHierarchyManager hm = get(OWLHierarchyManager.ID);
//        if (hm == null){
//            hm = new OWLHierarchyManagerImpl(this);
//            put(OWLHierarchyManager.ID, hm);
//        }
//        return hm;
//    }
//    
//    public ExplanationManager getExplanationManager() {
//    	return explanationManager;
//    }
//    
//    public void setExplanationManager(ExplanationManager explanationManager) {
//		this.explanationManager = explanationManager;
//	}
//
//    ///////////////////////////////////////////////////////////////////////////////////////
//    //
//    // Loading
//    //
//    ///////////////////////////////////////////////////////////////////////////////////////
//
//
//    /**
//     * A convenience method that loads an ontology from a file
//     * The location of the file is specified by the URI argument.
//     */
//    public boolean loadOntologyFromPhysicalURI(URI uri) {
//        if (uri.getScheme()  != null && uri.getScheme().equals("file")) {
//            // Load the URIs of other ontologies that are contained in the same folder.
//            addRootFolder(new File(uri).getParentFile());
//        }
//        OWLOntology ontology = null;
//        try {
//            ontology = manager.loadOntologyFromOntologyDocument(IRI.create(uri));
//            setActiveOntology(ontology);
//            fireEvent(EventType.ONTOLOGY_LOADED);
//            manager.addIRIMapper(new SimpleIRIMapper(ontology.getOntologyID().getDefaultDocumentIRI(), IRI.create(uri)));
//        }
//        catch (OWLOntologyCreationException ooce) {
//            ;             // will be handled by the loadErrorHandler, so ignore
//        }
//        return ontology != null;
//    }
//
//
//    public void startedLoadingOntology(LoadingStartedEvent event) {
//        try {
//        	Debug.println(this.getClass(), "loading " + event.getOntologyID() + " from " + event.getDocumentIRI());
//		} catch (Exception e) {
//			try {
//				Debug.println(this.getClass(), "Could not log event " + event + ". Check LoadingStartedEvent#getOntlogyID() and LoadingStartedEvent#getDocumentIRI().", e);
//			} catch (Exception t) {
//				Debug.println(this.getClass(), "Could not log event. Check LoadingStartedEvent#toString().",t);
//			}
//		}
//        fireBeforeLoadEvent(event.getOntologyID(), event.getDocumentIRI().toURI());
//    }
//
//
//    public void finishedLoadingOntology(LoadingFinishedEvent event) {
//        if (!event.isSuccessful()){
//            Exception e = event.getException();
//            if (loadErrorHandler != null){
//                try {
//                    loadErrorHandler.handleErrorLoadingOntology(event.getOntologyID(),
//                                                                event.getDocumentIRI().toURI(),
//                                                                e);
//                }
//                catch (Throwable e1) {
//                    // if, for any reason, the loadErrorHandler cannot report the error
//                    ErrorLogPanel.showErrorDialog(e1);
//                }
//            }
//        }
//        fireAfterLoadEvent(event.getOntologyID(), event.getDocumentIRI().toURI());
//    }
//
//    public XMLCatalog addRootFolder(File dir) {
//    	return ontologyLibraryManager.addFolder(dir);
//    }
//
//    private void fireBeforeLoadEvent(OWLOntologyID ontologyID, URI physicalURI) {
//        for(IOListener listener : new ArrayList<IOListener>(ioListeners)) {
//            try {
//                listener.beforeLoad(new IOListenerEvent(ontologyID, physicalURI));
//            }
//            catch (Throwable e) {
//                ProtegeApplication.getErrorLog().logError(e);
//            }
//        }
//    }
//
//
//    private void fireAfterLoadEvent(OWLOntologyID ontologyID, URI physicalURI) {
//        for(IOListener listener : new ArrayList<IOListener>(ioListeners)) {
//            try {
//                listener.afterLoad(new IOListenerEvent(ontologyID, physicalURI));
//            }
//            catch (Throwable e) {
//                ProtegeApplication.getErrorLog().logError(e);
//            }
//        }
//    }
//
//
//    ////////////////////////////////////////////////////////////////////////////////////////
//    //
//    //  Ontology URI to Physical URI mapping
//    //
//    ////////////////////////////////////////////////////////////////////////////////////////
//
//
//    public URI getOntologyPhysicalURI(OWLOntology ontology) {
//        return manager.getOntologyDocumentIRI(ontology).toURI();
//    }
//
//
//    public void setPhysicalURI(OWLOntology ontology, URI physicalURI) {
//        manager.setOntologyDocumentIRI(ontology, IRI.create(physicalURI));
//    }
//
//
//    public OWLOntology createNewOntology(OWLOntologyID ontologyID, URI physicalURI) throws OWLOntologyCreationException {
//        manager.addIRIMapper(new SimpleIRIMapper(ontologyID.getDefaultDocumentIRI(), IRI.create(physicalURI)));
//        OWLOntology ont = manager.createOntology(ontologyID);
//        dirtyOntologies.add(ont);
//        setActiveOntology(ont);
//        if (physicalURI != null) {
//        	try {
//        		File containingDirectory = new File(physicalURI).getParentFile();
//        		if (containingDirectory.exists()) {
//        			getOntologyCatalogManager().addFolder(containingDirectory);
//        		}
//        	}
//        	catch (IllegalArgumentException iae) {
//        		try {
//        			Debug.println(this.getClass(), "Cannot generate ontology catalog for ontology at " + physicalURI);
//        		} catch (Throwable t) {
//					t.printStackTrace();
//				}
//        		iae.printStackTrace();
//        	}
//        }
//        fireEvent(EventType.ONTOLOGY_CREATED);
//        return ont;
//    }
//
//
//    public OWLOntology reload(OWLOntology ont) throws OWLOntologyCreationException {
//        IRI ontologyDocumentIRI = IRI.create(getOntologyPhysicalURI(ont));
//        manager.removeOntology(ont);
//        try {
//            ont = manager.loadOntologyFromOntologyDocument(ontologyDocumentIRI);
//        }
//        catch (Throwable t) {
//            ((OWLOntologyManagerImpl) manager).ontologyCreated(ont);  // put it back - a hack but it works
//            manager.setOntologyDocumentIRI(ont, ontologyDocumentIRI);
//            throw (t instanceof OWLOntologyCreationException) ? (OWLOntologyCreationException) t : new OWLOntologyCreationException(t);
//        }
//        rebuildActiveOntologiesCache();
//        setOWLEntityRenderer(getOWLEntityRenderer());
//        fireEvent(EventType.ONTOLOGY_RELOADED);
//        return ont;
//    }
//
//
//    public boolean removeOntology(OWLOntology ont) {
//        if (manager.contains(ont.getOntologyID()) && manager.getOntologies().size() > 1){
//
//            boolean resetActiveOntologyRequired = ont.equals(activeOntology);
//            activeOntologies.remove(ont);
//            dirtyOntologies.remove(ont);
//            manager.removeOntology(ont);
//
//            if (resetActiveOntologyRequired){
//                OWLOntology newActiveOnt = null;
//                if (!activeOntologies.isEmpty()){
//                    newActiveOnt = activeOntologies.iterator().next();
//                }
//                if (newActiveOnt == null && !manager.getOntologies().isEmpty()){
//                    newActiveOnt = manager.getOntologies().iterator().next();
//                }
//
//                setActiveOntology(newActiveOnt, true);
//            }
//            else{
//                setActiveOntology(activeOntology, true);
//            }
//            return true;
//        }
//        return false;
//    }
//
//    ////////////////////////////////////////////////////////////////////////////////////////
//    //
//    // Saving
//    //
//    ///////////////////////////////////////////////////////////////////////////////////////
//
//
//    public void save() throws OWLOntologyStorageException {
//        // Save all of the ontologies that are editable and that
//        // have been modified.
//        for (OWLOntology ont : new HashSet<OWLOntology>(dirtyOntologies)) {
//            save(ont);
//        }
//    }
//
//
//    public void save(OWLOntology ont) throws OWLOntologyStorageException {
//        final URI physicalURI = manager.getOntologyDocumentIRI(ont).toURI();
//
//        try{
//            fireBeforeSaveEvent(ont.getOntologyID(), physicalURI);
//
//            try {
//                if (!physicalURI.getScheme().equals("file")){
//                    throw new ProtocolException("Cannot save file to remote location: " + physicalURI);
//                }
//
//                // the OWLAPI v3 saves to a temp file now
//                manager.saveOntology(ont, manager.getOntologyFormat(ont), IRI.create(physicalURI));
//
//                manager.setOntologyDocumentIRI(ont, IRI.create(physicalURI));
//            }
//            catch (IOException e) {
//                throw new OWLOntologyStorageException("Error while saving ontology " + ont.getOntologyID() + " to " + physicalURI, e);
//            }
//            try {
//            	Debug.println(this.getClass(), "Saved " + getRendering(ont) + " to " + physicalURI);
//            } catch (Throwable e) {
//				Debug.println(this.getClass(), "Log failed", e);
//			}
//
//            dirtyOntologies.remove(ont);
//
//            fireEvent(EventType.ONTOLOGY_SAVED);
//            fireAfterSaveEvent(ont.getOntologyID(), physicalURI);
//        }
//        catch(OWLOntologyStorageException e){
//            if (saveErrorHandler != null){
//                try {
//                    saveErrorHandler.handleErrorSavingOntology(ont, physicalURI, e);
//                }
//                catch (Exception e1) {
//                    throw new OWLOntologyStorageException(e1);
//                }
//            }
//            else{
//                throw e;
//            }
//        }
//    }
//    
//    /**
////     * @deprecated - this method would require user interaction - use <code>OWLEditorKit.saveAs()</code> instead
//     * @throws OWLOntologyStorageException if a problem occurs during the save
//     */
//    @Deprecated
//    public void saveAs() throws OWLOntologyStorageException {
//        save();
//    }
//
//
//    private void fireBeforeSaveEvent(OWLOntologyID ontologyID, URI physicalURI) {
//        for(IOListener listener : new ArrayList<IOListener>(ioListeners)) {
//            try {
//                listener.beforeSave(new IOListenerEvent(ontologyID, physicalURI));
//            }
//            catch (Throwable e) {
//                ProtegeApplication.getErrorLog().logError(e);
//            }
//        }
//    }
//
//
//    private void fireAfterSaveEvent(OWLOntologyID ontologyID, URI physicalURI) {
//        for(IOListener listener : new ArrayList<IOListener>(ioListeners)) {
//            try {
//                listener.afterSave(new IOListenerEvent(ontologyID, physicalURI));
//            }
//            catch (Throwable e) {
//                ProtegeApplication.getErrorLog().logError(e);
//            }
//        }
//    }
//
//
//    ////////////////////////////////////////////////////////////////////////////////////////
//    //
//    // Ontology Management
//    //
//    ///////////////////////////////////////////////////////////////////////////////////////
//
//
//    public Set<OWLOntology> getOntologies() {
//        return manager.getOntologies();
//    }
//
//
//    public Set<OWLOntology> getDirtyOntologies() {
//        return new HashSet<OWLOntology>(dirtyOntologies);
//    }
//
//
//    /**
//     * Forces the system to believe that an ontology
//     * has been modified.
//     * @param ontology The ontology to be made dirty.
//     */
//    public void setDirty(OWLOntology ontology) {
//        dirtyOntologies.add(ontology);
//    }
//
//
//    public OWLOntology getActiveOntology() {
//        return activeOntology;
//    }
//
//
//    public OWLDataFactory getOWLDataFactory() {
//        return manager.getOWLDataFactory();
//    }
//
//
//    public Set<OWLOntology> getActiveOntologies() {
//        return activeOntologies;
//    }
//
//
//    public boolean isActiveOntologyMutable() {
//        return isMutable(getActiveOntology());
//    }
//
//
//    public boolean isMutable(OWLOntology ontology) {
//        // Assume all ontologies are editable - even ones
//        // that have been loaded from non-editable locations e.g.
//        // the web.  The reason for this is that feedback from users
//        // has indicated that it is a pain when an ontology isn't editable
//        // just because it has been downloaded from a web because
//        // they can't experiment with adding or removing axioms.
//        return true;
//    }
//
//
//    /**
//     * This method update the active ontology.
//     * If the active ontology has an {@link OWLOntologyManager} different than expected ({@link #getManager()}),
//     * it calls {@link #setManager(OWLOntologyManager)} in order to start using the same {@link OWLOntologyManager}.
//     */
//    public void setActiveOntology(OWLOntology activeOntology) {
//    	if (activeOntology != null 
//    			&& activeOntology.getOWLOntologyManager() != null
//    			&& !activeOntology.getOWLOntologyManager().equals(this.getManager())) {
//    		// update the ontology manager (OWL API) if they differ
//    		this.setManager(activeOntology.getOWLOntologyManager());
//    	}
//        setActiveOntology(activeOntology, false);
//    }
//
//
//    public void setActiveOntologiesStrategy(OntologySelectionStrategy strategy) {
//        activeOntologiesStrategy = strategy;
//        setActiveOntology(getActiveOntology(), true);
//        fireEvent(EventType.ONTOLOGY_VISIBILITY_CHANGED);
//    }
//
//
//    public OntologySelectionStrategy getActiveOntologiesStrategy() {
//        return activeOntologiesStrategy;
//    }
//
//
//    public Set<OntologySelectionStrategy> getActiveOntologiesStrategies() {
//        return ontSelectionStrategies;
//    }
//
//
//    /**
//     * Sets the active ontology (and hence the set of active ontologies).
//     * @param activeOntology The ontology to be set as the active ontology.
//     * @param force          By default, if the specified ontology is already the
//     *                       active ontology then no changes will take place.  This flag can be
//     *                       used to force the active ontology to be reset and listeners notified
//     *                       of a change in the state of the active ontology.
//     */
//    private void setActiveOntology(OWLOntology activeOntology, boolean force) {
//        if (!force) {
//            if (this.activeOntology != null) {
//                if (this.activeOntology.equals(activeOntology)) {
//                    return;
//                }
//            }
//        }
//        this.activeOntology = activeOntology;
//        if (this.activeOntology != null) {
//        	try {
//        		Debug.println(this.getClass(), "Setting active ontology to " + activeOntology.getOntologyID());
//        	} catch (Throwable e) {
//        		e.printStackTrace();
//        	}
//        	rebuildActiveOntologiesCache();
//        	// Rebuild entity indices
//        	entityRenderer.ontologiesChanged();
//        	rebuildEntityIndices();
//        	// Inform our listeners
//        	fireEvent(EventType.ACTIVE_ONTOLOGY_CHANGED);
//        	Debug.println(this.getClass(), "... active ontology changed");
//        }
//    }
//
//
//    private void registerOntologySelectionStrategy(OntologySelectionStrategy strategy) {
//        ontSelectionStrategies.add(strategy);
//    }
//
//
//    private void rebuildActiveOntologiesCache() {
//        activeOntologies.clear();
//        activeOntologies.addAll(activeOntologiesStrategy.getOntologies());
//    }
//
//
//    ///////////////////////////////////////////////////////////////////////////////////////////////////
//    //
//    //  Ontology history management
//    //
//    ///////////////////////////////////////////////////////////////////////////////////////////////////
//
//
//    public void applyChange(OWLOntologyChange change) {
//        try {
//            AnonymousDefinedClassManager adcManager = get(AnonymousDefinedClassManager.ID);
//            if (adcManager != null){
//                change = adcManager.getChangeRewriter().rewriteChange(change);
//            }
//            manager.applyChange(change);
//        }
//        catch (OWLOntologyChangeException e) {
//            throw new OWLRuntimeException(e);
//        }
//    }
//
//
//    public void applyChanges(List<? extends OWLOntologyChange> changes) {
//        try {
//            AnonymousDefinedClassManager adcManager = get(AnonymousDefinedClassManager.ID);
//            if (adcManager != null){
//                changes = adcManager.getChangeRewriter().rewriteChanges(changes);
//            }
//            manager.applyChanges(changes);
//        }
//        catch (OWLOntologyChangeException e) {
//            throw new OWLRuntimeException(e);
//        }
//    }
//
//
//    public void ontologiesChanged(List<? extends OWLOntologyChange> changes) {
//        getHistoryManager().logChanges(changes);
//        boolean refreshActiveOntology = false;
//        for (OWLOntologyChange change : changes) {
//            dirtyOntologies.add(change.getOntology());
//            if (change.isImportChange()){
//                refreshActiveOntology = true;
//            }
//        }
//        if (refreshActiveOntology){
//            setActiveOntology(getActiveOntology(), true);
//        }
//    }
//
//
//    public boolean isChangedEntity(OWLEntity entity) {
//        return false;
//    }
//
//
//    public HistoryManager getHistoryManager() {
//        if (historyManager == null) {
//            historyManager = new HistoryManagerImpl(this);
//        }
//        return historyManager;
//    }
//
//
//    public void addOntologyChangeListener(OWLOntologyChangeListener listener) {
//        manager.addOntologyChangeListener(listener);
//        changeListenerManager.recordListenerAdded(listener);
//    }
//
//
//    public void removeOntologyChangeListener(OWLOntologyChangeListener listener) {
//        manager.removeOntologyChangeListener(listener);
//        changeListenerManager.recordListenerRemoved(listener);
//    }
//
//    ///////////////////////////////////////////////////////////////////////////////////////////////////
//
//
//    public void addListener(OWLModelManagerListener listener) {
//        modelManagerChangeListeners.add(listener);
//        modelManagerListenerManager.recordListenerAdded(listener);
//    }
//
//
//    public void removeListener(OWLModelManagerListener listener) {
//        modelManagerChangeListeners.remove(listener);
//        modelManagerListenerManager.recordListenerRemoved(listener);
//    }
//
//
//    public void fireEvent(EventType type) {
//        OWLModelManagerChangeEvent event = new OWLModelManagerChangeEvent(this, type);
//        for (OWLModelManagerListener listener : new ArrayList<OWLModelManagerListener>(modelManagerChangeListeners)) {
//            try {
//                listener.handleChange(event);
//            }
//            catch (Throwable e) {
//                try {
//                	Debug.println(this.getClass(), "Exception thrown by listener: " + listener.getClass().getName() + ".  Detatching bad listener!");
//                } catch (Throwable t) {
//					t.printStackTrace();
//				}
//                ProtegeApplication.getErrorLog().logError(e);
//                modelManagerChangeListeners.remove(listener);
//            }
//        }
//    }
//
//    public void addIOListener(IOListener listener) {
//        ioListeners.add(listener);
//    }
//
//
//    public void removeIOListener(IOListener listener) {
//        ioListeners.remove(listener);
//    }
//
//
//    //////////////////////////////////////////////////////////////////////////////////////
//    //
//    //  Entity rendering classes
//    //
//    //////////////////////////////////////////////////////////////////////////////////////
//
//
//    public OWLModelManagerEntityRenderer getOWLEntityRenderer() {
//        if (entityRenderer == null) {
//            try {
//                String clsName = OWLRendererPreferences.getInstance().getRendererClass();
//                Class c = Class.forName(clsName);
//                OWLModelManagerEntityRenderer ren = (OWLModelManagerEntityRenderer) c.newInstance();
//                // Force an update by using the setter method.
//                setOWLEntityRenderer(ren);
//            } catch (Throwable e) {
//                e.printStackTrace();
//            } 
//            if (entityRenderer == null) {
//                setOWLEntityRenderer(new OWLEntityRendererImpl());
//            }
//        }
//        return entityRenderer;
//    }
//
//
//    public String getRendering(OWLObject object) {
//        // Look for a cached version of the rendering first!
//        if (object instanceof OWLEntity) {
//            AnonymousDefinedClassManager adcManager = get(AnonymousDefinedClassManager.ID);
//            if (adcManager != null &&
//                object instanceof OWLClass &&
//                adcManager.isAnonymous((OWLClass)object)){
//                return owlObjectRenderingCache.getRendering(adcManager.getExpression((OWLClass)object), getOWLObjectRenderer());
//            }
//            else{
//                getOWLEntityRenderer();
//                String rendering = owlEntityRenderingCache.getRendering((OWLEntity) object);
//                if(rendering != null) {
//                    return rendering;
//                }
//                else {
//                    return getOWLEntityRenderer().render((OWLEntity) object);
//                }
//            }
//        }
//
//        return owlObjectRenderingCache.getRendering(object, getOWLObjectRenderer());
//    }
//
//
//    public void renderingChanged(OWLEntity entity, final OWLEntityRenderer renderer) {
//        owlEntityRenderingCache.updateRendering(entity);
//        owlObjectRenderingCache.clear();
//        // We should inform listeners
//        for (OWLModelManagerListener listener : new ArrayList<OWLModelManagerListener>(modelManagerChangeListeners)) {
//            listener.handleChange(new OWLModelManagerChangeEvent(this, EventType.ENTITY_RENDERING_CHANGED));
//        }
//    }
//
//
//    public void setOWLEntityRenderer(OWLModelManagerEntityRenderer renderer) {
//        if (entityRenderer != null) {
//            entityRenderer.removeListener(this);
//            try {
//                entityRenderer.dispose();
//            }
//            catch (Exception e) {
//                ProtegeApplication.getErrorLog().logError(e);
//            }
//        }
//        entityRenderer = renderer;
//        entityRenderer.addListener(this);
//        entityRenderer.setup(this);
//        entityRenderer.initialise();
//        rebuildEntityIndices();
//        fireEvent(EventType.ENTITY_RENDERER_CHANGED);
//    }
//
//
//    public OWLObjectRenderer getOWLObjectRenderer() {
//        return objectRenderer;
//    }
//
//
//    public OWLExpressionCheckerFactory getOWLExpressionCheckerFactory() {
//        return owlExpressionCheckerFactory;
//    }
//
//
//
//
//    public OWLEntityFactory getOWLEntityFactory() {
//        if (entityFactory == null){
//            entityFactory = new CustomOWLEntityFactory(this);
//        }
//        return entityFactory;
//    }
//
//
//    public void setOWLEntityFactory(OWLEntityFactory owlEntityFactory) {
//        this.entityFactory = owlEntityFactory;
//    }
//
//
//    public OWLEntityFinder getOWLEntityFinder() {
//        if (entityFinder == null){
//            entityFinder = UnBBayesOWLEntityFinder.newInstance(this, owlEntityRenderingCache);
//        }
//        return entityFinder;
//    }
//
//
//    public Comparator<OWLObject> getOWLObjectComparator(){
//        OWLObjectComparator<OWLObject> comparator = get(OWL_OBJECT_COMPARATOR_KEY);
//        if (comparator == null){
//            comparator = new OWLObjectRenderingComparator<OWLObject>(this);
//            put(OWL_OBJECT_COMPARATOR_KEY, comparator);
//        }
//        return comparator;
//    }
//
//
//    private void rebuildEntityIndices() {
//        Debug.println("Rebuilding entity indices...");
//        long t0 = System.currentTimeMillis();
//        owlEntityRenderingCache.rebuild();
//        owlObjectRenderingCache.clear();
//        try {
//        	Debug.println("... rebuilt in " + (System.currentTimeMillis() - t0) + " ms");
//        } catch (Throwable e) {
//			e.printStackTrace();
//		}
//    }
//
//    //////////////////////////////////////////////////////////////////////////////////////
//    //
//    //  Reasoner
//    //
//    //////////////////////////////////////////////////////////////////////////////////////
//
//
//    public OWLReasonerManager getOWLReasonerManager() {
////        if (owlReasonerManager == null) {
////            owlReasonerManager = new OWLReasonerManagerImpl(this);
////        }
//        return owlReasonerManager;
//    }
//
//
//    public OWLReasoner getReasoner() {
//        return getOWLReasonerManager().getCurrentReasoner();
//    }
//    
//    public ReasonerPreferences getReasonerPreferences() {
//        return getOWLReasonerManager().getReasonerPreferences();
//    }
//
//
//    //////////////////////////////////////////////////////////////////////////////////////
//    //
//    //  Error handling
//    //
//    //////////////////////////////////////////////////////////////////////////////////////
//
//
//    public void setMissingImportHandler(MissingImportHandler missingImportHandler) {
//        userResolvedIRIMapper.setMissingImportHandler(missingImportHandler);
//    }
//
//
//    public void setSaveErrorHandler(SaveErrorHandler handler) {
//        this.saveErrorHandler = handler;
//    }
//
//
//    public void setLoadErrorHandler(OntologyLoadErrorHandler handler) {
//        this.loadErrorHandler = handler;
//    }
//
//	/**
//	 * @return the entityRenderer
//	 */
//	public OWLModelManagerEntityRenderer getEntityRenderer() {
//		return entityRenderer;
//	}
//
//	/**
//	 * @param entityRenderer the entityRenderer to set
//	 */
//	public void setEntityRenderer(OWLModelManagerEntityRenderer entityRenderer) {
//		this.entityRenderer = entityRenderer;
//	}
//
//	/**
//	 * @return the objectRenderer
//	 */
//	public OWLObjectRenderer getObjectRenderer() {
//		return objectRenderer;
//	}
//
//	/**
//	 * @param objectRenderer the objectRenderer to set
//	 */
//	public void setObjectRenderer(OWLObjectRenderer objectRenderer) {
//		this.objectRenderer = objectRenderer;
//	}
//
//	/**
//	 * @return the owlEntityRenderingCache
//	 */
//	public OWLEntityRenderingCache getOwlEntityRenderingCache() {
//		return owlEntityRenderingCache;
//	}
//
//	/**
//	 * @param owlEntityRenderingCache the owlEntityRenderingCache to set
//	 */
//	public void setOwlEntityRenderingCache(
//			OWLEntityRenderingCache owlEntityRenderingCache) {
//		this.owlEntityRenderingCache = owlEntityRenderingCache;
//	}
//
//	/**
//	 * @return the owlObjectRenderingCache
//	 */
//	public OWLObjectRenderingCache getOwlObjectRenderingCache() {
//		return owlObjectRenderingCache;
//	}
//
//	/**
//	 * @param owlObjectRenderingCache the owlObjectRenderingCache to set
//	 */
//	public void setOwlObjectRenderingCache(
//			OWLObjectRenderingCache owlObjectRenderingCache) {
//		this.owlObjectRenderingCache = owlObjectRenderingCache;
//	}
//
//	/**
//	 * @return the entityFinder
//	 */
//	public OWLEntityFinder getEntityFinder() {
//		return entityFinder;
//	}
//
//	/**
//	 * @param entityFinder the entityFinder to set
//	 */
//	public void setEntityFinder(OWLEntityFinder entityFinder) {
//		this.entityFinder = entityFinder;
//	}
//
//	/**
//	 * @return the owlReasonerManager
//	 */
//	public OWLReasonerManager getOwlReasonerManager() {
//		return owlReasonerManager;
//	}
//
//	/**
//	 * @param owlReasonerManager the owlReasonerManager to set
//	 */
//	public void setOwlReasonerManager(OWLReasonerManager owlReasonerManager) {
//		this.owlReasonerManager = owlReasonerManager;
//	}
//
//	/**
//	 * @return the manager
//	 */
//	public OWLOntologyManager getManager() {
//		return manager;
//	}
//
//	/**
//	 * @param manager the manager to set
//	 */
//	public void setManager(OWLOntologyManager manager) {
//		this.manager = manager;
//	}
//
//	/**
//	 * @return the ontologyLibraryManager
//	 */
//	public OntologyCatalogManager getOntologyLibraryManager() {
//		return ontologyLibraryManager;
//	}
//
//	/**
//	 * @param ontologyLibraryManager the ontologyLibraryManager to set
//	 */
//	public void setOntologyLibraryManager(
//			OntologyCatalogManager ontologyLibraryManager) {
//		this.ontologyLibraryManager = ontologyLibraryManager;
//	}
//
//	/**
//	 * @return the entityFactory
//	 */
//	public OWLEntityFactory getEntityFactory() {
//		return entityFactory;
//	}
//
//	/**
//	 * @param entityFactory the entityFactory to set
//	 */
//	public void setEntityFactory(OWLEntityFactory entityFactory) {
//		this.entityFactory = entityFactory;
//	}
//
//	/**
//	 * @return the ontSelectionStrategies
//	 */
//	public Set<OntologySelectionStrategy> getOntSelectionStrategies() {
//		return ontSelectionStrategies;
//	}
//
//	/**
//	 * @param ontSelectionStrategies the ontSelectionStrategies to set
//	 */
//	public void setOntSelectionStrategies(
//			Set<OntologySelectionStrategy> ontSelectionStrategies) {
//		this.ontSelectionStrategies = ontSelectionStrategies;
//	}
//
//	/**
//	 * @return the owlExpressionCheckerFactory
//	 */
//	public OWLExpressionCheckerFactory getOwlExpressionCheckerFactory() {
//		return owlExpressionCheckerFactory;
//	}
//
//	/**
//	 * @param owlExpressionCheckerFactory the owlExpressionCheckerFactory to set
//	 */
//	public void setOwlExpressionCheckerFactory(
//			OWLExpressionCheckerFactory owlExpressionCheckerFactory) {
//		this.owlExpressionCheckerFactory = owlExpressionCheckerFactory;
//	}
//
//	/**
//	 * @return the autoMappedRepositoryIRIMapper
//	 */
//	public AutoMappedRepositoryIRIMapper getAutoMappedRepositoryIRIMapper() {
//		return autoMappedRepositoryIRIMapper;
//	}
//
//	/**
//	 * @param autoMappedRepositoryIRIMapper the autoMappedRepositoryIRIMapper to set
//	 */
//	public void setAutoMappedRepositoryIRIMapper(
//			AutoMappedRepositoryIRIMapper autoMappedRepositoryIRIMapper) {
//		this.autoMappedRepositoryIRIMapper = autoMappedRepositoryIRIMapper;
//	}
//
//	/**
//	 * @return the userResolvedIRIMapper
//	 */
//	public UserResolvedIRIMapper getUserResolvedIRIMapper() {
//		return userResolvedIRIMapper;
//	}
//
//	/**
//	 * @param userResolvedIRIMapper the userResolvedIRIMapper to set
//	 */
//	public void setUserResolvedIRIMapper(UserResolvedIRIMapper userResolvedIRIMapper) {
//		this.userResolvedIRIMapper = userResolvedIRIMapper;
//	}
//
//	/**
//	 * @return the modelManagerChangeListeners
//	 */
//	public List<OWLModelManagerListener> getModelManagerChangeListeners() {
//		return modelManagerChangeListeners;
//	}
//
//	/**
//	 * @param modelManagerChangeListeners the modelManagerChangeListeners to set
//	 */
//	public void setModelManagerChangeListeners(
//			List<OWLModelManagerListener> modelManagerChangeListeners) {
//		this.modelManagerChangeListeners = modelManagerChangeListeners;
//	}
//
//	/**
//	 * @return the modelManagerListenerManager
//	 */
//	public ListenerManager<OWLModelManagerListener> getModelManagerListenerManager() {
//		return modelManagerListenerManager;
//	}
//
//	/**
//	 * @param modelManagerListenerManager the modelManagerListenerManager to set
//	 */
//	public void setModelManagerListenerManager(
//			ListenerManager<OWLModelManagerListener> modelManagerListenerManager) {
//		this.modelManagerListenerManager = modelManagerListenerManager;
//	}
//
//	/**
//	 * @return the changeListenerManager
//	 */
//	public ListenerManager<OWLOntologyChangeListener> getChangeListenerManager() {
//		return changeListenerManager;
//	}
//
//	/**
//	 * @param changeListenerManager the changeListenerManager to set
//	 */
//	public void setChangeListenerManager(
//			ListenerManager<OWLOntologyChangeListener> changeListenerManager) {
//		this.changeListenerManager = changeListenerManager;
//	}
//
//	/**
//	 * @return the ioListeners
//	 */
//	public List<IOListener> getIOListeners() {
//		return ioListeners;
//	}
//
//	/**
//	 * @param ioListeners the ioListeners to set
//	 */
//	public void setIOListeners(List<IOListener> ioListeners) {
//		this.ioListeners = ioListeners;
//	}
//
//	/**
//	 * @return the saveErrorHandler
//	 */
//	public SaveErrorHandler getSaveErrorHandler() {
//		return saveErrorHandler;
//	}
//
//	/**
//	 * @return the loadErrorHandler
//	 */
//	public OntologyLoadErrorHandler getLoadErrorHandler() {
//		return loadErrorHandler;
//	}
//
//	/**
//	 * @param historyManager the historyManager to set
//	 */
//	public void setHistoryManager(HistoryManager historyManager) {
//		this.historyManager = historyManager;
//	}
//
//	/**
//	 * @param dirtyOntologies the dirtyOntologies to set
//	 */
//	public void setDirtyOntologies(Set<OWLOntology> dirtyOntologies) {
//		this.dirtyOntologies = dirtyOntologies;
//	}
//
//	/**
//	 * @param activeOntologies the activeOntologies to set
//	 */
//	public void setActiveOntologies(Set<OWLOntology> activeOntologies) {
//		this.activeOntologies = activeOntologies;
//	}
//}
