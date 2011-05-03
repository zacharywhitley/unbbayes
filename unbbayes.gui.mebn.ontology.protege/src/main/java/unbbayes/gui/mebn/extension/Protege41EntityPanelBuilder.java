/**
 * 
 */
package unbbayes.gui.mebn.extension;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.osgi.framework.Bundle;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.ui.workspace.Workspace;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder;
import unbbayes.io.mebn.owlapi.IOWLAPIStorageImplementorDecorator;
import unbbayes.io.mebn.protege.IProtegeStorageImplementorDecorator;
import unbbayes.io.mebn.protege.ProtegeStorageImplementorDecorator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.prs.mebn.ontology.protege.IBundleLauncher;
import unbbayes.prs.mebn.ontology.protege.ProtegeBundleLauncher;
import unbbayes.util.Debug;

/**
 * A builder for a tab showing the protege's OWL property editor.
 * 
 * @author Shou Matsumoto
 *
 */
public class Protege41EntityPanelBuilder extends JPanel implements IMEBNEditionPanelBuilder {
	
	
	public IBundleLauncher protegeBundleLauncher;
	

	private Properties launchProperties = new Properties();

	/** This is used in {@link #buildPanel(MultiEntityBayesianNetwork, IMEBNMediator)}. It was modeled as an attribute just because we need it to be visible for inner component listeners (for resize actions) */
	private Component view;


//	private JButton synchronizeReasonerButton;
//
//
//	private JButton saveProtegeOntologyButton;


	private JMenuItem synchronizeReasonerMenuItem;
	
	
	
	/**
	 * Default constructor must be public to enable plugin support
	 */
	public Protege41EntityPanelBuilder() {
		super();
	}

	

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder#buildPanel(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public JPanel buildPanel(MultiEntityBayesianNetwork mebn,IMEBNMediator mediator) {
		// we do not need this plugin if mebn is not bound to a project
		if (mebn == null || mebn.getStorageImplementor() == null ) {
			return null;
		}
		if (!(mebn.getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator)
				|| ((IOWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee() == null) {
			return null;
		}
		
		// migrated to ProtegeBundleLauncher
//		// from this line, we are doing workarounds in order to avoid protege bugs happening when OSGI is not initialized (e.g. protege plug-in errors)
//		// TODO find out a more smart way
//		this.initializeSystemProperties(mebn);
//		
//		// fill this.getLaunchProperties()
//		this.initializeLaunchProperties();
		
		// if mebn is carring protege's decorator, then protege is already started up
		if (!(mebn.getStorageImplementor() instanceof IProtegeStorageImplementorDecorator)) {
			// start up protege using osgi, because it was not started yet.
			try {
				this.getProtegeBundleLauncher().setDefaultOntolgyURI(this.extractURIFromMEBN(mebn));
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Could not set ontology URI, but we can still try to launch protege and set URI at a later stage.");
			}
			try {
				this.getProtegeBundleLauncher().startProtegeBundles();
			} catch (Exception e) {
				throw new IllegalStateException("BundleLauncher = " + this.getProtegeBundleLauncher(), e);
			}
		}
		

		// look for protege bundle
		Bundle bundle = this.getProtegeBundleLauncher().getProtegeBundle();
		
		try {
			view = this.extractWorkspace(bundle, mebn, mediator);
			if (view != null) {
				this.setLayout(new BorderLayout());
				
				// initialize size
				Dimension size = new Dimension(600,480);
				view.setPreferredSize(size);
				view.setSize(size);
				
				// prepare this component
//						this.setBorder(ComponentFactory.createTitledBorder(mebn.toString()));
				JScrollPane content = new JScrollPane(view);
				this.add(content, BorderLayout.CENTER);
				
				// create toolbar for useful protege functionalities
//				this.add(this.buildProtegeTools(bundle, mebn, mediator), BorderLayout.NORTH);
				
				// listener on resize event
				this.addComponentListener(new ComponentListener() {
					public void componentResized(ComponentEvent e) {
						if (e.getComponent() != null) {
							// resize protege workspace if the panel is resized
							Dimension d = new Dimension(e.getComponent().getSize().width - 10, e.getComponent().getSize().height - 50);
							view.setSize(d);
							view.setPreferredSize(d);
							view.repaint();
						}
					}
					public void componentShown(ComponentEvent e) {} 
					public void componentMoved(ComponentEvent e) {}
					public void componentHidden(ComponentEvent e) {}
				});
				
				// hide entities and individuals, because "this" panel will be responsible for those
				this.hideUnwantedComponents(mediator);	
				return this;
				// TODO dispose editor kit when inner frame is closed
			}
		} catch (Throwable e) {
			e.printStackTrace();
			Debug.println(this.getClass(), "Could not extract protege workspace.");
		}
		
		
		
//		try {
//			if (ProtegeManager.getInstance().loadAndSetupEditorKitFromURI(ProtegeManager.getInstance().getEditorKitFactoryPlugins().get(0), this.extractURIFromMEBN(mebn))) {
//				EditorKit editorKit = ProtegeManager.getInstance().getEditorKitManager().getEditorKits().get(0);
//				
//				// extract frame
//				JFrame workspaceFrame = ProtegeManager.getInstance().getFrame(editorKit.getWorkspace());
//				Component contentPane = workspaceFrame.getContentPane();
//				// unlink content from frame
//				workspaceFrame.setContentPane(null);
//				workspaceFrame.remove(contentPane);
//				
//				// hide frame
//				workspaceFrame.setVisible(false);
//				workspaceFrame.dispose();
//				
//				// return content
//				JPanel ret = new JPanel();
//				ret.setSize(600, 480);
//				ret.add(contentPane);
//				return ret;
//			}
//		} catch (Throwable t) {
//			throw new IllegalStateException("Could not start ProtegeManager and EditorKit.", t);
//		}
		
		return null;
	}
	
	/**
	 * This method hides some unwanted components, like the ones to display currently available entities or individuals
	 * (because such elements must be edited in the panel built by {@link Protege41EntityPanelBuilder}).
	 * @param mediator
	 */
	protected void hideUnwantedComponents(IMEBNMediator mediator) {
//		// hide entity, entity individuals and findings panel from original MEBN editor
//		try {
//			// remove object entity tab
//			mediator.getMebnEditionPane().getJtbTabSelection().remove(mediator.getMebnEditionPane().getBtnTabOptionEntity());
//		} catch (Exception t) {
//			t.printStackTrace();
//		}
//		try {
//			// remove object entity individuals panel
//			mediator.getMebnEditionPane().getJtbTabSelection().remove(mediator.getMebnEditionPane().getBtnTabOptionEntityFinding());
//		} catch (Exception t) {
//			t.printStackTrace();
//		}
//		try {
//			// remove finding panel
//			mediator.getMebnEditionPane().getJtbTabSelection().remove(mediator.getMebnEditionPane().getBtnTabOptionNodeFinding());
//		} catch (Exception t) {
//			t.printStackTrace();
//		}
	}
	
	// the following was migrated to ProtegeBundleLauncher
//	/**
//	 * Looks for a OSGi bundle carrying the protege application.
//	 * It searches in {@link #getLoadedCoreBundles()} for a bundle containing a symbolic
//	 * name matching {@link ProtegeApplication#ID}.
//	 * {@link #getLoadedCoreBundles()} is initialized in {@link #startProtegeBundles()}.
//	 * @param loadedCoreBundles
//	 * @return the bundle found or null if none was found.
//	 * @see #getLoadedCoreBundles()
//	 * @see #startProtegeBundles()
//	 * @see ProtegeApplication
//	 */
//	public Bundle searchLoadedProtegeBundle() {
//		if (this.getLoadedCoreBundles() != null) {
//			for (Bundle bundle : this.getLoadedCoreBundles()) {
//				if (ProtegeApplication.ID.equals(bundle.getSymbolicName())) {
//					return bundle;
//				}
//			}
//		}
//		return null;
//	}

//	protected JPanel buildProtegePanel(MultiEntityBayesianNetwork mebn,
//			IMEBNMediator mediator) {
//		
//		
//		try {
//			FrameworkFactory osgifactory = new FrameworkFactory();
//			Framework framework = osgifactory.newFramework(this.getLaunchProperties());
//			try {
//				framework.start();
//			} catch (BundleException e1) {
//				throw new IllegalStateException("Could not start OSGI.", e1);
//			}
//			BundleContext context = framework.getBundleContext();
//			
////			Activator activator = new Activator();
////			activator.start(context);
////			
////			ProtegeApplication application = new ProtegeApplication();
////			application.start(context);
//			
//			
////			ProtegeApplication protege = new ProtegeApplication();
////			protege.start(context);
////			
////			for (EditorKit editorKit : ProtegeManager.getInstance().getEditorKitManager().getEditorKits()) {
////				try {
////					if (OWLEditorKit.ID.equals(editorKit.getId())) {
////						JFrame frame = ProtegeManager.getInstance().getFrame(editorKit.getWorkspace());
////						JPanel ret = new JPanel();
////						ret.add(frame.getContentPane());
////						frame.setVisible(false);
////						return ret;
////					}
////				} catch (Throwable e) {
////					e.printStackTrace();
////					try {
////						Debug.println(this.getClass(), "Could not extract frame from protege editor kit " + editorKit);
////					} catch (Throwable t) {
////						t.printStackTrace();
////					}
////					continue;
////				}
////			}
//			
//		} catch (Throwable t) {
//			t.printStackTrace();
//		}
//		
//		return null;
//	}

	/**
	 * Uses bundle to extract {@link OWLWorkspace}.
	 * It uses reflection, because the classes loaded by OSGi differs from classes loaded by native (JPF) classLoader (even though the name matches)...
	 * @param bundle : if null, it assumes that mebn is carrying a reference to {@link OWLModelManager} through {@link MultiEntityBayesianNetwork#getStorageImplementor()}
	 * @param mebn : the model classes
	 * @param mediator : gives access to MEBN GUI, I/O and model classes
	 * @return instance of  {@link OWLWorkspace} or null if it could not be extracted.
	 * @throws ClassNotFoundException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 * @throws SecurityException 
	 * @throws IllegalArgumentException 
	 * @throws OWLOntologyCreationException 
	 */
	protected Component extractWorkspace(Bundle bundle, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) throws ClassNotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, OWLOntologyCreationException {
		
		// obtain OWL editor kit
		try {
			EditorKit kit = this.extractOWLEditorKit(bundle, mebn, mediator);
			if (kit != null) {
				// extract frame using protege manager (singleton).
				// We assume if kit != null then ProtegeManager was called previously and it is a singleton
				JFrame workspaceFrame = ((ProtegeManager)this.extractProtegeManager(bundle, mebn, mediator)).getFrame(kit.getWorkspace());
				Component view = workspaceFrame.getContentPane();
				
				// extract menu
				JMenuBar menuBar = workspaceFrame.getJMenuBar();
				
				// remove some undesired menu items
				this.removeUndesiredMenuItemFromMenuBar(menuBar, bundle, mebn, mediator);
				
				// create a panel containing both workspace and the menu bar
				JPanel workspacePanel = new JPanel(new BorderLayout());
				workspacePanel.add(menuBar, BorderLayout.NORTH);
				workspacePanel.add(kit.getWorkspace(), BorderLayout.CENTER);
				
				// hide the original frame
				workspaceFrame.setVisible(false);
				// remove view from the protege workspace
				workspaceFrame.remove(view);
				workspaceFrame.setContentPane(new JPanel());	// use a stub JPanel because null is an invalid content pane
				
				// hide frame
//			manager.getEditorKitManager().getWorkspaceManager().removeWorkspace(kit.getWorkspace());
				workspaceFrame.dispose();
				
//				return kit.getWorkspace();
				return workspacePanel;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.err.println("Loading protege using complete (pure) reflection... You must manually reload MEBN in case you save an ontology using protege.");
		// TODO find out a better way to extract workspace without reflection
		
		// this method should return a protege manager, no matter by reflection or not.
		Object protegeManager = this.extractProtegeManager(bundle, mebn, mediator);
		
//		EditorKitManager editorKitManager = protegeManager.getEditorKitManager();
		Object editorKitManager = protegeManager.getClass().getMethod("getEditorKitManager").invoke(protegeManager);
		
//		List<EditorKit> listEditorKits = editorKitManager.getEditorKits();
		List listEditorKits = (List)editorKitManager.getClass().getMethod("getEditorKits").invoke(editorKitManager);
		
				
//		Workspace workspace = listEditorKits.get(0).getWorkspace();
		Object workspace = bundle.loadClass(EditorKit.class.getName()).getMethod("getWorkspace").invoke(listEditorKits.get(0));

//		protegeManager.getFrame(workspace);
		JFrame workspaceFrame = (JFrame)protegeManager.getClass().getMethod("getFrame", bundle.loadClass(Workspace.class.getName())).invoke(protegeManager, workspace);
		
		// extract view
		Component view = workspaceFrame.getContentPane();
		
		// forcing size
//		Dimension size = new Dimension(600, 480);
//		view.setPreferredSize(size);
//		view.setSize(size);
//		workspaceFrame.setPreferredSize(size);
//		workspaceFrame.setSize(size);
//		workspaceFrame.pack();
		
		try {
			// finish the original frame
			workspaceFrame.remove(view);
			workspaceFrame.setContentPane(new JPanel());
			workspaceFrame.setVisible(false);
			workspaceFrame.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return (Component)workspace;
		
//		return view;
	}

	/**
	 * It removes some undesired menu items (e.g. the close operation) from menu bar
	 * @param menuBar : menu bar to be altered
	 * @param bundle : if null, it assumes that mebn is carrying a reference to {@link OWLModelManager} through {@link MultiEntityBayesianNetwork#getStorageImplementor()}
	 * @param mebn : the model classes
	 * @param mediator : gives access to MEBN GUI, I/O and model classes
	 */
	protected void removeUndesiredMenuItemFromMenuBar(JMenuBar menuBar, Bundle bundle, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		for (int i = 0; i < menuBar.getMenuCount(); i++) {
			try {
				if (menuBar.getMenu(i).getText().equalsIgnoreCase("File")) {
					// remove New..., Open... Open recent, Open from URL..., separator
					menuBar.getMenu(i).remove(0);
					menuBar.getMenu(i).remove(0);
					menuBar.getMenu(i).remove(0);
					menuBar.getMenu(i).remove(0);
					menuBar.getMenu(i).remove(0);
					// remove separator and close
					menuBar.getMenu(i).remove(10);
					menuBar.getMenu(i).remove(10);
					// remove exit and separator
					menuBar.getMenu(i).remove(menuBar.getMenu(i).getPopupMenu().getComponentCount() - 1);
					menuBar.getMenu(i).remove(menuBar.getMenu(i).getPopupMenu().getComponentCount() - 1);
				} else if (menuBar.getMenu(i).getText().equalsIgnoreCase("Reasoner")) {
					// remove all but
					menuBar.getMenu(i).removeAll();
					// add synchronize current reasoner
					this.setSynchronizeReasonerMenuItem(new JMenuItem("Synchronize Reasoner"));
					menuBar.getMenu(i).add(this.getSynchronizeReasonerMenuItem());
					
					// prepare the parameters for the action listener (they are variables marked as "final")
					final Bundle bundleAux = bundle;
					final MultiEntityBayesianNetwork mebnAux = mebn;
					final IMEBNMediator mediatorAux = mediator;
					
					this.getSynchronizeReasonerMenuItem().addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							try {
								EditorKit kit = extractOWLEditorKit(bundleAux, mebnAux, mediatorAux);
								if (kit instanceof OWLEditorKit) {
									((OWLEditorKit)kit).getModelManager().getOWLReasonerManager().classifyAsynchronously(((OWLEditorKit)kit).getModelManager().getReasonerPreferences().getPrecomputedInferences());
								}
							} catch (Exception e2) {
								e2.printStackTrace();
								JOptionPane.showMessageDialog(Protege41EntityPanelBuilder.this, e2.getMessage(), "Protégé Error", JOptionPane.ERROR_MESSAGE);
							}
						}
					});
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}



	/**
	 * This method is used in {@link #extractWorkspace(Bundle, MultiEntityBayesianNetwork, IMEBNMediator)}
	 * in order to obtain the owlEditorKit carring a protege-owl workspace
	 * @param bundle
	 * @param mebn
	 * @param mediator
	 * @return
	 */
	protected EditorKit extractOWLEditorKit(Bundle bundle, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		// if mebn is carring the kit, reuse it
		if (mebn != null
				&& mebn.getStorageImplementor() != null
				&& (mebn.getStorageImplementor() instanceof IProtegeStorageImplementorDecorator)
				&& ((IProtegeStorageImplementorDecorator)mebn.getStorageImplementor()) != null
				&& ((IProtegeStorageImplementorDecorator)mebn.getStorageImplementor()).getOWLEditorKit() != null) {
			try {
				Debug.println(this.getClass(), "MEBN is already carring an editor kit: " + ((IProtegeStorageImplementorDecorator)mebn.getStorageImplementor()).getOWLEditorKit());
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return ((IProtegeStorageImplementorDecorator)mebn.getStorageImplementor()).getOWLEditorKit();
		}
		
		// if mebn does not carry an editor kit, use protege manager to extract it
		try {
			Debug.println(this.getClass(), "MEBN is not carring an editor kit. Let's create a new one or extract it from protege's manager.");
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			// We assume the classloaders have loaded compatible classes (the parent classloader of JPF and OSGi were configured to a compatible one)
			
			// ProtegeManager is a singleton if JPF and OSGi classloaders are correctly configured to reuse the same class
			ProtegeManager manager = (ProtegeManager)this.extractProtegeManager(bundle, mebn, mediator);
			
			boolean isOntologyLoaded = false;
			if (manager.getEditorKitManager().getEditorKitCount() <= 0) {
				try {
					Debug.println(this.getClass(), "Could not open Protege editor kit using bundle launcher's default ontology URI = " + this.getProtegeBundleLauncher().getDefaultOntolgyURI()
							+ ". Retry calling ProtegeApplication#editURI(" + this.extractURIFromMEBN(mebn) + ") instead.");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				// Open ontology (and editor kit) using file. An editor kit will be created "automagically". 
				try {
					manager.getApplication().editURI(this.extractURIFromMEBN(mebn));
				} catch (Exception e) {
					try {
						Debug.println(this.getClass(), "Could not load ontology for mebn " + mebn, e);
					} catch (Throwable t) {
						t.printStackTrace();
					}
				}
				// wait for a while
				try {
					Thread.sleep(1000);
				} catch (Throwable t) {
					t.printStackTrace();
					// do not interrupt execution just because a waiting time was interrupted
				}
				isOntologyLoaded = true;
			} else {
				// ontology was loaded previously using System.getProperties() - simulating a protege's command line argument
				try {
					Debug.println(this.getClass(), "There were " + manager.getEditorKitManager().getEditorKitCount() + " previously loaded protege's editor kits.");
				} catch (Throwable t) {
					t.printStackTrace();
				}
				isOntologyLoaded = true;
			}
			// extract kit if it is already loaded
			if (isOntologyLoaded && (manager.getEditorKitManager().getEditorKitCount() > 0)) {
				// use the last opened kit.
				EditorKit kit = manager.getEditorKitManager().getEditorKits().get(manager.getEditorKitManager().getEditorKitCount() - 1);
				
				// update the owl model carried by mebn if it was not previously loaded
				if (kit instanceof OWLEditorKit) {
					// if mebn was not loaded by protege, update it.
					mebn.setStorageImplementor(ProtegeStorageImplementorDecorator.newInstance((OWLEditorKit)kit));
				} else {
					System.err.println(mebn + ": a new editor kit was extracted, but could not update MEBN's storage implementor. This may cause unsynchronized ontology (the ontology handled by protege's panel and UnBBayes' panel may not be the same).");
				}
				return kit;
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * This method extracts a protege manager from bundle.
	 * Since objects extracted from bundle may use imcompatible classloaders, it
	 * returns an Object instead of {@link ProtegeManager}.
	 * @param bundle
	 * @param mebn
	 * @param mediator
	 * @return the protege manager or null if it could not be extracted.
	 */
	protected Object extractProtegeManager(Bundle bundle, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
		try {
			if (bundle != null) {
				Debug.println(this.getClass(), "Extracting protege manager using OSGi class loader. Bundle = " + bundle.getSymbolicName());
				// ProtegeManager protegeManager = ProtegeManager.getInstance() 
				return (bundle.loadClass(ProtegeManager.class.getName()).getMethod("getInstance").invoke(null));
			} else {
				Debug.println(this.getClass(), "Extracting protege manager as a singleton using application class loader.");
				return ProtegeManager.getInstance();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Extract the Ontology's URI from MEBN. This is called in {@link #buildPanel(MultiEntityBayesianNetwork, IMEBNMediator)}
	 * @param mebn
	 * @return the URI (or null if not found).
	 */
	protected URI extractURIFromMEBN(MultiEntityBayesianNetwork mebn) {
		URI ret = null;
		try {
			ret = ((IOWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee().getOWLOntologyManager().getOntologyDocumentIRI(((IOWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee()).toURI();
		} catch (Exception e) {
			try {
				Debug.println(this.getClass(), "Impossible to extract URI from " + mebn, e);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return ret;
	}

	// migrated to ProtegeBundleLauncher
//	/**
//	 * This method is called in {@link #startProtegeBundles()} in order to force the initialization of ProtegeOWL bundles.
//	 * Overwrite this method if the initialization should be performed in another way (or if it should not be initialized at all).
//	 * @param context : the same context of {@link  org.protege.editor.core.ProtegeApplication}
//	 */
//	protected void startProtegeOWL(BundleContext context) {
//		
//		// force initialization of protege owl context
//    	ProtegeOWL protegeOWLApplication = new ProtegeOWL();
//    	try {
//    		// context is handled statically by ProtegeOWL
//			protegeOWLApplication.start(context); // this is a stub that only sets the context (it does not actually start an application)
//		} catch (Exception e) {
//			Debug.println(this.getClass(), "Could not start " + ProtegeOWL.class.toString());
//			e.printStackTrace();
//		}	
//		
//		// force initialization of protege's plug-in context (just in order to avoid some bugs). 
//    	try {
//    		PluginUtilities.getInstance().initialise(context); // singleton
//    	} catch (Exception e) {
//    		Debug.println(this.getClass(), "Could not start " + PluginUtilities.class.toString() + ", but the Protege will probably do it anyway (so, let's keep going on)", e);
//		}
//	}

//	/**
//	 * Startup the core bundles of protege using those from {@link #getProtegeCoreBundleNames()}
//	 * It also fills {@link #getLoadedCoreBundles()}.
//	 * This is necessary because Protege 4.1 relies heavily on OSGi and cannot be used as ordinal API.
//	 * @throws BundleException
//	 * @throws IOException
//	 * @throws IllegalAccessException 
//	 * @throws InstantiationException 
//	 */
//    protected void startProtegeBundles() throws BundleException, IOException, InstantiationException, IllegalAccessException {
//    	
//    	// try starting OSGI using apache felix (it comes with protege) to create protege's bundle (plug-in) context
//		FrameworkFactory osgifactory = new FrameworkFactory();
//		Framework framework = osgifactory.newFramework(this.getLaunchProperties());
//		try {
//			framework.start();
//		} catch (BundleException e1) {
//			throw new IllegalStateException("Could not start OSGI.", e1);
//		}
//		
//		BundleContext context = framework.getBundleContext();
//    	
//    	// start loading protege's core bundles (this is a fixed set)
//    	List<Bundle> core = new ArrayList<Bundle>();	// the loaded protege's core bundles
//    	for (String bundleName :  this.getProtegeCoreBundleNames()) {
//    		boolean success = false;
//    		try {
//    			// TODO use a more sophisticated way to concatenate the directory and bundle name
//    			String bundleDir = this.getProtegeBundleDir();
//    			if (!bundleDir.endsWith("/")) {
//    				bundleDir += "/";
//    			}
//    			URL bundleURL = this.getClass().getClassLoader().getResource(bundleDir + bundleName);
//    			if (bundleURL == null || bundleURL.toString() == null || bundleURL.toString().length() <= 0) {
//    				throw new IllegalArgumentException("Could not find a bundle in " + bundleDir + bundleName + ". This happens when some resources were not correclty included to the distributed plug-in.");
//    			}
//    			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(bundleDir + bundleName);
//    			core.add(context.installBundle(bundleURL.getFile(), inputStream));
//    			success = true;
//    		}
//    		finally {
//    			if (!success) {
//    				System.err.println("Protege's Core Bundle " + bundleName + " failed to install.");
//    			} else {
//    				Debug.println(this.getClass(), "Bundle " + bundleName + " installed.");
//    			}
//    		}
//    	}
//    	// start installed
//    	for (Bundle b : core) {
//    		boolean success = false;
//    		try {
//    			if (b.getState() == b.INSTALLED || b.getState() == b.RESOLVED) {
//    				b.start();
//    				success = true;
//    			}
//    		}
//    		finally {
//    			if (!success) {
//    				System.err.println("Protege's Core Bundle " + ((b!=null)?(b.getSymbolicName()):"null") + " failed to start (there was an error or bundle could be already running).");
//    			} else {
//    				Debug.println(this.getClass(), "Bundle " + b.getSymbolicName() + " started.");
//    			}
//    		}
//    	}
//    	this.getLoadedCoreBundles().addAll(core);
//    }

	// migrated to ProtegeBundleLauncher
//    /**
//     * Values in {@link #SYSTEMPROPERTIES} will be added to {@link System#setProperty(String, String)}.
//     * Overwrite this method in order to use other values.
//     */
//    protected void initializeSystemProperties(MultiEntityBayesianNetwork mebn) {
//    	
//		// set static properties (fixed values)
//    	for (int i = 0; i < SYSTEMPROPERTIES.length; i++) {
//    		System.setProperty(SYSTEMPROPERTIES[i][0], SYSTEMPROPERTIES[i][1]);
//    	}
//    	
//    	// set dynamic properties (resolved values)
//    	
//    	// force protege to look for a different directory for plugins
//    	System.setProperty("org.protege.plugin.dir", this.getProtegePluginDir());
//    	
//    	URI uri = this.extractURIFromMEBN(mebn);
//    	if (uri != null) {
//    		// simulate command line argument to protege
//			System.setProperty(PlatformArguments.ARG_PROPERTY + 0, uri.toString());
//		}
//    	
//    	
//    }

	/**
	 * This is the properties that initializes some important values (e.g. protege's osgi configurations)
	 * @return the launchProperties
	 */
	public Properties getLaunchProperties() {
		return launchProperties;
	}

	/**
	 * This is the properties that initializes some important values (e.g. protege's osgi configurations)
	 * @param launchProperties the launchProperties to set
	 */
	public void setLaunchProperties(Properties launchProperties) {
		this.launchProperties = launchProperties;
	}

	// migrated to ProtegeBundleLauncher
//	/**
//	 * Path to protege's bundles directory
//	 * @return the protegeBundleDir
//	 */
//	public String getProtegeBundleDir() {
//		if (protegeBundleDir == null || protegeBundleDir.length() <= 0) {
//			// search for resources (this is to solve JPF directory hierarchy using JPF classloaders)
////			try {
////				// We are using the File class because protege seems not to understand URL-normalized formats (i.e. those using '%')
////				// TODO ask protege developers to stop using java.io.File and start using this.getClass().getClassLoader().getResource instead, 
////				protegeBundleDir = new File(this.getClass().getClassLoader().getResource(DEFAULTPROTEGEBUNDLEDIR).toURI()).getCanonicalPath();
////			} catch (Throwable e) {
////				e.printStackTrace();
////				// use default value instead
////				protegeBundleDir = DEFAULTPROTEGEBUNDLEDIR;
////			}
//			protegeBundleDir = DEFAULTPROTEGEBUNDLEDIR;
//		}
//		return protegeBundleDir;
//	}

	// migrated to ProtegeBundleLauncher
//	/**
//	 * Path to protege's bundles directory
//	 * @param protegeBundleDir the protegeBundleDir to set
//	 */
//	public void setProtegeBundleDir(String protegeBundleDir) {
//		this.protegeBundleDir = protegeBundleDir;
//	}

	// migrated to ProtegeBundleLauncher
//	/**
//	 * Path to protege's plugin directory
//	 * @return the protegePluginDir
//	 */
//	public String getProtegePluginDir() {
//		if (protegePluginDir == null || protegePluginDir.length() <= 0) {
//			// search for resources (this is to solve JPF directory hierarchy using JPF classloaders)
//			try {
//				// We are using the File class because protege seems not to understand URL-normalized formats (i.e. those using '%')
//				// TODO ask protege developers to stop using java.io.File and start using this.getClass().getClassLoader().getResource instead, 
//				protegePluginDir = new File(this.getClass().getClassLoader().getResource(DEFAULTPROTEGEPLUGINDIR).toURI()).getCanonicalPath();
//			} catch (Throwable e) {
//				e.printStackTrace();
//				// use default value instead
//				protegePluginDir = DEFAULTPROTEGEPLUGINDIR;
//			}
//		}
//		return protegePluginDir;
//	}

	// migrated to ProtegeBundleLauncher
//	/**
//	 * Path to protege's plugin directory
//	 * @param protegePluginDir the protegePluginDir to set
//	 */
//	public void setProtegePluginDir(String protegePluginDir) {
//		this.protegePluginDir = protegePluginDir;
//	}

	// migrated to ProtegeBundleLauncher
//	/**
//	 * @return the protegeCoreBundleNames
//	 */
//	public String[] getProtegeCoreBundleNames() {
//		return protegeCoreBundleNames;
//	}
	
	// migrated to ProtegeBundleLauncher
//	/**
//	 * @param protegeCoreBundleNames the protegeCoreBundleNames to set
//	 */
//	public void setProtegeCoreBundleNames(String[] protegeCoreBundleNames) {
//		this.protegeCoreBundleNames = protegeCoreBundleNames;
//	}

	// migrated to ProtegeBundleLauncher
//	/**
//	 * @return the loadedCoreBundles
//	 */
//	public Collection<Bundle> getLoadedCoreBundles() {
//		return loadedCoreBundles;
//	}

	// migrated to ProtegeBundleLauncher
//	/**
//	 * @param loadedCoreBundles the loadedCoreBundles to set
//	 */
//	public void setLoadedCoreBundles(Collection<Bundle> loadedCoreBundles) {
//		this.loadedCoreBundles = loadedCoreBundles;
//	}



	/**
	 * This launcher is responsible for starting protege 4.1 as osgi bundles.
	 * It will lazily instantiate the protegeBundleLauncher if it is null.
	 * @return the non-null protegeBundleLauncher
	 */
	public IBundleLauncher getProtegeBundleLauncher() {
		if (protegeBundleLauncher == null) {
			protegeBundleLauncher = ProtegeBundleLauncher.getInstance();
		}
		return protegeBundleLauncher;
	}



	/**
	 * This launcher is responsible for starting protege 4.1 as osgi bundles
	 * @param protegeBundleLauncher the protegeBundleLauncher to set
	 */
	public void setProtegeBundleLauncher(IBundleLauncher protegeBundleLauncher) {
		this.protegeBundleLauncher = protegeBundleLauncher;
	}



//	/**
//	 * Creates a component containing a set of buttons (e.g. tool bar)
//	 * to configure useful protege properties.
//	 * @param bundle
//	 * @param mebn
//	 * @param mediator
//	 * @return
//	 */
//	protected JComponent buildProtegeTools(Bundle bundle, MultiEntityBayesianNetwork mebn, IMEBNMediator mediator) {
//		final JToolBar protegeMenu = new JToolBar("Protégé", JToolBar.HORIZONTAL);
//		
//		final Bundle bundleAux = bundle;
//		final MultiEntityBayesianNetwork mebnAux = mebn;
//		final IMEBNMediator mediatorAux = mediator;
//		// create button to synchronize reasoner
//		this.setSynchronizeReasonerButton(new JButton(IconController.getInstance().getPropagateIcon()));
//		this.getSynchronizeReasonerButton().setToolTipText("Synchronize Reasoner");
//		
//		// add listener to button
//		this.getSynchronizeReasonerButton().addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				try {
//					EditorKit kit = extractOWLEditorKit(bundleAux, mebnAux, mediatorAux);
//					if (kit instanceof OWLEditorKit) {
//						((OWLEditorKit)kit).getModelManager().getOWLReasonerManager().classifyAsynchronously(((OWLEditorKit)kit).getModelManager().getReasonerPreferences().getPrecomputedInferences());
//					}
//				} catch (Exception e2) {
//					e2.printStackTrace();
//					JOptionPane.showMessageDialog(protegeMenu, e2.getMessage(), "Protégé Error", JOptionPane.ERROR_MESSAGE);
//				}
//			}
//		});
//		
//		// add save button
//		this.setSaveProtegeOntologyButton(new JButton(IconController.getInstance().getSaveFindingsInstance()));
//		this.getSaveProtegeOntologyButton().setToolTipText("Save");
//		
//		// add listener for save button
//		this.getSaveProtegeOntologyButton().addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				try {
//					if (mebnAux != null
//							&& mebnAux.getStorageImplementor() != null
//							&& (mebnAux.getStorageImplementor() instanceof IOWLAPIStorageImplementorDecorator)) {
//						((IOWLAPIStorageImplementorDecorator)mebnAux.getStorageImplementor()).execute();
//					}
//				} catch (Exception e2) {
//					e2.printStackTrace();
//					JOptionPane.showMessageDialog(protegeMenu, e2.getMessage(), "Protégé Error", JOptionPane.ERROR_MESSAGE);
//				}
//			}
//		});
//		
//		
//		protegeMenu.add(this.getSynchronizeReasonerButton());
//		protegeMenu.add(this.getSaveProtegeOntologyButton());
//		
//		return protegeMenu;
//	}


//	/**
//	 * @return the synchronizeReasonerButton
//	 */
//	public JButton getSynchronizeReasonerButton() {
//		return synchronizeReasonerButton;
//	}
//
//
//
//	/**
//	 * @param synchronizeReasonerButton the synchronizeReasonerButton to set
//	 */
//	public void setSynchronizeReasonerButton(JButton synchronizeReasonerButton) {
//		this.synchronizeReasonerButton = synchronizeReasonerButton;
//	}
//
//
//
//	/**
//	 * @return the saveProtegeOntologyButton
//	 */
//	public JButton getSaveProtegeOntologyButton() {
//		return saveProtegeOntologyButton;
//	}



//	/**
//	 * @param saveProtegeOntologyButton the saveProtegeOntologyButton to set
//	 */
//	public void setSaveProtegeOntologyButton(JButton saveProtegeOntologyButton) {
//		this.saveProtegeOntologyButton = saveProtegeOntologyButton;
//	}



	/**
	 * @return the synchronizeReasonerMenuItem
	 */
	public JMenuItem getSynchronizeReasonerMenuItem() {
		return synchronizeReasonerMenuItem;
	}



	/**
	 * @param synchronizeReasonerMenuItem the synchronizeReasonerMenuItem to set
	 */
	public void setSynchronizeReasonerMenuItem(JMenuItem synchronizeReasonerMenuItem) {
		this.synchronizeReasonerMenuItem = synchronizeReasonerMenuItem;
	}


}
