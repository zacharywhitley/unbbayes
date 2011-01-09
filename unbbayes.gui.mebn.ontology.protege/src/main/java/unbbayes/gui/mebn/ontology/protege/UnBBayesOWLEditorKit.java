/**
 * 
 */
package unbbayes.gui.mebn.ontology.protege;

import java.io.File;
import java.net.ProtocolException;
import java.net.URI;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.osgi.framework.ServiceRegistration;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.ui.wizard.Wizard;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.protege.editor.owl.ProtegeOWL;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLModelManagerImpl;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.model.SaveErrorHandler;
import org.protege.editor.owl.ui.OntologyFormatPanel;
import org.protege.editor.owl.ui.SaveConfirmationPanel;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.error.OntologyLoadErrorHandlerUI;
import org.protege.editor.owl.ui.ontology.imports.missing.MissingImportHandlerUI;
import org.protege.editor.owl.ui.ontology.wizard.create.CreateOntologyWizard;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLOntologyStorerNotFoundException;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;

import unbbayes.util.Debug;

/**
 * It extends {@link OWLEditorKit} in order to
 * stop using {@link org.protege.editor.owl.model.OWLModelManagerImpl}
 * (which has some bugs when applications not using OSGI tries to use the library).
 * @author Shou Matsumoto
 *
 */
public class UnBBayesOWLEditorKit extends OWLEditorKit {
	
	// the superclass contains it too, but it was not visible for subclasses...
	private OntologyLoadErrorHandlerUI loadErrorHandler;
	private HashSet<URI> newPhysicalURIs;
	private ServiceRegistration registration;
	
	private OWLWorkspace workspace;

	/**
	 * This constructor is not private just to make it easier to extend.
	 * @deprecated use {@link #newInstance(OWLEditorKitFactory, OWLModelManager)} instead
	 * @param editorKitFactory
	 */
	protected UnBBayesOWLEditorKit(OWLEditorKitFactory editorKitFactory) {
		super(editorKitFactory);
	}
	
	/**
	 * Creates a new instance of UnBBayesOWLEditorKit and initializes the modelManager.
	 * It calls {@link #setOWLModelManager(OWLModelManager)}.
	 * @param editorKitFactory
	 * @param modelManager
	 * @return
	 */
	public static OWLEditorKit newInstance(OWLEditorKitFactory editorKitFactory, OWLModelManager modelManager) {
		UnBBayesOWLEditorKit ret = new UnBBayesOWLEditorKit(editorKitFactory);
		ret.setOWLModelManager(modelManager);
		return ret;
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.owl.OWLEditorKit#initialise()
	 */
	protected void initialise() {
		try {
			this.setNewPhysicalURIs(new HashSet<URI>());

	        this.setLoadErrorHandler(new OntologyLoadErrorHandlerUI(this));
	        this.loadIOListenerPlugins();
	        this.setRegistration(ProtegeOWL.getBundleContext().registerService(EditorKit.class.getCanonicalName(), this, new Properties()));
		} catch (Throwable e) {
			Debug.println(this.getClass(), "Failed to initialize protege's editor kit, but we may keep going on using only the UnBBayes' tool kit", e);
		}
	}
	
	/**
	 * This method is a unimplemented stub, but it can be used as a template method to load plug-ins.
	 * {@link #initialise()} calls this method.
	 * @see #initialise()
	 */
    protected void loadIOListenerPlugins() {
//        IOListenerPluginLoader loader = new IOListenerPluginLoader(this);
//        for(IOListenerPlugin pl : loader.getPlugins()) {
//            try {
//                IOListenerPluginInstance instance = pl.newInstance();
//                getModelManager().addIOListener(instance);
//            }
//            catch (Throwable e) {
//                ProtegeApplication.getErrorLog().logError(e);
//            }
//        }
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.OWLEditorKit#dispose()
	 */
	public void dispose() {
        super.dispose();
        if (this.getRegistration() != null) {
        	this.getRegistration().unregister();
        }
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.OWLEditorKit#handleNewRequest()
	 */
	public boolean handleNewRequest() throws Exception {
        CreateOntologyWizard w = new CreateOntologyWizard(null, this);
        int result = w.showModalDialog();
        if (result == Wizard.FINISH_RETURN_CODE) {
            OWLOntologyID id = w.getOntologyID();
            if (id != null) {
                OWLOntology ont = getModelManager().createNewOntology(id, w.getLocationURI());
                getModelManager().getOWLOntologyManager().setOntologyFormat(ont, w.getFormat());
                this.getNewPhysicalURIs().add(w.getLocationURI());
                addRecent(w.getLocationURI());
                return true;
            }
        }
        return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.protege.editor.owl.OWLEditorKit#handleSave()
	 */
    public void handleSave() throws Exception {
        try {
            Set<OWLOntology> dirtyOntologies = getModelManager().getDirtyOntologies();
            getModelManager().save();
            getWorkspace().save();
            for (URI uri : this.getNewPhysicalURIs()) {
                addRecent(uri);
            }
            this.getNewPhysicalURIs().clear();
            SaveConfirmationPanel.showDialog(this, dirtyOntologies);
        }
        catch (OWLOntologyStorerNotFoundException e) {
            OWLOntology ont = getModelManager().getActiveOntology();
            OWLOntologyFormat format = getModelManager().getOWLOntologyManager().getOntologyFormat(ont);
            try {
            	Debug.println(this.getClass(), "Could not save ontology in the specified format (" + format + ").\n" + "Please select 'Save As' and choose another format.");
            } catch (Throwable t) {
				t.printStackTrace();
			}
        }
    }


	/**
	 * This method is called in {@link #setOWLModelManager(OWLModelManager)} in order to initialize manager.
	 * @param ont
	 * @param physicalURIForOntology
	 * @param e
	 * @throws Exception
	 */
	protected void handleSaveError(OWLOntology ont, URI physicalURIForOntology, OWLOntologyStorageException e) throws Exception {
	        // catch the case where the user is trying to save an ontology that has been loaded from the web
	        if (e.getCause() != null && e.getCause() instanceof ProtocolException){
	            handleSaveAs(ont);
	        }
	        else{
	            throw e;
	        }
    }
	
	/**
	 * This method is called in {@link #handleSaveError(OWLOntology, URI, OWLOntologyStorageException)}.
     * This should only save the specified ontology.
     * @param ont the ontology to save
     * @throws Exception
     */
    protected boolean handleSaveAs(OWLOntology ont) throws Exception {
        OWLOntologyManager man = getModelManager().getOWLOntologyManager();
        OWLOntologyFormat oldFormat = man.getOntologyFormat(ont);
        OWLOntologyFormat format = OntologyFormatPanel.showDialog(this,
                                                                  oldFormat,
                                                                  getModelManager().getRendering(ont));
        if (format == null) {
        	Debug.println(this.getClass(), "Invalid format");
            return false;
        }
        if (oldFormat instanceof PrefixOWLOntologyFormat && format instanceof PrefixOWLOntologyFormat) {
        	PrefixOWLOntologyFormat oldPrefixes  = (PrefixOWLOntologyFormat) oldFormat;
        	for (String name : oldPrefixes.getPrefixNames()) {
        		((PrefixOWLOntologyFormat) format).setPrefix(name, oldPrefixes.getPrefix(name));
        	}
        }
        File file = this.getSaveAsOWLFile(ont);
        if (file != null){
            man.setOntologyFormat(ont, format);
            man.setOntologyDocumentIRI(ont, IRI.create(file));
            getModelManager().save(ont);
            addRecent(file.toURI());
            return true;
        } else{
        	Debug.println(this.getClass(), "No valid file specified for the save as operation - quitting");
            return false;
        }
    }
    
    /**
     * This method is called in {@link #handleSaveAs(OWLOntology)} in order to 
     * save an ontology as a file.
     * @param ont
     * @return
     */
    protected File getSaveAsOWLFile(OWLOntology ont) {
        UIHelper helper = new UIHelper(this);
        File file = helper.saveOWLFile("Please select a location in which to save: " + getModelManager().getRendering(ont));
        if (file != null) {
            int extensionIndex = file.toString().lastIndexOf('.');
            if (extensionIndex == -1) {
                file = new File(file.toString() + ".owl");
            }
            else if (extensionIndex != file.toString().length() - 4) {
                file = new File(file.toString() + ".owl");
            }
        }
        return file;
    }
	
	/* (non-Javadoc)
	 * @see org.protege.editor.owl.OWLEditorKit#setOWLModelManager(org.protege.editor.owl.model.OWLModelManager)
	 */
	public void setOWLModelManager(OWLModelManager modelManager) {
		if (modelManager == null) {
			return;	// return doing nothing
		}
		// update the model manager referenced by superclass
		try {
			super.setOWLModelManager(modelManager);
		} catch (Throwable e) {
			Debug.println(this.getClass(), "Could not set super's owl model manager, but that's not a problem yet.", e);
		}
		if (!modelManager.equals(this.getOWLModelManager())) {
			throw new IllegalStateException("Could not update the upper model manager to " + modelManager);
		}
		try {
			// update model manager's attributes
			modelManager.setExplanationManager(UnBBayesExplanationManager.getInstance(this));
	        modelManager.setMissingImportHandler(new MissingImportHandlerUI(this));	
	        modelManager.setSaveErrorHandler(new SaveErrorHandler(){	
	            public void handleErrorSavingOntology(OWLOntology ont, URI physicalURIForOntology, OWLOntologyStorageException e) throws Exception {
	                handleSaveError(ont, physicalURIForOntology, e);
	            }
	        });
	        this.setLoadErrorHandler(new OntologyLoadErrorHandlerUI(this));
	        modelManager.setLoadErrorHandler(this.getLoadErrorHandler());
		} catch (Throwable e) {
			
			Debug.println(this.getClass(), "Could not reset OWLModelManager");
			e.printStackTrace();
		}
	}
	
	/**
	 * @see OWLEditorKit#handleLoadFrom(URI)
	 */
	public boolean handleLoadFrom(URI uri) throws Exception {
		this.getLoadErrorHandler().setReloadFlag(false);
        boolean success = ((OWLModelManagerImpl) getModelManager()).loadOntologyFromPhysicalURI(uri);
        
        if (success){
            addRecent(uri);
        }
        else if (this.getLoadErrorHandler().getReloadFlag()) {
            success = handleLoadFrom(uri);
        }
        return success;
    }
	
	/**
     * Gets the <code>Workspace</code> that is used in the UI to
     * display the contents of the clsdescriptioneditor kit "model".
     */
    public OWLWorkspace getWorkspace() {
        if (workspace == null) {
        	// new OWLWorkspace()
//        	asdf
            workspace = UnBBayesOWLWorkspace.getInstance();	
            workspace.setup(this);
            workspace.initialise();
        }
        return workspace;
    }

	/**
	 * This loadErrorManager is used in {@link #handleLoadFrom(URI)} and {@link #setOWLModelManager(OWLModelManager)}
	 * in order to handle errors when opening ontologies or updating model managers
	 * @return the loadErrorHandler
	 */
	public OntologyLoadErrorHandlerUI getLoadErrorHandler() {
		return loadErrorHandler;
	}

	/**
	 * This loadErrorManager is used in {@link #handleLoadFrom(URI)} and {@link #setOWLModelManager(OWLModelManager)}
	 * in order to handle errors when opening ontologies or updating model managers
	 * @param loadErrorHandler the loadErrorHandler to set
	 */
	public void setLoadErrorHandler(OntologyLoadErrorHandlerUI loadErrorHandler) {
		this.loadErrorHandler = loadErrorHandler;
	}

	/**
	 * @return the newPhysicalURIs
	 */
	public HashSet<URI> getNewPhysicalURIs() {
		return newPhysicalURIs;
	}

	/**
	 * @param newPhysicalURIs the newPhysicalURIs to set
	 */
	public void setNewPhysicalURIs(HashSet<URI> newPhysicalURIs) {
		this.newPhysicalURIs = newPhysicalURIs;
	}

	/**
	 * @return the registration
	 */
	public ServiceRegistration getRegistration() {
		return registration;
	}

	/**
	 * @param registration the registration to set
	 */
	public void setRegistration(ServiceRegistration registration) {
		this.registration = registration;
	}

	/**
	 * @param workspace the workspace to set
	 */
	public void setWorkspace(OWLWorkspace workspace) {
		this.workspace = workspace;
	}

}
