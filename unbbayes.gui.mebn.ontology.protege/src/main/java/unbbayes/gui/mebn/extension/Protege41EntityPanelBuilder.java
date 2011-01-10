/**
 * 
 */
package unbbayes.gui.mebn.extension;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.apache.felix.framework.FrameworkFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.protege.editor.core.ProtegeApplication;
import org.protege.editor.core.ProtegeManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.platform.PlatformArguments;
import org.protege.editor.core.ui.workspace.Workspace;
import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import unbbayes.controller.mebn.IMEBNMediator;
import unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder;
import unbbayes.io.mebn.OWLAPIStorageImplementorDecorator;
import unbbayes.prs.mebn.MultiEntityBayesianNetwork;
import unbbayes.util.Debug;

/**
 * A builder for a tab showing the protege's OWL property editor.
 * 
 * @author Shou Matsumoto
 *
 */
public class Protege41EntityPanelBuilder extends JPanel implements IMEBNEditionPanelBuilder {
	
	/** Default place where protege's core bundles will be searched */
	public static final String DEFAULTPROTEGEBUNDLEDIR = "protege/bundles";
	
	/** Defaut place where protege's plugins will be searched (it is set in {@link #initializeSystemProperties()}) */
	public static final String DEFAULTPROTEGEPLUGINDIR = "protege/plugins";
	
	private String protegeBundleDir = null;
	private String protegePluginDir = null;
	
	private String[] protegeCoreBundleNames = PROTEGECOREBUNDLES;
	
	public Collection<Bundle> loadedCoreBundles = new HashSet<Bundle>();
	

	private Properties launchProperties = new Properties();

	private Component view;
	
	/** These values are used to fill {@link System#setProperty(String, String)} in {@link #initializeSystemProperties()} */
	public static final String[][] SYSTEMPROPERTIES = {
        {"file.encoding", "utf-8"},
        {"apple.laf.useScreenMenuBar", "true"},
        {"com.apple.mrj.application.growbox.intrudes", "true"},
		{"swing.defaultlaf", UIManager.getSystemLookAndFeelClassName()},
		{"javax.xml.transform.TransformerFactory","unbbayes.prs.mebn.ontology.protege.UnBBayesTransformerFactory"}	// force Xalan to use this factory (which is a workaround to solve the "indent-number" bug)
    };

	/** This is the name of the bundle carring the {@link org.protege.editor.core.ProtegeApplication} */
	public static final String PROTEGEAPPLICATIONBUNDLENAME = "org.protege.editor.core.application.jar";
	
    /** These are default values of {@link #getProtegeCoreBundleNames()} */
    public static final String[] PROTEGECOREBUNDLES = {
        "org.protege.common.jar",
        "org.eclipse.equinox.common.jar",
        "org.eclipse.equinox.registry.jar",
        "org.eclipse.equinox.supplement.jar",
        "org.protege.jaxb.jar",
        PROTEGEAPPLICATIONBUNDLENAME
    };
    
    /** These are some known values of {@link #getLaunchProperties()}. These values will be added to {@link #getLaunchProperties()} in {@link #initializeLaunchProperties()} */
    public static final String[][] PROTEGEFRAMEWORKPROPERTIES = { 
    	{Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_APP}, // who is the parent classloader
//        {Constants.FRAMEWORK_BOOTDELEGATION, "sun.*,com.sun.*,apple.*,com.apple.*"}, 
//        {Constants.FRAMEWORK_BOOTDELEGATION, "sun.*,com.sun.*,apple.*,com.apple.*,org.protege.editor.core,org.protege.editor.core.*,org.eclipse.core.internal.*"}, // what packages should be loaded by parent classloader
        {Constants.FRAMEWORK_BOOTDELEGATION, "*"}, // what packages should be loaded by parent classloader
        {Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "javax.xml.parsers,org.xml.sax,org.xml.sax.ext,org.xml.sax.helpers,org.apache.log4j"}, // packages automatically exported to OSGi by this application
        {Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT},
};
	
	/**
	 * Default constructor must be public to enable plugin support
	 */
	public Protege41EntityPanelBuilder() {
		super();
	}

	/**
	 * This method initializes the {@link #getLaunchProperties()}
	 * @see #getLaunchProperties()
	 * @see #setLaunchProperties(Properties)
	 */
	protected void initializeLaunchProperties() {
		this.setLaunchProperties(new Properties());	// reset
		
		// refill properties
		for (int i = 0; i < PROTEGEFRAMEWORKPROPERTIES.length; i++) {
			this.getLaunchProperties().setProperty(PROTEGEFRAMEWORKPROPERTIES[i][0], PROTEGEFRAMEWORKPROPERTIES[i][1]);
		}
		
		// add properties found in build.properties (in the original protege application)
		try {
			this.getLaunchProperties().setProperty("protege.osgi", new File(this.getClass().getClassLoader().getResource("protege/bin/felix.jar").toURI()).getCanonicalPath());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		File tempDir = new File("temp");
//		try {
//			this.getLaunchProperties().setProperty("org.osgi.framework.storage", tempDir.getCanonicalPath());
//		} catch (IOException e) {
//			throw new IllegalArgumentException("Could not initialize OSGI storage launch property",e);
//		}
//		tempDir.deleteOnExit();

//		try {
//			// use protege's temporary folders
//			File frameworkDir = new File(System.getProperty("java.io.tmpdir"), "ProtegeCache-" + UUID.randomUUID().toString());
//			frameworkDir.deleteOnExit();
//			this.getLaunchProperties().setProperty("org.osgi.framework.storage", frameworkDir.getCanonicalPath());
//		} catch (IOException e2) {
//			throw new IllegalStateException("Could not initialize temporary folder (ProtegeCache-*).", e2);
//		}
	}

	/*
	 * (non-Javadoc)
	 * @see unbbayes.gui.mebn.extension.editor.IMEBNEditionPanelBuilder#buildPanel(unbbayes.prs.mebn.MultiEntityBayesianNetwork, unbbayes.controller.mebn.IMEBNMediator)
	 */
	public JPanel buildPanel(MultiEntityBayesianNetwork mebn,
			IMEBNMediator mediator) {
		// we do not need this plugin if mebn is not bound to a project
		if (mebn == null || mebn.getStorageImplementor() == null ) {
			return null;
		}
		if (!(mebn.getStorageImplementor() instanceof OWLAPIStorageImplementorDecorator)
				|| ((OWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee() == null) {
			return null;
		}
		
		// from this line, we are doing workarounds in order to avoid protege bugs happening when OSGI is not initialized (e.g. protege plug-in errors)
		// TODO find out a more smart way
		this.initializeSystemProperties(mebn);
		
		// fill this.getLaunchProperties()
		this.initializeLaunchProperties();

//		try {
//			return this.buildProtegePanel(mebn, mediator);
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
		
		// try starting OSGI using apache felix (it comes with protege) to create protege's bundle (plug-in) context
		FrameworkFactory osgifactory = new FrameworkFactory();
		Framework framework = osgifactory.newFramework(this.getLaunchProperties());
		try {
			framework.start();
		} catch (BundleException e1) {
			throw new IllegalStateException("Could not start OSGI.", e1);
		}
		BundleContext context = framework.getBundleContext();
		
		try {
			// start protege bundles
			this.startProtegeBundles(context);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not initialize ProtegeApplication, but we can still try ProtegeOWL.");
		}
		

//		try {	
//			// force initialization of protege
//			this.startProtegeOWL(context);
//		} catch (Exception e) {
//			throw new IllegalStateException("Could not initialize ProtegeOWL.", e);
//		}
		
		// start protege as application
//		try {
//			ProtegeApplication protegeApplication = new ProtegeApplication();
//			protegeApplication.start(context);
//		} catch (Throwable e) {
//			e.printStackTrace();
//			return null;
//		}
		
		// look for protege bundle
		for (Bundle bundle : this.getLoadedCoreBundles()) {
			Debug.println(this.getClass(), bundle.getSymbolicName());
			if (ProtegeApplication.ID.equals(bundle.getSymbolicName())) {
				try {
					view = this.extractWorkspace(bundle, mebn, mediator);
					if (view != null) {
						// initialize size
						Dimension size = new Dimension(600,480);
						view.setPreferredSize(size);
						view.setSize(size);
						
						// prepare this component
//						this.setBorder(ComponentFactory.createTitledBorder(mebn.toString()));
						JScrollPane content = new JScrollPane(view);
						this.add(content);
						// listener on resize event
						this.addComponentListener(new ComponentListener() {
							public void componentResized(ComponentEvent e) {
								if (e.getComponent() != null) {
									// resize protege workspace if the panel is resized
									Dimension d = new Dimension(e.getComponent().getSize().width - 10, e.getComponent().getSize().height - 10);
									view.setSize(d);
									view.setPreferredSize(d);
									view.repaint();
								}
							}
							public void componentShown(ComponentEvent e) {}
							public void componentMoved(ComponentEvent e) {}
							public void componentHidden(ComponentEvent e) {}
						});
						return this;
					}
				} catch (Throwable e) {
					e.printStackTrace();
					Debug.println(this.getClass(), "Could not extract protege workspace.");
				}
			}
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
	 * @param bundle
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
		
		// ProtegeManager protegeManager = ProtegeManager.getInstance() 
		Object protegeManager = (bundle.loadClass(ProtegeManager.class.getName()).getMethod("getInstance").invoke(null));
		
		// first, let's try not using reflection
		try {
//			Debug.println("Re-starting Protege application using the same bundle context of " + bundle.getSymbolicName()); 
			
			if (protegeManager instanceof ProtegeManager) {
				// the classloaders have loaded compatible classes (the parent classloader of JPF and OSGi were configured to a compatible one)
				Debug.println("Reusing protege manager from " + bundle.getSymbolicName()); 
				
				ProtegeManager manager = (ProtegeManager)protegeManager;
				
				// this should initialize the singleton ProtegeManager
//				new ProtegeApplication().start(bundle.getBundleContext());
//				ProtegeManager manager = ProtegeManager.getInstance();

				try {
					if (manager.loadAndSetupEditorKitFromURI(manager.getEditorKitFactoryPlugins().get(0), this.extractURIFromMEBN(mebn))) {
						// extract the view component of the frame of the workspace
						EditorKit kit = manager.getEditorKitManager().getEditorKits().get(0);
						JFrame workspaceFrame = manager.getFrame(kit.getWorkspace());
						Component view = workspaceFrame.getContentPane();
						workspaceFrame.setVisible(false);
						// remove view from the protege workspace
						workspaceFrame.remove(view);
						workspaceFrame.setContentPane(new JPanel());
						// hide frame
//						manager.getEditorKitManager().getWorkspaceManager().removeWorkspace(kit.getWorkspace());
						workspaceFrame.dispose();
						
						return kit.getWorkspace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		
		Debug.println("Loading protege using complete (pure) reflection...");
		// TODO find out a better way to extract workspace without reflexion
		
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
	 * Extract the Ontology's URI from MEBN. This is called in {@link #buildPanel(MultiEntityBayesianNetwork, IMEBNMediator)}
	 * @param mebn
	 * @return the URI (or null if not found).
	 */
	protected URI extractURIFromMEBN(MultiEntityBayesianNetwork mebn) {
		URI ret = null;
		try {
			ret = ((OWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee().getOWLOntologyManager().getOntologyDocumentIRI(((OWLAPIStorageImplementorDecorator)mebn.getStorageImplementor()).getAdaptee()).toURI();
		} catch (Exception e) {
			try {
				Debug.println(this.getClass(), "Impossible to extract URI from " + mebn, e);
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return ret;
	}

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

	/**
	 * Startup the core bundles of protege using those from {@link #getProtegeCoreBundleNames()}
	 * It also fills {@link #getLoadedCoreBundles()}.
	 * This is necessary because Protege 4.1 relies heavily on OSGi and cannot be used as ordinal API.
	 * TODO find out a workaround in order for UnBBayes to use protege without using OSGi.
	 * @throws BundleException
	 * @throws IOException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
    protected void startProtegeBundles(BundleContext context) throws BundleException, IOException, InstantiationException, IllegalAccessException {
    	// use the same osgi factories from protege
//    	BufferedReader factoryReader = new BufferedReader(
//				new InputStreamReader(
//						getClass().getClassLoader().getResourceAsStream("META-INF/services/org.osgi.framework.launch.FrameworkFactory")));
//		String factoryClass = factoryReader.readLine();
//		factoryClass = factoryClass.trim();
//		factoryReader.close();
//    	FrameworkFactory factory = (FrameworkFactory) Class.forName(factoryClass).newInstance();
//    	Framework framework = factory.newFramework(this.getLaunchProperties());
//    	framework.start();
//    	BundleContext context = framework.getBundleContext();
    	
    	
    	// start loading protege's core bundles (this is a fixed set)
    	List<Bundle> core = new ArrayList<Bundle>();	// the loaded protege's core bundles
    	for (String bundleName :  this.getProtegeCoreBundleNames()) {
    		boolean success = false;
    		try {
    			// TODO use a more sophisticated way to concatenate the directory and bundle name
    			String bundleDir = this.getProtegeBundleDir();
    			if (!bundleDir.endsWith("/")) {
    				bundleDir += "/";
    			}
    			URL bundleURL = this.getClass().getClassLoader().getResource(bundleDir + bundleName);
    			if (bundleURL == null || bundleURL.toString() == null || bundleURL.toString().length() <= 0) {
    				throw new IllegalArgumentException("Could not find a bundle in " + bundleDir + bundleName + ". This happens when some resources were not correclty included to the distributed plug-in.");
    			}
    			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(bundleDir + bundleName);
    			core.add(context.installBundle(bundleURL.getFile(), inputStream));
    			success = true;
    		}
    		finally {
    			if (!success) {
    				System.err.println("Protege's Core Bundle " + bundleName + " failed to install.");
    			} else {
    				Debug.println(this.getClass(), "Bundle " + bundleName + " installed.");
    			}
    		}
    	}
    	for (Bundle b : core) {
    		boolean success = false;
    		try {
    			if (b.getState() == b.INSTALLED || b.getState() == b.RESOLVED) {
    				b.start();
    				success = true;
    			}
    		}
    		finally {
    			if (!success) {
    				System.err.println("Protege's Core Bundle " + ((b!=null)?(b.getSymbolicName()):"null") + " failed to start (there was an error or bundle could be already running).");
    			} else {
    				Debug.println(this.getClass(), "Bundle " + b.getSymbolicName() + " started.");
    			}
    		}
    	}
    	this.getLoadedCoreBundles().addAll(core);
    }

    /**
     * Values in {@link #SYSTEMPROPERTIES} will be added to {@link System#setProperty(String, String)}.
     * Overwrite this method in order to use other values.
     */
    protected void initializeSystemProperties(MultiEntityBayesianNetwork mebn) {
    	
		// set static properties (fixed values)
    	for (int i = 0; i < SYSTEMPROPERTIES.length; i++) {
    		System.setProperty(SYSTEMPROPERTIES[i][0], SYSTEMPROPERTIES[i][1]);
    	}
    	
    	// set dynamic properties (resolved values)
    	
    	// force protege to look for a different directory for plugins
    	System.setProperty("org.protege.plugin.dir", this.getProtegePluginDir());
    	
    	URI uri = this.extractURIFromMEBN(mebn);
    	if (uri != null) {
    		// simulate command line argument to protege
			System.setProperty(PlatformArguments.ARG_PROPERTY + 0, uri.toString());
		}
    	
    	
    }

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

	/**
	 * Path to protege's bundles directory
	 * @return the protegeBundleDir
	 */
	public String getProtegeBundleDir() {
		if (protegeBundleDir == null || protegeBundleDir.length() <= 0) {
			// search for resources (this is to solve JPF directory hierarchy using JPF classloaders)
//			try {
//				// We are using the File class because protege seems not to understand URL-normalized formats (i.e. those using '%')
//				// TODO ask protege developers to stop using java.io.File and start using this.getClass().getClassLoader().getResource instead, 
//				protegeBundleDir = new File(this.getClass().getClassLoader().getResource(DEFAULTPROTEGEBUNDLEDIR).toURI()).getCanonicalPath();
//			} catch (Throwable e) {
//				e.printStackTrace();
//				// use default value instead
//				protegeBundleDir = DEFAULTPROTEGEBUNDLEDIR;
//			}
			protegeBundleDir = DEFAULTPROTEGEBUNDLEDIR;
		}
		return protegeBundleDir;
	}

	/**
	 * Path to protege's bundles directory
	 * @param protegeBundleDir the protegeBundleDir to set
	 */
	public void setProtegeBundleDir(String protegeBundleDir) {
		this.protegeBundleDir = protegeBundleDir;
	}

	/**
	 * Path to protege's plugin directory
	 * @return the protegePluginDir
	 */
	public String getProtegePluginDir() {
		if (protegePluginDir == null || protegePluginDir.length() <= 0) {
			// search for resources (this is to solve JPF directory hierarchy using JPF classloaders)
			try {
				// We are using the File class because protege seems not to understand URL-normalized formats (i.e. those using '%')
				// TODO ask protege developers to stop using java.io.File and start using this.getClass().getClassLoader().getResource instead, 
				protegePluginDir = new File(this.getClass().getClassLoader().getResource(DEFAULTPROTEGEPLUGINDIR).toURI()).getCanonicalPath();
			} catch (Throwable e) {
				e.printStackTrace();
				// use default value instead
				protegePluginDir = DEFAULTPROTEGEPLUGINDIR;
			}
		}
		return protegePluginDir;
	}

	/**
	 * Path to protege's plugin directory
	 * @param protegePluginDir the protegePluginDir to set
	 */
	public void setProtegePluginDir(String protegePluginDir) {
		this.protegePluginDir = protegePluginDir;
	}

	/**
	 * @return the protegeCoreBundleNames
	 */
	public String[] getProtegeCoreBundleNames() {
		return protegeCoreBundleNames;
	}

	/**
	 * @param protegeCoreBundleNames the protegeCoreBundleNames to set
	 */
	public void setProtegeCoreBundleNames(String[] protegeCoreBundleNames) {
		this.protegeCoreBundleNames = protegeCoreBundleNames;
	}

	/**
	 * @return the loadedCoreBundles
	 */
	public Collection<Bundle> getLoadedCoreBundles() {
		return loadedCoreBundles;
	}

	/**
	 * @param loadedCoreBundles the loadedCoreBundles to set
	 */
	public void setLoadedCoreBundles(Collection<Bundle> loadedCoreBundles) {
		this.loadedCoreBundles = loadedCoreBundles;
	}

}
