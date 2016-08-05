/**
 * TODO make it compatible with Hermit 1.3.4
 */
package unbbayes.io.mebn.protege;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.osgi.framework.BundleException;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.inference.ProtegeOWLReasonerInfo;
import org.protege.editor.owl.model.inference.ReasonerStatus;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import unbbayes.io.mebn.MebnIO;
import unbbayes.io.mebn.exceptions.IOMebnException;
import unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWL2IO;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ontology.protege.IBundleLauncher;
import unbbayes.prs.mebn.ontology.protege.OWLClassExpressionParserFacade;
import unbbayes.prs.mebn.ontology.protege.ProtegeBundleLauncher;
import unbbayes.util.Debug;

/**
 * This class extends {@link OWLAPICompatiblePROWL2IO} in order
 * to fill {@link MultiEntityBayesianNetwork#getStorageImplementor()}
 * using {@link ProtegeStorageImplementorDecorator}
 * @author Shou Matsumoto
 *
 */
public class Protege41CompatiblePROWL2IO extends OWLAPICompatiblePROWL2IO {

	
	private long maximumBusyWaitingCount = 1000;
	private long sleepTimeWaitingReasonerInitialization = 900;
	
	private IBundleLauncher protegeBundleLauncher;
	
	/**
	 * This is public just to enable plug-in compatibility
	 * @deprecated
	 */
	protected Protege41CompatiblePROWL2IO() {
		// ontologies loaded with this class are expected to contain complex DL formulas, and such inferred knowledge must be reused by IO
		this.setToInitializeReasoner(true);
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
//		System.gc();
		
		OWLEditorKit kit = null;	// kit to extract ontology and fill storage implementor (of mebn)
		
		try {
			
			kit = this.startProtegeEditorKit(file.toURI());
			if (kit == null) {
				System.err.println("Could not open Protege");
				return null;
			}
			
			// indicate the super class to use the ontology loaded by protege
			this.setLastOWLOntology(kit.getOWLModelManager().getActiveOntology());

			if (isToInitializeReasoner()) {
				// Check if reasoner is set up. Set it as the reasoner to use for I/O operation
				OWLReasoner reasoner = this.setupOWLReasoner(kit.getOWLModelManager());
				if (reasoner != null) {
					this.setLastOWLReasoner(reasoner);
				}
			} else {
				// explicitly indicate that we don't want to use reasoners
				this.setLastOWLReasoner(null);
			}
			
			try {
				Debug.println(this.getClass(), "Ontology loaded by Protege : " + kit.getOWLModelManager().getActiveOntology());
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.println("Could not use protege's ontology loader. Using OWLAPI instead...");
			return super.loadMebn(file);
		}
		
		// update and initialize the parser of manchester owl syntax expressions (so that owl reasoners can be used for string expressions)
		this.setOwlClassExpressionParserDelegator(OWLClassExpressionParserFacade.getInstance(kit.getOWLModelManager()));
		
		// extract default name
		String defaultMEBNName = "MEBN";
		try {
			defaultMEBNName = this.extractName(this.getLastOWLOntology());
//			defaultMEBNName = this.getLastOWLOntology().getOntologyID().getOntologyIRI().getFragment();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// load mebn using the super class
		MultiEntityBayesianNetwork ret = this.getMEBNFactory().createMEBN(defaultMEBNName);	// instantiate using temporary name
		
		try {
			super.loadMEBNFromOntology(ret, this.getLastOWLOntology(), this.getLastOWLReasoner());					// populate MEBN including its name
		} catch (RuntimeException e) {
			e.printStackTrace();
			Debug.println(getClass(), "Could not load MTheory, but since we loaded the ontology, we will display it...");
		}
		
		
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
		

		
//		System.gc();
		
		return ret;
	}

	/**
	 * It starts protege and obtains the editor kit (a link to protege GUI and ontology)
	 * @param uri : the default ontology to be opened by protege. If set to null, it will load nothing
	 * @return
	 * @throws BundleException
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	protected OWLEditorKit startProtegeEditorKit(URI uri) throws BundleException, IOException, InstantiationException, IllegalAccessException {
		OWLEditorKit kit = null;
		
		// specify the bundle laucher that the desired URI is from file (this will set up the system properties and force protege to load this file)
		this.getProtegeBundleLauncher().setDefaultOntolgyURI(uri);
		
		// load ontology using protege
		this.getProtegeBundleLauncher().startProtegeBundles();
		
		// obtain manager. We expect it to be an instance of ProtegeManager
		ProtegeManager manager = (ProtegeManager)this.getProtegeBundleLauncher().getProtegeManager();
		
		// verify if at least one ontology is opened. If no ontology was opened previously, there is no kit (GUI) to display...
		if (manager.getEditorKitManager().getEditorKitCount() <= 0) {
			try {
				Debug.println(this.getClass(), "Could not open Protege editor kit using bundle launcher's default ontology URI = " + this.getProtegeBundleLauncher().getDefaultOntolgyURI()
						+ ". Retry calling ProtegeApplication#editURI(" + uri + ") instead.");
			} catch (Throwable t) {
				t.printStackTrace();
			}
			// Open ontology (and editor kit) using file. An editor kit will be created "automagically". 
			try {
				manager.getApplication().editURI(uri);
			} catch (Exception e) {
				throw new BundleException(this.getResource().getString("FileNotFoundException") + " : " + uri, e);
			}
			// wait for a while
			try {
				Thread.sleep(getSleepTimeWaitingReasonerInitialization());
			} catch (Throwable t) {
				t.printStackTrace();
				// do not interrupt execution just because a waiting time was interrupted
			}
		}
		
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
		return kit;
	}

	/**
	 * This method initializes OWL reasoner from protege bundles.
	 * If {@link #isToInitializeReasoner()} is false, it will return null.
	 * It currently uses polling (a loop with a sleep) to check if reasoner is ready.
	 * {@link #getMaximumBuzyWaitingCount()} is the polling limit.
	 * @param protegeModelManager : this is a protege's object for general purpose.
	 * @return : the initialized reasoner or null if it could not be initialized.
	 */
	protected OWLReasoner setupOWLReasoner(OWLModelManager protegeModelManager) {
		// force it to use the first reasoner different than "NoOpReasoner" (which in protege is a null object)
		if (this.isToInitializeReasoner()) {
			// it will hold the reasoner's ID except the null object (which is an instance of NoOPReasoner, and it's assigned name is "None")
			String reasonerIDExceptNone = null;
			for (ProtegeOWLReasonerInfo info : protegeModelManager.getOWLReasonerManager().getInstalledReasonerFactories()) {
//				if ( ! NoOpReasonerInfo.NULL_REASONER_ID.equals( info.getReasonerId() ) ){ // We are now sure that this is not a NULL reasoner.
				if ( info.getReasonerId().contains("HermiT") ){	// only initialize hermit, because waiting for all reasoners to initialize is too much time
					// user must manually initialize other reasoners by pressing the button Load KB after changing the reasoner.
					try {
						protegeModelManager.getOWLReasonerManager().setCurrentReasonerFactoryId(info.getReasonerId());
						protegeModelManager.getOWLReasonerManager().classifyAsynchronously(protegeModelManager.getOWLReasonerManager().getReasonerPreferences().getPrecomputedInferences());
						
						// maybe there would be some synchronization problems, because of protege's asynchronous initialization of reasoners. Let's wait until it becomes ready
						for (long i = 0; i < this.getMaximumBuzyWaitingCount(); i++) {
							// TODO Stop using buzy waiting!!!
							if (ReasonerStatus.NO_REASONER_FACTORY_CHOSEN.equals(protegeModelManager.getOWLReasonerManager().getReasonerStatus())) {
								// reasoner is not chosen...
								Debug.println(this.getClass(), "No reasoner is chosen. Trying to reload...");
								// try reloading again
								protegeModelManager.getOWLReasonerManager().setCurrentReasonerFactoryId(info.getReasonerId());
								protegeModelManager.getOWLReasonerManager().classifyAsynchronously(protegeModelManager.getOWLReasonerManager().getReasonerPreferences().getPrecomputedInferences());
								continue;
							}
							if (ReasonerStatus.INITIALIZED.equals(protegeModelManager.getOWLReasonerManager().getReasonerStatus())) {
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
						if (protegeModelManager.getReasoner().isConsistent()) {
							// let's just use the 1st (non NULL) option and ignore other protege reasoners
							return protegeModelManager.getReasoner();
						} else {
							try {
								Debug.println(this.getClass(), protegeModelManager.getActiveOntology() + " is an inconsistent ontology.");
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
		return null;
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
	 * @see #getMaximumBusyWaitingCount()
	 * @deprecated : method not spelled correctly.
	 */
	public long getMaximumBuzyWaitingCount() {
		return maximumBusyWaitingCount;
	}
	/**
	 * This is the maximum time we buzy-wait an uninitialized reasoner until it gets ready.
	 * @return the maximumBusyWaitingCount
	 * @deprecated TODO stop using buzy waiting
	 */
	public long getMaximumBusyWaitingCount() {
		return maximumBusyWaitingCount;
	}
	
	/**
	 * @see #setMaximumBusyWaitingCount(long)
	 * @deprecated : method not spelled correctly.
	 */
	public void setMaximumBuzyWaitingCount(long maximumBuzyWaitingCount) {
		this.maximumBusyWaitingCount = maximumBuzyWaitingCount;
	}

	/**
	 * This is the maximum time we buzy-wait an uninitialized reasoner until it gets ready.
	 * @param maximumBusyWaitingCount the maximumBusyWaitingCount to set
	 * @deprecated TODO stop using buzy waiting
	 */
	public void setMaximumBusyWaitingCount(long maximumBuzyWaitingCount) {
		this.maximumBusyWaitingCount = maximumBuzyWaitingCount;
	}

	/**
	 * This is the argument that will be passed to {@link Thread#sleep(long)} when we have to wait for reasoner initialization
	 * and reasoner is not ready yet.
	 * @return the sleepTimeWaitingReasonerInitialization
	 * @see #getMaximumBuzyWaitingCount()
	 */
	public long getSleepTimeWaitingReasonerInitialization() {
//		System.gc();	// ...just because methods calling this method are "expensive"
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

//	/* (non-Javadoc)
//	 * @see unbbayes.io.mebn.owlapi.OWLAPICompatiblePROWLIO#saveMebn(java.io.File, unbbayes.prs.mebn.MultiEntityBayesianNetwork)
//	 */
//	@Override
//	public void saveMebn(File file, MultiEntityBayesianNetwork mebn) throws IOException, IOMebnException {
//		// update storage implementor if it is not set
//		if (mebn.getStorageImplementor() == null || !(mebn.getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator)) {
//			try {
//				// start new protege instance
//				OWLEditorKit kit = this.startProtegeEditorKit(file.toURI());
//				// initialize reasoner (to be used)
//				this.setLastOWLReasoner(this.setupOWLReasoner(kit.getOWLModelManager()));
//				mebn.setStorageImplementor(ProtegeStorageImplementorDecorator.newInstance(kit));
//			} catch (BundleException e) {
//				// perform exception translation to match method signature
//				throw new IOMebnException(e);
//			} catch (InstantiationException e) {
//				// perform exception translation to match method signature
//				throw new IOMebnException(e);
//			} catch (IllegalAccessException e) {
//				// perform exception translation to match method signature
//				throw new IOMebnException(e);
//			}
//		}
//		super.saveMebn(file, mebn);
//	}
	
	

}
