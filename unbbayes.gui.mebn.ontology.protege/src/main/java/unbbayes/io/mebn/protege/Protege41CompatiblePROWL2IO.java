/**
 * 
 */
package unbbayes.io.mebn.protege;

import java.io.File;
import java.io.IOException;

import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.inference.NoOpReasonerInfo;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.protege.editor.owl.model.inference.ReasonerStatus;

import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ontology.protege.IBundleLauncher;
import unbbayes.prs.mebn.ontology.protege.OWLClassExpressionParserFacade;
import unbbayes.prs.mebn.ontology.protege.ProtegeBundleLauncher;
import unbbayes.util.Debug;

/**
 * This class extends {@link OWLAPICompatiblePROWLIO} in order
 * to fill {@link MultiEntityBayesianNetwork#getStorageImplementor()}
 * using {@link ProtegeStorageImplementorDecorator}
 * @author Shou Matsumoto
 *
 */
public class Protege41CompatiblePROWL2IO extends OWLAPICompatiblePROWLIO {

	private boolean initializeReasoner = true;
	
	private long maximumBuzyWaitingCount = 100;
	private long sleepTimeWaitingReasonerInitialization = 1000;
	
	private IBundleLauncher protegeBundleLauncher;
	
	/**
	 * This is public just to enable plug-in compatibility
	 * @deprecated
	 */
	public Protege41CompatiblePROWL2IO() {
	}
	
	/**
	 * Default constructor method.
	 * @return
	 */
	public static MebnIO newInstance() {
		return new Protege41CompatiblePROWL2IO();
	}

	/* (non-Javadoc)
	 * @see unbbayes.io.mebn.OWLAPICompatiblePROWL2IO#loadMebn(java.io.File)
	 */
	public MultiEntityBayesianNetwork loadMebn(File file) throws IOException,
			IOMebnException {
		System.gc();
		
		OWLEditorKit kit = null;	// kit to extract ontology and fill storage implementor (of mebn)
		
		try {
			// specify the bundle laucher that the desired URI is from file (this will set up the system properties and force protege to load this file)
			this.getProtegeBundleLauncher().setDefaultOntolgyURI(file.toURI());
			
			// load ontology using protege
			this.getProtegeBundleLauncher().startProtegeBundles();
			
			// obtain manager. We expect it to be an instance of ProtegeManager
			ProtegeManager manager = (ProtegeManager)this.getProtegeBundleLauncher().getProtegeManager();
			
			// obtain the last opened kit (which is the one opened now)
			try {
				kit = (OWLEditorKit)manager.getEditorKitManager().getEditorKits().get(manager.getEditorKitManager().getEditorKitCount() - 1);
			}catch (Exception e) {
				e.printStackTrace();
				try {
					if (this.getProtegeBundleLauncher() instanceof ProtegeBundleLauncher) {
						((ProtegeBundleLauncher)this.getProtegeBundleLauncher()).hideProtegeGUI();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				return null;
			}
			
			// hide protege's frame immediately (this is because the loading process may take so long, and the protege frame may be visible to users during that)
			try {
				manager.getFrame(kit.getWorkspace()).setVisible(false);
			} catch (Throwable t) {
				t.printStackTrace();
				// it is OK to ignore, because this frame will be removed from view after the ontology is created.
			}
			
			// indicate the super class to use the ontology loaded by protege
			this.setLastOWLOntology(kit.getOWLModelManager().getActiveOntology());
			
			// force it to use the first reasoner different than "NoOpReasoner" (which in protege is a null object)
			if (this.isToInitializeReasoner()) {
				// it will hold the reasoner's ID except the null object (which is an instance of NoOPReasoner, and it's assigned name is "None")
				String reasonerIDExceptNone = null;
				for (ProtegeOWLReasonerInfo info : kit.getOWLModelManager().getOWLReasonerManager().getInstalledReasonerFactories()) {
					if ( ! NoOpReasonerInfo.NULL_REASONER_ID.equals( info.getReasonerId() ) ){
						// We are now sure that this is not a NULL reasoner.
						try {
							kit.getOWLModelManager().getOWLReasonerManager().setCurrentReasonerFactoryId(info.getReasonerId());
							kit.getOWLModelManager().getOWLReasonerManager().classifyAsynchronously(kit.getOWLModelManager().getOWLReasonerManager().getReasonerPreferences().getPrecomputedInferences());
							
							// maybe there would be some synchronization problems, because of protege's asynchronous initialization of reasoners. Let's wait until it becomes ready
							for (long i = 0; i < this.getMaximumBuzyWaitingCount(); i++) {
								// TODO Stop using buzy waiting!!!
								if (ReasonerStatus.NO_REASONER_FACTORY_CHOSEN.equals(kit.getOWLModelManager().getOWLReasonerManager().getReasonerStatus())) {
									// reasoner is not chosen...
									Debug.println(this.getClass(), "No reasoner is chosen. Trying to reload...");
									// try reloading again
									kit.getOWLModelManager().getOWLReasonerManager().setCurrentReasonerFactoryId(info.getReasonerId());
									kit.getOWLModelManager().getOWLReasonerManager().classifyAsynchronously(kit.getOWLModelManager().getOWLReasonerManager().getReasonerPreferences().getPrecomputedInferences());
									continue;
								}
								if (ReasonerStatus.INITIALIZED.equals(kit.getOWLModelManager().getOWLReasonerManager().getReasonerStatus())) {
									// reasoner is ready now
									break;
								}
								Debug.println(this.getClass(), "Waiting for reasoner initialization...");
								try {
									// sleep and try reasoner status after
									Thread.sleep(this.getSleepTimeWaitingReasonerInitialization());
								} catch (Throwable t) {
									// a thread sleep should not break normal program flow...
									t.printStackTrace();
								}
							}
							
							// if the reasoner tells the ontology is consistent, use it to load MEBN.
							if (kit.getModelManager().getReasoner().isConsistent()) {
								// the reasoner is finally ready. Set it as the reasoner to use for I/O operation
								this.setLastOWLReasoner(kit.getModelManager().getReasoner());
								break;	// let's just use the 1st (non NULL) option and ignore other protege reasoners
							} else {
								try {
									Debug.println(this.getClass(), kit.getOWLModelManager().getActiveOntology() + " is an inconsistent ontology.");
								}catch (Throwable t) {
									t.printStackTrace();
								}
								// we will try another reasoner or we will not use a reasoner to load PR-OWL
							}
						} catch (Exception e) {
							e.printStackTrace();
							continue;
						}
					}
				}
			}
			
			try {
				Debug.println(this.getClass(), "Ontology loaded by Protege : " + kit.getOWLModelManager().getActiveOntology());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("Could not use protege's ontology loader. Using OWLAPI instead...");
		}
		
		// update and initialize the parser of manchester owl syntax expressions (so that owl reasoners can be used for string expressions)
		this.setOwlClassExpressionParserDelegator(OWLClassExpressionParserFacade.getInstance(kit.getOWLModelManager()));
		
		// extract default name
		String defaultMEBNName = "MEBN";
		try {
			defaultMEBNName = this.getLastOWLOntology().getOntologyID().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// load mebn using the super class
		MultiEntityBayesianNetwork ret = this.getMEBNFactory().createMEBN(defaultMEBNName);	// instantiate using temporary name
		super.loadMEBNFromOntology(ret, this.getLastOWLOntology(), null);					// populate MEBN including its name
		
		
		// fill mebn with protege's storage implementor if we could load protege previously
		if (kit != null) {
			// set storage implementor as the protege's decorator
			try {
				ret.setStorageImplementor(ProtegeStorageImplementorDecorator.newInstance(kit));
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not update storage implementor.");
			}
			try {
				Debug.println(this.getClass(), "Storage implementor set to protege editor kit: " + kit);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		
		System.gc();
		
		return ret;
	}

	/**
	 * This is a bundle laucher which launches protege using osgi.
	 * @return the protegeBundleLauncher. A non null value (it will lazily instantiate if none was specified)
	 */
	public IBundleLauncher getProtegeBundleLauncher() {
		if (protegeBundleLauncher == null) {
			protegeBundleLauncher = ProtegeBundleLauncher.getInstance();
		}
		return protegeBundleLauncher;
	}

	/**
	 * This is a bundle laucher which launches protege using osgi.
	 * @param protegeBundleLauncher the protegeBundleLauncher to set
	 */
	public void setProtegeBundleLauncher(IBundleLauncher protegeBundleLauncher) {
		this.protegeBundleLauncher = protegeBundleLauncher;
	}

	/**
	 * If true, it will initialize and use the reasoner in order to extract PR-OWL elements from the ontology
	 * @return the initializeReasoner
	 */
	public boolean isToInitializeReasoner() {
		return initializeReasoner;
	}

	/**
	 * If true, it will initialize and use the reasoner in order to extract PR-OWL elements from the ontology
	 * @param initializeReasoner the initializeReasoner to set
	 */
	public void setIsToInitializeReasoner(boolean initializeReasoner) {
		this.initializeReasoner = initializeReasoner;
	}

	/**
	 * This is the maximum time we buzy-wait an uninitialized reasoner until it gets ready.
	 * @return the maximumBuzyWaitingCount
	 * @deprecated TODO stop using buzy waiting
	 */
	public long getMaximumBuzyWaitingCount() {
		return maximumBuzyWaitingCount;
	}

	/**
	 * This is the maximum time we buzy-wait an uninitialized reasoner until it gets ready.
	 * @param maximumBuzyWaitingCount the maximumBuzyWaitingCount to set
	 * @deprecated TODO stop using buzy waiting
	 */
	public void setMaximumBuzyWaitingCount(long maximumBuzyWaitingCount) {
		this.maximumBuzyWaitingCount = maximumBuzyWaitingCount;
	}

	/**
	 * This is the argument that will be passed to {@link Thread#sleep(long)} when we have to wait for reasoner initialization
	 * and reasoner is not ready yet.
	 * @return the sleepTimeWaitingReasonerInitialization
	 * @see #getMaximumBuzyWaitingCount()
	 */
	public long getSleepTimeWaitingReasonerInitialization() {
		return sleepTimeWaitingReasonerInitialization;
	}

	/**
	 * This is the argument that will be passed to {@link Thread#sleep(long)} when we have to wait for reasoner initialization
	 * and reasoner is not ready yet.
	 * @param sleepTimeWaitingReasonerInitialization the sleepTimeWaitingReasonerInitialization to set
	 * @see #getMaximumBuzyWaitingCount()
	 */
	public void setSleepTimeWaitingReasonerInitialization(
			long sleepTimeWaitingReasonerInitialization) {
		this.sleepTimeWaitingReasonerInitialization = sleepTimeWaitingReasonerInitialization;
	}
	
	

}
