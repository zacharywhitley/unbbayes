/**
 * 
 */
package unbbayes.prs.mebn.ontology.protege;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
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
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;

import unbbayes.util.Debug;

/**
 * This class starts up protege 4.1 application using OSGi configurations.
 * @author Shou Matsumoto
 *
 */
public class ProtegeBundleLauncher implements IBundleLauncher {
	
	/** Default place where protege's core bundles will be searched */
	public static final String DEFAULTPROTEGEBUNDLEDIR = "protege/bundles";
	
	/** Defaut place where protege's plugins will be searched (it is set in {@link #initializeSystemProperties()}) */
	public static final String DEFAULTPROTEGEPLUGINDIR = "protege/plugins";
	
	/** These values are used to fill {@link System#setProperty(String, String)} in {@link #initializeSystemProperties()} */
	public static final String[][] SYSTEMPROPERTIES = {
        {"file.encoding", "utf-8"},
        {"apple.laf.useScreenMenuBar", "true"},
        {"com.apple.mrj.application.growbox.intrudes", "true"},
		{"swing.defaultlaf", UIManager.getSystemLookAndFeelClassName()},
//		{"javax.xml.transform.TransformerFactory",UnBBayesTransformerFactory.class.getName()}	// force Xalan to use this factory (which is a workaround to solve the "indent-number" bug)
    };

	/** This is the name of the bundle carring the {@link org.protege.editor.core.ProtegeApplication} */
	public static final String PROTEGEAPPLICATIONBUNDLENAME = "org.protege.editor.core.application.jar";
	
    
    /** These are some known values of {@link #getLaunchProperties()}. These values will be added to {@link #getLaunchProperties()} in {@link #initializeLaunchProperties()} */
    public static final String[][] PROTEGEFRAMEWORKPROPERTIES = { 
    	{Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK}, // who is the parent classloader
//        {Constants.FRAMEWORK_BOOTDELEGATION, "sun.*,com.sun.*,apple.*,com.apple.*"}, 
//        {Constants.FRAMEWORK_BOOTDELEGATION, "sun.*,com.sun.*,apple.*,com.apple.*,org.protege.editor.core,org.protege.editor.core.*,org.eclipse.core.internal.*"}, // what packages should be loaded by parent classloader
        {Constants.FRAMEWORK_BOOTDELEGATION, "org.protege.editor.*,org.protege.editor.core.*,org.protege.editor.owl.*,org.protege.editor.owl.model.*,*"}, // what packages should be loaded by parent classloader
        {Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "javax.xml.parsers,org.xml.sax,org.xml.sax.ext,org.xml.sax.helpers,org.apache.log4j,org.protege,org.protege.editor,org.protege.editor.core,org.protege.editor.owl,org.protege.editor.owl.model"}, // packages automatically exported to OSGi by this application
        {Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT}, 
    };

    private String[] coreBundleNames = {
        "org.protege.common.jar",
        "org.eclipse.equinox.common.jar",
        "org.eclipse.equinox.registry.jar",
        "org.eclipse.equinox.supplement.jar",
        "org.protege.jaxb.jar",
        PROTEGEAPPLICATIONBUNDLENAME
    };

	private URI ontologyURI;

	private Properties launchProperties;
	
	private Collection<Bundle> loadedBundles = new HashSet<Bundle>();

	private String pluginDir;

	private String bundleDir;

	private Framework framework;
	
	
	/** Holder for a singleton instance */
	private static final class SingletonHolder {
		public static final IBundleLauncher INSTANCE = new ProtegeBundleLauncher();
	}
	
	/**
	 * the default (empty) constructor is visible in order to allow inheritance.
	 * It will call {@link #initializeLaunchProperties()} and {@link #initializeSystemProperties()}.
	 * @deprecated use {@link #getInstance()} instead
	 */
	protected ProtegeBundleLauncher() {
		super();
		try {
			this.initializeSystemProperties();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			this.initializeLaunchProperties();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This is the default construction method.
	 * @return a singleton instance of ProtegeBundleLauncher
	 */
	public static IBundleLauncher getInstance() {
//		return SingletonHolder.INSTANCE;
		return new ProtegeBundleLauncher();
	}
	
	/**
     * Values in {@link #SYSTEMPROPERTIES} will be added to {@link System#setProperty(String, String)}.
     * Overwrite this method in order to use other values.
     */
    protected void initializeSystemProperties() {
    	
		// set static properties (fixed values)
    	for (int i = 0; i < SYSTEMPROPERTIES.length; i++) {
    		System.setProperty(SYSTEMPROPERTIES[i][0], SYSTEMPROPERTIES[i][1]);
    	}
    	
    	// set dynamic properties (resolved values)
    	
    	// force protege to look for a different directory for plugins
    	System.setProperty("org.protege.plugin.dir", this.getProtegePluginDir());
    	
    	if (this.getDefaultOntolgyURI() != null) {
    		// simulate command line argument to protege
			System.setProperty(PlatformArguments.ARG_PROPERTY + 0, this.getDefaultOntolgyURI().toString());
		}
    	
    	// verify if the Xalan's ident-number bug fix is set in system property. If not, set it
    	if (System.getProperty("javax.xml.transform.TransformerFactory") == null
    			|| System.getProperty("javax.xml.transform.TransformerFactory").trim().length() <= 0) {
    		try {
				Debug.println(this.getClass(), "The \"javax.xml.transform.TransformerFactory\" system property is not set. Forcing it to " + unbbayes.util.XalanIndentNumberBugFixer.class.getName());
			} catch (Throwable t) {
				t.printStackTrace();
			}
    		try {
    			System.setProperty("javax.xml.transform.TransformerFactory", unbbayes.util.XalanIndentNumberBugFixer.class.getName());
    		} catch (Throwable t) {
    			t.printStackTrace();
			}
    	}
    	
    	try {
			Debug.println(this.getClass(), "System properties initialized: " + System.getProperties());
		} catch (Throwable t) {
			t.printStackTrace();
		}
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
		Enumeration<URL> osgiImplEnum  = null;
		try {
			osgiImplEnum = this.getClass().getClassLoader().getResources("protege/bin/felix.jar");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		URI uriOfOSGIImplUsedByProtege = null;
		if (osgiImplEnum != null) {
			while(osgiImplEnum.hasMoreElements()) {
				try {
					URI tempURI = osgiImplEnum.nextElement().toURI();
					if (!tempURI.isOpaque()) {
						uriOfOSGIImplUsedByProtege = tempURI;
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		try {
			try{
				Debug.println(this.getClass(), "Using osgi implementation: " + uriOfOSGIImplUsedByProtege);
			} catch (Throwable t) {
				t.printStackTrace();
			}
			this.getLaunchProperties().setProperty("protege.osgi", new File(uriOfOSGIImplUsedByProtege).getCanonicalPath());
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

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#getDefaultOntolgyURI()
	 */
	public URI getDefaultOntolgyURI() {
		return ontologyURI;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#setDefaultOntolgyURI(java.net.URI)
	 */
	public void setDefaultOntolgyURI(URI ontologyURI) {
		this.ontologyURI = ontologyURI;
		// system properties has URI. Reload system properties again.
		this.initializeSystemProperties();
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#getLaunchProperties()
	 */
	public Properties getLaunchProperties() {
		return launchProperties;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#setLaunchProperties(java.util.Properties)
	 */
	public void setLaunchProperties(Properties launchProperties) {
		this.launchProperties = launchProperties;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#startProtegeBundles()
	 */
	public Collection<Bundle> startProtegeBundles() throws BundleException, IOException, InstantiationException, IllegalAccessException{
		
		// if framework is not set, set it
		if (this.getFramework() == null) {
			// try starting OSGI using apache felix (it comes with protege) to create protege's bundle (plug-in) context
			FrameworkFactory osgifactory = new FrameworkFactory();
			this.setFramework(osgifactory.newFramework(this.getLaunchProperties()));
			try {
				Debug.println(this.getClass(), "OSGI Launch properties: " + this.getLaunchProperties());
			} catch (Throwable t) {
				t.printStackTrace();
			}
			try {
				if (this.getFramework().getState() == this.getFramework().INSTALLED 
						|| this.getFramework().getState() == this.getFramework().RESOLVED ) {
					// because the framework is a singleton, we must start it only if it is not already started
					this.getFramework().start();
				}
			} catch (BundleException e1) {
				throw new IllegalStateException("Could not start OSGI.", e1);
			}
		}
		
		if (this.getFramework() == null) {
			throw new IllegalStateException("Could not obtain osgi framework.");
		}
		
		try {
			// force framework classloader to eager load some classes
			this.forceFrameworkToLoadClasses();
		} catch (ClassNotFoundException e) {
			Debug.println(this.getClass(),"Could not load some classes using framework's classloader. This may leed to unsynchronized classes (e.g. classes with same name but incompatible because of classloaders)" + e.getMessage(), e);
		}
		
		BundleContext context = this.getFramework().getBundleContext();
    	
    	// start loading protege's core bundles (this is a fixed set)
    	List<Bundle> core = new ArrayList<Bundle>();	// the loaded protege's core bundles
    	for (String bundleName :  this.getCoreBundleNames()) {
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
    	Set<Bundle> startedBundles = new HashSet<Bundle>(); // a set of initialized (started) bundles
    	// start installed
    	for (Bundle b : core) {
    		try {
    			if (b.getState() == b.INSTALLED || b.getState() == b.RESOLVED) {
    				b.start();
    				startedBundles.add(b);
    				try {
    					Debug.println(this.getClass(), "Bundle " + b.getSymbolicName() + " started.");
    				} catch (Throwable e) {
    					e.printStackTrace();
    				}
    			}
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    			System.err.println("Protege's Core Bundle " + ((b!=null)?(b.getSymbolicName()):"null") + " failed to start (there was an error or bundle could be already running).");
    		}
    	}
    	
    	// add only started bundles
    	this.getLoadedBundles().addAll(startedBundles);
    	
    	try {
    		// force the Protege GUI to be hidden immediately
    		this.hideProtegeGUI();
    	} catch (Throwable t) {
    		t.printStackTrace();
    		// ignore failure, because it will become hidden anyway after an ontology is loaded.
    	}
    	
    	return startedBundles;
	}

	/**
	 * Force osgi framework's classloader to load the following classes:
	 * org.protege.editor.core.ProtegeManager; org.protege.editor.core.editorkit.EditorKit; 
	 * org.protege.editor.core.ui.workspace.Workspace; org.protege.editor.owl.OWLEditorKit; 
	 * org.protege.editor.owl.model.OWLModelManager; import org.protege.editor.owl.model.OWLWorkspace;
	 * unbbayes.prs.mebn.ontology.protege.UnBBayesTransformerFactory;
	 * @throws ClassNotFoundException
	 */
	protected void forceFrameworkToLoadClasses() throws ClassNotFoundException{
		this.getFramework().getClass().getClassLoader().loadClass(ProtegeManager.class.getName());
		this.getFramework().getClass().getClassLoader().loadClass(EditorKit.class.getName());
		this.getFramework().getClass().getClassLoader().loadClass(Workspace.class.getName());
		this.getFramework().getClass().getClassLoader().loadClass(OWLEditorKit.class.getName());
		this.getFramework().getClass().getClassLoader().loadClass(OWLModelManager.class.getName());
		this.getFramework().getClass().getClassLoader().loadClass(OWLWorkspace.class.getName());
//		this.getFramework().getClass().getClassLoader().loadClass(UnBBayesTransformerFactory.class.getName());
	}

	/**
	 * Tries to hide the GUI of Protege application.
	 */
	public void hideProtegeGUI() {
		try {
			Debug.println(this.getClass(), "Attempting to hide protege application's GUI using reflection");
			// get to the running protege application from the singleton instance of ProtegeManager
			Object managerObject = this.getProtegeManager();
			Object protegeApplication = managerObject.getClass().getMethod("getApplication").invoke(managerObject);
			// once obtained the application, try to access the private field which is the welcome frame (this frame is displayed every time and it is becoming annoying)
			Field welcomeFrameField = protegeApplication.getClass().getDeclaredField("welcomeFrame");
			welcomeFrameField.setAccessible(true);	// enable access to field
			// hide and dispose the old frame
			Object welcomeFrame = welcomeFrameField.get(protegeApplication);
			if (welcomeFrame != null 
					&& welcomeFrame instanceof JFrame) {
				((JFrame)welcomeFrame).setVisible(false);
				((JFrame)welcomeFrame).dispose();
			}
			// replace it to a frame which is always hidden
			welcomeFrameField.set(protegeApplication, new JFrame() {
				public void setVisible(boolean b) {
					super.setVisible(false);	// it is always invisible!!
				}
				protected void frameInit() {
					super.frameInit();
					this.setVisible(false);
				}
			});
		} catch (Throwable t) {
			Debug.println(this.getClass(), "Could not hide Protege GUI using reflection", t);
		}
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#getProtegeBundle()
	 */
	public Bundle getProtegeBundle() {
		if (this.getLoadedBundles() != null) {
			for (Bundle bundle : this.getLoadedBundles()) {
				if (ProtegeApplication.ID.equals(bundle.getSymbolicName())) {
					return bundle;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#getProtegeManager()
	 */
	public Object getProtegeManager() {
		try {
			try{
				Debug.println(this.getClass(), "ProtegeManager class loader = " + ProtegeManager.class.getClassLoader());
				Debug.println(this.getClass(), "ProtegeManager class loader's parent = " + ProtegeManager.class.getClassLoader().getParent());
				Debug.println(this.getClass(), "This class loader = " + this.getClass().getClassLoader());
				Debug.println(this.getClass(), "This class loader's parent = " + this.getClass().getClassLoader().getParent());
			} catch (Throwable t) {
				t.printStackTrace();
			}
			if (this.getProtegeBundle() != null) {
				Debug.println(this.getClass(), "Extracting protege manager using OSGi class loader. Bundle = " + this.getProtegeBundle().getSymbolicName());
				// ProtegeManager protegeManager = ProtegeManager.getInstance() 
				
				Object ret = (this.getProtegeBundle().loadClass(ProtegeManager.class.getName()).getMethod("getInstance").invoke(null));
				try{
					Debug.println(this.getClass(), "Protege bundle's class loader = " + this.getProtegeBundle().getClass().getClassLoader());
					Debug.println(this.getClass(), "Parent class loader of Protege bundle's class loader = " + this.getProtegeBundle().getClass().getClassLoader().getParent());
					Debug.println(this.getClass(), "Class loader of ProtegeManager loaded from bundle = " + ret.getClass().getClassLoader());
					Debug.println(this.getClass(), "Parent class loader of ProtegeManager loaded from bundle = " + ret.getClass().getClassLoader().getParent());
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return ret;
			} else {
				Debug.println("Extracting protege manager as a singleton using application class loader. This may cause unsynchronized ontology.");
				return ProtegeManager.getInstance();
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
		Debug.println("Protege manager is null");
		return null;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#getProtegePluginDir()
	 */
	public String getProtegePluginDir() {
		if (pluginDir == null || pluginDir.length() <= 0) {
			// search for resources (this is to solve JPF directory hierarchy using JPF classloaders)
			try {
				// We are using the File class because protege seems not to understand URL-normalized formats (i.e. those using '%')
				// TODO ask protege developers to stop using java.io.File and start using this.getClass().getClassLoader().getResource instead, 
				pluginDir = new File(this.getClass().getClassLoader().getResource(DEFAULTPROTEGEPLUGINDIR).toURI()).getCanonicalPath();
			} catch (Throwable e) {
				e.printStackTrace();
				// use default value instead
				pluginDir = DEFAULTPROTEGEPLUGINDIR;
			}
		}
		return pluginDir;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#setProtegePluginDir(java.lang.String)
	 */
	public void setProtegePluginDir(String pluginDir) {
		this.pluginDir = pluginDir;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#getProtegeBundleDir()
	 */
	public String getProtegeBundleDir() {
		if (bundleDir == null || bundleDir.length() <= 0) {
			// search for resources (this is to solve JPF directory hierarchy using JPF classloaders)
//			try {
//				// We are using the File class because protege seems not to understand URL-normalized formats (i.e. those using '%')
//				// TODO ask protege developers to stop using java.io.File and start using this.getClass().getClassLoader().getResource instead, 
//				bundleDir = new File(this.getClass().getClassLoader().getResource(DEFAULTPROTEGEBUNDLEDIR).toURI()).getCanonicalPath();
//			} catch (Throwable e) {
//				e.printStackTrace();
//				// use default value instead
//				bundleDir = DEFAULTPROTEGEBUNDLEDIR;
//			}
			bundleDir = DEFAULTPROTEGEBUNDLEDIR;
		}
		return bundleDir;
	}

	/* (non-Javadoc)
	 * @see unbbayes.prs.mebn.ontology.protege.IBundleLauncher#setProtegeBundleDir(java.lang.String)
	 */
	public void setProtegeBundleDir(String bundleDir) {
		this.bundleDir = bundleDir;
	}

	/**
	 * @return the coreBundleNames
	 */
	public String[] getCoreBundleNames() {
		return coreBundleNames;
	}

	/**
	 * @param coreBundleNames the coreBundleNames to set
	 */
	public void setCoreBundleNames(String[] coreBundleNames) {
		this.coreBundleNames = coreBundleNames;
	}

	/**
	 * The bundles loaded by {@link #startProtegeBundles()}
	 * @return the loadedBundles
	 */
	public Collection<Bundle> getLoadedBundles() {
		return loadedBundles;
	}

	/**
	 * @param loadedBundles the loadedBundles to set
	 */
	public void setLoadedBundles(Collection<Bundle> loadedBundles) {
		this.loadedBundles = loadedBundles;
	}

	/**
	 * The OSGi framework to be used in {@link #startProtegeBundles()}
	 * @return the framework
	 */
	public Framework getFramework() {
		return framework;
	}

	/**
	 * The OSGi framework to be used in {@link #startProtegeBundles()}
	 * @param framework the framework to set
	 */
	public void setFramework(Framework framework) {
		this.framework = framework;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		try {
			return this.getProtegeBundle() + "[bundleDir=" + this.getProtegeBundleDir() + ", pluginDir=" + this.getProtegePluginDir() + ",bundleNames=" +  this.getCoreBundleNames() + ", ontologyURI=" + this.getDefaultOntolgyURI() + "]";
		} catch (Throwable e) {
			
		}
		return super.toString();
	}

//	/**
//	 * Use this method if you do not want to use {@link #getInstance()}, which returns a singleton instance.
//	 * @see java.lang.Object#clone()
//	 */
//	public IBundleLauncher clone() throws CloneNotSupportedException {
//		ProtegeBundleLauncher ret = new ProtegeBundleLauncher();
//		return ret;
//	}

}
