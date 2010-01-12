/**
 * 
 */
package unbbayes.util.extension.manager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.java.plugin.ObjectFactory;
import org.java.plugin.PluginManager;
import org.java.plugin.PluginManager.PluginLocation;
import org.java.plugin.standard.StandardPluginLocation;

import unbbayes.io.exception.UBIOException;
import unbbayes.util.ApplicationPropertyHolder;


/**
 * This is a class that holds a virtually static (singleton) instance
 * of plugin manager used by UnBBayes core.
 * This class can be used to access plugin utility.
 * @author Shou Matsumoto
 *
 */
public class UnBBayesPluginContextHolder {
	
	private String pluginsDirectoryName = null;
	private String pluginCoreID = null;
//	static {
//		pluginsDirectoryName = ApplicationPropertyHolder.getProperty().getProperty("unbbayes.util.extension.manager.UnBBayesPluginContextHolder.pluginsDirectoryName");
//		if (pluginsDirectoryName == null) {
//			pluginsDirectoryName = "plugins";
//		}
//		pluginCoreID = ApplicationPropertyHolder.getProperty().getProperty("unbbayes.util.extension.manager.UnBBayesPluginContextHolder.pluginCoreID");
//		if (pluginCoreID == null) {
//			pluginCoreID = "unbbayes.util.extension.core";
//		}
//	}
	
	/** Tells us if the plugin infrastructure is already initialized (published) */
	private boolean initialized = false;
	

	private List<OnReloadActionListener> onReloadListeners = new ArrayList<OnReloadActionListener>();
	
	private PluginManager pluginManager;
	
	/**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() 
     * or the first access to SingletonHolder.INSTANCE, not before.
     * This is used for creating singleton instances of Plugin context holder
     */
    private static class SingletonHolder { 
    	private static final UnBBayesPluginContextHolder INSTANCE = new UnBBayesPluginContextHolder();
    }
	
	/**
	 * Default constructor is protected in order to help
	 * subclasses.
	 * This constructor initializes the values of some attributes, such as
	 * pluginsDirectoryName, plugin manager or pluginCoreID
	 */
	protected UnBBayesPluginContextHolder() {
		this.setPluginsDirectoryName(ApplicationPropertyHolder.getProperty().getProperty("unbbayes.util.extension.manager.UnBBayesPluginContextHolder.pluginsDirectoryName"));
		if (this.getPluginsDirectoryName() == null) {
			this.setPluginsDirectoryName("plugins");
		}
		this.setPluginCoreID(ApplicationPropertyHolder.getProperty().getProperty("unbbayes.util.extension.manager.UnBBayesPluginContextHolder.pluginCoreID"));
		if (this.getPluginCoreID() == null) {
			this.setPluginCoreID("unbbayes.util.extension.core");
		}
		this.setPluginManager(ObjectFactory.newInstance().createManager());
	}
	
	/**
	 * Obtains a singleton instance of {@link UnBBayesPluginContextHolder}
	 * @return
	 */
	public static UnBBayesPluginContextHolder newInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * Return a singleton instance of plugin manager used
	 * by UnBBayes
	 * @return a singleton instance of plugin manager.
	 */
	public PluginManager getPluginManager() {
		return this.pluginManager;
	}
	
	/**
	 * Loads plugins situated at {@link #getPluginsDirectoryName()} folder
	 * and publish them (make them usable).
	 * @throws IOException
	 */
	public synchronized void publishPlugins() throws IOException {
		// search for files inside plugin directory
		File pluginsDir = new File(getPluginsDirectoryName());
		File[] plugins = pluginsDir.listFiles();

		// publish (load) plugins
		try {
	        Set<PluginLocation> locations = new HashSet<PluginLocation>(plugins.length);
	        for (File file : plugins) {
				PluginLocation location = StandardPluginLocation.create(file);
				if (location != null) {
					locations.add(location);
				}
			}
	        
	        // enable plugin
	        getPluginManager().publishPlugins(locations.toArray(new PluginLocation[locations.size()]));
	    } catch (Exception e) {
	    	throw new UBIOException(e);
	    }
	    
	    // if we published the plugins, they are initialized.
	    initialized = true;
	}
	
//	/**
//	 * Activates all plugins connected to a given extension point.
//	 * Basically, it does {@link #getPluginManager()}.activatePlugin() for
//	 * every connected extensions of a given extension point.
//	 * @param point : extension point to activate.
//	 */
//	public static void activateAllExtensionPoint(ExtensionPoint point) {
//		for (Extension extension : point.getConnectedExtensions()) {
//			PluginDescriptor descr = extension.getDeclaringPluginDescriptor();
//            try {
//				getPluginManager().activatePlugin(descr.getId());
//			} catch (PluginLifecycleException e) {
//				e.printStackTrace();
//				// we could not load this plugin, but we shall continue
//				continue;
//			}
//		}
//	}

	/**
	 * @return the pluginsDirectoryName
	 */
	public String getPluginsDirectoryName() {
		return pluginsDirectoryName;
	}

	/**
	 * @param pluginsDirectoryName the pluginsDirectoryName to set
	 */
	public void setPluginsDirectoryName(String pluginsDirectoryName) {
		this.pluginsDirectoryName = pluginsDirectoryName;
	}
	
	/**
	 * The ID of the core plugin.
	 * @return the pluginCoreID
	 */
	public String getPluginCoreID() {
		return pluginCoreID;
	}

	/**
	 * The ID of the core plugin.
	 * @param pluginCoreID the pluginCoreID to set
	 */
	public void setPluginCoreID(String newPluginCoreID) {
		pluginCoreID = newPluginCoreID;
	}

	/**
	 * Tells us if the plugin infrastructure is already initialized (published).
	 * {@link #publishPlugins()} initializes the plugins.
	 * @return true if the plugins were published. False otherwise.
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Triggers every listeners at {@link #getOnReloadListeners()}.
	 * @param origin : object that originated the notify event.
	 */
	public void notifyReload(Object origin) {
		for (OnReloadActionListener listener : this.getOnReloadListeners()) {
			listener.onReload(new EventObject(origin));
		}
	}
	
	/**
	 * Adds a new {@link OnReloadActionListener} into this plugin holder.
	 * These listeners will be trigged by {@link #notifyReload(Object)}
	 * @param listener
	 * @see #notifyReload(Object)
	 */
	public void addListener(OnReloadActionListener listener) {
		this.getOnReloadListeners().add(listener);
	}

	/**
	 * @return the onReloadListeners
	 */
	public List<OnReloadActionListener> getOnReloadListeners() {
		return onReloadListeners;
	}

	/**
	 * @param onReloadListeners the onReloadListeners to set
	 */
	public void setOnReloadListeners(List<OnReloadActionListener> onReloadListeners) {
		this.onReloadListeners = onReloadListeners;
	}
	
	/**
	 * An interface to represent a listener, containing {@link #onReload(EventObject)}
	 * which shall be called on every plugin reload events (e.g. by pressing
	 * "reload plugins" button).
	 * This is useful if you implement hotplugging, which needs to 
	 * reload something when an event happens.
	 * @author Shou Matsumoto
	 *
	 */
	public interface OnReloadActionListener extends EventListener {
		/**
		 * This method will be called by UnBBayes when a plugin reload
		 * action is called.
		 * @param eventObject
		 */
		public abstract void onReload(EventObject eventObject);
	}

	/**
	 * @param pluginManager the pluginManager to set
	 */
	protected void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

}
